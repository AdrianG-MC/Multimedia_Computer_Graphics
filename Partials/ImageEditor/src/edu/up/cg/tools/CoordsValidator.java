package edu.up.cg.tools;

import java.awt.image.BufferedImage;



public class CoordsValidator {

    /**
     * Validates that coordinates are inside image bounds and form a valid rectangle.
     *
     * @throws IllegalArgumentException if invalid
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

        // Check rectangle size
        if (x1 == x2 || y1 == y2) {
            throw new IllegalArgumentException( "Rectangle must have positive width and height.");
        }
    }
}
