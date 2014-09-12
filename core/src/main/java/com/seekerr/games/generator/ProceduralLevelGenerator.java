package com.seekerr.games.generator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.seekerr.games.generator.screen.CaveScreen;
import com.seekerr.games.generator.screen.DefaultScreen;
import com.seekerr.games.generator.screen.ForestScreen;
import com.seekerr.games.generator.screen.OverlayScreen;
import com.seekerr.games.generator.screen.ScreenFactory;
import com.seekerr.games.generator.screen.ScreenFactory.ScreenEnum;

public class ProceduralLevelGenerator implements ApplicationListener {
    /** Give a static reference to save time with method calling. */
    public static ProceduralLevelGenerator game;

    /** Tag used for logging purposes. */
    private static final String TAG = "ProceduralLevelGenerator";

    private DefaultScreen screen;
    
    @Override
    public void create() {      
        Gdx.app.log(TAG, "Create");
        game = this;

        Gdx.graphics.setVSync(true);    
        ScreenFactory.get().addScreen(ScreenEnum.cave, new CaveScreen());
        ScreenFactory.get().addScreen(ScreenEnum.forest, new ForestScreen());
        ScreenFactory.get().addScreen(ScreenEnum.overlay, new OverlayScreen());

        setScreen(ScreenEnum.overlay);
    }
    
    public void setScreen(ScreenEnum screenEnum) { 
        DefaultScreen screen = ScreenFactory.get().getScreen(screenEnum);
        setScreen(screen);
    }
    
    public void setScreen(DefaultScreen screen) {
        if (this.screen != null) this.screen.hide();
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    /** @return the currently active {@link Screen}. */
    public Screen getScreen () {
        return screen;
    }
    
    @Override
    public void render() {
        if (screen.getChangeScreen()) setScreen(screen.getNewScreen());
        if (screen != null) screen.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        if (screen != null) screen.resize(width, height);
        Gdx.app.log(TAG, "Resized " + width + ", " + height);
    }

    @Override
    public void pause() {
        if (screen != null) screen.pause();
        Gdx.app.log(TAG, "Paused");
    }

    @Override
    public void resume() {
        if (screen != null) screen.resume();
        Gdx.app.log(TAG, "Resume");
    }
    
    @Override
    public void dispose() {
        if (screen != null) screen.hide();
        Assets.assetManager.dispose();
    }
}
