package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for controlling sound effects in LibGDX.
 */
public class SoundController {

    private static HashMap<String, Sound> namesToSounds = new HashMap<String, Sound>();

    public static Sound jumpSound() {
        LevelLoader.populateAssets("jsons/assets.json");
        if(!namesToSounds.containsKey("jump")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(LevelLoader.getSoundPath("jump")));
            namesToSounds.put("jump", s);
            return s;
        }
        return namesToSounds.get("jump");
    }
    public static Sound flightSound(){
        LevelLoader.populateAssets("jsons/assets.json");
        if(!namesToSounds.containsKey("flight")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(LevelLoader.getSoundPath("flight")));
            namesToSounds.put("flight", s);
            return s;
        }
        return namesToSounds.get("flight");
    }
    public static Sound menuSelectSound() {
        LevelLoader.populateAssets("jsons/assets.json");
        if(!namesToSounds.containsKey("menu_select")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(LevelLoader.getSoundPath("menu_select")));
            namesToSounds.put("menu_select", s);
            return s;
        }
        return namesToSounds.get("menu_select");
    }
    public static Sound startLevelSound() {
        LevelLoader.populateAssets("jsons/assets.json");
        if(!namesToSounds.containsKey("start_level")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(LevelLoader.getSoundPath("start_level")));
            namesToSounds.put("start_level", s);
            return s;
        }
        return namesToSounds.get("start_level");
    }
    public static Sound touchFountainSound(){
        LevelLoader.populateAssets("jsons/assets.json");
        if(!namesToSounds.containsKey("touch_fountain")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(LevelLoader.getSoundPath("touch_fountain")));
            namesToSounds.put("touch_fountain", s);
            return s;
        }
        return namesToSounds.get("touch_fountain");
    }

    public static void playSound(Sound s){
        s.play();
    }

    public static void playSound(Sound s, float volume){
        s.play(volume);
    }

    public static void dispose(String soundName){
        Sound s = namesToSounds.get(soundName);
        s.dispose();
        namesToSounds.remove(soundName);
    }

    public static void disposeAllSounds(){
        ArrayList<String> names = new ArrayList<String>(namesToSounds.keySet());
        for (String key : names){
            dispose(key);
        }
    }

}
