package com.lifesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
import static imgui.flag.ImGuiWindowFlags.*;

public class MainScreen implements Screen {

	public static Array<Creature> creatures = new Array<>();

	public static HashMap<Index, Creature> creaturesIndex = new HashMap<>();

	int timer = 0;

	Vector2 mouse = new Vector2();
	Vector2 oldMouse = new Vector2();

	public Vector2[] neuronPositions;
    float[] population = new float[1];
    float[] performance = new float[1];
    float[] deathRate = new float[1];
    float[] birthRate = new float[1];

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
        foodChunks.clear();
        obstaclesChunks.clear();
		obstacles.clear();
		creatures.clear();
        waste.clear();
		creaturesIndex.clear();
		population = new float[] {maxCreatures.get()};
        performance = new float[] {0};
        deathRate = new float[] {0};
        birthRate = new float[] {0};

		for (int i = 0; i < 20; i++) {
            Utils.addFood(new Food(MathUtils.random(foodSpawnArea.x, foodSpawnArea.width), MathUtils.random(foodSpawnArea.y, foodSpawnArea.height)));
		}

		for (int i = 0; i < maxCreatures.get(); i++) {
            Creature c = new Creature((int) MathUtils.random(creatureSpawnArea.x, creatureSpawnArea.width), (int) MathUtils.random(creatureSpawnArea.y, creatureSpawnArea.height));
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
                bRate++;
                creature.energy -= creatureBaseEnergy.get() + 4;
            }
            creature.update();
            if (creature.energy <= 0) {
                creatures.removeIndex(i);
                creaturesIndex.remove(new Index(creature.x, creature.y));
                chunks.remove(creature);
                dRate++;
            }
        }
        if (canSeeOthers.get()) chunks.update();
	}

    int dRate = 0;
    int bRate = 0;

	void simulate() {
        dRate = 0;
        bRate = 0;
		updateCreatures();
		timer++;
		otimer++;
        if (otimer % obstacleSpawnInterval.get() == 0 && obstacles.size < maxObstacles.get()) {
            Obstacle obstacle = new Obstacle(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize), MathUtils.random(1,10));
            obstacles.add(obstacle);
            obstaclesChunks.add(obstacle);
        }
        if (timer >= foodSpawnInterval.get() && food.size < maxFood.get()) {
            timer -= foodSpawnInterval.get();
            for (int k = 0; k < foodSpawnAmount.get(); k++) {
                if (food.size < maxFood.get())
                    Utils.addFood(new Food(MathUtils.random(foodSpawnArea.x, foodSpawnArea.width), MathUtils.random(foodSpawnArea.y, foodSpawnArea.height)));
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

        ArrayList<Float> newPerformance = new ArrayList<>();
        for (int k = (performance.length < 200 ? 0 : 1); k < performance.length; k++) {
            newPerformance.add(performance[k]);
        }
        newPerformance.add((float) (int) (1000 / delay));
        float[] c = new float[newPerformance.size()];
        for (int k = 0; k < newPerformance.size(); k++) {
            c[k] = newPerformance.get(k);
        }
        performance = c;

        ArrayList<Float> newDeathRate = new ArrayList<>();
        for (int k = (deathRate.length < 200 ? 0 : 1); k < deathRate.length; k++) {
            newDeathRate.add(deathRate[k]);
        }
        newDeathRate.add((float) dRate);
        c = new float[newDeathRate.size()];
        for (int k = 0; k < newDeathRate.size(); k++) {
            c[k] = newDeathRate.get(k);
        }
        deathRate = c;

        ArrayList<Float> newBirthRate = new ArrayList<>();
        for (int k = (birthRate.length < 200 ? 0 : 1); k < birthRate.length; k++) {
            newBirthRate.add(birthRate[k]);
        }
        newBirthRate.add((float) bRate);
        c = new float[newBirthRate.size()];
        for (int k = 0; k < newBirthRate.size(); k++) {
            c[k] = newBirthRate.get(k);
        }
        birthRate = c;
	}

    public static float[] eye = new float[visionRays.get() * 4];

    float delay;

    boolean edit = false;
    boolean fEdit = false;
    boolean tEdit = false;
    boolean rEdit = false;

    int threadMemoryLifetime = 0;

    void tc() {
        boolean s = true;
        while (!stopSimulation && (s || threadMemoryLifetime >= 250)) {

            s = false;
            threadMemoryLifetime = 0;
            while (creatures.size > 0 && threadMemoryLifetime < 250 && !stopSimulation) {
                threaded = true;
                double start = System.currentTimeMillis();
                simulate();
                delay = (float) (System.currentTimeMillis() - start);
                try {
                    if (simulationSpeed[0] == 2) Thread.sleep((long) delay);
                    if (simulationSpeed[0] == 3) Thread.sleep((long) delay * 3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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

    float[] bgc = new float[] {1, 1, 1};
    float[] bdc = new float[] {1, 1, 1};

    static Creature selectedCreature = null;

    ImBoolean autoFocus = new ImBoolean(false);

    Vector3 v3t = new Vector3();

    public static Vector2[] points = new Vector2[visionRays.get()];

    int[] simulationSpeed = new int[] {1};

	@Override
	public void render(float delta) {

		ScreenUtils.clear(backgroundColor);

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

        if (selectedCreature != null) {
            int idx = 0;
            for (int i = 0; i < eye.length; i += 4) {
                float dir = idx / (float) (visionRays.get());
                dir *= MathUtils.PI2;
                float x = MathUtils.cos(dir) * eye[i + 3] * visionRange.get();
                float y = MathUtils.sin(dir) * eye[i + 3] * visionRange.get();
                drawer.line(selectedCreature.x, selectedCreature.y, selectedCreature.x + x, selectedCreature.y + y, Color.BLACK, .8f);

                if (simpleVision.get()) {
                    if (eye[i] == 1)
                        drawer.setColor(1, 0, 0, 1);
                    if (eye[i + 1] == 1)
                        drawer.setColor(0, 1, 0, 1);
                    if (eye[i + 2] == 1)
                        drawer.setColor(0, 0, 1, 1);
                    if (eye[i + 1] == 1 && eye[i + 2] == 1)
                        drawer.setColor(1, 1, 0, 1);
                } else {
                    drawer.setColor(eye[i], eye[i + 1], eye[i + 2], 1);
                }
                drawer.line(selectedCreature.x, selectedCreature.y, selectedCreature.x + x, selectedCreature.y + y, .5f);
                drawer.setColor(Color.WHITE);
                idx++;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            creatures.get(0).update();

            if (timer >= foodSpawnInterval.get() && food.size < maxFood.get()) {
                timer -= foodSpawnInterval.get();
                for (int k = 0; k < foodSpawnAmount.get(); k++) {
                    if (food.size < maxFood.get())
                        Utils.addFood(new Food(MathUtils.random(foodSpawnArea.x, foodSpawnArea.width), MathUtils.random(foodSpawnArea.y, foodSpawnArea.height)));
                    else
                        break;
                }
            }
            if (otimer % obstacleSpawnInterval.get() == 0 && obstacles.size < maxObstacles.get()) {
                Obstacle obstacle = new Obstacle(MathUtils.random(0,mapSize), MathUtils.random(0,mapSize), MathUtils.random(1,10));
                obstacles.add(obstacle);
                obstaclesChunks.add(obstacle);
            }
            timer++;
            otimer++;
        }

        drawer.rectangle(0, 0, mapSize, mapSize, borderColor, 3);

		if (enableRendering.get()) {

			for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
				if (obstacle != null) {
					drawer.filledCircle(obstacle.x, obstacle.y, obstacle.radius, Color.BLACK);
				}
			}

            for (int i = 0; i < food.size; i++) {
                Vector2 f = food.get(i);
                if (f != null) {
                    drawer.filledCircle(f.x, f.y, 1, Color.RED);
                }
            }

            for (int i = 0; i < waste.size; i++) {
                Vector2 w = waste.get(i);
                if (w != null) {
                    drawer.filledCircle(w.x, w.y, 1, Color.BROWN);
                }
            }

            int idx = 0;
            for (int i = 0; i < creatures.size; i++) {
                Creature creature = creatures.get(i);
				drawer.filledRectangle(creature.x - (creatureDisplaySize.get() / 2f), creature.y - (creatureDisplaySize.get() / 2f), creatureDisplaySize.get(), creatureDisplaySize.get(), creature.color);

                idx++;
			}

		}

        if (edit) Utils.csEdit(editing, mouse);
        if (fEdit) Utils.fsEdit(editing, mouse);
        if (tEdit) Utils.tEdit(editing, mouse);
        if (rEdit) Utils.rEdit(editing, mouse);

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
            ImGui.checkbox("Speed Control Ability", speedControlAbility);
            ImGui.checkbox("Neuron Addition/Subtraction Mutations", neuronASMutation);
            ImGui.checkbox("Simple Vision (Recommended)", simpleVision);
            ImGui.checkbox("Depth Vision (Recommended)", depthVision);
            ImGui.checkbox("Defecation Ability", defecationAbility);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Display Size", creatureDisplaySize);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Number of hidden neurons", numberOfHiddenNeurons);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Number of connections", numberOfConnections);
            ImGui.setNextItemWidth(120);
            ImGui.inputFloat("Mutation Chance", mutationChance);
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
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Lifespan", creatureLifespan);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Energy Capacity", energyCapacity);
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Waste Capacity", wasteCapacity);

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
            ImGui.setNextItemWidth(120);
            ImGui.inputInt("Waste Lifetime", wasteLifetime);

            if (ImGui.button("Remove Every Food", 180, 20)) {
                food.clear();
                foodChunks.clear();
            }
            if (ImGui.button("Remove Every Obstacle", 180, 20)) {
                obstacles.clear();
                obstaclesChunks.clear();
            }
            if (ImGui.button("Remove Every Waste", 180, 20)) waste.clear();

            ImGui.spacing();
            ImGui.spacing();

            ImGui.beginDisabled(fEdit || tEdit || rEdit);
            if (ImGui.button(edit ? "Done" : "Edit Creature Spawn Area", 180, 20)) {
                edit = !edit;
                if (edit) stopSimulation = true;
            }
            ImGui.endDisabled();

            ImGui.beginDisabled(edit || tEdit || rEdit);
            if (ImGui.button(fEdit ? "Done" : "Edit Food Spawn Area", 180, 20)) {
                fEdit = !fEdit;
                if (fEdit) stopSimulation = true;
            }
            ImGui.endDisabled();

            /*ImGui.spacing();

            ImGui.beginDisabled(fEdit || edit || rEdit || tEdit);
            ImGui.checkbox("Enabled##tox", toxicEnabled);
            ImGui.endDisabled();
            ImGui.beginDisabled(!toxicEnabled.get() || fEdit || edit || rEdit);
            if (ImGui.button(tEdit ? "Done" : "Edit Toxic Area", 180, 30)) {
                tEdit = !tEdit;
                if (tEdit) stopSimulation = true;
            }
            ImGui.endDisabled();*/

            ImGui.spacing();

            if (ImGui.treeNode("Radiation")) {

                ImGui.setNextItemWidth(120);
                ImGui.inputFloat("Background Radiation", backgroundRadiation);
                ImGui.setNextItemWidth(120);
                ImGui.inputFloat("Radiation Coefficient", backgroundRadiation);
                ImGui.setNextItemWidth(120);
                ImGui.inputFloat("Radiation Area Intensity", backgroundRadiation);

                ImGui.beginDisabled(fEdit || tEdit || edit || rEdit);
                ImGui.checkbox("Enabled##rad", radiationEnabled);
                ImGui.endDisabled();
                ImGui.beginDisabled(!radiationEnabled.get() || fEdit || tEdit || edit);
                if (ImGui.button(rEdit ? "Done" : "Edit Radiation Area", 180, 30)) {
                    rEdit = !rEdit;
                    if (rEdit) stopSimulation = true;
                }
                ImGui.endDisabled();

                ImGui.treePop();

            }

            ImGui.treePop();

        }

        if (ImGui.treeNodeEx("Simulation", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.checkbox("Restart Simulation If Simulation Ended", restartSimulationIfEnded);
            ImGui.setNextItemWidth(120);
            ImGui.sliderInt("Simulation Speed",  simulationSpeed, 1, 3, simulationSpeed[0] == 1 ? "Full" : "1/" + String.valueOf((simulationSpeed[0] - 1) * 2));
            ImGui.setNextItemWidth(120);
            ImGui.colorEdit3("Background Color", bgc);
            ImGui.setNextItemWidth(120);
            ImGui.colorEdit3("Border Color", bdc);
            ImGui.checkbox("Use Dst2", useDst2);

            backgroundColor.set(bgc[0], bgc[1], bgc[2], 1);
            borderColor.set(bdc[0], bdc[1], bdc[2], 1);

            ImGui.treePop();

        }

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
			close();
		}

		ImGui.endDisabled();

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

        ImGui.checkbox("Enable Rendering", enableRendering);
        ImGui.checkbox("Auto Focus", autoFocus);

		ImGui.spacing();
		ImGui.spacing();

		ImGui.separator();

		ImGui.spacing();
		ImGui.spacing();

        ImGui.plotLines("Population", population, population.length);
        ImGui.plotLines("Death Rate", deathRate, deathRate.length);
        ImGui.plotLines("Birth Rate", birthRate, birthRate.length);
        ImGui.spacing();
        ImGui.plotLines("Performance", performance, performance.length);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Lower is better");
        }

		ImGui.spacing();

        ImGui.text("Simulation Delay:  " + ((int) delay) + "ms" + " / " + ((int) (1000 / delay)) + " FPS");
        ImGui.text("FPS:               " + Utils.addCommasToNumericString(String.valueOf((int) (1 / delta))));
		ImGui.text("Frame:             " + Utils.addCommasToNumericString(String.valueOf(otimer)));
        ImGui.text("Population:        " + Utils.addCommasToNumericString(String.valueOf(creatures.size)));
        ImGui.text("Food:              " + Utils.addCommasToNumericString(String.valueOf(food.size)));
        ImGui.text("Obstacles:         " + Utils.addCommasToNumericString(String.valueOf(obstacles.size)));
        ImGui.text("Waste:             " + Utils.addCommasToNumericString(String.valueOf(waste.size)));

		hoveringAnyWindow |= ImGui.isWindowHovered() | ImGui.isAnyItemHovered();

		ImGui.end();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !hoveringAnyWindow)
            selectedCreature = null;

        if (selectedCreature == null) {
            for (Creature creature : creatures) {
                if (creature != null && mouse.dst2(creature.x, creature.y) <= 1) {
                    ImGui.setNextWindowPos(Gdx.input.getX() + 20,Gdx.input.getY());
                    ImGui.setNextWindowSize(220, 120);
                    ImGui.begin("Popup", NoMove | NoResize | NoCollapse | NoTitleBar | NoScrollbar);
                    ImGui.text("Species:           UNKN");
                    ImGui.text("Color:             #" + creature.color.toString().substring(0,6));
                    ImGui.text("Position:          (" + creature.x + ", " + creature.y + ")");
                    ImGui.text("Energy:            " + creature.energy);
                    ImGui.text("Stored Waste:      " + creature.waste);
                    ImGui.spacing();
                    ImGui.spacing();
                    ImGui.textColored(.5f, .5f, .5f, 1f, "Click for more info");
                    ImGui.end();
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !hoveringAnyWindow)
                        selectedCreature = creature;
                    break;
                }
            }
        } else {
            Creature creature = selectedCreature;
            camera.position.lerp(v3t.set(selectedCreature.x, selectedCreature.y, 0), .5f);
            ImGui.setNextWindowPos(Gdx.graphics.getWidth() - 230, 10);
            ImGui.setNextWindowSize(220, 140);
            ImGui.begin("Popup", NoMove | NoResize | NoCollapse | NoTitleBar | NoScrollbar);
            ImGui.text("Species:           UNKN");
            ImGui.text("Color:             #" + creature.color.toString().substring(0,6));
            ImGui.text("Position:          (" + creature.x + ", " + creature.y + ")");
            ImGui.text("Energy:            " + creature.energy);
            ImGui.text("Age:               " + creature.age);
            ImGui.text("Stored Waste:      " + creature.waste);
            ImGui.spacing();
            ImGui.spacing();
            ImGui.textColored(.5f, .5f, .5f, 1f, "Click for more info");
            ImGui.end();
            if (creature.energy <= 0) selectedCreature = null;
        }

        if (otimer % 50 == 0 && autoFocus.get()) {
            Creature c = null;
            float highestEnergy = 0;
            for (Creature creature : creatures) {
                float oe = highestEnergy;
                highestEnergy = Math.max(highestEnergy, creature.energy);
                if (oe != highestEnergy) c = creature;
            }
            selectedCreature = c;
        }

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
        restartSimulationIfEnded.set(false);
	}

}
