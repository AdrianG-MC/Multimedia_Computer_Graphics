package edu.up.cg.tools;

import java.awt.image.BufferedImage;

public class RegionExtractor {

    private final int topLeftX;
    private final int topLeftY;
    private final int bottomRightX;
    private final int bottomRightY;

    // Constructor
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


    public BufferedImage extract(BufferedImage image) {
        int width = bottomRightX - topLeftX;
        int height = bottomRightY - topLeftY;

        return image.getSubimage(topLeftX, topLeftY, width, height);
    }
}
