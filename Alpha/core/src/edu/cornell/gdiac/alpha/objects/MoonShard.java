package edu.cornell.gdiac.alpha.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.alpha.GameCanvas;
import edu.cornell.gdiac.alpha.obstacle.BoxObstacle;

public class MoonShard extends BoxObstacle {
    //Class attributes
    /** Whether or not the player has claimed the moon shard yet */
    private boolean claimed;
    public Vector2 originalPosition;
    public float horizontalRadius;
    public float verticalRadius;
    public Vector2 scale;

    //Constants
    /** Width */
    protected float MOONSHARD_WIDTH = 50;
    /** Height */
    protected float MOONSHARD_HEIGHT = 50;

    public MoonShard(float x, float y, Vector2 velocity,
                     float horizontalRadius, float verticalRadius, Vector2 scale) {
        super(x, y, 2, 2);
        this.setPosition(x, y);
        this.scale = scale;
        this.width = 2;
        this.height = 2;
        this.velocity = velocity;
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.originalPosition = new Vector2(x, y);
    }


    /** Creates a new moon shard at the given location.
     *
     * @param x The x-position of the fountain.
     * @param y The y-position of the fountain.
     */
    public MoonShard(float x, float y, Vector2 scale) {
        super(x,y,2, 2); //guess-stimates
        this.setPosition(x, y);
        this.scale = scale;
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0.0f);
        setFriction(0.0f);
        setRestitution(0.0f);
        setSensor(true);
        claimed = false;
    }

    /** Whether or not the moon shard has been claimed */
    public boolean isTaken() { return claimed; }
    /** Sets whether or not a moon shard has been taken */
    public void setTaken(boolean claim) {
        claimed= claim;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public float getHorizontalRadius() {
        return horizontalRadius;
    }

    public void setHorizontalRadius(float horizontalRadius) {
        this.horizontalRadius = horizontalRadius;
    }

    public float getVerticalRadius() {
        return verticalRadius;
    }

    public void setVerticalRadius(float verticalRadius) {
        this.verticalRadius = verticalRadius;
    }

    public Vector2 getOriginalPosition(){
        return originalPosition;
    }

    public void setOriginalPosition(Vector2 originalPosition){
        this.originalPosition = originalPosition;
    }

    public void setBodyNull() {
        body = null;
    }
    /**
     * Creates the alpha Body(s) for this object, adding them to the world.
     *
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
     * Draws the alpha object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        Color color = Color.WHITE;
        if (!claimed) {
            float sx = width / (texture.getRegionWidth() / scale.x);
            float sy = height / (texture.getRegionHeight() / scale.y);
            canvas.draw(texture, color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), sx, sy);
        }
    }
}
