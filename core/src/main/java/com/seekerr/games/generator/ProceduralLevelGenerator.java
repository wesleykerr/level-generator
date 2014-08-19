package com.seekerr.games.generator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.seekerr.games.procedural.ProceduralGenerationScreen;

public class ProceduralLevelGenerator extends Game {
    /** Give a static reference to save time with method calling. */
    public static ProceduralLevelGenerator game;

    /** Tag used for logging purposes. */
    private static final String TAG = "ProceduralLevelGenerator";

    private ProceduralGenerationScreen screen;

    @Override
    public void create() {      
        Gdx.app.log(TAG, "Create");
        game = this;

        Gdx.graphics.setVSync(true);    

        screen = new ProceduralGenerationScreen();
        setScreen(screen);
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        Gdx.app.log(TAG, "Resized " + width + ", " + height);
    }

    @Override
    public void pause() {
        super.pause();
        Gdx.app.log(TAG, "Paused");
    }

    @Override
    public void resume() {
        super.resume();
        Gdx.app.log(TAG, "Resume");
    }
    
    @Override
    public void dispose() {
        super.dispose();
        Assets.assetManager.dispose();
    }
}
