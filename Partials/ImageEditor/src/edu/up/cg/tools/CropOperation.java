package edu.up.cg.tools;

import java.awt.image.BufferedImage;

// Crop operation extracts a rectangular region from an image and make it as the new image.

public class CropOperation  {

    private final RegionExtractor extractor;


//     Constructor
    public CropOperation(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, BufferedImage image) {
        CoordsValidator.validate(topLeftX,topLeftY,bottomRightX,bottomRightY, image);
        this.extractor = new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);

    }



//    Applies crop and returns the new image

    public BufferedImage crop(BufferedImage image) {
        return extractor.extract(image);
    }
}
