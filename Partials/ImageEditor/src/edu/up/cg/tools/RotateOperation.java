package edu.up.cg.tools;

import java.awt.image.BufferedImage;

public class RotateOperation {

    private RegionExtractor extractor;
    private int angle;

//    Constructor
    public RotateOperation(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, int angle, BufferedImage image) {

        CoordsValidator.validate(topLeftX, topLeftY, bottomRightX, bottomRightY, image);


        this.extractor =  new RegionExtractor(topLeftX, topLeftY, bottomRightX, bottomRightY);
        this.angle = angle;
    }

//     Applies rotation overwriting original image

    public BufferedImage rotateImage(BufferedImage image) {
        BufferedImage region = extractor.extract(image);

        int width = region.getWidth();
        int height = region.getHeight();

        BufferedImage rotated;

        if (angle == 90 || angle == 270){
            rotated = new BufferedImage(height, width, image.getType());
        } else{
            rotated = new BufferedImage(width, height, image.getType());
        }

        // Rotation of pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                if (angle == 90)
                    rotated.setRGB(height - y - 1, x, pixel);

                else if (angle == 180)
                    rotated.setRGB(width - x - 1, height - y - 1, pixel);

                else if (angle == 270)
                    rotated.setRGB(y, width - x - 1, pixel);
            }
        }

        // Place rotated pixels back
        int startX = extractor.getTopLeftX();
        int startY = extractor.getTopLeftY();

        for (int y = 0; y < rotated.getHeight(); y++) {
            for (int x = 0; x < rotated.getWidth(); x++) {

                if (startX + x < image.getWidth() && startY + y < image.getHeight()){
                    image.setRGB(startX + x, startY + y, rotated.getRGB(x, y));
                }
            }
        }
        return image;
    }

}
