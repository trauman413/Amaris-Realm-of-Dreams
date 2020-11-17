package edu.cornell.gdiac.alpha.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.alpha.GameCanvas;
import edu.cornell.gdiac.alpha.obstacle.BoxObstacle;
import edu.cornell.gdiac.alpha.platform.PlayerModel;

public class CloudPlatform extends BoxObstacle implements Platform {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public Vector2 originalPosition;
    public float horizontalRadius;
    public float verticalRadius;

    public CloudPlatform(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
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
    }

    @Override
    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        float sx = width / (texture.getRegionWidth() / scale.x) ;
        float sy = height*2 / (texture.getRegionHeight() / scale.y);
        canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
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
