package edu.cornell.gdiac.techprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Class for reading player input.
 *
 * This supports a keyboard controller.
 */
public class InputController {
    /** The singleton instance of the input controller */
    private static InputController theController = null;

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    // Fields to manage buttons
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    /** Whether the primary action button was pressed. */
    private boolean primePressed;
    private boolean primePrevious;
    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;
    /** Whether the use ability button was pressed. */
    private boolean abilityPressed;
    private boolean abilityPrevious;
    /** Whether the activate ability button was pressed. */
    private boolean activateAbilityPressed;
    /** Whether the map button was pressed. */
    private boolean mapPressed;

    //TEMPORARY VARIABLES:
    private boolean dashPressed;
    private boolean flightPressed;
    private boolean transparencyPressed;

    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;

    /**
     * Returns the amount of sideways movement.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * Returns true if the primary action button was pressed.
     *
     * @return true if the primary action button was pressed.
     */
    public boolean didPrimary() {
        return primePressed;
    }

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

    /**
     * Returns true if the activate ability button was pressed.
     *
     * @return true if the activate ability button was pressed.
     */
    public boolean didActivateAbility() { return activateAbilityPressed; }

    /**
     * Returns true if the use ability button was pressed.
     *
     * @return true if the use ability button was pressed.
     */
    public boolean didAbility() { return abilityPressed && !abilityPrevious; }

    /**
     * Returns true if the map button was pressed.
     *
     * @return true if the map button was pressed.
     */
    public boolean didMap() { return mapPressed; }

    //TODO: delete me
    public boolean didDash() { return dashPressed; }
    public boolean didFlight() { return flightPressed; }
    public boolean didTransparency() { return transparencyPressed; }

    /**
     * Reads the input for the player and converts the result into game logic.
     */
    public void readInput() {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        primePrevious  = primePressed;
        resetPrevious  = resetPressed;
        exitPrevious = exitPressed;
        abilityPrevious = abilityPressed;

        readKeyboard(false);
    }

    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard(boolean secondary) {
        // Give priority to gamepad results
        resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
        primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP));
        exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
        activateAbilityPressed =  Gdx.input.isKeyPressed(Input.Keys.SPACE);
        abilityPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        mapPressed = Gdx.input.isKeyPressed(Input.Keys.Z);

        //TEMPORARY TESTING VALUES: TODO PLEASE DELETE ME
        dashPressed = Gdx.input.isKeyPressed(Input.Keys.D);
        flightPressed = Gdx.input.isKeyPressed(Input.Keys.F);
        transparencyPressed = Gdx.input.isKeyPressed(Input.Keys.T);

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }

        vertical = (secondary ? vertical : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
    }
}