/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import edu.cornell.gdiac.amaris.util.ScreenListener;
import edu.cornell.gdiac.amaris.util.SoundController;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class EndingStory implements Screen, InputProcessor, ControllerListener {
    private static String BACKGROUND_FILE1;
    private static String BACKGROUND_FILE2;
    private static String BACKGROUND_FILE3;
    private static String BACKGROUND_FILE4;
    private static String BACKGROUND_FILE5;
    private static String BUTTON;
    private static String BUTTON_CLICKED;


    /** Background texture */
    private Texture background1;
    private Texture background2;
    private Texture background3;
    private Texture background4;
    private Texture background5;
//    /** Button texture */
//    private Texture button;
//    /** Button clicked texture*/
//    private Texture buttonClicked;
//    /** The current state of the play button */
//    private int pressState;
//    /** x position of the play button */
//    private int BUTTON_X = 924;
//    /** y position of the play button */
//    private int BUTTON_Y = 480;
//    /** Diameter of the play button */
//    private int BUTTON_DIAMETER = 100;


    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this mode is still active */
    private boolean active;
    /** Whether or not the key has been pressed*/
    private int keyPressed;

    private int bkgndTime;
    private float alpha = 0.5f;

    /**
     * Creates a IntroScreen with the default size and position.
     *
     */
    public EndingStory(GameCanvas canvas, LevelLoader levelLoader) {
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        BACKGROUND_FILE1 = levelLoader.getImagePath("ending_slide1");
        background1 = new Texture(BACKGROUND_FILE1);
        BACKGROUND_FILE2 = levelLoader.getImagePath("ending_slide2");
        background2= new Texture(BACKGROUND_FILE2);
        BACKGROUND_FILE3 = levelLoader.getImagePath("ending_slide3");
        background3= new Texture(BACKGROUND_FILE3);
        BACKGROUND_FILE4 = levelLoader.getImagePath("ending_slide4");
        background4= new Texture(BACKGROUND_FILE4);
        BACKGROUND_FILE5 = levelLoader.getImagePath("ending_slide5");
        background5= new Texture(BACKGROUND_FILE5);
//        BUTTON = levelLoader.getImagePath("skip_button");
//        button = new Texture(BUTTON);
//        BUTTON_CLICKED = levelLoader.getImagePath("skip_button_clicked");
//        buttonClicked = new Texture(BUTTON_CLICKED);


        active = false;

        Gdx.input.setInputProcessor(this);

        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        active = true;
        bkgndTime = 0;

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        background1.dispose();
        background2.dispose();
        background1 = null;
        background2 = null;
    }

    private boolean isReady() {
        return bkgndTime > 1000;
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
        bkgndTime++;
        alpha = alpha+0.005f;
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();


        Color color = new Color(1,1,1,alpha);
        //Color color = Color.WHITE;
        if(bkgndTime % 200 == 0) {
            alpha = 0.5f;
        }
        if(bkgndTime < 200) {
            //canvas.drawConstantBackground(background1, -30, -7);
            canvas.draw(background1, color, -152, -85, 1332, 750);
        }
        else if (bkgndTime < 400) {
            // canvas.drawConstantBackground(background2, -30, -7);
            canvas.draw(background2, color, -152, -85, 1332, 750);
        }
        else if(bkgndTime < 600) {
            canvas.draw(background3, color, -152, -85, 1332, 750);
        }
        else if(bkgndTime < 800) {
            canvas.draw(background4, color, -152, -85, 1332, 750);
        }
        else if(bkgndTime < 1000) {
            canvas.draw(background5, color, -152, -85, 1332, 750);
        }
        canvas.end();
//        canvas.beginConstantBatch();
//        if (pressState == 1 || keyPressed == 1) {
//            canvas.drawConstant(buttonClicked, Color.WHITE, BUTTON_X, BUTTON_Y, BUTTON_DIAMETER, BUTTON_DIAMETER);
//        } else {
//            canvas.drawConstant(button, Color.WHITE, BUTTON_X, BUTTON_Y, BUTTON_DIAMETER, BUTTON_DIAMETER);
//        }
//        canvas.endConstantBatch();
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
                listener.exitScreen(this, 0, 0);
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
//        if (pressState == 2) return true;
//        if (canvas != null) {
//            // Flip to match graphics coordinates
//            screenY = canvas.getHeight()-screenY;
//
//            // Inside play button
//            float radius = BUTTON_DIAMETER/2.0f;
//            float x = BUTTON_X+radius;
//            float y = BUTTON_Y+radius;
//            float dist = (screenX-x)*(screenX-x)+(screenY-y)*(screenY-y);
//            if (dist < radius*radius) {
//                pressState = 1;
//                Sound s = SoundController.menuSelectSound();
//                s.play(0.8f);
//            }
//        }
//
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
//        if (pressState == 1) {
//            pressState = 2;
//            return false;
//        }
//        return true;
        return false;
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
        return true;
    }

    // SUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            keyPressed = 1;
            Sound s = SoundController.menuSelectSound();
            s.play(0.8f);
            return false;
        }
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
        if (keycode == Input.Keys.SPACE) {
            keyPressed = 2;
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