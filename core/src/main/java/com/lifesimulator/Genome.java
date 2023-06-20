package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
public class Genome {

    public int neurons;
    public int connections;
    public float colorHue = MathUtils.random(0f,1f);
    public float colorSaturation = MathUtils.random(0f,1f);
    public float colorValue = MathUtils.random(0f,1f);

    public Genome(int neurons, int connections) {
        this.neurons = neurons;
        this.connections = connections;
    }

}
