package edu.cornell.gdiac.techprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * A class for controlling sound effects in LibGDX.
 */
public class SoundController {

    private static String soundLocation = "sounds/";
    private static String fileFormat = ".mp3";

    private static HashMap<String, Sound> namesToSounds = new HashMap<String, Sound>();

    public static Sound jumpSound() {
        if(!namesToSounds.containsKey("jump")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + "jump" + fileFormat));
            namesToSounds.put("jump", s);
            return s;
        }
        return namesToSounds.get("jump");
    }
    public static Sound flightSound(){
        if(!namesToSounds.containsKey("flight")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + "flight" + fileFormat));
            namesToSounds.put("flight", s);
            return s;
        }
        return namesToSounds.get("flight");
    }
    public static Sound menuSelectSound() {
        if(!namesToSounds.containsKey("menu_select")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + "menu_select" + fileFormat));
            namesToSounds.put("menu_select", s);
            return s;
        }
        return namesToSounds.get("menu_select");
    }
    public static Sound startLevelSound() {
        if(!namesToSounds.containsKey("start_level")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + "start_level" + fileFormat));
            namesToSounds.put("start_level", s);
            return s;
        }
        return namesToSounds.get("start_level");
    }
    public static Sound touchFountainSound(){
        if(!namesToSounds.containsKey("touch_fountain")){
            Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + "touch_fountain" + fileFormat));
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
