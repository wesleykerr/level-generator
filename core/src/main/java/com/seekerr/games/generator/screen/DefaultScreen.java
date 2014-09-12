package com.seekerr.games.generator.screen;

import static com.seekerr.games.procedural.LatticeFns.FILLED;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.seekerr.games.generator.Assets;
import com.seekerr.games.generator.screen.ScreenFactory.ScreenEnum;
import com.seekerr.games.procedural.ForestGenerationImpl;
import com.seekerr.games.procedural.Point;

/**
 * This screen is for rendering different levels and allowing a graphical
 * representation of our procedural generation algorithms.
 * 
 * @author wkerr
 *
 */
// TODO(wkerr): Extend ScreenAdapter instead of implementing Screen
public abstract class DefaultScreen implements Screen {
    /** Tag used for logging purposes. */
    private static final String TAG = "DefaultScreen";
    public static final Color F_GREEN = new Color(0, 0.4f, 0, 0);

    protected OrthographicCamera camera;

    protected SpriteBatch batch;
    protected BitmapFont font;

    protected int width;
    protected int height;

    protected long seed;
    protected boolean initialized;

    protected ShapeRenderer shapeRenderer;
    protected SpriteBatch spriteBatch;
    
    protected boolean changeScreen = false;
    protected ScreenEnum newScreen = null;
    
    public DefaultScreen() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    /**
     * Construct all of the maps and load in all of the assets.
     */
    protected void initialize() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width, height);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Assets.assetManager.load("level-generator.pack", TextureAtlas.class);
        Assets.assetManager.finishLoading();
        
        changeScreen = false;
        newScreen = null;
    }
    
    /**
     * Render the forest map.
     * @param camera
     * @param gridSize
     * @param map
     */
    protected void renderForest(Camera camera, int gridSize, byte[][] map) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(F_GREEN);

        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {

                float x = j * gridSize;
                float y = i * gridSize;
                if (map[i][j] == ForestGenerationImpl.FOREST) {
                    shapeRenderer.rect(x, y, gridSize, gridSize);
                }
            }
        }
        shapeRenderer.end();
    }

    protected void renderSprites(Camera camera, int gridSize, boolean[][] map) {
        TextureAtlas atlas = Assets.assetManager.get("level-generator.pack",
                TextureAtlas.class);
        TextureRegion floor = atlas.findRegion("floor");
        TextureRegion wall = atlas.findRegion("wall");

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {
                float x = j * gridSize;
                float y = i * gridSize;

                TextureRegion region = map[i][j] == FILLED ? wall : floor;
                spriteBatch.draw(region, x, y, gridSize, gridSize);
            }
        }
        spriteBatch.end();
    }

    /**
     * 
     * @param camera
     * @param gridSize
     * @param contour
     */
    protected void renderContour(Camera camera, int gridSize, List<Point> contour) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        for (Point p : contour) {
            float x = p.getX() * gridSize;
            float y = p.getY() * gridSize;
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
        Gdx.app.log(TAG, "Hide");
    }

    @Override
    public void pause() {
        Gdx.app.log(TAG, "Pause");
    }

    @Override
    public void resume() {
        Gdx.app.log(TAG, "Resume");
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
    
    public boolean getChangeScreen() { 
        return changeScreen;
    }
    
    public ScreenEnum getNewScreen() { 
        return newScreen;
    }
    
    public void setNewScreen(ScreenEnum screen) { 
        changeScreen = true;
        newScreen = screen;
    }
}
