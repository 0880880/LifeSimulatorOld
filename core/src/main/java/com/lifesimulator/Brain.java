package com.lifesimulator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Brain {

    Array<Neuron> neurons = new Array<>();
    Array<Connection> connections = new Array<>();
    Neuron[] inputNeurons;
    Neuron[] outputNeurons;
    Neuron[] hiddenNeurons;

    int inputSize = 0;
    int hiddenSize = 0;
    int outputSize = 0;

    public Brain(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        inputNeurons = new Neuron[inputSize];
        hiddenNeurons = new Neuron[hiddenSize];
        outputNeurons = new Neuron[outputSize];

        for (int i = 0; i < inputSize; i++) {
            Neuron neuron = new Neuron(true, false);
            inputNeurons[i] = neuron;
            neurons.add(neuron);
        }
        for (int i = 0; i < hiddenSize; i++) {
            Neuron neuron = new Neuron(true, true);
            neurons.add(neuron);
            hiddenNeurons[i] = neuron;
        }
        for (int i = 0; i < outputSize; i++) {
            Neuron neuron = new Neuron(false, true);
            neurons.add(neuron);
            outputNeurons[i] = neuron;
        }

        for (int i = 0; i < neurons.size; i++) {
            for (int j = 0; j < neurons.size; j++) {
                if (!neurons.get(i).output && !neurons.get(j).input) {
                    Connection c = new Connection(neurons.get(i), neurons.get(j), Math.random() * 2.0 - 1.0);
                    connections.add(c);
                    neurons.get(i).connections.add(c);
                    neurons.get(j).connections.add(c);
                }
            }
        }

    }

    public boolean evolved = false;

    public Brain(Brain brainA, int caNeurons) {
        this.inputSize = brainA.inputSize;
        this.hiddenSize = caNeurons;
        this.outputSize = brainA.outputSize;

        inputNeurons = new Neuron[inputSize];
        hiddenNeurons = new Neuron[caNeurons];
        outputNeurons = new Neuron[outputSize];

        for (int i = 0; i < inputSize; i++) {
            Neuron neuron = new Neuron(true, false);
            inputNeurons[i] = neuron;
            neurons.add(neuron);
        }
        for (int i = 0; i < hiddenSize; i++) {
            Neuron neuron = new Neuron(true, true);
            neurons.add(neuron);
            hiddenNeurons[i] = neuron;
        }
        for (int i = 0; i < outputSize; i++) {
            Neuron neuron = new Neuron(false, true);
            neurons.add(neuron);
            outputNeurons[i] = neuron;
        }

        int idx = 0;
        for (int i = 0; i < neurons.size; i++) {
            for (int j = 0; j < neurons.size; j++) {
                if (!neurons.get(i).output && !neurons.get(j).input) {
                    Connection c = new Connection(neurons.get(i), neurons.get(j), brainA.connections.get(idx).weight);
                    connections.add(c);
                    neurons.get(i).connections.add(c);
                    neurons.get(j).connections.add(c);
                    idx++;
                }
            }
        }

        mutate();

    }

    public Brain(Brain brainA, Brain brainB, int caNeurons) {
        this.inputSize = brainA.inputSize;
        this.hiddenSize = caNeurons;
        this.outputSize = brainB.outputSize;

        inputNeurons = new Neuron[inputSize];
        hiddenNeurons = new Neuron[caNeurons];
        outputNeurons = new Neuron[outputSize];

        for (int i = 0; i < inputSize; i++) {
            Neuron neuron = new Neuron(true, false);
            inputNeurons[i] = neuron;
            neurons.add(neuron);
        }
        for (int i = 0; i < hiddenSize; i++) {
            Neuron neuron = new Neuron(true, true);
            neurons.add(neuron);
            hiddenNeurons[i] = neuron;
        }
        for (int i = 0; i < outputSize; i++) {
            Neuron neuron = new Neuron(false, true);
            neurons.add(neuron);
            outputNeurons[i] = neuron;
        }

        int idx = 0;
        for (int i = 0; i < neurons.size; i++) {
            for (int j = 0; j < neurons.size; j++) {
                if (!neurons.get(i).output && !neurons.get(j).input) {
                    Connection c = new Connection(neurons.get(i), neurons.get(j), MathUtils.randomBoolean(.5f) ? brainA.connections.get(idx).weight : brainB.connections.get(idx).weight);
                    connections.add(c);
                    neurons.get(i).connections.add(c);
                    neurons.get(j).connections.add(c);
                    idx++;
                }
            }
        }

        mutate();

    }

    public void mutate() {
        for (Connection c : connections) {
            if (MathUtils.randomBoolean(Statics.mutationRate.get())) { // DEFAULT CHANCE .003f
                c.weight = Math.random() * 2.0 - 1.0;
                evolved = true;
            }
        }
        /*for (Neuron n : neurons) {
            if (MathUtils.randomBoolean(.003f)) {
                n.bias = Math.random() * 2.0 - 1.0;
                evolved = true;
            }
        }*/
    }

    public double processNeuron(Neuron neuron) {
        double value = 0;
        if (neuron.input) {
            value = neuron.value;
        } else {
            for (int i = 0; i < neuron.connections.size; i++) {
                Connection connection = neuron.connections.get(i);
                if (connection.neuronA == neuron) {
                    double input = processNeuron(connection.neuronB);
                    value += input * connection.weight;
                } else if (connection.neuronB == neuron) {
                    double input = processNeuron(connection.neuronA);
                    value += input * connection.weight;
                }
            }
            for (int i = 0; i < neuron.connections.size; i++) {
                Connection connection = neuron.connections.get(i);
                if (connection.neuronA == neuron) {
                    double input = processNeuron(connection.neuronB);
                    connection.weight = connection.weight + Statics.learningRate.get() * (input * connection.weight * Math.atan(value));
                } else if (connection.neuronB == neuron) {
                    double input = processNeuron(connection.neuronA);
                    connection.weight = connection.weight + Statics.learningRate.get() * (input * connection.weight * Math.atan(value));
                }
            }
        }
        return neuron.input ? value + neuron.bias : Math.atan(value) + neuron.bias;
    }

    public double[] get(double[] inputs) {
        double[] outputs = new double[this.outputSize];

        for (int i = 0; i < inputs.length; i++) {
            inputNeurons[i].value = inputs[i];
        }

        for (int i = 0; i < outputs.length; i++) {
            Neuron neuron = outputNeurons[i];
            neuron.value = processNeuron(neuron);
            outputs[i] = neuron.value;
        }

        return outputs;

    }

}
