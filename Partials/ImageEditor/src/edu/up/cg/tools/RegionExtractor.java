package edu.up.cg.tools;

import java.awt.image.BufferedImage;


/**
 *Stores a rectangle defined by two corner coordinates and can
 * cut ("extract") that rectangle out of any BufferedImage.
 * <p>
 * Used by:
 *   - CropOperation   → the extracted sub-image becomes the new image
 *   - InvertOperation → the extracted region is inverted in-place
 *   - RotateOperation → the extracted region is rotated and written back
 * <p>
 * Important note about getSubimage():
 * Java's BufferedImage.getSubimage() returns a VIEW of the parent
 * image that shares the same underlying pixel data. This means:
 *   • Pixel writes to the sub-image ARE reflected in the original.
 *   • The sub-image itself uses its own local coordinate system
 *     starting at (0, 0), NOT the coordinates of its position in
 *     the parent image.
 */
public class RegionExtractor {

    private final int topLeftX;
    private final int topLeftY;
    private final int bottomRightX;
    private final int bottomRightY;

    /**
     * Constructs a RegionExtractor for the rectangle
     * from (topLeftX, topLeftY) to (bottomRightX, bottomRightY).
     *
     * @param topLeftX     Left edge column, inclusive.
     * @param topLeftY     Top edge row, inclusive.
     * @param bottomRightX Right edge column, exclusive.
     * @param bottomRightY Bottom edge row, exclusive.
     */
    public RegionExtractor(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
    }


//  Getters for the reinserted region
    public int getTopLeftX() {
        return topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    /**
     * Extracts the stored rectangle from the given image as a sub-image.
     *
     * The returned BufferedImage shares its pixel data with {@code image}
     * (it is a view, not a copy). Writing pixels into the returned image
     * will modify {@code image} as well.
     *
     * Its local coordinate system starts at (0, 0) — the pixel at
     * (topLeftX, topLeftY) in the parent becomes (0, 0) in the sub-image.
     *
     * @param image The source image to extract from.
     * @return A sub-image view covering the stored rectangle.
     */
    public BufferedImage extract(BufferedImage image) {
        int width = bottomRightX - topLeftX;
        int height = bottomRightY - topLeftY;

        return image.getSubimage(topLeftX, topLeftY, width, height);
    }
}
