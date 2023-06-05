package com.lifesimulator.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.lifesimulator.Main;

public class Lwjgl3Launcher {

	static Main main;

	public static void main(String[] args) {
		createApplication();
	}

	private static Lwjgl3Application createApplication() {
		main = new Main();
		return new Lwjgl3Application(main, getDefaultConfiguration());
	}

	private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("Life Simulator");
		configuration.useVsync(true);
		configuration.setWindowListener(new Lwjgl3WindowListener() {
			@Override
			public void created(Lwjgl3Window window) {}

			@Override
			public void iconified(boolean isIconified) {}

			@Override
			public void maximized(boolean isMaximized) {}

			@Override
			public void focusLost() {}

			@Override
			public void focusGained() {}

			@Override
			public boolean closeRequested() {
				main.close();
				return true;
			}

			@Override
			public void filesDropped(String[] files) {}

			@Override
			public void refreshRequested() {}
		});
		configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
		configuration.setMaximized(true);
		configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		return configuration;
	}
}
