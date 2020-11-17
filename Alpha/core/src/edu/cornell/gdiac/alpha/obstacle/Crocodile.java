package edu.cornell.gdiac.alpha.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.alpha.GameCanvas;

public class Crocodile extends BoxObstacle {
    private Vector2 position;
    private Vector2 scale;
    private float horizontalRadius;
    private float verticalRadius;
    private Vector2 originalPosition;
    private boolean faceRight;

    public Crocodile(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                     float horizontalRadius, float verticalRadius) {
        super(width*0.7f, height);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.velocity = velocity;
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.originalPosition = new Vector2(x, y);
        this.faceRight = true;
    }

    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        //float sx = width * 1.5f/ (texture.getRegionWidth() / scale.x) ;
        float effect = faceRight ? -1f : 1f;
        //float sy = height * 1.5f/ (texture.getRegionHeight() / scale.y);
        canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1f);
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

    public void setFaceRight(boolean dir) { faceRight = dir; }
    public boolean getFaceRight() { return faceRight; }
}
