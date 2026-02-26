package edu.up.cg.templates;

import java.awt.image.BufferedImage;

public class ImageTemplate {

    private BufferedImage image;

    public ImageTemplate(BufferedImage image) {
        this.image = image;
    }
    public BufferedImage getImage() {
        return image;
    }
    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
