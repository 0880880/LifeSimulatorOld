package com.lifesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.lifesimulator.Statics.*;
import static com.lifesimulator.Statics.inputProcessor;

public class Utils {

    public static Color hsvToRgb(float hue, float saturation, float brightness) {

        java.awt.Color jColor = java.awt.Color.getHSBColor(hue, saturation, brightness);

        return new Color(jColor.getRed() / 255f, jColor.getGreen() / 255f, jColor.getBlue() / 255f, 1f);

    }

    public static void init() {
        batch = new SpriteBatch();
        drawer = new ShapeDrawer(batch, new TextureRegion(new Texture(new APixmap(1, 1, Pixmap.Format.RGBA8888, Color.WHITE))));

        viewport = new ExtendViewport(1920 / 5, 1080 / 5);
        camera = (OrthographicCamera) viewport.getCamera();

        Gdx.input.setInputProcessor(inputProcessor = new InputProcessor());
    }

    public static void addCreature(Array<Creature> array, Creature creature) {
        boolean isInsideOtherCreature = false;
        Array<Array<Chunkable>> chunks = Statics.chunks.getChunksInRange(creature.getPosition(), 32);
        if (chunks.size > 0) {
            for (int i = 0; i < chunks.size; i++) {
                Array<Chunkable> chunk = chunks.get(i);
                for (int j = 0; j < chunk.size; j++) {
                    if (chunk.get(j).getPosition().equals(creature.getPosition()))
                        isInsideOtherCreature = true;
                }
            }
        }
        while (isInsideOtherCreature) {
            isInsideOtherCreature = false;
            creature.x++;
            if (chunks.size > 0) {
                for (int i = 0; i < chunks.size; i++) {
                    Array<Chunkable> chunk = chunks.get(i);
                    for (int j = 0; j < chunk.size; j++) {
                        if (chunk.get(j).getPosition().equals(creature.getPosition()))
                            isInsideOtherCreature = true;
                    }
                }
            }
        }
        /*for (int i = 0; i < obstacles.size; i++) {
            Circle obstacle = obstacles.get(i);
            isInsideObstacle |= obstacle.contains(creature.x, creature.y);
        }
        while (isInsideObstacle) {
            isInsideObstacle = false;
            creature.x = MathUtils.random(0,mapSize);
            creature.y = MathUtils.random(0,mapSize);
            for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
                isInsideObstacle |= obstacle.contains(creature.x, creature.y);
            }
        }*/
        array.add(creature);
    }

    public static void addFood(Food f) {
        boolean isInsideObstacle = false;
        for (int i = 0; i < obstacles.size; i++) {
            Circle obstacle = obstacles.get(i);
            isInsideObstacle |= obstacle.contains(f);
        }
        while (isInsideObstacle) {
            isInsideObstacle = false;
            f.set(new Food(MathUtils.random(0,mapSize), MathUtils.random(0, mapSize)));
            for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
                isInsideObstacle |= obstacle.contains(f);
            }
        }

        food.add(f);
        foodChunks.add(f);
    }

    public static boolean intersect(Vector2 lineStart, Vector2 lineEnd, Vector2 circleCenter, float circleRadius) {
        Vector2 lineDir = lineEnd.cpy().sub(lineStart).nor();

        Vector2 lineToCircle = circleCenter.cpy().sub(lineStart);

        float projection = lineToCircle.dot(lineDir);

        Vector2 closestPoint;
        if (projection <= 0) {
            closestPoint = lineStart.cpy();
        } else if (projection >= lineEnd.dst(lineStart)) {
            closestPoint = lineEnd.cpy();
        } else {
            Vector2 scaledDir = lineDir.cpy().scl(projection);
            closestPoint = lineStart.cpy().add(scaledDir);
        }

        return closestPoint.dst(circleCenter) <= circleRadius;
    }

    public static String addCommasToNumericString(String digits) {
        String result = "";
        for (int i=1; i <= digits.length(); ++i) {
            char ch = digits.charAt(digits.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        return result;
    }

    public static void csEdit(int editing, Vector2 mouse) {
        drawer.rectangle(creatureSpawnArea.x, creatureSpawnArea.y, creatureSpawnArea.width - creatureSpawnArea.x, creatureSpawnArea.height - creatureSpawnArea.y, Color.DARK_GRAY, 2);
        drawer.setColor(.5f,.5f,.5f,.5f);
        drawer.filledRectangle(creatureSpawnArea.x, creatureSpawnArea.y, creatureSpawnArea.width - creatureSpawnArea.x, creatureSpawnArea.height - creatureSpawnArea.y);
        drawer.setColor(Color.WHITE);

        drawer.filledCircle(creatureSpawnArea.x, creatureSpawnArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(creatureSpawnArea.x, creatureSpawnArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(creatureSpawnArea.width, creatureSpawnArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(creatureSpawnArea.width, creatureSpawnArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(creatureSpawnArea.width, creatureSpawnArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(creatureSpawnArea.width, creatureSpawnArea.height, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(creatureSpawnArea.x, creatureSpawnArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(creatureSpawnArea.x, creatureSpawnArea.height, 5, Color.LIGHT_GRAY);

        boolean move;

        move = mouse.dst2(creatureSpawnArea.x, creatureSpawnArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 1;

        move = mouse.dst2(creatureSpawnArea.width, creatureSpawnArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 2;

        move = mouse.dst2(creatureSpawnArea.x, creatureSpawnArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 3;

        move = mouse.dst2(creatureSpawnArea.width, creatureSpawnArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 4;

        if (editing == 1) {
            creatureSpawnArea.x = mouse.x;
            creatureSpawnArea.x = Math.max(Math.min(creatureSpawnArea.x, mapSize), 0);
            creatureSpawnArea.y = mouse.y;
            creatureSpawnArea.y = Math.max(Math.min(creatureSpawnArea.y, mapSize), 0);
        } else if (editing == 2) {
            creatureSpawnArea.width = mouse.x;
            creatureSpawnArea.width = Math.max(Math.min(creatureSpawnArea.width, mapSize), 0);
            creatureSpawnArea.y = mouse.y;
            creatureSpawnArea.y = Math.max(Math.min(creatureSpawnArea.y, mapSize), 0);
        } else if (editing == 3) {
            creatureSpawnArea.x = mouse.x;
            creatureSpawnArea.x = Math.max(Math.min(creatureSpawnArea.x, mapSize), 0);
            creatureSpawnArea.height = mouse.y;
            creatureSpawnArea.height = Math.max(Math.min(creatureSpawnArea.height, mapSize), 0);
        } else if (editing == 4) {
            creatureSpawnArea.width = mouse.x;
            creatureSpawnArea.width = Math.max(Math.min(creatureSpawnArea.width, mapSize), 0);
            creatureSpawnArea.height = mouse.y;
            creatureSpawnArea.height = Math.max(Math.min(creatureSpawnArea.height, mapSize), 0);
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) editing = 0;
    }

    public static void fsEdit(int editing, Vector2 mouse) {
        drawer.rectangle(foodSpawnArea.x, foodSpawnArea.y, foodSpawnArea.width - foodSpawnArea.x, foodSpawnArea.height - foodSpawnArea.y, Color.DARK_GRAY, 2);
        drawer.setColor(.5f,.5f,.5f,.5f);
        drawer.filledRectangle(foodSpawnArea.x, foodSpawnArea.y, foodSpawnArea.width - foodSpawnArea.x, foodSpawnArea.height - foodSpawnArea.y);
        drawer.setColor(Color.WHITE);

        drawer.filledCircle(foodSpawnArea.x, foodSpawnArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(foodSpawnArea.x, foodSpawnArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(foodSpawnArea.width, foodSpawnArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(foodSpawnArea.width, foodSpawnArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(foodSpawnArea.width, foodSpawnArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(foodSpawnArea.width, foodSpawnArea.height, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(foodSpawnArea.x, foodSpawnArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(foodSpawnArea.x, foodSpawnArea.height, 5, Color.LIGHT_GRAY);

        boolean move;

        move = mouse.dst2(foodSpawnArea.x, foodSpawnArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 1;

        move = mouse.dst2(foodSpawnArea.width, foodSpawnArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 2;

        move = mouse.dst2(foodSpawnArea.x, foodSpawnArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 3;

        move = mouse.dst2(foodSpawnArea.width, foodSpawnArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 4;

        if (editing == 1) {
            foodSpawnArea.x = mouse.x;
            foodSpawnArea.x = Math.max(Math.min(foodSpawnArea.x, mapSize), 0);
            foodSpawnArea.y = mouse.y;
            foodSpawnArea.y = Math.max(Math.min(foodSpawnArea.y, mapSize), 0);
        } else if (editing == 2) {
            foodSpawnArea.width = mouse.x;
            foodSpawnArea.width = Math.max(Math.min(foodSpawnArea.width, mapSize), 0);
            foodSpawnArea.y = mouse.y;
            foodSpawnArea.y = Math.max(Math.min(foodSpawnArea.y, mapSize), 0);
        } else if (editing == 3) {
            foodSpawnArea.x = mouse.x;
            foodSpawnArea.x = Math.max(Math.min(foodSpawnArea.x, mapSize), 0);
            foodSpawnArea.height = mouse.y;
            foodSpawnArea.height = Math.max(Math.min(foodSpawnArea.height, mapSize), 0);
        } else if (editing == 4) {
            foodSpawnArea.width = mouse.x;
            foodSpawnArea.width = Math.max(Math.min(foodSpawnArea.width, mapSize), 0);
            foodSpawnArea.height = mouse.y;
            foodSpawnArea.height = Math.max(Math.min(foodSpawnArea.height, mapSize), 0);
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) editing = 0;
    }

    public static void tEdit(int editing, Vector2 mouse) {
        drawer.rectangle(toxicArea.x, toxicArea.y, toxicArea.width - toxicArea.x, toxicArea.height - toxicArea.y, Color.DARK_GRAY, 2);
        drawer.setColor(.5f,.5f,.5f,.5f);
        drawer.filledRectangle(toxicArea.x, toxicArea.y, toxicArea.width - toxicArea.x, toxicArea.height - toxicArea.y);
        drawer.setColor(Color.WHITE);

        drawer.filledCircle(toxicArea.x, toxicArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(toxicArea.x, toxicArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(toxicArea.width, toxicArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(toxicArea.width, toxicArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(toxicArea.width, toxicArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(toxicArea.width, toxicArea.height, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(toxicArea.x, toxicArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(toxicArea.x, toxicArea.height, 5, Color.LIGHT_GRAY);

        boolean move;

        move = mouse.dst2(toxicArea.x, toxicArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 1;

        move = mouse.dst2(toxicArea.width, toxicArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 2;

        move = mouse.dst2(toxicArea.x, toxicArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 3;

        move = mouse.dst2(toxicArea.width, toxicArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 4;

        if (editing == 1) {
            toxicArea.x = mouse.x;
            toxicArea.x = Math.max(Math.min(toxicArea.x, mapSize), 0);
            toxicArea.y = mouse.y;
            toxicArea.y = Math.max(Math.min(toxicArea.y, mapSize), 0);
        } else if (editing == 2) {
            toxicArea.width = mouse.x;
            toxicArea.width = Math.max(Math.min(toxicArea.width, mapSize), 0);
            toxicArea.y = mouse.y;
            toxicArea.y = Math.max(Math.min(toxicArea.y, mapSize), 0);
        } else if (editing == 3) {
            toxicArea.x = mouse.x;
            toxicArea.x = Math.max(Math.min(toxicArea.x, mapSize), 0);
            toxicArea.height = mouse.y;
            toxicArea.height = Math.max(Math.min(toxicArea.height, mapSize), 0);
        } else if (editing == 4) {
            toxicArea.width = mouse.x;
            toxicArea.width = Math.max(Math.min(toxicArea.width, mapSize), 0);
            toxicArea.height = mouse.y;
            toxicArea.height = Math.max(Math.min(toxicArea.height, mapSize), 0);
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) editing = 0;
    }

    public static void rEdit(int editing, Vector2 mouse) {
        drawer.rectangle(radiationArea.x, radiationArea.y, radiationArea.width - radiationArea.x, radiationArea.height - radiationArea.y, Color.DARK_GRAY, 2);
        drawer.setColor(.5f,.5f,.5f,.5f);
        drawer.filledRectangle(radiationArea.x, radiationArea.y, radiationArea.width - radiationArea.x, radiationArea.height - radiationArea.y);
        drawer.setColor(Color.WHITE);

        drawer.filledCircle(radiationArea.x, radiationArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(radiationArea.x, radiationArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(radiationArea.width, radiationArea.y, 7, Color.DARK_GRAY);
        drawer.filledCircle(radiationArea.width, radiationArea.y, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(radiationArea.width, radiationArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(radiationArea.width, radiationArea.height, 5, Color.LIGHT_GRAY);

        drawer.filledCircle(radiationArea.x, radiationArea.height, 7, Color.DARK_GRAY);
        drawer.filledCircle(radiationArea.x, radiationArea.height, 5, Color.LIGHT_GRAY);

        boolean move;

        move = mouse.dst2(radiationArea.x, radiationArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 1;

        move = mouse.dst2(radiationArea.width, radiationArea.y) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 2;

        move = mouse.dst2(radiationArea.x, radiationArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 3;

        move = mouse.dst2(radiationArea.width, radiationArea.height) < 40 * 40;

        if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 4;

        if (editing == 1) {
            radiationArea.x = mouse.x;
            radiationArea.x = Math.max(Math.min(radiationArea.x, mapSize), 0);
            radiationArea.y = mouse.y;
            radiationArea.y = Math.max(Math.min(radiationArea.y, mapSize), 0);
        } else if (editing == 2) {
            radiationArea.width = mouse.x;
            radiationArea.width = Math.max(Math.min(radiationArea.width, mapSize), 0);
            radiationArea.y = mouse.y;
            radiationArea.y = Math.max(Math.min(radiationArea.y, mapSize), 0);
        } else if (editing == 3) {
            radiationArea.x = mouse.x;
            radiationArea.x = Math.max(Math.min(radiationArea.x, mapSize), 0);
            radiationArea.height = mouse.y;
            radiationArea.height = Math.max(Math.min(radiationArea.height, mapSize), 0);
        } else if (editing == 4) {
            radiationArea.width = mouse.x;
            radiationArea.width = Math.max(Math.min(radiationArea.width, mapSize), 0);
            radiationArea.height = mouse.y;
            radiationArea.height = Math.max(Math.min(radiationArea.height, mapSize), 0);
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) editing = 0;
    }

}
