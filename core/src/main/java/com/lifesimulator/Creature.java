package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;

import static com.lifesimulator.Statics.*;
import static com.lifesimulator.Statics.drawer;

public class Creature implements Chunkable {

    public int x;
    public int y;
    public float direction;

    public Brain brain;
    public Brain originalBrain;

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
        init(x, y);
        genome = new Genome(Statics.numberOfHiddenNeurons.get(), numberOfConnections.get());
        color = Utils.hsvToRgb(
            genome.colorHue,
            genome.colorSaturation,
            genome.colorValue
        );
        brain = new Brain(2 + Statics.visionRays.get() * (eye ? 4 : 0) + 4 + 1 + 4 + 1 + 1, genome.neurons, 2 + 2 + 1, genome.connections);
        originalBrain = brain;
        totalNeurons = brain.neurons.size;
    }
    public Creature(Creature creatureA, int x, int y) {
        init(x, y);
        genome = creatureA.genome;
        if (MathUtils.randomBoolean(Statics.mutationChance.get()))
            genome.colorHue += MathUtils.random(-.1f, .1f);
        if (MathUtils.randomBoolean(Statics.mutationChance.get()))
            genome.colorSaturation += MathUtils.random(-.1f, .1f);
        if (MathUtils.randomBoolean(Statics.mutationChance.get()))
            genome.colorValue += MathUtils.random(-.1f, .1f);
        genome.colorHue = MathUtils.clamp(genome.colorHue, 0, 1);
        genome.colorSaturation = MathUtils.clamp(genome.colorSaturation, 0, 1);
        genome.colorValue = MathUtils.clamp(genome.colorValue, 0, 1);
        color = Utils.hsvToRgb(
            genome.colorHue,
            genome.colorSaturation,
            genome.colorValue
        );
        if (Statics.neuronASMutation.get()) {
            if (MathUtils.randomBoolean(Statics.mutationChance.get())) {
                genome.neurons++;
            } else {
                if (MathUtils.randomBoolean(Statics.mutationChance.get()) && genome.neurons > 2)
                    genome.neurons--;
            }
        }
        brain = new Brain(creatureA.originalBrain, genome.neurons, genome.connections);
        originalBrain = brain;
        totalNeurons = brain.neurons.size;
    }
    public Creature(Creature creatureA, Creature creatureB, int x, int y) {
        color = new Color((creatureA.color.r + creatureB.color.r) / 2f, (creatureA.color.g + creatureB.color.g) / 2f, (creatureA.color.b + creatureB.color.b) / 2f, 1);
        init(x, y);
        genome = creatureA.genome;
        if (MathUtils.randomBoolean(.001f)) {
            genome.neurons++;
        } else {
            if (MathUtils.randomBoolean(.001f)) {
                genome.neurons--;
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

    int waste = 0;

    boolean bCheck(double[] inputs, int idx, Vector2 start, Vector2 end) {

        if (!Statics.simpleVision.get()) {
            inputs[2 + idx * 4] = 1;
            inputs[2 + idx * 4 + 1] = 1;
            inputs[2 + idx * 4 + 2] = 1;
            if (MainScreen.selectedCreature == this) {
                MainScreen.eye[idx * 4] = 1;
                MainScreen.eye[idx * 4 + 1] = 1;
                MainScreen.eye[idx * 4 + 2] = 1;
            }
        } else {
            inputs[2 + idx * 4] = -1;
            inputs[2 + idx * 4 + 1] = -1;
            inputs[2 + idx * 4 + 2] = -1;
            if (MainScreen.selectedCreature == this) {
                MainScreen.eye[idx * 4] = -1;
                MainScreen.eye[idx * 4 + 1] = -1;
                MainScreen.eye[idx * 4 + 2] = -1;
            }
        }
        inputs[2 + idx * 4 + 3] = 1;
        if (MainScreen.selectedCreature == this) {
            MainScreen.eye[idx * 4 + 3] = 1;
        }

        boolean d = false;

        if (Statics.canSeeOthers.get()) {
            Array<Array<Chunkable>> chunks = Statics.chunks.getChunksInRange(getPosition(), Statics.visionRange.get());
            for (int i = 0; i < chunks.size; i++) {
                Array<Chunkable> chunk = chunks.get(i);
                for (int j = 0; j < chunk.size; j++) {
                    Chunkable chunkable = chunk.get(j);
                    if (Utils.intersect(start, end, chunkable.getPosition(), chunkable.getRadius())) {
                        Creature creature = (Creature) chunkable;
                        if (!Statics.simpleVision.get()) {
                            inputs[2 + idx * 4] = creature.color.r;
                            inputs[2 + idx * 4 + 1] = creature.color.g;
                            inputs[2 + idx * 4 + 2] = creature.color.b;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4] = 1;
                                MainScreen.eye[idx * 4 + 1] = 0;
                                MainScreen.eye[idx * 4 + 2] = 1;
                            }
                        } else {
                            inputs[2 + idx * 4] = 1;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4] = 1;
                            }
                        }
                        if (Statics.depthVision.get()) {
                            float dst = (useDst2.get() ? start.dst2(creature.x, creature.y) / (float) Statics.visionRange.get() : start.dst(creature.x, creature.y) / (float) Statics.visionRange.get());
                            inputs[2 + idx * 4 + 3] = dst;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4 + 3] = dst;
                            }
                        }
                        d = true;
                        break;
                    }
                }
            }
        }

        float obstacleDistance = visionRange.get();
        float foodDistance = visionRange.get();
        float wasteDistance = visionRange.get();
        Array<Array<Chunkable>> chunks = obstaclesChunks.getChunksInRange(getPosition(), Statics.visionRange.get());
        for (int i = 0; i < chunks.size; i++) {
            Array<Chunkable> chunk = chunks.get(i);
            for (int j = 0; j < chunk.size; j++) {
                Chunkable chunkable = chunk.get(j);
                if (Utils.intersect(start, end, chunkable.getPosition(), chunkable.getRadius())) {
                    obstacleDistance = (useDst2.get() ? start.dst2(chunkable.getPosition()) - chunkable.getRadius() : start.dst(chunkable.getPosition()) - chunkable.getRadius());
                    d = true;
                    break;
                }
            }
            if (d) break;
        }
        chunks = foodChunks.getChunksInRange(getPosition(), Statics.visionRange.get());
        for (int i = 0; i < chunks.size; i++) {
            Array<Chunkable> chunk = chunks.get(i);
            for (int j = 0; j < chunk.size; j++) {
                Chunkable chunkable = chunk.get(j);
                if (chunkable != null && Utils.intersect(start, end, chunkable.getPosition(), 1.5f)) {
                    foodDistance = (useDst2.get() ? start.dst2(chunkable.getPosition()) : start.dst(chunkable.getPosition()));
                    d = true;
                    break;
                }
            }
            if (d) break;
        }

        if (foodDistance > obstacleDistance) {

            if (!Statics.simpleVision.get()) {
                inputs[2 + idx * 4] = 0;
                inputs[2 + idx * 4 + 1] = 0;
                inputs[2 + idx * 4 + 2] = 0;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4] = 0;
                    MainScreen.eye[idx * 4 + 1] = 0;
                    MainScreen.eye[idx * 4 + 2] = 0;
                }
            } else {
                inputs[2 + idx * 4 + 1] = 1;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4 + 1] = 1;
                }
            }
            if (Statics.depthVision.get()) {
                float dst = obstacleDistance / (float) Statics.visionRange.get();
                inputs[2 + idx * 4 + 3] = dst;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4 + 3] = dst;
                }
            }

        } else if (obstacleDistance > foodDistance) {


            if (!Statics.simpleVision.get()) {
                inputs[2 + idx * 4] = 1;
                inputs[2 + idx * 4 + 1] = 0;
                inputs[2 + idx * 4 + 2] = 0;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4] = 1;
                    MainScreen.eye[idx * 4 + 1] = 0;
                    MainScreen.eye[idx * 4 + 2] = 0;
                }
            } else {
                inputs[2 + idx * 4 + 2] = 1;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4 + 2] = 1;
                }
            }
            if (Statics.depthVision.get()) {
                float dst = foodDistance / (float) Statics.visionRange.get();
                inputs[2 + idx * 4 + 3] = dst;
                if (MainScreen.selectedCreature == this) {
                    MainScreen.eye[idx * 4 + 3] = dst;
                }
            }

        }

        return d;
    }

    boolean checkIntersection(HashMap<Index, Creature> creaturesIndex, double[] inputs, int idx, Vector2 start, Vector2 end) {

        if (!Statics.simpleVision.get()) {
            inputs[2 + idx * 4] = 1;
            inputs[2 + idx * 4 + 1] = 1;
            inputs[2 + idx * 4 + 2] = 1;
            if (MainScreen.selectedCreature == this) {
                MainScreen.eye[idx * 4] = 1;
                MainScreen.eye[idx * 4 + 1] = 1;
                MainScreen.eye[idx * 4 + 2] = 1;
            }
        } else {
            inputs[2 + idx * 4] = -1;
            inputs[2 + idx * 4 + 1] = -1;
            inputs[2 + idx * 4 + 2] = -1;
            if (MainScreen.selectedCreature == this) {
                MainScreen.eye[idx * 4] = -1;
                MainScreen.eye[idx * 4 + 1] = -1;
                MainScreen.eye[idx * 4 + 2] = -1;
            }
        }
        inputs[2 + idx * 4 + 3] = 1;
        if (MainScreen.selectedCreature == this) {
            MainScreen.eye[idx * 4 + 3] = 1;
        }

        boolean det = false;
        if (Statics.canSeeOthers.get()) {
            Array<Array<Chunkable>> chunks = Statics.chunks.getChunksInRange(getPosition(), Statics.visionRange.get());
            for (int i = 0; i < chunks.size; i++) {
                Array<Chunkable> chunk = chunks.get(i);
                for (int j = 0; j < chunk.size; j++) {
                    Chunkable chunkable = chunk.get(j);
                    if (Utils.intersect(start, end, chunkable.getPosition(), chunkable.getRadius() * 1.5f)) {
                        Creature creature = (Creature) chunkable;
                        if (!Statics.simpleVision.get()) {
                            inputs[2 + idx * 4] = creature.color.r;
                            inputs[2 + idx * 4 + 1] = creature.color.g;
                            inputs[2 + idx * 4 + 2] = creature.color.b;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4] = 1;
                                MainScreen.eye[idx * 4 + 1] = 0;
                                MainScreen.eye[idx * 4 + 2] = 1;
                            }
                        } else {
                            inputs[2 + idx * 4] = 1;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4] = 1;
                            }
                        }
                        if (Statics.depthVision.get()) {
                            float dst = start.dst(creature.x, creature.y) / (float) Statics.visionRange.get();
                            inputs[2 + idx * 4 + 3] = dst;
                            if (MainScreen.selectedCreature == this) {
                                MainScreen.eye[idx * 4 + 3] = dst;
                            }
                        }
                        det = true;
                        break;
                    }
                }
            }
        }
        float obstacleDistance = Float.MAX_VALUE;
        float foodDistance = Float.MAX_VALUE;
        float wasteDistance = Float.MAX_VALUE;
        Array<Array<Chunkable>> chunks = obstaclesChunks.getChunksInRange(getPosition(), Statics.visionRange.get());
        for (int i = 0; i < chunks.size; i++) {
            Array<Chunkable> chunk = chunks.get(i);
            for (int j = 0; j < chunk.size; j++) {
                Chunkable chunkable = chunk.get(j);
                if (Utils.intersect(start, end, chunkable.getPosition(), chunkable.getRadius())) {
                    obstacleDistance = start.dst(chunkable.getPosition()) - chunkable.getRadius();
                    det = true;
                    break;
                }
            }
        }
        chunks = foodChunks.getChunksInRange(getPosition(), Statics.visionRange.get());
        for (int i = 0; i < chunks.size; i++) {
            Array<Chunkable> chunk = chunks.get(i);
            for (int j = 0; j < chunk.size; j++) {
                Chunkable chunkable = chunk.get(j);
                if (chunkable != null && Utils.intersect(start, end, chunkable.getPosition(), 1)) {
                    foodDistance = start.dst(chunkable.getPosition()) - 1;
                    det = true;
                    break;
                }
            }
        }
        for (int j = 0; j < Statics.waste.size; j++) {
            Vector2 w = Statics.waste.get(j);
            if (w != null && Utils.intersect(start, end, w, 1)) {
                wasteDistance = start.dst(w) - 1;
                det = true;
                break;
            }
        }

        if (obstacleDistance < foodDistance) {
            if (obstacleDistance < wasteDistance) {
                if (!Statics.simpleVision.get()) {
                    inputs[2 + idx * 4] = 0;
                    inputs[2 + idx * 4 + 1] = 0;
                    inputs[2 + idx * 4 + 2] = 0;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4] = 0;
                        MainScreen.eye[idx * 4 + 1] = 0;
                        MainScreen.eye[idx * 4 + 2] = 0;
                    }
                } else {
                    inputs[2 + idx * 4 + 1] = 1;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 1] = 1;
                    }
                }
                if (Statics.depthVision.get()) {
                    float dst = obstacleDistance / (float) Statics.visionRange.get();
                    inputs[2 + idx * 4 + 3] = dst;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 3] = dst;
                    }
                }
            } else {
                if (!Statics.simpleVision.get()) {
                    inputs[2 + idx * 4] = Color.BROWN.r;
                    inputs[2 + idx * 4 + 1] = Color.BROWN.g;
                    inputs[2 + idx * 4 + 2] = Color.BROWN.b;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4] = Color.BROWN.r;
                        MainScreen.eye[idx * 4 + 1] = Color.BROWN.g;
                        MainScreen.eye[idx * 4 + 2] = Color.BROWN.b;
                    }
                } else {
                    inputs[2 + idx * 4 + 1] = 1;
                    inputs[2 + idx * 4 + 2] = 1;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 1] = 1;
                        MainScreen.eye[idx * 4 + 2] = 1;
                    }
                }
                if (Statics.depthVision.get()) {
                    float dst = wasteDistance / (float) Statics.visionRange.get();
                    inputs[2 + idx * 4 + 3] = dst;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 3] = dst;
                    }
                }
            }
        }

        if (obstacleDistance > foodDistance) {
            if (foodDistance < wasteDistance) {
                if (!Statics.simpleVision.get()) {
                    inputs[2 + idx * 4] = 1;
                    inputs[2 + idx * 4 + 1] = 0;
                    inputs[2 + idx * 4 + 2] = 0;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4] = 1;
                        MainScreen.eye[idx * 4 + 1] = 0;
                        MainScreen.eye[idx * 4 + 2] = 0;
                    }
                } else {
                    inputs[2 + idx * 4 + 2] = 1;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 2] = 1;
                    }
                }
                if (Statics.depthVision.get()) {
                    float dst = foodDistance / (float) Statics.visionRange.get();
                    inputs[2 + idx * 4 + 3] = dst;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 3] = dst;
                    }
                }
            } else {
                if (!Statics.simpleVision.get()) {
                    inputs[2 + idx * 4] = Color.BROWN.r;
                    inputs[2 + idx * 4 + 1] = Color.BROWN.g;
                    inputs[2 + idx * 4 + 2] = Color.BROWN.b;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4] = Color.BROWN.r;
                        MainScreen.eye[idx * 4 + 1] = Color.BROWN.g;
                        MainScreen.eye[idx * 4 + 2] = Color.BROWN.b;
                    }
                } else {
                    inputs[2 + idx * 4 + 1] = 1;
                    inputs[2 + idx * 4 + 2] = 1;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 1] = 1;
                        MainScreen.eye[idx * 4 + 2] = 1;
                    }
                }
                if (Statics.depthVision.get()) {
                    float dst = wasteDistance / (float) Statics.visionRange.get();
                    inputs[2 + idx * 4 + 3] = dst;
                    if (MainScreen.selectedCreature == this) {
                        MainScreen.eye[idx * 4 + 3] = dst;
                    }
                }
            }
        }

        return det;
    }

    boolean eye = true;

    public double sender0 = 0;
    public double sender1 = 0;

    public double time = 0;

    public int age = 0;

    Vector2 tmp0 = new Vector2();
    Vector2 tmp1 = new Vector2();

    public void update() {
        frameCounter++;

        time += 1;

        double[] inputs = new double[2 + (Statics.visionRays.get() * (eye ? 4 : 0)) + 4 + 1 + 2 + 1];
        if (Statics.positionInput.get()) {
            inputs[0] = x / ((float) Statics.mapSize) * 2 - 1;
            inputs[1] = y / ((float) Statics.mapSize) * 2 - 1;
        }
        int idx = 0;
        // EYE
        if (eye) {
            for (int i = 0; i < Statics.visionRays.get(); i++) {
                float dir = i / (float) Statics.visionRays.get();
                dir *= MathUtils.PI2;
                float x = MathUtils.cos(dir) * visionRange.get();
                float y = MathUtils.sin(dir) * visionRange.get();
                bCheck(inputs, idx, tmp0.set(this.x, this.y), tmp1.set(this.x + x, this.y + y));
                // checkIntersection (MainScreen.creaturesIndex, inputs, idx, new Vector2(this.x, this.y), end);
                idx++;
            }
        }

        // TOUCH
        tempIndex.x = x + 1;
        tempIndex.y = y;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0))] = MainScreen.creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.x = x - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 1] = MainScreen.creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.x = x;
        tempIndex.y = y + 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 2] = MainScreen.creaturesIndex.containsKey(tempIndex) ? 1 : 0;
        tempIndex.y = y - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3] = MainScreen.creaturesIndex.containsKey(tempIndex) ? 1 : 0;

        // CLOCK

        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1] = Statics.oscillatorInput.get() ? clock ? -1 : 1 : 0;

        // COMMUNICATION

        tempIndex.y = y + 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.y = y - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.x = x + 1;
        tempIndex.y = y;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender1 : 0);
        tempIndex.x = x - 1;
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 1] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender0 : 0);
        inputs[2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2] =  (MainScreen.creaturesIndex.containsKey(tempIndex) ? MainScreen.creaturesIndex.get(tempIndex).sender1 : 0);

        int a = 2 + (Statics.visionRays.get() * (eye ? 3 : 0)) + 3 + 1 + 2;

        inputs[a + 1] = (energy / 300f) * 2 - 1;
        inputs[a + 2] = (waste / (float) Statics.wasteCapacity.get()) * 2 - 1;

        double[] outputs = brain.get(inputs);

        float speed = 1;

        if (speedControlAbility.get()) {
            speed = ((float) outputs[3] + 1) + .5f;
        }

        Vector2 oldPosition = new Vector2(x, y);
        if (outputs[0] > .5f)
            x += speed;
        if (outputs[0] < -.5f)
            x -= speed;
        if (outputs[1] > .5f)
            y += speed;
        if (outputs[1] < -.5f)
            y -= speed;

        sender0 = outputs[2];

        if (Statics.defecationAbility.get() && outputs[3] > .5f && waste > 20) {
            Vector2 w = new Vector2(x, y);
            Statics.waste.add(w);
            waste -= 20;
        }

        if (oldPosition.x != x && oldPosition.y != y) {
            moveMeter++;
            energy -= Statics.moveMetabolism.get() * speed;
        }

        if (frameCounter > 5) {
            energy -= Statics.idleMetabolism.get();
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
            Food f = food.get(i);
            if (f != null && f.dst(x, y) < 2) {
                foodEaten++;
                if (Statics.defecationAbility.get()) waste += 20;
                energy += Statics.foodConsumptionGain.get();
                food.removeIndex(i);
                foodChunks.remove((Chunkable) f);
                break;
            }
        }
        x = MathUtils.clamp(x, 0, Statics.mapSize);
        y = MathUtils.clamp(y, 0, Statics.mapSize);
        for (int i = 0; i < MainScreen.creatures.size; i++) {
            Creature creature = MainScreen.creatures.get(i);
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
            MainScreen.creaturesIndex.remove(new Index((int) oldPosition.x, (int) oldPosition.y));
            MainScreen.creaturesIndex.put(new Index(x, y), this);
        }

        x = MathUtils.clamp(x, 0, Statics.mapSize);
        y = MathUtils.clamp(y, 0, Statics.mapSize);

        //if (enableRendering) drawer.line(x - 128, y - 128, x - 128 + MathUtils.cos(getRealDirection() / 4f * MathUtils.PI2) * 10, y - 128 + MathUtils.sin(getRealDirection() / 4f * MathUtils.PI2) * 10, Color.RED, .5f);

        if (age >= Statics.creatureLifespan.get()) {
            energy = 0;
        }

        clock = !clock;

        if (waste >= Statics.wasteCapacity.get() && Statics.defecationAbility.get()) {
            for (int i = 0; i < waste / 20; i++) {
                Vector2 w = new Vector2(x, y);
                Statics.waste.add(w);
            }
            waste = 0;
        }

        age++;

        energy = Math.min(energy, Statics.energyCapacity.get());

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
