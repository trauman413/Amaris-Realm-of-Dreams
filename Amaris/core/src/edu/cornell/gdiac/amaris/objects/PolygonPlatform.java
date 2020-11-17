package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.GameCanvas;
import edu.cornell.gdiac.amaris.obstacle.PolygonObstacle;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

public class PolygonPlatform extends PolygonObstacle implements Platform {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public Vector2 originalPosition;
    public float horizontalRadius;
    public float verticalRadius;

    public PolygonPlatform(float[] points, float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                           float horizontalRadius, float verticalRadius) {
        super(points, x, y, width, height);
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
        if (texture != null) {
            float sx = width / (texture.getRegionWidth() / scale.x) ;
            float sy = height / (texture.getRegionHeight() / scale.y);
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
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
