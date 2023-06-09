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
    public float colorHue = MathUtils.random(0f,1f);
    public float colorSaturation = MathUtils.random(0f,1f);
    public float colorValue = MathUtils.random(0f,1f);
    public int speed = 1;

    public Genome(int neurons) {
        this.neurons = neurons;
    }

}
