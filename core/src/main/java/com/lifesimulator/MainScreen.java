package com.lifesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.lwjgl.glfw.GLFW;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static com.lifesimulator.Statics.*;

public class MainScreen implements Screen {

	public static Array<Creature> creatures = new Array<>();

	Array<Circle> obstacles = new Array<>();

	public static HashMap<Index, Creature> creaturesIndex = new HashMap<>();

	int timer = 0;

	Array<Vector2> food = new Array<>();

	public static Array<Creature>[][] chunks = new Array[17][17];

	Vector2 mouse = new Vector2();
	Vector2 oldMouse = new Vector2();

	public Vector2[] neuronPositions;
	float[] population = new float[1];

	boolean threaded = false;

	Vector2 tempVector = new Vector2();

	public final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
	public final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

	public ImInt nFramesToSimulate = new ImInt(10);
	ImBoolean enableRendering = new ImBoolean(true);

	int otimer = 0;

	boolean stopSimulation;

	boolean hoveringAnyWindow = false;

	InputProcessor inputProcessor;

	public void reset() {

		timer = 0;

        food.clear();
		obstacles.clear();
		creatures.clear();
		creaturesIndex.clear();

		for (int i = 0; i < chunks.length; i++) {
			for (int j = 0; j < chunks.length; j++) {
				if (chunks[i][j] == null)
					chunks[i][j] = new Array<>();
				chunks[i][j].clear();
			}
		}

		population = new float[] {maxCreatures.get()};

		for (int i = 0; i < 20; i++) {
			addFood(new Vector2(MathUtils.random(0,256), MathUtils.random(0,256)));
		}

		for (int i = 0; i < maxCreatures.get(); i++) {
			addCreature(creatures, new Creature(MathUtils.random(128 - 40,128 + 40), MathUtils.random(128 - 40,128 + 40)));
			creaturesIndex.put(new Index(creatures.peek().x, creatures.peek().y), creatures.peek());
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

		batch = new SpriteBatch();
		drawer = new ShapeDrawer(batch, new TextureRegion(new Texture(new APixmap(1, 1, Pixmap.Format.RGBA8888, Color.WHITE))));

		viewport = new ExtendViewport(1920 / 5, 1080 / 5);
		camera = (OrthographicCamera) viewport.getCamera();

		Gdx.input.setInputProcessor(inputProcessor = new InputProcessor());

		reset();

		camera.position.add(128,128,0);
	}

	void addCreature(Array<Creature> array, Creature creature) {
		boolean isInsideObstacle = false;
		for (int i = 0; i < obstacles.size; i++) {
			Circle obstacle = obstacles.get(i);
			isInsideObstacle |= obstacle.contains(creature.x, creature.y);
		}
		while (isInsideObstacle) {
			isInsideObstacle = false;
			creature.x = MathUtils.random(80,120);
			creature.y = MathUtils.random(80,120);
			for (int i = 0; i < obstacles.size; i++) {
				Circle obstacle = obstacles.get(i);
				isInsideObstacle |= obstacle.contains(creature.x, creature.y);
			}
		}
		if (chunks[creature.x / 16][creature.y / 16] == null)
			chunks[creature.x / 16][creature.y / 16] = new Array<>();
		chunks[creature.x / 16][creature.y / 16].add(creature);
		array.add(creature);
	}

	void addFood(Vector2 food) {
		boolean isInsideObstacle = false;
        for (int i = 0; i < obstacles.size; i++) {
            Circle obstacle = obstacles.get(i);
			isInsideObstacle |= obstacle.contains(food);
		}
		while (isInsideObstacle) {
			isInsideObstacle = false;
			food.set(new Vector2(MathUtils.random(0,256), MathUtils.random(0, 256)));
            for (int i = 0; i < obstacles.size; i++) {
                Circle obstacle = obstacles.get(i);
				isInsideObstacle |= obstacle.contains(food);
			}
		}
		this.food.add(food);
	}

	void updateCreatures() {
		for (int i = 0; i < creatures.size; i++) {
			Creature creature = creatures.get(i);
			if (creature.energy > creatureBaseEnergy.get() + 20 && MathUtils.randomBoolean(.01f + ((creature.energy - 7f) / 24f)) && creatures.size < maxCreatures.get()) {
				addCreature(creatures, new Creature(creature, MathUtils.random(0,256), MathUtils.random(0, 256)));
				creature.energy -= creatureBaseEnergy.get() + 4;
			}
			//if (creature.moveMeter < (timer / 10)) creature.dead = true;
			Array<Creature> currentChunk = chunks[creature.x / 16][creature.y / 16];
			creature.update(creaturesIndex, creatures, chunks, obstacles, food, drawer, false);
			currentChunk.removeValue(creature, false);
			if (chunks[creature.x / 16][creature.y / 16] == null)
				chunks[creature.x / 16][creature.y / 16] = new Array<>();
			chunks[creature.x / 16][creature.y / 16].add(creature);
			if (creature.energy <= 0) {
				creatures.removeIndex(i);
				currentChunk.removeValue(creature, true);
				creaturesIndex.remove(new Index(creature.x, creature.y));
			}
		}
	}

	void simulate() {
		updateCreatures();
		timer++;
		otimer++;
		if (otimer % obstacleSpawnInterval.get() == 0) {
			obstacles.add(new Circle(MathUtils.random(0,256), MathUtils.random(0,256), MathUtils.random(1,10)));
		}
		if (timer >= foodSpawnInterval.get()) {
			timer -= foodSpawnInterval.get();
			for (int k = 0; k < foodSpawnAmount.get(); k++) {
				addFood(new Vector2(MathUtils.random(0,256), MathUtils.random(0,256)));
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

            for (int i = 0; i < creatures.size; i++) {
                Creature creature = creatures.get(i);
				drawer.filledRectangle(creature.x, creature.y, 1, 1, creature.color);

			}

		}

		batch.end();

		hoveringAnyWindow = false;

		imGuiGlfw.newFrame();
		ImGui.newFrame();

		ImGui.begin("Debug");

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
		ImGui.inputFloat("Creature Base Energy", creatureBaseEnergy);

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

		ImGui.setNextItemWidth(120);
		ImGui.inputInt("Number of frames to simulate (x1000)", nFramesToSimulate);
		ImGui.spacing();

		/*if (ImGui.button("Simulate Multithreaded (WIP)")) {
			Array<Creature> creaturesCloned = new Array<>();
			creaturesCloned.addAll(creatures);
			HashMap<Index, Creature> creaturesIndexCloned = new HashMap<>();
			creaturesIndexCloned.putAll(creaturesIndex);
			Array<Creature>[][] chunksCloned = new Array[17][17];
			for (int i = 0; i < chunks.length; i++) {
				for (int j = 0; j < chunks.length; j++) {
					if (chunks[i][j] == null) {
						chunks[i][j] = new Array<>();
					}
				}
			}

			final int threads = 8;
			final int gens = 1;
			Latch latch = new Latch(threads);

			Thread[] ts = new Thread[threads];

			for (int i = 0; i < threads; i++) {
				int part = i;
				threaded = true;
				Thread t = new Thread(() -> {
					for (int iter = 0; iter < 20; iter++) {
						for (int j = 0; j < generationLength * gens; j++) {
							threaded = true;
							int percent = (int) ((j / ((float) generationLength * gens)) * 100);
							//if (part == 0)System.out.println("THREAD #" + (part + 1) + " %" + percent);

							for (int k = (part * (creatures.size / threads)) + 1; k < ((part + 1) * (creatures.size / threads)); k++) {
								Creature creature = creatures.get(k);
								creature.update(creaturesIndex, creatures, chunks, obstacles, food, drawer, false);
							}

							latch.countUp();

							latch.await();

							if (part == 0) {
								timer++;
								//System.out.println("TIMER: " + timer);
								for (int k = 0; k < creatures.size; k++) {
									Creature creature = creatures.get(k);
									Array<Creature> currentChunk = chunks[creature.x / 16][creature.y / 16];
									currentChunk.removeValue(creature, true);
									if (chunks[creature.x / 16][creature.y / 16] == null)
										chunks[creature.x / 16][creature.y / 16] = new Array<>();
									chunks[creature.x / 16][creature.y / 16].add(creature);
								}
								latch.reset(threads);
								if (timer >= generationLength) {
									newGen();
								}
							}

						}
						threaded = false;
					}
				});
				ts[i] = t;
				t.start();
			}
		}*/

		if (ImGui.button("Simulate")) {
			stopSimulation = false;
			new Thread(() -> {
				for (int j = 0; j < 1000 * nFramesToSimulate.get(); j++) {
					threaded = true;
					if (stopSimulation) {
						break;
					}
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

		ImGui.text("FPS:            " + ((int) (1 / delta)));
		ImGui.text("Frame:          " + otimer);
		ImGui.text("Population:     " + creatures.size);

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
