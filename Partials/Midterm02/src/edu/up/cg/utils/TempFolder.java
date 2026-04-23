package edu.up.cg.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Creates and manages a temporary directory used during video processing.
 * The directory holds intermediate files (clips, audio, images) that can be
 * safely deleted once the final video has been rendered.
 *
 * Always call {@link #delete()} in a finally block:
 *
 */
public class TempFolder {

    private final File directory;

    /**
     * Creates a new temporary directory in the system temp folder.
     *
     * @param prefix prefix for the directory name, e.g. "vc_work_"
     * @throws RuntimeException if the directory cannot be created
     */
    public TempFolder(String prefix) {
        try {
            directory = Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temp directory: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the temporary directory as a {@link File}.
     * Use this to build paths for intermediate files:
     *   new File(temp.getPath(), "clip_01.mp4")
     */
    public File getPath() {
        return directory;
    }

    /**
     * Recursively deletes the temp directory and all its contents.
     * Errors are silently ignored (best-effort cleanup).
     */
    public void delete() {
        deleteRecursive(directory);
    }


    private void deleteRecursive(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteRecursive(child);
                } else {
                    child.delete();
                }
            }
        }
        dir.delete();
    }
}
