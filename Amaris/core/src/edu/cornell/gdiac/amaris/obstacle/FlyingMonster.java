package edu.cornell.gdiac.amaris.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.GameCanvas;

public class FlyingMonster extends BoxObstacle {
    private Vector2 position;
    private Vector2 scale;
    private float horizontalRadius;
    private float verticalRadius;
    private Vector2 originalPosition;
    private boolean faceRight;
    TextureRegion[] animationFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
    float elapsedTime;
    Animation<TextureRegion> animation;
    private static final int FRAME_COLS = 3, FRAME_ROWS = 2;

    public FlyingMonster(float x, float y, float width, float height, Vector2 scale, Vector2 velocity,
                         float horizontalRadius, float verticalRadius) {
        super(width, height*0.5f);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.velocity = velocity;
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.originalPosition = new Vector2(x, y);
        this.faceRight = false;
    }

    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        //float sx = width * 1.5f/ (texture.getRegionWidth() / scale.x) ;
        float effect = faceRight ? -1f : 1f;
       //float sy = height * 1.5f/ (texture.getRegionHeight() / scale.y);
        elapsedTime+= Gdx.graphics.getDeltaTime();
        TextureRegion[][] tmpFrames = TextureRegion.split(texture.getTexture(), texture.getRegionWidth()/FRAME_COLS, texture.getRegionHeight()/FRAME_ROWS);
        int index = 0;

        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j<FRAME_COLS; j++) {
                animationFrames[index++] = tmpFrames[i][j];
            }
        }
        animation = new Animation(0.1f, animationFrames);

        TextureRegion currentFrame = animation.getKeyFrame(elapsedTime, true);
        if (effect == 1) {
            canvas.draw(currentFrame, color, origin.x, origin.y, getX() * drawScale.x + 90, getY() * drawScale.y + 50, getAngle(), effect, 1f);
        }
        else {
            canvas.draw(currentFrame, color, origin.x, origin.y, getX() * drawScale.x - 90, getY() * drawScale.y + 50, getAngle(), effect, 1f);
        }
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
