package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    public static ChunkSystem foodChunks = new ChunkSystem(16, 16, mapSize / 16, mapSize / 16);
    public static ChunkSystem obstaclesChunks = new ChunkSystem(16, 16, mapSize / 16, mapSize / 16);

    public static Array<Obstacle> obstacles = new Array<>();
    public static Array<Food> food = new Array<>();
    public static Array<Vector2> waste = new Array<>();

    public static ImBoolean changePosition = new ImBoolean(false);
    public static ImInt numberOfHiddenNeurons = new ImInt(50);
    public static ImInt numberOfConnections = new ImInt(100);
    public static ImInt creatureDisplaySize = new ImInt(5);
    public static ImFloat mutationChance = new ImFloat(.009f);
    public static ImFloat learningRate = new ImFloat(.001f);
    public static ImFloat foodConsumptionGain = new ImFloat(100);
    public static ImFloat idleMetabolism = new ImFloat(1f);
    public static ImFloat moveMetabolism = new ImFloat(1f);
    public static ImFloat creatureBaseEnergy = new ImFloat(34f);
    public static ImFloat backgroundRadiation = new ImFloat(0);
    public static ImInt foodSpawnInterval = new ImInt(10);
    public static ImInt obstacleSpawnInterval = new ImInt(100);
    public static ImInt foodSpawnAmount = new ImInt(20);
    public static ImInt maxCreatures = new ImInt(750);
    public static ImInt maxFood = new ImInt(100);
    public static ImInt maxObstacles = new ImInt(100);
    public static ImInt visionRange = new ImInt(50);
    public static ImInt visionRays = new ImInt(50);
    public static ImInt creatureLifespan = new ImInt(999999);
    public static ImInt energyCapacity = new ImInt(99999);
    public static ImInt wasteCapacity = new ImInt(100);
    public static ImInt wasteLifetime = new ImInt(100);
    public static ImBoolean killOnTouch = new ImBoolean(false);
    public static ImBoolean canSeeOthers = new ImBoolean(true);
    public static ImBoolean positionInput = new ImBoolean(false);
    public static ImBoolean oscillatorInput = new ImBoolean(true);
    public static ImBoolean speedControlAbility = new ImBoolean(false);
    public static ImBoolean neuronASMutation = new ImBoolean(true);
    public static ImBoolean simpleVision = new ImBoolean(true);
    public static ImBoolean depthVision = new ImBoolean(true);
    public static ImBoolean defecationAbility = new ImBoolean(false);
    public static ImBoolean toxicEnabled = new ImBoolean(false);
    public static ImBoolean radiationEnabled = new ImBoolean(false);
    public static ImBoolean useDst2 = new ImBoolean(false);

    public static Rectangle creatureSpawnArea = new Rectangle(0, 0, mapSize, mapSize);
    public static Rectangle foodSpawnArea = new Rectangle(0, 0, mapSize, mapSize);
    public static Rectangle toxicArea = new Rectangle(0, 0, mapSize, mapSize);
    public static Rectangle radiationArea = new Rectangle(0, 0, mapSize, mapSize);

    public static Color backgroundColor = new Color(Color.WHITE);
    public static Color borderColor = new Color(Color.WHITE);

}
