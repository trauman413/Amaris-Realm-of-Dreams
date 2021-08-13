package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.Timer;
import java.util.TimerTask;

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
	private boolean activateFlightAbilityPressed;
	private boolean activateDashAbilityPressed;
	/** Whether the map button was pressed. */
	private boolean mapPressed;
	private boolean mapPrevious;
	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the pause button was pressed. */
	private boolean pausePressed;
	private boolean pausePrevious;
	/** Whether the nextt button was pressed. */
	private boolean nextPressed;
	private boolean walkPressed;
	private int flyCount = 0;
	/** The time of the last jump */
	private long lastJump = System.currentTimeMillis();
	/** The time of the last jump */
	private long doubleJumpRange = 500;
	/** The time of the last jump */
	private boolean didDoubleJump;
	private boolean jumpReleased = false;
	private boolean firstJumpPressed = false;
	private boolean isCooldownDone = true;
	private float doubleJumpCooldown = 1.0f;
	/** The time of the last side press */
	private long lastDash = System.currentTimeMillis();
	/** The time of the last side press */
	private long doubleDashRange = 500;
	/** The time of the last side press */
	private boolean didDoubleDash;
	private boolean sideReleased = false;
	private boolean firstSidePressed = false;
	private boolean isDashCooldownDone = true;
	private float doubleDashCooldown = 1f;
	private float lastHorizontal = 0f;



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
	 * Returns true if the pause button was pressed.
	 *
	 * @return true if the pause button was pressed.
	 */
	public boolean didPause() {return pausePressed && !pausePrevious; }

	/**
	 * Returns true if the next button was pressed.
	 *
	 * @return true if the next button was pressed.
	 */
	public boolean didNext() {return nextPressed; }

	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}

	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return false;
	}

	/**
	 * Returns true if the activate ability button was pressed.
	 *
	 * @return true if the activate ability button was pressed.
	 */
	public boolean didActivateAbility() {
		return activateAbilityPressed;
	}

	/**
	 * Returns true if the use ability button was pressed.
	 *
	 * @return true if the use ability button was pressed.
	 */
	public boolean didAbility() { return abilityPressed && !abilityPrevious; }

	/**
	 * Returns true if the player double jumped.
	 *
	 * @return true if the player double jumped.
	 */
	public boolean didDoubleJump() { return didDoubleJump; }

	/**
	 * Returns true if the player double dashed.
	 *
	 * @return true if the player double dashed.
	 */
	public boolean didDoubleDash() { return didDoubleDash; }

	/**
	 * Returns true if the map button was pressed.
	 *
	 * @return true if the map button was pressed.
	 */
	public boolean didMap() { return mapPressed && !mapPrevious; }

	public boolean didWalk() { return walkPressed; }

	/**
	 * Reads the input for the player and converts the result into game logic.
	 */
	public void readInput() {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		primePrevious  = primePressed;
		resetPrevious  = resetPressed;
		exitPrevious = exitPressed;
		debugPrevious  = debugPressed;
		abilityPrevious = abilityPressed;
		pausePrevious = pausePressed;
		mapPrevious = mapPressed;

		readKeyboard(false);
	}

	public boolean activateFlightAbility() {
		return flyCount > 1;
	}

	public void resetDoubleJump(){
		jumpReleased = false;
		firstJumpPressed = false;
	}

	private static Timer doubleJumpTimer = new Timer();

	public void resetDoubleDash(){
		sideReleased = false;
		firstSidePressed = false;
		lastHorizontal = 0f;
	}

	private static Timer doubleDashTimer = new Timer();
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
		/*if(!Gdx.input.isKeyPressed(Input.Keys.UP)) {
			flyCount = 0;
		}*/
		debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.Z));
		resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP) ||
				(Gdx.input.isKeyPressed(Input.Keys.W)));
//		exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		activateAbilityPressed =  Gdx.input.isKeyPressed(Input.Keys.SPACE);
//		activateFlightAbilityPressed = activateFlightAbility();
		abilityPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

		mapPressed = Gdx.input.isKeyPressed(Input.Keys.M);
		pausePressed = Gdx.input.isKeyPressed(Input.Keys.P);
		nextPressed = Gdx.input.isKeyPressed(Input.Keys.N);


//		//TEMPORARY TESTING VALUES: TODO PLEASE DELETE ME
//		dashPressed = Gdx.input.isKeyPressed(Input.Keys.D);
//		flightPressed = Gdx.input.isKeyPressed(Input.Keys.F);
//		transparencyPressed = Gdx.input.isKeyPressed(Input.Keys.T);

		// Directional controls
		horizontal = (secondary ? horizontal : 0.0f);
		walkPressed = (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) ||
				Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A));
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || (Gdx.input.isKeyPressed(Input.Keys.D))) {
			horizontal += 1.0f;

		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || (Gdx.input.isKeyPressed(Input.Keys.A))) {
			horizontal -= 1.0f;
		}

		vertical = (secondary ? vertical : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.UP) || (Gdx.input.isKeyPressed(Input.Keys.W))) {
			vertical += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || (Gdx.input.isKeyPressed(Input.Keys.S))) {
			vertical -= 1.0f;
		}

		//Double jump logic
		didDoubleJump = ((Gdx.input.isKeyPressed(Input.Keys.UP)||(Gdx.input.isKeyPressed(Input.Keys.W))) && (System.currentTimeMillis() - lastJump < doubleJumpRange)
				&& jumpReleased && firstJumpPressed && isCooldownDone);
		if((Gdx.input.isKeyPressed(Input.Keys.UP)||(Gdx.input.isKeyPressed(Input.Keys.W))) && !jumpReleased && !firstJumpPressed) {
			lastJump = System.currentTimeMillis();
			firstJumpPressed = true;
		}
		else if (!((Gdx.input.isKeyPressed(Input.Keys.UP)||(Gdx.input.isKeyPressed(Input.Keys.W)))) && !jumpReleased && firstJumpPressed){
			jumpReleased = true;
		}
		if(System.currentTimeMillis() - lastJump > doubleJumpRange && firstJumpPressed){
			resetDoubleJump();
		}
		if(didDoubleJump) {
			//System.out.println("DOUBLE JUMP");
			resetDoubleJump();
//			if (!isCooldownDone){
//				doubleJumpTimer.cancel();
//				doubleJumpTimer = new Timer();
//			}
			isCooldownDone = false;
			doubleJumpTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					isCooldownDone = true;
				}
			}, (long)doubleJumpCooldown * 1000);
		}

		float lastHorizontalCache = lastHorizontal;

		//Double dash logic
		didDoubleDash = (
				(((Gdx.input.isKeyPressed(Input.Keys.LEFT)|| Gdx.input.isKeyPressed(Input.Keys.A)) && lastHorizontal == -1) ||
						((Gdx.input.isKeyPressed(Input.Keys.RIGHT)
						 || Gdx.input.isKeyPressed(Input.Keys.D)) && lastHorizontal == 1))
						&& (System.currentTimeMillis() - lastDash < doubleDashRange)
				&& sideReleased && firstSidePressed && isDashCooldownDone && !AbilityController.getInstance().isApplyingDash());

		if(((Gdx.input.isKeyPressed(Input.Keys.LEFT)|| Gdx.input.isKeyPressed(Input.Keys.A))) ||
				((Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)))
				&& !sideReleased && !firstSidePressed && isDashCooldownDone) {

			if(((Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) ||
					(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)))
					&&
					!((Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) &&
							(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)))
			){
				if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
					if(lastHorizontal != -1){
						lastDash = System.currentTimeMillis();
						sideReleased = false;
					}
					if(lastHorizontal == 1){
						lastHorizontal = 0;
					} else {
						lastHorizontal = -1;
					}
				}
				else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)){
					if(lastHorizontal != 1){
						lastDash = System.currentTimeMillis();
						sideReleased = false;
					}
					if(lastHorizontal == -1){
						lastHorizontal = 0;
					} else {
						lastHorizontal = 1;
					}
				}
			}
			firstSidePressed = true;
		}
		if (!(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D))
				&& !sideReleased && firstSidePressed && isDashCooldownDone){
			sideReleased = true;
		}

		if(System.currentTimeMillis() - lastDash > doubleDashRange){
			resetDoubleDash();
		}
		if(didDoubleDash) {
			//System.out.println("DOUBLE DASH");
			resetDoubleDash();
			isDashCooldownDone = false;
			doubleDashTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					isDashCooldownDone = true;
				}
			}, (long)doubleDashCooldown * 1000);
		}
	}
}