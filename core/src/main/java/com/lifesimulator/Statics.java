package com.lifesimulator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Statics {

    public static SpriteBatch batch;
    public static ShapeDrawer drawer;
    public static ExtendViewport viewport;
    public static OrthographicCamera camera;

    public static ImBoolean obstacleMovement = new ImBoolean(true);
    public static ImInt numberOfHiddenNeurons = new ImInt(20);
    public static ImFloat mutationRate = new ImFloat(.008f);
    public static ImFloat learningRate = new ImFloat(.001f);
    public static ImFloat foodConsumptionGain = new ImFloat(100);
    public static ImFloat idleMetabolism = new ImFloat(.5f);
    public static ImFloat moveMetabolism = new ImFloat(1f);
    public static ImFloat creatureBaseEnergy = new ImFloat(34f);
    public static ImInt foodSpawnInterval = new ImInt(10);
    public static ImInt obstacleSpawnInterval = new ImInt(200);
    public static ImInt foodSpawnAmount = new ImInt(20);
    public static ImInt maxCreatures = new ImInt(1000);

}
