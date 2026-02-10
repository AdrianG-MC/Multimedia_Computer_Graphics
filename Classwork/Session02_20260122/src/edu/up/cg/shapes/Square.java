package edu.up.cg.shapes;

import edu.up.cg.tools.IOHandler;

public class Square extends Shape{
    private float side;

    public Square(IOHandler console){
        super(console);
    }

    public float getSide() {
        return side;
    }

    public void setSide(float side) {
        this.side = side;
    }

    @Override
    protected void obtainParameters() {
        setSide(getInputOutput().getFloat("Please provide the side:", "Not valid input."));
    }

    @Override
    public float getPerimeter() {
        return getSide() * 4;
    }

    @Override
    public float getArea() {
        return getSide() * 4;
    }

}
