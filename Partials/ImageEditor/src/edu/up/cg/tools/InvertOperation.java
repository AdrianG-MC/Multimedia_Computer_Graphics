package edu.up.cg.tools;

import java.awt.image.BufferedImage;

public class InvertOperation {

    private boolean fullImageSelected;
    private RegionExtractor extractor;

//    Constructor for full image invert (set as default)
    public InvertOperation() {
        this.fullImageSelected = true;
    }

// Constructor for Invert region
    public InvertOperation (int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, BufferedImage image) {

        CoordsValidator.validate(topLeftX, topLeftY, bottomRightX, bottomRightY, image);

        this.fullImageSelected = false;
        this.extractor = new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);
    }

// Applies the invert operation
    public BufferedImage invertImage(BufferedImage image) {

        if (fullImageSelected) {
             invertRegion(image,0,0, image.getWidth(),image.getHeight());
        } else {
            int startX = extractor.getTopLeftX();
            int startY = extractor.getTopLeftY();

            BufferedImage region = extractor.extract(image);

             invertRegion(region, startX, startY, region.getWidth(),region.getHeight());
        }
        return image;
    }

    //    Invert RGB values in a region
    private void invertRegion(BufferedImage image,int startX, int startY,  int width, int height) {

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int r = 255 - (rgb >> 16) & 0xFF;
                int g = 255  - (rgb >> 8) & 0xFF;
                int b = 255 - rgb & 0xFF;

                int inverted = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, inverted);
            }
        }
    }

}


