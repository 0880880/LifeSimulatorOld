package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;

public class Creature implements Chunkable {

    public int x;
    public int y;
    public float direction;

    public Brain brain;

    public Color color;

    public float foodEaten = 0;

    public int moveMeter = 0;

    public Genome genome;

    float energy = Statics.creatureBaseEnergy.get();

    boolean clock = false;

    void init(int x, int y) {
        this.x = x;
        this.y = y;
        direction = MathUtils.random(0, MathUtils.PI2);
    }

    public int totalNeurons;

    public Creature(int x, int y) {
        color = new Color(MathUtils.random(0f,1f),MathUtils.random(0f,1f),MathUtils.random(0f,1f),1f);
        init(x, y);
        genome = new Genome(Statics.numberOfHiddenNeurons.get());
        brain = new Brain(2 + Statics.visionRays.get() * (eye ? 3 : 0) + 4 + 1 + 4, 20, 2 + 2);
        totalNeurons = brain.neurons.size;
    }
    public Creature(Creature creatureA, int x, int y) {
        color = creatureA.color;
        init(x, y);
        genome = creatureA.genome;
        if (MathUtils.randomBoolean(.001f)) {
            //gene.neurons++;
        } else {
            if (MathUtils.randomBoolean(.001f)) {
                //gene.neurons--;
            }
        }
        brain = new Brain(creatureA.brain, genome.neurons);
        if (brain.evolved)
            color.add(MathUtils.random(-.001f,.001f),MathUtils.random(-.001f,.001f),MathUtils.random(-.001f,.001f),0);
        totalNeurons = brain.neurons.size;
    }
    public Creature(Creature creatureA, Creature creatureB, int x, int y) {
        color = new Color((creatureA.color.r + creatureB.color.r) / 2f, (creatureA.color.g + creatureB.color.g) / 2f, (creatureA.color.b + creatureB.color.b) / 2f, 1);
        init(x, y);
        genome = creatureA.genome;
        if (MathUtils.randomBoolean(.001f)) {
            //gene.neurons++;
        } else {
            if (MathUtils.randomBoolean(.001f)) {
                //gene.neurons--;
            }
        }
        brain = new Brain(creatureA.brain, creatureB.brain, genome.neurons);
        if (brain.evolved)
            color.add(MathUtils.random(-.001f,.001f),MathUtils.random(-.001f,.001f),MathUtils.random(-.001f,.001f),0);
        totalNeurons = brain.neurons.size;
    }

    int frameCounter = 0;

    Index tempIndex = new Index();

    Vector2 temp = new Vector2();

    boolean checkIntersection(HashMap<Index, Creature> creaturesIndex, Array<Circle> obstacles, double[] inputs, int idx, Vector2 start, Vector2 end, Array<Vector2> food) {
        boolean det = false;
        for (int j = 0; j < obstacles.size; j++) {
            Circle obstacle = obstacles.get(j);
            temp.set(obstacle.x, obstacle.y);
            if (Utils.intersect(start, end, temp, obstacle.radius)) {
                inputs[2 + idx * 3 + 1] = 1.0;
                det = true;
                break;
            }
        }
        for (int j = 0; j < food.size; j++) {
            Vector2 f = food.get(j);
            if (f != null && Utils.intersect(start, end, f, 2)) {
                inputs[2 + idx * 3 + 2] = 1.0;
                det = true;
                break;
            }
        }
        return det;
    }

    boolean eye = true;

    public double sender0 = 0;
    public double sender1 = 0;

    public void update(HashMap<Index, Creature> creaturesIndex, Array<Creature> creatures, Array<Circle> obstacles, Array<Vector2> food, ShapeDrawer drawer, boolean enableRendering) {
        frameCounter++;

        double[] inputs = new double[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 4 + 1 + 2];
        inputs[0] = x / 256.0;
        inputs[1] = y / 256.0;
        int idx = 0;
        // EYE
        if (eye) {
            for (int d = 0; d < 360; d += (360 / (Statics.visionRays.get() - 1))) {
                float dir = d * MathUtils.degRad;
                Vector2 end = new Vector2(x, y);
                end.x += MathUtils.cos(dir) * Statics.visionRange.get();
                end.y += MathUtils.sin(dir) * Statics.visionRange.get();
                if (checkIntersection(creaturesIndex, obstacles, inputs, idx, new Vector2(x, y), end, food)) {

                }
                idx++;
            }
        }

        // TOUCH
        tempIndex.x = x + 1;
        tempIndex.y = y;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0))] = creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.x = x - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 1] = creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.x = x;
        tempIndex.y = y + 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 2] = creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.y = y - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3] = creaturesIndex.containsKey(tempIndex) ? 1 : 0;

        // CLOCK

        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1] = clock ? -1 : 1;

        // COMMUNICATION

        tempIndex.y = y + 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.y = y - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.x = x + 1;
        tempIndex.y = y;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.x = x - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex) ? creaturesIndex.get(tempIndex).sender1 : 0);

        double[] outputs = brain.get(inputs);

        Vector2 oldPosition = new Vector2(x, y);
        if (outputs[0] > .3f)
            x++;
        if (outputs[0] < -.3f)
            x--;
        if (outputs[1] > .5f)
            y++;
        if (outputs[1] < -.5f)
            y--;

        sender0 = outputs[2];

        if (oldPosition.x != x && oldPosition.y != y) {
            moveMeter++;
            energy -= .5f;
        }

        if (frameCounter > 5) {
            energy--;
            frameCounter = 0;
        }

        boolean hit = false;
        for (int i = 0; i < obstacles.size; i++) {
            Circle obstacle = obstacles.get(i);
            if (obstacle != null && obstacle.contains(x, y)) {
                x = (int) oldPosition.x;
                y = (int) oldPosition.y;
                if (Statics.killOnTouch.get())
                    energy = 0;
                hit = true;
                break;
            }
        }
        for (int i = 0; i < food.size; i++) {
            Vector2 f = food.get(i);
            if (f != null && f.dst(x, y) < 2) {
                foodEaten++;
                energy += 142;
                food.removeIndex(i);
                break;
            }
        }
        x = MathUtils.clamp(x, 0, Statics.mapSize);
        y = MathUtils.clamp(y, 0, Statics.mapSize);
        for (int i = 0; i < creatures.size; i++) {
            Creature creature = creatures.get(i);
            if (creature != this) {
                if (creature.x == x && creature.y == y) {
                    x = (int) oldPosition.x;
                    y = (int) oldPosition.y;
                    hit = true;
                    break;
                }
            }
        }
        if (!hit && x <= Statics.mapSize && y <= Statics.mapSize) {
            creaturesIndex.remove(new Index((int) oldPosition.x, (int) oldPosition.y));
            creaturesIndex.put(new Index(x, y), this);
        }

        x = MathUtils.clamp(x, 0, Statics.mapSize);
        y = MathUtils.clamp(y, 0, Statics.mapSize);

        //if (enableRendering) drawer.line(x - 128, y - 128, x - 128 + MathUtils.cos(getRealDirection() / 4f * MathUtils.PI2) * 10, y - 128 + MathUtils.sin(getRealDirection() / 4f * MathUtils.PI2) * 10, Color.RED, .5f);

        clock = !clock;

    }

    @Override
    public Vector2 getPosition() {
        temp.set(x,y);
        return temp;
    }

    @Override
    public float getRadius() {
        return .5f;
    }
}
