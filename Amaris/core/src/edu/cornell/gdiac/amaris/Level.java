package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.amaris.objects.*;
import edu.cornell.gdiac.amaris.obstacle.Obstacle;
import edu.cornell.gdiac.amaris.platform.FountainModel;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

import java.util.List;

/**
 * Class that represents a level.
 */
public class Level {
    public String name; // name of level
    public String background; // name of background
    public int num; // num of level
    public String path; // path to file of level
    public Boolean available; // if available to play
    public Boolean complete; // if completed
    public int checkpointsPassed; // # of checkpoints passed
    public int currentSerenity; // current amount of serenity left
    public float width; // width of this level
    public int numStarsCollected; // num stars player won
    public Level nextLevel; // next level

    // tutorial level
    public Array<Message> messages;
    public List<SignPost> signposts;

    // Gameplay attributes
    public PlayerModel player;
    public GoalDoor goalDoor;
    public int maxSerenity;
    public float threeStars; //The minimum for getting three stars
    public float twoStars; //The minimum for getting two stars
    public float oneStar; //The minimum for getting one star
    public List<FountainModel> fountains;
    public List<Obstacle> platforms;
    public List<MoonShard> shards;
    public List<Obstacle> obstacles;
    public List<FountainModel> checkpoints;
    public List<RegularPlatform> windows;

    public static class Message {
        float x;
        float y;
        String text;
    }

    /** Returns exit position (where to place stairs) */
    public GoalDoor getGoalDoor() {
        return goalDoor;
    }

    /** Returns player (Amaris) with the entrance position */
    public PlayerModel getPlayer() {
        return player;
    }

    /** Returns max serenity for this level */
    public int getMaxSerenity() { return maxSerenity; }

    /** Returns list of fountains with position, type, etc. */
    public List<FountainModel> getFountains() {
        return fountains;
    }

    /** Returns list of platforms with position, width, height, type, direction, velocity, bounds, etc. */
    public List<Obstacle> getPlatforms() {
        return platforms;
    }

    /** Returns list of moon shards with their positions */
    public List<MoonShard> getMoonShards() {
        return shards;
    }

    /** Returns list of obstacles with position, width, height, type, etc. */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    /** Returns list of checkpoints with their positions */
    public List<FountainModel> getCheckpoints() {
        return checkpoints;
    }

    /** Returns list of signposts for tutorial levels */
    public List<SignPost> getSignposts() {
        return signposts;
    }

    /** Returns list of tutorial messages */
    public Array<Message> getMessages() {
        return messages;
    }
}
