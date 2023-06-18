package com.lifesimulator;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Obstacle extends Circle implements Chunkable {

    Vector2 tmp = new Vector2();

    public Obstacle(float x, float y, float radius) {
        super(x, y, radius);
    }

    public Obstacle(Vector2 position, float radius) {
        super(position, radius);
    }

    @Override
    public Vector2 getPosition() {
        return tmp.set(x, y);
    }

    @Override
    public float getRadius() {
        return radius;
    }
}
