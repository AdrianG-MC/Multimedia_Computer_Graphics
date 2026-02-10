package edu.up.cg.Tools;

import edu.up.cg.Utils.RGBPixel;
import edu.up.cg.Utils.RLEBlock;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Compresses images using RLE
public class ImageCompressor {
    private int tolerance;
    private static final int MAX_RUN = 255; // max pixels per block

    public ImageCompressor(int tolerance) {
        this.tolerance = tolerance;
    }

    public List<RLEBlock> compress(BufferedImage img) {
        List<RLEBlock> blocks = new ArrayList<>();
        int width = img.getWidth();
        int height = img.getHeight();

        // current run variables
        int sumR = 0, sumG = 0, sumB = 0;
        int runLength = 0;
        RGBPixel avgColor = null;

        // scan image left to right, top to bottom
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGBPixel pixel = RGBPixel.fromRGB(img.getRGB(x, y));

                if (runLength == 0) {
                    // start new run
                    sumR = pixel.getRed();
                    sumG = pixel.getGreen();
                    sumB = pixel.getBlue();
                    runLength = 1;
                    avgColor = pixel;
                } else {
                    // check if pixel can be added to current run
                    boolean similar = pixel.isSimilar(avgColor, tolerance);
                    boolean notTooLong = runLength < MAX_RUN;

                    if (similar && notTooLong) {
                        // add to current run
                        sumR += pixel.getRed();
                        sumG += pixel.getGreen();
                        sumB += pixel.getBlue();
                        runLength++;
                        // update average
                        avgColor = new RGBPixel(sumR/runLength, sumG/runLength, sumB/runLength);
                    } else {
                        // save current run and start new one
                        blocks.add(new RLEBlock(sumR/runLength, sumG/runLength, sumB/runLength, runLength));
                        sumR = pixel.getRed();
                        sumG = pixel.getGreen();
                        sumB = pixel.getBlue();
                        runLength = 1;
                        avgColor = pixel;
                    }
                }
            }
        }

        // save last run
        if (runLength > 0) {
            blocks.add(new RLEBlock(sumR/runLength, sumG/runLength, sumB/runLength, runLength));
        }

        return blocks;
    }

    public int getTolerance() {
        return tolerance;
    }
}

