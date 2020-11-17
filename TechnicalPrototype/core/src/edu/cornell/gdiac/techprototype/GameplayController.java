package edu.cornell.gdiac.techprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.techprototype.objects.FountainModel;
import edu.cornell.gdiac.techprototype.objects.PlayerModel;
import edu.cornell.gdiac.techprototype.obstacles.BoxObstacle;
import edu.cornell.gdiac.techprototype.obstacles.Obstacle;
import edu.cornell.gdiac.techprototype.obstacles.PolygonObstacle;
import edu.cornell.gdiac.techprototype.platforms.CloudPlatform;
import edu.cornell.gdiac.techprototype.platforms.SpikedPlatform;
import edu.cornell.gdiac.techprototype.platforms.Platform;
import com.badlogic.gdx.audio.Sound;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller to handle gameplay interactions.
 *
 * This controller also acts as the root class for all the models.
 */
public class GameplayController {

    /** The world scale */
    protected Vector2 scale;
    /** Reference to the player */
    private PlayerModel player;
    /** The serenity for the game, in charge of overall time and health for the player */
    protected int serenity;
    /** All the objects in the world. */
    protected Array<Obstacle> objects  = new Array<Obstacle>();
    //spec later
    protected boolean usedDashFountain;
    protected boolean usedFlightFountain;
    protected boolean usedTransparencyFountain;


    /** The Box2D world */
    protected World world;

    /** The texture file for a player */
    private static final String PLAYER_FILE = "images/player.png";
    private static final String FLIGHT_FILE = "images/player_flight.png";
    private static final String DASH_FILE = "images/player_dash.png";
    private static final String TRANSP_FILE = "images/player_transp.png";
    /** Texture for the player */
    private TextureRegion playerTexture;
    private TextureRegion dashTexture;
    private TextureRegion flightTexture;
    private TextureRegion transpTexture;

    /** The last used fountain */
    private FountainModel lastUsed;
    /** The texture file for a dash fountain */
    private static final String DASH_FOUNTAIN_FILE = "images/dash_fountain.png";
    /** Texture for the dash fountain */
    private TextureRegion dashFountainTexture;
    /** The texture file for a dash ability card */
    private static final String DASH_CARD_FILE = "images/dash_icon.png";
    /** Texture for the dash ability card */
    private TextureRegion dashAbilityTexture;

    /** The texture file for a cloud fountain */
    private static final String CLOUD_FOUNTAIN_FILE = "images/cloud_fountain.png";
    /** Texture for the cloud fountain */
    private TextureRegion cloudFountainTexture;
    /** The texture file for a transparency ability card  */
    private static final String TRANSPARENT_CARD_FILE = "images/transparent_icon.png";
    /** Texture for the transparency ability card */
    private TextureRegion transparencyAbilityTexture;
    /** File to texture for the win door */
    private static String GOAL_FILE = "images/stairs.png";
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    /** The texture file for a flight fountain */
    private static final String FLIGHT_FOUNTAIN_FILE = "images/flight_fountain.png";
    /** Texture for the flight fountain */
    private TextureRegion flightFountainTexture;
    /** The texture file for a flight ability card  */
    private static final String FLIGHT_CARD_FILE = "images/flight_icon.png";
    /** Texture for the flight ability card */
    private TextureRegion flightAbilityTexture;

    /** Retro font for displaying messages */
    private static String FONT_FILE = "fonts/MarkerFelt.ttf";
    private static int FONT_SIZE = 64;

    /** Handles the abilities and their timers */
    private AbilityController abilityController = AbilityController.getInstance();

    /** The spike platform - castle spike platform*/
    private static final String CASTLE_SPIKE_FILE = "images/spike_castle.png";
    /** Texture for the castle spike platform */
    private TextureRegion castleSpikeTexture;

    /** File to texture for walls and platforms */
    private static String TILE_FILE = "images/tile.png";
    /** The texture for walls and platforms */
    protected TextureRegion tile;

    /** File to texture for cloud platforms */
    private static String CLOUD_FILE = "images/cloud_platform.png";
    /** The texture for walls and platforms */
    protected TextureRegion cloudTile;

    /** File to texture for windows */
    private static String WINDOW_FILE = "images/window.png";
    /** The texture for walls and platforms */
    protected TextureRegion window;


    /** Reference to the game canvas */
    protected GameCanvas canvas;

    /** The boundary of the world */
    protected Rectangle bounds;
    /** Whether we have completed this level */
    private boolean complete;
    /** Whether we have failed at this world (and need a reset) */
    private boolean failed;
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;

    //Important constants
    private int MAX_SERENITY = 10000;
    private int NUM_FOUNTAINS = 3;
    private static Vector2 TFOUNTAIN_POS = new Vector2(170, 270);
    private static Vector2 FFOUNTAIN_POS = new Vector2(530, 10);
    private static Vector2 DFOUNTAIN_POS = new Vector2(190, 10);
    private static Vector2 DFOUNTAIN_POS2 = new Vector2(60, 340);
    private int ABILITY_TIME = 5000;



    // Physics constants for initialization
    /** The new heavier gravity for this world (so it is not so floaty) */
    private static final float  DEFAULT_GRAVITY = -14.7f;
    /** The density for most physics objects */
    private static final float  BASIC_DENSITY = 0.0f;
    /** The density for a bullet */
    private static final float  HEAVY_DENSITY = 10.0f;
    /** Friction of most platforms */
    private static final float  BASIC_FRICTION = 0.4f;
    /** The restitution for all physics objects */
    private static final float  BASIC_RESTITUTION = 0.1f;
    /** Spike castle vertices*/
    public float[] spikeVertices = {0.0f, 0.2f, 1.0f, 3.0f};


    /** The set of moving platforms in the level */
    protected List<Platform> movingPlatforms = new ArrayList<Platform>();

    /** The outlines of all of the platforms */
    private static final float[][] PLATFORMS = {
            {200f, 120f,310f, 120f,310f, 150f,200f, 150f},
            {100f, 200f,210f, 200f,210f, 170f,100f, 170f},
            {-200f, 55f,-200f, 0f,100f, 55f,100f, 0f},
            {800f, 55f,100f, 55f,100f, 0f,900f, 0f},
            {230f, 80f,310f, 80f,310f, 50f,230f, 50f},
            {230f, 80f,310f, 80f,310f, 50f,230f, 50f},
            {250f, 100f,330f, 100f,330f, 70f,250f, 70f},
            {230f, 80f,310f, 80f,310f, 50f,230f, 50f},
            {800f, 35f,300f, 35f,300f, 0f,900f, 0f},
            // {190f,125f,230f,125f,230f,120f,190f,120f},
            //{ 50f,125f, 70f,125f, 70f,120f, 50f,120f},
            // { 120f,120f, 150f,120f, 150f,125f, 120f,125f}
    };

    private List<FountainModel> fountainsList = new ArrayList<FountainModel>();
    private BoxObstacle dfountain;
    private BoxObstacle dfountain2;
    private BoxObstacle ffountain;
    private BoxObstacle efountain;
    private PlayerModel avatar;

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds	The game bounds in Box2d coordinates
     */
    public GameplayController(Rectangle bounds, World world) {
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        this.world = world;
        complete = false;
        failed = false;
        usedDashFountain = false;
        usedFlightFountain = false;
        usedTransparencyFountain = false;
        lastUsed = null;


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
     */
    public GameplayController(float width, float height, World world) {
        this(new Rectangle(0,0,width,height), world);
    }

    /**
     * Returns the list of the currently active (not destroyed) game objects
     *
     * As this method returns a reference and Lists are mutable, other classes can
     * technical modify this list.  That is a very bad idea.  Other classes should
     * only mark objects as destroyed and leave list management to this class.
     *
     * @return the list of the currently active (not destroyed) game objects
     */
    public Array<Obstacle> getObjects() {
        return objects;
    }

    public void reset(World world, ContactListener cl) {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        abilityController.reset();
        setComplete(false);
        setFailure(false);
    }

    /**
     * Returns the time left from ability controller
     *
     * @return float representing time left with ability
     */
    public float getTimer() {return abilityController.getTimeLeftForAbility(); }

    public void setTimer(int val) {abilityController.setTimer(val);}

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
        failed = value;
    }

    /**
     * Preloads the assets for this game.
     *
     * The asset manager for LibGDX is asynchronous.  That means that you
     * tell it what to load and then wait while it loads them.  This is
     * the first step: telling it what to load.
     *
     * @param manager Reference to global asset manager.
     * @param assets  Asset list to track which assets where loaded
     */
    public void preLoadContent(AssetManager manager, Array<String> assets) {
        manager.load(PLAYER_FILE,Texture.class);
        assets.add(PLAYER_FILE);
        manager.load(DASH_FILE,Texture.class);
        assets.add(DASH_FILE);
        manager.load(FLIGHT_FOUNTAIN_FILE,Texture.class);
        assets.add(FLIGHT_FOUNTAIN_FILE);
        manager.load(FLIGHT_FILE,Texture.class);
        assets.add(FLIGHT_FILE);
        manager.load(TRANSP_FILE,Texture.class);
        assets.add(TRANSP_FILE);
        manager.load(DASH_FOUNTAIN_FILE,Texture.class);
        assets.add(DASH_FOUNTAIN_FILE);
        manager.load(DASH_CARD_FILE,Texture.class);
        assets.add(DASH_CARD_FILE);
        manager.load(CLOUD_FOUNTAIN_FILE,Texture.class);
        assets.add(CLOUD_FOUNTAIN_FILE);
        manager.load(CASTLE_SPIKE_FILE, Texture.class);
        assets.add(CASTLE_SPIKE_FILE);
        manager.load(TILE_FILE,Texture.class);
        assets.add(TILE_FILE);
        manager.load(WINDOW_FILE,Texture.class);
        assets.add(WINDOW_FILE);
        manager.load(TRANSPARENT_CARD_FILE,Texture.class);
        assets.add(TRANSPARENT_CARD_FILE);
        manager.load(FLIGHT_CARD_FILE,Texture.class);
        assets.add(FLIGHT_CARD_FILE);
        manager.load(GOAL_FILE,Texture.class);
        assets.add(GOAL_FILE);
        manager.load(CLOUD_FILE,Texture.class);
        assets.add(CLOUD_FILE);
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = FONT_FILE;
        size2Params.fontParameters.size = FONT_SIZE;
        manager.load(FONT_FILE, BitmapFont.class, size2Params);
        assets.add(FONT_FILE);
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

    public void setAbility() {
        if(abilityController.isAbilityActive(FountainModel.FountainType.DASH)) {
            player.setTexture(dashTexture);
            lastUsed = abilityController.getLastAbilityUsed();
        }
        else if(abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)) {
            player.setTexture(flightTexture);
            lastUsed = abilityController.getLastAbilityUsed();
        }
        else if(abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY)) {
            player.setTexture(transpTexture);
            lastUsed = abilityController.getLastAbilityUsed();
            player.setTransparencyActive(true);
        }
        else {
            player.setTexture(playerTexture);
        }

    }

    public PlayerModel getPlayer() { return avatar; }
    public BoxObstacle getDfountain() { return dfountain; }
    public BoxObstacle getFfountain() { return ffountain; }
    public BoxObstacle getEfountain() { return efountain; }
    public BoxObstacle getGoalDoor() { return goalDoor; }
    public List<FountainModel> getFountainsList() { return fountainsList; }


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
        playerTexture = createTexture(manager,PLAYER_FILE, false);
        dashTexture = createTexture(manager,DASH_FILE, false);
        flightFountainTexture = createTexture(manager,FLIGHT_FOUNTAIN_FILE, false);
        flightTexture = createTexture(manager,FLIGHT_FILE, false);
        transpTexture = createTexture(manager,TRANSP_FILE, false);
        dashFountainTexture = createTexture(manager,DASH_FOUNTAIN_FILE, false);
        dashAbilityTexture = createTexture(manager,DASH_CARD_FILE, false);
        transparencyAbilityTexture = createTexture(manager,TRANSPARENT_CARD_FILE, false);
        flightAbilityTexture = createTexture(manager,FLIGHT_CARD_FILE, false);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        castleSpikeTexture = createTexture(manager, CASTLE_SPIKE_FILE, false);
        tile = createTexture(manager,TILE_FILE,true);
        window = createTexture(manager,WINDOW_FILE,true);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        transparencyAbilityTexture = createTexture(manager,TRANSPARENT_CARD_FILE, false);
        flightAbilityTexture = createTexture(manager,FLIGHT_CARD_FILE, false);
        goalTile  = createTexture(manager,GOAL_FILE,true);
        cloudTile  = createTexture(manager,CLOUD_FILE,true);
        if (manager.isLoaded(FONT_FILE)) {
            displayFont = manager.get(FONT_FILE,BitmapFont.class);
        } else {
            displayFont = null;
        }
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

    /** Adds the ability to the queue */
    public void addToQueue(FountainModel fountain) {
//        if(ability == FountainModel.FountainType.DASH && !usedDashFountain) {
//            usedDashFountain = true;
//            abilityController.addAbility(ability);
//        }
//        else if(ability == FountainModel.FountainType.FLIGHT && !usedFlightFountain) {
//            usedFlightFountain = true;
//            abilityController.addAbility(ability);
//        }
//        else if(ability == FountainModel.FountainType.TRANSPARENCY && !usedTransparencyFountain) {
//            usedTransparencyFountain = true;
//            abilityController.addAbility(ability);
//        }
        if(fountain.isAvailable()) {
            abilityController.addAbility(fountain);
            fountain.setAvailable(false);
        }

    }
    /**
     * Lays out the game geography.
     */
    public void start() {

        Sound s = SoundController.startLevelSound();
        s.play(0.8f);

        // Drawing cloud platforms
        //TODO: can make cloud platforms visible only when transparency is activated
        List<Vector2> cloudPlatformPositions = new ArrayList<Vector2>();
        cloudPlatformPositions.add(new Vector2(700f, 200f));
        cloudPlatformPositions.add(new Vector2(700f, 300f));
        cloudPlatformPositions.add(new Vector2(900f, 300f));
        List<Vector2> cloudPlatformVelocities = new ArrayList<Vector2>();
        cloudPlatformVelocities.add(new Vector2(0.0f, 0.0f));
        cloudPlatformVelocities.add(new Vector2(0f, 1f));
        cloudPlatformVelocities.add(new Vector2(1f, 0f));
        List<Vector2> cloudPlatformRadiusBounds = new ArrayList<Vector2>();
        cloudPlatformRadiusBounds.add(new Vector2(0f, 0f));
        cloudPlatformRadiusBounds.add(new Vector2(0f, 50f));
        cloudPlatformRadiusBounds.add(new Vector2(50f, 0f));
        Vector2 platformScale = new Vector2(1.0f, 0.5f);
        //tile.getRegionHeight()/scale.y * platformScale.y
        for(int x = 0; x < cloudPlatformPositions.size(); x++){
            CloudPlatform p = new CloudPlatform(cloudPlatformPositions.get(x).x, cloudPlatformPositions.get(x).y,
                    tile.getRegionWidth()/scale.x * platformScale.x,
                    80, scale,
                    cloudPlatformVelocities.get(x), cloudPlatformRadiusBounds.get(x).x,
                    cloudPlatformRadiusBounds.get(x).y);
            p.setBodyType(BodyDef.BodyType.StaticBody);
            p.setDensity(BASIC_DENSITY);
            p.setFriction(BASIC_FRICTION);
            p.setRestitution(BASIC_RESTITUTION);
            p.setSensor(false);
            p.setDrawScale(scale);
            p.setTexture(cloudTile);
            p.setName("cloud platform " + String.valueOf(x));
            addObject(p);
            movingPlatforms.add(p);
        }

        // add normal platforms
        List<Vector2> regPlatformPositions = new ArrayList<Vector2>();
        regPlatformPositions.add(new Vector2(50,180));
        regPlatformPositions.add(new Vector2(-60,150));
        regPlatformPositions.add(new Vector2(150,-40));
        regPlatformPositions.add(new Vector2(400,-40));
        regPlatformPositions.add(new Vector2(200,180));
        regPlatformPositions.add(new Vector2(180,320));
        regPlatformPositions.add(new Vector2(105,195));
        regPlatformPositions.add(new Vector2(-80,200));
        regPlatformPositions.add(new Vector2(550,320));

        String pname = "platform";
        for (int ii = 0; ii < PLATFORMS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(PLATFORMS[ii], regPlatformPositions.get(ii).x, regPlatformPositions.get(ii).y);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(tile);
            obj.setName(pname+ii);
            addObject(obj);
        }

        // Add level goal
        float gwidth  = goalTile.getRegionWidth()/scale.x;
        float gheight = goalTile.getRegionHeight()/scale.y;
        goalDoor = new BoxObstacle(980,400,100,100);
        //goalDoor = new BoxObstacle(100,100,gwidth,gheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(BASIC_DENSITY);
        goalDoor.setFriction(BASIC_FRICTION);
        goalDoor.setRestitution(BASIC_RESTITUTION);
        goalDoor.setSensor(true);
        goalDoor.setTexture(goalTile);
        goalDoor.setDrawScale(scale);
        goalDoor.setName("goal");
        addObject(goalDoor);



        float dwidth  = dashFountainTexture.getRegionWidth()/scale.x;
        float dheight = dashFountainTexture.getRegionWidth()/scale.x;
//        FountainModel dashFountain = new FountainModel(DFOUNTAIN_POS.x, DFOUNTAIN_POS.y, FountainModel.FountainType.DASH);
        FountainModel dashFountain = new FountainModel(DFOUNTAIN_POS.x,DFOUNTAIN_POS.y, FountainModel.FountainType.DASH);
        dashFountain.setDrawScale(scale);
        dashFountain.setName("dash_fountain");
        dashFountain.setTexture(dashFountainTexture);
        dashFountain.setDrawScale(scale);
        addObject(dashFountain);
        dfountain = dashFountain;
        fountainsList.add(dashFountain);

        FountainModel dashFountain2 = new FountainModel(DFOUNTAIN_POS2.x,DFOUNTAIN_POS2.y, FountainModel.FountainType.DASH);
        dashFountain2.setDrawScale(scale);
        dashFountain2.setName("dash_fountain");
        dashFountain2.setTexture(dashFountainTexture);
        dashFountain2.setDrawScale(scale);
        addObject(dashFountain2);
        dfountain2 = dashFountain2;
        fountainsList.add(dashFountain2);

        FountainModel cloudFountain = new FountainModel(TFOUNTAIN_POS.x, TFOUNTAIN_POS.y, FountainModel.FountainType.TRANSPARENCY);
        cloudFountain.setDrawScale(scale);
        cloudFountain.setName("cloud_fountain");
        cloudFountain.setTexture(cloudFountainTexture);
        cloudFountain.setDrawScale(scale);
        addObject(cloudFountain);
        efountain = cloudFountain;
        fountainsList.add(cloudFountain);

        FountainModel flightFountain = new FountainModel(FFOUNTAIN_POS.x, FFOUNTAIN_POS.y, FountainModel.FountainType.FLIGHT);
        flightFountain.setDrawScale(scale);
        flightFountain.setName("flight_fountain");
        flightFountain.setTexture(flightFountainTexture);
        flightFountain.setDrawScale(scale);
        addObject(flightFountain);
        ffountain = flightFountain;
       fountainsList.add(flightFountain);


        //Drawing spike castle
        SpikedPlatform sp = new SpikedPlatform(700,80,200,200, new Vector2(1,1), new Vector2(0,0),
                0f, 4f, SpikedPlatform.SpikeDirection.UP);
        sp.setBodyType(BodyDef.BodyType.StaticBody);
        sp.setDensity(0.0f);
        sp.setFriction(.4f);
        sp.setRestitution(0.1f);
        sp.setSensor(false);
        sp.setDrawScale(scale);
        sp.setTexture(castleSpikeTexture);
        sp.setName("spiked castle");
        addObject(sp);

        SpikedPlatform sp2 = new SpikedPlatform(900,80,200,200, new Vector2(1,1), new Vector2(0,0),
                0f, 4f, SpikedPlatform.SpikeDirection.UP);
        sp2.setBodyType(BodyDef.BodyType.StaticBody);
        sp2.setDensity(0.0f);
        sp2.setFriction(.4f);
        sp2.setRestitution(0.1f);
        sp2.setSensor(false);
        sp2.setDrawScale(scale);
        sp2.setTexture(castleSpikeTexture);
        sp2.setName("spiked castle");
        addObject(sp2);



        dwidth = playerTexture.getRegionWidth() *2 / scale.x;
        dheight = playerTexture.getRegionHeight() * 2/ scale.y;
        player = new PlayerModel(100, 55, dwidth, dheight);
        player.setTexture(playerTexture);
        player.setDrawScale(scale);
        addObject(player);
        avatar = player;
        serenity = MAX_SERENITY;

        abilityController = AbilityController.getInstance();
        abilityController.setDashTexture(dashAbilityTexture);
        abilityController.setFlightTexture(flightAbilityTexture);
        abilityController.setTransparentTexture(transparencyAbilityTexture);
//        abilityController.addAbility(FountainModel.FountainType.DASH);
//        abilityController.addAbility(FountainModel.FountainType.FLIGHT);
//        abilityController.addAbility(FountainModel.FountainType.TRANSPARENCY);

    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x+30 <= obj.getX() && obj.getX() <= bounds.x+bounds.width -30);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * The core gameplay loop of this world.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(InputController inputController, float dt) {
        // Process actions in object model
        setAbility();
        if (inputController.didAbility()) {
            abilityController.useAbility(ABILITY_TIME);
        }
        if(lastUsed != null && !abilityController.isAbilityActive(lastUsed.getFountainType())) {
            if(lastUsed.getFountainType() == FountainModel.FountainType.TRANSPARENCY) { player.setTransparencyActive(false); }
            lastUsed.setAvailable(true);
            lastUsed = null;
        }

        player.setMovement(InputController.getInstance().getHorizontal() * player.getForce());
        player.setJumping(inputController.didPrimary());
        player.setDashing(inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.DASH));
        player.setFlying(inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT));
        player.setTransparent(inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY));

        serenity -= 1;
        player.applyForce();

        //add sound effects
        if (player.isJumping() && player.getGrounded()) {
            player.setGrounded(false);
            Sound s = SoundController.jumpSound();
            s.play(0.8f);
        }
        if (!inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT))
            player.flightState = false;
        if (inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)
        && !player.flightState) {
            player.setGrounded(false);
            player.flightState = true;
            Sound s = SoundController.flightSound();
            s.play(0.8f);
        }

        //check for failure
        if (!failed && player.getPosition().y < 0) {
            setFailure(true);
        }

        //moving the platforms
        for(Platform platform : movingPlatforms){
            Vector2 newPos = platform.getPosition();
            newPos.add(platform.getVelocity());
            if(Math.abs(newPos.x - platform.getOriginalPosition().x) > platform.getHorizontalRadius())
                platform.setVelocity(platform.getVelocity().scl(-1, 1));
            if(Math.abs(newPos.y - platform.getOriginalPosition().y) > platform.getVerticalRadius())
                platform.setVelocity(platform.getVelocity().scl(1,-1));
            platform.setPosition(newPos);
        }

        boolean horiz = (bounds.x+30 <= player.getX() && player.getX() <= bounds.x+bounds.width -30);
        if (!horiz) {
            player.setVX(-player.getVX());
        }
        if (player.getY() > bounds.y+bounds.height-30) {
            player.setVY(-player.getVY());
        }

        for (Obstacle object : objects) {
            object.update(dt);
        }
    }

    public void draw(GameCanvas canvas) {
        abilityController.drawQueue(canvas);
    }

}