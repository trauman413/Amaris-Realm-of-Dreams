package edu.cornell.gdiac.techprototype.obstacles;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.techprototype.GameCanvas;
import edu.cornell.gdiac.techprototype.obstacles.CircularObstacle;
/**
 * Rock class.
 */
public class Rock extends CircularObstacle {

    /** the velocity of the falling rock */
    private Vector2 velocity;

    /** the position of the falling rock */
    private Vector2 position;

    /** Creates the rock object
     *
     * @param x 		Initial x position of the circle center
     * @param y  		Initial y position of the circle center
     * @param r     	The rock's radius
     * @param vel       The velocity of this falling rock */
    public Rock(float x, float y, float r, Vector2 vel){
        super(x,y,r);
        this.velocity = vel;
        this.position = new Vector2(x,y);
    }

    /** @Return the velocity of the falling rock */
    public Vector2 getVelocity() {
        return velocity;
    }

    /** @param vel       Changes the velocity of the rock to vel */
    public void setVelocity (Vector2 vel) {
        velocity = vel;
    }

    /** @Return the position of the falling rock */
    public Vector2 getPosition() {
        return position;
    }

    /** @param pos       Changes the velocity of the rock to vel */
    public void setPosition (Vector2 pos) {
        position = pos;
    }

    /**TODO: finish */
    public void draw(GameCanvas canvas) {

    }

}
