package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

import edu.cornell.gdiac.amaris.util.*;

/**
 * Root class for a LibGDX.
 *
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * platforms. In addition, this functions as the root class for all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Displaying the intro story */
	private IntroStory introStory;
	/** Displaying the ending story */
	private EndingStory endingStory;
	/** Mode for intro screen */
	private IntroScreen introScreen;
	/** Mode for menu */
	private Menu menu;
	/** Mode for level select */
	private LevelSelect levelSelect;
	/** Mode for playing */
	private GameMode playing;
	/** Mode for loading level */
	private LevelLoader levelLoader;
	/** Retrieves available levels */
	private SavedGameLoader savedGameLoader;
	private boolean mute;

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

		mute = false;
	}

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();

		// Launch introduction screen
		LevelLoader.populateAssets("jsons/assets.json");
		introStory = new IntroStory(canvas, levelLoader);
		introStory.setScreenListener(this);
		setScreen(introStory);
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

		if (levelSelect != null) {
			levelSelect.dispose();
		}

		levelLoader.clear();
		levelLoader = null;

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
		if (screen == introStory) {
			introScreen = new IntroScreen(canvas, levelLoader);
			introScreen.setScreenListener(this);
			setScreen(introScreen);

			Music m = SoundController.titleMusic();
			SoundController.playMusic(m, 0.16f, true);

			introStory.dispose();
			introStory = null;
		} else if (screen == introScreen) {
			menu = new Menu(canvas, levelLoader);
			menu.setScreenListener(this);
			setScreen(menu);

			//Start the music
			SoundController.disposeAllMusic();
			Music m = SoundController.menuMusic();
			SoundController.playMusic(m, 0.16f, true);

			introScreen.dispose();
			introScreen = null;
		} else if (screen == menu) {
			if (exitCode == 1) {
				savedGameLoader.getNewGame();
			}
			levelSelect = new LevelSelect(canvas, manager, savedGameLoader, levelLoader, 15);
			levelSelect.setScreenListener(this);
			setScreen(levelSelect);

			menu.dispose();
			menu = null;
		} else if (screen == levelSelect) {
			loading = new LoadingMode(canvas,manager,15);
			loading.setScreenListener(this);
			setScreen(loading);

			// Initialize the game world
			levelLoader.level = savedGameLoader.getLevel(level);
			playing = new GameplayController(levelLoader, savedGameLoader.getLevel(level), mute);
			playing.preLoadContent(manager);

			levelSelect.dispose();
			levelSelect = null;
		} else if (screen == loading) {
			playing.loadContent(manager);
			playing.setScreenListener(this);
			playing.setCanvas(canvas);
			playing.reset();
			//Start the music
			String s = levelLoader.level.background;
			SoundController.disposeAllMusic();
			Music m = null;
			if(s.equalsIgnoreCase("ground")) {
				m = SoundController.level1Music();
				SoundController.playMusic(m, 0.16f, true);
			}
			if(s.equalsIgnoreCase("cloud")) {
				m = SoundController.level3Music();
				SoundController.playMusic(m, 0.16f, true);
			}
			if(s.equalsIgnoreCase("space")) {
				m = SoundController.level2Music();
				SoundController.playMusic(m, 0.2f, true);
			}
			setScreen(playing);

			loading.dispose();
			loading = null;
		} else if (screen == playing) {
			this.mute = playing.mute;
			if (level == -2) {
				introScreen = new IntroScreen(canvas, levelLoader);
				introScreen.setScreenListener(this);
				setScreen(introScreen);

				//Start the music
				SoundController.disposeAllMusic();
				Music m = SoundController.titleMusic();
				SoundController.playMusic(m, 0.16f, true);
			} else if (level == -1) {
				levelSelect = new LevelSelect(canvas, manager, savedGameLoader, levelLoader, 15);
				levelSelect.setScreenListener(this);
				setScreen(levelSelect);

				//Start the music
				SoundController.disposeAllMusic();
				Music m = SoundController.menuMusic();
				SoundController.playMusic(m, 0.16f, true);
			} else {
				// Initialize new level
				if (level < savedGameLoader.getLevels().size) {
					loading = new LoadingMode(canvas,manager,15);
					loading.setScreenListener(this);
					setScreen(loading);

					levelLoader.level = savedGameLoader.getLevel(level);
					playing = new GameplayController(levelLoader, savedGameLoader.getLevel(level), mute);
					playing.preLoadContent(manager);

				} else {
//					level_select = new LevelSelect(canvas, manager, savedGameLoader, levelLoader, 1);
//					level_select.setScreenListener(this);
//					setScreen(level_select);
					endingStory = new EndingStory(canvas, levelLoader);
					endingStory.setScreenListener(this);
					setScreen(endingStory);
				}
			}
		} else if (screen == endingStory) {
			introScreen = new IntroScreen(canvas, levelLoader);
			introScreen.setScreenListener(this);
			setScreen(introScreen);

			endingStory.dispose();
			endingStory = null;
			//Start the music
			SoundController.disposeAllMusic();
			Music m = SoundController.titleMusic();
			SoundController.playMusic(m, 0.16f, true);
		}
		else if (exitCode == GameMode.EXIT_QUIT) {
			// We quit the main application
			if (playing != null) playing.editSaveJson(false);
			Gdx.app.exit();
		}
	}

}
