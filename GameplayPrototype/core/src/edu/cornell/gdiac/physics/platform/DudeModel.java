/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class DudeModel extends CapsuleObstacle {
	// Physics constants
	/** The density of the character */
	private static final float DUDE_DENSITY = 1.0f;
	/** The factor to multiply by the input */
	private static final float DUDE_FORCE = 20.0f;
	/** The amount to slow the character down */
	private static final float DUDE_DAMPING = 10.0f; 
	/** The dude is a slippery one */
	private static final float DUDE_FRICTION = 0.0f; 
	/** The maximum character speed */
	private static final float DUDE_MAXSPEED = 5.0f;
    /** The maximum speed the character can fall at */
    private static final float DUDE_MAX_FALL_SPEED = 11.0f;
	/** The impulse for the character jump */
	private static final float DUDE_JUMP = 5.5f;
	/** The impulse for the character to fly */
	private static final float DUDE_FLIGHT = 8.0f;
	/** Cooldown (in animation frames) for ability */
	private static final int ABILITY_COOLDOWN = 100;
	/** Cooldown (in animation frames) for jumping */
	private static final int JUMP_COOLDOWN = 40;
	/** Ability timer */
	private static final int ABILITY_TIMER = 300;
	/** Height of the sensor attached to the player's feet */
	private static final float SENSOR_HEIGHT = 0.05f;
	/** Identifier to allow us to track the sensor in ContactListener */
	private static final String SENSOR_NAME = "DudeGroundSensor";

	// This is to fit the image to a tigher hitbox
	/** The amount to shrink the body fixture (vertically) relative to the image */
	private static final float DUDE_VSHRINK = 0.95f;
	/** The amount to shrink the body fixture (horizontally) relative to the image */
	private static final float DUDE_HSHRINK = 0.7f;
	/** The amount to shrink the sensor fixture (horizontally) relative to the image */
	private static final float DUDE_SSHRINK = 0.6f;
	
	/** The current horizontal movement of the character */
	private float   movement;
	/** Which direction is the character facing */
	private boolean faceRight;
	/** How long until we can jump again */
	private int jumpCooldown;
	/** How long until we can fly again */
	private int flightCooldown;
	/** How long until we can start flight again*/
	private int flightTimer;
	/** Whether we are actively jumping */
	private boolean isJumping;
	/** How long until we can shoot again */
	private int shootCooldown;
	/** Whether our feet are on the ground */
	private int dashDuration;
	/** Whether we are actively jumping */
	private boolean isGrounded;
	/** Whether we are actively shooting */
	private boolean isShooting;
	/** Whether we are actively dashing */
	private boolean isDashing;
	/** Whether we were actively dashing */
	private boolean dashingPrev;
	/** Whether we are actively flying */
	private boolean isFlying;
	/** Whether we are starting flight */
	private boolean isStartingFlight;
	/** Whether we are actively transparent */
	private boolean isTransparent;
	/** Whether we are actively transparent */
	private boolean isTransparencyActive = false;
	/** How long until we can use transparent again*/
	private int abilityCoolDown;
	/** How long until transparency ability runs out*/
	private int transparencyTimer;
	/** How long until we can use transparent again*/
	private int dashingCoolDown;
	/** How long until transparency ability runs out*/
	private int dashingTimer;
	/** Whether ability was pressed */
	private boolean pressedAbility;
	/** Whether or not to limit player motion **/
	public boolean limitPlayerMotion = true;

	private int abilityTimer;
	private int abilityCooldown;
	private int prevAbilityTimer;

	private boolean timerEnded;
	/** Ground sensor to represent our feet */
	private Fixture sensorFixture;
	private PolygonShape sensorShape;
	
	/** Cache for internal force calculations */
	private Vector2 forceCache = new Vector2();

	/**
	 * Returns left/right movement of this character.
	 * 
	 * This is the result of input times dude force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}
	
	/**
	 * Sets left/right movement of this character.
	 * 
	 * This is the result of input times dude force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		movement = value; 
		// Change facing if appropriate
		if (movement < 0) {
			faceRight = false;
		} else if (movement > 0) {
			faceRight = true;
		}
	}
	
	/**
	 * Returns true if the dude is actively firing.
	 *
	 * @return true if the dude is actively firing.
	 */
	public boolean isShooting() {
		return isShooting && shootCooldown <= 0;
	}
	
	/**
	 * Sets whether the dude is actively firing.
	 *
	 * @param value whether the dude is actively firing.
	 */
	public void setShooting(boolean value) {
		isShooting = value; 
	}

	/**
	 * Returns true if the dude is actively jumping.
	 *
	 * @return true if the dude is actively jumping.
	 */
	public boolean isJumping() {
		return isJumping && isGrounded && jumpCooldown <= 0;
	}
	
	/**
	 * Sets whether the dude is actively jumping.
	 *
	 * @param value whether the dude is actively jumping.
	 */
	public void setJumping(boolean value) {
		isJumping = value; 
	}

	/**
	 * Returns true if the dude is on the ground.
	 *
	 * @return true if the dude is on the ground.
	 */
	public boolean isGrounded() {
		return isGrounded;
	}
	
	/**
	 * Sets whether the dude is on the ground.
	 *
	 * @param value whether the dude is on the ground.
	 */
	public void setGrounded(boolean value) {
		isGrounded = value; 
	}

	/**
	 * Returns true if the dude is actively dashing.
	 *
	 * @return true if the dude is actively dashing.
	 */
	public boolean isDashing() {
		return isDashing && !dashingPrev;
	}

	/**
	 * Sets whether the dude is actively dashing.
	 *
	 * @param value whether the dude is actively dashing.
	 */
	public void setDashing(boolean value) {
		isDashing = value;
	}


	public int getAbilityCooldown() {
		return abilityCoolDown;
	}
	/**
	 * Sets whether the dude is actively dashing.
	 *
	 * @param value whether the dude is actively dashing.
	 */
	public void setDashDuration(int value) {
		dashDuration = value;
	}

	/**
	 * Sets whether the dude is actively dashing.
	 *
	 * @param value whether the dude is actively dashing.
	 */

	/**
	 * Gets whether the dude is actively transparent.
	 */
	public boolean isTransparencyActive() {
		return isTransparencyActive;
	}

	/**
	 * Sets whether the dude is actively transparent.
	 *
	 * @param transparencyActive whether the dude is actively transparent.
	 */
	public void setTransparencyActive(boolean transparencyActive) {
		isTransparencyActive = transparencyActive;
	}

	/**
	 * Returns true if the dude is actively double jumping.
	 *
	 * @return true if the dude is actively flying.
	 */
	public boolean isFlying() {return isFlying && isGrounded; }

	/**
	 * Sets whether the dude is actively flying.
	 *
	 * @param value whether the dude is actively flying.
	 */
	public void setFlying(boolean value) { isFlying = value; }

	/**
	 * Returns true if the dude is actively transparent.
	 *
	 * @return true if the dude is actively transparent.
	 */
	public boolean isTransparent() {
		return isTransparent;
	}

	/**
	 * Sets whether the dude is actively transparent.
	 *
	 * @param value whether the dude is actively transparent.
	 */
	public void setTransparent(boolean value) {
		isTransparent = value;
	}

	/**
	 * Returns how much force to apply to get the dude moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the dude moving
	 */
	public float getForce() {
		if (isDashing()) {
			return DUDE_FORCE * 10f;
		}
		return DUDE_FORCE;
	}

	/**
	 * Returns ow hard the brakes are applied to get a dude to stop moving
	 *
	 * @return ow hard the brakes are applied to get a dude to stop moving
	 */
	public float getDamping() {
		return DUDE_DAMPING;
	}
	
	/**
	 * Returns the upper limit on dude left-right movement.  
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on dude left-right movement.  
	 */
	public float getMaxSpeed() {
		if (isDashing()) {
			return 15*DUDE_MAXSPEED;
		}
		if (!limitPlayerMotion){
			return 5*DUDE_MAXSPEED;
		}
		return DUDE_MAXSPEED;
	}

	public int getAbilityTimer() {
        return abilityTimer;
    }

	public void setAbilityTimer(int time) {
		abilityTimer = time;
	}

    /**
     * Returns the upper limit on dude up-down movement.
     *
     * This does NOT apply to horizontal movement.fx
     *
     * @return the upper limit on dude up-down movement.
     */
    public float getMaxFallSpeed() {
        return DUDE_MAX_FALL_SPEED;
    }

	public int getTransparencyTimer() {
		return transparencyTimer;
	}

	public void setPressedAbility(boolean pt) { pressedAbility = pt; }

	/**
	 * Returns the name of the ground sensor
	 *
	 * This is used by ContactListener
	 *
	 * @return the name of the ground sensor
	 */
	public String getSensorName() { 
		return SENSOR_NAME;
	}

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	public boolean isTimerDone() {
		boolean done =  abilityTimer == 0 && prevAbilityTimer > 0;
		if (done) prevAbilityTimer = 0;
		//System.out.println("" + done);
		return done;
	}
	/**
	 * Creates a new dude at the origin.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public DudeModel(float width, float height) {
		this(0,0,width,height);
	}

	/**
	 * Creates a new dude avatar at the given position.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param x  		Initial x position of the avatar center
	 * @param y  		Initial y position of the avatar center
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public DudeModel(float x, float y, float width, float height) {
		super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(DUDE_DENSITY);
		setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);
		
		// Gameplay attributes
		isGrounded = false;
		isShooting = false;
		isJumping = false;
		isTransparent = false;
		pressedAbility = false;
		faceRight = true;

		abilityTimer = 0;
		abilityCooldown = 0;

		shootCooldown = 0;
		jumpCooldown = 0;
		dashDuration = 300;
		flightCooldown = 0;
		prevAbilityTimer = 0;


		shootCooldown = 0;
		jumpCooldown = 0;
		abilityCoolDown = 0;
		transparencyTimer = 0;

		setName("dude");
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// create the box from our superclass
		if (!super.activatePhysics(world)) {
			return false;
		}

		// Ground Sensor
		// -------------
		// We only allow the dude to jump when he's on the ground. 
		// Double jumping is not allowed.
		//
		// To determine whether or not the dude is on the ground, 
		// we create a thin sensor under his feet, which reports 
		// collisions with the world but has no collision response.
		Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = DUDE_DENSITY;
		sensorDef.isSensor = true;
		sensorShape = new PolygonShape();
		sensorShape.setAsBox(DUDE_SSHRINK*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
		sensorDef.shape = sensorShape;
		
		sensorFixture = body.createFixture(sensorDef);
		sensorFixture.setUserData(getSensorName());
		
		return true;
	}
	

	/**
	 * Applies the force to the body of this dude
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}
		
		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f ) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		// Velocity too high, clamp it
		if (Math.abs(getVX()) >= getMaxSpeed() && limitPlayerMotion) {
			setVX(Math.signum(getVX())*getMaxSpeed());
		} else if (!isFlying()) {
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVY()) >= getMaxFallSpeed() && limitPlayerMotion){
			setVY(Math.signum(getVY())*getMaxFallSpeed());
		}

		// Jump!
		if (isJumping()) {
			forceCache.set(0, DUDE_JUMP);
			body.applyLinearImpulse(forceCache,getPosition(),true);
		}

		// Flight!
		if (isFlying() && abilityTimer > 0) {
			forceCache.set(0, DUDE_FLIGHT);
			body.applyLinearImpulse(forceCache,getPosition(),true);
		}

		// Dash!
		if (isDashing() && abilityTimer >0) {
		    this.setVY(0f);
			float force = getForce();
			if (!faceRight) force *= -1;
			forceCache.set(force, 0);
			body.applyLinearImpulse(forceCache,getPosition(),true);
		}
	}
	
	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Apply cooldowns
		if (isJumping()) {
			jumpCooldown = JUMP_COOLDOWN;
		} else {
			jumpCooldown = Math.max(0, jumpCooldown - 1);
		}

		if (abilityTimer > 0) {
			prevAbilityTimer = abilityTimer;
			abilityTimer = Math.max(0, abilityTimer - 1);
		}
		if (pressedAbility && abilityTimer == 0) {
			pressedAbility = false;
			abilityTimer = ABILITY_TIMER;
		}
		dashingPrev = isDashing;
//		else {
//			if (abilityTimer == 0) {
//				abilityCooldown = ABILITY_COOLDOWN;
//			}
//			abilityCooldown = Math.max(0, abilityCooldown - 1);
//		}

		if (isShooting()) {
			shootCooldown = ABILITY_COOLDOWN;
		} else {
			shootCooldown = Math.max(0, shootCooldown - 1);
		}

		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		float effect = faceRight ? 1.0f : -1.0f;
		Color color = Color.WHITE;
		if (isTransparent() && abilityTimer > 0) {
			color = new Color(1, 1, 1, 0.5f);
			isTransparencyActive = true;
		} else {
			isTransparencyActive = false;
		}
		canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
	}
	
	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		super.drawDebug(canvas);
		canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
	}

	public void giveFreeMovement(long milliseconds){
		limitPlayerMotion = false;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				limitPlayerMotion = true;
			}
		}, milliseconds);
	}

}