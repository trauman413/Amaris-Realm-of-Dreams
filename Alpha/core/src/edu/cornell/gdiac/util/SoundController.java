package edu.cornell.gdiac.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for controlling sound effects in LibGDX.
 */
public class SoundController {

	private static String soundLocation = "sounds/";
	private static String fileFormat = ".mp3";

	private static HashMap<String, Sound> namesToSounds = new HashMap<String, Sound>();

	public static Sound jumpSound() {
		return soundFromName("jump");
	}
	public static Sound flightSound(){
		return soundFromName("flight");
	}
	public static Sound menuSelectSound() {
		return soundFromName("menu_select");
	}
	public static Sound startLevelSound() {
		return soundFromName("start_level");
	}
	public static Sound touchFountainSound(){
		return soundFromName("touch_fountain");
	}
	public static Sound moonShardSound() {
		return soundFromName("moon_shard");
	}
	public static Sound goalSound() {
		return soundFromName("goal");
	}
	public static Sound dashSound() {
		return soundFromName("dash");
	}

	public static Sound soundFromName(String name){
		if(!namesToSounds.containsKey(name)){
			Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + name + fileFormat));
			namesToSounds.put(name, s);
			return s;
		}
		return namesToSounds.get(name);
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

	private static String musicLocation = "music/";
	private static String musicFormat = ".mp3";

	private static HashMap<String, Music> namesToMusic = new HashMap<String, Music>();

	public static Music level1Music() {
		return musicFromName("level1");
	}

	public static Music level2Music() {
		return musicFromName("level2");
	}

	public static Music musicFromName(String name){
		if(!namesToMusic.containsKey(name)){
			Music s = Gdx.audio.newMusic(Gdx.files.internal(musicLocation + name + fileFormat));
			namesToMusic.put(name, s);
			return s;
		}
		return namesToMusic.get(name);
	}

	public static void playMusic(Music m){
		m.play();
	}

	public static void playMusic(Music m, float volume){
		m.setVolume(volume);
		m.play();
	}

	public static void disposeMusic(String musicName){
		Music s = namesToMusic.get(musicName);
		s.dispose();
		namesToMusic.remove(musicName);
	}

	public static void disposeAllMusic(){
		ArrayList<String> names = new ArrayList<String>(namesToMusic.keySet());
		for (String key : names){
			disposeMusic(key);
		}
	}



}
