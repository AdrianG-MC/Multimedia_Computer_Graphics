# Video Creator

Turn your GPS-tagged photos and videos into a narrated, portrait-mode travel video automatically.

---

## What it does

1. Reads every photo / video from the `input/` folder.
2. Reads the GPS coordinates and capture date embedded in each file.
3. Sorts your media from oldest to newest.
4. Generates an AI opening image that represents the spirit of your journey.
5. Generates a spoken narration for each photo / video using Google TTS.
6. Fetches a map image that pins your first and last location.
7. Overlays an AI-generated inspirational phrase on the map.
8. Stitches everything into a single **1080 × 1920 portrait MP4**.
9. Saves the video and your original files to `output/Project_N/` and empties `input/`.

---

## Requirements

| Tool | Version | Install |
|------|---------|---------|
| Java JDK | 17 or later | https://adoptium.net |
| ExifTool | any | https://exiftool.org (macOS: `brew install exiftool`, Linux: `sudo apt install libimage-exiftool-perl`) |
| FFmpeg | 4.x or later | https://ffmpeg.org (macOS: `brew install ffmpeg`, Linux: `sudo apt install ffmpeg`) |

### API keys you need

| Key | Where to get it | Environment variable |
|-----|----------------|----------------------|
| Google AI Studio key | https://aistudio.google.com → "Get API key" | `GEMINI_API_KEY` |
| Geoapify key | https://myprojects.geoapify.com → create a project | `GEOAPIFY_API_KEY` |

> **Note:** The same `GEMINI_API_KEY` is also used for Google Cloud Text-to-Speech.  
> If you want a separate TTS key, set `GOOGLE_TTS_KEY` as well.

---

## Quick-start (step by step)

### Step 1 — Set your API keys as an Enviromental Variable

> If it does not recognize the key, put in into the Main class at line 49 and 50.  

### Step 2 — Add your media

Copy your GPS-tagged photos and/or videos into the `input/` folder.

Supported formats:
- Photos: **JPG, JPEG, PNG**
- Videos: **MP4, MOV, AVI**

> **Important:** every file must have GPS data in its EXIF metadata.  
> Most smartphone photos include GPS automatically. If yours don't,  
> run `exiftool yourphoto.jpg` and look for "GPS Latitude" in the output.

### Step 3 — Compile and Run

Compile the Main class and wait for the output.


The program prints its progress at each step. When it finishes you will see:
```
══════════════════════════════════════════════════════
  VIDEO CREATED SUCCESSFULLY!
  /path/to/output/Project_1/video.mp4
══════════════════════════════════════════════════════
```

### Step 5 — Find your video

```
output/
  Project_1/
    video.mp4          ← your finished video
    photo_001.jpg      ← your original input files (moved here)
    photo_002.jpg
    ...
```

---

## Folder structure

```
VideoCreator/
├── input/              ← put your media here before running
├── output/             ← auto-created; holds Project_N folders
├── src/
│   └── edu/up/cg/
│       ├── Main.java
│       ├── services/
│       │   ├── AIService.java
│       │   └── MapService.java
│       ├── tools/
│       │   ├── AudioGenerator.java
│       │   ├── MediaItem.java
│       │   ├── MediaLoader.java
│       │   ├── MediaSorter.java
│       │   ├── MetadataExtractor.java
│       │   └── VideoComposer.java
│       └── utils/
│           ├── ConsoleLogger.java
│           ├── EnvConfig.java
│           ├── ProcessRunner.java
│           ├── ProjectFolder.java
│           └── TempFolder.java
```

---

## Common errors and fixes

| Error message | Fix                                                                         |
|---------------|-----------------------------------------------------------------------------|
| `GEMINI_API_KEY is not set` | Put the key as hardcoded (only way i found)                                 |
| `ExifTool is not installed or not in PATH` | Install ExifTool and restart your terminal                                  |
| `No supported media files found in input/` | Copy your photos/videos into the `input/` folder                            |
| `No files could be processed` | Your files have no GPS data. Check with `exiftool yourfile.jpg`             |
| `Geoapify returned HTTP 401` | Your `GEOAPIFY_API_KEY` is wrong or not set                                 |
| `FFmpeg failed` | Install or update FFmpeg: `brew install ffmpeg` / `sudo apt install ffmpeg` |

---

## Tips

- Run the program multiple times — each run creates a new `Project_N` folder.
- If Imagen 3 is not available on your free-tier key, the program automatically uses a gradient placeholder for the opening image.
- If TTS fails, the program generates silent audio so the video still renders.
- The `input/` folder is always emptied after a successful run.
- If the image analysis by Gemini fails, its probably the permits of the key provided, it won't work.
