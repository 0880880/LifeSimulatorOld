package com.lifesimulator;

import com.badlogic.gdx.utils.Array;

public class Neuron {

    public boolean input = false;
    public boolean output = false;

    public double value;
    public double bias;

    public Array<Connection> connections = new Array<>();

    public Neuron(boolean input, boolean output) {
        this.input = input;
        this.output = output;
    }



}
