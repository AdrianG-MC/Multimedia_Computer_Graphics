package edu.up.cg;

public class CartesianCoordinate {

    private double x;
    private double y;

    public CartesianCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PolarCoordinate toPolar() {
        double r = Math.sqrt(x * x + y * y);
        double theta = Math.toDegrees(Math.atan2(y, x));
        return new PolarCoordinate(r, theta);
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}

