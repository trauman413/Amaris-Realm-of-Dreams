package edu.cornell.gdiac.alpha.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.alpha.GameCanvas;
import edu.cornell.gdiac.alpha.obstacle.BoxObstacle;
import edu.cornell.gdiac.alpha.platform.PlayerModel;

public class GoalDoor extends BoxObstacle {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public boolean flipped;

    public GoalDoor(float x, float y, float width, float height, Vector2 scale) {
        super(width-4, height-5);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.flipped = false;
    }

    public void setFlipped(boolean value) { flipped = value; }

    @Override
    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        float sx = width / (texture.getRegionWidth() / scale.x) ;
        float sy = height / (texture.getRegionHeight() / scale.y);
        if (flipped) {
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),-sx,sy);

        } else {
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
        }
    }

}
