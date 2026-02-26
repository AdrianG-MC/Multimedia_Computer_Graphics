package edu.up.cg.handlers;


import edu.up.cg.templates.ImageTemplate;
import edu.up.cg.tools.CropOperation;
import edu.up.cg.tools.InvertOperation;
import edu.up.cg.tools.RotateOperation;

import java.awt.image.BufferedImage;

/**
 * ImageEditorService coordinates all image operations.
 *
 * It acts as the middle layer between:
 * - UI (Console or future GUI)
 * - Operations (Crop, Invert, Rotate)
 * - ImageModel (stores image state)
 *
 * This class does NOT handle:
 * - File loading/saving
 * - User input
 */

public class ImageEditorHandler {

    private final ImageTemplate template;

//    Constructor that receives the image template
    public ImageEditorHandler(ImageTemplate template) {
        this.template = template;
    }

//    Returns the current Image
    public BufferedImage getCurrentImage() {
        return template.getImage();
    }

//    Applies crop operation. Replaces the entire image with cropped version.
    public void crop(int x, int y, int width, int height) {
        CropOperation cropOperation = new CropOperation(x, y,  width, height, template.getImage());
        BufferedImage newImage = cropOperation.crop(template.getImage());

        template.setImage(newImage);
    }

//    Applies full image invert
    public void invertFull(){
        InvertOperation invertOperation = new InvertOperation();
        invertOperation.invertImage(template.getImage());
    }

//    Applies invert to a selected region
    public void invertRegion(int x, int y, int width, int height) {
        InvertOperation invertOperation = new InvertOperation(x, y,  width, height, template.getImage());
        invertOperation.invertImage(template.getImage());
    }

//    Applies rotation to selected region
    public void rotate(int x, int y, int width, int height, int angle) {
        RotateOperation rotateOperation = new RotateOperation(x, y, width, height,angle, template.getImage());
        rotateOperation.rotateImage(template.getImage());
    }

}
