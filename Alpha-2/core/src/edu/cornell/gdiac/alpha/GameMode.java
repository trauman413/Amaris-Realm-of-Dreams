package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.*;
import edu.cornell.gdiac.alpha.obstacles.Obstacle;

/**
 * The primary controller class for the game.
 */
public class GameMode implements Screen {
    /**
     * Track the current state of the game for the update loop.
     * TODO: add more states, like map
     */
    public enum GameState {
        /** Before the game has started */
        INTRO,
        /** While we are playing the game */
        PLAY,
        /** When the player wins */
        WIN,
        /** When the player loses */
        LOSE,
        /** When the player pauses */
        PAUSE,
        /** When the player enters Map Mode */
        MAP
    }

    // GRAPHICS AND SOUND RESOURCES
    /** The file for the background image to scroll */

    private static String BKGD_FILE;
    /** Retro font for displaying messages */
    private static String TIMER_FONT_FILE = "fonts/MarkerFelt.ttf";

    /** The file for the pause image to scroll */

    private static String PAUSE_FILE;

    // Loaded assets
    /** The background image for the game */
    private Texture background;
    /** The background image for the pause */
    private Texture pauseBackground;
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets;

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController physicsController;
    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;
    private LevelLoader levelLoader;

    /** Variable to track the game state */
    private GameState gameState;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** The camera for the game */
    //TODO: probably should put this in MapController/new class to maintain CMV
    private MapController cam;


    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    private static int TIMER_TEXT_X = 70;
    private static int TIMER_TEXT_Y = 600;
    private static int FONT_SIZE = 26;

    private static int SERENITY_TEXT_X = 800;
    private static int SERENITY_TEXT_Y = 600;

    /** The Box2D world */
    protected World world = new World(new Vector2(0, -80f),false);
    /** The boundary of the world */
    protected Rectangle bounds;

    /**
     * Preloads the assets for this game.
     *
     * The asset manager for LibGDX is asynchronous.  That means that you
     * tell it what to load and then wait while it loads them.  This is
     * the first step: telling it what to load.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        levelLoader.populateAssets("jsons/assets.json");
        BKGD_FILE = levelLoader.getImagePath("long_background");
        // Load the background.
        manager.load(BKGD_FILE,Texture.class);
        assets.add(BKGD_FILE);

        PAUSE_FILE = levelLoader.getImagePath("pause");
        // Load the pause.
        manager.load(PAUSE_FILE,Texture.class);
        assets.add(PAUSE_FILE);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = TIMER_FONT_FILE;
        size2Params.fontParameters.size = FONT_SIZE;
        manager.load(TIMER_FONT_FILE, BitmapFont.class, size2Params);
        assets.add(TIMER_FONT_FILE);

        gameplayController.preLoadContent(manager,assets);
    }

    /**
     * Loads the assets for this game.
     *
     * The asset manager for LibGDX is asynchronous.  That means that you
     * tell it what to load and then wait while it loads them.  This is
     * the second step: extracting assets from the manager after it has
     * finished loading them.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        // Allocate the background
        if (manager.isLoaded(BKGD_FILE)) {
            background = manager.get(BKGD_FILE, Texture.class);
            background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Allocate the pause background
        if (manager.isLoaded(PAUSE_FILE)) {
            pauseBackground = manager.get(PAUSE_FILE, Texture.class);
            pauseBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Allocate the font
        if (manager.isLoaded(TIMER_FONT_FILE)) {
            displayFont = manager.get(TIMER_FONT_FILE,BitmapFont.class);
        } else {
            displayFont = null;
        }

        // Load gameplay content
        gameplayController.loadContent(manager);
    }

    /**
     * Creates a new game with the given drawing context.
     *
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     */
    public GameMode(Rectangle bounds, GameCanvas canvas) {
        this.canvas = canvas;
        this.bounds = new Rectangle(bounds);
        active = false;
        // Null out all pointers, 0 out all ints, etc.
        gameState = GameState.INTRO;
        assets = new Array<String>();

        // Create the controllers.
        levelLoader = new LevelLoader();
        inputController = new InputController();
        gameplayController = new GameplayController(canvas.getWidth(), canvas.getHeight(), world, levelLoader);
        physicsController = new CollisionController(canvas.getWidth(), canvas.getHeight(), world, gameplayController.getPlayer(),
                gameplayController.getFountainsList(),  gameplayController.getObjects(),
                gameplayController.getGoalDoor(), 8);
        world.setContactListener(physicsController);
        cam = new MapController(gameplayController.getPlayer());

    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        //inputController = null;
        //gameplayController = null;
        //physicsController  = null;
        canvas = null;
    }

    /**
     * Update the game state.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        // Process the game input
        inputController.readInput();
        // Test whether to reset the game.
        switch (gameState) {
            case INTRO:
                gameState = GameState.PLAY;
                gameplayController.start();
                physicsController.setPlayer(gameplayController.getPlayer());
                cam.setPlayer(gameplayController.getPlayer());
//                physicsController.setDFountain(gameplayController.getDfountain());
//                physicsController.setEFountain(gameplayController.getEfountain());
//                physicsController.setFFountain(gameplayController.getFfountain());
                physicsController.setFountainsList(gameplayController.getFountainsList());
                physicsController.setGoalDoor(gameplayController.getGoalDoor());
                physicsController.setMoonShardList(gameplayController.getMSList());
                physicsController.setRockList(gameplayController.getRockList());
                physicsController.setPlatformList(gameplayController.getPlatformsList());

                break;
            case PLAY:
                play(delta);
                cam.render();
                if(physicsController.getAbilityToAdd() != null) {
                    gameplayController.addToQueue(physicsController.getTouchedFountain());
                    physicsController.setAbilityToAdd(null);
                }

                if(physicsController.playerRockCollision || physicsController.spikeCollision ||
                        physicsController.getTouchedMonster()) {
                    gameplayController.getPlayer().setHit(true);

                }
                else {
                    gameplayController.getPlayer().setHit(false);
                }

                if(physicsController.getSpikeCollision()) {
                    //TODO: figure out scaling
                    gameplayController.serenity -= 100;
                    physicsController.setSpikeCollision(false);
                }

                if(physicsController.getRockCollision()) {
                    if(physicsController.getPlayerRockCollision()) {
                        gameplayController.serenity -= 100;
                        physicsController.setPlayerRockCollision(false);
                    }
                    gameplayController.resetRock(physicsController.collidedRock);
                    physicsController.collidedRock = null;
                    physicsController.setRockCollision(false);
                }

                if(physicsController.getPlayerRockCollision()) {
                    //TODO: figure out scaling
                    gameplayController.serenity -= 100;
                    physicsController.setRockCollision(false);
                    physicsController.setPlayerRockCollision(false);
                }

                if (physicsController.getComplete()) {
                    gameplayController.setComplete(physicsController.getComplete());
                    gameState = GameState.WIN;
                }
                if(gameplayController.serenity <= 0) {
                    gameplayController.serenity = 0;
                    gameState = GameState.LOSE;
                }
                else {
                    gameplayController.serenity -= 1;
                }

                if (gameplayController.isFailure()) {
                    gameState = GameState.LOSE;
                }
                if(physicsController.getRestore()) {
                    gameplayController.serenity += 100;
                    physicsController.setRestore(false);
                }

                if(physicsController.getTouchedMonster()) {
                    gameplayController.serenity -= 100;
                    physicsController.setTouchedMonster(false);
                }


                break;
            case WIN:
                play(delta);
                break;
            case LOSE:
                //play(delta);
                break;
            case PAUSE:
                break;
            case MAP:
                cam.render();
                break;
            default:
                break;
        }
    }

    /**
     * This method processes a single step in the game loop.
     *
     * @param delta Number of seconds since last animation frame
     */
    protected void play(float delta) {
        gameplayController.update(inputController,delta);
    }

    /**
     * Draw the status of this player mode.
     */
    private void draw(float delta) {
        canvas.clear();
        canvas.begin(gameplayController.getPlayer().getX(), gameplayController.getPlayer().getY());
        canvas.drawBackground(background, 0,0);

        // Draw the game objects
        for (Obstacle o : gameplayController.getObjects()) {
            o.draw(canvas);
        }
        canvas.end();

        canvas.beginConstantBatch();
        gameplayController.draw(canvas); // draws ability queue
        displayFont.setColor(Color.ORANGE);
        String timer = "Timer: " + (String.valueOf(gameplayController.getTimer()) + "0000").substring(0, 5);
        canvas.drawText(timer, displayFont, TIMER_TEXT_X, TIMER_TEXT_Y);

        String serenity = "Serenity: " + (gameplayController.serenity);
        canvas.drawText(serenity, displayFont, SERENITY_TEXT_X, SERENITY_TEXT_Y);
        // Flush information to the graphic buffer.



        // Final message
        if (gameState == GameState.WIN) {
            displayFont.setColor(Color.GOLD);
            //canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
        }
        else if (gameState == GameState.LOSE) {
            displayFont.setColor(Color.RED);
            canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
        }
        else if(gameState == GameState.PAUSE) {
            displayFont.setColor(Color.BLUE);
            canvas.drawTextCentered("PAUSE", displayFont, 0.0f);
            //canvas.draw(pauseBackground, 0,0);
        }

        canvas.endConstantBatch();

    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // nothing
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
                postUpdate(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // nothing
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // nothing
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        InputController input = InputController.getInstance();
        input.readInput();
        if (listener == null) {
            return true;
        }

        // Handle resets
        if (inputController.didReset()) {
            gameState = GameState.INTRO;
            gameplayController.reset(world);
            physicsController.setComplete(false);

        }
        if(inputController.didPause() && gameState == GameState.PAUSE) {
            gameState = GameState.PLAY;
        }
        else if(inputController.didPause() && gameState != GameState.PAUSE) {
            gameState = GameState.PAUSE;
        }

        if(inputController.didMap() && gameState != GameState.MAP) {
            gameState = GameState.MAP;
        }

       else if(inputController.didMap() && gameState == GameState.MAP) {
            gameState = GameState.PLAY;
        }



        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            listener.exitScreen(this, 0);
            return false;
        }

        return true;
    }

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Turn the physics engine crank.
        if(gameState != GameState.PAUSE) {
            world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
            for (int i = 0; i <  physicsController.removeMS.size; i++) {
                physicsController.removeMS.get(i).deactivatePhysics(world);
            }

        }
    }

}