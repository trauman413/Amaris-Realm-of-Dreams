package edu.cornell.gdiac.amaris.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.amaris.GameCanvas;
import edu.cornell.gdiac.amaris.obstacle.BoxObstacle;
import edu.cornell.gdiac.amaris.obstacle.PolygonObstacle;
import edu.cornell.gdiac.amaris.platform.PlayerModel;

public class GoalDoor extends PolygonObstacle {

    public float width;
    public float height;
    public Vector2 velocity;
    public Vector2 scale;
    public boolean flipped;
    public Array<BoxObstacle> sensors;
    public boolean canComplete;

    public GoalDoor(float[] points, float x, float y, float width, float height, Vector2 scale, boolean flipped) {
        super(points, x, y, width, height);
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.flipped = flipped;

        sensors = new Array<BoxObstacle>();

        float sensorWidth = (points[0]-points[2])/2.0f;
        BoxObstacle sensor = new BoxObstacle(x+points[2]+sensorWidth, y+points[1]+1/2.0f, sensorWidth, 1);
        sensor.setBodyType(BodyDef.BodyType.StaticBody);
        sensor.setFriction(0);
        sensor.setDensity(0);
        sensor.setRestitution(0);
        sensor.setDrawScale(scale);
        sensor.setSensor(true);
        sensor.setName("goal-sensor");
        sensors.add(sensor);

        sensor = new BoxObstacle(x+points[6]+sensorWidth/2.0f, y+points[5], sensorWidth, 0.7f);
        sensor.setBodyType(BodyDef.BodyType.StaticBody);
        sensor.setFriction(0);
        sensor.setDensity(0);
        sensor.setRestitution(0);
        sensor.setDrawScale(scale);
        sensor.setSensor(true);
        sensor.setName("goal-sensor");
        sensors.add(sensor);

        sensor = new BoxObstacle(x+points[10]+sensorWidth/2.0f, y+points[9], sensorWidth, 0.7f);
        sensor.setBodyType(BodyDef.BodyType.StaticBody);
        sensor.setFriction(0);
        sensor.setDensity(0);
        sensor.setRestitution(0);
        sensor.setDrawScale(scale);
        sensor.setSensor(true);
        sensor.setName("goal-sensor");
        sensors.add(sensor);

        sensor = new BoxObstacle(x+points[14]+sensorWidth/2.0f, y+points[13], sensorWidth, 0.5f);
        sensor.setBodyType(BodyDef.BodyType.StaticBody);
        sensor.setFriction(0);
        sensor.setDensity(0);
        sensor.setRestitution(0);
        sensor.setDrawScale(scale);
        sensor.setSensor(true);
        sensor.setName("goal-sensor");
        sensors.add(sensor);
    }

    public void setFlipped(boolean value) { flipped = value; }

    public void setComplete(boolean value) {
        canComplete = value;
        setSensor(!value);
    }

    @Override
    public void draw(GameCanvas canvas){
        Color color;
        if(!canComplete) {
            color =  new Color(1,1,1,.5f);
        }
        else {
            color = Color.WHITE;
        }
        float sx = width / (texture.getRegionWidth() / scale.x) ;
        float sy = height / (texture.getRegionHeight() / scale.y);
        if (flipped) {
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),-sx,sy);

        } else {
            canvas.draw(texture,color,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),sx,sy);
        }
    }

}
