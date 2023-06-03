package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;

public class Creature {

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
        genome = new Genome(20);
        brain = new Brain(2 + rays * (eye ? 3 : 0) + 4 + 1 + 4, 20, 2 + 2);
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

    int rayResolution = 16;
    int rayLength = 12;
    int rays = 20;

    Index tempIndex = new Index();
    Index tempIndex1 = new Index();

    boolean checkDst(HashMap<Index, Creature> creaturesIndex, Array<Circle> obstacles, double[] inputs, int idx, int offX, int offY, Vector2 position, Array<Vector2> food) {
        tempIndex.x = (int) position.x;
        tempIndex.y = (int) position.y;
        boolean det = false;
        if (creaturesIndex.containsKey(tempIndex)) {
            inputs[2 + idx * 3] = 1.0;
            det = true;
        }
        for (int j = 0; j < obstacles.size; j++) {
            Circle obstacle = obstacles.get(j);
            if (obstacle != null && obstacle.contains(position)) {
                inputs[2 + idx * 3 + 1] = 1.0;
                det = true;
            }
        }
        for (int j = 0; j < food.size; j++) {
            Vector2 f = food.get(j);
            if (f != null && f.dst2(position) <= 2f) {
                inputs[2 + idx * 3 + 2] = 1.0;
                det = true;
            }
        }
        return det;
    }

    boolean eye = true;

    public double sender0 = 0;
    public double sender1 = 0;

    public void update(HashMap<Index, Creature> creaturesIndex, Array<Creature> creatures, Array<Creature>[][] chunks, Array<Circle> obstacles, Array<Vector2> food, ShapeDrawer drawer, boolean enableRendering) {
        frameCounter++;

        double[] inputs = new double[2 + (rays * (eye ? 3 : 0)) + 4 + 1 + 2];
        //inputs[0] = x / 256.0;
        //inputs[1] = y / 256.0;
        int idx = 0;
        // EYE
        if (eye) {
            for (int d = 0; d < 360; d += (360 / (rays - 1))) {
                float dir = d * MathUtils.degRad;
                for (int i = rayResolution - 1; i >= 0; i--) {
                    Vector2 position = new Vector2(x, y);
                    position.x += MathUtils.cos(dir) * ((i / (float) rayResolution) * rayLength);
                    position.y += MathUtils.sin(dir) * ((i / (float) rayResolution) * rayLength);
                    //drawer.line(x - 128, y - 128, x - 128 + MathUtils.cos(dir) * ((i / (float) rayResolution) * rayLength), y - 128 + MathUtils.sin(dir) * ((i / (float) rayResolution) * rayLength), Color.RED, .1f);
                    if (checkDst(creaturesIndex, obstacles, inputs, idx, 0, 0, position, food))
                        break;
                    //checkDst(creaturesIndex, obstacles, inputs, idx, 1, 0, position);
                    //checkDst(creaturesIndex, obstacles, inputs, idx, -1, 0, position);
                    //checkDst(creaturesIndex, obstacles, inputs, idx, 0, 1, position);
                    //checkDst(creaturesIndex, obstacles, inputs, idx, 0, -1, position);

                    /*checkDst(chunks, inputs, idx, 1, 1, position);
                    checkDst(chunks, inputs, idx, 1, -1, position);
                    checkDst(chunks, inputs, idx, -1, 1, position);
                    checkDst(chunks, inputs, idx, -1, 1, position);*/
                }
                idx++;
            }
        }

        // TOUCH
        tempIndex1.x = x + 1;
        tempIndex1.y = y;
        inputs[2 + (rays * (eye ? 3 : 0))] = creaturesIndex.containsKey(tempIndex1) ? 1 : 0;
        tempIndex1.x = x - 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 1] = creaturesIndex.containsKey(tempIndex1) ? 1 : 0;
        tempIndex1.x = x;
        tempIndex1.y = y + 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 2] = creaturesIndex.containsKey(tempIndex1) ? 1 : 0;
        tempIndex1.y = y - 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 3] = creaturesIndex.containsKey(tempIndex1) ? 1 : 0;

        // CLOCK

        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1] = clock ? -1 : 1;

        // COMMUNICATION

        tempIndex1.y = y + 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender0 : 0);
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender1 : 0);
        tempIndex1.y = y - 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender0 : 0);
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender1 : 0);
        tempIndex1.x = x + 1;
        tempIndex1.y = y;
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender0 : 0);
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender1 : 0);
        tempIndex1.x = x - 1;
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 1] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender0 : 0);
        inputs[2 + (rays * (eye ? 3 : 0)) + 3 + 1 + 2] = (creaturesIndex.containsKey(tempIndex1) ? creaturesIndex.get(tempIndex1).sender1 : 0);

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
        x = MathUtils.clamp(x, 0, 256);
        y = MathUtils.clamp(y, 0, 256);
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
        if (!hit && x <= 256 && y <= 256) {
            creaturesIndex.remove(new Index((int) oldPosition.x, (int) oldPosition.y));
            creaturesIndex.put(new Index(x, y), this);
        }

        x = MathUtils.clamp(x, 0, 256);
        y = MathUtils.clamp(y, 0, 256);

        //if (enableRendering) drawer.line(x - 128, y - 128, x - 128 + MathUtils.cos(getRealDirection() / 4f * MathUtils.PI2) * 10, y - 128 + MathUtils.sin(getRealDirection() / 4f * MathUtils.PI2) * 10, Color.RED, .5f);

        clock = !clock;

    }

}
