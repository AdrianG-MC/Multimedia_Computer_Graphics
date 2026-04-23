package edu.up.cg.services;

import edu.up.cg.tools.MediaItem;
import edu.up.cg.utils.EnvConfig;
import edu.up.cg.utils.TempFolder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * Wraps the Google Gemini REST API for three tasks:
 *
 *   1. generateOpeningImage  - asks Gemini for an image prompt, sends it to
 *                              Imagen 3, and saves the returned PNG. Falls back
 *                              to a Java2D gradient placeholder if Imagen is
 *                              unavailable on the free tier.
 *   2. generateNarrations    - produces a two-sentence travel narration for each
 *                              media item using Gemini 2.0 Flash.
 *   3. generateInspirationalPhrase - produces the closing quote for the map image.
 *
 * Required environment variable: GEMINI_API_KEY
 */
public class AIService {

    private static final Logger LOG = Logger.getLogger(AIService.class.getName());

    // Gemini REST base URL
    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    // Model identifiers
    private static final String TEXT_MODEL  = "gemini-2.0-flash:generateContent";
    private static final String IMAGE_MODEL = "imagen-3.0-generate-001:predict";

    // Portrait frame size used for the placeholder image
    private static final int FRAME_W = 1080;
    private static final int FRAME_H = 1920;

    private final String apiKey;
    private final HttpClient http;

    /**
     * Reads GEMINI_API_KEY from the environment.
     * @throws IllegalStateException if the variable is not set
     */
    public AIService() {
        this.apiKey = EnvConfig.requireEnv("GEMINI_API_KEY");
        this.http   = HttpClient.newHttpClient();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generates the opening image that represents the spirit of the journey.
     *
     * Steps:
     *   1. Ask Gemini for a vivid image-generation prompt.
     *   2. Pass that prompt to Imagen 3 and decode the PNG.
     *   3. Fall back to a gradient placeholder if Imagen fails.
     *
     * @param items      all media items (used to build the prompt)
     * @param tempFolder where to save the generated PNG
     * @return the opening PNG file
     * @throws Exception if even the fallback fails
     */
    public File generateOpeningImage(List<MediaItem> items, TempFolder tempFolder)
            throws Exception {

        File out = new File(tempFolder.getPath(), "opening_image.png");

        // Step 1: get a good Imagen prompt from Gemini
        String imagePrompt = generateEssencePrompt(items);

        // Step 2: try Imagen 3
        try {
            callImagen(imagePrompt, out);
            if (out.exists() && out.length() > 0) return out;
        } catch (Exception e) {
            LOG.warning("Imagen unavailable, using gradient placeholder: " + e.getMessage());
        }

        // Step 3: fallback gradient
        createGradientPlaceholder("Your Journey", out);
        return out;
    }

    /**
     * Generates a two-sentence narration for every media item.
     * For photos, the image is sent to Gemini Vision inline.
     * Items that fail individually get a plain metadata-based fallback.
     *
     * @param items sorted list of media items
     * @return narration strings in the same order as items
     * @throws Exception if the API cannot be reached at all
     */
    public List<String> generateNarrations(List<MediaItem> items) throws Exception {
        List<String> narrations = new ArrayList<>();
        for (MediaItem item : items) {
            try {
                String text = describeItem(item);
                item.setNarration(text);
                narrations.add(text);
                Thread.sleep(1500);
            } catch (Exception e) {
                LOG.warning("Narration failed for " + item.getFile().getName()
                        + ": " + e.getMessage());
                String fallback = buildFallback(item);
                item.setNarration(fallback);
                narrations.add(fallback);
            }
        }
        return narrations;
    }

    /**
     * Generates the inspirational closing phrase based on the first and last GPS location.
     *
     * @param first oldest media item
     * @param last  newest media item
     * @return one or two sentence inspirational phrase
     * @throws Exception if Gemini fails
     */
    public String generateInspirationalPhrase(MediaItem first, MediaItem last)
            throws Exception {

        String prompt =
            "Write a short, poetic, and inspirational travel phrase (1-2 sentences) " +
            "for a journey that started near coordinates (" +
            String.format("%.4f, %.4f", first.getLatitude(), first.getLongitude()) +
            ") and ended near (" +
            String.format("%.4f, %.4f", last.getLatitude(), last.getLongitude()) +
            "). Make it feel universal and meaningful. " +
            "Do NOT mention coordinates or GPS. Respond with ONLY the phrase.";

        return callGemini(prompt).trim();
    }

    // ── Private: text generation ──────────────────────────────────────────────

    /** Asks Gemini to describe one item in 2 sentences. Uses Vision for photos. */
    private String describeItem(MediaItem item) throws Exception {
        String prompt = String.format(
            "Describe this %s in exactly 2 natural, engaging sentences as if narrating " +
            "a travel video. Location: %.4f, %.4f. Date: %s. Be vivid and conversational.",
            item.getType() == MediaItem.Type.PHOTO ? "photo" : "video",
            item.getLatitude(), item.getLongitude(),
            item.getCaptureDate() != null ? item.getCaptureDate().toLocalDate() : "unknown");

        if (item.getType() == MediaItem.Type.PHOTO) {
            return callGeminiWithImage(prompt, item.getFile()).trim();
        } else {
            return callGemini(prompt).trim();
        }
    }

    /** Asks Gemini to produce a concise Imagen prompt from the media list. */
    private String generateEssencePrompt(List<MediaItem> items) throws Exception {
        StringBuilder sb = new StringBuilder(
            "Create a concise image generation prompt (max 200 characters) " +
            "for an AI image generator. The prompt should capture the essence " +
            "of a travel journey through these GPS locations:\n");

        for (MediaItem it : items) {
            sb.append(String.format("  - %s at (%.4f, %.4f)\n",
                    it.getFile().getName(), it.getLatitude(), it.getLongitude()));
        }
        sb.append("Make it a portrait-oriented, cinematic, artistic travel scene. " +
                  "Respond with ONLY the prompt text, nothing else.");

        return callGemini(sb.toString()).trim();
    }

    // ── Private: HTTP calls ───────────────────────────────────────────────────

    /** Sends a text-only prompt to Gemini and returns the response text. */
    private String callGemini(String prompt) throws Exception {
        String url  = BASE + TEXT_MODEL + "?key=" + apiKey;
        String body = buildTextRequest(prompt, null, null);
        return postAndExtractText(url, body);
    }

    /**
     * Sends a text prompt together with an inline base64 image (Gemini Vision).
     * Falls back to text-only if the image cannot be read.
     */
    private String callGeminiWithImage(String prompt, File imageFile) throws Exception {
        try {
            byte[] bytes = Files.readAllBytes(imageFile.toPath());
            String b64   = Base64.getEncoder().encodeToString(bytes);
            String mime  = guessMime(imageFile.getName());
            String url   = BASE + TEXT_MODEL + "?key=" + apiKey;
            String body  = buildTextRequest(prompt, b64, mime);
            return postAndExtractText(url, body);
        } catch (IOException e) {
            return callGemini(prompt); // fallback: text-only
        }
    }

    /**
     * Calls Imagen 3 with an image-generation prompt and writes the PNG to {@code out}.
     *
     * @throws Exception if Imagen returns an error or no image data
     */
    private void callImagen(String prompt, File out) throws Exception {
        String url  = BASE + IMAGE_MODEL + "?key=" + apiKey;
        String body =
            "{\n" +
            "  \"instances\": [{\"prompt\": \"" + escapeJson(prompt) + "\"}],\n" +
            "  \"parameters\": {\"sampleCount\": 1, \"aspectRatio\": \"9:16\"}\n" +
            "}";

        String response = post(url, body);
        String b64      = extractField(response, "bytesBase64Encoded");

        if (b64 == null || b64.isEmpty()) {
            throw new Exception("Imagen returned no image data.");
        }
        try {
            Files.write(out.toPath(), Base64.getDecoder().decode(b64));
        } catch (IOException e) {
            throw new Exception("Could not write Imagen output: " + e.getMessage(), e);
        }
    }

    /** POSTs a JSON body, expects HTTP 200, and returns the raw response body. */
    private String post(String url, String body) throws Exception {
        int maxRetries = 5;
        long defaultDelayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() == 200) {
                    return res.body();
                }

                // ── Manejo de 429 ─────────────────────────────
                if (res.statusCode() == 429) {
                    long delay = extractRetryDelay(res.body(), defaultDelayMs);

                    LOG.warning("Rate limit hit (429). Retry in " + delay + " ms (attempt " + attempt + ")");

                    Thread.sleep(delay);

                    // exponential backoff adicional
                    defaultDelayMs *= 2;
                    continue;
                }

                // Otros errores HTTP
                throw new Exception("Gemini API HTTP " + res.statusCode() + ": " + res.body());

            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();

                if (attempt == maxRetries) {
                    throw new Exception("HTTP request failed after retries: " + e.getMessage(), e);
                }

                LOG.warning("Request failed, retrying... (" + attempt + ")");
                Thread.sleep(defaultDelayMs);
                defaultDelayMs *= 2;
            }
        }

        throw new Exception("Max retries reached for Gemini API");
    }

    /**
     * Extracts retryDelay from the JSON (ej: "retryDelay": "25s") convert to ms.
     */
    private long extractRetryDelay(String json, long fallbackMs) {
        String delayStr = extractField(json, "retryDelay");

        if (delayStr == null) return fallbackMs;

        try {
            if (delayStr.endsWith("s")) {
                long seconds = Long.parseLong(delayStr.replace("s", "").trim());
                return seconds * 1000;
            }
        } catch (Exception ignored) {}

        return fallbackMs;
    }


    /** Calls post() and extracts the first "text" field from the Gemini response. */
    private String postAndExtractText(String url, String body) throws Exception {
        String response = post(url, body);
        String text = extractField(response, "text");
        if (text == null || text.isEmpty()) {
            throw new Exception("Empty text response from Gemini. Full response: " + response);
        }
        return text;
    }

    // ── Private: JSON helpers ─────────────────────────────────────────────────

    /**
     * Builds the generateContent JSON request body.
     * If b64 is non-null, an inlineData image part is included (Vision mode).
     */
    private String buildTextRequest(String prompt, String b64, String mime) {
        String imagePart = "";
        if (b64 != null) {
            imagePart = ",\n    {\"inlineData\": {\"mimeType\": \"" + mime +
                        "\", \"data\": \"" + b64 + "\"}}";
        }

        return "{\n" +
               "  \"contents\": [{\"parts\": [\n" +
               "    {\"text\": \"" + escapeJson(prompt) + "\"}" + imagePart + "\n" +
               "  ]}],\n" +
               "  \"generationConfig\": {\"temperature\": 0.7, \"maxOutputTokens\": 400}\n" +
               "}";
    }

    /**
     * Finds the first occurrence of {@code "field": "value"} in a JSON string
     * and returns the value, handling common escape sequences.
     * Simple hand-rolled parser — avoids any JSON library dependency.
     */
    private String extractField(String json, String field) {
        String key = "\"" + field + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        idx += key.length();

        // Skip whitespace then the opening quote
        while (idx < json.length() && json.charAt(idx) != '"') idx++;
        if (idx >= json.length()) return null;
        idx++; // past opening quote

        StringBuilder sb = new StringBuilder();
        while (idx < json.length() && json.charAt(idx) != '"') {
            char c = json.charAt(idx);
            if (c == '\\' && idx + 1 < json.length()) {
                char next = json.charAt(++idx);
                switch (next) {
                    case 'n':  sb.append('\n');  break;
                    case 't':  sb.append('\t');  break;
                    case '"':  sb.append('"');   break;
                    case '\\': sb.append('\\');  break;
                    default:   sb.append('\\'); sb.append(next); break;
                }
            } else {
                sb.append(c);
            }
            idx++;
        }
        return sb.toString();
    }

    /** Escapes special characters for embedding inside a JSON string literal. */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Guesses the MIME type from the file extension. */
    private String guessMime(String name) {
        String ext = name.toLowerCase();
        if (ext.endsWith(".png"))  return "image/png";
        if (ext.endsWith(".webp")) return "image/webp";
        if (ext.endsWith(".gif"))  return "image/gif";
        return "image/jpeg";
    }

    // ── Private: fallbacks ────────────────────────────────────────────────────

    /** Plain-text fallback when Gemini is unreachable for a particular item. */
    private String buildFallback(MediaItem item) {
        return String.format("A %s captured on %s near coordinates %.4f, %.4f.",
                item.getType() == MediaItem.Type.PHOTO ? "photo" : "video",
                item.getCaptureDate() != null
                        ? item.getCaptureDate().toLocalDate().toString() : "an unknown date",
                item.getLatitude(), item.getLongitude());
    }

    /**
     * Creates a 1080x1920 gradient PNG with a centered label using Java2D.
     * Used when Imagen 3 is not available on the current API key/tier.
     *
     * @param label text to display in the centre
     * @param out   destination PNG file
     * @throws Exception if ImageIO cannot write the file
     */
    private void createGradientPlaceholder(String label, File out) throws Exception {
        BufferedImage img = new BufferedImage(FRAME_W, FRAME_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Navy → deep purple vertical gradient
        GradientPaint gp = new GradientPaint(
            0, 0,      new Color(10, 10, 70),
            0, FRAME_H, new Color(70, 0, 100));
        g.setPaint(gp);
        g.fillRect(0, 0, FRAME_W, FRAME_H);

        // Draw a subtle decorative line
        g.setColor(new Color(255, 255, 255, 40));
        g.setStroke(new BasicStroke(2));
        g.drawLine(80, FRAME_H / 2 - 120, FRAME_W - 80, FRAME_H / 2 - 120);
        g.drawLine(80, FRAME_H / 2 + 120, FRAME_W - 80, FRAME_H / 2 + 120);

        // Centred white label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 90));
        FontMetrics fm = g.getFontMetrics();
        int x = (FRAME_W - fm.stringWidth(label)) / 2;
        int y = FRAME_H / 2 + fm.getAscent() / 2;
        g.drawString(label, x, y);

        // Sub-label
        g.setFont(new Font("SansSerif", Font.ITALIC, 42));
        fm = g.getFontMetrics();
        String sub = "A Visual Story";
        int sx = (FRAME_W - fm.stringWidth(sub)) / 2;
        g.setColor(new Color(200, 200, 255));
        g.drawString(sub, sx, y + 80);

        g.dispose();

        try {
            ImageIO.write(img, "PNG", out);
        } catch (IOException e) {
            throw new Exception("Could not write placeholder image: " + e.getMessage(), e);
        }
    }
}
