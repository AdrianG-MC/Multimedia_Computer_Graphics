package edu.up.cg.templates;

import java.awt.image.BufferedImage;


/**
 *
 * This class acts as a simple data container (a "model") that
 * holds the current state of the image being edited.
 * <p>
 * Why have a wrapper instead of using BufferedImage directly?
 *
 * Operations like Crop replace the entire BufferedImage object
 * (they return a new image). If every class held a direct
 * reference to a BufferedImage, those references would go stale
 * after a crop. By routing everything through ImageTemplate, any
 * class that holds an ImageTemplate will always see the latest
 * image simply by calling getImage().
 * <p>
 * Responsibilities:
 *   - Store the current BufferedImage
 *   - Allow the image to be replaced
 */
public class ImageTemplate {

    private BufferedImage image;

    /**
     * Constructs an ImageTemplate wrapping the given image.
     *
     * @param image The initial BufferedImage loaded from disk.
     */
    public ImageTemplate(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns the current image.
     * @return The current BufferedImage state.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Replaces the stored image with a new one.
     * Called by operations that produce a brand-new BufferedImage
     * rather than modifying pixels in-place.
     *
     * @param image The new BufferedImage to store.
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
