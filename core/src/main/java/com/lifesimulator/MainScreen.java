package com.lifesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

import static com.lifesimulator.Statics.*;

public class MainScreen implements Screen {

	public static Array<Creature> creatures = new Array<>();

	public static HashMap<Index, Creature> creaturesIndex = new HashMap<>();

	int timer = 0;

	Vector2 mouse = new Vector2();
	Vector2 oldMouse = new Vector2();

	public Vector2[] neuronPositions;
	float[] population = new float[1];

	boolean threaded = false;

	Vector2 tempVector = new Vector2();

	public final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
	public final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

	ImBoolean enableRendering = new ImBoolean(true);

	int otimer = 0;

	boolean stopSimulation;

	boolean hoveringAnyWindow = false;

	public void reset() {

		timer = 0;
        otimer = 0;

        food.clear();
		obstacles.clear();
		creatures.clear();
		creaturesIndex.clear();
		population = new float[] {maxCreatures.get()};

		for (int i = 0; i < 20; i++) {
			Utils.addFood(new Vector2(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize)));
		}

		for (int i = 0; i < maxCreatures.get(); i++) {
            Creature c = new Creature(MathUtils.random(0, mapSize), MathUtils.random(0, mapSize));
            Utils.addCreature(creatures, c);
			creaturesIndex.put(new Index(creatures.peek().x, creatures.peek().y), creatures.peek());
            chunks.add(c);
		}

		neuronPositions = new Vector2[creatures.get(0).totalNeurons];

		for (int i = 0; i < neuronPositions.length; i++) {
			neuronPositions[i] = new Vector2(MathUtils.random(0f,100f), MathUtils.random(0f,100f));
		}

	}

	@Override
	public void show() {

		long handle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
		GLFW.glfwMakeContextCurrent(handle);
		ImGui.setCurrentContext(ImGui.createContext());

		ImPlot.createContext();

		imGuiGlfw.init(handle, true);
		imGuiGl3.init("#version 330");

        Utils.init();

		reset();

		camera.position.add(mapSize / 2f,mapSize / 2f,0);
	}

	void updateCreatures() {
        for (int i = 0; i < creatures.size; i++) {
            Creature creature = creatures.get(i);
            if (creature.energy > creatureBaseEnergy.get() + 20 && MathUtils.randomBoolean(.01f + ((creature.energy - 7f) / 24f)) && creatures.size < maxCreatures.get()) {
                Creature c = new Creature(creature, MathUtils.random(0,mapSize), MathUtils.random(0, mapSize));
                Utils.addCreature(creatures, c);
                chunks.add(c);
                creature.energy -= creatureBaseEnergy.get() + 4;
            }
            creature.update(creaturesIndex, creatures, obstacles, food, drawer, false);
            if (creature.energy <= 0) {
                creatures.removeIndex(i);
                creaturesIndex.remove(new Index(creature.x, creature.y));
                chunks.remove(creature);
            }
        }
        chunks.update();
	}

	void simulate() {
		updateCreatures();
		timer++;
		otimer++;
        if (otimer % obstacleSpawnInterval.get() == 0) {
            Circle obstacle = new Circle(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize), MathUtils.random(1,10));
            float x = obstacle.x;
            float y = obstacle.y;
            float radius = obstacle.radius;
            obstacles.add(obstacle);
        }
        if (timer >= foodSpawnInterval.get()) {
            timer -= foodSpawnInterval.get();
            for (int k = 0; k < foodSpawnAmount.get(); k++) {
                Utils.addFood(new Vector2(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize)));
            }
        }

        if (changePosition.get() && otimer % 300 == 0) {

            for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
                float x = obstacle.x;
                float y = obstacle.y;
                float radius = obstacle.radius;
                obstacle.setPosition(MathUtils.random(0, mapSize), MathUtils.random(0, mapSize));
            }

            for (int i = 0; i < creatures.size; i++) {
                Creature creature = creatures.get(i);
                creature.x = MathUtils.random(0,mapSize);
                creature.y = MathUtils.random(0,mapSize);

                boolean isInsideObstacle = false;
                for (int j = 0; j < obstacles.size; j++) {
                    Circle obstacle = obstacles.get(j);
                    isInsideObstacle |= obstacle.contains(creature.x, creature.y);
                }
                while (isInsideObstacle) {
                    isInsideObstacle = false;
                    creature.x = MathUtils.random(80,120);
                    creature.y = MathUtils.random(80,120);
                    for (int j = 0; j < obstacles.size; j++) {
                        Circle obstacle = obstacles.get(j);
                        isInsideObstacle |= obstacle.contains(creature.x, creature.y);
                    }
                }
            }
        }

        ArrayList<Float> newPopulation = new ArrayList<>();
        for (int k = (population.length < 200 ? 0 : 1); k < population.length; k++) {
            newPopulation.add(population[k]);
        }
        newPopulation.add((float) creatures.size);
        float[] b = new float[newPopulation.size()];
        for (int k = 0; k < newPopulation.size(); k++) {
            b[k] = newPopulation.get(k);
        }
        population = b;
	}

    public static float[] eye = new float[20];

	@Override
	public void render(float delta) {

		ScreenUtils.clear(Color.WHITE);

		viewport.apply();

		mouse.set(Gdx.input.getX(), Gdx.input.getY());

		float deltaX = mouse.x - oldMouse.x;
		float deltaY = mouse.y - oldMouse.y;

		tempVector.set(deltaX, deltaY);
		tempVector.x = MathUtils.map(0, Gdx.graphics.getWidth(), 0, 1920 / 5f, deltaX);
		tempVector.y = MathUtils.map(0, Gdx.graphics.getHeight(), 0, 1080 / 5f, deltaY);
		tempVector.scl(camera.zoom);

		if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) && !hoveringAnyWindow)
			camera.position.add(-tempVector.x, tempVector.y, 0);

		if (!hoveringAnyWindow) {
			Statics.camera.zoom += inputProcessor.scrollY / 10f * Statics.camera.zoom;
		}
		inputProcessor.scrollY = 0;

		oldMouse.set(mouse);

		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();

		if (enableRendering.get()) {

			for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
				if (obstacle != null) {
					drawer.filledCircle(obstacle.x, obstacle.y, obstacle.radius, Color.BLACK);
				}
			}

            for (int i = 0; i < food.size; i++) {
                Vector2 f = food.get(i);
				drawer.filledCircle(f.x, f.y, 1, Color.RED);
			}

            int idx = 0;
            for (int i = 0; i < creatures.size; i++) {
                Creature creature = creatures.get(i);
				drawer.filledRectangle(creature.x - (creatureDisplaySize.get() / 2f), creature.y - (creatureDisplaySize.get() / 2f), creatureDisplaySize.get(), creatureDisplaySize.get(), creature.color);

                idx++;
			}

		}

		batch.end();

		hoveringAnyWindow = false;

		imGuiGlfw.newFrame();
		ImGui.newFrame();

		ImGui.begin("Settings");

        ImGui.text("Creature:");
        ImGui.indent();

        ImGui.checkbox("Change Position", changePosition);
        if (ImGui.isItemHovered())
            ImGui.setTooltip("Every 300 frames the position of obstacles and creatures with be reset");
        ImGui.checkbox("Kill when touching obstacle", killOnTouch);
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Display Size", creatureDisplaySize);
		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Number of hidden neurons", numberOfHiddenNeurons);
		ImGui.setNextItemWidth(120);
		ImGui.inputFloat("Mutation Rate", mutationRate);
		ImGui.setNextItemWidth(120);
		ImGui.inputFloat("Learning Rate", learningRate);
		ImGui.setNextItemWidth(120);
		ImGui.inputFloat("Idle Metabolism", idleMetabolism);
        ImGui.setNextItemWidth(120);
        ImGui.inputFloat("Move Metabolism", moveMetabolism);
        ImGui.setNextItemWidth(120);
        ImGui.inputFloat("Base Energy", creatureBaseEnergy);
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Vision Range", visionRange);
        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Vision Rays", visionRays);

        ImGui.unindent();

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Food Spawn Interval", foodSpawnInterval);
		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Obstacle Spawn Interval", obstacleSpawnInterval);
		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Food Spawn Amount", foodSpawnAmount);

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputFloat("Food Consumption Energy Gain", foodConsumptionGain);
		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Max creatures", maxCreatures);

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.beginDisabled(threaded);

        if (ImGui.button("Simulate")) {
            stopSimulation = false;
            new Thread(() -> {
                while (!stopSimulation && creatures.size > 0) {
                    threaded = true;
                    simulate();
                }
                threaded = false;
            }).start();
        }

		ImGui.spacing();

		if (ImGui.button("Single Step")) {
			simulate();
		}

		ImGui.spacing();

		if (ImGui.button("Reset"))
			reset();

		ImGui.endDisabled();

		ImGui.spacing();

		ImGui.beginDisabled(!threaded);

		if (ImGui.button("Stop")) {
			stopSimulation = true;
		}

		ImGui.endDisabled();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.checkbox("Enable Rendering", enableRendering);

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.plotLines("Population", population, population.length);

		ImGui.spacing();

		ImGui.text("FPS:            " + Utils.addCommasToNumericString(String.valueOf((int) (1 / delta))));
		ImGui.text("Frame:          " + Utils.addCommasToNumericString(String.valueOf(otimer)));
		ImGui.text("Population:     " + Utils.addCommasToNumericString(String.valueOf(creatures.size)));

		hoveringAnyWindow |= ImGui.isWindowHovered() | ImGui.isAnyItemHovered();

		ImGui.end();

		ImGui.render();
		imGuiGl3.renderDrawData(ImGui.getDrawData());

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() {}

	public void close() {
		stopSimulation = true;
	}

}
