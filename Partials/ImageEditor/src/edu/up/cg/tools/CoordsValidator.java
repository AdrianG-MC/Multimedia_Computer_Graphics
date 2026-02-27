package edu.up.cg.tools;

import java.awt.image.BufferedImage;


/**
 *
 * Utility class that validates a pair of (topLeft, bottomRight)
 * pixel coordinates against a BufferedImage before any operation
 * (crop, invert-region, rotate) is performed.
 *
 * Validation rules:
 *   1. No coordinate may be negative.
 *   2. No coordinate may exceed the image's width/height.
 *   3. x1 must be strictly less than x2 (non-zero width).
 *   4. y1 must be strictly less than y2 (non-zero height).
 */
public class CoordsValidator {

    /**
     * Validates that coordinates are inside image bounds
     * and form a valid rectangle.
     *
     * @param x1    Top-left X coordinate (column).
     * @param y1    Top-left Y coordinate (row).
     * @param x2    Bottom-right X coordinate (column).
     * @param y2    Bottom-right Y coordinate (row).
     * @param image The image the rectangle must fit inside.
     *
     * @throws IllegalArgumentException if any validation rule is violated.
     */
    public static void validate(int x1, int y1, int x2, int y2,  BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        // Check negative values
        if (x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0) {
            throw new IllegalArgumentException( "Coordinates cannot be negative.");
        }

        // Check bounds
        if (x1 > width || x2 > width ||  y1 > height || y2 > height) {
            throw new IllegalArgumentException( "Coordinates exceed image dimensions.");
        }

        // Check rectangle size (positive area)
        if (x1 == x2 || y1 == y2) {
            throw new IllegalArgumentException( "Rectangle must have positive width and height.");
        }
    }
}
