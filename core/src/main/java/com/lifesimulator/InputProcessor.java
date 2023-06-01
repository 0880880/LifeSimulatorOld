package com.lifesimulator;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntIntMap;

public class InputProcessor implements com.badlogic.gdx.InputProcessor {

    public IntIntMap keys = new IntIntMap();

    public boolean leftUp;

    public float scrollY;

    @Override
    public boolean keyDown(int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
    	if (character == '+' || character == '=')
    		scrollY = -1;
    	else if (character == '-' || character == '_')
    		scrollY = 1;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT)
        	leftUp = true;
        return false;
    }

    public void update() {
        leftUp = false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollY = amountY;
        return false;
    }
}
