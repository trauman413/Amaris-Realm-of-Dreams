package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;

/**
 * Root class for a LibGDX.
 */
public class GDXRoot extends Game implements ScreenListener {
    /** AssetManager to load game assets (textures, sounds, etc.) */
    private AssetManager manager;
    /** Drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;
    /** Player mode for the asset loading screen (CONTROLLER CLASS) */
    private LoadingMode loading;
    private LevelSelect level_select;
    /** Player mode for the the game proper (CONTROLLER CLASS) */
    private GameMode    playing;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;

    /**
     * Creates a new game from the configuration settings.
     */
    public GDXRoot() {
        manager = new AssetManager();

        // Add font support to the asset manager
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
    }

    @Override
    public void create () {
        canvas  = new GameCanvas();
        level_select = new LevelSelect(canvas, manager, 1);
        level_select.setScreenListener(this);
        setScreen(level_select);
    }

    /**
     * Called when the Application is destroyed.
     */
    @Override
    public void dispose () {
        // Call dispose on our children
        Screen screen = getScreen();
        setScreen(null);
        screen.dispose();

        canvas.dispose();
        canvas = null;

        // Unload all of the resources
        //playing.unloadContent(manager);
        manager.clear();
        manager.dispose();

        SoundController.disposeAllSounds();

        super.dispose();
    }

    /**
     * Called when the Application is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to create().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        canvas.resize();
        super.resize(width,height);
    }

    /**
     * The given screen has made a request to exit its player mode.
     *
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {
        if (exitCode != 0) {
            Gdx.app.error("GDXRoot", "Exit with error code "+exitCode, new RuntimeException());
            Gdx.app.exit();
        } else if (screen == level_select) {
            //loading.loadContent(manager);
            loading = new LoadingMode(canvas, manager, 1);
            loading.setScreenListener(this);
            setScreen(loading);
            playing = new GameMode(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), canvas);
            playing.preLoadContent(manager); // Load game assets statically.

            level_select.dispose();
            level_select = null;
        } else if (screen == loading) {
            playing.loadContent(manager);
            playing.setScreenListener(this);
            setScreen(playing);

            loading.dispose();
            loading = null;
        } else {
            // We quit the main application
            Gdx.app.exit();
        }
    }
}