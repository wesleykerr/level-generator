package com.seekerr.games.generator.screen;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.seekerr.games.generator.DefaultGameplayInput;
import com.seekerr.games.generator.ScreenshotFactory;
import com.seekerr.games.procedural.CaveGenerationImpl;
import com.seekerr.games.procedural.ForestGenerationImpl;
import com.seekerr.games.procedural.LatticeFns;
import com.seekerr.games.procedural.Point;

/**
 * This screen is for rendering different levels and allowing a graphical
 * representation of our procedural generation forest algorithms.
 * 
 * @author wkerr
 *
 */
public class OverlayScreen extends DefaultScreen {
    /** Tag used for logging purposes. */
    private static final String TAG = "OverlayScreen";
    private static final Color F_GREEN = new Color(0, 0.4f, 0, 1);

    private CaveGenerationImpl caveGenerator;
    private List<Point> contour;

    private ForestGenerationImpl forestGenerator;
    
    public OverlayScreen() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }
    
    private void generateLevel() { 
        seed = System.currentTimeMillis();
        Gdx.app.log(TAG, "generateLevel seed: " + seed);
        
        generateCave();
        generateForest();
    }
    
    private void generateCave() {
        caveGenerator = CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(seed)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        caveGenerator.generate();
        contour = LatticeFns.getContour(caveGenerator.getMap());
    }

    
    private void generateForest() { 
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
        Gdx.input.setInputProcessor(new OverlayGameplayInput(this));
        generateLevel();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int gridx = width / caveGenerator.getMap()[0].length;
        int gridy = height / caveGenerator.getMap().length;
        int gridSize = Math.min(gridx, gridy);
        renderSprites(camera, gridSize, caveGenerator.getMap());
//        renderContour(camera, gridSize, contour);
        
        camera.update();
        gridx = width / forestGenerator.getForest()[0].length;
        gridy = height / forestGenerator.getForest().length;
        gridSize = Math.min(gridx, gridy);
        renderForest(camera, gridSize, forestGenerator.getForest());

        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
        batch.end();
    }

    class OverlayGameplayInput extends DefaultGameplayInput {    
        public OverlayGameplayInput(DefaultScreen screen) {
            super(screen);
        }
        
        @Override
        public boolean keyDown(int keyCode) {
            switch (keyCode) {
            case Keys.G:
                Gdx.app.log(TAG, "Regenerate forest!");
                generateLevel();
                return true;
            }
            return false;
        }
    }
}
