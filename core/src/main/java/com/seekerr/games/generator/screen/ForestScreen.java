package com.seekerr.games.generator.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.seekerr.games.generator.DefaultGameplayInput;
import com.seekerr.games.generator.ScreenshotFactory;
import com.seekerr.games.procedural.ForestGenerationImpl;

/**
 * This screen is for rendering different levels and allowing a graphical
 * representation of our procedural generation forest algorithms.
 * 
 * @author wkerr
 *
 */
public class ForestScreen extends DefaultScreen {
    /** Tag used for logging purposes. */
    private static final String TAG = "OverlayScreen";
    private static final Color F_GREEN = new Color(0, 0.4f, 0, 1);

    private ForestGenerationImpl forestGenerator;
    
    public ForestScreen() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    private void generateForest() { 
        seed = System.currentTimeMillis();
        forestGenerator = ForestGenerationImpl.Builder.create()
                .withSize(240, 160)
                .withRandomSeed(seed)
                .withInitialTrees(20)
                .withSeedParams(7, 0.1, 0.05)
                .build();
        forestGenerator.generate();
    }

    /**
     * Construct all of the maps and load in all of the assets.
     */
    @Override
    protected void initialize() {
        super.initialize();

        // TODO(wkerr): this is a bug and needs to be moved to show or resume.
        Gdx.input.setInputProcessor(new ForestGameplayInput(this));
        generateForest();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        int gridx = width / forestGenerator.getForest()[0].length;
        int gridy = height / forestGenerator.getForest().length;
        int gridSize = Math.min(gridx, gridy);
        renderForest(camera, gridSize, forestGenerator.getForest());

        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
        batch.end();
    }

    class ForestGameplayInput extends DefaultGameplayInput {    
        private ScreenshotFactory ssFactory = null;
        
        public ForestGameplayInput(DefaultScreen screen) {
            super(screen);
        }
        
        @Override
        public boolean keyDown(int keyCode) {
            switch (keyCode) {
            case Keys.G:
                Gdx.app.log(TAG, "Regenerate forest!");
                generateForest();
                return true;
            case Keys.I:
                Gdx.app.log(TAG, "Initialize forest!");
                forestGenerator.setSeed(System.currentTimeMillis());
                forestGenerator.initialize();
                return true;
            case Keys.S:
                Gdx.app.log(TAG, "Step Forest!");
                forestGenerator.step();
                return true;
            case Keys.O:
                forestGenerator.setSeed(System.currentTimeMillis());
                forestGenerator.initialize();
                ssFactory = new ScreenshotFactory("forest");
                ssFactory.saveScreenshot();
                return true;
            case Keys.P:
                forestGenerator.step();
                ssFactory.saveScreenshot();
                return true;
            }
            return false;
        }
    }
}
