package edu.up.cg.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scans the input/ folder for supported media files and, at the end
 * of the pipeline, moves those files into the project output folder
 * so the input/ folder is left empty for the next run.
 */
public class MediaLoader {
    private static final String[] PHOTO_EXT = {"jpg", "jpeg", "png"};
    private static final String[] VIDEO_EXT = {"mp4", "mov", "avi"};

    private final File inputDir;

    /**
     * @param inputDir the folder to scan; created automatically if it does not exist
     */
    public MediaLoader(File inputDir) {
        this.inputDir = inputDir;
        if (!inputDir.exists()) {
            inputDir.mkdirs();
            System.out.println("Created input folder: " + inputDir.getPath());
        }
    }

    /**
     * Lists every supported media file in the input directory.
     *
     * @return non-empty list of files
     * @throws Exception if the folder can't be read or contains no supported files
     */
    public List<File> load() throws Exception {
        File[] all = inputDir.listFiles();
        if (all == null) {
            throw new Exception("Cannot read input directory: " + inputDir.getAbsolutePath());
        }

        Arrays.sort(all); // deterministic order before metadata extraction

        List<File> accepted = new ArrayList<>();
        for (File f : all) {
            if (f.isFile() && isSupported(f)) {
                accepted.add(f);
                System.out.println("  Found: " + f.getName());
            } else if (f.isFile()) {
                System.out.println("  Skipped (unsupported): " + f.getName());
            }
        }

        if (accepted.isEmpty()) {
            throw new Exception(
                "No supported media files found in " + inputDir.getAbsolutePath() +
                "\nSupported formats: JPG, JPEG, PNG, MP4, MOV, AVI");
        }
        return accepted;
    }

    /**
     * Moves every file that is still in the input folder to {@code destination}.
     * Call this at the very end of the pipeline to empty the input folder.
     *
     * @param destination target folder (must already exist)
     * @throws Exception if any move fails
     */
    public void moveFilesTo(File destination) throws Exception {
        File[] files = inputDir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isFile()) {
                File target = new File(destination, f.getName());
                try {
                    Files.move(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("  Moved: " + f.getName());
                } catch (IOException e) {
                    throw new Exception("Could not move " + f.getName() + ": " + e.getMessage(), e);
                }
            }
        }
    }


    /** Returns true if the file has a supported photo or video extension. */
    public static boolean isSupported(File file) {
        String ext = getExtension(file.getName());
        for (String p : PHOTO_EXT) if (ext.equals(p)) return true;
        for (String v : VIDEO_EXT) if (ext.equals(v)) return true;
        return false;
    }

    /** Returns the lowercase extension without the dot, or "" if none. */
    public static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
    }
}
