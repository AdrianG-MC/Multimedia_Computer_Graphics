package edu.up.cg.tools;

import java.awt.image.BufferedImage;

// Crop operation extracts a rectangular region from an image and make it as the new image.


/**
 *
 Extracts a rectangular sub-region from the current image and
 * returns it as the NEW image. After a crop, the canvas is
 * permanently resized to the selected area.
 *
 *
 * How it works:
 *   1. The caller provides two corners: (x1,y1) top-left and
 *      (x2,y2) bottom-right, plus the current BufferedImage.
 *   2. CoordsValidator checks the rectangle is valid.
 *   3. RegionExtractor cuts the pixels out via getSubimage().
 *   4. The caller (ImageEditorHandler) replaces the stored image
 *      with the result — the image is now permanently smaller.
 */
public class CropOperation  {

    private final RegionExtractor extractor;


    /**
     * Constructs a CropOperation for the rectangle
     * from (topLeftX, topLeftY) to (bottomRightX, bottomRightY).
     *
     * Validation is performed immediately — if the rectangle is
     * outside the image or has zero area, an exception is thrown
     * before any pixel work begins.
     *
     * @param topLeftX     Top-left X of the crop area.
     * @param topLeftY     Top-left Y of the crop area.
     * @param bottomRightX Bottom-right X of the crop area.
     * @param bottomRightY Bottom-right Y of the crop area.
     * @param image        The image to crop (used only for validation here).
     *
     * @throws IllegalArgumentException if the rectangle is invalid.
     */
    public CropOperation(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, BufferedImage image) {
        CoordsValidator.validate(topLeftX,topLeftY,bottomRightX,bottomRightY, image);
        this.extractor = new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);

    }

    /**
     * Performs the crop and returns the resulting sub-image.
     *
     * The returned image shares pixel data with the source via
     * BufferedImage.getSubimage(). It is the caller's responsibility
     * to store this result (ImageEditorHandler does this via
     * template.setImage()).
     *
     * @param image The image to crop.
     * @return A new BufferedImage containing only the cropped region.
     */
    public BufferedImage crop(BufferedImage image) {
        return extractor.extract(image);
    }
}
