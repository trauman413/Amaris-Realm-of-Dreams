package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.GameCanvas;
import edu.cornell.gdiac.amaris.obstacle.BoxObstacle;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

public class SpikedPlatform extends BoxObstacle implements Platform {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public Vector2 originalPosition;
    public float horizontalRadius;
    public float verticalRadius;
    public SpikeDirection direction;


    public SpikedPlatform(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                          float horizontalRadius, float verticalRadius, SpikeDirection direction) {
        super(width, height);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.velocity = velocity;
        this.horizontalRadius = horizontalRadius;
        this.originalPosition = getPosition();
        this.verticalRadius = verticalRadius;
        this.direction = direction;
    }

    @Override
    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        float sx = width / (texture.getRegionWidth() / scale.x) ;
        float sy = height / (texture.getRegionHeight() / scale.y);
        if (direction == SpikeDirection.RIGHT) {
            sx = width / (texture.getRegionHeight() / scale.y) ;
            sy = height / (texture.getRegionWidth() / scale.x);
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,11f,sx,sy);
        }
        else if (direction == SpikeDirection.LEFT) {
            sx = width / (texture.getRegionHeight() / scale.y) ;
            sy = height / (texture.getRegionWidth() / scale.x);
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,-11f,sx,sy);
        }
        else {
            canvas.draw(texture, color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), sx, sy);
        }
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
        if(direction == SpikeDirection.UP){
            return (playerPos.y > platformPos.y &&
                    playerPos.x > (platformPos.x - width/2) &&
                    playerPos.x < (platformPos.x + width/2));
        }
        else if (direction == SpikeDirection.DOWN){
            return (playerPos.y < platformPos.y &&
                    playerPos.x > (platformPos.x - width/2) &&
                    playerPos.x < (platformPos.x + width/2));
        }
        else if (direction == SpikeDirection.LEFT){
            return (playerPos.x < platformPos.x &&
                    playerPos.y > (platformPos.y - height/2) &&
                    playerPos.y < (platformPos.y + height/2));
        }
        else if (direction == SpikeDirection.RIGHT){
            return (playerPos.x > platformPos.x &&
                    playerPos.y > (platformPos.y - height/2) &&
                    playerPos.y < (platformPos.y + height/2));
        }
        return false;
    }

    public Vector2 getLaunchDirection(){
        if(direction == SpikeDirection.UP){
            return new Vector2(0,1);
        }
        if(direction == SpikeDirection.DOWN){
            return new Vector2(0,-1);
        }
        if(direction == SpikeDirection.LEFT){
            return new Vector2(-1, 0);
        }
        if(direction == SpikeDirection.RIGHT){
            return new Vector2(1, 0);
        }
        return new Vector2();
    }

    public enum SpikeDirection {
        UP, DOWN, LEFT, RIGHT
    }
}