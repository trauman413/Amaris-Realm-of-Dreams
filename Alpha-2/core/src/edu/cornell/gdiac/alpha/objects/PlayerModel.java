package edu.cornell.gdiac.alpha.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.alpha.*;
import edu.cornell.gdiac.alpha.obstacles.*;

/**
 * Player avatar (Amaris).
 */

public class PlayerModel extends CapsuleObstacle {
    /**
     * The factor to multiply by the input
     */
    private static final float FORCE = 15.0f;
    /**
     * The amount to slow the player down
     */
    private static final float DAMPING = 2.5f;
    /**
     * The maximum character speed
     */
    private static final float PLAYER_MAXSPEED = 5.0f;
    /**
     * The maximum speed the character can fall at
     */
    private static final float PLAYER_MAX_FALL_SPEED = 5.0f;
    /**
     * The impulse for the character to fly
     */
    private static final float DUDE_FLIGHT = 8.0f;
    /**
     * The scale for the character's left/right movement
     */
    private static final float MOVEMENT_SCALE = 80.0f;
    /**
     * The current horizontal movement of the player
     */
    private float movement;
    /**
     * Which direction is the player facing
     */
    private boolean faceRight;
    /**
     * Whether we are actively jumping
     */
    private boolean isJumping;
    /**
     * Whether we actively were jumping
     */
    private boolean wasJumping;
    /**
     * Whether we actively were jumping
     */
    public boolean flightState = false;
    /**
     * Whether we actively going up (in a jump)
     */
    private boolean goingUp;
    /**
     * Whether we are actively jumping
     */
    private boolean isGrounded;
    /**
     * Whether we are actively dashing
     */
    private boolean isDashing;
    /**
     * Whether we are actively flying
     */
    private boolean isFlying;
    /**
     * Whether we are actively transparent
     */
    private boolean isTransparent;
    /**
     * Whether we are actively transparent
     */
    private boolean isTransparencyActive = false;
    /**
     * Whether ability was pressed
     */
    private boolean pressedAbility;
    /**
     * Whether or not to limit player motion
     **/
    public boolean limitPlayerMotion = true;

    /**
     * If Amaris is hit by an obstacle.
     */
    public boolean isHit = false;

    /**
     * Cache for internal force calculations
     */
    private Vector2 forceCache = new Vector2();
    /**
     * Ground sensor to represent our feet
     */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;

    // This is to fit the image to a tigher hitbox
    /**
     * The amount to shrink the body fixture (vertically) relative to the image
     */
    private static final float VSHRINK = 0.28f;
    /**
     * The amount to shrink the body fixture (horizontally) relative to the image
     */
    private static final float HSHRINK = 0.23f;
    /**
     * The amount to shrink the sensor fixture (horizontally) relative to the image
     */
    private static final float SSHRINK = 0.6f;
    /**
     * Height of the sensor attached to the player's feet
     */
    private static final float SENSOR_HEIGHT = 0.05f;
    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private static final String SENSOR_NAME = "PlayerGroundSensor";
    /**
     * The density of the character
     */
    private static final float DENSITY = 1.0f;
    /**
     * Max jump amount of the character
     */
    private int MAX_JUMP_Y = 60;
    /**
     * Gravity of the character
     */
    private float GRAVITY = 0.5f;
    /**
     * Position of ground
     */
    private int GROUND_POS = 0;
    /**
     * Jump speed of the character
     */
    private int JUMP_SPEED = 5;

    private float change_y = 0;

    /**
     * Returns left/right movement of this player.
     *
     * @return left/right movement of this player.
     */
    public float getMovement() {
        return movement * MOVEMENT_SCALE;
    }

    /**
     * Sets left/right movement of this player.
     *
     * @param value left/right movement of this player.
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

    public boolean isHit() { return isHit; }
    public void setHit(boolean hit) {isHit = hit; }

    /**
     * Returns true if the player is actively jumping.
     *
     * @return true if the player is actively jumping.
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * Sets whether the player is actively jumping.
     *
     * @param value whether the player is actively jumping.
     */
    public void setJumping(boolean value) {
        isJumping = value;
        if (!wasJumping && isJumping) {
            goingUp = true;
            wasJumping = true;
        }
    }

    /**
     * Returns true if the player is actively dashing.
     *
     * @return true if the player is actively dashing.
     */
    public boolean isDashing() {
        return isDashing;
    }

    /**
     * Sets whether the player is actively dashing.
     *
     * @param value whether the player is actively dashing.
     */
    public void setDashing(boolean value) {
        isDashing = value;
    }


    /**
     * Returns the name of the ground sensor
     * <p>
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Sets whether the player is actively dashing.
     *
     * @param value whether the player is actively dashing.
     */

    /**
     * Gets whether the player is actively transparent.
     */
    public boolean isTransparencyActive() {
        return isTransparencyActive;
    }

    /**
     * Sets whether the player is actively transparent.
     *
     * @param transparencyActive whether the player is actively transparent.
     */
    public void setTransparencyActive(boolean transparencyActive) {
        isTransparencyActive = transparencyActive;
    }

    /**
     * Returns true if the player is actively double jumping.
     *
     * @return true if the player is actively flying.
     */
    public boolean isFlying() {
        return isFlying;
    }

    /**
     * Sets whether the player is actively flying.
     *
     * @param value whether the player is actively flying.
     */
    public void setFlying(boolean value) {
        isFlying = value;
    }

    /**
     * Returns true if the player is actively transparent.
     *
     * @return true if the player is actively transparent.
     */
    public boolean isTransparent() {
        return isTransparent;
    }

    /**
     * Sets whether the player is actively transparent.
     *
     * @param value whether the player is actively transparent.
     */
    public void setTransparent(boolean value) {
        isTransparent = value;
    }

    /**
     * Returns how much force to apply to get the player moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the player moving
     */
    public float getForce() {
        if (isDashing()) {
            return FORCE * 40f;
        }
        return FORCE;
    }

    /**
     * Returns how hard the brakes are applied to get a player to stop moving
     *
     * @return how hard the brakes are applied to get a player to stop moving
     */
    public float getDamping() {
        return DAMPING;
    }

//    public Vector2 getPosition() { return position; }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }
    public boolean getGrounded() { return isGrounded; }

    /**
     * Returns the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        if (isDashing()) {
            return 15 * PLAYER_MAXSPEED;
        }
        if (!limitPlayerMotion) {
            return 5 * PLAYER_MAXSPEED;
        }
        return PLAYER_MAXSPEED;
    }

    /**
     * Returns the upper limit on dude up-down movement.
     * <p>
     * This does NOT apply to horizontal movement.fx
     *
     * @return the upper limit on dude up-down movement.
     */
    public float getMaxFallSpeed() {
        return PLAYER_MAX_FALL_SPEED;
    }

    /**
     * Creates a new player at the given position and with the
     * given width and height.
     *
     * @param x      Initial x position of the player center
     * @param y      Initial y position of the player center
     * @param width  The player width
     * @param height The player height
     */
    public PlayerModel(float x, float y, float width, float height) {
        super(x, y, width * HSHRINK, height * VSHRINK);
        //setDensity(DENSITY);
        //setFriction(0);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

//        position = new Vector2(x, y);
//        this.width = width;
//        this.height = height;

        // Gameplay attributes
        isGrounded = false;
        isJumping = false;
        isFlying = false;
        isDashing = false;
        isTransparent = false;
        isTransparencyActive = false;
        faceRight = true;

        setName("Amaris");
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
//
//        // Ground Sensor
//        // -------------
//        // We only allow the dude to jump when he's on the ground.
//        // Double jumping is not allowed.
//        //
//        // To determine whether or not the dude is on the ground,
//        // we create a thin sensor under his feet, which reports
//        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -77f / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(SSHRINK * 0.73f / 2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(SENSOR_NAME);

        return true;
    }

    /**
     * Updates the player's state
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
    }

    /**
     * Applies the force to the body of this player
     * <p>
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        // check if doing movement here
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f && !isDashing()) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        if (!isFlying() && Math.abs(getVX()) < getMaxSpeed()) {
            forceCache.set(getMovement(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Jump!
        if (isJumping() && isGrounded) {
            //TODO: change 8f to be a constant
            forceCache.set(0, 8f);
            body.applyLinearImpulse(forceCache, getPosition(), true);
        }

        if (isFlying()) {
            forceCache.set(0, 15.0f);
            body.applyLinearImpulse(forceCache, getPosition(), true);
        }

        // Dash!
        if (isDashing()) {
            this.setVY(0f);
            float force = getForce();
            if (!faceRight) force *= -1;
            forceCache.set(force, 0);
            body.applyLinearImpulse(forceCache, getPosition(), true);
        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw (GameCanvas canvas){
        float effect = faceRight ? 1.0f : -1.0f;
        Color color = Color.WHITE;
        canvas.draw(texture, color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect, 1.0f);
    }
}
