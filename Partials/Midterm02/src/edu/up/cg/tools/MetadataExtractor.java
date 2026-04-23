package edu.up.cg.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs ExifTool as a subprocess to read GPS coordinates and capture date
 * from each media file, then populates the corresponding MediaItem fields.
 *
 * ExifTool must be installed and in the system PATH.
 *
 */
public class MetadataExtractor {

    /** Date formats that ExifTool commonly returns. */
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    };

    /**
     * Extracts metadata for all files and wraps them in MediaItem objects.
     * Files that fail are skipped with a warning.
     *
     * @param files raw file list from MediaLoader
     * @return list of MediaItems with GPS and date populated
     * @throws Exception if ExifTool is missing or no file has valid GPS data
     */
    public List<MediaItem> extractAll(List<File> files) throws Exception {
        ensureExifToolAvailable();

        List<MediaItem> items = new ArrayList<>();
        for (File file : files) {
            MediaItem item = new MediaItem(file);
            try {
                extractOne(item);
                items.add(item);
                System.out.println("  " + item);
            } catch (Exception e) {
                System.out.println("  WARNING: Skipping " + file.getName() + " — " + e.getMessage());
            }
        }

        if (items.isEmpty()) {
            throw new Exception(
                "No files could be processed. Make sure your media files have GPS EXIF data.\n" +
                "Tip: most smartphone photos include GPS automatically.");
        }
        return items;
    }

    /**
     * Runs ExifTool on one file and fills the MediaItem's latitude, longitude,
     * and captureDate. Throws if the file has no GPS data.
     */
    public void extractOne(MediaItem item) throws Exception {
        List<String> lines = runExifTool(item.getFile());

        boolean latSet = false, lonSet = false;
        boolean latNeg = false, lonNeg = false;

        for (String line : lines) {
            if (!line.contains(":")) continue;

            // Split on first colon only
            int colon = line.indexOf(':');
            String key   = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();

            switch (key) {
                case "GPS Latitude":
                    item.setLatitude(parseGpsDegrees(value));
                    latSet = true;
                    break;
                case "GPS Longitude":
                    item.setLongitude(parseGpsDegrees(value));
                    lonSet = true;
                    break;
                case "GPS Latitude Ref":
                    latNeg = value.equalsIgnoreCase("South");
                    break;
                case "GPS Longitude Ref":
                    lonNeg = value.equalsIgnoreCase("West");
                    break;
                default:
                    // Capture the first date we find
                    if (item.getCaptureDate() == null &&
                        (key.equals("Date/Time Original") ||
                         key.equals("Create Date") ||
                         key.equals("Media Create Date"))) {
                        LocalDateTime dt = parseDate(value);
                        if (dt != null) item.setCaptureDate(dt);
                    }
                    break;
            }
        }

        if (latSet && latNeg) item.setLatitude(-Math.abs(item.getLatitude()));
        if (lonSet && lonNeg) item.setLongitude(-Math.abs(item.getLongitude()));

        if (!item.hasGps()) {
            throw new Exception("No GPS data found in " + item.getFile().getName());
        }
    }


    /** Runs "exiftool <file>" and returns its output as a list of lines. */
    private List<String> runExifTool(File file) throws Exception {
        List<String> lines = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("exiftool", file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            }
            p.waitFor();
        } catch (java.io.IOException e) {
            throw new Exception(
                "ExifTool not found. Please install it:\n" +
                "  macOS : brew install exiftool\n" +
                "  Linux : sudo apt install libimage-exiftool-perl\n" +
                "  Win   : https://exiftool.org", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("ExifTool was interrupted.", e);
        }
        return lines;
    }

    /** Verifies that ExifTool is installed and on the PATH. */
    private void ensureExifToolAvailable() throws Exception {
        try {
            Process p = new ProcessBuilder("exiftool", "-ver")
                    .redirectErrorStream(true).start();
            p.waitFor();
            if (p.exitValue() != 0) throw new Exception("Non-zero exit from exiftool -ver");
        } catch (Exception e) {
            throw new Exception(
                "ExifTool is not installed or not in PATH.\n" +
                "Install it from: https://exiftool.org");
        }
    }

    /**
     * Parses a GPS value from ExifTool output.
     * Handles "21.1458 deg" (decimal) and "21 deg 8' 44.88\"" (DMS) formats.
     */
    private double parseGpsDegrees(String value) {
        try {
            // Decimal format: "21.1458 deg N"
            String firstNum = value.split("[^0-9.]")[0];
            return Double.parseDouble(firstNum);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Parses a LocalDateTime from a raw ExifTool date string.
     * Returns null if no known format matches.
     */
    private LocalDateTime parseDate(String value) {
        // Strip timezone suffix like "+05:00"
        String cleaned = value.replaceAll("[+-]\\d{2}:\\d{2}$", "").trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDateTime.parse(cleaned, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }
}
