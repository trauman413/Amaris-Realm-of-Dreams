package edu.cornell.gdiac.alpha.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.alpha.GameCanvas;
import edu.cornell.gdiac.alpha.obstacles.CircularObstacle;
/**
 * Rock class.
 */
public class Rock extends CircularObstacle{

    /** the velocity of the falling rock */
    private Vector2 velocity;
    /** The current position of the falling rock */
    //private Vector2 position;
    public Vector2 scale;
    /** The original position of the falling rock */
    private Vector2 original_pos;
    private boolean isGrounded;
    private static final float DENSITY = 1.0f;
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    /**
     * The amount to shrink the sensor fixture (horizontally) relative to the image
     */
    private static final float SSHRINK = 0.5f;
    /**
     * Height of the sensor attached to the rock
     */
    private static final float SENSOR_HEIGHT = 0.01f;
    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private static final String SENSOR_NAME = "RockGroundSensor";

    /** Creates the rock object
     *
     * @param x 		Initial x position of the circle center
     * @param y  		Initial y position of the circle center
     * @param r     	The rock's radius
     * @param vel       The velocity of this falling rock */
    public Rock(float x, float y, float r, Vector2 vel, Vector2 sc){
        super(x,y,r);
        this.scale = sc;
        this.velocity = vel;
        this.setPosition(x,y);
        //this.position = new Vector2(x,y);
        this.original_pos = new Vector2(x,y);
        isGrounded = false;

    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     *
     * @param world Box2D world to store body
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
        Vector2 sensorCenter = new Vector2(0, -getRadius() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(SSHRINK * getRadius() / 2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(SENSOR_NAME);

        return true;
    }

    public void setGrounded(boolean value) {
        isGrounded = value;
    }
    public boolean getGrounded() { return isGrounded; }
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

    /** @Return the velocity of the falling rock */
    public Vector2 getVelocity() {
        return velocity;
    }

    /** @param vel       Changes the velocity of the rock to vel */
    public void setVelocity (Vector2 vel) {
        velocity = vel;
    }

    public Vector2 getOriginalPos() { return original_pos; }

    public void draw(GameCanvas canvas) {
        Color color = Color.WHITE;
        canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX(),getY(),getAngle(),1,1);

    }

}
