package edu.cornell.gdiac.alpha;

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
import edu.cornell.gdiac.alpha.objects.FountainModel;
import edu.cornell.gdiac.alpha.objects.MoonShard;
import edu.cornell.gdiac.alpha.objects.PlayerModel;
import edu.cornell.gdiac.alpha.obstacles.*;
import edu.cornell.gdiac.alpha.platforms.CloudPlatform;
import edu.cornell.gdiac.alpha.platforms.SpikedPlatform;
import edu.cornell.gdiac.alpha.platforms.Platform;
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

    /** The level */
    protected LevelLoader levelLoader;

    /** The texture files for the player */
    private static String PLAYER_FILE;
    private static String FLIGHT_FILE;
    private static String DASH_FILE;
    private static String TRANSP_FILE;
    private static String HURT_FILE;
    /** Textures for the player */
    private TextureRegion playerTexture;
    private TextureRegion dashTexture;
    private TextureRegion flightTexture;
    private TextureRegion transpTexture;
    private TextureRegion hurtTexture;

    /** The last used fountain */
    private FountainModel lastUsed;
    /** The texture file for a dash fountain */
    private static String DASH_FOUNTAIN_FILE;
    /** Texture for the dash fountain */
    private TextureRegion dashFountainTexture;
    /** The texture file for a dash ability icon */
    private static String DASH_ICON_FILE;
    /** Texture for the dash ability icon*/
    private TextureRegion dashAbilityTexture;

    /** The texture file for a cloud fountain */
    private static String CLOUD_FOUNTAIN_FILE;
    /** Texture for the cloud fountain */
    private TextureRegion cloudFountainTexture;
    /** The texture file for a transparency ability icon  */
    private static String TRANSPARENT_ICON_FILE;
    /** Texture for the transparency ability icon */
    private TextureRegion transparencyAbilityTexture;

    /** The texture file for a cloud fountain */
    private static String SERENITY_FOUNTAIN_FILE;
    /** Texture for the cloud fountain */
    private TextureRegion serenityFountainTexture;

    /** File to texture for the moon shard */
    private static String MOON_SHARD_FILE;
    /** The texture for the moon shard */
    protected TextureRegion moonShardTexture;
    /** File to texture for the win door */
    private static String GOAL_FILE = "images/stairs.png";
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    /** The texture file for a flight fountain */
    private static String FLIGHT_FOUNTAIN_FILE;
    /** Texture for the flight fountain */
    private TextureRegion flightFountainTexture;
    /** The texture file for a flight ability icon  */
    private static String FLIGHT_ICON_FILE;
    /** Texture for the flight ability card */
    private TextureRegion flightAbilityTexture;

    /** Retro font for displaying messages */
    private static String FONT_FILE;
    private static int FONT_SIZE = 64;

    /** Handles the abilities and their timers */
    private AbilityController abilityController = AbilityController.getInstance();

    /** The spike platform - castle spike platform*/
    private static String CASTLE_SPIKE_FILE;
    /** Texture for the castle spike platform */
    private TextureRegion castleSpikeTexture;

    /** The falling rock obstacles */
    private static String ROCK_FILE1;
    private static String ROCK_FILE2;
    private static String ROCK_FILE3;
    private static String ROCK_TILE_FILE;
    /** Texture for the rock obstacle */
    private TextureRegion rockTexture1;
    private TextureRegion rockTexture2;
    private TextureRegion rockTexture3;
    private TextureRegion rockTileTexture;

    /** Monsters and textures */
    private static String CROC_FILE;
    private static String FLYING_FILE;
    private TextureRegion crocTexture;
    private TextureRegion flyingTexture;

    /** File to texture for walls and platforms */
    private static String TILE_FILE;
    /** The texture for walls and platforms */
    protected TextureRegion tile;
    /** Files to texture for the end of walls and platforms */
    private static String BRICK_ENDTILE_FILE;
    private static String BRICK_ENDTILE2_FILE;
    private static String BRICK_ENDTILE3_FILE;
    private static String BRICK_ENDTILE4_FILE;
    /** The textures for the end of walls and platforms */
    protected TextureRegion brick_endtile;
    protected TextureRegion brick_endtile2;
    protected TextureRegion brick_endtile3;
    protected TextureRegion brick_endtile4;

    /** File to texture for cloud platforms */
    private static String CLOUD_FILE;
    /** The texture for clouds */
    protected TextureRegion cloudTile;
    /** File to texture for left side cloud platforms */
    private static String CLOUD_LEFT_FILE;
    /** The texture for left side cloud platforms */
    protected TextureRegion cloudLeftTile;
    /** File to texture for right side cloud platforms */
    private static String CLOUD_RIGHT_FILE;
    /** The texture for right side cloud platforms */
    protected TextureRegion cloudRightTile;

    /** File to texture for windows */
    private static String WINDOW_FILE;
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
    private int ABILITY_TIME = 5000;
    private Vector2 ROCK_VELOCITY = new Vector2(0,-1f);

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


    /** The set of moving platforms in the level */
    protected List<Platform> movingPlatforms = new ArrayList<Platform>();

    /** The set of rocks in the level */
    protected List<Rock> rocks = new ArrayList<Rock>();

    /** The set of all platforms in the level */
    protected List<BoxObstacle> platforms = new ArrayList<BoxObstacle>();

    /** The set of crocodiles in the level */
    protected List<Crocodile> crocodiles = new ArrayList<Crocodile>();

    /** The set of flying monsters in the level */
    protected List<FlyingMonster> flyingMonsters = new ArrayList<FlyingMonster>();

    private List<FountainModel> fountainsList = new ArrayList<FountainModel>();
    private List<MoonShard> moonShardsList = new ArrayList<MoonShard>();
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
    public GameplayController(Rectangle bounds, World world, LevelLoader level) {
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        this.world = world;
        this.levelLoader = level;
        complete = false;
        failed = false;
        lastUsed = null;

        reset(world);
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
    public GameplayController(float width, float height, World world, LevelLoader level) {
        this(new Rectangle(0,0,width,height), world, level);
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

    public void reset(World world) {
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
     * Retrieves the path for each asset used in this file from the LevelLoader.
     * These paths have been specified in jsons/assets.json.
     */
    private void getAssetPaths() {
        PLAYER_FILE = levelLoader.getImagePath("player");
        HURT_FILE = levelLoader.getImagePath("player_hurt");
        FLIGHT_FOUNTAIN_FILE = levelLoader.getImagePath("flight_fountain");
        CASTLE_SPIKE_FILE = levelLoader.getImagePath("castle");
        ROCK_FILE1 = levelLoader.getImagePath("falling_rock1");
        ROCK_FILE2 = levelLoader.getImagePath("falling_rock2");
        ROCK_FILE3 = levelLoader.getImagePath("falling_rock3");
        ROCK_TILE_FILE = levelLoader.getImagePath("falling_rock_tile");
        CLOUD_FOUNTAIN_FILE = levelLoader.getImagePath("transparency_fountain");
        DASH_FOUNTAIN_FILE = levelLoader.getImagePath("dash_fountain");
        FLIGHT_FILE = levelLoader.getImagePath("player_flight");
        DASH_FILE = levelLoader.getImagePath("player_dash");
        TRANSP_FILE = levelLoader.getImagePath("player_transparency");
        SERENITY_FOUNTAIN_FILE = levelLoader.getImagePath("restore_fountain");
        DASH_ICON_FILE = levelLoader.getImagePath("dash_icon");
        TRANSPARENT_ICON_FILE = levelLoader.getImagePath("transparency_icon");
        FLIGHT_ICON_FILE = levelLoader.getImagePath("flight_icon");
        GOAL_FILE = levelLoader.getImagePath("goal");
        MOON_SHARD_FILE = levelLoader.getImagePath("moon_shard");
        CLOUD_FILE = levelLoader.getImagePath("cloud");
        CLOUD_LEFT_FILE = levelLoader.getImagePath("cloudlefttile");
        CLOUD_RIGHT_FILE = levelLoader.getImagePath("cloudrighttile");
        WINDOW_FILE = levelLoader.getImagePath("window");
        TILE_FILE = levelLoader.getImagePath("tile");
        BRICK_ENDTILE_FILE = levelLoader.getImagePath("brick_endtile");
        BRICK_ENDTILE2_FILE = levelLoader.getImagePath("brick_endtile2");
        BRICK_ENDTILE3_FILE = levelLoader.getImagePath("brick_endtile3");
        BRICK_ENDTILE4_FILE = levelLoader.getImagePath("brick_endtile4");
        CROC_FILE = levelLoader.getImagePath("enemy_croc");
        FLYING_FILE = levelLoader.getImagePath("enemy_fly");

        FONT_FILE = levelLoader.getFontPath("MarkerFelt");
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
        getAssetPaths();

        manager.load(PLAYER_FILE,Texture.class);
        assets.add(PLAYER_FILE);
        manager.load(HURT_FILE,Texture.class);
        assets.add(HURT_FILE);
        manager.load(DASH_FILE,Texture.class);
        assets.add(DASH_FILE);
        manager.load(FLIGHT_FOUNTAIN_FILE,Texture.class);
        assets.add(FLIGHT_FOUNTAIN_FILE);
        manager.load(SERENITY_FOUNTAIN_FILE,Texture.class);
        assets.add(SERENITY_FOUNTAIN_FILE);
        manager.load(FLIGHT_FILE,Texture.class);
        assets.add(FLIGHT_FILE);
        manager.load(TRANSP_FILE,Texture.class);
        assets.add(TRANSP_FILE);
        manager.load(DASH_FOUNTAIN_FILE,Texture.class);
        assets.add(DASH_FOUNTAIN_FILE);
        manager.load(DASH_ICON_FILE,Texture.class);
        assets.add(DASH_ICON_FILE);
        manager.load(CLOUD_FOUNTAIN_FILE,Texture.class);
        assets.add(CLOUD_FOUNTAIN_FILE);
        manager.load(CASTLE_SPIKE_FILE, Texture.class);
        assets.add(CASTLE_SPIKE_FILE);
        manager.load(TILE_FILE,Texture.class);
        assets.add(TILE_FILE);
        manager.load(BRICK_ENDTILE_FILE,Texture.class);
        assets.add(BRICK_ENDTILE_FILE);
        manager.load(BRICK_ENDTILE2_FILE,Texture.class);
        assets.add(BRICK_ENDTILE2_FILE);
        manager.load(BRICK_ENDTILE3_FILE,Texture.class);
        assets.add(BRICK_ENDTILE3_FILE);
        manager.load(BRICK_ENDTILE4_FILE,Texture.class);
        assets.add(BRICK_ENDTILE4_FILE);
        manager.load(ROCK_FILE1,Texture.class);
        assets.add(ROCK_FILE1);
        manager.load(ROCK_FILE2,Texture.class);
        assets.add(ROCK_FILE2);
        manager.load(ROCK_FILE3,Texture.class);
        assets.add(ROCK_FILE3);
        manager.load(ROCK_TILE_FILE,Texture.class);
        assets.add(ROCK_TILE_FILE);
        manager.load(WINDOW_FILE,Texture.class);
        assets.add(WINDOW_FILE);
        manager.load(TRANSPARENT_ICON_FILE,Texture.class);
        assets.add(TRANSPARENT_ICON_FILE);
        manager.load(FLIGHT_ICON_FILE,Texture.class);
        assets.add(FLIGHT_ICON_FILE);
        manager.load(GOAL_FILE,Texture.class);
        assets.add(GOAL_FILE);
        manager.load(CLOUD_FILE,Texture.class);
        assets.add(CLOUD_FILE);
        manager.load(CLOUD_LEFT_FILE,Texture.class);
        assets.add(CLOUD_LEFT_FILE);
        manager.load(CLOUD_RIGHT_FILE,Texture.class);
        assets.add(CLOUD_RIGHT_FILE);
        manager.load(MOON_SHARD_FILE,Texture.class);
        assets.add(MOON_SHARD_FILE);
        manager.load(CROC_FILE,Texture.class);
        assets.add(CROC_FILE);
        manager.load(FLYING_FILE,Texture.class);
        assets.add(FLYING_FILE);
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
        else if(player.isHit) {
            player.setTexture(hurtTexture);
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
    public List<MoonShard> getMSList() { return moonShardsList;}
    public List<Rock> getRockList() { return rocks;}
    public List<BoxObstacle> getPlatformsList() { return platforms;}


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
        hurtTexture = createTexture(manager, HURT_FILE, false);
        dashTexture = createTexture(manager,DASH_FILE, false);
        flightFountainTexture = createTexture(manager,FLIGHT_FOUNTAIN_FILE, false);
        flightTexture = createTexture(manager,FLIGHT_FILE, false);
        transpTexture = createTexture(manager,TRANSP_FILE, false);
        dashFountainTexture = createTexture(manager,DASH_FOUNTAIN_FILE, false);
        dashAbilityTexture = createTexture(manager,DASH_ICON_FILE, false);
        serenityFountainTexture = createTexture(manager,SERENITY_FOUNTAIN_FILE,false);
        transparencyAbilityTexture = createTexture(manager,TRANSPARENT_ICON_FILE, false);
        flightAbilityTexture = createTexture(manager,FLIGHT_ICON_FILE, false);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        castleSpikeTexture = createTexture(manager, CASTLE_SPIKE_FILE, false);
        tile = createTexture(manager,TILE_FILE,true);
        brick_endtile = createTexture(manager,BRICK_ENDTILE_FILE,true);
        brick_endtile2 = createTexture(manager,BRICK_ENDTILE2_FILE,true);
        brick_endtile3 = createTexture(manager,BRICK_ENDTILE3_FILE,true);
        brick_endtile4 = createTexture(manager,BRICK_ENDTILE4_FILE,true);
        rockTexture1 = createTexture(manager,ROCK_FILE1,true);
        rockTexture2 = createTexture(manager,ROCK_FILE2,true);
        rockTexture3 = createTexture(manager,ROCK_FILE3,true);
        rockTileTexture = createTexture(manager,ROCK_TILE_FILE,true);
        window = createTexture(manager,WINDOW_FILE,true);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        transparencyAbilityTexture = createTexture(manager,TRANSPARENT_ICON_FILE, false);
        flightAbilityTexture = createTexture(manager,FLIGHT_ICON_FILE, false);
        goalTile  = createTexture(manager,GOAL_FILE,true);
        cloudTile  = createTexture(manager,CLOUD_FILE,true);
        cloudLeftTile  = createTexture(manager,CLOUD_LEFT_FILE,true);
        cloudRightTile  = createTexture(manager,CLOUD_RIGHT_FILE,true);
        moonShardTexture = createTexture(manager,MOON_SHARD_FILE,true);
        crocTexture = createTexture(manager,CROC_FILE,true);
        flyingTexture = createTexture(manager,FLYING_FILE,true);
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
        if(fountain.isAvailable()) {
            abilityController.addAbility(fountain);
            fountain.setAvailable(false);
        }

    }
    /**
     * Lays out the game geography.
     */
    public void start() {
        levelLoader.populateLevel("jsons/levels2.json", 1);

        Sound s = SoundController.startLevelSound();
        s.play(0.8f);

        for (LevelLoader.Platform p : levelLoader.getPlatforms()) {
            if (p.type.equals("spike_castle")) {
                SpikedPlatform sp = new SpikedPlatform(p.x,p.y,p.width,p.height, new Vector2(1,1), new Vector2(0,0),
                        0f, 4f, p.direction);
                sp.setBodyType(BodyDef.BodyType.StaticBody);
                sp.setDensity(0.0f);
                sp.setFriction(.4f);
                sp.setRestitution(0.1f);
                sp.setSensor(false);
                sp.setDrawScale(scale);
                sp.setTexture(castleSpikeTexture);
                sp.setName(p.name);
                addObject(sp);
            } else if (p.type.equals("cloudlefttile")) {
                CloudPlatform cp = new CloudPlatform(p.x, p.y, p.width*2, p.height*1.5f, scale,
                        p.velocity, p.bounds.x, p.bounds.y);
                cp.setBodyType(BodyDef.BodyType.StaticBody);
                cp.setDensity(BASIC_DENSITY);
                cp.setFriction(BASIC_FRICTION);
                cp.setRestitution(BASIC_RESTITUTION);
                cp.setSensor(false);
                cp.setDrawScale(scale);
                cp.setTexture(cloudTile);
                cp.setName(p.name);
                addObject(cp);
                movingPlatforms.add(cp);
            } else if (p.type.equals("cloudrighttile")) {
//                    CloudPlatform cp = new CloudPlatform(p.x, p.y, p.width, p.height, scale,
//                            p.velocity, p.bounds.x, p.bounds.y);
//                    cp.setBodyType(BodyDef.BodyType.StaticBody);
//                    cp.setDensity(BASIC_DENSITY);
//                    cp.setFriction(BASIC_FRICTION);
//                    cp.setRestitution(BASIC_RESTITUTION);
//                    cp.setSensor(false);
//                    cp.setDrawScale(scale);
//                    cp.setTexture(cloudRightTile);
//                    cp.setName(p.name);
//                    addObject(cp);
//                    movingPlatforms.add(cp);
            } else { // tile
                Obstacle obj;
                if (p.type.equals("tile")) {
                    float[] points = {p.points.get(0), p.points.get(1), p.points.get(2), p.points.get(3), p.points.get(4),
                            p.points.get(5), p.points.get(6), p.points.get(7)};
                    obj = new PolygonObstacle(points);

                } else {
                    obj = new BoxObstacle(p.points.get(0) + p.width/2.0f, p.points.get(1) + p.height/2.0f, p.width+10, p.height);
                }
                obj.setBodyType(BodyDef.BodyType.StaticBody);
                obj.setDensity(BASIC_DENSITY);
                obj.setFriction(BASIC_FRICTION);
                obj.setRestitution(BASIC_RESTITUTION);
                obj.setDrawScale(scale);
                obj.setName(p.name);
                if (p.type.equals("tile")) {
                    obj.setTexture(tile);
                } else if (p.type.equals("brick_endtile")) {
                    obj.setTexture(brick_endtile);
                } else if (p.type.equals("brick_endtile2")) {
                    obj.setTexture(brick_endtile2);
                } else if (p.type.equals("brick_endtile3")) {
                    obj.setTexture(brick_endtile3);;
                } else if (p.type.equals("window")) {
                    obj.setTexture(window);
                } else {
                    obj.setTexture(brick_endtile4);
                }
                addObject(obj);
                //platforms.add(obj);
            }
        }
        for(LevelLoader.Obstacle o : levelLoader.getObstacles()) {
            if(o.type.equals("falling_rock_tile")) {
                float[] points = {o.points.get(0), o.points.get(1), o.points.get(2), o.points.get(3), o.points.get(4),
                        o.points.get(5), o.points.get(6), o.points.get(7)};
                BoxObstacle obj = new BoxObstacle(o.points.get(0) + o.width/2.0f, o.points.get(1) + o.height/2.0f, o.width, o.height);
                obj.setBodyType(BodyDef.BodyType.StaticBody);
                obj.setDensity(BASIC_DENSITY);
                obj.setFriction(BASIC_FRICTION);
                obj.setRestitution(BASIC_RESTITUTION);
                obj.setDrawScale(scale);
                obj.setName(o.name);
                obj.setTexture(rockTileTexture);
                addObject(obj);
                initRock(o);
            }
            else if(o.type.equals("croc_enemy")) {
                Crocodile cr = new Crocodile(o.x,o.y,o.width,o.height,scale,o.velocity,o.bounds.x,o.bounds.y);
                cr.setBodyType(BodyDef.BodyType.StaticBody);
                cr.setDensity(BASIC_DENSITY);
                cr.setFriction(BASIC_FRICTION);
                cr.setRestitution(BASIC_RESTITUTION);
                cr.setSensor(false);
                cr.setDrawScale(scale);
                cr.setTexture(crocTexture);
                cr.setName(o.name);
                addObject(cr);
                crocodiles.add(cr);
            }
            else if(o.type.equals("fly_enemy")) {
                FlyingMonster fl = new FlyingMonster(o.x,o.y,o.width,o.height,scale,o.velocity,o.bounds.x,o.bounds.y);
                fl.setBodyType(BodyDef.BodyType.StaticBody);
                fl.setDensity(BASIC_DENSITY);
                fl.setFriction(BASIC_FRICTION);
                fl.setRestitution(BASIC_RESTITUTION);
                fl.setSensor(false);
                fl.setDrawScale(scale);
                fl.setTexture(flyingTexture);
                fl.setName(o.name);
                addObject(fl);
                flyingMonsters.add(fl);
            }
        }

        // Add level goal
        float gwidth  = goalTile.getRegionWidth()/scale.x;
        float gheight = goalTile.getRegionHeight()/scale.y;
        goalDoor = new BoxObstacle(levelLoader.getExitPos().x,levelLoader.getExitPos().y, gwidth, gheight-10);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(BASIC_DENSITY);
        goalDoor.setFriction(BASIC_FRICTION);
        goalDoor.setRestitution(BASIC_RESTITUTION);
        goalDoor.setSensor(true);
        goalDoor.setTexture(goalTile);
        goalDoor.setDrawScale(scale);
        goalDoor.setName("goal");
        addObject(goalDoor);

        for (LevelLoader.Fountain f : levelLoader.getFountains()) {
            FountainModel fountain = new FountainModel(f.x, f.y, f.type);
            fountain.setDrawScale(scale);
            fountain.setName(f.name);
            if (f.type == FountainModel.FountainType.DASH) {
                fountain.setTexture(dashFountainTexture);
            } else if (f.type == FountainModel.FountainType.FLIGHT) {
                fountain.setTexture(flightFountainTexture);
            } else if(f.type == FountainModel.FountainType.TRANSPARENCY) {
                fountain.setTexture(cloudFountainTexture);
            }
            addObject(fountain);
            fountainsList.add(fountain);
        }

        for (Vector2 pos : levelLoader.getCheckpoints()) {
            FountainModel fountain = new FountainModel(pos.x, pos.y, FountainModel.FountainType.RESTORE);
            fountain.setDrawScale(scale);
            fountain.setName("checkpoint");
            fountain.setTexture(serenityFountainTexture);
            addObject(fountain);
            fountainsList.add(fountain);
        }

        for (LevelLoader.Shard shard : levelLoader.getMoonShards()) {
            MoonShard ms = new MoonShard(shard.x, shard.y, shard.velocity, shard.bounds.x, shard.bounds.y);
            ms.setBodyType(BodyDef.BodyType.StaticBody);
            ms.setDensity(0.0f);
            ms.setFriction(.4f);
            ms.setRestitution(0.1f);
            ms.setSensor(false);
            ms.setDrawScale(scale);
            ms.setTexture(moonShardTexture);
            ms.setName(shard.name);
            addObject(ms);
            moonShardsList.add(ms);
        }

        float dwidth = playerTexture.getRegionWidth() *2 / scale.x;
        float dheight = playerTexture.getRegionHeight() * 2/ scale.y;
        player = new PlayerModel(levelLoader.getEntrancePos().x, levelLoader.getEntrancePos().y, dwidth, dheight);
        player.setTexture(playerTexture);
        player.setDrawScale(scale);
        addObject(player);
        avatar = player;
        serenity = levelLoader.getMaxSerenity();

        abilityController = AbilityController.getInstance();
        abilityController.setDashTexture(dashAbilityTexture);
        abilityController.setFlightTexture(flightAbilityTexture);
        abilityController.setTransparentTexture(transparencyAbilityTexture);
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

    public void initRock(LevelLoader.Obstacle o) {
        //TODO: fix radius to make detection better
        Rock rk = new Rock(o.x+30,o.y,rockTexture1.getRegionWidth()/(4.0f*scale.x),ROCK_VELOCITY,new Vector2(1,1));
        rk.setBodyType(BodyDef.BodyType.StaticBody);
        rk.setDensity(BASIC_DENSITY);
        rk.setFriction(BASIC_FRICTION);
        rk.setRestitution(BASIC_RESTITUTION);
        rk.setSensor(false);
        rk.setDrawScale(scale);
        rk.setTexture(rockTexture1);
//                if(o.type.equals("falling_rock1")) {
//                    rk.setTexture(rockTexture1);
//                }
//                else if(o.type.equals("falling_rock2")) {
//                    rk.setTexture(rockTexture2);
//                }
//                else if(o.type.equals("falling_rock3")) {
//                    rk.setTexture(rockTexture3);
//                }
        rk.setName("rock");
        addObject(rk);
        rocks.add(rk);
    }

    public void rockFall(Rock rk) {
        Vector2 newPos = rk.getPosition();
        newPos.add(rk.getVelocity());
        rk.setPosition(newPos);
    }

    public void resetRock(Rock rk) {
        rk.setPosition(rk.getOriginalPos());
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

        for(Crocodile croc : crocodiles){
            Vector2 newPos = croc.getPosition();
            newPos.add(croc.getVelocity());
            if(Math.abs(newPos.x - croc.getOriginalPosition().x) > croc.getHorizontalRadius()) {
                croc.setVelocity(croc.getVelocity().scl(-1, 1));
                //System.out.println("hi");
            }
            if(newPos.x - croc.getOriginalPosition().x > croc.getHorizontalRadius()) {
                croc.setFaceRight(false);
            }
            if(croc.getOriginalPosition().x - newPos.x  > croc.getHorizontalRadius()) {
                croc.setFaceRight(true);
            }
            if(Math.abs(newPos.y - croc.getOriginalPosition().y) > croc.getVerticalRadius()) {
                croc.setVelocity(croc.getVelocity().scl(1, -1));
            }
            croc.setPosition(newPos);
        }

        for(FlyingMonster fly : flyingMonsters){
            Vector2 newPos = fly.getPosition();
            newPos.add(fly.getVelocity());
            if(Math.abs(newPos.x - fly.getOriginalPosition().x) > fly.getHorizontalRadius())
                fly.setVelocity(fly.getVelocity().scl(-1, 1));
            if(newPos.x - fly.getOriginalPosition().x > fly.getHorizontalRadius()) {
                fly.setFaceRight(false);
            }
            if(fly.getOriginalPosition().x - newPos.x  > fly.getHorizontalRadius()) {
                fly.setFaceRight(true);
            }
            if(Math.abs(newPos.y - fly.getOriginalPosition().y) > fly.getVerticalRadius())
                fly.setVelocity(fly.getVelocity().scl(1,-1));
            fly.setPosition(newPos);
        }

        //moving the rocks
        //TODO make come from the rock launcher
        for(Rock rk : rocks) {
            rockFall(rk);
            //System.out.println(rk.getY());

            if (rk.getY() == 200) {
                resetRock(rk);
            }
        }

//        boolean horiz = (bounds.x+30 <= player.getX() && player.getX() <= bounds.x+bounds.width -30);
//        if (!horiz) {
//            player.setVX(-player.getVX());
//        }
//        if (player.getY() > bounds.y+bounds.height-30) {
//            player.setVY(-player.getVY());
//        }

        for (Obstacle object : objects) {
            object.update(dt);
        }
    }

    public void draw(GameCanvas canvas) {
        abilityController.drawQueue(canvas);
    }

}