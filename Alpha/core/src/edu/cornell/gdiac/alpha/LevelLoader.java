package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.alpha.objects.*;
import edu.cornell.gdiac.alpha.obstacle.*;
import edu.cornell.gdiac.alpha.platform.FountainModel;
import edu.cornell.gdiac.alpha.platform.PlayerModel;
import edu.cornell.gdiac.alpha.platform.PlayerModel.playerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that loads a level using JSON.
 */
public class LevelLoader {

    private static HashMap<String, String> images = new HashMap<String, String>();
    private static HashMap<String, String> filmStrips = new HashMap<String, String>();
    private static HashMap<String, String> fonts = new HashMap<String, String>();
    private static HashMap<String, String> sounds = new HashMap<String, String>();
    private static HashMap<String, String> music = new HashMap<String, String>();

    /** The density for most alpha objects */
    private static final float  BASIC_DENSITY = 0.0f;
    /** Friction of most platforms */
    private static final float  BASIC_FRICTION = 0.4f;
    /** The restitution for all alpha objects */
    private static final float  BASIC_RESTITUTION = 0.1f;

    /** Current level */
    public Level level;

    // GETTERS ----------------------------------------------------------------------------

    /** Returns exit position (where to place stairs) */
    public GoalDoor getGoalDoor() {
        return level.goalDoor;
    }

    /** Returns player (Amaris) with the entrance position */
    public PlayerModel getPlayer() {
        return level.player;
    }

    /** Returns max serenity for this level */
    public int getMaxSerenity() { return level.maxSerenity; }

    /** Returns the current serenity for this level */
    //TODO: UNCOMMENT AND FIX CURRENT SERENITY IN LEVEL FILES
    public int getCurrentSerenity() { return level.currentSerenity; }

    /** Returns list of fountains with position, type, etc. */
    public List<FountainModel> getFountains() {
        return level.fountains;
    }

    /** Returns list of platforms with position, width, height, type, direction, velocity, bounds, etc. */
    public List<Obstacle> getPlatforms() {
        return level.platforms;
    }

    /** Returns list of moon shards with their positions */
    public List<MoonShard> getMoonShards() {
        return level.shards;
    }

    /** Returns list of obstacles with position, width, height, type, etc. */
    public List<Obstacle> getObstacles() {
        return level.obstacles;
    }

    /** Returns list of checkpoints with their positions */
    public List<FountainModel> getCheckpoints() {
        return level.checkpoints;
    }

    /** Returns directory path to image file */
    public String getImagePath(String name) {
        return images.get(name);
    }

    /** Returns directory path to film strip file */
    public String getFilmStrip(String name) {
        return filmStrips.get(name);
    }

    /** Returns directory path to font file */
    public String getFontPath(String name) {
        return fonts.get(name);
    }

    /** Returns directory path to sound file */
    public static String getSoundPath(String name) {
        return sounds.get(name);
    }

    /** Returns directory path to music file */
    public static String getMusicPath(String name) {
        return music.get(name);
    }

    // POPULATING METHODS ------------------------------------------------------------------

    /** Creates the player for this level with the entrance position. */
    private void createPlayer(JsonValue levelJson, Vector2 scale) {
        JsonValue entrance = levelJson.get("entrance");
        float dwidth = playerTexture.getRegionWidth() / scale.x;
        float dheight = playerTexture.getRegionHeight() / scale.y;
        float x = entrance.getFloat("x")/scale.x;
        float y = entrance.getFloat("y")/scale.y;
        level.player = new PlayerModel(x, y, dwidth, dheight);
        level.player.setTexture(playerTexture);
        level.player.setDrawScale(scale);
    }

    /** Creates the goal door for this level with the exit position. */
    private void createGoalDoor(JsonValue levelJson, Vector2 scale) {
        JsonValue exit = levelJson.get("exit");
        float x = exit.getFloat("x")/scale.x;
        float y = exit.getFloat("y")/scale.y;

        level.goalDoor = new GoalDoor(x, y, 250/scale.x, 250/scale.y, scale);
        level.goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        level.goalDoor.setDensity(0.0f);
        level.goalDoor.setFriction(0.0f);
        level.goalDoor.setRestitution(0.0f);
        level.goalDoor.setSensor(true);
        level.goalDoor.setDrawScale(scale);
        level.goalDoor.setTexture(goalTile);
        level.goalDoor.setName("goal");

        if (exit.has("flipped")) {
            level.goalDoor.setFlipped(exit.getBoolean("flipped"));
        } else {
            level.goalDoor.setFlipped(false);
        }
    }

    /** Populates the fountains in this level with position, type, etc. */
    private void populateFountains(JsonValue levelJson, Vector2 scale) {
        level.fountains = new ArrayList<FountainModel>();
        JsonValue fountains = levelJson.get("fountains");
        if (fountains != null) {
            for (JsonValue entry = fountains.child; entry != null; entry = entry.next) {
                float x = entry.getFloat("x")/scale.x;
                float y = entry.getFloat("y")/scale.y;
                FountainModel.FountainType type;
                TextureRegion texture;
                TextureRegion icon;
                if (entry.getString("type").equals("dash")) {
                    type = FountainModel.FountainType.DASH;
                    texture = dashFountainTexture;
                    icon = dashAbilityTexture;
                } else if (entry.getString("type").equals("flight"))  {
                    type = FountainModel.FountainType.FLIGHT;
                    texture = flightFountainTexture;
                    icon = flightAbilityTexture;
                } else if (entry.getString("type").equals("restore"))  {
                    type = FountainModel.FountainType.RESTORE;
                    texture = serenityFountainTexture;
                    icon = serenityTexture;
                } else {
                    type = FountainModel.FountainType.TRANSPARENCY;
                    texture = cloudFountainTexture;
                    icon = transparencyAbilityTexture;
                }
                FountainModel fountain = new FountainModel(x, y, 3, 3, type);
                fountain.setName("fountain" + entry.getString("id"));
                fountain.setTexture(texture);
                fountain.setDrawScale(scale);
                fountain.setIcon(icon);
                level.fountains.add(fountain);
            }
        }
    }

    /** Populates the platforms in this level with position, width, height, type, direction, velocity, bounds, etc. */
    private void populatePlatforms(JsonValue levelJson, Vector2 scale) {
        level.platforms = new ArrayList<Obstacle>();
        JsonValue platforms = levelJson.get("platforms");
        if (platforms != null) {
            for (JsonValue entry = platforms.child; entry != null; entry = entry.next) {
                String type =  entry.getString("type");
                float x = entry.getFloat("x")/scale.x;
                float y = entry.getFloat("y")/scale.y;
                float width = entry.getFloat("width")/scale.x;
                float height = entry.getFloat("height")/scale.y;
                String name = "platform" + entry.getString("id");
                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity")/scale.x;
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity")/scale.y;
                Vector2 velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds")/scale.x;
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds")/scale.y;
                Vector2 bounds = new Vector2(xBound, yBound);

                SpikedPlatform.SpikeDirection direction = SpikedPlatform.SpikeDirection.RIGHT;
                if (entry.has("direction")) {
                    if (entry.getString("direction").equals("UP")) {
                        direction = SpikedPlatform.SpikeDirection.UP;
                    } else if (entry.getString("direction").equals("DOWN")) {
                        direction = SpikedPlatform.SpikeDirection.DOWN;
                    } else if (entry.getString("direction").equals("LEFT")) {
                        direction = SpikedPlatform.SpikeDirection.LEFT;
                    } else {
                        direction = SpikedPlatform.SpikeDirection.RIGHT;
                    }
                }
                if (type.equals("spike_castle")) {
                    SpikedPlatform sp = new SpikedPlatform(x,y,width,height, scale, new Vector2(0,0),
                            0f, 4f/scale.x, direction);
                    sp.setBodyType(BodyDef.BodyType.StaticBody);
                    sp.setDensity(BASIC_DENSITY);
                    sp.setFriction(0);
                    sp.setRestitution(BASIC_RESTITUTION);
                    sp.setSensor(false);
                    sp.setDrawScale(scale);
                    sp.setName(name);
                    sp.setTexture(castleSpikeTexture);
                    level.platforms.add(sp);
                } else if (type.equals("cloudlefttile")) {
                    CloudPlatform cp = new CloudPlatform(x, y, width*2, height, scale,
                            velocity, bounds.x, bounds.y);
                    cp.setBodyType(BodyDef.BodyType.StaticBody);
                    cp.setDensity(BASIC_DENSITY);
                    cp.setFriction(0);
                    cp.setRestitution(BASIC_RESTITUTION);
                    cp.setSensor(false);
                    cp.setDrawScale(scale);
                    cp.setName(name);
                    cp.setTexture(cloudTile);
                    level.platforms.add(cp);
                } else if (type.equals("cloudrighttile")) {
                    // nothing
                } else { // tile
                    Obstacle obj;
                    obj = new RegularPlatform(x + width/2.0f, y + height/2.0f, width, height, scale, velocity,
                            bounds.x, bounds.y);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(BASIC_DENSITY);
                    obj.setFriction(0);
                    obj.setRestitution(BASIC_RESTITUTION);
                    obj.setDrawScale(scale);
                    obj.setName(name);
                    if (type.equals("tile")) {
                        obj.setTexture(tile);
                    } else if (type.equals("brick_endtile")) {
                        obj.setTexture(brick_endtile);
                    } else if (type.equals("brick_endtile2")) {
                        obj.setTexture(brick_endtile2);
                    } else if (type.equals("brick_endtile3")) {
                        obj.setTexture(brick_endtile3);;
                    } else if (type.equals("window")) {
                        obj.setTexture(window);
                    } else {
                        obj.setTexture(brick_endtile4);
                    }
                    level.platforms.add(obj);
                }
            }
        }
    }

    /** Populates the moon shards in this level with their position */
    private void populateMoonShards(JsonValue levelJson, Vector2 scale) {
        level.shards = new ArrayList<MoonShard>();
        JsonValue shards = levelJson.get("moon-shards");
        if (shards != null) {
            for (JsonValue entry = shards.child; entry != null; entry = entry.next) {
                float x = entry.getFloat("x")/scale.x;
                float y = entry.getFloat("y")/scale.y;
                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity")/scale.x;
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity")/scale.y;
                Vector2 velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds")/scale.x;
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds")/scale.y;
                Vector2 bounds = new Vector2(xBound, yBound);

                String name = "shard" + entry.getString("id");

                MoonShard ms = new MoonShard(x, y, velocity, bounds.x, bounds.y, scale);
                ms.setBodyType(BodyDef.BodyType.StaticBody);
                ms.setDensity(0);
                ms.setFriction(0);
                ms.setRestitution(0);
                ms.setSensor(true);
                ms.setDrawScale(scale);
                ms.setTexture(moonShardTexture);
                ms.setName(name);

                level.shards.add(ms);
            }
        }
    }

    /** Populates the text for tutorial levels */
    private void populateText(JsonValue levelJson, Vector2 scale) {
        level.messages = new Array<Level.Message>();
        if (levelJson.has("messages")) {
            JsonValue msgs = levelJson.get("messages");
            if (msgs != null) {
                for (JsonValue entry = msgs.child; entry != null; entry = entry.next) {
                    float x = entry.getFloat("x")/scale.x;
                    float y = entry.getFloat("y")/scale.y;
                    String text = entry.getString("text");
                    Level.Message m = new Level.Message();
                    m.text = text;
                    m.x = x;
                    m.y = y;
                    level.messages.add(m);
                }
            }
        }
    }


    public Rock initRock(float x, float y, String type, Vector2 scale) {
        //TODO: fix radius to make detection better
        Vector2 velocity = new Vector2(0, -5f / scale.y);
        Rock rk = new Rock(x + 1, y, rockTexture1.getRegionWidth() / (9.0f * scale.x), velocity, scale, type);
        //rk.setBodyType(BodyDef.BodyType.StaticBody);
        rk.setDensity(BASIC_DENSITY);
        rk.setFriction(BASIC_FRICTION);
        rk.setRestitution(BASIC_RESTITUTION);
        rk.setSensor(false);
        rk.setDrawScale(scale);
        rk.setTexture(rockTexture1);
        if (type.equals("falling_rock1")) {
            rk.setTexture(rockTexture1);
        } else if (type.equals("falling_rock2")) {
            rk.setTexture(rockTexture2);
        } else if (type.equals("falling_rock3")) {
            rk.setTexture(rockTexture3);
        }
        rk.setName("rock");
        level.obstacles.add(rk);
        return rk;
    }

    /** Populates the obstacles in this level with position, width, height, type, etc. */
    private void populateObstacles(JsonValue levelJson, Vector2 scale) {
        level.obstacles = new ArrayList<Obstacle>();
        JsonValue obstacles = levelJson.get("obstacles");
        if (obstacles != null) {
            for (JsonValue entry = obstacles.child; entry != null; entry = entry.next) {
                float x = entry.getFloat("x")/scale.x;
                float y = entry.getFloat("y")/scale.y;
                float width = entry.getFloat("width")/scale.x;
                float height = entry.getFloat("height")/scale.y;
                String name = entry.getString("type") + entry.getString("id");
                String type = entry.getString("type");

                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity")/scale.x;
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity")/scale.y;
                Vector2 velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds")/scale.x;
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds")/scale.y;
                Vector2 bounds = new Vector2(xBound, yBound);
                if(type.equals("falling_rock_tile")) {
                    RegularPlatform obj = new RegularPlatform(x + width/2.0f, y + height/2.0f, width, height,
                            scale, velocity, bounds.x, bounds.y);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(BASIC_DENSITY);
                    obj.setFriction(0);
                    obj.setRestitution(BASIC_RESTITUTION);
                    obj.setDrawScale(scale);
                    obj.setName(name);
                    obj.setTexture(rockTileTexture);
                    initRock(x, y, type, scale);
                    level.obstacles.add(obj);
                } else if(type.equals("enemy_croc")) {
                    Crocodile cr = new Crocodile(x,y,width,height,scale,velocity,bounds.x,bounds.y);
                    cr.setBodyType(BodyDef.BodyType.StaticBody);
                    cr.setDensity(BASIC_DENSITY);
                    cr.setFriction(0);
                    cr.setRestitution(BASIC_RESTITUTION);
                    cr.setSensor(false);
                    cr.setDrawScale(scale);
                    cr.setTexture(crocTexture);
                    cr.setName(name);
                    level.obstacles.add(cr);
                } else if(type.equals("enemy_flying")) {
                    FlyingMonster fl = new FlyingMonster(x,y,width,height,scale,velocity,bounds.x,bounds.y);
                    fl.setBodyType(BodyDef.BodyType.StaticBody);
                    fl.setDensity(BASIC_DENSITY);
                    fl.setFriction(0);
                    fl.setRestitution(BASIC_RESTITUTION);
                    fl.setSensor(false);
                    fl.setDrawScale(scale);
                    fl.setTexture(flyingTexture);
                    fl.setName(name);
                    level.obstacles.add(fl);
                }
            }
        }
    }

    /** Populates the checkpoints in this level with their position */
    private void populateCheckpoints(JsonValue levelJson, Vector2 scale) {
        level.checkpoints = new ArrayList<FountainModel>();
        JsonValue checkpoints = levelJson.get("checkpoints");
        if (checkpoints != null) {
            for (JsonValue entry = checkpoints.child; entry != null; entry = entry.next) {
                float x = entry.getFloat("x")/scale.x;
                float y = entry.getFloat("y")/scale.y;
                FountainModel fountain = new FountainModel(x, y, 3, 3, FountainModel.FountainType.RESTORE);
                fountain.setDrawScale(scale);
                fountain.setName("checkpoint");
                fountain.setTexture(serenityFountainTexture);
                fountain.setIcon(serenityTexture);
                level.checkpoints.add(fountain);
            }
        }
    }

    /** Populates the box2d border for the level */
    private void populateBorder(Vector2 scale) {
        Obstacle obj;
        obj = new RegularPlatform(-1/scale.x, -200/scale.y, 10/scale.x, 3500/scale.y, scale, new Vector2(0, 0),
                0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(0);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setName("border");
        level.platforms.add(obj);

        obj = new RegularPlatform(level.width/scale.x, -200/scale.y, 10/scale.x, 3500/scale.y, scale, new Vector2(0, 0),
                0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(0);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setName("border");
        level.platforms.add(obj);

        obj = new RegularPlatform(-200/scale.x, 1090/scale.y, 4500/scale.x, 10/scale.y, scale, new Vector2(0, 0),
                0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(0);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setName("border");
        level.platforms.add(obj);
    }

    /** Populates the specific level with the info provided in the JSON file `file` */
    public void populateLevel(String file, Vector2 scale) {
        JsonReader jsonReader = new JsonReader();
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal(file));

        JsonValue currentLevel = levelFormat.get(level.name);

        if (currentLevel.has("max_width")) {
            level.width = currentLevel.getFloat("max_width");
        } else {
            level.width = 1920;
        }

        createPlayer(currentLevel, scale);
        createGoalDoor(currentLevel, scale);
        populateFountains(currentLevel, scale);
        populatePlatforms(currentLevel, scale);
        populateMoonShards(currentLevel, scale);
        populateObstacles(currentLevel, scale);
        populateCheckpoints(currentLevel, scale);
        populateText(currentLevel, scale);
        populateBorder(scale);

        level.maxSerenity = currentLevel.getInt("max_serenity");
    }

    /** Populates the assets of the game with the info provided in the JSON file `file` */
    public static void populateAssets(String file) {
        JsonReader jsonReader = new JsonReader();
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal(file));

        // Images
        JsonValue imagesJson = levelFormat.get("static-images");
        if (imagesJson != null) {
            for (JsonValue entry = imagesJson.child; entry != null; entry = entry.next) {
                images.put(entry.getString(0), entry.getString("path"));
            }
        }

        // Film Strips
        JsonValue filmStripsJson = levelFormat.get("film-strips");
        if (filmStripsJson != null) {
            for (JsonValue entry = filmStripsJson.child; entry != null; entry = entry.next) {
                filmStrips.put(entry.getString(0), entry.getString("path"));
            }
        }

        // Fonts
        JsonValue fontsJson = levelFormat.get("fonts");
        if (fontsJson != null) {
            for (JsonValue entry = fontsJson.child; entry != null; entry = entry.next) {
                fonts.put(entry.getString(0), entry.getString("path"));
            }
        }

        // Sounds
        JsonValue soundsJson = levelFormat.get("sounds");
        if (soundsJson != null) {
            for (JsonValue entry = soundsJson.child; entry != null; entry = entry.next) {
                sounds.put(entry.getString(0), entry.getString("path"));
            }
        }

        // Music
        JsonValue musicJson = levelFormat.get("music");
        if (musicJson != null) {
            for (JsonValue entry = musicJson.child; entry != null; entry = entry.next) {
                music.put(entry.getString(0), entry.getString("path"));
            }
        }
    }

    // LOADING ASSETS ------------------------------------------------------------------

    /** The texture files for the player */
    private static String PLAYER_FILE;
    private static String PLAYER_WALK_FILE;
    private static String PLAYER_TRANS_WALK_FILE;
    private static String PLAYER_FLIGHT_WALK_FILE;
    private static String PLAYER_DASH_WALK_FILE;
    private static String FLIGHT_FILE;
    private static String DASH_FILE;
    private static String TRANSP_FILE;
    private static String HURT_FILE;
    private static String FLYING_AMARIS_FILE;
    /** Textures for the player */
    private TextureRegion playerWalkTexture;
    private TextureRegion playerTransWalkTexture;
    private TextureRegion playerFlightWalkTexture;
    private TextureRegion playerDashWalkTexture;
    private TextureRegion playerTexture;
    private TextureRegion dashTexture;
    private TextureRegion flightTexture;
    private TextureRegion transpTexture;
    private TextureRegion hurtTexture;
    private TextureRegion flyingPlayerTexture;

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

    /** The texture file for a flight fountain */
    private static String FLIGHT_FOUNTAIN_FILE;
    /** Texture for the flight fountain */
    private TextureRegion flightFountainTexture;
    /** The texture file for a flight ability icon  */
    private static String FLIGHT_ICON_FILE;
    /** Texture for the flight ability card */
    private TextureRegion flightAbilityTexture;

    /** The texture file for a cloud fountain */
    private static String SERENITY_FOUNTAIN_FILE;
    /** Texture for the cloud fountain */
    private TextureRegion serenityFountainTexture;
    /** The texture file for a serenity icon  */
    private static String SERENITY_ICON_FILE;
    /** Texture for the serenity card */
    private TextureRegion serenityTexture;

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

    /** File to texture for the moon shard */
    private static String MOON_SHARD_FILE;
    /** The texture for the moon shard */
    protected TextureRegion moonShardTexture;
    /** File to texture for the win door */
    private static String GOAL_FILE = "images/stairs.png";
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    /** File to texture for cloud platforms */
    private static String CLOUD_FILE = "images/cloud_platform.png";
    /** The texture for walls and platforms */
    protected TextureRegion cloudTile;
    /** File to texture for windows */
    private static String WINDOW_FILE = "images/window.png";
    /** The texture for walls and platforms */
    protected TextureRegion window;

    /** The spike platform - castle spike platform*/
    private static String CASTLE_SPIKE_FILE;
    /** Texture for the castle spike platform */
    private TextureRegion castleSpikeTexture;

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

    /** Sets player texture depending on which ability is active and the action*/
    public void setPlayerTextures(AbilityController abilityController) {
        if(level.player.isHurt) {
            level.player.setTexture(hurtTexture);
        }
        else if(abilityController.isAbilityActive(FountainModel.FountainType.DASH)) {
            if (level.player.isWalking()) {
                level.player.setTexture(playerDashWalkTexture);
            }
            else {
                level.player.setTexture(dashTexture);
            }
        }
        else if(abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)) {
            if (level.player.isWalking()) {
                level.player.setTexture(playerFlightWalkTexture);
            }
            else if (!level.player.isGrounded()) {
                level.player.setTexture(flyingPlayerTexture);
            }
            else {
                level.player.setTexture(flightTexture);
            }
        }
        else if(abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY)) {
            level.player.setTransparent(true);
            if (level.player.isWalking()) {
                level.player.setTexture(playerTransWalkTexture);
            }
            else {
                level.player.setTexture(transpTexture);
            }

        }
        else if (level.player.isWalking()) {
            level.player.setTexture(playerWalkTexture);
        }
        else {
            level.player.setTexture(playerTexture);
        }
    }

    /** Sets ability textures for queue */
    public void setAbilityTextures(AbilityController abilityController) {
        abilityController.setDashTexture(dashAbilityTexture);
        abilityController.setFlightTexture(flightAbilityTexture);
        abilityController.setTransparentTexture(transparencyAbilityTexture);
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
     * Retrieves the path for each asset used in this file from the LevelLoader.
     * These paths have been specified in jsons/assets.json.
     */
    private void getAssetPaths() {
        PLAYER_FILE = getImagePath("player");
        HURT_FILE = getImagePath("player_hurt");
        PLAYER_WALK_FILE = getImagePath("player_walk");
        PLAYER_FLIGHT_WALK_FILE = getImagePath("player_flight_walk");
        PLAYER_TRANS_WALK_FILE = getImagePath("player_trans_walk");
        PLAYER_DASH_WALK_FILE = getImagePath("player_dash_walk");
        FLYING_AMARIS_FILE = getImagePath("player_flying");
        FLIGHT_FOUNTAIN_FILE = getImagePath("flight_fountain");
        CASTLE_SPIKE_FILE = getImagePath("castle");
        ROCK_FILE1 = getImagePath("falling_rock1");
        ROCK_FILE2 = getImagePath("falling_rock2");
        ROCK_FILE3 = getImagePath("falling_rock3");
        ROCK_TILE_FILE = getImagePath("falling_rock_tile");
        CLOUD_FOUNTAIN_FILE = getImagePath("transparency_fountain");
        DASH_FOUNTAIN_FILE = getImagePath("dash_fountain");
        FLIGHT_FILE = getImagePath("player_flight");
        DASH_FILE = getImagePath("player_dash");
        TRANSP_FILE = getImagePath("player_transparency");
        SERENITY_FOUNTAIN_FILE = getImagePath("restore_fountain");
        DASH_ICON_FILE = getImagePath("dash_icon");
        TRANSPARENT_ICON_FILE = getImagePath("transparency_icon");
        FLIGHT_ICON_FILE = getImagePath("flight_icon");
        SERENITY_ICON_FILE = getImagePath("serenity_icon");
        GOAL_FILE = getImagePath("goal");
        MOON_SHARD_FILE = getImagePath("moon_shard");
        CLOUD_FILE = getImagePath("cloud");
        WINDOW_FILE = getImagePath("window");
        TILE_FILE = getImagePath("tile");
        BRICK_ENDTILE_FILE = getImagePath("brick_endtile");
        BRICK_ENDTILE2_FILE = getImagePath("brick_endtile2");
        BRICK_ENDTILE3_FILE = getImagePath("brick_endtile3");
        BRICK_ENDTILE4_FILE = getImagePath("brick_endtile4");
        CROC_FILE = getImagePath("enemy_croc");
        FLYING_FILE = getImagePath("enemy_fly");
    }

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
    public void preLoadContent(AssetManager manager, Array<String> assets) {
        populateAssets("jsons/assets.json");
        getAssetPaths();

        manager.load(PLAYER_FILE, Texture.class);
        assets.add(PLAYER_FILE);
        manager.load(HURT_FILE,Texture.class);
        assets.add(HURT_FILE);
        manager.load(PLAYER_WALK_FILE, Texture.class);
        assets.add(PLAYER_WALK_FILE);
        manager.load(PLAYER_FLIGHT_WALK_FILE, Texture.class);
        assets.add(PLAYER_FLIGHT_WALK_FILE);
        manager.load(PLAYER_TRANS_WALK_FILE, Texture.class);
        assets.add(PLAYER_TRANS_WALK_FILE);
        manager.load(PLAYER_DASH_WALK_FILE, Texture.class);
        assets.add(PLAYER_DASH_WALK_FILE);
        manager.load(FLYING_AMARIS_FILE, Texture.class);
        assets.add(FLYING_AMARIS_FILE);
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
        manager.load(SERENITY_ICON_FILE,Texture.class);
        assets.add(SERENITY_ICON_FILE);
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
        manager.load(MOON_SHARD_FILE,Texture.class);
        assets.add(MOON_SHARD_FILE);
        manager.load(CROC_FILE,Texture.class);
        assets.add(CROC_FILE);
        manager.load(FLYING_FILE,Texture.class);
        assets.add(FLYING_FILE);
    }

    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        playerTexture = createTexture(manager,PLAYER_FILE, false);
        playerFlightWalkTexture = createTexture(manager,PLAYER_FLIGHT_WALK_FILE, false);
        playerTransWalkTexture = createTexture(manager,PLAYER_TRANS_WALK_FILE, false);
        playerDashWalkTexture = createTexture(manager,PLAYER_DASH_WALK_FILE, false);
        playerWalkTexture = createTexture(manager,PLAYER_WALK_FILE, false);
        flyingPlayerTexture = createTexture(manager,FLYING_AMARIS_FILE, false);
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
        serenityTexture = createTexture(manager,SERENITY_ICON_FILE, false);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        castleSpikeTexture = createTexture(manager, CASTLE_SPIKE_FILE, false);
        tile = createTexture(manager,TILE_FILE,false);
        brick_endtile = createTexture(manager,BRICK_ENDTILE_FILE,false);
        brick_endtile2 = createTexture(manager,BRICK_ENDTILE2_FILE,false);
        brick_endtile3 = createTexture(manager,BRICK_ENDTILE3_FILE,false);
        brick_endtile4 = createTexture(manager,BRICK_ENDTILE4_FILE,false);
        rockTexture1 = createTexture(manager,ROCK_FILE1,false);
        rockTexture2 = createTexture(manager,ROCK_FILE2,false);
        rockTexture3 = createTexture(manager,ROCK_FILE3,false);
        rockTileTexture = createTexture(manager,ROCK_TILE_FILE,false);
        window = createTexture(manager,WINDOW_FILE,false);
        cloudFountainTexture = createTexture(manager,CLOUD_FOUNTAIN_FILE, false);
        transparencyAbilityTexture = createTexture(manager,TRANSPARENT_ICON_FILE, false);
        flightAbilityTexture = createTexture(manager,FLIGHT_ICON_FILE, false);
        goalTile  = createTexture(manager,GOAL_FILE,false);
        cloudTile  = createTexture(manager,CLOUD_FILE,false);
        moonShardTexture = createTexture(manager,MOON_SHARD_FILE,false);
        crocTexture = createTexture(manager,CROC_FILE,false);
        flyingTexture = createTexture(manager,FLYING_FILE,false);
    }
}
