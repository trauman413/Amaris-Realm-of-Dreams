package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.alpha.objects.FountainModel;
import edu.cornell.gdiac.alpha.platforms.SpikedPlatform;

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

    private Level level;

    // Fountain info to populate level
    public class Fountain {
        protected float x;
        protected float y;
        protected String name;
        protected FountainModel.FountainType type;
    }

    // Platform info to populate level
    public class Platform {
        protected float x;
        protected float y;
        protected List<Float> points;
        protected float width;
        protected float height;
        protected String name;
        protected String type;
        protected SpikedPlatform.SpikeDirection direction;
        protected Vector2 velocity;
        protected Vector2 bounds;
    }

    // Obstacle info to populate level
    public class Obstacle {
        protected float x;
        protected float y;
        protected float width;
        protected float height;
        protected String name;
        protected String type;
        protected List<Float> points;
        protected Vector2 velocity;
        protected Vector2 bounds;
    }

    // Shard info to populate level
    public class Shard {
        protected float x;
        protected float y;
        protected String name;
        protected Vector2 velocity;
        protected Vector2 bounds;
    }

    // Level info
    private class Level {
        private Vector2 entrance;
        private Vector2 exit;
        private int maxSerenity;
        private List<Fountain> fountains;
        private List<Platform> platforms;
        private List<Shard> shards;
        private List<Obstacle> obstacles;
        private List<Vector2> checkpoints;
    }

    // GETTERS ----------------------------------------------------------------------------

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

    /** Returns exit position (where to place stairs) */
    public Vector2 getExitPos() {
        return level.exit;
    }

    /** Returns entrance position (where to place Amaris) */
    public Vector2 getEntrancePos() {
        return level.entrance;
    }

    /** Returns max serenity for this level */
    public int getMaxSerenity() { return level.maxSerenity; }

    /** Returns list of fountains with position, type, etc. */
    public List<Fountain> getFountains() {
        return level.fountains;
    }

    /** Returns list of platforms with position, width, height, type, direction, velocity, bounds, etc. */
    public List<Platform> getPlatforms() {
        return level.platforms;
    }

    /** Returns list of moon shards with their positions */
    public List<Shard> getMoonShards() {
        return level.shards;
    }

    /** Returns list of obstacles with position, width, height, type, etc. */
    public List<Obstacle> getObstacles() {
        return level.obstacles;
    }

    /** Returns list of checkpoints with their positions */
    public List<Vector2> getCheckpoints() {
        return level.checkpoints;
    }


    // POPULATING METHODS ------------------------------------------------------------------

    /** Populates the fountains in this level with position, type, etc. */
    private void populateFountains(JsonValue levelJson) {
        level.fountains = new ArrayList<Fountain>();
        JsonValue fountains = levelJson.get("fountains");
        if (fountains != null) {
            for (JsonValue entry = fountains.child; entry != null; entry = entry.next) {
                Fountain fountain = new Fountain();
                fountain.x = entry.getFloat("x");
                fountain.y = entry.getFloat("y");
                if (entry.getString("type").equals("dash")) {
                    fountain.type = FountainModel.FountainType.DASH;
                } else if (entry.getString("type").equals("flight"))  {
                    fountain.type = FountainModel.FountainType.FLIGHT;
                } else if (entry.getString("type").equals("restore"))  {
                    fountain.type = FountainModel.FountainType.RESTORE;
                } else {
                    fountain.type = FountainModel.FountainType.TRANSPARENCY;
                }
                fountain.name = "fountain" + entry.getString("id");
                level.fountains.add(fountain);
            }
        }
    }

    /** Populates the platforms in this level with position, width, height, type, direction, velocity, bounds, etc. */
    private void populatePlatforms(JsonValue levelJson) {
        level.platforms = new ArrayList<Platform>();
        JsonValue platforms = levelJson.get("platforms");
        if (platforms != null) {
            for (JsonValue entry = platforms.child; entry != null; entry = entry.next) {
                Platform platform = new Platform();
                platform.points = new ArrayList<Float>();
                platform.x = entry.getFloat("x");
                platform.y = entry.getFloat("y");
                platform.width = entry.getFloat("width");
                platform.height = entry.getFloat("height");
                platform.points.add(platform.x);
                platform.points.add(platform.y);

                platform.points.add(platform.x + platform.width);
                platform.points.add(platform.y);

                platform.points.add(platform.x + platform.width);
                platform.points.add(platform.y + platform.height);

                platform.points.add(platform.x);
                platform.points.add(platform.y + platform.height);

                platform.type = entry.getString("type");
                platform.name = "platform" + entry.getString("id");
                if (entry.has("direction")) {
                    if (entry.getString("direction").equals("UP")) {
                        platform.direction = SpikedPlatform.SpikeDirection.UP;
                    } else if (entry.getString("direction").equals("DOWN")) {
                        platform.direction = SpikedPlatform.SpikeDirection.DOWN;
                    } else if (entry.getString("direction").equals("LEFT")) {
                        platform.direction = SpikedPlatform.SpikeDirection.LEFT;
                    } else {
                        platform.direction = SpikedPlatform.SpikeDirection.RIGHT;
                    }
                }

                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity");
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity");
                platform.velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds");
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds");
                platform.bounds = new Vector2(xBound, yBound);

                level.platforms.add(platform);
            }
        }
    }

    /** Populates the moon shards in this level with their position */
    private void populateMoonShards(JsonValue levelJson) {
        level.shards = new ArrayList<Shard>();
        JsonValue shards = levelJson.get("moon-shards");
        if (shards != null) {
            for (JsonValue entry = shards.child; entry != null; entry = entry.next) {
                Shard shard = new Shard();
                shard.x = entry.getFloat("x");
                shard.y = entry.getFloat("y");
                shard.name = "shard" + entry.getString("id");

                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity");
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity");
                shard.velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds");
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds");
                shard.bounds = new Vector2(xBound, yBound);

                level.shards.add(shard);
            }
        }
    }

    /** Populates the obstacles in this level with position, width, height, type, etc. */
    private void populateObstacles(JsonValue levelJson) {
        level.obstacles = new ArrayList<Obstacle>();
        JsonValue obstacles = levelJson.get("obstacles");
        if (obstacles != null) {
            for (JsonValue entry = obstacles.child; entry != null; entry = entry.next) {
                Obstacle obstacle = new Obstacle();
                obstacle.x = entry.getFloat("x");
                obstacle.y = entry.getFloat("y");
                obstacle.width = entry.getFloat("width");
                obstacle.height = entry.getFloat("height");
                obstacle.name = entry.getString("type") + entry.getString("id");
                obstacle.type = entry.getString("type");
                if (obstacle.type.equals("falling_rock_tile")) {
                    obstacle.points = new ArrayList<Float>();
                    obstacle.points.add(obstacle.x);
                    obstacle.points.add(obstacle.y);

                    obstacle.points.add(obstacle.x + obstacle.width);
                    obstacle.points.add(obstacle.y);

                    obstacle.points.add(obstacle.x + obstacle.width);
                    obstacle.points.add(obstacle.y + obstacle.height);

                    obstacle.points.add(obstacle.x);
                    obstacle.points.add(obstacle.y + obstacle.height);
                }

                float xVel = 0;
                float yVel = 0;
                if (entry.has("x-velocity")) xVel = entry.getFloat("x-velocity");
                if (entry.has("y-velocity")) yVel = entry.getFloat("y-velocity");
                obstacle.velocity = new Vector2(xVel, yVel);

                float xBound = 0;
                float yBound = 0;
                if (entry.has("x-bounds")) xBound = entry.getFloat("x-bounds");
                if (entry.has("y-bounds")) yBound = entry.getFloat("y-bounds");
                obstacle.bounds = new Vector2(xBound, yBound);

                level.obstacles.add(obstacle);
            }
        }
    }

    /** Populates the checkpoints in this level with their position */
    private void populateCheckpoints(JsonValue levelJson) {
        level.checkpoints = new ArrayList<Vector2>();
        JsonValue checkpoints = levelJson.get("checkpoints");
        if (checkpoints != null) {
            for (JsonValue entry = checkpoints.child; entry != null; entry = entry.next) {
                Vector2 checkpoint = new Vector2(entry.getFloat("x"), entry.getFloat("y"));
                level.checkpoints.add(checkpoint);
            }
        }
    }

    /** Populates the specific level with the info provided in the JSON file `file` */
    public void populateLevel(String file, int num) {
        level = new Level();
        JsonReader jsonReader = new JsonReader();
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal(file));

        JsonValue currentLevel = levelFormat.get("level" + num);

        JsonValue entrance = currentLevel.get("entrance");
        level.entrance = new Vector2(entrance.getFloat("x"), entrance.getFloat("y"));

        JsonValue exit = currentLevel.get("exit");
        level.exit = new Vector2(exit.getFloat("x"), exit.getFloat("y"));

        level.maxSerenity = currentLevel.getInt("max_serenity");

        populateFountains(currentLevel);
        populatePlatforms(currentLevel);
        populateMoonShards(currentLevel);
        populateObstacles(currentLevel);
        populateCheckpoints(currentLevel);
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
}
