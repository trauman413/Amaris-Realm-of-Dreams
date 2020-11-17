package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.amaris.util.*;

/**
 * Class that provides a loading screen for the state of the game.
 */
public class LevelSelect implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "images/level_select.png";
    private static String COMPLETED_FILE;
    private static String AVAILABLE_FILE;
    private static String AVAILABLE_CLICKED_FILE;
    private static String COMPLETED_CLICKED_FILE;
    private static String GLOW_FILE;
    private static String GLOW_CLICKED_FILE;
    private static String UNCLICKABLE_FILE;
    private static String STAR_FILE;

    /** Background texture for start-up */
    private Texture background;
    /** Play buttons */
    private Array<Texture> buttons;
    /** Play buttons clicked */
    private Array<Texture> clickedButtons;
    /** Nums for buttons */
    private Array<Texture> numbers;
    /** Positions of buttons */
    private Array<Vector2> positions;
    /** Background texture for star */
    private Texture star;

    private float width;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.47f;

    /** Start button for XBox controller on Windows */
    private static int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static int MAC_OS_X_START = 4;

    /** AssetManager to be loading in the background */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Current progress (0 to 1) of the asset manager */
    private float progress;
    /** The current state of the play button */
    private int   pressState;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** Level that gets selected */
    private int level;

    /** Available levels */
    private Array<Level> levels;
    /** Retrieves available levels */
    private SavedGameLoader savedGameLoader;
    /** Retrieves assets */
    private LevelLoader levelLoader;

    private int num;
    private int TOTAL_NUMBER_LEVELS = 14;

    /** Number stars per level */
    private Array<Integer> numStars;


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param manager The AssetManager to load in the background
     * @param millis The loading budget in milliseconds
     */
    public LevelSelect(GameCanvas canvas, AssetManager manager, SavedGameLoader savedGameLoader, LevelLoader levelLoader, int millis) {
        this.manager = manager;
        this.canvas  = canvas;
        budget = millis;

        // No progress so far.
        progress   = 0;
        pressState = 0;
        active = false;
        level = 0;

        // Load the next two images immediately.
        background = new Texture(BACKGROUND_FILE);
        COMPLETED_FILE = levelLoader.getImagePath("completed_unclicked");
        COMPLETED_CLICKED_FILE = levelLoader.getImagePath("completed_clicked");
        AVAILABLE_FILE = levelLoader.getImagePath("available_unclicked");
        AVAILABLE_CLICKED_FILE = levelLoader.getImagePath("available_clicked");
        GLOW_FILE = levelLoader.getImagePath("completed_unclicked_glow");
        GLOW_CLICKED_FILE = levelLoader.getImagePath("completed_clicked_glow");
        UNCLICKABLE_FILE = levelLoader.getImagePath("unclickable");
        STAR_FILE = levelLoader.getImagePath("star");


        this.savedGameLoader = savedGameLoader;
        this.savedGameLoader.getSavedGame();
        this.levelLoader = levelLoader;
        levels = savedGameLoader.getLevels();
        buttons = new Array<Texture>();
        clickedButtons = new Array<Texture>();
        numbers = new Array<Texture>();
        numStars = new Array<Integer>();

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        active = true;

        num = 0;
        int unavailable = 0;
        for (Level level : savedGameLoader.getLevels()) {
            Texture button;
            Texture clickedButton;
            if (level.complete) {
                if (level.numStarsCollected == 3) {
                    button = new Texture(GLOW_FILE);
                    clickedButton = new Texture(GLOW_CLICKED_FILE);
                    clickedButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    clickedButtons.add(clickedButton);
                } else {
                    button = new Texture(COMPLETED_FILE);
                    clickedButton = new Texture(COMPLETED_CLICKED_FILE);
                    clickedButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    clickedButtons.add(clickedButton);
                }

            } else if (level.available) {
                button = new Texture(AVAILABLE_FILE);
                clickedButton = new Texture(AVAILABLE_CLICKED_FILE);
                clickedButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                clickedButtons.add(clickedButton);
            } else {
                button = new Texture(UNCLICKABLE_FILE);
                unavailable++;
                clickedButtons.add(null);
            }
            button.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            buttons.add(button);
            Texture number = new Texture(levelLoader.getImagePath(String.valueOf(num+1)));
            number.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            numbers.add(number);
            num++;
            numStars.add(level.numStarsCollected);
        }

        for (int i = num; i < TOTAL_NUMBER_LEVELS; i++) {
            Texture button = new Texture(UNCLICKABLE_FILE);
            button.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            buttons.add(button);
            Texture number = new Texture(levelLoader.getImagePath(String.valueOf(i + 1)));
            number.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            numbers.add(number);
            //num++;
        }

        star = new Texture(STAR_FILE);
        star.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        Texture number = new Texture(levelLoader.getImagePath("9"));
//        number.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        numbers.add(number);

        num -= unavailable;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        if (background != null) {
            background.dispose();
            background = null;
        }
        if (buttons != null) {
            for (Texture b : buttons) {
                if (b != null) {
                    b.dispose();
                }
            }
            buttons = null;
        }

        if (clickedButtons != null) {
            for (Texture b : clickedButtons) {
                if (b != null) {
                    b.dispose();
                }
            }
            clickedButtons = null;
        }
        pressState = 0;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
    }

    public void reset() {
        pressState = 0;
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.beginConstantBatch();

        canvas.drawConstantBackground(background, -30, -25);

        for (int i = 0; i < TOTAL_NUMBER_LEVELS; i++) {
            if (i < positions.size && i < buttons.size) {
                Color tint = Color.WHITE;
                float x = positions.get(i).x;
                float y = positions.get(i).y;
                if (buttons.size > i) {
                    Texture b = (level == i && pressState == 1 ? clickedButtons.get(i) : buttons.get(i));
                    canvas.drawConstant(b, tint, b.getWidth() / 2.0f, b.getHeight() / 2.0f,
                            x, y, 0, scale, scale);
                    Texture n = numbers.get(i);
                    canvas.drawConstant(n, tint, n.getWidth() / 2.0f, n.getHeight() / 2.0f,
                            x, y, 0, scale, scale);
                    float sx = x - 37;
                    float sy = y + 34;
                    for (int s = 0; s < numStars.get(i); s++) {
                        canvas.drawConstant(star, tint, star.getWidth() / 2.0f, star.getHeight() / 2.0f,
                                sx, sy, 0, scale, scale);
                        if (s == 1) {
                            sx += 36;
                        } else {
                            sx += 36;
                        }
                        if (s == 0) {
                            sy += 10;
                        } else {
                            sy -= 10;
                        }
                    }
                }
            }
        }
//        Texture n = numbers.get(8);
//        float x = positions.get(8).x;
//        float y = positions.get(8).y;
//        Color tint = Color.WHITE;
//        canvas.draw(n, tint, n.getWidth() / 2, n.getHeight() / 2, x, y, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        canvas.endConstantBatch();
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
                pressState = 0;
                listener.exitScreen(this, 0, level);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        heightY = height;

        this.width = (int)(BAR_WIDTH_RATIO*width);

        positions = new Array<Vector2>();

        // TODO: do differently, once we have level select assets
        positions.add(new Vector2(width/2 - 119, (int)(BAR_HEIGHT_RATIO*height) - 47)); //1
        positions.add(new Vector2(width/2 - 4, (int)(BAR_HEIGHT_RATIO*height) - 24)); //2
        positions.add(new Vector2(width/2 + 112, (int)(BAR_HEIGHT_RATIO*height) - 45)); //3
        positions.add(new Vector2(width/2 + 273, (int)(BAR_HEIGHT_RATIO*height)+3)); //4
        positions.add(new Vector2(width/2 + 148, (int)(BAR_HEIGHT_RATIO*height)+67)); //5
        positions.add(new Vector2(width/2 - 3, (int)(BAR_HEIGHT_RATIO*height)+91)); //6
        positions.add(new Vector2(width/2 - 155, (int)(BAR_HEIGHT_RATIO*height)+66)); //7
        positions.add(new Vector2(width/2 - 281, (int)(BAR_HEIGHT_RATIO*height)+7)); //8
        positions.add(new Vector2(width/2 - 420, (int)(BAR_HEIGHT_RATIO*height)+73)); //9
        positions.add(new Vector2(width/2 - 308, (int)(BAR_HEIGHT_RATIO*height)+148)); //10
        positions.add(new Vector2(width/2 - 169, (int)(BAR_HEIGHT_RATIO*height)+205)); //11
        positions.add(new Vector2(width/2 + 159, (int)(BAR_HEIGHT_RATIO*height)+203)); //12
        positions.add(new Vector2(width/2 + 410, (int)(BAR_HEIGHT_RATIO*height)+74)); //13
        positions.add(new Vector2(width/2 + 296, (int)(BAR_HEIGHT_RATIO*height)+246)); //14

    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (buttons.size == 0 || pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        
        for (int i = 0; i < TOTAL_NUMBER_LEVELS; i++) {
            if (levels.get(i).available) {
                if (i < positions.size && i < buttons.size) {
                    Texture b = buttons.get(i);
                    float radius = scale*b.getWidth()/2.0f - 20;
                    float x = positions.get(i).x;
                    float y = positions.get(i).y;
                    float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
                    if (dist < radius*radius) {
                        pressState = 1;
                        level = i;
                        Sound s = SoundController.menuSelectSound();
                        SoundController.playSound(s,0.8f);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (buttonCode == startButton && pressState == 0) {
            pressState = 1;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1 && buttonCode == startButton) {
            pressState = 2;
            return false;
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}
