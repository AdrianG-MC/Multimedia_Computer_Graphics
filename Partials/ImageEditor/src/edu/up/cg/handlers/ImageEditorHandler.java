package edu.up.cg.handlers;


import edu.up.cg.templates.ImageTemplate;
import edu.up.cg.tools.CropOperation;
import edu.up.cg.tools.InvertOperation;
import edu.up.cg.tools.RotateOperation;

import java.awt.image.BufferedImage;

/**
 * ImageEditorService coordinates all image operations.
 *
 * It acts as the middle layer between:
 * - UI (Console or future GUI)
 * - Operations (Crop, Invert, Rotate)
 * - ImageModel (stores image state)
 *
 * This class does NOT handle:
 * - File loading/saving
 * - User input
 */

public class ImageEditorHandler {


    // Model that holds the current BufferedImage.
    private final ImageTemplate template;

    // Constructor that receives the image template

    /**
     * Constructs the handler wrapping for the loaded image
     * @param template
     */
    public ImageEditorHandler(ImageTemplate template) {
        this.template = template;
    }

//    Returns the current Image

    /**
     * Returns the current image in its last state.
     * This is used  by the ConsoleMenuHanler.handleSave() to pass the final
     * image to ImageFileHandler for saving into the folder.
     * @return The current BufferedImage (edited).
     */
    public BufferedImage getCurrentImage() {
        return template.getImage();
    }

//    Applies crop operation. Replaces the entire image with cropped version.

    /**
     * Crops the image to the rectangle defined by two opposite corner points.
     *
     * It replaces the stored image with a smaller (cropped) one. After this,
     * getCurrentImage() returns the cropped version of the image.
     *
     * Coords note: (x, y) are passed as (topLeftX, topLeftY) and
     * (width, height) are (bottomRightX, bottomRightY)
     *
     * @param x         Top Left X of the cropped area
     * @param y         Top Left Y of the cropped area
     * @param width     Bottom Right X of the cropped area
     * @param height    Bottom Right Y of the cropped area
     *
     * @throws IllegalArgumentException if the coords are invalid.
     */
    public void crop(int x, int y, int width, int height) {
        CropOperation cropOperation = new CropOperation(x, y,  x + width, y + height, template.getImage());
        BufferedImage newImage = cropOperation.crop(template.getImage());

        template.setImage(newImage);
    }



    /**
     * Invert all pixel colors in the entire image.
     *
     * Each color channel (R,G,B) is replaced by 255 - channel.
     * The alpha channel or transparency is preserved.
     */
    public void invertFull(){

        // No argument constructor equals a full image mode.
        InvertOperation invertOperation = new InvertOperation();

        //Modifies pixels of the whole image, so same BufferedImage is updated.
        invertOperation.invertImage(template.getImage());
    }



    /**
     * Invert Pixel colors in the specified rectangular region.
     *
     *
     * @param x         Top Left X of the region.
     * @param y         Top Left Y of the region.
     * @param width     Bottom Right X of the region.
     * @param height    Bottom Right Y of the region.
     *
     * @throws IllegalArgumentException if the coords are invalid.
     */
    public void invertRegion(int x, int y, int width, int height) {

        //Constructor with parameters equals region mode invert.
        InvertOperation invertOperation =
                new InvertOperation(x, y,  x +width, y +height, template.getImage());

        // Modifies pixels into the selected region.
        invertOperation.invertImage(template.getImage());
    }

//    Applies rotation to selected region

    /**
     *Rotates the specified rectangular region by the given angle.
     *
     * The rotated pixels are written back into the same area of the image.
     * Original dimensions doesn't change and pixels outside the rectangle are not affected.
     *
     * @param x         Top Left X of the region.
     * @param y         Top Left Y of the region.
     * @param width     Bottom Right X of the region.
     * @param height    Bottom Right Y of the region.
     * @param angle     Clockwise rotation in degrees. Must be 90, 180, or 270.
     *
     * @throws IllegalArgumentException if the coords are invalid.
     */
    public void rotate(int x, int y, int width, int height, int angle) {
        RotateOperation rotateOperation =
                new RotateOperation(x, y, width, height,angle, template.getImage());

        //Modifies the region in-place and returns the same image
        rotateOperation.rotateImage(template.getImage());
    }

}
