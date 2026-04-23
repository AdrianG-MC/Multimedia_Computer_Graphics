package edu.up.cg.utils;

import java.io.File;

/**
 * Determines and creates the next numbered output project folder.
 *
 * The output root directory is created if it does not already exist.
 */
public class ProjectFolder {

    private static final String PREFIX = "Project_";

    private final File outputRoot;

    /**
     * @param outputRoot the root output directory )
     */
    public ProjectFolder(File outputRoot) {
        this.outputRoot = outputRoot;
    }

    /**
     * Creates and returns the next project directory
     *
     * Scans existing Project_N folders to find the highest N, then creates
     * Project_(N+1). Starts at Project_1 if no folders exist yet.
     *
     * @return the newly created directory
     * @throws RuntimeException if the directory cannot be created
     */
    public File create() {
        outputRoot.mkdirs();
        int  next       = findNextNumber();
        File projectDir = new File(outputRoot, PREFIX + next);
        if (!projectDir.mkdirs()) {
            throw new RuntimeException(
                "Could not create project folder: " + projectDir.getAbsolutePath());
        }
        return projectDir;
    }


    /**
     * Finds the highest existing Project_N number and returns N+1.
     * Returns 1 if no numbered folders exist yet.
     */
    private int findNextNumber() {
        int max= 0;
        File[] children = outputRoot.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && child.getName().startsWith(PREFIX)) {
                    try {
                        int n = Integer.parseInt(child.getName().substring(PREFIX.length()));
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return max + 1;
    }
}
