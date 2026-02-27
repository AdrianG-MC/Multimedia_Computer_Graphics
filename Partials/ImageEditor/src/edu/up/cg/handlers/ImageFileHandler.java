package edu.up.cg.handlers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Handles all file-system interactions:
 *   - Listing available images in the Input folder.
 *   - Loading a selected image from disk into a BufferedImage.
 *   - Saving the edited BufferedImage to the Edited folder.
 *
 * Folder layout (relative to the project root / working directory):
 *   src/edu/up/cg/Images/
 *       Input/        ← place images here before running
 *       Edited/       ← saved results appear here (auto-created)
 *
 * Supported image formats for INPUT:  PNG, JPG, JPEG, BMP
 * Output format is inferred from the original file's extension.
 *
 * Naming convention for saved files:
 *   <originalName>_edited.<extension>
 *   Example: photo.png  →  photo_edited.png
 *
 */
public class ImageFileHandler {

    // Path where the user selects an image. Relative to the project root path.
    private static final String INPUT_FOLDER = "src/edu/up/cg/Images/Input";

    // Path where the edited images are written. Created automatically if it doesn't exist.
    private static final String OUTPUT_FOLDER = "src/edu/up/cg/Images/Edited";

    /**
     * Returns an array of image files available in the Input folder.
     *
     * Only files with the extensions .png, .jpg, .jpeg, and .bmp are included.
     *
     * @return Non-empty array of File objects for each image found.
     *
     * @throws RuntimeException If the Input folder doesn't exist or contains unsupported image files.
     */
    public File[] listInputImages() {

        File folder = new File(INPUT_FOLDER);


        // Verify that the folder exist and is a directory.
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Input folder not found: " + INPUT_FOLDER);
        }


        // Filter that list supported files.
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
     * Loads the selected file into a BufferedImage
     *
     * Uses ImageIO library that supports PNG, JPG, BMP, and GIF formats
     * whitout external dependencies.
     *
     * @param file The image file to load.
     * @return A BufferedImage containing the decoded pixel data.
     *
     * @throws IOException if the file cant be read, or it's an unsupported image format (returns null).
     *
     */
    public BufferedImage load(File file) throws IOException {

        BufferedImage image = ImageIO.read(file);

        // ImageIO.read() returns null for unsupported formats.
        if (image == null) {
            throw new IOException(
                    "Could not decode the image: " + file.getName() +
                    "\nInvalid or corrupted image.");
        }

        return image;
    }

    /**
     * Saves the edited image to the Output folder.
     *
     * The output file name is derived from the original file name by
     * adding "_edited" before the extension.
     *
     * The Edited/ directory is created automatically if absent.

     * @param image        The BufferedImage to write to disk.
     * @param originalName The original file name (with extension),
     *                     used to make the output file name.
     *
     * @throws IOException if ImageIO cannot write the image (e.g.
     *         unsupported format string or I/O error on the file system).
     */
    public void save(BufferedImage image, String originalName) throws IOException {

        // Ensure the output directory exists (create it if needed).
        File outputDir = new File(OUTPUT_FOLDER);

        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the full path.
        }

        // Split the name.
        int dotIndex = originalName.lastIndexOf('.');
        String nameWithoutExtension = originalName.substring(0, dotIndex);
        String extension = originalName.substring(dotIndex + 1);

        // Build the output file name.
        String newName = nameWithoutExtension + "_edited." + extension;

        File outputFile = new File(outputDir, newName);

        // ImageIO.write() returns false if cant be written.
        // We treat false as a failure so it can get a clean error message.
        boolean success = ImageIO.write(image, extension, outputFile);

        if (!success) {
            throw new IOException(
                    "No image writer available for format: " + extension +
                    "\nTry saving as PNG or JPG."
            );
        }
    }
}
