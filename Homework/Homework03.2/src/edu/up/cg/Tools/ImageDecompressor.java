package edu.up.cg.Tools;

import edu.up.cg.Utils.RGBPixel;
import edu.up.cg.Utils.RLEBlock;

import java.awt.image.BufferedImage;
import java.util.List;

// Decompresses RLE blocks back to image
public class ImageDecompressor {
    private final boolean useGradient;

    public ImageDecompressor(boolean useGradient) {
        this.useGradient = useGradient;
    }

    public BufferedImage decompress(List<RLEBlock> blocks, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int pixelIndex = 0;

        for (int i = 0; i < blocks.size(); i++) {
            RLEBlock block = blocks.get(i);
            RLEBlock nextBlock = (i < blocks.size() - 1) ? blocks.get(i + 1) : null;

            // decide if we use gradient or flat color
            boolean applyGradient = useGradient && nextBlock != null &&
                    block.getCount() > 1 && nextBlock.getCount() > 1 &&
                    block.getColor().isSimilar(nextBlock.getColor(), 50);

            if (applyGradient) {
                // paint with gradient
                pixelIndex = paintGradient(img, block, nextBlock, pixelIndex, width);
            } else {
                // paint flat color
                pixelIndex = paintFlat(img, block, pixelIndex, width);
            }
        }

        return img;
    }

    private int paintFlat(BufferedImage img, RLEBlock block, int startIdx, int width) {
        RGBPixel color = block.getColor();
        int rgb = color.toRGB();
        int count = block.getCount();

        for (int i = 0; i < count; i++) {
            int x = startIdx % width;
            int y = startIdx / width;
            img.setRGB(x, y, rgb);
            startIdx++;
        }
        return startIdx;
    }

    private int paintGradient(BufferedImage img, RLEBlock block, RLEBlock nextBlock,
                              int startIdx, int width) {
        RGBPixel startColor = block.getColor();
        RGBPixel endColor = nextBlock.getColor();
        int count = block.getCount();

        for (int i = 0; i < count; i++) {
            double ratio = (double)i / Math.max(1, count - 1);
            RGBPixel color = startColor.interpolate(endColor, ratio);

            int x = startIdx % width;
            int y = startIdx / width;
            img.setRGB(x, y, color.toRGB());
            startIdx++;
        }
        return startIdx;
    }
}

