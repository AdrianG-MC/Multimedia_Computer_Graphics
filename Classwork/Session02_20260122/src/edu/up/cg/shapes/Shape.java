package edu.up.cg.shapes;

import edu.up.cg.tools.IOHandler;

public abstract class Shape {
    IOHandler inputOutput;

    public Shape(IOHandler console) {
        setInputOutput(console);
    }

    public IOHandler getInputOutput() {
        return inputOutput;
    }

    public void setInputOutput(IOHandler console) {
        this.inputOutput = console;
    }

    protected abstract void obtainParameters();
    public abstract float getArea();
    public abstract float getPerimeter();


}
