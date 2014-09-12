package com.seekerr.games.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.seekerr.games.generator.screen.DefaultScreen;
import com.seekerr.games.generator.screen.ScreenFactory;

public class DefaultGameplayInput implements InputProcessor {
    private static final String TAG = "DefaultGameplayInput";
    
    private DefaultScreen screen;
    
    public DefaultGameplayInput(DefaultScreen screen) {
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keyCode) {
        return false;
    }
    
    @Override
    public boolean keyUp(int keyCode) {
        switch (keyCode) {
        case Keys.F1:
            Gdx.app.log(TAG, "Overlay Mode!");
            screen.setNewScreen(ScreenFactory.ScreenEnum.overlay);
            return true;
        case Keys.F2:
            Gdx.app.log(TAG, "Cave Generation Mode!");
            screen.setNewScreen(ScreenFactory.ScreenEnum.cave);
            return true;
        case Keys.F3:
            Gdx.app.log(TAG, "Forest Generation Mode!");
            screen.setNewScreen(ScreenFactory.ScreenEnum.forest);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer,
            int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
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
    public boolean scrolled(int amount) {
        return false;
    }
}
