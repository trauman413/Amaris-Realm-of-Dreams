/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
 package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

import edu.cornell.gdiac.util.*;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas; 
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Mode for intro screen */
	private IntroScreen intro_screen;
	/** Mode for level select */
	private LevelSelect level_select;
	/** Mode for playing */
	private GameMode playing;
	/** Mode for loading level */
	private LevelLoader levelLoader;
	/** Retrieves available levels */
	private SavedGameLoader savedGameLoader;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {
		// Start loading with the asset manager
		manager = new AssetManager();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		levelLoader = new LevelLoader();
		savedGameLoader = new SavedGameLoader();
	}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();

		// Level Select
		levelLoader.populateAssets("jsons/assets.json");
		intro_screen = new IntroScreen(canvas, levelLoader);
		intro_screen.setScreenListener(this);
		setScreen(intro_screen);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		screen.dispose();
		setScreen(null);

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		manager.clear();
		manager.dispose();

		SoundController.disposeAllSounds();

		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 * @param level	   The level selected
	 */
	public void exitScreen(Screen screen, int exitCode, int level) {
		if (screen == intro_screen) {
			level_select = new LevelSelect(canvas, manager, savedGameLoader, levelLoader, 1);
			level_select.setScreenListener(this);
			setScreen(level_select);

			intro_screen.dispose();
			intro_screen = null;
		}else if (screen == level_select) {
			loading = new LoadingMode(canvas,manager,1);
			loading.setScreenListener(this);
			setScreen(loading);

			//Start the music
			Music m = SoundController.level2Music();
			SoundController.playMusic(m, 0.16f);
			m.setLooping(true);

			// Initialize the game world
			levelLoader.level = savedGameLoader.getLevel(level);
			playing = new GameplayController(levelLoader, savedGameLoader.getLevel(level));
			playing.preLoadContent(manager);

			level_select.dispose();
			level_select = null;
		}else if (screen == loading) {
			playing.loadContent(manager);
			playing.setScreenListener(this);
			playing.setCanvas(canvas);
			playing.reset();
			setScreen(playing);

			loading.dispose();
			loading = null;
		} else if (screen == playing) {
			if (level == -1) {
				level_select = new LevelSelect(canvas, manager, savedGameLoader, levelLoader, 1);
				level_select.setScreenListener(this);
				setScreen(level_select);
			} else {
				loading = new LoadingMode(canvas,manager,5);
				loading.setScreenListener(this);
				setScreen(loading);

				// Initialize new level
				if (level < savedGameLoader.getLevels().size) {
					levelLoader.level = savedGameLoader.getLevel(level);
					playing = new GameplayController(levelLoader, savedGameLoader.getLevel(level));
					playing.preLoadContent(manager);
				} else {
					Gdx.app.exit();
				}
			}


		} else if (exitCode == GameMode.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}

}
