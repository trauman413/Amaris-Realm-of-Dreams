package edu.cornell.gdiac.amaris.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
	public static Sound hurtSound() {
		return soundFromName("hurt");
	}
	public static Sound etherialitySound() {
		return soundFromName("ethereality");
	}

	public static Sound soundFromName(String name){
		if(!namesToSounds.containsKey(name)){
			Sound s = Gdx.audio.newSound(Gdx.files.internal(soundLocation + name + fileFormat));
			namesToSounds.put(name, s);
			return s;
		}
		return namesToSounds.get(name);
	}

	public static void playSound(Sound s, float volume){
		if(!isMuted){
			s.play(volume);
		}
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
	private static Music currentlyPlaying = null;

	private static HashMap<String, Music> namesToMusic = new HashMap<String, Music>();

	public static Music level1Music() {
		return musicFromName("level1");
	}

	public static Music level2Music() {
		return musicFromName("level2");
	}

	public static Music level3Music() { return musicFromName("level3"); }

	public static Music menuMusic() { return musicFromName("main_theme"); }

	public static Music titleMusic() { return musicFromName("title"); }

	public static Music musicFromName(String name){
		if(!namesToMusic.containsKey(name)){
			Music s = Gdx.audio.newMusic(Gdx.files.internal(musicLocation + name + fileFormat));
			namesToMusic.put(name, s);
			return s;
		}
		return namesToMusic.get(name);
	}

	public static Timer waitForMusicTimer = new Timer();

	public static void playMusic(Music m, final float volume, boolean looping){
		if(m == currentlyPlaying){
			if(currentlyPlaying.isPlaying()){
				return;
			}
		}
		if(currentlyPlaying != null){
			currentlyPlaying.stop();
			currentlyPlaying = null;
		}
		if(isMuted){
			final Music m_ = m;
			final float vol_ = volume;
			final boolean loop_ = looping;
			if(waitForMusicTimer != null){
				waitForMusicTimer.cancel();
				waitForMusicTimer = null;
				waitForMusicTimer = new Timer();
			}
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					if(!isMuted){
						playMusic(m_, vol_, loop_);
					}
				}
			};
			waitForMusicTimer.schedule(task, 0, 100);
		} else {
			m.setVolume(volume);
			m.setLooping(looping);
			m.setOnCompletionListener(new Music.OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					currentlyPlaying = null;
				}
			});
			m.play();
			currentlyPlaying = m;
		}
	}

	public static void pauseMusic(){
		if(currentlyPlaying != null){
			currentlyPlaying.pause();
		}
	}

	public static void resumeMusic(){
		if(currentlyPlaying != null && !isMuted){
			currentlyPlaying.play();
		}
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

	public static boolean isMuted = false;

	public static void mute(){
		isMuted = true;
		for(Sound s : namesToSounds.values()){
			s.stop();
		}
		if(currentlyPlaying != null){
			pauseMusic();
		}
	}

	public static void unmute(){
		isMuted = false;
//		for(Sound s : namesToSounds.values()){
//			s.play();
//		}
		if(currentlyPlaying != null){
			resumeMusic();
		}
	}



}
