package edu.up.cg.Utils;

// Simple RGB pixel class
public class RGBPixel {
    private int red;
    private int green;
    private int blue;

    public RGBPixel(int red, int green, int blue) {
        this.red = clamp(red);
        this.green = clamp(green);
        this.blue = clamp(blue);
    }

    private int clamp(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    // Check if two pixels are similar
    public boolean isSimilar(RGBPixel other, int tolerance) {
        return Math.abs(this.red - other.red) <= tolerance &&
                Math.abs(this.green - other.green) <= tolerance &&
                Math.abs(this.blue - other.blue) <= tolerance;
    }

    // Convert to packed RGB int
    public int toRGB() {
        return (red << 16) | (green << 8) | blue;
    }

    // Create from packed RGB int
    public static RGBPixel fromRGB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new RGBPixel(r, g, b);
    }

    // Interpolate between two pixels (for gradients)
    public RGBPixel interpolate(RGBPixel other, double ratio) {
        int newR = (int)(this.red + (other.red - this.red) * ratio);
        int newG = (int)(this.green + (other.green - this.green) * ratio);
        int newB = (int)(this.blue + (other.blue - this.blue) * ratio);
        return new RGBPixel(newR, newG, newB);
    }

    @Override
    public String toString() {
        return "RGB(" + red + "," + green + "," + blue + ")";
    }
}

