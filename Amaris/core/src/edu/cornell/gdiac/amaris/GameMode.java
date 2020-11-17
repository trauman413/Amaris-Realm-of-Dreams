package edu.cornell.gdiac.amaris;

import java.util.*;
import java.util.Timer;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import edu.cornell.gdiac.amaris.objects.MoonShard;
import edu.cornell.gdiac.amaris.objects.RegularPlatform;
import edu.cornell.gdiac.amaris.objects.SignPost;
import edu.cornell.gdiac.amaris.util.*;
import edu.cornell.gdiac.amaris.obstacle.*;
import edu.cornell.gdiac.amaris.platform.*;

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
		MAP,
		/** When the player enters controls Mode */
		CONTROLS
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
	private static String SERENITY_LINE;
	private static String SERENITY_LABEL;
	private static float SERENITY_X = 620;
	private Texture serenityBarEmpty;
	private Texture serenityBarFull;
	private Texture serenityBarHurt;
	private Texture serenityLine;
	private Texture serenityLabel;
	public float MAX_SERENITY;
	public float THREE_STAR;
	public float TWO_STAR;
	public float ONE_STAR;
	private float THREESTAR_X;
	private float TWOSTAR_X;
	private float ONESTAR_X;
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
	protected float serenityOffset = 0f;


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
	private static int LEVEL_TEXT_Y = 560;


	public boolean hurt = false;

	private int pressState = 0;
	private int mouseState = 0;
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
	/** Background files */
	private static String BKGD_FILE;
	private static String SPACE_BKGD_FILE;
	private static String CLOUD_BKGD_FILE;
	private static String PAUSE_BKGD_FILE;
	private static String WIN_BKGD_FILE;
	private static String WIN_BKGD_FILE0;
	private static String WIN_BKGD_FILE1;
	private static String WIN_BKGD_FILE2;
	private static String WIN_BKGD_FILE3;
	private static String LOSE_BKGD_FILE;
	private static String SMOKE_FILE;
	private static String VIGNETTE_FILE;
	private static String PAUSE_BUTTON;
	private static String PAUSE_BUTTON_CLICKED;
	private static String MAP_BUTTON;
	private static String MAP_BUTTON_CLICKED;
	private static String BACK_BUTTON;
	private static String BACK_BUTTON_CLICKED;
	private static String MOON_ICON;
    private static String LEVELSELECT_UNCLICKED;
    private static String LEVELSELECT_CLICKED;
    private static String NEXTLEVEL_CLICKED;
    private static String NEXTLEVEL_UNCLICKED;
    private static String MUTE_BUTTON;
    private static String MUTE_BUTTON_CLICKED;
    private static String MUTE_OFF;
	private static String RESUME_BUTTON;
	private static String RESUME_BUTTON_CLICKED;
	private static String RESUME_BUTTON_HOVER;
	private static String CONTROLS_BUTTON;
	private static String CONTROLS_BUTTON_CLICKED;
	private static String CONTROLS_BUTTON_HOVER;
	private static String TITLE_SCREEN_BUTTON;
	private static String TITLE_SCREEN_BUTTON_CLICKED;
	private static String TITLE_SCREEN_BUTTON_HOVER;
	private static String LEVEL_SELECT_BUTTON;
	private static String LEVEL_SELECT_BUTTON_CLICKED;
	private static String LEVEL_SELECT_BUTTON_HOVER;
	private static String CONTROLS_FILE;
	private static String NUMBER_FILE;
	private static String CIRCLE_FILE;

    private Texture levelSelectUnclicked;
    private Texture levelSelectClicked;
    private Texture nextLevelClicked;
    private Texture nextLevelUnclicked;

    private Texture number;
    private Texture circle;

    /** The background image for the game at ground */
	private Texture background;
	/** The background image for the game at space */
	private Texture spaceBackground;
	/** The background image for the game at clouds */
	private Texture cloudBackground;
	/** The background image for pause */
	private Texture pauseBackground;
	/** The background image for win */
	private Texture winBackground;
	/** The background image for win with 0 stars */
	private Texture winBackground0;
	/** The background image for win with 1 star*/
	private Texture winBackground1;
	/** The background image for win with 2 stars*/
	private Texture winBackground2;
	/** The background image for win with 3 stars*/
	private Texture winBackground3;
	/** The background image for lose */
	private Texture loseBackground;
	public Texture smoke;
	public Texture pause;
	public Texture pauseClick;
	public Texture map;
	public Texture mapClick;
	public Texture back;
	public Texture backClick;
	public Texture moonIcon;
	public Texture muteButton;
	public Texture muteClick;
	public Texture muteOff;
	public Texture resumeButton;
	public Texture resumeClick;
	public Texture resumeHover;
	public Texture titleScreenButton;
	public Texture titleScreenHover;
	public Texture titleScreenClick;
	public Texture controlsButton;
	public Texture controlsHover;
	public Texture controlsClick;
	public Texture levelSelectHover;
	public Texture levelSelectButton;
	public Texture levelSelectClick;
	public Texture controls;
	/** The vignette imagee */
	private Texture vignette;
	/** list of moon shards player collided with that needed to be removed */
	public Array<MoonShard> removeMS = new Array<MoonShard>();
	public Array<Rock> removeRocks = new Array<Rock>();
	public Array<RegularPlatform> removeWindows = new Array<RegularPlatform>();
	public Array<Array<Float>> smokesCoord = new Array<Array<Float>>();
	public Array<Float> playerSmokeCoord = new Array<Float>();
	/** The serenity of player */
	public float serenity;
	/** Current level */
	protected Level level;
	/** Current game state */
	protected GameState gameState;
	/** Total number of moons */
	protected int num_moons;

	private int PAUSE_X = 960;
	private int PAUSE_Y = 510;
	private int RESUME_X = 340;
	private int RESUME_Y = 275;
	private int LEVEL_SELECT_X = 340;
	private int LEVEL_SELECT_Y = 181;
	private int TITLE_SCREEN_X = 340;
	private int TITLE_SCREEN_Y = 114;
	private int CONTROLS_X = 340;
	private int CONTROLS_Y = 47;

	public boolean mute;


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
		BKGD_FILE = levelLoader.getImagePath("ground_background");
		SPACE_BKGD_FILE = levelLoader.getImagePath("space_background");
		CLOUD_BKGD_FILE = levelLoader.getImagePath("cloud_background");
		PAUSE_BKGD_FILE = levelLoader.getImagePath("pause");
		LOSE_BKGD_FILE = levelLoader.getImagePath("lose");
		WIN_BKGD_FILE = levelLoader.getImagePath("win");
		WIN_BKGD_FILE0 = levelLoader.getImagePath("win0");
		WIN_BKGD_FILE1 = levelLoader.getImagePath("win1");
		WIN_BKGD_FILE2 = levelLoader.getImagePath("win2");
		WIN_BKGD_FILE3 = levelLoader.getImagePath("win3");
		SERENITY_EMPTY = levelLoader.getImagePath("serenity_empty");
		SERENITY_FULL = levelLoader.getImagePath("serenity_full");
		SERENITY_HURT = levelLoader.getImagePath("serenity_hurt");
		SERENITY_LINE = levelLoader.getImagePath("serenity_line");
		SERENITY_LABEL = levelLoader.getImagePath("serenity_label");
		ABILITY_QUEUE = levelLoader.getImagePath("ability_queue");
		ABILITY_TIMER = levelLoader.getImagePath("ability_timer");
		RECTANGLE = levelLoader.getImagePath("rectangle");
		SMOKE_FILE = levelLoader.getImagePath("smoke");
		VIGNETTE_FILE = levelLoader.getImagePath("vignette");
		PAUSE_BUTTON = levelLoader.getImagePath("pause_button");
		PAUSE_BUTTON_CLICKED = levelLoader.getImagePath("pause_button_clicked");
		MAP_BUTTON = levelLoader.getImagePath("map_button");
		MAP_BUTTON_CLICKED = levelLoader.getImagePath("map_button_clicked");
		BACK_BUTTON = levelLoader.getImagePath("back_button");
		BACK_BUTTON_CLICKED = levelLoader.getImagePath("back_button_clicked");
		MOON_ICON = levelLoader.getImagePath("moon_icon");
		LEVELSELECT_CLICKED = levelLoader.getImagePath("levelselect_clicked");
		LEVELSELECT_UNCLICKED = levelLoader.getImagePath("levelselect_unclicked");
		NEXTLEVEL_CLICKED = levelLoader.getImagePath("nextlevel_clicked");
		NEXTLEVEL_UNCLICKED = levelLoader.getImagePath("nextlevel_unclicked");
		MUTE_BUTTON = levelLoader.getImagePath("mute_button");
		MUTE_BUTTON_CLICKED = levelLoader.getImagePath("mute_button_clicked");
		MUTE_OFF = levelLoader.getImagePath("mute_button_off");
		RESUME_BUTTON = levelLoader.getImagePath("resume_button");
		RESUME_BUTTON_CLICKED = levelLoader.getImagePath("resume_button_onclick");
		RESUME_BUTTON_HOVER = levelLoader.getImagePath("resume_button_hover");
		CONTROLS_BUTTON = levelLoader.getImagePath("controls_button");
		CONTROLS_BUTTON_CLICKED = levelLoader.getImagePath("controls_button_onclick");
		CONTROLS_BUTTON_HOVER = levelLoader.getImagePath("controls_button_hover");
		LEVEL_SELECT_BUTTON = levelLoader.getImagePath("level_select_button");
		LEVEL_SELECT_BUTTON_CLICKED = levelLoader.getImagePath("level_select_button_onclick");
		LEVEL_SELECT_BUTTON_HOVER = levelLoader.getImagePath("level_select_button_hover");
		TITLE_SCREEN_BUTTON = levelLoader.getImagePath("title_button");
		TITLE_SCREEN_BUTTON_CLICKED = levelLoader.getImagePath("title_button_onclick");
		TITLE_SCREEN_BUTTON_HOVER = levelLoader.getImagePath("title_button_hover");
		CONTROLS_FILE = levelLoader.getImagePath("controls");
		NUMBER_FILE = levelLoader.getImagePath(String.valueOf(levelLoader.level.num+1));
		CIRCLE_FILE = levelLoader.getImagePath("completed_unclicked");

		// Load the backgrounds.
		manager.load(BKGD_FILE,Texture.class);
		assets.add(BKGD_FILE);
		manager.load(SPACE_BKGD_FILE,Texture.class);
		assets.add(SPACE_BKGD_FILE);
		manager.load(CLOUD_BKGD_FILE,Texture.class);
		assets.add(CLOUD_BKGD_FILE);
		manager.load(PAUSE_BKGD_FILE,Texture.class);
		assets.add(PAUSE_BKGD_FILE);
		manager.load(WIN_BKGD_FILE,Texture.class);
		assets.add(WIN_BKGD_FILE);
		manager.load(WIN_BKGD_FILE0,Texture.class);
		assets.add(WIN_BKGD_FILE0);
		manager.load(WIN_BKGD_FILE1,Texture.class);
		assets.add(WIN_BKGD_FILE1);
		manager.load(WIN_BKGD_FILE2,Texture.class);
		assets.add(WIN_BKGD_FILE2);
		manager.load(WIN_BKGD_FILE3,Texture.class);
		assets.add(WIN_BKGD_FILE3);
		manager.load(LOSE_BKGD_FILE,Texture.class);
		assets.add(LOSE_BKGD_FILE);
		manager.load(VIGNETTE_FILE, Texture.class);
		assets.add(VIGNETTE_FILE);
		manager.load(PAUSE_BUTTON, Texture.class);
		assets.add(PAUSE_BUTTON);
		manager.load(PAUSE_BUTTON_CLICKED, Texture.class);
		assets.add(PAUSE_BUTTON_CLICKED);
		manager.load(MAP_BUTTON, Texture.class);
		assets.add(MAP_BUTTON);
		manager.load(MAP_BUTTON_CLICKED, Texture.class);
		assets.add(MAP_BUTTON_CLICKED);
		manager.load(BACK_BUTTON, Texture.class);
		assets.add(BACK_BUTTON);
		manager.load(BACK_BUTTON_CLICKED, Texture.class);
		assets.add(BACK_BUTTON_CLICKED);
		manager.load(NEXTLEVEL_CLICKED,Texture.class);
		assets.add(NEXTLEVEL_CLICKED);
        manager.load(NEXTLEVEL_UNCLICKED,Texture.class);
        assets.add(NEXTLEVEL_UNCLICKED);
        manager.load(LEVELSELECT_CLICKED,Texture.class);
        assets.add(LEVELSELECT_CLICKED);
        manager.load(LEVELSELECT_UNCLICKED,Texture.class);
        assets.add(LEVELSELECT_UNCLICKED);
		manager.load(MOON_ICON, Texture.class);
		assets.add(MOON_ICON);
		manager.load(MUTE_BUTTON, Texture.class);
		assets.add(MUTE_BUTTON);
		manager.load(MUTE_BUTTON_CLICKED, Texture.class);
		assets.add(MUTE_BUTTON_CLICKED);
		manager.load(MUTE_OFF, Texture.class);
		assets.add(MUTE_OFF);
		manager.load(RESUME_BUTTON, Texture.class);
		assets.add(RESUME_BUTTON);
		manager.load(RESUME_BUTTON_CLICKED, Texture.class);
		assets.add(RESUME_BUTTON_CLICKED);
		manager.load(RESUME_BUTTON_HOVER, Texture.class);
		assets.add(RESUME_BUTTON_HOVER);
		manager.load(CONTROLS_BUTTON, Texture.class);
		assets.add(CONTROLS_BUTTON);
		manager.load(CONTROLS_BUTTON_CLICKED, Texture.class);
		assets.add(CONTROLS_BUTTON_CLICKED);
		manager.load(CONTROLS_BUTTON_HOVER, Texture.class);
		assets.add(CONTROLS_BUTTON_HOVER);
		manager.load(LEVEL_SELECT_BUTTON, Texture.class);
		assets.add(LEVEL_SELECT_BUTTON);
		manager.load(LEVEL_SELECT_BUTTON_CLICKED, Texture.class);
		assets.add(LEVEL_SELECT_BUTTON_CLICKED);
		manager.load(LEVEL_SELECT_BUTTON_HOVER, Texture.class);
		assets.add(LEVEL_SELECT_BUTTON_HOVER);
		manager.load(TITLE_SCREEN_BUTTON, Texture.class);
		assets.add(TITLE_SCREEN_BUTTON);
		manager.load(TITLE_SCREEN_BUTTON_CLICKED, Texture.class);
		assets.add(TITLE_SCREEN_BUTTON_CLICKED);
		manager.load(TITLE_SCREEN_BUTTON_HOVER, Texture.class);
		assets.add(TITLE_SCREEN_BUTTON_HOVER);
		manager.load(CONTROLS_FILE, Texture.class);
		assets.add(CONTROLS_FILE);
		//Load the serenity bar
		manager.load(SERENITY_EMPTY,Texture.class);
		assets.add(SERENITY_EMPTY);
		manager.load(SERENITY_FULL,Texture.class);
		assets.add(SERENITY_FULL);
		manager.load(SERENITY_HURT,Texture.class);
		assets.add(SERENITY_HURT);
		manager.load(SERENITY_LINE,Texture.class);
		assets.add(SERENITY_LINE);
		manager.load(SERENITY_LABEL, Texture.class);
		assets.add(SERENITY_LABEL);

		manager.load(NUMBER_FILE, Texture.class);
		assets.add(NUMBER_FILE);
		manager.load(CIRCLE_FILE, Texture.class);
		assets.add(CIRCLE_FILE);

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
		if (manager.isLoaded(SPACE_BKGD_FILE)) {
			spaceBackground = manager.get(SPACE_BKGD_FILE, Texture.class);
			spaceBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CLOUD_BKGD_FILE)) {
			cloudBackground = manager.get(CLOUD_BKGD_FILE, Texture.class);
			cloudBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(PAUSE_BKGD_FILE)) {
			pauseBackground = manager.get(PAUSE_BKGD_FILE, Texture.class);
			pauseBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE)) {
			winBackground = manager.get(WIN_BKGD_FILE, Texture.class);
			winBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE0)) {
			winBackground0 = manager.get(WIN_BKGD_FILE0, Texture.class);
			winBackground0.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE1)) {
			winBackground1 = manager.get(WIN_BKGD_FILE1, Texture.class);
			winBackground1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE2)) {
			winBackground2 = manager.get(WIN_BKGD_FILE2, Texture.class);
			winBackground2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(WIN_BKGD_FILE3)) {
			winBackground3 = manager.get(WIN_BKGD_FILE3, Texture.class);
			winBackground3.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(LOSE_BKGD_FILE)) {
			loseBackground = manager.get(LOSE_BKGD_FILE, Texture.class);
			loseBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(VIGNETTE_FILE)) {
			vignette = manager.get(VIGNETTE_FILE, Texture.class);
			vignette.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(PAUSE_BUTTON)) {
			pause = manager.get(PAUSE_BUTTON, Texture.class);
			pause.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(PAUSE_BUTTON_CLICKED)) {
			pauseClick = manager.get(PAUSE_BUTTON_CLICKED, Texture.class);
			pauseClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(MUTE_BUTTON)) {
			muteButton = manager.get(MUTE_BUTTON, Texture.class);
			muteButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(MUTE_BUTTON_CLICKED)) {
			muteClick = manager.get(MUTE_BUTTON_CLICKED, Texture.class);
			muteClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(MUTE_OFF)) {
			muteOff = manager.get(MUTE_OFF, Texture.class);
			muteOff.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(MAP_BUTTON)) {
			map = manager.get(MAP_BUTTON, Texture.class);
			map.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(MAP_BUTTON_CLICKED)) {
			mapClick = manager.get(MAP_BUTTON_CLICKED, Texture.class);
			mapClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(BACK_BUTTON)) {
			back = manager.get(BACK_BUTTON, Texture.class);
			back.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(BACK_BUTTON_CLICKED)) {
			backClick = manager.get(BACK_BUTTON_CLICKED, Texture.class);
			backClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CONTROLS_FILE)) {
			controls = manager.get(CONTROLS_FILE, Texture.class);
			controls.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(NUMBER_FILE)) {
			number = manager.get(NUMBER_FILE, Texture.class);
			number.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CIRCLE_FILE)) {
			circle = manager.get(CIRCLE_FILE, Texture.class);
			circle.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(MOON_ICON)) {
			moonIcon = manager.get(MOON_ICON, Texture.class);
			moonIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(SERENITY_FULL)) {
			serenityBarFull = manager.get(SERENITY_FULL,Texture.class);
			//serenityBarFull.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(SERENITY_EMPTY)) {
			serenityBarEmpty = manager.get(SERENITY_EMPTY,Texture.class);
			//serenityBarEmpty.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(SERENITY_HURT)) {
			serenityBarHurt = manager.get(SERENITY_HURT,Texture.class);
		}

		if(manager.isLoaded(SERENITY_LINE)) {
			serenityLine = manager.get(SERENITY_LINE,Texture.class);
		}
		if(manager.isLoaded(SERENITY_LABEL)) {
			serenityLabel = manager.get(SERENITY_LABEL,Texture.class);
		}

		if(manager.isLoaded(NEXTLEVEL_CLICKED)) {
			nextLevelClicked = manager.get(NEXTLEVEL_CLICKED,Texture.class);
		}

		if (manager.isLoaded(NEXTLEVEL_UNCLICKED)) {
			nextLevelUnclicked = manager.get(NEXTLEVEL_UNCLICKED,Texture.class);
		}

		if (manager.isLoaded(LEVELSELECT_CLICKED)) {
			levelSelectClicked = manager.get(LEVELSELECT_CLICKED,Texture.class);
		}

		if (manager.isLoaded(LEVELSELECT_UNCLICKED)) {
			levelSelectUnclicked = manager.get(LEVELSELECT_UNCLICKED,Texture.class);
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

		if (manager.isLoaded(RESUME_BUTTON)) {
			resumeButton = manager.get(RESUME_BUTTON, Texture.class);
			resumeButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(RESUME_BUTTON_CLICKED)) {
			resumeClick = manager.get(RESUME_BUTTON_CLICKED, Texture.class);
			resumeClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(RESUME_BUTTON_HOVER)) {
			resumeHover = manager.get(RESUME_BUTTON_HOVER, Texture.class);
			resumeHover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(LEVEL_SELECT_BUTTON)) {
			levelSelectButton = manager.get(LEVEL_SELECT_BUTTON, Texture.class);
			levelSelectButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(LEVEL_SELECT_BUTTON_CLICKED)) {
			levelSelectClick = manager.get(LEVEL_SELECT_BUTTON_CLICKED, Texture.class);
			levelSelectClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(LEVEL_SELECT_BUTTON_HOVER)) {
			levelSelectHover = manager.get(LEVEL_SELECT_BUTTON_HOVER, Texture.class);
			levelSelectHover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(TITLE_SCREEN_BUTTON)) {
			titleScreenButton = manager.get(TITLE_SCREEN_BUTTON, Texture.class);
			titleScreenButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(TITLE_SCREEN_BUTTON_CLICKED)) {
			titleScreenClick = manager.get(TITLE_SCREEN_BUTTON_CLICKED, Texture.class);
			titleScreenClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(TITLE_SCREEN_BUTTON_HOVER)) {
			titleScreenHover = manager.get(TITLE_SCREEN_BUTTON_HOVER, Texture.class);
			titleScreenHover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CONTROLS_BUTTON)) {
			controlsButton = manager.get(CONTROLS_BUTTON, Texture.class);
			controlsButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CONTROLS_BUTTON_CLICKED)) {
			controlsClick = manager.get(CONTROLS_BUTTON_CLICKED, Texture.class);
			controlsClick.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		if (manager.isLoaded(CONTROLS_BUTTON_HOVER)) {
			controlsHover = manager.get(CONTROLS_BUTTON_HOVER, Texture.class);
			controlsHover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
	protected GameMode(float width, float height, float gravity, LevelLoader levelLoader, Level level, boolean mute) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity), levelLoader, level, mute);
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
	protected GameMode(Rectangle bounds, Vector2 gravity, LevelLoader levelLoader, Level level, boolean mute) {
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
		//this.serenity = level.maxSerenity;
		this.gameState = GameState.INTRO;
		Gdx.input.setInputProcessor(this);
		this.mute = mute;
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
		removeMS.clear();
		removeRocks.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
		removeMS = null;
		removeRocks = null;
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
//		} else if (complete && InputController.getInstance().didNext()) {
//			listener.exitScreen(this, 0, level.num+1);
//			return false;
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
				if (pressState == 2 && buttonOption == 2) {
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.MAP;
					abilityPaused = abilityController.cancelTimers();
					canvas.savePosition();
				} else if (pressState == 2 && buttonOption == 1) {
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.PAUSE;
					abilityPaused = abilityController.cancelTimers();
					canvas.savePosition();
				} else if (InputController.getInstance().didReset()) {
					reset();
				} else if (complete) {
					canvas.savePosition();
					abilityPaused = abilityController.cancelTimers();
					gameState = GameState.WIN;
				} else if (pressState == 2 && buttonOption == 3 && !mute) {
					SoundController.mute();
					mute = true;
					pressState = 0;
					buttonOption = 0;
				} else if (pressState == 2 && buttonOption == 3) {
					SoundController.unmute();
					mute = false;
					pressState = 0;
					buttonOption = 0;
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
				} else if (pressState == 2 && buttonOption == 1) { // button
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.PLAY;
					reset();
				}
				break;
			case PAUSE:
				SoundController.pauseMusic();
				Music m = SoundController.menuMusic();
				m.setVolume(0.16f);
				m.setLooping(true);
				if(!SoundController.isMuted){
					m.play();
				}
//				SoundController.playMusic(m, 0.16f, true);
				if (InputController.getInstance().didPause()) { // P key
					m.stop();
					m.dispose();
					SoundController.resumeMusic();
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.PLAY;
					if (abilityPaused) {
						abilityController.startTimers();
					}
				} else if (pressState == 2 && buttonOption == 1) { // Resume
					m.stop();
					m.dispose();
					SoundController.resumeMusic();
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.PLAY;
					canvas.resetPrevZoom();
					if (abilityPaused) {
						abilityController.startTimers();
					}
				} else if (pressState == 2 && buttonOption == 2) { // Menu
					m.stop();
					m.dispose();
					pressState = 0;
					buttonOption = 0;
					canvas.resetZoom();
					listener.exitScreen(this, 0, -1);
				} else if (pressState == 2 && buttonOption == 3) { // Title screen
					m.stop();
					m.dispose();
					pressState = 0;
					buttonOption = 0;
					canvas.resetZoom();
					listener.exitScreen(this, 0, -2);
				} else if (pressState == 2 && buttonOption == 4) { // Controls
					pressState = 0;
					buttonOption = 0;
					gameState = GameState.CONTROLS;
				}
				break;
			case MAP:
				if (pressState == 2 && buttonOption == 2) {
					zoomIn = true;
				} else if (!zoomIn) {
					canvas.zoomOut();
				}
				if (zoomIn) {
					canvas.zoomIn();
					if (canvas.doneZooming()) {
						canvas.moveBack();
						if (canvas.doneMovingBack()) {
							pressState = 0;
							buttonOption = 0;
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
			case CONTROLS:
				if (pressState == 2 && buttonOption == 1) {
					gameState = GameState.PAUSE;
					pressState = 0;
					buttonOption = 0;
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

//			for (int i = 0; i <  removeMS.size; i++) {
//				removeMS.get(i).deactivatePhysics(world);
//			}

			for (int i = 0; i <  removeRocks.size; i++) {
				removeRocks.get(i).deactivatePhysics(world);
				//removeRocks.clear();
			}

			for (int i = 0; i <  removeWindows.size; i++) {
				removeWindows.get(i).deactivatePhysics(world);
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

	//=====================================smoke animation=======================
	float ox =  0;
	float oy = 0;
	float rx = 25;
	float ry = 25;
	TextureRegion[] animationFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
	float elapsedTime;
	Animation<TextureRegion> animation;
	private static final int FRAME_COLS = 3, FRAME_ROWS = 2;
	public void drawSmoke(GameCanvas canvas, float ox, float oy, float x, float y) {
		float duration = 7f;
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


		animation = new Animation<TextureRegion>(1f, animationFrames);


		TextureRegion currentFrame = animation.getKeyFrame(elapsedTime, true);
		if (elapsedTime < duration) {
			canvas.draw(currentFrame, color, ox, oy, x, y, smoke.getWidth() / FRAME_COLS, smoke.getHeight() / FRAME_ROWS);
		}
		else {
			isSmoke = false;
		}
	}
	//================================================================================================
	float elapsedTimeSP;
	TextureRegion[] animationFramesSP;
	private int FRAME_COLS_SP;
	private int FRAME_ROWS_SP;
	private int add_x;
	public void drawSignpostAnimation(GameCanvas canvas, float x, float y, TextureRegion image, int sp_id) {

		Color color = Color.WHITE;

		elapsedTimeSP += Gdx.graphics.getDeltaTime();
		int index = 0;
		if (sp_id == 1) {
			FRAME_COLS_SP = 5;
			FRAME_ROWS_SP = 1;
		}
		else if (sp_id == 3) {
			FRAME_COLS_SP = 15;
			FRAME_ROWS_SP = 1;
			add_x = -150;
		}
		else if (sp_id == 5) {
			FRAME_COLS_SP = 11;
			FRAME_ROWS_SP = 1;
		}
		else if (sp_id == 7) {
			FRAME_COLS_SP = 7;
			FRAME_ROWS_SP = 1;
		}
		else {
			FRAME_COLS_SP = 1;
			FRAME_ROWS_SP = 1;
		}
		animationFramesSP = new TextureRegion[FRAME_COLS_SP * FRAME_ROWS_SP];

		//System.out.println("draw" + rx);
		TextureRegion[][] tmpFrames = TextureRegion.split(image.getTexture(), image.getRegionWidth()/FRAME_COLS_SP, image.getRegionHeight()/FRAME_ROWS_SP);
		for (int i = 0; i < FRAME_ROWS_SP; i++) {
			System.out.println(FRAME_COLS_SP);
			for (int j = 0; j < FRAME_COLS_SP; j++) {
				animationFramesSP[index++] = tmpFrames[i][j];
			}
		}


		animation = new Animation<TextureRegion>(0.3f, animationFramesSP);


		TextureRegion currentFrame = animation.getKeyFrame(elapsedTimeSP, true);
		canvas.draw(currentFrame, color, x + add_x, y, image.getRegionWidth() / FRAME_COLS_SP, image.getRegionHeight() / FRAME_ROWS_SP);
	}

	public Vector2 findMessagePos(String msg) {
		for (Level.Message m : level.messages) {
			if (m.text.equals(msg)) {
				return new Vector2(m.x, m.y);
			}
		}
		return null;
	}

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

		if (gameState == GameState.CONTROLS) {
			canvas.beginConstantBatch();
			canvas.drawConstantBackground(controls, 0, 0);
			if (pressState == 1 && buttonOption == 1) {
				canvas.drawConstant(backClick, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
			} else {
				canvas.drawConstant(back, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
			}
			canvas.endConstantBatch();
		} else if (gameState == GameState.PAUSE) {
			canvas.beginConstantBatch();
			canvas.drawConstantBackground(pauseBackground, 0, 0);
			if (pressState == 1 && buttonOption == 1) {
				canvas.drawConstant(resumeClick, Color.WHITE, RESUME_X, RESUME_Y);
//			} else if (mouseState == 1 && buttonOption == 1) {
//				canvas.drawConstant(resumeHover, Color.WHITE, RESUME_X, RESUME_Y);
			} else {
				canvas.drawConstant(resumeButton, Color.WHITE, RESUME_X, RESUME_Y);

			}
			if (pressState == 1 && buttonOption == 2) {
				canvas.drawConstant(levelSelectClick, Color.WHITE, LEVEL_SELECT_X, LEVEL_SELECT_Y);
//			} else if (mouseState == 1 && buttonOption == 2) {
//				canvas.drawConstant(levelSelectHover, Color.WHITE, LEVEL_SELECT_X, LEVEL_SELECT_Y);
			} else {
				canvas.drawConstant(levelSelectButton, Color.WHITE, LEVEL_SELECT_X, LEVEL_SELECT_Y);
			}
			if (pressState == 1 && buttonOption == 3) {
				canvas.drawConstant(titleScreenClick, Color.WHITE, TITLE_SCREEN_X, TITLE_SCREEN_Y);
			} else {
				canvas.drawConstant(titleScreenButton, Color.WHITE, TITLE_SCREEN_X, TITLE_SCREEN_Y);
			}
			if (pressState == 1 && buttonOption == 4) {
				canvas.drawConstant(controlsClick, Color.WHITE, CONTROLS_X, CONTROLS_Y);
			} else {
				canvas.drawConstant(controlsButton, Color.WHITE, CONTROLS_X, CONTROLS_Y);
			}
			canvas.endConstantBatch();
		} else if (gameState == GameState.WIN) {
			canvas.beginConstantBatch();
			if(serenity >= THREE_STAR) {
				canvas.drawConstantBackground(winBackground3, 0, 0);
			}
			else if(serenity >= TWO_STAR) {
				canvas.drawConstantBackground(winBackground2, 0, 0);
			}
			else if(serenity >= ONE_STAR) {
				canvas.drawConstantBackground(winBackground1, 0, 0);
			}
			else {
				canvas.drawConstantBackground(winBackground0, 0, 0);
			}
			if( pressState == 1 && buttonOption == 1) {
			    canvas.drawConstant(levelSelectClicked, Color.WHITE, -27, -5, 405, 130);
                canvas.drawConstant(nextLevelUnclicked,Color.WHITE,648,-5,405,130);
			}
			else if(InputController.getInstance().didNext() || pressState == 1 && buttonOption == 2) {
			    canvas.drawConstant(nextLevelClicked,Color.WHITE,648,-5,405,130);
                canvas.drawConstant(levelSelectUnclicked, Color.WHITE, -27, -5, 405, 130);
            }
			else {
			    canvas.drawConstant(levelSelectUnclicked, Color.WHITE, -27, -5, 405, 130);
                canvas.drawConstant(nextLevelUnclicked,Color.WHITE,648,-5,405,130);
            }
            canvas.endConstantBatch();

		} else {
			if (player != null && gameState != GameState.MAP) {
				canvas.begin(player.getX() * scale.x, player.getY() * scale.y, level.width);
			} else {
				canvas.begin();
			}
			//canvas.begin(player.getX()*scale.x, player.getY()*scale.y);
			if (level != null) {
				if (level.background.equals("space")) {
					canvas.drawBackground(spaceBackground, 0,128);
				} else if (level.background.equals("cloud")) {
					canvas.drawBackground(cloudBackground, 0,128);
				} else {
					canvas.drawBackground(background, 0,128);
				}
			} else {
				canvas.drawBackground(background, 0,128);
			}

			for (Obstacle obj : objects) {
				obj.draw(canvas);

				//if (isSmoke) {
				//System.out.println("inside" + isSmoke);
//					if (isAnimationFinished)
//					isSmoke = false;

			}

			//if (System.currentTimeMillis() - lastSmokeAnim < 5000) {

			if (isSmoke) {
				elapsedTime = 0;
				isSmoke = false;
			}
			if(!playerSmokeCoord.isEmpty()) {
				drawSmoke(canvas, playerSmokeCoord.get(0), playerSmokeCoord.get(1), playerSmokeCoord.get(2)-15 , playerSmokeCoord.get(3)-10);
				if(player.isWalking() || player.isJumping()) {
					playerSmokeCoord.clear();
				}
			}

			for (int i = 0; i < smokesCoord.size; i++) {
				Array<Float> temp = smokesCoord.get(i);
				drawSmoke(canvas, temp.get(0), temp.get(1), temp.get(2)+5 , temp.get(3)-10);
			}


			fountainFont.setColor(Color.BLACK);

			fountainFont.setColor(Color.WHITE);

			for (SignPost post : level.getSignposts()) {
				if (post.getMessageVisible()) {
					//canvas.draw(post.image,Color.WHITE,post.messagePos.x,post.messagePos.y,post.image.getRegionWidth(),post.image.getRegionHeight());
					drawSignpostAnimation(canvas, post.messagePos.x, post.messagePos.y, post.image, post.getId());

				}
				else {
					//post.elapsedTimeSP = 0;
				}
			}
//			for (Level.Message message : level.getMessages()) {
//				canvas.drawText(message.text, fountainFont, message.x, message.y, false);
//			}

			canvas.end();

			//display timer for ability

			if (debug) {
				canvas.beginDebug();
				for (Obstacle obj : objects) {
					obj.drawDebug(canvas);
				}
				canvas.endDebug();
			}

			canvas.beginConstantBatch();

			if (abilityController.startedAbility) {
				offset = 0;
			}

			if (gameState == GameState.PLAY) {
				canvas.drawConstant(vignette,new Color(1, 1, 1, 0.8f), -50, 0, 1124, 576);
				canvas.drawConstant(circle, Color.WHITE, -16, 506, 85, 85);
				canvas.drawConstant(number, Color.WHITE, -16, 506, 85, 85);
				canvas.drawConstant(rectangle, Color.WHITE, 0, 0, 2000, 100);

				//canvas.drawConstant(rectangle, Color.WHITE, 21, 490, 420, 125);
				if (abilityController.isUsingAbility()) {
					abilityController.startedAbility = false;
					offset += .09;
					if(abilityController.getLastAbilityUsed().getFountainType() == FountainModel.FountainType.FLIGHT) {
						timeLeftScale = 100 * (abilityController.getTimeLeftForAbility() / 3);
					}
					else {
						timeLeftScale = 100 * (abilityController.getTimeLeftForAbility() / 5);
					}
					canvas.drawConstant(abilityTimer, Color.WHITE, 312, -5 + offset, 110, timeLeftScale);
				} else {
					offset = 0;
				}
				canvas.drawConstant(abilityQueue, Color.WHITE, 10, 0, 450, 100);
				canvas.drawConstant(serenityLabel, Color.WHITE, 609, 5);
				canvas.drawConstant(serenityBarEmpty,Color.WHITE,600,25,400,50);

				if(serenity >= 0) {
					serenityScale = 400 * (serenity/MAX_SERENITY);
					//serenityOffset += .005f;
					if(hurt) {
						canvas.drawConstant(serenityBarHurt,Color.WHITE,600+serenityOffset,25,serenityScale,50);
						//canvas.drawConstant(serenityBarHurt,Color.WHITE,450+serenityOffset,25,serenityScale,50);
					}
					else {
						canvas.drawConstant(serenityBarFull,Color.WHITE,600+serenityOffset,25,serenityScale,50);
						//canvas.drawConstant(serenityBarFull,Color.WHITE,450+serenityOffset,25,serenityScale,50);
					}
					//NOTE: approximate length of serenity bar is X = 970 so we need range x = 600-970
					THREESTAR_X = ((370 * THREE_STAR)/MAX_SERENITY) + 600;
					TWOSTAR_X = ((370 * TWO_STAR)/MAX_SERENITY) + 600;
					ONESTAR_X = ((370 * ONE_STAR)/MAX_SERENITY) + 600;
					canvas.drawConstant(serenityLine,Color.WHITE,THREESTAR_X,37,20,30);
					canvas.drawConstant(serenityLine,Color.WHITE,TWOSTAR_X,37,20,30);
					canvas.drawConstant(serenityLine,Color.WHITE,ONESTAR_X,37,20,30);
				}
				abilityController.drawQueue(canvas);

				hurt = false;
				//canvas.drawConstant(moonIcon, Color.WHITE, 870, 20, 65, 65);
				canvas.drawConstant(moonIcon, Color.WHITE, 470, 20, 65, 65);

				fountainFont.setColor(Color.YELLOW);

				String moonsLeft = (removeMS.size) + " / " + level.getMoonShards().size();
				//String timer = "Timer: " + (String.valueOf(abilityController.getTimeLeftForAbility()) + "0000").substring(0, 5);
				//canvas.drawText(moonsLeft, fountainFont, SERENITY_X + 295, 67, true);
				canvas.drawText(moonsLeft, fountainFont, SERENITY_X - 105, 67, true);
				if (pressState == 1 && buttonOption == 1) {
					canvas.drawConstant(pauseClick, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
				} else {
					canvas.drawConstant(pause, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
				}
				if (pressState == 1 && buttonOption == 2) {
					canvas.drawConstant(mapClick, Color.WHITE, PAUSE_X-60, PAUSE_Y, 65, 65);
				} else {
					canvas.drawConstant(map, Color.WHITE, PAUSE_X-60, PAUSE_Y, 65, 65);
				}
				if (pressState == 1 && buttonOption == 3) {
					canvas.drawConstant(muteClick, Color.WHITE, PAUSE_X-120, PAUSE_Y, 65, 65);
				} else if (mute){
					canvas.drawConstant(muteButton, Color.WHITE, PAUSE_X-120, PAUSE_Y, 65, 65);
				} else {
					canvas.drawConstant(muteOff, Color.WHITE, PAUSE_X-120, PAUSE_Y, 65, 65);
				}
			} else if (gameState == GameState.MAP) {
				if (pressState == 1 && buttonOption == 2) {
					canvas.drawConstant(backClick, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
				} else {
					canvas.drawConstant(back, Color.WHITE, PAUSE_X, PAUSE_Y, 65, 65);
				}
			}

//		if(gameplayController.getCollisions()) {
//			fountainFont.setColor(Color.RED);
//		}
			//else {
			fountainFont.setColor(Color.GREEN);
			//}
			//String serenity = "Serenity: " + (this.serenity);
			//canvas.drawText(serenity, fountainFont, SERENITY_TEXT_X, SERENITY_TEXT_Y);


			//fountainFont.setColor(Color.BLACK);
			//canvas.drawText(this.level.name, fountainFont, LEVEL_TEXT_X, LEVEL_TEXT_Y, true);

			//canvas.end();


			// Final message
			if (complete && !failed) {
				//displayFont.setColor(Color.YELLOW);

				//canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
				//canvas.drawText("Press n for next level.", fountainFont, canvas.getWidth()/2.0f - 110, canvas.getHeight()/2.0f-50,true);

			} else if (failed) {
				gameState = GameState.LOSE;
				canvas.drawConstantBackground(loseBackground, 0, 0);
				if (pressState == 1 && buttonOption == 1) {
					canvas.drawConstant(backClick, Color.WHITE, 463, 4);
				}
				//canvas.drawText("Press R to try again", fountainFont, 10, canvas.getHeight() - 25, true);
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
				//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

	private float[] resumePositions = {365, 636, 323, 387}; //x1, x2, y1, y2
	private float[] menuPositions = {388, 611, 238, 287}; //x1, x2, y1, y2
	private float[] titlePositions = {389, 610, 175, 217}; //x1, x2, y1, y2
	private float[] controlsPositions = {389, 610, 106, 150}; //x1, x2, y1, y2
	private float[] mapPositions = {404, 614, 70, 112}; //x1, x2, y1, y2
	private float[] nextLevelPositions = {705, 997, 24, 96}; //x1, x2, y1, y2
	private float[] menuWinPositions = {28, 319, 25, 97}; //x1, x2, y1, y2

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

				// Inside title button
				if (screenX >= titlePositions[0] && screenX <= titlePositions[1]) {
					if (screenY >= titlePositions[2] && screenY <= titlePositions[3]) {
						pressState = 1;
						buttonOption = 3;
					}
				}

				// Inside controls button
				if (screenX >= controlsPositions[0] && screenX <= controlsPositions[1]) {
					if (screenY >= controlsPositions[2] && screenY <= controlsPositions[3]) {
						pressState = 1;
						buttonOption = 4;
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
			} else if (gameState == GameState.PLAY) {
				// Inside pause button
				float radius = 65/2.0f;
				float x = PAUSE_X+radius;
				float y = PAUSE_Y+radius;
				float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 1;
				}

				// Inside map button
				x = PAUSE_X-60+radius;
				y = PAUSE_Y+radius;
				dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 2;
				}
				// Inside mute button
				x = PAUSE_X-120+radius;
				y = PAUSE_Y+radius;
				dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 3;
				}
			} else if (gameState == GameState.MAP) {
				// Inside map button
				float radius = 65/2.0f;
				float x = PAUSE_X+radius;
				float y = PAUSE_Y+radius;
				float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 2;
				}
			} else if (gameState == GameState.CONTROLS) {
				// Inside back button
				float radius = 65/2.0f;
				float x = PAUSE_X+radius;
				float y = PAUSE_Y+radius;
				float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 1;
				}
			} else if (gameState == GameState.LOSE) {
				// Inside lose button
				float radius = 100/2.0f;
				float x = 514;
				float y = 55;
				float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
				if (dist < radius*radius) {
					pressState = 1;
					buttonOption = 1;
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
	public boolean keyDown(int keycode) {
		if ((keycode == Input.Keys.N && gameState == GameState.WIN) || (keycode == Input.Keys.M && (gameState == GameState.PLAY || gameState == GameState.MAP))) {
			pressState = 1;
			buttonOption = 2;
			return false;
		} else if (keycode == Input.Keys.P && (gameState==GameState.PAUSE || gameState == GameState.PLAY)) {
			pressState = 1;
			buttonOption = 1;
			return false;
		}
		return true;
	}

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
	public boolean keyUp(int keycode) {
		if (pressState == 1) {
			pressState = 2;
			return false;
		}
		return true;
	}

	/**
	 * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @return whether to hand the event to other listeners.
	 */
	public boolean mouseMoved(int screenX, int screenY) {
		if (canvas != null) {

			// Flip to match graphics coordinates
			screenY = canvas.getHeight() - screenY;

			if (gameState == GameState.PAUSE) {
				// Inside resume button
				if (screenX >= resumePositions[0] && screenX <= resumePositions[1]) {
					if (screenY >= resumePositions[2] && screenY <= resumePositions[3]) {
						mouseState = 1;
						buttonOption = 1;
					}
				} else if (screenX >= menuPositions[0] && screenX <= menuPositions[1]) {
					if (screenY >= menuPositions[2] && screenY <= menuPositions[3]) {
						mouseState = 1;
						buttonOption = 2;
					}
				} else {
					mouseState = 0;
				}
			}
		}


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

	public void editSaveJson(Boolean comp) {};

}