package com.lifesimulator;

import com.badlogic.gdx.math.Vector2;

public class Food extends Vector2 implements Chunkable {

    Vector2 tmp = new Vector2();

    public Food(float x, float y) {
        super(x, y);
    }

    public Food(Vector2 position) {
        super(position);
    }

    @Override
    public Vector2 getPosition() {
        return tmp.set(x, y);
    }

    @Override
    public float getRadius() {
        return 1;
    }

}
