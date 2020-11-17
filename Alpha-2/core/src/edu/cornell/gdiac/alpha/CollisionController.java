package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.alpha.objects.*;
import edu.cornell.gdiac.alpha.obstacles.*;
import edu.cornell.gdiac.alpha.platforms.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller to handle gameplay interactions.
 *
 * This controller also acts as the root class for all the models.
 */
public class CollisionController implements ContactListener {
    /** The world scale */
    protected Vector2 scale;
    /** Reference to the player */
    private PlayerModel player;
    /** All the objects in the world. */
    protected Array<Obstacle> objects  = new Array<Obstacle>();
    /** Whether or not a collision with spikes has occurred */
    protected boolean spikeCollision;
    /** Wehther or not a collision with rocks has occurred */
    protected boolean rockCollision;
    /** list of moon shards player collided with that needed to be removed */
    public Array<MoonShard> removeMS = new Array<MoonShard>();
    /** Whether or not a collision with rocks and player has occurred */
    protected boolean playerRockCollision;
    /** Collided rock */
    protected Rock collidedRock;

    // Physics objects for the game
    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;
    /** Reference to the dash fountain (for collision detection) */
    private BoxObstacle dfountain;
    /** Reference to the flight fountain (for collision detection) */
    private BoxObstacle ffountain;
    /** Reference to the transparent fountain (for collision detection) */
    private BoxObstacle tfountain;
    /** Reference to the serenity fountain (for collision detection) */
    private BoxObstacle sfountain;
    /** list of fountains in game */
    private List<FountainModel> fountainsList;
    /** list of moon shards in game */
    private List<MoonShard> msList;
    /** list of rocks in game */
    private List<Rock> rockList;
    /** list of platforms in game */
    private List<BoxObstacle> platformList;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;

    // Fields to be retrieved as results from collisions
    /** Whether the level failed or not */
    private boolean didFail;
    /** Whether the level is completed or not */
    private boolean isComplete;
    /** Whether the level is completed or not */
    FountainModel.FountainType abilityToAdd;
    /** The most recently touched fountain */
    FountainModel touchedFountain;
    /** Whether or not a serenity fountain has been touched */
    private boolean toRestore = false;
    /** Whether or not a monster has been contacted */
    private boolean touchedMonster = false;


    // Getters and setters for private returnable fields
    /** Sets whether the level failed or not */
    public void setFailure(boolean value) { didFail = value; }
    /** For returning whether the level failed or not */
    public boolean getFailure() { return didFail; }

    /** Sets whether the level is completed or not */
    public void setComplete(boolean value) { isComplete = value; }
    /** For returning whether the level is completed or not */
    public boolean getComplete() { return isComplete; }
    /** Sets the ability to add */
    public void setAbilityToAdd(FountainModel.FountainType ability) { abilityToAdd = ability; }
    /** For returning the ability to add */
    public FountainModel.FountainType getAbilityToAdd() { return abilityToAdd; }
    public void setPlayer (PlayerModel avatar) { player = avatar; }
    public void setDFountain (BoxObstacle dash) { dfountain = dash; }
    public void setFFountain (BoxObstacle flight) { ffountain = flight; }
    public void setEFountain (BoxObstacle ethereal) { tfountain = ethereal; }
    public void setFountainsList(List<FountainModel> fountains) { fountainsList = fountains; }
    public void setGoalDoor (BoxObstacle goal) { goalDoor = goal; }
    public void setMoonShardList (List<MoonShard> moons) {msList = moons;}
    public void setRockList(List<Rock> r) {rockList = r;}
    public void setPlatformList(List<BoxObstacle> p) {platformList = p;}
    public FountainModel getTouchedFountain() { return touchedFountain; }
    public void setTouchedFountain(FountainModel fountain) { touchedFountain = fountain; }
    public void setRestore(boolean val) { toRestore = false; }
    public boolean getRestore() { return toRestore; }
    public void setTouchedMonster(boolean val) { touchedMonster = false; }
    public boolean getTouchedMonster() { return touchedMonster; }





    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */

    public CollisionController(float width, float height, World toUse, PlayerModel avatar, List<FountainModel> fountains, Array<Obstacle> obj, BoxObstacle goal, float gravity) {
        this(new Rectangle(0,0,width,height), toUse, new Vector2(0,gravity));
        player = avatar;
        fountainsList = fountains;
        goalDoor = goal;
        objects = obj;
        touchedFountain = null;
        spikeCollision = false;
    }

    public CollisionController(Rectangle bounds, World toUse, Vector2 gravity) {
        world = toUse;
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // See if we have landed on the ground.
            if ((player.getSensorName().equals(fd2) && player != bd1) ||
                    (player.getSensorName().equals(fd1) && player != bd2)) {
                // Only set grounded to true on CloudPlatforms if the avatar is transparent
                if(bd1 instanceof CloudPlatform || bd2 instanceof CloudPlatform){
                    System.out.println(player.isTransparencyActive());
                    if(player.isTransparencyActive()){
                        player.setGrounded(true);
                        System.out.println("cloud");
                    }
                } else {
                    player.setGrounded(true);
                }
                sensorFixtures.add(player == bd1 ? fix2 : fix1); // Could have more than one ground
            }
           //System.out.println(player.getGrounded());

//            //check is rock collided with platform
//            for (int i = 0; i < rockList.size(); i++) {
//                Rock r = rockList.get(i);
//                if ((r.getSensorName().equals(fd2) && r != bd1) ||
//                        (r.getSensorName().equals(fd1) && r != bd2)) {
////                    // Only set grounded to true on CloudPlatforms if the avatar is transparent
////                    if (bd1 instanceof CloudPlatform || bd2 instanceof CloudPlatform) {
////                        if (player.isTransparencyActive()) {
////                            player.setGrounded(true);
////                        }
////                    } else {
//                        r.setGrounded(true);
//
//                    sensorFixtures.add(r == bd1 ? fix2 : fix1); // Could have more than one ground
//                    System.out.println(r.getGrounded());
//                }
//            }


            //check for rock collisions
            for (int i = 0; i < rockList.size(); i++) {
                Rock r = rockList.get(i);
                if ((bd1 == r && bd2 == player)
                        || (bd1 == player && bd2 == r)) {
                    setPlayerRockCollision(true);

                }
            }

            // Check for win condition
            if (((bd1 == player   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == player)) && (msList.size() == removeMS.size))  {
                //TODO: account for win condition with moon shard
                setComplete(true);
            }



            //check for fountains
            for (int i = 0; i < fountainsList.size(); i++) {
                if ((bd1 == player   && bd2 == fountainsList.get(i)) ||
                        (bd1 == fountainsList.get(i) && bd2 == player)) {
                    if(fountainsList.get(i).getFountainType() != FountainModel.FountainType.RESTORE) {
                        setAbilityToAdd(fountainsList.get(i).getFountainType());
                    }
                    if(bd2 == fountainsList.get(i)) {
                        touchedFountain = (FountainModel)bd2;
                    }
                    else if(bd1 == fountainsList.get(i)) {
                        touchedFountain = (FountainModel)bd1;
                    }
                    if(touchedFountain.isAvailable() && touchedFountain.getFountainType() != FountainModel.FountainType.RESTORE){
                        Sound s = SoundController.touchFountainSound();
                        s.play(0.8f);
                    }
                    if(touchedFountain.getFountainType() == FountainModel.FountainType.RESTORE && touchedFountain.isAvailable()) {
                        //TODO: play sound
                        toRestore = true;
                        touchedFountain.setAvailable(false);
                    }
                }

            }

            //check for moon shard collision
            for (int i = 0; i < msList.size(); i++) {
                MoonShard ms = msList.get(i);
                if ((bd1 == ms && bd2 == player)
                        || (bd1 == player && bd2 == ms)) {
                    ms.setTaken(true);
                    //ms.deactivatePhysics(world);
                    System.out.println("moon");
                    removeMS.add(ms);
                }
            }

            //check for monster collisions
            if(bd1 instanceof Crocodile && bd2 instanceof PlayerModel || bd2 instanceof  Crocodile
            && bd1 instanceof  PlayerModel) {
                touchedMonster = true;
            }
            if(bd1 instanceof FlyingMonster && bd2 instanceof PlayerModel || bd2 instanceof  FlyingMonster
                    && bd1 instanceof  PlayerModel) {
                touchedMonster = true;
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((player.getSensorName().equals(fd2) && player != bd1) ||
                (player.getSensorName().equals(fd1) && player != bd2)) {
            sensorFixtures.remove(player == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                player.setGrounded(false);
            }
        }
    }

    public boolean getSpikeCollision() {
        return spikeCollision;
    }

    public void setSpikeCollision(boolean sc) {
        spikeCollision = sc;
    }

    public boolean getRockCollision() {
        return rockCollision;
    }

    public void setRockCollision(boolean rk) {
        rockCollision = rk;
    }

    public boolean getPlayerRockCollision() {
        return playerRockCollision;
    }

    public void setPlayerRockCollision(boolean rk) {
        playerRockCollision = rk;
    }



    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        // Presolve collisions with CloudPlatforms and SpikedPlatforms

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            // Check object types for CloudPlatform & DudeModel collision
            if ((bd1 instanceof CloudPlatform && bd2 instanceof PlayerModel)
                    || (bd1 instanceof PlayerModel && bd2 instanceof CloudPlatform)) {
                // Get the DudeModel and CloudPlatform
                final PlayerModel dm;
                final CloudPlatform platform;
                if(bd1 instanceof PlayerModel){
                    dm = (PlayerModel) bd1;
                    platform = (CloudPlatform) bd2;
                }
                else {
                    dm = (PlayerModel) bd2;
                    platform = (CloudPlatform) bd1;
                }
                // If the intangible ability is not held, cancel the collision
                if(!dm.isTransparencyActive()) {
                    contact.setEnabled(false);
                } else {
                    // On the next render frame, add the platform's velocity to the player's position so
                    // the player sticks to the platform and moves with it.
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            Vector2 newPlayerPos = dm.getPosition().add(platform.getVelocity());
                            dm.setPosition(newPlayerPos);
                        }
                    });
                }
            }

            // Check object types for SpikedPlatform & DudeModel collision
            if ((bd1 instanceof SpikedPlatform && bd2 instanceof PlayerModel)
                    || (bd1 instanceof PlayerModel && bd2 instanceof SpikedPlatform)) {
                // Find and assign the DudeModel object and SpikedPlatform Object
                final PlayerModel dm;
                final SpikedPlatform sp;
                if(bd1 instanceof PlayerModel){
                    dm = (PlayerModel) bd1;
                    sp = (SpikedPlatform) bd2;
                }
                else{
                    dm = (PlayerModel) bd2;
                    sp = (SpikedPlatform) bd1;
                }
                if(sp.isValidCollision(dm)){
                    // Handle knockback from spikes bsaed on spike direction
                    spikeCollision = true;
                    Float xVariation = (new Random()).nextFloat();
                    float knockBackScale = 150f;
                    Vector2 newVelocity = sp.getLaunchDirection();
                    // Uncomment the following line to give a random x-direction knockback
                    //newVelocity = newVelocity.add((xVariation - 0.5f) * 10, 0f);
                    newVelocity = newVelocity.scl(knockBackScale);
                    //newVelocity.add(-dm.getLinearVelocity().x/2, 0f);
                    dm.setLinearVelocity(newVelocity);

                    dm.setGrounded(false);

                    contact.setEnabled(false);
                    dm.applyForce();
                    // On the next render frame, add the platform's velocity to the player's position so
                    // the player sticks to the platform and moves with it.
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            Vector2 platformVelocity = new Vector2(sp.getVelocity());
                            Vector2 newPlayerPos =
                                    dm.getPosition().add(platformVelocity);
                            dm.setPosition(newPlayerPos);
                        }
                    });
                }
            }
//            // Check object types for Rock and player collision
//            if ((bd1 instanceof Rock && bd2 instanceof Obstacle)
//                    || (bd1 instanceof Obstacle && bd2 instanceof Rock)) {
//                // Find and assign the DudeModel object and SpikedPlatform Object
//                if(bd1 instanceof PlayerModel || bd2 instanceof PlayerModel){
//                    playerRockCollision = true;
//                }
//                if(bd1 instanceof  Rock) {
//                    collidedRock = (Rock) bd1;
//                }
//                else if(bd2 instanceof  Rock) {
//                    collidedRock = (Rock) bd2;
//                }
//                rockCollision = true;
//            }


        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
