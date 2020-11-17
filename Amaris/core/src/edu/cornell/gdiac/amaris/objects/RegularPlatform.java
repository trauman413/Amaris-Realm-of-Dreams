package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.GameCanvas;
import edu.cornell.gdiac.amaris.obstacle.BoxObstacle;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

public class RegularPlatform extends BoxObstacle implements Platform {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public Vector2 originalPosition;
    public float horizontalRadius;
    public float verticalRadius;
    public String type;
    private int stepCount;
    private boolean onWindow;


    public RegularPlatform(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                         float horizontalRadius, float verticalRadius) {
        super(width, height);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.velocity = velocity;
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.originalPosition = new Vector2(x, y);
        this.type = "regular";
    }

    //ONLY USE FOR WINDOW
    public RegularPlatform(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                           float horizontalRadius, float verticalRadius, String t) {
        this(x,y+4,width,height,scale,velocity,horizontalRadius,verticalRadius);
        this.type = t;
        stepCount = 0;
        onWindow = false;
    }

    @Override
    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        if (texture != null) {
            float sx = width / (texture.getRegionWidth() / scale.x) ;
            float sy = height / (texture.getRegionHeight() / scale.y);
            if (type.equals("window")) {
                if(stepCount < 3) {
                    canvas.draw(texture, color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), sx, sy);
                }
            }
            else {
                canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
            }

        }
    }

    public boolean getOnWindow() {
        return onWindow;
    }

    public void setOnWindow(boolean w) {
        onWindow = w;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int step) {
        stepCount = step;
    }

    public void incrementStepCount() {
        stepCount++;
    }


    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    @Override
    public float getHorizontalRadius() {
        return horizontalRadius;
    }

    @Override
    public void setHorizontalRadius(float horizontalRadius) {
        this.horizontalRadius = horizontalRadius;
    }

    @Override
    public float getVerticalRadius() {
        return verticalRadius;
    }

    @Override
    public void setVerticalRadius(float verticalRadius) {
        this.verticalRadius = verticalRadius;
    }

    public Vector2 getOriginalPosition(){
        return originalPosition;
    }

    public void setOriginalPosition(Vector2 originalPosition){
        this.originalPosition = originalPosition;
    }

    public boolean isValidCollision(PlayerModel model){
        Vector2 playerPos = model.getPosition();
        Vector2 platformPos = this.getPosition();
        return (playerPos.y > platformPos.y + height/2 &&
                playerPos.x > (platformPos.x - width/2) &&
                playerPos.x < (platformPos.x + width/2));
    }
}
