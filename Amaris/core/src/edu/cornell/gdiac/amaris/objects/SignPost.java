package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.amaris.GameCanvas;
import edu.cornell.gdiac.amaris.obstacle.BoxObstacle;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

import java.util.ArrayList;

public class SignPost extends BoxObstacle {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    //public String message;
    public boolean messageVisible;
    public Vector2 messagePos;
    public TextureRegion image;
    public boolean hasRead;
    public int id;

    public SignPost(float x, float y, float mx, float my, float width, float height, Vector2 scale, TextureRegion image, int id) {
        super(x, y, width*0.2f, height);
        //message = msg;
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.messageVisible = false;
        this.messagePos = new Vector2(mx, my);
        this.image = image;
        this.hasRead = false;
        this.id = id;
    }

    public void setMessageVisible(boolean value) {
        this.messageVisible = value;
    }

    public boolean getMessageVisible() {
        return messageVisible;
    }

    public boolean getHasRead() { return hasRead; }

    public void setHasRead(boolean val) { hasRead = val; }

    public int getId() { return id;}

    @Override
    public void draw(GameCanvas canvas){
        Color color = Color.WHITE;
        float sx = width / (texture.getRegionWidth() / scale.x) ;
        float sy = height / (texture.getRegionHeight() / scale.y);
        canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
    }

}
