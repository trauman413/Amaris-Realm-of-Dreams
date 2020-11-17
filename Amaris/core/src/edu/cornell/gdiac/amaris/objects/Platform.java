package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

public interface Platform {

    public Vector2 getPosition();
    public Vector2 getVelocity();
    public Vector2 getOriginalPosition();
    public void setOriginalPosition(Vector2 originalPosition);
    public void setPosition(Vector2 position);
    public void setVelocity(Vector2 velocity);
    public float getHorizontalRadius();
    public void setHorizontalRadius(float horizontalRadius);
    public float getVerticalRadius();
    public void setVerticalRadius(float verticalRadius);
    public boolean isValidCollision(PlayerModel model);
    public float getWidth();
    public float getHeight();


}
