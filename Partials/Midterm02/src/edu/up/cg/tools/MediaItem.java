package edu.up.cg.tools;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Represents a single media file (photo or video) together with
 * the metadata extracted from it: GPS coordinates, capture date,
 * and the AI-generated narration.
 *
 * Implements Comparable so Collections.sort() gives chronological order.
 */
public class MediaItem implements Comparable<MediaItem> {

    /** Whether this item is a photo or a video clip. */
    public enum Type { PHOTO, VIDEO }


    private final File file;
    private final Type type;

    private double        latitude;
    private double        longitude;
    private LocalDateTime captureDate;

    private String narration;

    /**
     * @param file the media file on disk; type is inferred from its extension
     */
    public MediaItem(File file) {
        this.file = file;
        this.type = detectType(file.getName());
    }


    private static Type detectType(String name) {
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0) ext = name.substring(dot + 1).toLowerCase();
        if (ext.equals("mp4") || ext.equals("mov") || ext.equals("avi")) return Type.VIDEO;
        return Type.PHOTO;
    }


    public LocalDateTime getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(LocalDateTime captureDate) {
        this.captureDate = captureDate;
    }

    public File getFile() {
        return file;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public Type getType() {
        return type;
    }

    /** Returns true if GPS coordinates have been set (non-zero). */
    public boolean hasGps() {
        return latitude != 0.0 || longitude != 0.0;
    }

    /**
     * Oldest-first comparison based on captureDate.
     * Items with no date sort to the end.
     */
    @Override
    public int compareTo(MediaItem other) {
        if (this.captureDate == null && other.captureDate == null) return 0;
        if (this.captureDate == null) return 1;
        if (other.captureDate == null) return -1;
        return this.captureDate.compareTo(other.captureDate);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | lat=%.4f lon=%.4f | %s",
                type, file.getName(), latitude, longitude, captureDate);
    }
}
