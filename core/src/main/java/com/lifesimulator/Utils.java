package com.lifesimulator;

import com.badlogic.gdx.Gdx;
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

    public static Color hsvToRgb(float hue, float saturation, float value) {

        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0: return rgbToColor(value, t, p);
            case 1: return rgbToColor(q, value, p);
            case 2: return rgbToColor(p, value, t);
            case 3: return rgbToColor(p, q, value);
            case 4: return rgbToColor(t, p, value);
            case 5: return rgbToColor(value, p, q);
            default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
    }

    public static Color rgbToColor(float r, float g, float b) {
        return new Color(r, g, b, 1);
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

    public static void addFood(Vector2 f) {
        boolean isInsideObstacle = false;
        for (int i = 0; i < obstacles.size; i++) {
            Circle obstacle = obstacles.get(i);
            isInsideObstacle |= obstacle.contains(f);
        }
        while (isInsideObstacle) {
            isInsideObstacle = false;
            f.set(new Vector2(MathUtils.random(0,mapSize), MathUtils.random(0, mapSize)));
            for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
                isInsideObstacle |= obstacle.contains(f);
            }
        }
        food.add(f);
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

}
