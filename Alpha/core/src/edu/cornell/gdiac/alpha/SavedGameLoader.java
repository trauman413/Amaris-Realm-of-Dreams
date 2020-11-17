package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Class that loads the possible levels in the game using JSON.
 */
public class SavedGameLoader {

    /** List of possible levels */
    private Array<Level> levels;

    // HELPFUL GETTERS ---------------------------------------------------------------------

    public Level getLevel(int i) {
        return levels.get(i);
    }

    public Array<Level> getJustAvailableLevels() {
        Array<Level> availableLevels = new Array<Level>();
        for (Level level : levels) {
            if (level.available && !level.complete) {
                availableLevels.add(level);
            }
        }
        return availableLevels;
    }

    public Array<Level> getLevels() {
        return levels;
    }

    public Array<Level> getCompletedLevels() {
        Array<Level> completedLevels = new Array<Level>();
        for (Level level : levels) {
            if (level.complete) {
                completedLevels.add(level);
            }
        }
        return completedLevels;
    }

    // SAVE GAME PARSING -------------------------------------------------------------------

    public void getSavedGame() {
        String file = "jsons/saved_game.json";
        JsonReader jsonReader = new JsonReader();
        JsonValue savedGameFile = jsonReader.parse(Gdx.files.internal(file));

        JsonValue levelsJson = savedGameFile.get("levels");
        if (levelsJson != null) {
            levels = new Array<Level>();
            int num = 0;
            for (JsonValue entry = levelsJson.child; entry != null; entry = entry.next) {
                Level newLevel = new Level();
                newLevel.name = entry.getString("name");
                newLevel.num = num;
                newLevel.complete = entry.getBoolean("completed");
                newLevel.available = entry.getBoolean("unlocked");
                newLevel.checkpointsPassed = entry.getInt("checkpoints");
                newLevel.currentSerenity = entry.getInt("serenity-left");
                newLevel.path = entry.getString("path");
                levels.add(newLevel);
                num++;
            }
        }
    }
}
