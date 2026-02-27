package edu.up.cg.tools;

import java.awt.image.BufferedImage;


/**
 *
 * Inverts the RGB channels of every pixel in either the whole
 * image or a rectangular sub-region.
 * <p>
 *   Each color channel (Red, Green, Blue) is flipped:
 *     new_value = 255 - old_value
 *   This produces the photographic "negative" of the image.
 *   Black (0,0,0) becomes white (255,255,255) and vice versa.
 *   The Alpha (transparency) channel is preserved untouched.
 * <p>
 * Two operating modes, selected at construction time:
 *   1. Full image  → use no-argument constructor InvertOperation()
 *   2. Region only → use InvertOperation(x1, y1, x2, y2, image)
 *
 *
 *
 */
public class InvertOperation {

    private boolean fullImageSelected;
    private RegionExtractor extractor;

    /**
     * Constructor for FULL-IMAGE invert mode.
     *
     * Every pixel in the image will be inverted when
     * {@link #invertImage(BufferedImage)} is called.
     */
    public InvertOperation() {
        this.fullImageSelected = true;
    }

    /**
     * Constructor for region invert mode.
     *
     * Only pixels inside the rectangle from (topLeftX, topLeftY)
     * to (bottomRightX, bottomRightY) will be inverted.
     *
     * @param topLeftX     Top-left X of the region.
     * @param topLeftY     Top-left Y of the region.
     * @param bottomRightX Bottom-right X of the region.
     * @param bottomRightY Bottom-right Y of the region.
     * @param image        Used by CoordsValidator to check bounds.
     *
     * @throws IllegalArgumentException if the rectangle is invalid.
     */
    public InvertOperation(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, BufferedImage image) {

        CoordsValidator.validate(topLeftX, topLeftY, bottomRightX, bottomRightY, image);

        this.fullImageSelected = false;
        this.extractor = new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);
    }


    /**
     * Applies the invert operation to the given image.
     * <p>
     * Pixel changes are made directly in {@code image}
     * (in-place modification). The same reference is returned for
     * convenience, but the caller does not need to use the return value
     * since the original BufferedImage is already updated.
     *
     * @param image The BufferedImage to invert.
     * @return The same {@code image} reference with inverted pixels.
     */
    public BufferedImage invertImage(BufferedImage image) {

        if (fullImageSelected) {
            invertPixels(image,0,0, image.getWidth(),image.getHeight());
        } else {
            int startX = extractor.getTopLeftX();
            int startY = extractor.getTopLeftY();

            BufferedImage region = extractor.extract(image);

            invertPixels(region, startX, startY, region.getWidth(),region.getHeight());
        }
        return image;
    }


    /**
     * Core pixel-inversion loop.
     * <p>
     * Iterates over every pixel in the rectangle defined by
     * (startX, startY) to (startX+width, startY+height) inside
     * {@code image} and inverts its R, G, B channels while
     * preserving its Alpha channel.
     * <p>
     * Pixel format (32-bit ARGB):
     *   Bits 31-24 → Alpha  (transparency, 0=transparent, 255=opaque)
     *   Bits 23-16 → Red
     *   Bits 15-8  → Green
     *   Bits  7-0  → Blue
     * <p>
     * Inversion formula per channel:
     *   new_channel = 255 - old_channel
     *
     * @param image  The BufferedImage whose pixels will be modified.
     * @param startX Starting X within {@code image} (inclusive).
     * @param startY Starting Y within {@code image} (inclusive).
     * @param width  Number of columns to process.
     * @param height Number of rows to process.
     */
    private void invertPixels(BufferedImage image,int startX, int startY,  int width, int height) {

        for (int y = startY; y < height; y++) {
            for (int x = startX; x < width; x++) {

                // Read the 32-bit ARGB value for this pixel.
                int rgb = image.getRGB(x, y);


                int a = (rgb >> 24) & 0xFF; // Alpha — NOT inverted
                int r = (rgb >> 16) & 0xFF; // Red   channel (bits 23-16)
                int g = (rgb >>  8) & 0xFF; // Green channel (bits 15-8)
                int b = rgb  & 0xFF;        // Blue  channel (bits  7-0)

                // Invert R, G, B
                int invertedR = 255 - r;
                int invertedG = 255 - g;
                int invertedB = 255 - b;

                int inverted = (a << 24) | (invertedR << 16) | (invertedG << 8) | invertedB;
                image.setRGB(x, y, inverted);
            }
        }
    }

}


