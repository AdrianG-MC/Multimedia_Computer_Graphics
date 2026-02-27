package edu.up.cg.tools;

import java.awt.image.BufferedImage;


/**
 * Rotates a rectangular region of the image by a fixed angle
 * (90°, 180°, or 270° clockwise) and writes the result back
 * into the same area of the original image.
 * <p>
 * Supported angles: 90, 180, 270 (degrees clockwise).
 * <p>
 * Important: The rotation is applied to a REGION of the image,
 * not the whole canvas. Pixels outside the rectangle are
 * untouched. The canvas size does not change.
 * <p>
 * After building the rotated canvas, the pixels are written
 * back starting at (startX, startY) in the original image.
 * Pixels that would fall outside the image bounds are clipped.
 */
public class RotateOperation {

    private RegionExtractor extractor;
    private int angle;

    /**
     * Constructs a RotateOperation for the given rectangle and angle.
     *
     * Validates the rectangle against the image before doing any work.
     *
     * @param topLeftX     Top-left X of the region to rotate (inclusive).
     * @param topLeftY     Top-left Y of the region to rotate (inclusive).
     * @param bottomRightX Bottom-right X of the region (exclusive).
     * @param bottomRightY Bottom-right Y of the region (exclusive).
     * @param angle        Clockwise rotation in degrees: 90, 180, or 270.
     * @param image        Used by CoordsValidator for bounds checking.
     *
     * @throws IllegalArgumentException if the rectangle is invalid.
     */
    public RotateOperation(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, int angle, BufferedImage image) {

        CoordsValidator.validate(topLeftX, topLeftY, bottomRightX, bottomRightY, image);


        this.extractor =  new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);
        this.angle = angle;
    }

    /**
     * Applies the rotation to the stored region and writes the result
     * back into the parent image at the same (startX, startY) offset.
     *
     * Step-by-step:
     *   1. Extract the region as a sub-image.
     *   2. Allocate a new rotated canvas (W×H becomes H×W for 90°/270°).
     *   3. Map every pixel (x, y) of the region to its new position
     *      in the rotated canvas.
     *   4. Copy the rotated canvas pixels back into the parent image,
     *      starting at (startX, startY), clipping at image boundaries.
     *
     * @param image The full BufferedImage to modify.
     * @return The same {@code image} reference with the rotated region applied.
     */
    public BufferedImage rotateImage(BufferedImage image) {

        // Extract the region
        BufferedImage region = extractor.extract(image);

        int regionWidth = region.getWidth();
        int regionHeight = region.getHeight();


        // Allocate the rotated canvas
        BufferedImage rotated;
        if (angle == 90 || angle == 270){
            rotated = new BufferedImage(regionHeight, regionWidth, image.getType());
        } else{
            rotated = new BufferedImage(regionWidth, regionHeight, image.getType());
        }

        // Pixel remapping
        for (int y = 0; y < regionHeight; y++) {
            for (int x = 0; x < regionWidth; x++) {
                int pixel = region.getRGB(x, y);

                // Place the pixel at its rotated destination inside 'rotated'.
                if (angle == 90)
                    rotated.setRGB(regionHeight - y - 1, x, pixel);

                else if (angle == 180)
                    rotated.setRGB(regionWidth - x - 1, regionHeight - y - 1, pixel);

                else if (angle == 270)
                    rotated.setRGB(y, regionWidth - x - 1, pixel);
            }
        }

        // Place rotated pixels back
        // The rotated region is placed starting at the same top-left corner
        // (startX, startY) where the original region began. It must be at the
        // center but i did late the work so its extra work for later
        // Pixels are clipped if the rotated canvas doesn't fit (can happen
        // for 90°/270° if the region is near the image edge).
        int startX = extractor.getTopLeftX();
        int startY = extractor.getTopLeftY();

        for (int y = 0; y < rotated.getHeight(); y++) {
            for (int x = 0; x < rotated.getWidth(); x++) {

                int destX = startX + x;
                int destY = startY + y;

                // Clipping: skip pixels that would land outside the image.
                if (destX < image.getWidth() && destY < image.getHeight()) {
                    image.setRGB(destX, destY, rotated.getRGB(x, y));
                }
            }
        }
        return image;
    }

}
