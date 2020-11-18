package edu.cornell.gdiac.amaris.platform;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.amaris.*;
import edu.cornell.gdiac.amaris.objects.MoonShard;
import edu.cornell.gdiac.amaris.objects.RegularPlatform;
import edu.cornell.gdiac.amaris.obstacle.BoxObstacle;

//import javax.xml.soap.Text;
import java.util.ArrayList;

/**
 * Fountain class.
 */

public class FountainModel extends BoxObstacle {

    /** Enums representing the different types of abilities obtained
     * by fountains.
     */
    public enum FountainType {
        /** A fountain that gives Dash. */
        DASH,
        /** A fountain that gives Flight. */
        FLIGHT,
        /** A fountain that gives Transparency. */
        TRANSPARENCY,
        /** A fountain that restores player's Serenity. Cannot be obtained as an ability. */
        RESTORE
    }

    //Class attributes
    /** The type of fountain */
    protected FountainType fountainType;
    /** Whether or not the fountain can grant an ability. Based upon whether the
     * ability is in the queue and has not been used yet */
    protected boolean isAvailable;
    /* Fountain width */
    protected float FOUNTAIN_WIDTH = 100;
    /* Fountain height */
    protected float FOUNTAIN_HEIGHT = 100;
    //probably more
    TextureRegion[] animationFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
    float elapsedTime;
    Animation<TextureRegion> animation;
    private static final int FRAME_COLS = 2, FRAME_ROWS = 2;
    private TextureRegion icon;
    private TextureRegion empty;

    //Getters
    /** Returns the fountain type of the fountain.
     *
     * @return what type of fountain it is.
     */
    public FountainType getFountainType() { return fountainType; }

    /** Returns whether or not the fountain can actually grant an ability or serenity.
     *
     * @return if the fountain is available for use or not (a bool).
     */
    public boolean isAvailable() { return isAvailable; }

    /** Sets whether or not the fountain can be accessed.
     *
     * @param active a boolean representing if an ability can be obtained from the fountain.
     */
    public void setAvailable(boolean active) {isAvailable = active; }

    private ArrayList<String> fountainIDs;
    private ArrayList<String> moonShardIDs;
    private ArrayList<String> windowIDs;

    public void setFountainsQueued(AbilityQueue queue) {
        fountainIDs = new ArrayList<String>();
        for (FountainModel fountain : queue.abilities) {
            fountainIDs.add(fountain.getName());
        }
    }

    public void setMoonShardsCollected(ArrayList<MoonShard> moons) {
        moonShardIDs = new ArrayList<String>();
        for (MoonShard m : moons) {
            moonShardIDs.add(m.getName());
        }
    }

    public void setWindowsBroken(ArrayList<RegularPlatform> windows) {
        windowIDs = new ArrayList<String>();
        for (RegularPlatform w : windows) {
            windowIDs.add(w.getName());
        }
    }

    public ArrayList<String> getFountainsQueued() {
        return fountainIDs;
    }

    public ArrayList<String> getMoonShardsCollected() {
        return moonShardIDs;
    }

    public ArrayList<String> getWindowsBroken() {
        return windowIDs;
    }


    //Constructor
    /** Creates a new fountain at the given location with the specified fountain type.
     *
     * @param x The x-position of the fountain.
     * @param y The y-position of the fountain.
     * @param fountain The type of fountain being created.
     */
    public FountainModel(float x, float y, float width, float height, FountainType fountain) {
        super(x,y,width*0.7f, height*0.8f);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0.0f);
        setFriction(0.0f);
       // setRestitution(0.0f);
        setSensor(true);

        fountainType = fountain;
        isAvailable = true;
    }

    public FountainModel(BoxObstacle bd, FountainType fountain) {
        super(bd.getX(),bd.getY(),1, 1);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(0.0f);
        setFriction(0.0f);
        setRestitution(0.0f);
        setSensor(true);
        fountainType = fountain;
        isAvailable = true;
    }

    /**
     * Creates the alpha Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        return true;
    }

    public void setIcon(TextureRegion texture) {
        this.icon = texture;
    }

    public void setEmptyTexture(TextureRegion texture) { this.empty = texture; }

//    /**
//     * Draws the alpha object.
//     *
//     * @param canvas Drawing context
//     */
//    public void draw(GameCanvas canvas) {
//        Color color = Color.WHITE;
//        if(!isAvailable) {
//            color = new Color(1,1,1,.5f);
//        }
//        //float sx = getWidth() / (texture.getRegionWidth() / drawScale.x) ;
//        //float sy = getHeight() / (texture.getRegionHeight() / drawScale.y);
//        canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1.0f,1.0f);    }

/**
 * Draws the fountain with animation.
 *
 */
    public void draw(GameCanvas canvas) {
        Color color = Color.WHITE;

        if (fountainType == FountainType.RESTORE) {
            if (isAvailable) {
                canvas.draw(texture, color, origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y+45, texture.getRegionWidth(), texture.getRegionHeight());
            } else {
                canvas.draw(empty, color, origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y+45, empty.getRegionWidth(), empty.getRegionHeight());

            }

        } else {
            if(!isAvailable) {
                color = new Color(1,1,1,.5f);
            }

            elapsedTime+= Gdx.graphics.getDeltaTime();
            TextureRegion[][] tmpFrames = TextureRegion.split(texture.getTexture(), texture.getRegionWidth()/FRAME_COLS, texture.getRegionHeight()/FRAME_COLS);
            int index = 0;

            for (int i = 0; i < FRAME_ROWS; i++) {
                for (int j = 0; j<FRAME_COLS; j++) {
                    animationFrames[index++] = tmpFrames[i][j];
                }
            }

            animation = new Animation(0.11f, animationFrames);

            TextureRegion currentFrame = animation.getKeyFrame(elapsedTime, true);
            canvas.draw(currentFrame, color, origin.x,origin.y,getX()*drawScale.x+65,getY()*drawScale.y+93, texture.getRegionWidth()/FRAME_COLS, texture.getRegionHeight()/FRAME_COLS);
            if (isAvailable && icon != null) {
                canvas.draw(icon, color, origin.x,origin.y,getX()*drawScale.x+100,getY()*drawScale.y+220, texture.getRegionWidth()/4.2f, texture.getRegionHeight()/4.2f);
            }
        }

    }
}

