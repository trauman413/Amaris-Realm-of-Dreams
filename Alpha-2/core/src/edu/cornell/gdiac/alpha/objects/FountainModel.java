package edu.cornell.gdiac.alpha.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.alpha.*;
import edu.cornell.gdiac.alpha.obstacles.BoxObstacle;

/**
 * Fountain class.
 */
public class FountainModel extends BoxObstacle {

    /** Enums representing the different types of abilities obtained
     * by fountains.
     */
    public enum FountainType {
        /** A fountain that gives Dash. */
        DASH,
        /** A fountain that gives Flight. */
        FLIGHT,
        /** A fountain that gives Transparency. */
        TRANSPARENCY,
        /** A fountain that restores player's Serenity. Cannot be obtained as an ability. */
        RESTORE
    }

    //Class attributes
    /** The type of fountain */
    protected FountainType fountainType;
    /** Whether or not the fountain can grant an ability. Based upon whether the
     * ability is in the queue and has not been used yet */
    protected boolean isAvailable;
    /* Fountain width */
    protected float FOUNTAIN_WIDTH = 100;
    /* Fountain height */
    protected float FOUNTAIN_HEIGHT = 100;
    //probably more

    //Getters
    /** Returns the fountain type of the fountain.
     *
     * @return what type of fountain it is.
     */
    public FountainType getFountainType() { return fountainType; }

    /** Returns whether or not the fountain can actually grant an ability or serenity.
     *
     * @return if the fountain is available for use or not (a bool).
     */
    public boolean isAvailable() { return isAvailable; }

    /** Sets whether or not the fountain can be accessed.
     *
     * @param active a boolean representing if an ability can be obtained from the fountain.
     */
    public void setAvailable(boolean active) {isAvailable = active; }

    //Constructor
    /** Creates a new fountain at the given location with the specified fountain type.
     *
     * @param x The x-position of the fountain.
     * @param y The y-position of the fountain.
     * @param fountain The type of fountain being created.
     */
    public FountainModel(float x, float y, FountainType fountain) {
        super(x,y,50, 40);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0.0f);
        setFriction(0.0f);
        setRestitution(0.0f);
        setSensor(true);
        fountainType = fountain;
        isAvailable = true;
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

        return true;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        Color color = Color.WHITE;
        if(!isAvailable) {
            color = new Color(1,1,1,.5f);
        }
        canvas.draw(texture, color, getX()-30, getY(), FOUNTAIN_WIDTH, FOUNTAIN_HEIGHT);
    }
}
