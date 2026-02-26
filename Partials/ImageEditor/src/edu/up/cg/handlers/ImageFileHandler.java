package edu.up.cg.handlers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFileHandler {
    private static final String INPUT_FOLDER = "src/edu/up/cg/Images/Input";
    private static final String OUTPUT_FOLDER = "src/edu/up/cg/Images/Edited";

    /**
     * Lists available images in Input folder.
     */
    public File[] listInputImages() {

        File folder = new File(INPUT_FOLDER);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Input folder not found: " + INPUT_FOLDER);
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".bmp")
        );

        if (files == null || files.length == 0) {
            throw new RuntimeException("No images found in Input folder.");
        }

        return files;
    }

    /**
     * Loads selected image.
     */
    public BufferedImage load(File file) throws IOException {

        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new IOException("Invalid or corrupted image.");
        }

        return image;
    }

    /**
     * Saves image automatically inside Edited folder
     * using original name + _edited
     */
    public void save(BufferedImage image, String originalName) throws IOException {

        File outputDir = new File(OUTPUT_FOLDER);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String nameWithoutExtension = originalName.substring(0, originalName.lastIndexOf("."));
        String extension = originalName.substring(originalName.lastIndexOf(".") + 1);

        String newName = nameWithoutExtension + "_edited." + extension;

        File outputFile = new File(outputDir, newName);

        boolean success = ImageIO.write(image, extension, outputFile);

        if (!success) {
            throw new IOException("Failed to save image.");
        }
    }
}
