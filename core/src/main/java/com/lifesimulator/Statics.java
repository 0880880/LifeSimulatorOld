package com.lifesimulator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
    public static InputProcessor inputProcessor;

    public static int mapSize = 512;

    public static ChunkSystem chunks = new ChunkSystem(16, 16, mapSize / 16, mapSize / 16);

    public static Array<Circle> obstacles = new Array<>();
    public static Array<Vector2> food = new Array<>();

    public static ImBoolean changePosition = new ImBoolean(true);
    public static ImInt numberOfHiddenNeurons = new ImInt(20);
    public static ImInt creatureDisplaySize = new ImInt(1);
    public static ImFloat mutationRate = new ImFloat(.009f);
    public static ImFloat learningRate = new ImFloat(.001f);
    public static ImFloat foodConsumptionGain = new ImFloat(100);
    public static ImFloat idleMetabolism = new ImFloat(1f);
    public static ImFloat moveMetabolism = new ImFloat(1f);
    public static ImFloat creatureBaseEnergy = new ImFloat(34f);
    public static ImInt foodSpawnInterval = new ImInt(10);
    public static ImInt obstacleSpawnInterval = new ImInt(100);
    public static ImInt foodSpawnAmount = new ImInt(20);
    public static ImInt maxCreatures = new ImInt(2000);
    public static ImInt visionRange = new ImInt(30);
    public static ImInt visionRays = new ImInt(20);
    public static ImBoolean killOnTouch = new ImBoolean(false);
    public static ImBoolean canSeeOthers = new ImBoolean(true);

    public static Rectangle spawnArea = new Rectangle(0, 0, mapSize, mapSize);

}
