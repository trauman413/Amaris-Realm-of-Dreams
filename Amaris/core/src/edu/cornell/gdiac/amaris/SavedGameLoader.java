package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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

    public Array<Level> getAvailableLevels() {
        Array<Level> availableLevels = new Array<Level>();
        for (Level level : levels) {
            if (level.available) {
                availableLevels.add(level);
            }
        }
        return availableLevels;
    }

    // SAVE GAME PARSING -------------------------------------------------------------------

    public void getSavedGame() {
        String file = "jsons/saved_game.json";
        JsonReader jsonReader = new JsonReader();
        JsonValue savedGameFile = jsonReader.parse(Gdx.files.internal(file));

        JsonValue levelsJson = savedGameFile.get("levels");
        levels = new Array<Level>();
        if (levelsJson != null) {
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
                newLevel.numStarsCollected = entry.getInt("num-stars");
                levels.add(newLevel);
                num++;
            }
        }
        for (int i = 0; i < levels.size; i++) {
            if (i < levels.size - 1) {
                levels.get(i).nextLevel = levels.get(i+1);
            } else {
                levels.get(i).nextLevel = null;
            }
        }
    }

    public String createJsonString(String name, Boolean complete, Boolean unlock, int checkpts, int serenityLeft, String path, String abilQueue, int numStars) {

        String text = "{ \n \"name\": \"" + name + "\",\n \"completed\": " + complete + ",\n " + "\"unlocked\": " + unlock + ",\n \"checkpoints\": " + checkpts + ",\n \"serenity-left\": " + serenityLeft + ",\n \"path\": \"" + path + "\",\n \"ability-queue\": " + abilQueue + ",\n \"num-stars\": " + numStars + " \n},";

        return text;
    }

    public void getNewGame() {
        String file = "jsons/saved_game.json";

        JsonReader jsonReader = new JsonReader();
        JsonValue savedGameFile = jsonReader.parse(Gdx.files.internal(file));
//		Array<Level> levels = new Array<Level>();


        String text = "{\"levels\": [";
        JsonValue levelsJson = savedGameFile.get("levels");
        levels = new Array<Level>();
        if (levelsJson != null) {
            int num = 0;
            for (JsonValue entry = levelsJson.child; entry != null; entry = entry.next) {
                Level newLevel = new Level();
                newLevel.name = entry.getString("name");
                newLevel.num = num;
                newLevel.complete = false;
                if (num == 0) {
                    newLevel.available = true;
                } else {
                    newLevel.available = false;
                }
                newLevel.checkpointsPassed = 0;
                newLevel.currentSerenity = 0;
                newLevel.path = entry.getString("path");
                newLevel.numStarsCollected = 0;
                levels.add(newLevel);
                num++;

                String lvlText;
                lvlText = createJsonString(newLevel.name, newLevel.complete, newLevel.available, newLevel.checkpointsPassed, newLevel.currentSerenity, newLevel.path, null, newLevel.numStarsCollected);
                text = text + "\n" + lvlText;

            }
        }
        for (int i = 0; i < levels.size; i++) {
            if (i < levels.size - 1) {
                levels.get(i).nextLevel = levels.get(i+1);
            } else {
                levels.get(i).nextLevel = null;
            }
        }

        text = text + "\n]\n}";
        FileHandle fileToPrint = Gdx.files.local("jsons/saved_game.json");
        fileToPrint.writeString(text, false);
    }
}
