package com.lifesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiTreeNodeFlags;
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

    ImBoolean restartSimulationIfEnded = new ImBoolean(false);

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
            Creature c = new Creature((int) MathUtils.random(spawnArea.x, spawnArea.width), (int) MathUtils.random(spawnArea.y, spawnArea.height));
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
                Creature c = new Creature(creature, creature.x + 1, creature.y);
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
        if (canSeeOthers.get()) chunks.update();
	}

	void simulate() {
		updateCreatures();
		timer++;
		otimer++;
        if (otimer % obstacleSpawnInterval.get() == 0 && obstacles.size < maxObstacles.get()) {
            Circle obstacle = new Circle(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize), MathUtils.random(1,10));
            obstacles.add(obstacle);
        }
        if (timer >= foodSpawnInterval.get() && food.size < maxFood.get()) {
            timer -= foodSpawnInterval.get();
            for (int k = 0; k < foodSpawnAmount.get(); k++) {
                if (food.size < maxFood.get())
                    Utils.addFood(new Vector2(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize)));
                else
                    break;
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

    float delay;

    boolean edit = false;

    int threadMemoryLifetime = 0;

    void tc() {
        boolean s = true;
        while (!stopSimulation && (s || threadMemoryLifetime >= 250)) {

            s = false;
            threadMemoryLifetime = 0;
            while (creatures.size > 0 && threadMemoryLifetime < 250) {
                threaded = true;
                double start = System.currentTimeMillis();
                simulate();
                delay = (float) (System.currentTimeMillis() - start);
                threadMemoryLifetime++;
            }
        }

        if (restartSimulationIfEnded.get()) {
            stopSimulation = false;
            reset();
            tc();
        } else {
            stopSimulation = true;
        }
        threaded = false;
    }

    void thread() {
        stopSimulation = false;
        new Thread(this::tc).start();
    }

    int editing = 0;

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

        mouse.set(viewport.unproject(mouse));

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

        if (edit) {
            drawer.rectangle(spawnArea.x, spawnArea.y, spawnArea.width - spawnArea.x, spawnArea.height - spawnArea.y, Color.DARK_GRAY, 2);
            drawer.setColor(.5f,.5f,.5f,.5f);
            drawer.filledRectangle(spawnArea.x, spawnArea.y, spawnArea.width - spawnArea.x, spawnArea.height - spawnArea.y);
            drawer.setColor(Color.WHITE);

            drawer.filledCircle(spawnArea.x, spawnArea.y, 7, Color.DARK_GRAY);
            drawer.filledCircle(spawnArea.x, spawnArea.y, 5, Color.LIGHT_GRAY);

            drawer.filledCircle(spawnArea.width, spawnArea.y, 7, Color.DARK_GRAY);
            drawer.filledCircle(spawnArea.width, spawnArea.y, 5, Color.LIGHT_GRAY);

            drawer.filledCircle(spawnArea.width, spawnArea.height, 7, Color.DARK_GRAY);
            drawer.filledCircle(spawnArea.width, spawnArea.height, 5, Color.LIGHT_GRAY);

            drawer.filledCircle(spawnArea.x, spawnArea.height, 7, Color.DARK_GRAY);
            drawer.filledCircle(spawnArea.x, spawnArea.height, 5, Color.LIGHT_GRAY);

            boolean move;

            move = mouse.dst2(spawnArea.x, spawnArea.y) < 40 * 40;

            if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 1;

            move = mouse.dst2(spawnArea.width, spawnArea.y) < 40 * 40;

            if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 2;

            move = mouse.dst2(spawnArea.x, spawnArea.height) < 40 * 40;

            if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 3;

            move = mouse.dst2(spawnArea.width, spawnArea.height) < 40 * 40;

            if (move && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && editing == 0) editing = 4;

            if (editing == 1) {
                spawnArea.x = mouse.x;
                spawnArea.x = Math.max(Math.min(spawnArea.x, mapSize), 0);
                spawnArea.y = mouse.y;
                spawnArea.y = Math.max(Math.min(spawnArea.y, mapSize), 0);
            } else if (editing == 2) {
                spawnArea.width = mouse.x;
                spawnArea.width = Math.max(Math.min(spawnArea.width, mapSize), 0);
                spawnArea.y = mouse.y;
                spawnArea.y = Math.max(Math.min(spawnArea.y, mapSize), 0);
            } else if (editing == 3) {
                spawnArea.x = mouse.x;
                spawnArea.x = Math.max(Math.min(spawnArea.x, mapSize), 0);
                spawnArea.height = mouse.y;
                spawnArea.height = Math.max(Math.min(spawnArea.height, mapSize), 0);
            } else if (editing == 4) {
                spawnArea.width = mouse.x;
                spawnArea.width = Math.max(Math.min(spawnArea.width, mapSize), 0);
                spawnArea.height = mouse.y;
                spawnArea.height = Math.max(Math.min(spawnArea.height, mapSize), 0);
            }

            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) editing = 0;

            /*spawnArea.x = MathUtils.clamp(spawnArea.x, 0, spawnArea.width - 1);
            spawnArea.y = MathUtils.clamp(spawnArea.y, 0, spawnArea.height - 1);
            spawnArea.width = MathUtils.clamp(spawnArea.width, spawnArea.x + 1, mapSize);
            spawnArea.height = MathUtils.clamp(spawnArea.height, spawnArea.y + 1, mapSize);*/
        }

		batch.end();

		hoveringAnyWindow = false;

		imGuiGlfw.newFrame();
		ImGui.newFrame();

		ImGui.begin("Settings");

        if (ImGui.treeNodeEx("Creature", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.checkbox("Change Position", changePosition);
            if (ImGui.isItemHovered())
                ImGui.setTooltip("Every 300 frames the position of obstacles and creatures with be reset");
            ImGui.checkbox("Kill when touching obstacle", killOnTouch);
            ImGui.checkbox("Can See Other Creatures", canSeeOthers);
            ImGui.checkbox("Position Input", positionInput);
            ImGui.checkbox("Oscillator Input", oscillatorInput);
            ImGui.checkbox("Speed Mutation", speedMutation);
            ImGui.checkbox("Neuron Addition/Subtraction Mutation", neuronASMutation);
            ImGui.checkbox("Simple Vision", simpleVision);
            ImGui.checkbox("Depth Vision", depthVision);
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

            ImGui.treePop();

        }

		ImGui.spacing();
        if (ImGui.treeNodeEx("Environment", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Food Spawn Interval", foodSpawnInterval);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Obstacle Spawn Interval", obstacleSpawnInterval);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Food Spawn Amount", foodSpawnAmount);
            ImGui.setNextItemWidth(120);
            ImGui.inputFloat("Food Consumption Energy Gain", foodConsumptionGain);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Max creatures", maxCreatures);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Max Food", maxFood);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Max Obstacles", maxObstacles);

            if (ImGui.button("Remove Every Food", 170, 20)) food.clear();
            if (ImGui.button("Remove Every Obstacle", 170, 20)) obstacles.clear();

            if (ImGui.treeNode("Areas")) {

                if (ImGui.button(edit ? "Done" : "Edit Creature Spawn Area", 180, 30)) {
                    edit = !edit;
                    if (edit) stopSimulation = true;
                }

                if (ImGui.button(edit ? "Done" : "Edit Food Spawn Area", 180, 30)) {
                    edit = !edit;
                    if (edit) stopSimulation = true;
                }

                if (ImGui.button(edit ? "Done" : "Edit Obstacle Spawn Area", 180, 30)) {
                    edit = !edit;
                    if (edit) stopSimulation = true;
                }

                if (ImGui.button(edit ? "Done" : "Edit Toxic Area", 180, 30)) {
                    edit = !edit;
                    if (edit) stopSimulation = true;
                }

                ImGui.treePop();

            }

            ImGui.treePop();

        }

        ImGui.text("Simulation:");
        ImGui.indent();

        ImGui.checkbox("Restart Simulation If Simulation Ended", restartSimulationIfEnded);

        ImGui.unindent();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.beginDisabled(threaded || edit);

        if (ImGui.button("Simulate")) {
            stopSimulation = false;
            thread();
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

        ImGui.text("Simulation Delay:  " + ((int) delay) + "ms" + " / " + ((int) (1000 / delay)) + " FPS");
        ImGui.text("FPS:               " + Utils.addCommasToNumericString(String.valueOf((int) (1 / delta))));
		ImGui.text("Frame:             " + Utils.addCommasToNumericString(String.valueOf(otimer)));
		ImGui.text("Population:        " + Utils.addCommasToNumericString(String.valueOf(creatures.size)));

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
