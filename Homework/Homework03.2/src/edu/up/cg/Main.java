package edu.up.cg;

import edu.up.cg.Tools.FileHandler;
import edu.up.cg.Tools.ImageCompressor;
import edu.up.cg.Tools.ImageDecompressor;
import edu.up.cg.Utils.RGBPixel;
import edu.up.cg.Utils.RLEBlock;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // Example: smart compress and choose best option
            String inputImage = "src/edu/up/cg/Resources/test.png";
            String outputFile = "src/edu/up/cg/Resources/compressed.dat";

            smartCompress(inputImage, outputFile);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Compress an image trying different tolerances and gradient modes,
     * then save the smallest file size version
     */
    public static void smartCompress(String inputPath, String outputPath) throws Exception {
        System.out.println("---- SMART COMPRESSION ----");
        System.out.println("Input: " + inputPath);

        // Load image
        BufferedImage img = ImageIO.read(new File(inputPath));
        if (img == null) {
            throw new Exception("Could not load image");
        }

        int width = img.getWidth();
        int height = img.getHeight();
        int totalPixels = width * height;

        System.out.println("Image: " + width + "x" + height + " (" + totalPixels + " pixels)");
        System.out.println();

        // Test different tolerances
        int[] tolerances = {5, 10, 15, 20};
        boolean[] gradientModes = {false, true};

        // Store best option
        long bestSize = Long.MAX_VALUE;
        int bestTolerance = 0;
        boolean bestGradient = false;
        List<RLEBlock> bestBlocks = null;

        System.out.println("Testing combinations:");
        System.out.println("Tolerance | Gradient | Blocks | File Size | Compression");
        System.out.println("----------|----------|--------|-----------|------------");

        // Try all combinations
        for (int tol : tolerances) {
            ImageCompressor compressor = new ImageCompressor(tol);
            List<RLEBlock> blocks = compressor.compress(img);
            long fileSize = FileHandler.getFileSize(blocks);

            for (boolean gradient : gradientModes) {
                // Calculate stats
                double compressionRatio = (1.0 - (double)fileSize / (totalPixels * 3)) * 100;

                System.out.printf("%6d    | %8s | %6d | %7d B | %6.2f%%\n",
                        tol, gradient ? "yes" : "no", blocks.size(), fileSize, compressionRatio);

                // Check if this is better
                if (fileSize < bestSize) {
                    bestSize = fileSize;
                    bestTolerance = tol;
                    bestGradient = gradient;
                    bestBlocks = blocks;
                }
            }
        }

        System.out.println();
        System.out.println("=== BEST OPTION ===");
        System.out.println("Tolerance: " + bestTolerance);
        System.out.println("Gradient: " + (bestGradient ? "yes" : "no"));
        System.out.println("File size: " + bestSize + " bytes");
        System.out.println("Blocks: " + bestBlocks.size());

        // Save best version
        FileHandler.save(outputPath, bestBlocks, width, height, bestTolerance);
        System.out.println("Saved to: " + outputPath);

        // Decompress and save test image
        ImageDecompressor decompressor = new ImageDecompressor(bestGradient);
        BufferedImage decompressed = decompressor.decompress(bestBlocks, width, height);

        String testOutput = outputPath.replace(".dat", "_test.png");
        ImageIO.write(decompressed, "PNG", new File(testOutput));
        System.out.println("Test output: " + testOutput);

        // Show quality comparison
        System.out.println();
        System.out.println("=== QUALITY CHECK ===");
        int differentPixels = 0;
        long totalDiff = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGBPixel orig = RGBPixel.fromRGB(img.getRGB(x, y));
                RGBPixel decomp = RGBPixel.fromRGB(decompressed.getRGB(x, y));

                int diffR = Math.abs(orig.getRed() - decomp.getRed());
                int diffG = Math.abs(orig.getGreen() - decomp.getGreen());
                int diffB = Math.abs(orig.getBlue() - decomp.getBlue());
                int maxDiff = Math.max(diffR, Math.max(diffG, diffB));

                if (maxDiff > 0) {
                    differentPixels++;
                    totalDiff += maxDiff;
                }
            }
        }

        System.out.println("Different pixels: " + differentPixels + " / " + totalPixels);
        System.out.println("Average difference: " + (differentPixels > 0 ? totalDiff / differentPixels : 0));
    }

    /**
     * Simple compression with fixed parameters
     */
    public static void simpleCompress(String inputPath, String outputPath, int tolerance, boolean gradient) throws Exception {
        System.out.println("Compressing: " + inputPath);

        BufferedImage img = ImageIO.read(new File(inputPath));
        int width = img.getWidth();
        int height = img.getHeight();

        // Compress
        ImageCompressor compressor = new ImageCompressor(tolerance);
        List<RLEBlock> blocks = compressor.compress(img);

        // Save
        FileHandler.save(outputPath, blocks, width, height, tolerance);

        System.out.println("Saved " + blocks.size() + " blocks to " + outputPath);
    }

    /**
     * Decompress a file
     */
    public static void decompress(String inputPath, String outputPath, boolean gradient) throws Exception {
        System.out.println("Decompressing: " + inputPath);

        // Load
        FileHandler.CompressedData data = FileHandler.load(inputPath);

        // Decompress
        ImageDecompressor decompressor = new ImageDecompressor(gradient);
        BufferedImage img = decompressor.decompress(data.blocks, data.width, data.height);

        // Save
        ImageIO.write(img, "PNG", new File(outputPath));

        System.out.println("Saved to " + outputPath);
    }
}

