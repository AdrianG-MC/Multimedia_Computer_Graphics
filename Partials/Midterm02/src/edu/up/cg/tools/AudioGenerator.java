package edu.up.cg.tools;

import edu.up.cg.utils.EnvConfig;
import edu.up.cg.utils.ProcessRunner;
import edu.up.cg.utils.TempFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * Converts narration text to MP3 audio using the Google Cloud Text-to-Speech API,
 * then normalizes the result to meet YouTube loudness standards using FFmpeg.
 *
 * If the TTS API call fails the generator falls back to a silent MP3 so the
 * rest of the pipeline always has a valid audio file to work with.
 *
 * API key used: GEMINI_API_KEY (the same key works for Google TTS on the same project).
 * Alternatively set GOOGLE_TTS_KEY to use a dedicated TTS key.
 */
public class AudioGenerator {

    private static final Logger LOG = Logger.getLogger(AudioGenerator.class.getName());

    /** Google Cloud TTS REST endpoint. */
    private static final String TTS_URL =
            "https://texttospeech.googleapis.com/v1/text:synthesize?key=";

    /** FFmpeg loudnorm filter string targeting YouTube standard. */
    private static final String LOUDNORM = "loudnorm=I=-14:TP=-1.5:LRA=7";

    private final String apiKey;
    private final HttpClient http;

    /**
     * Reads the TTS key from the environment.
     * Prefers GOOGLE_TTS_KEY; falls back to GEMINI_API_KEY.
     */
    public AudioGenerator() {
        String key = System.getenv("GOOGLE_TTS_KEY");
        if (key == null || key.isBlank()) {
            key = EnvConfig.requireEnv("GEMINI_API_KEY");
        }
        this.apiKey = key;
        this.http   = HttpClient.newHttpClient();
    }

    /**
     * Synthesises one narration string into a normalised MP3 file.
     *
     * @param text       the narration text (may be null/blank → silent audio)
     * @param tempFolder working directory for the output file
     * @param baseName   filename without extension, e.g. "audio_01"
     * @return the MP3 file (always exists, possibly silent)
     * @throws Exception if even the silence fallback fails
     */
    public File synthesize(String text, TempFolder tempFolder, String baseName)
            throws Exception {

        File mp3 = new File(tempFolder.getPath(), baseName + ".mp3");

        if (text == null || text.isBlank()) {
            generateSilence(mp3, 3.0);
            return mp3;
        }

        try {
            byte[] audioBytes = callTts(text);
            Files.write(mp3.toPath(), audioBytes);
            normalizeLoudness(mp3, tempFolder);
            return mp3;
        } catch (Exception e) {
            LOG.warning("TTS failed (" + e.getMessage() + "), using silence fallback.");
        }

        // Fallback: silent audio whose length matches the estimated narration duration
        generateSilence(mp3, estimateDuration(text));
        return mp3;
    }

    /**
     * Synthesises a list of narration strings — one MP3 per item.
     *
     * @param texts      narration strings in the same order as the media items
     * @param tempFolder working directory
     * @return list of MP3 files in the same order as {@code texts}
     * @throws Exception on critical failure
     */
    public List<File> synthesizeAll(List<String> texts, TempFolder tempFolder)
            throws Exception {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String baseName = String.format("audio_%02d", i + 1);
            files.add(synthesize(texts.get(i), tempFolder, baseName));
            System.out.println("  Audio " + (i + 1) + "/" + texts.size() + " ready.");
        }
        return files;
    }

    /**
     * Creates a silent MP3 of the requested duration using FFmpeg's anullsrc source.
     *
     * @param output  destination file
     * @param seconds silence duration
     * @throws Exception if FFmpeg fails
     */
    public void generateSilence(File output, double seconds) throws Exception {
        String[] cmd = {
            "ffmpeg", "-y",
            "-f", "lavfi",
            "-i", "anullsrc=r=44100:cl=stereo",
            "-t", String.valueOf(seconds),
            "-q:a", "2",
            output.getAbsolutePath()
        };
        try {
            ProcessRunner.run(cmd, "generate silence");
        } catch (Exception e) {
            throw new Exception("Could not generate silence audio: " + e.getMessage(), e);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Calls the Google Cloud TTS REST API and returns the decoded MP3 bytes.
     *
     * @param text narration text (max ~5 000 characters for the basic tier)
     * @return raw MP3 bytes
     * @throws Exception on HTTP error or empty audio response
     */
    private byte[] callTts(String text) throws Exception {
        String body = buildTtsRequest(text);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(TTS_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                throw new Exception("TTS HTTP " + res.statusCode() + ": " + res.body());
            }

            String b64 = extractAudioContent(res.body());
            if (b64 == null || b64.isEmpty()) {
                throw new Exception("TTS returned no audio data.");
            }
            return Base64.getDecoder().decode(b64);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new Exception("TTS HTTP request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Applies FFmpeg loudness normalisation in-place (overwrites the MP3 file).
     * Failures are non-fatal — the un-normalised file is kept as-is.
     */
    private void normalizeLoudness(File mp3, TempFolder tempFolder) {
        File tmp = new File(tempFolder.getPath(), "norm_" + mp3.getName());
        String[] cmd = {
            "ffmpeg", "-y",
            "-i", mp3.getAbsolutePath(),
            "-af", LOUDNORM,
            "-ar", "44100",
            "-ac", "2",
            tmp.getAbsolutePath()
        };
        try {
            ProcessRunner.run(cmd, "loudness normalisation");
            Files.move(tmp.toPath(), mp3.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOG.warning("Loudness normalisation skipped: " + e.getMessage());
            tmp.delete();
        }
    }

    /**
     * Builds the JSON body for the Google Cloud TTS API.
     * Uses the en-US-Neural2-D voice for natural-sounding narration.
     */
    private String buildTtsRequest(String text) {
        // Escape characters that would break the JSON string
        String escaped = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");

        return "{\n" +
               "  \"input\": {\"text\": \"" + escaped + "\"},\n" +
               "  \"voice\": {\n" +
               "    \"languageCode\": \"en-US\",\n" +
               "    \"name\": \"en-US-Neural2-D\",\n" +
               "    \"ssmlGender\": \"NEUTRAL\"\n" +
               "  },\n" +
               "  \"audioConfig\": {\n" +
               "    \"audioEncoding\": \"MP3\",\n" +
               "    \"speakingRate\": 0.95,\n" +
               "    \"sampleRateHertz\": 44100\n" +
               "  }\n" +
               "}";
    }

    /**
     * Extracts the base64 "audioContent" value from a TTS API JSON response.
     * Uses simple string parsing — no external JSON library needed.
     */
    private String extractAudioContent(String json) {
        String key = "\"audioContent\":";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        idx += key.length();
        // Skip whitespace and opening quote
        while (idx < json.length() && json.charAt(idx) != '"') idx++;
        if (idx >= json.length()) return null;
        idx++; // skip opening quote
        int end = json.indexOf('"', idx);
        return end < 0 ? null : json.substring(idx, end);
    }

    /**
     * Estimates spoken duration at ~3 words per second (minimum 3 seconds).
     */
    private double estimateDuration(String text) {
        int words = text.trim().split("\\s+").length;
        return Math.max(3.0, words / 3.0);
    }
}
