/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.objects.CloudPlatform;
import edu.cornell.gdiac.physics.objects.Platform;
import edu.cornell.gdiac.physics.objects.SpikedPlatform;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {
	/** The texture file for the character avatar (no animation) */
	private static final String DUDE_FILE  = "platform/dude.png";

	
	/** The sound file for a jump */
	private static final String JUMP_FILE = "platform/jump.mp3";
	/** The sound file for a bullet fire */
	private static final String PEW_FILE = "platform/pew.mp3";
	/** The sound file for a bullet collision */
	private static final String POP_FILE = "platform/plop.mp3";

	/** Texture asset for character avatar */
	private TextureRegion avatarTexture;
	
	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;
	
	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */	
	public void preLoadContent(AssetManager manager) {
		if (platformAssetState != AssetState.EMPTY) {
			return;
		}
		
		platformAssetState = AssetState.LOADING;
		manager.load(DUDE_FILE, Texture.class);
		assets.add(DUDE_FILE);


		manager.load(JUMP_FILE, Sound.class);
		assets.add(JUMP_FILE);
		manager.load(PEW_FILE, Sound.class);
		assets.add(PEW_FILE);
		manager.load(POP_FILE, Sound.class);
		assets.add(POP_FILE);
		
		super.preLoadContent(manager);
	}

	/**
	 * Load the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (platformAssetState != AssetState.LOADING) {
			return;
		}
		
		avatarTexture = createTexture(manager,DUDE_FILE,false);


		SoundController sounds = SoundController.getInstance();
		sounds.allocate(manager, JUMP_FILE);
		sounds.allocate(manager, PEW_FILE);
		sounds.allocate(manager, POP_FILE);
		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}
	
	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -14.7f;
	/** The density for most physics objects */
	private static final float  BASIC_DENSITY = 0.0f;
	/** The density for a bullet */
	private static final float  HEAVY_DENSITY = 10.0f;
	/** Friction of most platforms */
	private static final float  BASIC_FRICTION = 0.4f;
	/** The restitution for all physics objects */
	private static final float  BASIC_RESTITUTION = 0.1f;
	/** The width of the rope bridge */
	private static final float  BRIDGE_WIDTH = 14.0f;
	/** Offset for bullet when firing */
	private static final float  BULLET_OFFSET = 0.2f;
	/** The speed of the bullet after firing */
	private static final float  BULLET_SPEED = 20.0f;
	/** The volume for sound effects */
	private static final float EFFECT_VOLUME = 0.8f;

	// Since these appear only once, we do not care about the magic numbers.
	// In an actual game, this information would go in a data file.
	// Wall vertices
	private static final float[][] WALLS = { 
			  								{16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
			  								  1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
			  								{32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
			  							     31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f}
											};
	
	/** The outlines of all of the platforms */
	private static final float[][] PLATFORMS = { 
												{ 1.0f, 3.0f, 6.0f, 3.0f, 6.0f, 2.5f, 1.0f, 2.5f},
												{ 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
												{23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
												//{26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
												//{29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
												{24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
												//{29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
												//{23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
												{19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
												{ 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f},
                                                { 12.0f,12.0f, 15.0f,12.0f, 15.0f,12.5f, 12.0f,12.5f}
											   };

	// Other game objects
	/** The goal door position */
	private static Vector2 GOAL_POS = new Vector2(4.0f,14.0f);
	/** The initial position of the dude */
	private static Vector2 DUDE_POS = new Vector2(2.5f, 5.0f);
	/** The position of the rope bridge */
	private static Vector2 BRIDGE_POS  = new Vector2(9.0f, 3.8f);
	/** The fountain position */
	private static Vector2 TFOUNTAIN_POS = new Vector2(5.0f,3.4f);
	private static Vector2 FFOUNTAIN_POS = new Vector2(25.0f,4.4f);
	private static Vector2 DFOUNTAIN_POS = new Vector2(20.0f,13f);

	// Physics objects for the game
	/** Reference to the character avatar */
	private DudeModel avatar;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** Reference to the dash fountain (for collision detection) */
	private BoxObstacle dfountain;
	/** Reference to the flight fountain (for collision detection) */
	private BoxObstacle ffountain;
	/** Reference to the transparent fountain (for collision detection) */
	private BoxObstacle tfountain;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/** The set of platforms in the level */
	protected List<Platform> platforms = new ArrayList<Platform>();


	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		abilityState = AbilityState.LAME;
		abilityTimer = 0;
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {

		// Add the platforms
		List<Vector2> cloudPlatformPositions = new ArrayList<Vector2>();
		cloudPlatformPositions.add(new Vector2(9.0f, 6.0f));
		cloudPlatformPositions.add(new Vector2(12.0f, 6.0f));
		cloudPlatformPositions.add(new Vector2(15.0f, 6.0f));
		List<Vector2> cloudPlatformVelocities = new ArrayList<Vector2>();
		cloudPlatformVelocities.add(new Vector2(0.0f, 0.03f));
		cloudPlatformVelocities.add(new Vector2(0.02f, 0.0f));
		cloudPlatformVelocities.add(new Vector2(0.02f, 0.02f));
		List<Vector2> cloudPlatformRadiusBounds = new ArrayList<Vector2>();
		cloudPlatformRadiusBounds.add(new Vector2(0f, 2f));
		cloudPlatformRadiusBounds.add(new Vector2(2f, 4f));
		cloudPlatformRadiusBounds.add(new Vector2(2f, 2f));
		Vector2 platformScale = new Vector2(1.0f, 0.5f);

		for(int x = 0; x < cloudPlatformPositions.size(); x++){
			CloudPlatform p = new CloudPlatform(cloudPlatformPositions.get(x).x, cloudPlatformPositions.get(x).y,
					earthTile.getRegionWidth()/scale.x * platformScale.x,
					earthTile.getRegionHeight()/scale.y * platformScale.y, scale,
					cloudPlatformVelocities.get(x), cloudPlatformRadiusBounds.get(x).x,
					cloudPlatformRadiusBounds.get(x).y);
			p.setBodyType(BodyDef.BodyType.StaticBody);
			p.setDensity(BASIC_DENSITY);
			p.setFriction(BASIC_FRICTION);
			p.setRestitution(BASIC_RESTITUTION);
			p.setSensor(false);
			p.setDrawScale(scale);
			p.setTexture(earthTile);
			p.setName("cloud platform " + String.valueOf(x));
			addObject(p);
			platforms.add(p);
		}

		List<Vector2> spikedPlatformPositions = new ArrayList<Vector2>();
		spikedPlatformPositions.add(new Vector2(15.0f, 6.0f));
		spikedPlatformPositions.add(new Vector2(18.0f, 6.0f));
		List<Vector2> spikedPlatformVelocities = new ArrayList<Vector2>();
		spikedPlatformVelocities.add(new Vector2(0.0f, 0.03f));
		spikedPlatformVelocities.add(new Vector2(0.02f, 0.0f));
		List<Vector2> spikedPlatformRadiusBounds = new ArrayList<Vector2>();
		spikedPlatformRadiusBounds.add(new Vector2(0f, 4f));
		spikedPlatformRadiusBounds.add(new Vector2(2f, 1f));
		platformScale = new Vector2(1.0f, 0.5f);

		for(int x = 0; x < 2; x++){
			SpikedPlatform p = new SpikedPlatform(spikedPlatformPositions.get(x).x, spikedPlatformPositions.get(x).y,
					earthTile.getRegionWidth()/scale.x * platformScale.x,
					earthTile.getRegionHeight()/scale.y * platformScale.y, scale,
					spikedPlatformVelocities.get(x), spikedPlatformRadiusBounds.get(x).x,
					spikedPlatformRadiusBounds.get(x).y, SpikedPlatform.SpikeDirection.UP);
			p.setBodyType(BodyDef.BodyType.StaticBody);
			p.setDensity(BASIC_DENSITY);
			p.setFriction(BASIC_FRICTION);
			p.setRestitution(BASIC_RESTITUTION);
			p.setSensor(false);
			p.setDrawScale(scale);
			p.setTexture(earthTile);
			p.setName("spiked platform " + String.valueOf(x));
			addObject(p);
			platforms.add(p);
		}

		// Add level goal
		float dwidth  = goalTile.getRegionWidth()/scale.x;
		float dheight = goalTile.getRegionHeight()/scale.y;
		goalDoor = new BoxObstacle(GOAL_POS.x,GOAL_POS.y,dwidth,dheight);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setDensity(0.0f);
		goalDoor.setFriction(0.0f);
		goalDoor.setRestitution(0.0f);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		goalDoor.setName("goal");
		addObject(goalDoor);

		// Add dash fountain
		dwidth  = dfountainTile.getRegionWidth()/scale.x;
		dheight = dfountainTile.getRegionWidth()/scale.x;
		dfountain = new BoxObstacle(DFOUNTAIN_POS.x,DFOUNTAIN_POS.y,dwidth,dheight);
		dfountain.setBodyType(BodyDef.BodyType.StaticBody);
		dfountain.setDensity(0.0f);
		dfountain.setFriction(0.0f);
		dfountain.setRestitution(0.0f);
		dfountain.setSensor(true);
		dfountain.setDrawScale(scale);
		dfountain.setTexture(dfountainTile);
		dfountain.setName("dash_fountain");
		addObject(dfountain);

		// Add flight fountain
		dwidth  = ffountainTile.getRegionWidth()/scale.x;
		dheight = ffountainTile.getRegionWidth()/scale.x;
		ffountain = new BoxObstacle(FFOUNTAIN_POS.x,FFOUNTAIN_POS.y,dwidth,dheight);
		ffountain.setBodyType(BodyDef.BodyType.StaticBody);
		ffountain.setDensity(0.0f);
		ffountain.setFriction(0.0f);
		ffountain.setRestitution(0.0f);
		ffountain.setSensor(true);
		ffountain.setDrawScale(scale);
		ffountain.setTexture(ffountainTile);
		ffountain.setName("flight_fountain");
		addObject(ffountain);

		// Add transparent fountain
		dwidth  = tfountainTile.getRegionWidth()/scale.x;
		dheight = tfountainTile.getRegionWidth()/scale.x;
		tfountain = new BoxObstacle(TFOUNTAIN_POS.x,TFOUNTAIN_POS.y,dwidth,dheight);
		tfountain.setBodyType(BodyDef.BodyType.StaticBody);
		tfountain.setDensity(0.0f);
		tfountain.setFriction(0.0f);
		tfountain.setRestitution(0.0f);
		tfountain.setSensor(true);
		tfountain.setDrawScale(scale);
		tfountain.setTexture(tfountainTile);
		tfountain.setName("transparent_fountain");
		addObject(tfountain);

	    String wname = "wall";
	    for (int ii = 0; ii < WALLS.length; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(WALLS[ii], 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(BASIC_DENSITY);
			obj.setFriction(BASIC_FRICTION);
			obj.setRestitution(BASIC_RESTITUTION);
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(wname+ii);
			addObject(obj);
	    }
	    
	    String pname = "platform";
	    for (int ii = 0; ii < PLATFORMS.length; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(PLATFORMS[ii], 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(BASIC_DENSITY);
			obj.setFriction(BASIC_FRICTION);
			obj.setRestitution(BASIC_RESTITUTION);
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(pname+ii);
			addObject(obj);
	    }

		// Create dude
		dwidth  = avatarTexture.getRegionWidth()/scale.x;
		dheight = avatarTexture.getRegionHeight()/scale.y;
		avatar = new DudeModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight);
		avatar.setDrawScale(scale);
		avatar.setTexture(avatarTexture);
		addObject(avatar);

//		InputController.getInstance().setAbilityAonCD(false);
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}
		
		if (!isFailure() && avatar.getY() < -1) {
			setFailure(true);
			return false;
		}
		
		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Process actions in object model
		avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
		avatar.setJumping(InputController.getInstance().didPrimary());
		avatar.setDashing(InputController.getInstance().didActivateAbility() && abilityState == AbilityState.DASH);
		avatar.setFlying(InputController.getInstance().didActivateAbility() && abilityState == AbilityState.FLIGHT);
		avatar.setTransparent(InputController.getInstance().didActivateAbility() && abilityState == AbilityState.TRANSPARENT);
		abilityTimer = avatar.getAbilityTimer();

		avatar.setShooting(InputController.getInstance().didSecondary());
//		if (InputController.getInstance().didAbility() && abilityState != AbilityState.LAME) {
//			avatar.setPressedAbility(true);
//		}
		avatar.setPressedAbility(InputController.getInstance().didAbility() && abilityState != AbilityState.LAME);

		// Move the platforms horizontally or vertically
		for(Platform platform : platforms){
			Vector2 newPos = platform.getPosition();
			newPos.add(platform.getVelocity());
			if(Math.abs(newPos.x - platform.getOriginalPosition().x) > platform.getHorizontalRadius())
				platform.setVelocity(platform.getVelocity().scl(-1, 1));
			if(Math.abs(newPos.y - platform.getOriginalPosition().y) > platform.getVerticalRadius())
				platform.setVelocity(platform.getVelocity().scl(1,-1));
			platform.setPosition(newPos);
		}
		
		avatar.applyForce();
	    if (avatar.isJumping()) {
	        SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
	    }

		if (avatar.isTimerDone()) {
			abilityState = AbilityState.LAME;
		}
		//System.out.println("ability state " + abilityState );
	    // If we use sound, we must remember this.
	    SoundController.getInstance().update();
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
			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
				// Only set grounded to true on CloudPlatforms if the avatar is transparent
				if(bd1 instanceof CloudPlatform || bd2 instanceof CloudPlatform){
					if(avatar.isTransparencyActive()){
						avatar.setGrounded(true);
					}
				} else {
					avatar.setGrounded(true);
					System.out.println("is grounded");
				}
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
			}

			// Check for dash fountain interaction
			if ((bd1 == avatar   && bd2 == dfountain) ||
					(bd1 == dfountain && bd2 == avatar)) {
				if(abilityState != AbilityState.LAME && !avatar.isTimerDone()) {
					avatar.setAbilityTimer(0);
				}
				abilityState = AbilityState.DASH;

			}

			// Check for flight fountain interaction
			if ((bd1 == avatar   && bd2 == ffountain) ||
					(bd1 == ffountain && bd2 == avatar)) {
				if(abilityState != AbilityState.LAME && !avatar.isTimerDone()) {
					avatar.setAbilityTimer(0);
				}
				abilityState = AbilityState.FLIGHT;
			}

			// Check for transparent fountain interaction
			if ((bd1 == avatar   && bd2 == tfountain) ||
					(bd1 == tfountain && bd2 == avatar)) {
				if(abilityState != AbilityState.LAME && !avatar.isTimerDone()) {
					avatar.setAbilityTimer(0);
				}
				abilityState = AbilityState.TRANSPARENT;
			}



			// Check for win condition
			if ((bd1 == avatar   && bd2 == goalDoor) ||
				(bd1 == goalDoor && bd2 == avatar)) {
				setComplete(true);
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

		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
			(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
			if (sensorFixtures.size == 0) {
				avatar.setGrounded(false);
			}
		}
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
			if ((bd1 instanceof CloudPlatform && bd2 instanceof DudeModel)
					|| (bd1 instanceof DudeModel && bd2 instanceof CloudPlatform)) {
				// Get the DudeModel and CloudPlatform
				final DudeModel dm;
				final CloudPlatform platform;
				if(bd1 instanceof DudeModel){
					dm = (DudeModel) bd1;
					platform = (CloudPlatform) bd2;
				}
				else {
					dm = (DudeModel) bd2;
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
			if ((bd1 instanceof SpikedPlatform && bd2 instanceof DudeModel)
					|| (bd1 instanceof DudeModel && bd2 instanceof SpikedPlatform)) {
				// Find and assign the DudeModel object and SpikedPlatform Object
				final DudeModel dm;
				final SpikedPlatform sp;
				if(bd1 instanceof DudeModel){
					dm = (DudeModel) bd1;
					sp = (SpikedPlatform) bd2;
				}
				else{
					dm = (DudeModel) bd2;
					sp = (SpikedPlatform) bd1;
				}
				if(sp.isValidCollision(dm)){
					// Handle knockback from spikes bsaed on spike direction
					Float xVariation = (new Random()).nextFloat();
					float knockBackScale = 8f;
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

		} catch (Exception e){
			e.printStackTrace();
		}

	}
}