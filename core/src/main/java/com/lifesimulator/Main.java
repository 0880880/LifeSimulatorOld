package com.lifesimulator;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

	@Override
	public void create() {
		setScreen(new MainScreen());
	}

	public void close() {
		((MainScreen) getScreen()).close();
	}

}