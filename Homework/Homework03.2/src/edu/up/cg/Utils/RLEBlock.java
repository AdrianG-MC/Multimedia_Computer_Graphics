package edu.up.cg.Utils;

// RLE block - stores a run of similar pixels
public class RLEBlock {
    private int avgRed;
    private int avgGreen;
    private int avgBlue;
    private int count;

    public RLEBlock(int r, int g, int b, int count) {
        this.avgRed = r;
        this.avgGreen = g;
        this.avgBlue = b;
        this.count = count;
    }

    public int getRed() { return avgRed; }
    public int getGreen() { return avgGreen; }
    public int getBlue() { return avgBlue; }
    public int getCount() { return count; }

    public RGBPixel getColor() {
        return new RGBPixel(avgRed, avgGreen, avgBlue);
    }

    @Override
    public String toString() {
        return "Block[" + count + " pixels, RGB(" + avgRed + "," + avgGreen + "," + avgBlue + ")]";
    }
}
