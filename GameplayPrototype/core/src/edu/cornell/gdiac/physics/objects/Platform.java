package edu.cornell.gdiac.physics.objects;

import com.badlogic.gdx.math.Vector2;

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


}
