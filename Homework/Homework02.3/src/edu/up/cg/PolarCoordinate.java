package edu.up.cg;

public class PolarCoordinate {

    private double r;
    private double theta; // degrees

    public PolarCoordinate(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    public CartesianCoordinate toCartesian() {
        double radians = Math.toRadians(theta);
        double x = r * Math.cos(radians);
        double y = r * Math.sin(radians);
        return new CartesianCoordinate(x, y);
    }

    public double getR() {
        return r;
    }

    public double getTheta() {
        return theta;
    }

}

