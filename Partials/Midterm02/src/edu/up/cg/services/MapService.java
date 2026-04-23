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

/**
 * Downloads a static map image from the Geoapify Static Maps API with two
 * distinct pin markers (green = start, red = end), then uses Java2D to
 * overlay the AI-generated inspirational phrase onto the image.
 *
 * Required environment variable: GEOAPIFY_API_KEY
 */
public class MapService {

    private static final String API_URL = "https://maps.geoapify.com/v1/staticmap";

    // Portrait frame — matches the video frame
    private static final int MAP_W    = 1080;
    private static final int MAP_H    = 1920;

    // Height of the semi-transparent text banner at the bottom
    private static final int BANNER_H = 380;

    private final String apiKey;
    private final HttpClient http;

    /**
     * Reads GEOAPIFY_API_KEY from the environment.
     * @throws IllegalStateException if the variable is not set
     */
    public MapService() {
        this.apiKey = EnvConfig.requireEnv("GEOAPIFY_API_KEY");
        this.http   = HttpClient.newHttpClient();
    }

    /**
     * Downloads a static map image with two colored pin markers.
     *
     *   Green mark as first (oldest) location
     *   red mark as last (newest) location
     *
     * @param first      oldest media item
     * @param last       newest media item
     * @param tempFolder where to save the PNG
     * @return the downloaded PNG file
     * @throws Exception if the API call or file write fails
     */
    public File generateMap(MediaItem first, MediaItem last, TempFolder tempFolder)
            throws Exception {

        File out = new File(tempFolder.getPath(), "map_image.png");

        String url = buildUrl(
            first.getLatitude(),  first.getLongitude(),
            last.getLatitude(),   last.getLongitude());

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (res.statusCode() != 200) {
                throw new Exception("Geoapify returned HTTP " + res.statusCode() +
                    ". Check your GEOAPIFY_API_KEY.");
            }
            Files.write(out.toPath(), res.body());

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new Exception("Map download failed: " + e.getMessage(), e);
        }

        return out;
    }

    /**
     * Overlays a semi-transparent banner and the inspirational phrase onto the
     * map PNG file in place. Also draws a Start / End legend.
     *
     * @param mapFile the PNG file to modify (overwritten)
     * @param phrase  the inspirational text to render
     * @throws Exception if the image cannot be read or written
     */
    public void overlayPhrase(File mapFile, String phrase) throws Exception {
        try {
            BufferedImage img = ImageIO.read(mapFile);
            if (img == null) throw new Exception("Could not read map image from disk.");

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = img.getWidth();
            int h = img.getHeight();

            // Semi-transparent dark gradient banner at the bottom
            GradientPaint fade = new GradientPaint(
                0, h - BANNER_H,      new Color(0, 0, 0, 0),
                0, h - BANNER_H + 80, new Color(0, 0, 0, 200));
            g.setPaint(fade);
            g.fillRect(0, h - BANNER_H, w, BANNER_H);
            // solid dark base below the gradient
            g.setColor(new Color(0, 0, 0, 210));
            g.fillRect(0, h - BANNER_H + 80, w, BANNER_H - 80);

            // Inspirational phrase (word-wrapped)
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.ITALIC, 52));
            drawWrapped(g, phrase, 60, h - BANNER_H + 110, w - 120, 66);

            // Start / End legend
            g.setFont(new Font("SansSerif", Font.BOLD, 38));
            g.setColor(new Color(30, 210, 100));  // green
            g.drawString("● Start", 60, h - 50);
            g.setColor(new Color(255, 70, 70));   // red
            g.drawString("● End",  340, h - 50);

            g.dispose();
            ImageIO.write(img, "PNG", mapFile);

        } catch (IOException e) {
            throw new Exception("Failed to overlay phrase on map: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the Geoapify Static Maps URL with two markers and an automatic zoom.
     *
     * Marker format accepted by Geoapify:
     *   lonlat:<lon>,<lat>;color:%23<hex>;size:large;type:material
     * (%23 is the URL-encoded '#')
     */
    private String buildUrl(double lat1, double lon1, double lat2, double lon2) {
        double centerLat = (lat1 + lat2) / 2.0;
        double centerLon = (lon1 + lon2) / 2.0;
        int    zoom      = estimateZoom(lat1, lon1, lat2, lon2);

        String startMarker = String.format(
            "lonlat:%f,%f;color:%%2300cc55;size:large;type:material", lon1, lat1);
        String endMarker = String.format(
            "lonlat:%f,%f;color:%%23ff3333;size:large;type:material", lon2, lat2);

        return API_URL +
               "?style=osm-bright" +
               "&width="  + MAP_W +
               "&height=" + MAP_H +
               "&center=lonlat:" + String.format("%f,%f", centerLon, centerLat) +
               "&zoom=" + zoom +
               "&marker=" + startMarker +
               "&marker=" + endMarker +
               "&apiKey=" + apiKey;
    }

    /**
     * Estimates a good Geoapify zoom level so both markers fit comfortably.
     * Based on the largest coordinate delta between the two points.
     */
    private int estimateZoom(double lat1, double lon1, double lat2, double lon2) {
        double delta = Math.max(Math.abs(lat1 - lat2), Math.abs(lon1 - lon2));
        if (delta < 0.01) return 15;
        if (delta < 0.1)  return 12;
        if (delta < 0.5)  return 10;
        if (delta < 2.0)  return 8;
        if (delta < 10.0) return 6;
        if (delta < 30.0) return 4;
        return 2;
    }

    /**
     * Word-wraps {@code text} inside a bounding box and draws it.
     *
     * @param g          graphics context
     * @param text       text to render
     * @param x          left edge
     * @param y          top-left y of the first line
     * @param maxWidth   max line width in pixels
     * @param lineHeight vertical spacing in pixels
     */
    private void drawWrapped(Graphics2D g, String text, int x, int y,
                             int maxWidth, int lineHeight) {
        FontMetrics fm    = g.getFontMetrics();
        String[]    words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int curY = y;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                g.drawString(line.toString(), x, curY);
                line = new StringBuilder(word);
                curY += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), x, curY);
        }
    }
}
