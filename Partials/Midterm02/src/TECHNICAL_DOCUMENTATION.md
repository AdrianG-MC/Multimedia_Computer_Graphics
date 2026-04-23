# Technical Documentation

## Overview

Video Creator is a command-line Java application that reads GPS-tagged media files,
calls external APIs (Gemini, Google TTS, Geoapify), and uses FFmpeg to produce a
narrated, portrait-mode travel video.

---

## Architecture

The project is split into three packages, each with a single responsibility:

```
edu.up.cg
├── Main.java              — orchestrates the full pipeline
├── services/
│   ├── AIService.java     — Gemini text generation + Imagen image generation
│   └── MapService.java    — Geoapify static map + Java2D phrase overlay
├── tools/
│   ├── MediaItem.java     — data model for one media file
│   ├── MediaLoader.java   — scans input/ and moves files to output
│   ├── MediaSorter.java   — chronological sort
│   ├── MetadataExtractor.java — ExifTool subprocess wrapper
│   ├── AudioGenerator.java   — Google Cloud TTS + FFmpeg loudness normalisation
│   └── VideoComposer.java    — FFmpeg clip builder and concatenator
└── utils/
    ├── ConsoleLogger.java — coloured ANSI console output
    ├── EnvConfig.java     — safe environment variable reader
    ├── ProcessRunner.java — generic subprocess runner
    ├── ProjectFolder.java — auto-numbered output folder creator
    └── TempFolder.java    — self-cleaning temporary directory
```

---

## Pipeline (Main.java)

```
Step 1  MediaLoader.load()
           Scans input/ for JPG, JPEG, PNG, MP4, MOV, AVI files.

Step 2  MetadataExtractor.extractAll()
           Runs `exiftool <file>` for each file.
           Parses GPS Latitude/Longitude/Ref and Date/Time Original.
           Files without GPS are skipped with a warning.

Step 3  MediaSorter.sort()
           Returns a new list sorted by captureDate (oldest first).
           Items with no date sort to the end.

Step 4  TempFolder("vc_work_")
           Creates a system temp directory for intermediate files.

Step 5  AIService.generateOpeningImage()
           Asks Gemini for a cinematic Imagen prompt, then calls Imagen 3.
           Falls back to a Java2D gradient PNG if Imagen is unavailable.

        AIService.generateNarrations()
           Calls Gemini 2.0 Flash once per item (Vision for photos, text-only
           for videos). Builds a metadata-based string as fallback.

        AIService.generateInspirationalPhrase()
           Sends first/last GPS coordinates to Gemini; returns 1-2 sentences.

Step 6  AudioGenerator.synthesize() / synthesizeAll()
           POSTs each narration string to Google Cloud TTS.
           Decodes the base64 MP3 response and normalises loudness with FFmpeg.
           Falls back to silent audio on TTS failure.

Step 7  MapService.generateMap()
           Builds a Geoapify Static Maps URL with two markers.
           Downloads the PNG via HTTP GET.

        MapService.overlayPhrase()
           Reads the PNG with ImageIO, draws a semi-transparent banner
           at the bottom, word-wraps the phrase, and writes the file back.

Step 8  VideoComposer.compose()
           Builds one MP4 clip per segment:
             - Opening: still image looped for 4 s
             - Each media item: still (5 s) or re-encoded video
             - Closing map: still image looped for 8 s
           Writes a concat_list.txt and runs ffmpeg -f concat.
           A final loudness normalisation pass (loudnorm) is applied.

Step 9  MediaLoader.moveFilesTo(projectDir)
           Moves every file remaining in input/ to the project output folder.
           TempFolder.delete() runs in the finally block.
```

---

## Classes

### MediaItem

| Field | Type | Set by |
|-------|------|--------|
| file | File | constructor |
| type | Type (PHOTO/VIDEO) | constructor (from extension) |
| latitude | double | MetadataExtractor |
| longitude | double | MetadataExtractor |
| captureDate | LocalDateTime | MetadataExtractor |
| narration | String | AIService |

Implements `Comparable<MediaItem>` — compares by `captureDate`, oldest first.

---

### MediaLoader

- `load()` — returns a `List<File>` of supported files sorted by filename.
- `moveFilesTo(File dest)` — moves remaining files in `input/` to `dest`.
- `isSupported(File)` — static helper checking extension against allowed lists.

---

### MetadataExtractor

Runs `exiftool <filepath>` and parses the output line-by-line.

Key parsing rules:
- `GPS Latitude` / `GPS Longitude` → `parseGpsDegrees()` extracts the leading number.
- `GPS Latitude Ref` = "South" → negate latitude; `GPS Longitude Ref` = "West" → negate longitude.
- Date fields tried in order: `Date/Time Original`, `Create Date`, `Media Create Date`.
- Date formats: `yyyy:MM:dd HH:mm:ss` and `yyyy-MM-dd HH:mm:ss`.

---

### AIService

**Text generation** — `POST /v1beta/models/gemini-2.0-flash:generateContent`

Request body (simplified):
```json
{
  "contents": [{ "parts": [{ "text": "<prompt>" }] }],
  "generationConfig": { "temperature": 0.7, "maxOutputTokens": 400 }
}
```

For photos, an `inlineData` part with the base64-encoded image is appended
(Gemini Vision mode).

**Image generation** — `POST /v1beta/models/imagen-3.0-generate-001:predict`

Request body:
```json
{
  "instances": [{ "prompt": "<imagen_prompt>" }],
  "parameters": { "sampleCount": 1, "aspectRatio": "9:16" }
}
```

Response field extracted: `bytesBase64Encoded` → decoded and written as PNG.

**Fallback** — if Imagen returns no data or returns an error (common on free tier),
`createGradientPlaceholder()` draws a 1080×1920 navy-to-purple gradient PNG with
Java2D and writes it via `ImageIO.write()`.

**JSON parsing** — no external library. `extractField(json, fieldName)` scans for
`"fieldName": "` and reads until the next unescaped `"`, handling `\n`, `\t`, `\"`, `\\`.

---

### MapService

**URL format:**
```
https://maps.geoapify.com/v1/staticmap
  ?style=osm-bright
  &width=1080&height=1920
  &center=lonlat:<centerLon>,<centerLat>
  &zoom=<auto>
  &marker=lonlat:<lon1>,<lat1>;color:%2300cc55;size:large;type:material
  &marker=lonlat:<lon2>,<lat2>;color:%23ff3333;size:large;type:material
  &apiKey=<GEOAPIFY_API_KEY>
```

`%23` is the URL-encoded `#` character required by Geoapify for hex colour codes.

**Zoom estimation** — based on the maximum lat/lon delta between the two points:

| Delta | Zoom |
|-------|------|
| < 0.01° | 15 |
| < 0.1° | 12 |
| < 0.5° | 10 |
| < 2° | 8 |
| < 10° | 6 |
| < 30° | 4 |
| ≥ 30° | 2 |

**Phrase overlay** — `Graphics2D` draws:
1. Gradient fade from transparent to semi-opaque black over the bottom 380 px.
2. White italic phrase, word-wrapped to stay within 60 px margins.
3. Green "● Start" and red "● End" legend labels near the bottom edge.

---

### AudioGenerator

**TTS endpoint:** `https://texttospeech.googleapis.com/v1/text:synthesize?key=<KEY>`

Voice: `en-US-Neural2-D` (neutral, natural-sounding).

**Loudness normalisation (FFmpeg) for the final video (might be used in the future)**
```
ffmpeg -i input.mp3 -af "loudnorm=I=-14:TP=-1.5:LRA=7" -ar 44100 -ac 2 output.mp3
```
Targets:
- Integrated loudness: −14 LUFS (YouTube target)
- True peak: −1.5 dBTP (within −2 to −1 spec)
- LRA: 7 LU (within 5–10 LU spec)

Silence fallback uses:
```
ffmpeg -f lavfi -i anullsrc=r=44100:cl=stereo -t <seconds> output.mp3
```

---

### VideoComposer

**Scale + pad filter (preserves aspect ratio, fills 1080×1920 with black):**
```
scale=1080:1920:force_original_aspect_ratio=decrease,
pad=1080:1920:(ow-iw)/2:(oh-ih)/2:black
```

**Still image clip command:**
```
ffmpeg -loop 1 -i image.png -i audio.mp3
  -vf <SCALE_PAD> -c:v libx264 -preset fast -crf 23
  -c:a aac -b:a 192k -t <duration> -r 30 -pix_fmt yuv420p -shortest
  clip.mp4
```

**Video clip command (discards original audio, uses narration):**
```
ffmpeg -i video.mp4 -i audio.mp3
  -vf <SCALE_PAD> -map 0:v:0 -map 1:a:0
  -c:v libx264 -preset fast -crf 23
  -c:a aac -b:a 192k -t <duration> -r 30 -pix_fmt yuv420p -shortest
  clip.mp4
```


---

### ProcessRunner

Generic subprocess utility. Merges stderr into stdout with `redirectErrorStream(true)`
to prevent the pipe buffer from filling and blocking the JVM thread. Throws
`ProcessException` (a checked exception subclassing `Exception`) on non-zero exit.

---

### TempFolder

Wraps `Files.createTempDirectory(prefix)`. `delete()` recursively removes all
contents then the directory itself. Always called from a `finally` block in `Main`.

---

### ProjectFolder

Scans `output/` for directories matching `Project_\d+`, finds the maximum N,
and creates `Project_(N+1)`. Starts at `Project_1` on the first run.

---

## Error handling strategy

- **API failures** — Gemini text and TTS failures are caught per-item; a fallback
  description or silent audio is substituted so the pipeline never stops (Theoretically, in practice,
  the API doesnt send anything and brakes the whole program :[sad_face]: ).
- **Imagen failure** — caught at the image-generation step; a gradient PNG is used.
- **ExifTool / FFmpeg missing** — caught in `ProcessRunner`; pipeline stops with a
  friendly message indicating which tool to install.
- **No GPS in file** — file is skipped with a warning; pipeline stops only if *no*
  files remain.
- **TempFolder** — `delete()` runs in the `finally` block even on failure.


---

## External dependencies

No third-party JAR files are required. The project uses only:
- Java SE standard libraries
- External CLI tools: `exiftool`, `ffmpeg`, `ffprobe` (must be on PATH)
- REST APIs: Google Generative Language, Google Cloud TTS, Geoapify Static Maps

## Clarification

This project should work if a Gemini API Key with the necessary permits its provided. By now,
i could not run it properly.
