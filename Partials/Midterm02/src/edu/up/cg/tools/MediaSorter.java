package edu.up.cg.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sorts a list of MediaItems chronologically (oldest capture date first).
 * Items with no capture date are placed at the end.
 * The original list is never modified , a new list is returned.
 */
public class MediaSorter {

    /**
     * Returns a new list sorted from oldest to newest.
     * Relies on {@link MediaItem#compareTo(MediaItem)}.
     *
     * @param items unsorted list of media items
     * @return new list sorted chronologically
     */
    public List<MediaItem> sort(List<MediaItem> items) {
        List<MediaItem> sorted = new ArrayList<>(items);
        Collections.sort(sorted);
        return sorted;
    }
}
