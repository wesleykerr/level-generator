package com.seekerr.games.procedural;

import static com.seekerr.games.procedural.LatticeFns.FILLED;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;
import com.seekerr.games.generator.Assets;
import com.seekerr.games.procedural.CaveGenerationImpl.Phase;

/**
 * This screen is for rendering different levels and allowing a graphical
 * representation of our procedural generation algorithms.
 * 
 * @author wkerr
 *
 */
// TODO(wkerr): Extend ScreenAdapter instead of implementing Screen
public class ProceduralGenerationScreen implements Screen {
    /** Tag used for logging purposes. */
    private static final String TAG = "ProceduralGenerationScreen";

    private OrthographicCamera camera;

    private SpriteBatch batch;
    private BitmapFont font;

    private int width;
    private int height;

    private boolean initialized;

    private CaveGenerationImpl caveGenerator;
    private ForestGenerationImpl forestGenerator;
    
    private List<Point> contour;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;

    private boolean shouldDrawForest = false;
    private boolean shouldDrawShapes = false;

    public ProceduralGenerationScreen() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    private void generateCave() {
        long seed = System.currentTimeMillis();
        Gdx.app.log(TAG, "generateCave seed: " + seed);
        
        caveGenerator = CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(seed)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        caveGenerator.generate();
        contour = LatticeFns.getContour(caveGenerator.getMap());

        forestGenerator = ForestGenerationImpl.Builder.create()
                .withSize(240, 160)
                .withRandomSeed(seed)
                .withInitialTrees(20)
                .withSeedParams(7, 0.5, 0.1)
                .build();
        forestGenerator.generate();
    }

    /**
     * Construct all of the maps and load in all of the assets.
     */
    private void initialize() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width, height);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Gdx.input.setInputProcessor(new GameplayInput());

        Assets.assetManager.load("level-generator.pack", TextureAtlas.class);
        Assets.assetManager.finishLoading();

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
        if (shouldDrawShapes)
            renderShapes(camera, gridSize);
        else
            renderSprites(camera, gridSize);
        renderContour(camera, gridSize);
        
        if (shouldDrawForest) {
            gridx = width / forestGenerator.getForest()[0].length;
            gridy = height / forestGenerator.getForest().length;
            gridSize = Math.min(gridx, gridy);
            renderForest(camera, gridSize);
        }

        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
        batch.end();
    }

    private void renderSprites(Camera camera, int gridSize) {
        TextureAtlas atlas = Assets.assetManager.get("level-generator.pack",
                TextureAtlas.class);
        TextureRegion floor = atlas.findRegion("floor");
        TextureRegion wall = atlas.findRegion("wall");

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        boolean[][] map = caveGenerator.getMap();
        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {
                float x = j * gridSize;
                float y = i * gridSize;

                TextureRegion region = map[i][j] == FILLED ? wall
                        : floor;
                spriteBatch.draw(region, x, y, gridSize, gridSize);
            }
        }
        spriteBatch.end();
    }

    private void renderShapes(Camera camera, int gridSize) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        boolean[][] map = caveGenerator.getMap();
        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {

                float x = j * gridSize;
                float y = i * gridSize;
                if (map[i][j] == FILLED)
                    shapeRenderer.setColor(Color.DARK_GRAY);
                else {
                    shapeRenderer.setColor(Color.WHITE);
                }
                shapeRenderer.rect(x, y, gridSize, gridSize);
            }
        }
        shapeRenderer.end();
    }

    private void renderForest(Camera camera, int gridSize) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        
        byte[][] map = forestGenerator.getForest();
        for (int i = 0; i < map.length; ++i) { 
            for (int j = 0; j < map[i].length; ++j) { 
                
                float x = j*gridSize;
                float y = i*gridSize;
                if (map[i][j] == ForestGenerationImpl.FOREST) {
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.rect(x, y, gridSize, gridSize);
                }
            }
        }
        shapeRenderer.end();
    }

    private void renderContour(Camera camera, int gridSize) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        for (Point p : contour) {
            float x = p.x * gridSize;
            float y = p.y * gridSize;
            shapeRenderer.rect(x, y, gridSize, gridSize);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        Gdx.app.log(TAG, "Resized " + width + ", " + height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void show() {
        if (!initialized) {
            initialize();
        }

        // when show is called, we do not have the width / height of the screen.
        Gdx.app.log(TAG, "Show " + width + ", " + height);
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }

    class GameplayInput implements InputProcessor {

        public GameplayInput() {
        }

        @Override
        public boolean keyDown(int keyCode) {
            Gdx.app.log(TAG, "key pressed: " + keyCode);
            switch (keyCode) {
            case Keys.G:
                Gdx.app.log(TAG, "Regenerate cave!");
                generateCave();
                return true;
            case Keys.I:
                Gdx.app.log(TAG, "Initialize cave!");
                caveGenerator.initialize();
                contour = Lists.newArrayList();
                return true;
            case Keys.NUM_1:
                Phase params1 = caveGenerator.getPhase(0);
                caveGenerator.step(params1.min, params1.max);
                return true;
            case Keys.NUM_2:
                Phase params2 = caveGenerator.getPhase(1);
                caveGenerator.step(params2.min, params2.max);
                return true;
            case Keys.F:
                shouldDrawForest = !shouldDrawForest;
                return true;
            }
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer,
                int button) {
            if (button == Buttons.LEFT) {
                // do something
            } else if (button == Buttons.RIGHT) {
                // do something
            }
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
}
