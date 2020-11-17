package edu.cornell.gdiac.amaris.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.amaris.GameCanvas;

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
    private boolean canReset;
    private boolean canDraw;
    private boolean canCollide;
    public String type;
    private Vector2 original_vel;
    private boolean animCoord;

    /** Creates the rock object
     *
     * @param x 		Initial x position of the circle center
     * @param y  		Initial y position of the circle center
     * @param r     	The rock's radius
     * @param vel       The velocity of this falling rock */
    public Rock(float x, float y, float r, Vector2 vel, Vector2 sc, String t){
        super(x,y,r);
        this.scale = sc;
        this.velocity = vel;
        this.setPosition(x,y+1.2f);
        //this.position = new Vector2(x,y);
        this.original_pos = new Vector2(x,y+1.2f);
        this.original_vel = new Vector2(vel.x, vel.y);
        canReset = false;
        canDraw = true;
        canCollide = true;
        type = t;

    }

    /**
     * Creates the alpha Body(s) for this object, adding them to the world.
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
        return true;
    }

//    /** @Return the velocity of the falling rock */
//    public Vector2 getVelocity() {
//        return velocity;
//    }
//
//    /** @param vel       Changes the velocity of the rock to vel */
//    public void setVelocity (Vector2 vel) {
//        velocity = vel;
//    }

    public boolean getReset() { return canReset; }

    public void setReset(boolean val) {
        canReset = val;
    }

    public boolean getDraw() { return canDraw; }

    public void setDraw(boolean val) {
        canDraw = val;
    }

    public boolean getCollide() { return canCollide; }

    public void setCollide(boolean val) {
        canCollide = val;
    }

    public boolean getAnimCoord() { return animCoord; }

    public void setAnimCoord(boolean val) {
        animCoord = val;
    }

    public Vector2 getOriginalPos() { return original_pos; }

    public void setOriginalPos(Vector2 pos) { original_pos = pos; }

    public Vector2 getOriginalVel() { return original_vel; }

    public void draw(GameCanvas canvas) {
        if (!canReset & canDraw) {
            Color color = Color.WHITE;
            float sx = 1.5f / (texture.getRegionWidth() / drawScale.x);
            float sy = 1.5f / (texture.getRegionHeight() / drawScale.y);
            canvas.draw(texture, color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), sx, sy);
        }
    }

}
