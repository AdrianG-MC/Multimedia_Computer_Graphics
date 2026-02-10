package edu.up.gc;

public class AspectRatio {

    private int width;
    private int height;

    public AspectRatio(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String calculate() {
        int gcd = gcd(width, height);
        return (width / gcd) + ":" + (height / gcd);
    }

    // Euclid's algorithm
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}


