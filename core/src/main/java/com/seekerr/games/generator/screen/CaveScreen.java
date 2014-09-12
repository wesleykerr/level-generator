package com.seekerr.games.generator.screen;

import static com.seekerr.games.procedural.LatticeFns.FILLED;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;
import com.seekerr.games.generator.Assets;
import com.seekerr.games.generator.DefaultGameplayInput;
import com.seekerr.games.procedural.CaveGenerationImpl;
import com.seekerr.games.procedural.CaveGenerationImpl.Phase;
import com.seekerr.games.procedural.LatticeFns;
import com.seekerr.games.procedural.Point;

/**
 * This screen is for rendering different levels and allowing a graphical
 * representation of our procedural generation algorithms.
 * 
 * @author wkerr
 *
 */
// TODO(wkerr): Extend ScreenAdapter instead of implementing Screen
public class CaveScreen extends DefaultScreen {
    /** Tag used for logging purposes. */
    private static final String TAG = "CaveScreen";

    private CaveGenerationImpl caveGenerator;
    private List<Point> contour;

    public CaveScreen() {
        super();
    }

    private void generateCave() {
        seed = System.currentTimeMillis();
        Gdx.app.log(TAG, "generateCave seed: " + seed);

        caveGenerator = CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(seed)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        caveGenerator.generate();
        contour = LatticeFns.getContour(caveGenerator.getMap());
    }
    
    /**
     * Construct all of the maps and load in all of the assets.
     */
    protected void initialize() {
        super.initialize();

        Gdx.input.setInputProcessor(new CaveGameplayInput(this));
        generateCave();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        int gridx = width / caveGenerator.getMap()[0].length;
        int gridy = height / caveGenerator.getMap().length;
        int gridSize = Math.min(gridx, gridy);
        renderSprites(camera, gridSize, caveGenerator.getMap());
//        renderContour(camera, gridSize, contour);

        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
        batch.end();
    }
    
    class CaveGameplayInput extends DefaultGameplayInput {
        
        public CaveGameplayInput(DefaultScreen screen) { 
            super(screen);
        }
        
        @Override
        public boolean keyDown(int keyCode) {
            switch (keyCode) {
            case Keys.G:
                Gdx.app.log(TAG, "Regenerate cave!");
                generateCave();
                return true;
            case Keys.I:
                Gdx.app.log(TAG, "Initialize cave!");
                caveGenerator.setSeed(System.currentTimeMillis());
                caveGenerator.initialize();
                contour = Lists.newArrayList();
                return true;
            case Keys.NUM_1:
                Phase params1 = caveGenerator.getPhase(0);
                caveGenerator.step(params1.getMin(), params1.getMax());
                return true;
            case Keys.NUM_2:
                Phase params2 = caveGenerator.getPhase(1);
                caveGenerator.step(params2.getMin(), params2.getMax());
                return true;
            }
            return false;
        }
    }
}
