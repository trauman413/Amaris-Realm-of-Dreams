package edu.cornell.gdiac.alpha;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import edu.cornell.gdiac.alpha.objects.MoonShard;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.alpha.obstacle.*;
import edu.cornell.gdiac.alpha.platform.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public abstract class GameMode implements Screen, InputProcessor {

	/**
	 * Tracks the asset state.  Otherwise subclasses will try to load assets 
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

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

	/** Track asset loading from all instances and subclasses */
	protected AssetState worldAssetState = AssetState.EMPTY;
	/** Whether or not to limit player motion **/
	public AbilityController abilityController = AbilityController.getInstance();
	public GameplayController gameplayController;
	/** Track all loaded assets (for unloading purposes) */
	protected Array<String> assets;
	public PlayerModel player;

	//Information for the serenity bar
	//TODO: make into HUD Controller
	private static String SERENITY_FULL;
	private static String SERENITY_EMPTY;
	private static String SERENITY_HURT;
	private static float SERENITY_X = 620;
	private Texture serenityBarEmpty;
	private Texture serenityBarFull;
	private Texture serenityBarHurt;
	public float MAX_SERENITY;
	//private TextureRegion serenityBar;

	//Information for ability queue
	private static String ABILITY_QUEUE;
	private static String RECTANGLE;
	private Texture abilityQueue;
	private Texture rectangle;
	private int abilityqueue_x = 320;
	private int abilityqueue_y = 520;
	private static String ABILITY_TIMER;
	private Texture abilityTimer;
	private float timeLeftScale = abilityController.getTimeLeftForAbility();
	private float serenityScale = 0;
	private float offset = 0;


	// Pathnames to shared assets
	/** Retro font for displaying messages */
	private static String FONT_FILE = "shared/RetroGame.ttf";
	/** Retro font for displaying messages */
	private static String FOUNTAIN_FONT_FILE = "shared/MarkerFelt.ttf";
	private static String TUTORIAL_FONT_FILE = "fonts/MarkerFelt.ttf"; //TODO: CHANGE THIS FONT
	private static int FONT_SIZE = 100;
	private static int FOUNTAIN_FONT_SIZE = 26;
	private static int FOUNTAIN_TEXT_X = 35;
	private static int FOUNTAIN_TEXT_Y = 50;
	private static int TIMER_TEXT_X = 70;
	private static int TIMER_TEXT_Y = 570;

	private static int SERENITY_TEXT_X = 800;
	private static int SERENITY_TEXT_Y = 570;

	private static int LEVEL_TEXT_X = 920;
	private static int LEVEL_TEXT_Y = 33;

	public boolean hurt = false;

	private int pressState = 0;
	private int buttonOption = 0;


	/** Texture asset for the dash fountain */
	protected TextureRegion dfountainTile;
	/** Texture asset for the flight fountain */
	protected TextureRegion ffountainTile;
	/** Texture asset for the transparent fountain */
	protected TextureRegion tfountainTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;
	/** The font for displaying the fountain information */
	protected BitmapFont fountainFont;
	/** The font for displaying tutorial text */
	protected BitmapFont tutorialFont;
	private static String BKGD_FILE;
	private static String PAUSE_BKGD_FILE;
	private static String WIN_BKGD_FILE;
	private static String LOSE_BKGD_FILE;
	private static String SMOKE_FILE;
	/** The background image for the game */
	private Texture background;
	/** The background image for pause */
	private Texture pauseBackground;
	/** The background image for win */
	private Texture winBackground;
	/** The background image for lose */
	private Texture loseBackground;
	public Texture smoke;
	/** list of moon shards player collided with that needed to be removed */
	public Array<MoonShard> removeMS = new Array<MoonShard>();
	public Array<Rock> removeRocks = new Array<Rock>();
	/** The serenity of player */
	public float serenity;
	/** Current level */
	protected Level level;
	/** Current game state */
	protected GameState gameState;
	/** Total number of moons */
	protected int num_moons;


	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (worldAssetState != AssetState.EMPTY) {
			return;
		}
		levelLoader.populateAssets("jsons/assets.json");
		BKGD_FILE = levelLoader.getImagePath("long_background");
		PAUSE_BKGD_FILE = levelLoader.getImagePath("pause");
		LOSE_BKGD_FILE = levelLoader.getImagePath("lose");
		WIN_BKGD_FILE = levelLoader.getImagePath("win");
		SERENITY_EMPTY = levelLoader.getImagePath("serenity_empty");
		SERENITY_FULL = levelLoader.getImagePath("serenity_full");
		SERENITY_HURT = levelLoader.getImagePath("serenity_hurt");
		ABILITY_QUEUE = levelLoader.getImagePath("ability_queue");
		ABILITY_TIMER = levelLoader.getImagePath("ability_timer");
		RECTANGLE = levelLoader.getImagePath("rectangle");
		SMOKE_FILE = levelLoader.getImagePath("smoke");


		// Load the backgrounds.
		manager.load(BKGD_FILE,Texture.class);
		assets.add(BKGD_FILE);
		manager.load(PAUSE_BKGD_FILE,Texture.class);
		assets.add(PAUSE_BKGD_FILE);
		manager.load(WIN_BKGD_FILE,Texture.class);
		assets.add(WIN_BKGD_FILE);
		manager.load(LOSE_BKGD_FILE,Texture.class);
		assets.add(LOSE_BKGD_FILE);

		//Load the serenity bar
		manager.load(SERENITY_EMPTY,Texture.class);
		assets.add(SERENITY_EMPTY);
		manager.load(SERENITY_FULL,Texture.class);
		assets.add(SERENITY_FULL);
		manager.load(SERENITY_HURT,Texture.class);
		assets.add(SERENITY_HURT);

		//load the ability queue
		manager.load(ABILITY_QUEUE,Texture.class);
		assets.add(ABILITY_QUEUE);
		manager.load(ABILITY_TIMER,Texture.class);
		assets.add(ABILITY_TIMER);

		manager.load(RECTANGLE,Texture.class);
		assets.add(RECTANGLE);

		manager.load(SMOKE_FILE,Texture.class);
		assets.add(SMOKE_FILE);

		worldAssetState = AssetState.LOADING;

		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
		// Load the fountain font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params2 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params2.fontFileName = FOUNTAIN_FONT_FILE;
		size2Params2.fontParameters.size = FOUNTAIN_FONT_SIZE;
		manager.load(FOUNTAIN_FONT_FILE, BitmapFont.class, size2Params2);
		assets.add(FOUNTAIN_FONT_FILE);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (worldAssetState != AssetState.LOADING) {
			return;
		}

		// Allocate the backgrounds
		if (manager.isLoaded(BKGD_FILE)) {
			background = manager.get(BKGD_FILE, Texture.class);
			background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(PAUSE_BKGD_FILE)) {
			pauseBackground = manager.get(PAUSE_BKGD_FILE, Texture.class);
			pauseBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE)) {
			winBackground = manager.get(WIN_BKGD_FILE, Texture.class);
			winBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(LOSE_BKGD_FILE)) {
			loseBackground = manager.get(LOSE_BKGD_FILE, Texture.class);
			loseBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(SERENITY_FULL) && manager.isLoaded(SERENITY_EMPTY) && manager.isLoaded(SERENITY_HURT)) {
			serenityBarFull = manager.get(SERENITY_FULL,Texture.class);
			//serenityBarFull.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			serenityBarEmpty = manager.get(SERENITY_EMPTY,Texture.class);
			//serenityBarEmpty.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
			serenityBarHurt = manager.get(SERENITY_HURT,Texture.class);
		}

		if(manager.isLoaded(SMOKE_FILE)) {
			smoke = manager.get(SMOKE_FILE,Texture.class);
		}

		if(manager.isLoaded(ABILITY_QUEUE)) {
			abilityQueue = manager.get(ABILITY_QUEUE,Texture.class);
		}

		if(manager.isLoaded(RECTANGLE)) {
			rectangle = manager.get(RECTANGLE,Texture.class);
		}

		if(manager.isLoaded(ABILITY_TIMER)) {
			abilityTimer = manager.get(ABILITY_TIMER,Texture.class);
		}

		// Allocate the font
		if (manager.isLoaded(FONT_FILE)) {
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		} else {
			displayFont = null;
		}

		if (manager.isLoaded(FOUNTAIN_FONT_FILE)) {
			fountainFont = manager.get(FOUNTAIN_FONT_FILE);
		} else {
			fountainFont = null;
		}
//
//        if (manager.isLoaded(TUTORIAL_FONT_FILE)) {
//            tutorialFont = manager.get(TUTORIAL_FONT_FILE);
//        } else {
//            fountainFont = null;
//        }

		worldAssetState = AssetState.COMPLETE;
	}

	/**
	 * Returns a newly loaded texture region for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * whether or not the texture should repeat) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param repeat	Whether the texture should be repeated
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
		if (manager.isLoaded(file)) {
			TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			if (repeat) {
				region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			}
			return region;
		}
		return null;
	}

	/**
	 * Returns a newly loaded filmstrip for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * the number of animation frames) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
		if (manager.isLoaded(file)) {
			FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
			strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return strip;
		}
		return null;
	}

	/**
	 * Unloads the assets for this game.
	 *
	 * This method erases the static variables.  It also deletes the associated textures
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
		for(String s : assets) {
			if (manager.isLoaded(s)) {
				manager.unload(s);
			}
		}
	}

	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
	/** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 120;

	/** The amount of time for a alpha engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;

	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -4.9f;

	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Countdown active for winning or losing */
	private int countdown;
	/** Whether or not debug mode is active */
	private boolean debug;
	/** If zooming in */
	private boolean zoomIn;
	/** If ability timer was paused */
	private boolean abilityPaused;
	private boolean isSmoke;


	/**
	 * Returns the current serenity left
	 */
	public float currentSerenity() { return serenity; }
	public void setSerenity(float val) { serenity = val; }
	public void setSmoke(boolean s) { isSmoke = s; }


	private LevelLoader levelLoader;

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
//		if (value) {
//			countdown = EXIT_COUNT;
//		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
//		if (value) {
//			countdown = EXIT_COUNT;
//		}
		failed = value;
	}

	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their alpha bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their alpha bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @return the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected GameMode(float width, float height, float gravity, LevelLoader levelLoader, Level level) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity), levelLoader, level);
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected GameMode(Rectangle bounds, Vector2 gravity, LevelLoader levelLoader, Level level) {
		assets = new Array<String>();
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		active = false;
		debug  = false;
		countdown = -1;
		this.levelLoader = levelLoader;
		this.level = level;
		//this.serenity = 400f;
//		this.serenity = levelLoader.getMaxSerenity();
		this.gameState = GameState.INTRO;
		MAX_SERENITY = 10000;
		Gdx.input.setInputProcessor(this);
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
	}

	/**
	 *
	 * Adds a alpha object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the alpha world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		//System.out.println(obj.getName());
		//System.out.println(inBounds(obj));
//		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the alpha.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		System.out.println(bounds);
		System.out.println(obj.getX());
		System.out.println(obj.getY());
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public abstract void reset();

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

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}

		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT, 0);
			return false;
		} else if (complete && InputController.getInstance().didNext()) {
			listener.exitScreen(this, 0, level.num+1);
			return false;
		}

		else if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed) {
				reset();
			} else if (complete) {
				listener.exitScreen(this, EXIT_NEXT, 0);
				return false;
			}
		}
		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		switch (gameState) {
			case INTRO:
				gameState = GameState.PLAY;
				break;
			case PLAY:
				if (InputController.getInstance().didMap()) {
					gameState = GameState.MAP;
					abilityPaused = abilityController.cancelTimers();
					canvas.savePosition();
				} else if (InputController.getInstance().didPause()) {
					gameState = GameState.PAUSE;
					abilityPaused = abilityController.cancelTimers();
					canvas.savePosition();
				} else if (InputController.getInstance().didReset()) {
					reset();
				} else if (complete) {
					gameState = GameState.WIN;
					abilityPaused = abilityController.cancelTimers();
				}
				break;
			case WIN:
				if (pressState == 2 && buttonOption == 1) { // Menu
					pressState = 0;
					buttonOption = 0;
					canvas.resetZoom();
					listener.exitScreen(this, 0, -1);
				} else if (pressState == 2 && buttonOption == 2) { // Next level
					pressState = 0;
					buttonOption = 0;
					canvas.resetZoom();
					listener.exitScreen(this, 0, level.num+1);
				}
				break;
			case LOSE:
				if (InputController.getInstance().didReset()) { // R key
					gameState = GameState.PLAY;
					reset();
				} else if (InputController.getInstance().didPause()) { // P key
					gameState = GameState.PAUSE;
				}
				break;
			case PAUSE:
				if (InputController.getInstance().didMap()) {
					gameState = GameState.MAP;
					canvas.savePosition();
				} else if (InputController.getInstance().didPause()) { // P key
					gameState = GameState.PLAY;
					if (abilityPaused) {
						abilityController.startTimers();
					}
				} else if (pressState == 2 && buttonOption == 1) { // Resume
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.PLAY;
					canvas.resetPrevZoom();
					if (abilityPaused) {
						abilityController.startTimers();
					}
				} else if (pressState == 2 && buttonOption == 2) { // Menu
					pressState = 0;
					buttonOption = 0;
					canvas.resetZoom();
					listener.exitScreen(this, 0, -1);
				} else if (pressState == 2 && buttonOption == 3) { // Map
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.MAP;
				}
				break;
			case MAP:
				if (InputController.getInstance().didMap()) {
					zoomIn = true;
				} else if (InputController.getInstance().didPause()) { // P key
					gameState = GameState.PAUSE;
				} else if (!zoomIn) {
					canvas.zoomOut();
				}
				if (zoomIn) {
					canvas.zoomIn();
					if (canvas.doneZooming()) {
						canvas.moveBack();
						if (canvas.doneMovingBack()) {
							gameState = GameState.PLAY;
							if (abilityPaused) {
								abilityController.startTimers();
							}
							zoomIn = false;
						}
					}
				} else {
					canvas.moveCam(InputController.getInstance().getHorizontal(), InputController.getInstance().getVertical(), level.width);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Processes alpha
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * alpha.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		if (gameState == GameState.PLAY) {
			while (!addQueue.isEmpty()) {
				addObject(addQueue.poll());
			}

			// Turn the alpha engine crank.
			world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

			for (int i = 0; i <  removeMS.size; i++) {
				removeMS.get(i).deactivatePhysics(world);
			}

			for (int i = 0; i <  removeRocks.size; i++) {
				removeRocks.get(i).deactivatePhysics(world);
				//removeRocks.clear();
			}


			// Garbage collect the deleted objects.
			// Note how we use the linked list nodes to delete O(1) in place.
			// This is O(n) without copying.
			Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
			while (iterator.hasNext()) {
				PooledList<Obstacle>.Entry entry = iterator.next();
				Obstacle obj = entry.getValue();
				if (obj.isRemoved()) {
					obj.deactivatePhysics(world);
					entry.remove();
				} else {
					// Note that update is called last!
					obj.update(dt);
				}
			}
		}
	}

	//=====================================PUT ALL ANIMATIONS IN ONE CLASS INCLUDING THIS=======================
	boolean animationFirst = true;
	float ox =  0;
	float oy = 0;
	float rx = 25;
	float ry = 25;
	TextureRegion[] animationFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
	float elapsedTime;
	Animation<TextureRegion> animation;
	private static final int FRAME_COLS = 3, FRAME_ROWS = 2;
	public void drawSmoke(GameCanvas canvas, float ox, float oy, float x, float y) {
		Color color = Color.WHITE;

		elapsedTime += Gdx.graphics.getDeltaTime();
		int index = 0;
		//System.out.println("draw" + rx);
		TextureRegion[][] tmpFrames = TextureRegion.split(smoke, smoke.getWidth()/FRAME_COLS, smoke.getHeight()/FRAME_ROWS);
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				animationFrames[index++] = tmpFrames[i][j];
			}
		}

		animation = new Animation<TextureRegion>(0.1f, animationFrames);
		TextureRegion currentFrame = animation.getKeyFrame(elapsedTime, true);
		canvas.draw(currentFrame, color, ox, oy, x, y, smoke.getWidth() / FRAME_COLS, smoke.getHeight() / FRAME_ROWS);
	}
	//================================================================================================

	/**
	 * Draw the alpha objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void draw(float delta) {
		if (canvas == null) return;

		canvas.clear();

		if (gameState == GameState.PAUSE) {
			canvas.beginConstantBatch();
			canvas.drawConstantBackground(pauseBackground, 0, 0);
			canvas.endConstantBatch();
		} else if (gameState == GameState.WIN) {
			canvas.beginConstantBatch();
			canvas.drawConstantBackground(winBackground, 0, 0);
			canvas.endConstantBatch();
		} else {
			if (player != null && gameState != GameState.MAP) {
				canvas.begin(player.getX()*scale.x, player.getY()*scale.y, level.width);
			} else {
				canvas.begin();
			}
			//canvas.begin(player.getX()*scale.x, player.getY()*scale.y);
			canvas.drawBackground(background, 0,0);
			for(Obstacle obj : objects) {
				obj.draw(canvas);
			}
			if (isSmoke) {
				for (int i =0; i< 100; i++) {
					drawSmoke(canvas, ox, oy, rx, ry);
				}
				isSmoke = false;
				animationFirst = false;
			}
			//drawSmoke(canvas, 0,0, 100,100);
			//drawSmoke(canvas, ox, oy, rx, ry);
            fountainFont.setColor(Color.BLACK);

			fountainFont.setColor(Color.WHITE);
			for (Level.Message message: level.getMessages()) {
				canvas.drawText(message.text,fountainFont,message.x,message.y,false);
			}

			canvas.end();

			//display timer for ability

			if (debug) {
				canvas.beginDebug();
				for(Obstacle obj : objects) {
					obj.drawDebug(canvas);
				}
				canvas.endDebug();
			}

			canvas.beginConstantBatch();
			if(abilityController.startedAbility) {
				offset = 0;
			}

			if (gameState == GameState.PLAY) {
				canvas.drawConstant(rectangle, Color.WHITE, 21, 490, 420, 125);
				if(abilityController.isUsingAbility()) {
					abilityController.startedAbility = false;
					offset += .055;
					timeLeftScale = 110 * (abilityController.getTimeLeftForAbility()/5);
					canvas.drawConstant(abilityTimer, Color.WHITE, 321, 472+offset, 127, timeLeftScale);
				}
				else {
					offset = 0;
				}
				canvas.drawConstant(abilityQueue,Color.WHITE,10,465,450,125);

				canvas.drawConstant(serenityBarEmpty, Color.WHITE,620,520,400,50);
				if(serenity >= 0) {
					serenityScale = 400 * (serenity/MAX_SERENITY);
					if(hurt) {
						canvas.drawConstant(serenityBarHurt, Color.WHITE, SERENITY_X, 520, serenityScale, 50);
					}
					else {
						canvas.drawConstant(serenityBarFull, Color.WHITE, SERENITY_X, 520, serenityScale, 50);
					}
				}
				abilityController.drawQueue(canvas);

				hurt = false;
				fountainFont.setColor(Color.YELLOW);
				String moonsLeft = "Moon Shards Remaining: " + num_moons;
				//String timer = "Timer: " + (String.valueOf(abilityController.getTimeLeftForAbility()) + "0000").substring(0, 5);
				canvas.drawText(moonsLeft, fountainFont, SERENITY_X + 90, 510,true);
			}

//		if(gameplayController.getCollisions()) {
//			fountainFont.setColor(Color.RED);
//		}
			//else {
			fountainFont.setColor(Color.GREEN);
			//}
			//String serenity = "Serenity: " + (this.serenity);
			//canvas.drawText(serenity, fountainFont, SERENITY_TEXT_X, SERENITY_TEXT_Y);



			fountainFont.setColor(Color.BLACK);
			canvas.drawText(this.level.name, fountainFont, LEVEL_TEXT_X, LEVEL_TEXT_Y,true);

			//canvas.end();


			// Final message
			if (complete && !failed) {
				//displayFont.setColor(Color.YELLOW);

				//canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
				//canvas.drawText("Press n for next level.", fountainFont, canvas.getWidth()/2.0f - 110, canvas.getHeight()/2.0f-50,true);

			} else if (failed) {
				gameState = GameState.LOSE;
				canvas.drawConstantBackground(loseBackground, 0, 0);
				canvas.drawText("Press R to try again", fountainFont, 10, canvas.getHeight() - 25, true);
				//displayFont.setColor(Color.RED);
				//canvas.drawTextCentered("FAILURE", displayFont, 0.0f);
			}

			canvas.endConstantBatch();
		}
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
		// IGNORE FOR NOW
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
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	public abstract boolean detectLocation(float x, float y);



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


	// InputProcessor Methods

	private float[] resumePositions = {363, 650, 287, 350}; //x1, x2, y1, y2
	private float[] menuPositions = {403, 614, 209, 251}; //x1, x2, y1, y2
	private float[] mapPositions = {404, 614, 70, 112}; //x1, x2, y1, y2
	private float[] nextLevelPositions = {671, 917, 50, 120}; //x1, x2, y1, y2
	private float[] menuWinPositions = {107, 352, 50, 120}; //x1, x2, y1, y2

	/**
	 * Called when the screen was touched or a mouse button was pressed.
	 *
	 * This method checks to see if the play button is available and if the click
	 * is in the bounds of the play button.  If so, it signals the that the button
	 * has been pressed and is currently down. Any mouse button is accepted.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners.
	 */
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (pressState == 2) return true;

		if (canvas != null) {

			// Flip to match graphics coordinates
			screenY = canvas.getHeight()-screenY;

			if (gameState == GameState.PAUSE) {
				// Inside resume button
				if (screenX >= resumePositions[0] && screenX <= resumePositions[1]) {
					if (screenY >= resumePositions[2] && screenY <= resumePositions[3]) {
						pressState = 1;
						buttonOption = 1;
					}
				}

				// Inside menu button
				if (screenX >= menuPositions[0] && screenX <= menuPositions[1]) {
					if (screenY >= menuPositions[2] && screenY <= menuPositions[3]) {
						pressState = 1;
						buttonOption = 2;
					}
				}

				// Inside map button
				if (screenX >= mapPositions[0] && screenX <= mapPositions[1]) {
					if (screenY >= mapPositions[2] && screenY <= mapPositions[3]) {
						pressState = 1;
						buttonOption = 3;
					}
				}
			} else if (gameState == GameState.WIN) {
				// Inside menu button
				if (screenX >= menuWinPositions[0] && screenX <= menuWinPositions[1]) {
					if (screenY >= menuWinPositions[2] && screenY <= menuWinPositions[3]) {
						pressState = 1;
						buttonOption = 1;
					}
				}

				// Inside next level button
				if (screenX >= nextLevelPositions[0] && screenX <= nextLevelPositions[1]) {
					if (screenY >= nextLevelPositions[2] && screenY <= nextLevelPositions[3]) {
						pressState = 1;
						buttonOption = 2;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Called when a finger was lifted or a mouse button was released.
	 *
	 * This method checks to see if the play button is currently pressed down. If so,
	 * it signals the that the player is ready to go.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners.
	 */
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pressState == 1) {
			pressState = 2;
			return false;
		}
		return true;
	}


	/**
	 * Called when a key is pressed
	 *
	 * @param keycode the key pressed
	 * @return whether to hand the event to other listeners.
	 */
	public boolean keyDown(int keycode) { return true; }

	/**
	 * Called when a key is typed
	 *
	 * @param character the key typed
	 * @return whether to hand the event to other listeners.
	 */
	public boolean keyTyped(char character) {
		return true;
	}

	/**
	 * Called when a key is released.
	 *
	 * We allow key commands to start the game this time.
	 *
	 * @param keycode the key released
	 * @return whether to hand the event to other listeners.
	 */
	public boolean keyUp(int keycode) { return true; }

	/**
	 * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @return whether to hand the event to other listeners.
	 */
	public boolean mouseMoved(int screenX, int screenY) {
		return true;
	}

	/**
	 * Called when the mouse wheel was scrolled. (UNSUPPORTED)
	 *
	 * @param amount the amount of scroll from the wheel
	 * @return whether to hand the event to other listeners.
	 */
	public boolean scrolled(int amount) {
		return true;
	}

	/**
	 * Called when the mouse or finger was dragged. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners.
	 */
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}

}