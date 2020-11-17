package edu.cornell.gdiac.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.alpha.objects.CloudPlatform;
import edu.cornell.gdiac.alpha.objects.Platform;
import edu.cornell.gdiac.alpha.objects.MoonShard;
import edu.cornell.gdiac.alpha.objects.SpikedPlatform;
import edu.cornell.gdiac.alpha.objects.*;
import edu.cornell.gdiac.alpha.platform.FountainModel;
import edu.cornell.gdiac.alpha.platform.PlayerModel;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.alpha.obstacle.*;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
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
public class GameplayController extends GameMode implements ContactListener {
	/** The last used fountain */
	private FountainModel lastUsed;
	/** The ability controller **/
	public AbilityController abilityController = AbilityController.getInstance();
	/** The input controller **/
	public InputController inputController = InputController.getInstance();
	/** The number of moon shards collected */
	public int numMoonCollected = 0;

	//Important constants
	private float MAX_SERENITY;
	private int ABILITY_TIME = 5000;

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
		levelLoader.preLoadContent(manager, assets);

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
//		if (platformAssetState != AssetState.LOADING) {
//			return;
//		}
		levelLoader.loadContent(manager);

		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}

	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -20.7f;

	// Physics objects for the game
	/** Reference to the goalDoor (for collision detection) */
	private GoalDoor goalDoor;

	// Physics objects for the game
	/** Reference to the goalDoor (for collision detection) */
	private List<FountainModel> fountainsList = new ArrayList<FountainModel>();
	private List<MoonShard> moonShardsList = new ArrayList<MoonShard>();

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/** The set of platforms in the level */
	protected List<Obstacle> platforms = new ArrayList<Obstacle>();
	/** The set of rocks in the level */
	protected Array<Rock> rocks = new Array<Rock>();


	/** The set of crocodiles in the level */
	protected List<Crocodile> crocodiles = new ArrayList<Crocodile>();

	/** The set of flying monsters in the level */
	protected List<FlyingMonster> flyingMonsters = new ArrayList<FlyingMonster>();

	// Fields to be retrieved as results from collisions
	/** Whether the level failed or not */
	private boolean didFail;
	/** Whether the level is completed or not */
	private boolean isComplete;
	/** Whether the level is completed or not */
	FountainModel.FountainType abilityToAdd;
	/** The most recently touched fountain */
	FountainModel touchedFountain;
	/** Whether or not a collision with spikes has occurred */
	protected boolean spikeCollision;
	/** Last checkpoint the player collided with */
	public FountainModel lastpt = null;
	/** Starting position of the player */
	public float playerStartX;
	public float playerStartY;

	private LevelLoader levelLoader;
//	private int level;

	/** The set of moving platforms in the level */
	protected List<Platform> movingPlatforms = new ArrayList<Platform>();
	/** Detecting collisions with obstacles */
	private boolean isSpikeCollision;
	private boolean isMonsterCollision;
	private boolean isRockCollision;


	/** Sets the ability to add */
	public void setAbilityToAdd(FountainModel.FountainType ability) { abilityToAdd = ability; }
	/** For returning the ability to add */
	public FountainModel.FountainType getAbilityToAdd() { return abilityToAdd; }
	public PlayerModel getPlayer() { return player; }
	public void setPlayer (PlayerModel avatar) { player = avatar; }
	public void setFountainsList(List<FountainModel> fountains) { fountainsList = fountains; }
	public void setGoalDoor (GoalDoor goal) { goalDoor = goal; }
	public FountainModel getTouchedFountain() { return touchedFountain; }
	public void setTouchedFountain(FountainModel fountain) { touchedFountain = fountain; }

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public GameplayController(LevelLoader levelLoader, Level level) {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY, levelLoader, level);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		this.levelLoader = levelLoader;
		MAX_SERENITY = super.MAX_SERENITY;
		super.serenity = MAX_SERENITY;
		super.num_moons = moonShardsList.size();
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
		abilityController.reset();
		removeMS.clear();
		removeRocks.clear();
		super.setSerenity(MAX_SERENITY);
		moonShardsList.clear();
		//abilityTimer = 0;
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

//	/** Adds the ability to the queue */
//	public void addToQueue(FountainModel fountain) {
//		if(fountain.isAvailable()) {
//			abilityController.addAbility(fountain);
//			fountain.setAvailable(false);
//		}
//
//	}

	public boolean getCollisions() {
		return isMonsterCollision || isRockCollision || isSpikeCollision;
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		levelLoader.populateLevel(level.path, scale);
		Sound s = SoundController.startLevelSound();
		s.play(0.8f);

		for (Obstacle p : levelLoader.getPlatforms()) {
			addObject(p);
			if (p instanceof CloudPlatform || p instanceof RegularPlatform) {
				movingPlatforms.add((Platform) p);
			}
			platforms.add(p);
		}

		for(Obstacle o : levelLoader.getObstacles()) {
			addObject(o);
			if (o instanceof Crocodile) {
				crocodiles.add((Crocodile) o);
			} else if (o instanceof FlyingMonster) {
				flyingMonsters.add((FlyingMonster) o);
			} else if (o instanceof Rock) {
				rocks.add((Rock) o);
			}
		}

		goalDoor = levelLoader.getGoalDoor();
		addObject(goalDoor);

		for (FountainModel f : levelLoader.getFountains()) {
			addObject(f);
			fountainsList.add(f);
		}

		for (FountainModel f : levelLoader.getCheckpoints()) {
			addObject(f);
			fountainsList.add(f);
		}

		for (MoonShard shard : levelLoader.getMoonShards()) {
			addObject(shard);
			moonShardsList.add(shard);
			//super.num_moons += 1;
		}

		player = levelLoader.getPlayer();
		playerStartX = player.getX();
//		System.out.println(playerStartX);
		playerStartY = player.getY();
//		System.out.println(playerStartY);
		addObject(player);

		serenity = levelLoader.getCurrentSerenity();
		//TODO: MAKE ADJUSTABLE IN LEVEL FILES

		abilityController = AbilityController.getInstance();
		levelLoader.setAbilityTextures(abilityController);
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
//		if(lastUsed != null && !abilityController.isAbilityActive(lastUsed.getFountainType())) {
////			if(lastUsed.getFountainType() == FountainModel.FountainType.TRANSPARENCY) { player.setTransparencyActive(false); }
//			lastUsed.setAvailable(true);
//			lastUsed = null;
//		}

//		if (!isFailure() && player.getY() < -1) {
//			setFailure(true);
//			return false;
//		}
		if(player.getY() < -1) {
			super.serenity-=100;
			if(lastpt != null) {
				player.setX(lastpt.getX());
				player.setY(lastpt.getY());
			}
			else {
				player.setX(playerStartX);
				player.setY(playerStartY);
			}
		}
		return true;
	}

	public void resetRock(Rock rk) {
		//rk.setPosition(rk.getOriginalPos());
		Rock r = levelLoader.initRock(rk.getOriginalPos().x-1, rk.getOriginalPos().y, rk.type, rk.scale);
		addObject(r);
		rocks.add(r);
		//rk.setReset(false);
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
		super.update(dt);
		super.num_moons = moonShardsList.size();


		// Process actions in object model
		if (super.gameState == GameState.PLAY) {
			setAbility();
			if (inputController.didAbility()) {
				abilityController.useAbility(ABILITY_TIME);
				//abilityController.getLastAbilityUsed().setAvailable(true);
				//if (abilityController.getLastAbilityUsed() != null) abilityController.getLastAbilityUsed().setAvailable(true);
			}
			if(lastUsed != null && !abilityController.isAbilityActive(lastUsed.getFountainType())) {
				if(lastUsed.getFountainType() == FountainModel.FountainType.TRANSPARENCY) { player.setTransparent(false); }
				lastUsed.setAvailable(true);
				lastUsed = null;
			}
			if(getCollisions()) {
				player.isHurt = true;
				super.hurt = true;
				isMonsterCollision = false;
				isRockCollision = false;
				isSpikeCollision = false;
			}
			else {
				player.isHurt = false;
			}
			if(super.serenity > 0) {
				super.serenity -= 1f;
				//super.serenity -= 1f;
				//super.setSerenity(super.currentSerenity()- 0.1f);
			}


			if(super.serenity <= 0) {
				//Do we have another failure gamestate?
				setFailure(true);
			}
			

			// Process actions in object model
			player.setMovement(InputController.getInstance().getHorizontal() *player.getForce());
			player.setJumping(InputController.getInstance().didPrimary() && player.isGrounded());
//		player.setDashing(InputController.getInstance().didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.DASH));
//		player.setFlying(InputController.getInstance().didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT));
//		player.setTransparent(InputController.getInstance().didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY));
//		player.setDashing(InputController.getInstance().didDash());
//		player.setFlying(InputController.getInstance().didFlight());
			if(abilityController.isAbilityActive(FountainModel.FountainType.DASH)){
				player.setDashing(InputController.getInstance().didDoubleDash() || abilityController.isApplyingDash());
			}
			player.setFlying(inputController.didDoubleJump() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT));
			player.setTransparent(abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY));
			//============================UNCOMMENT FOR WALK ANIMATION===============
			player.setWalking(InputController.getInstance().didWalk());
			//==============================++++++++++++++++++++++++++===============

//		if (InputController.getInstance().didAbility() && abilityState != AbilityState.LAME) {
//			player.setPressedAbility(true);
//		}
			//player.setPressedAbility(InputController.getInstance().didAbility() && abilityState != AbilityState.LAME);

			player.applyForce();
			if (player.isJumping()) {
//			SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
			}

			//add sound effects
			if (player.isJumping() && player.isGrounded()) {
				player.setGrounded(false);
				Sound s = SoundController.jumpSound();
				s.play(0.8f);
			}
//			if (!inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT))
//				player.setFlying(false);
			if (abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)
					&& inputController.didDoubleJump()) {
				player.setGrounded(false);
				player.setFlying(true);
				Sound s = SoundController.flightSound();
				s.play(0.8f);
			}

			if(abilityController.isAbilityActive(FountainModel.FountainType.DASH) &&
					inputController.didDoubleDash()){
				Sound s = SoundController.dashSound();
				s.play(0.7f);
			}

			//moving the platforms
			for(Platform platform : movingPlatforms){
				Vector2 newPos = platform.getPosition();
				newPos.add(platform.getVelocity());
				if(Math.abs(newPos.x - platform.getOriginalPosition().x) > platform.getHorizontalRadius())
					platform.setVelocity(platform.getVelocity().scl(-1, 1));
				if(Math.abs(newPos.y - platform.getOriginalPosition().y) > platform.getVerticalRadius())
					platform.setVelocity(platform.getVelocity().scl(1,-1));
				platform.setPosition(newPos);
			}

			//moving the shards
			for(MoonShard shard: moonShardsList){
				Vector2 newPos = shard.getPosition();
				newPos.add(shard.getVelocity());
				if(Math.abs(newPos.x - shard.getOriginalPosition().x) > shard.getHorizontalRadius())
					shard.setVelocity(shard.getVelocity().scl(-1, 1));
				if(Math.abs(newPos.y - shard.getOriginalPosition().y) > shard.getVerticalRadius())
					shard.setVelocity(shard.getVelocity().scl(1,-1));
				shard.setPosition(newPos);
			}

			for(Crocodile croc : crocodiles){
				Vector2 newPos = croc.getPosition();
				newPos.add(croc.getVelocity());
				if(Math.abs(newPos.x - croc.getOriginalPosition().x) > croc.getHorizontalRadius()) {
					croc.setVelocity(croc.getVelocity().scl(-1, 1));
					//System.out.println("hi");
				}
				if(newPos.x - croc.getOriginalPosition().x > croc.getHorizontalRadius()) {
					croc.setFaceRight(false);
				}
				if(croc.getOriginalPosition().x - newPos.x  > croc.getHorizontalRadius()) {
					croc.setFaceRight(true);
				}
				if(Math.abs(newPos.y - croc.getOriginalPosition().y) > croc.getVerticalRadius()) {
					croc.setVelocity(croc.getVelocity().scl(1, -1));
				}
				croc.setPosition(newPos);
			}

			for(FlyingMonster fly : flyingMonsters){
				Vector2 newPos = fly.getPosition();
				newPos.add(fly.getVelocity());
				if(Math.abs(newPos.x - fly.getOriginalPosition().x) > fly.getHorizontalRadius())
					fly.setVelocity(fly.getVelocity().scl(-1, 1));
				if(newPos.x - fly.getOriginalPosition().x > fly.getHorizontalRadius()) {
					fly.setFaceRight(false);
				}
				if(fly.getOriginalPosition().x - newPos.x  > fly.getHorizontalRadius()) {
					fly.setFaceRight(true);
				}
				if(Math.abs(newPos.y - fly.getOriginalPosition().y) > fly.getVerticalRadius())
					fly.setVelocity(fly.getVelocity().scl(1,-1));
				fly.setPosition(newPos);
			}

			//resetting the rocks
			for(int i = 0; i < rocks.size; i++) {
				Rock rk = rocks.get(i);
				if (rk.getReset() || rk.getY() < 0) {
					resetRock(rk);
					rocks.removeIndex(i);
				}
			}
			//if()

//		if (player.isTimerDone()) {
//			abilityState = AbilityState.LAME;
//		}
			//System.out.println("ability state " + abilityState );
			// If we use sound, we must remember this.
//		SoundController.getInstance().update();


		}
	}

	public boolean detectLocation(float x,float y) {
		return player.getX() == x && player.getY() == y;
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
				// Only set grounded to true on CloudPlatforms if the player is transparent
				if(bd1 instanceof CloudPlatform || bd2 instanceof CloudPlatform){
					CloudPlatform cp;
					if(bd1 instanceof CloudPlatform)
						cp = (CloudPlatform) bd1;
					else
						cp = (CloudPlatform) bd2;
					if(cp.getPosition().y > player.getPosition().y){
						contact.setEnabled(false);
					}
					if(player.isTransparent()){
						player.setGrounded(true);
					}
				} else if (!(bd1 instanceof FountainModel || bd2 instanceof FountainModel
						|| bd1 instanceof MoonShard || bd2 instanceof MoonShard)){
					player.setGrounded(true);
				}
				sensorFixtures.add(player == bd1 ? fix2 : fix1); // Could have more than one ground
			}


			//System.out.println("grounded" + player.isGrounded());
			//check for fountains
			for (int i = 0; i < fountainsList.size(); i++) {
				if ((bd1 == player   && bd2 == fountainsList.get(i)) ||
						(bd1 == fountainsList.get(i) && bd2 == player)) {
					//System.out.println(bd1);
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
						touchedFountain.setAvailable(false);
						abilityController.addAbility(touchedFountain);
					}
					if(touchedFountain.getFountainType() == FountainModel.FountainType.RESTORE && touchedFountain.isAvailable()) {
						Sound s = SoundController.touchFountainSound();
						s.play(0.8f);
						//toRestore = true;
//						editSaveJson(false);
						lastpt = touchedFountain;
						level.checkpointsPassed++;
						editSaveJson(false);
						super.serenity = Math.min(super.serenity+5000,MAX_SERENITY);
						touchedFountain.setAvailable(false);
					}
				}

			}

			//check for moon shard collision
			for (int i = 0; i < moonShardsList.size(); i++) {
				MoonShard ms = moonShardsList.get(i);
				if ((bd1 == ms && bd2 == player)
						|| (bd1 == player && bd2 == ms) && !ms.isTaken()) {
					ms.setTaken(true);
					moonShardsList.remove(ms);
					//ms.deactivatePhysics(world);
					//removeMS.add(ms);
					Sound s = SoundController.moonShardSound();
					s.play(0.3f);
				}
			}

			//detect rock collision with player
			for (int i = 0; i < rocks.size; i++) {
				Rock r = rocks.get(i);
				if ((bd1 == r && bd2 == player)
						|| (bd1 == player && bd2 == r)) {
					if (!player.isTransparent()) {
						isRockCollision = true;
						super.serenity -= 500;
						if (super.serenity < 0) {
							super.serenity = 0;
						}
					}
				}
			}

			//detect rock collision with platforms
			for (int i = 0; i < rocks.size; i++) {
				Rock r = rocks.get(i);
				for (int j = 0; j < platforms.size(); j++) {
					Obstacle p = platforms.get(j);
					if ((bd1 == r && bd2 == p)
							|| (bd1 == p && bd2 == r)) {
						r.setReset(true);
						removeRocks.add(r);
						rx = r.getX()*r.getDrawScale().x;
						ry = r.getY()*r.getDrawScale().y;
						ox = r.getOriginalPos().x;
						oy = r.getOriginalPos().y;
						//setSmoke(true);

					}
				}
			}


			// Check for win condition
			if (((bd1 == player   && bd2 == goalDoor) ||
					(bd1 == goalDoor && bd2 == player)))  {
				numMoonCollected = 0;
				for (int i =0; i<moonShardsList.size(); i++) {
					if (moonShardsList.get(i).isTaken()) {
						numMoonCollected++;
					}
				}
				if (numMoonCollected == moonShardsList.size()) {
					lastpt = null;
					Sound s = SoundController.goalSound();
					s.play(0.1f);editSaveJson(true);
					setComplete(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	public void beginContact(Contact contact) {
//		Fixture fix1 = contact.getFixtureA();
//		Fixture fix2 = contact.getFixtureB();
//
//		Body body1 = fix1.getBody();
//		Body body2 = fix2.getBody();
//
//		Object fd1 = fix1.getUserData();
//		Object fd2 = fix2.getUserData();
//
//		try {
//			Obstacle bd1 = (Obstacle)body1.getUserData();
//			Obstacle bd2 = (Obstacle)body2.getUserData();
//
//			// See if we have landed on the ground.
//			if ((player.getSensorName().equals(fd2) && player != bd1) ||
//					(player.getSensorName().equals(fd1) && player != bd2)) {
//				// Only set grounded to true on CloudPlatforms if the avatar is transparent
//				if(bd1 instanceof CloudPlatform || bd2 instanceof CloudPlatform){
//					if(player.isTransparent()){
//						player.setGrounded(true);
//					}
//				} else {
//					player.setGrounded(true);
//				}
//				sensorFixtures.add(player == bd1 ? fix2 : fix1); // Could have more than one ground
//			}
//
//			// Check for win condition
//			if ((bd1 == player   && bd2 == goalDoor) ||
//					(bd1 == goalDoor && bd2 == player)) {
//				setComplete(true);
//			}
//
//			//check for fountains
//			for (int i = 0; i < fountainsList.size(); i++) {
//				if ((bd1 == player   && bd2 == fountainsList.get(i)) ||
//						(bd1 == fountainsList.get(i) && bd2 == player)) {
//					setAbilityToAdd(fountainsList.get(i).getFountainType());
//					if(bd2 == fountainsList.get(i)) {
//						touchedFountain = (FountainModel)bd2;
//					}
//					else if(bd1 == fountainsList.get(i)) {
//						touchedFountain = (FountainModel)bd1;
//					}
//					if(touchedFountain.isAvailable()){
//						Sound s = SoundController.touchFountainSound();
//						s.play(0.8f);
//					}
//				}
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

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

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {
		if (gameState == GameState.PLAY) {
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

				// Check object types for CloudPlatform & PlayerModel collision
				if ((bd1 instanceof CloudPlatform && bd2 instanceof PlayerModel)
						|| (bd1 instanceof PlayerModel && bd2 instanceof CloudPlatform)) {
					// Get the PlayerModel and CloudPlatform
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
					if(!dm.isTransparent() || !platform.isValidCollision(dm)) {
						contact.setEnabled(false);
					} else if (dm.isTransparent() && platform.isValidCollision(dm)){
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

				if((bd1 instanceof Platform && bd2 instanceof PlayerModel)
						|| (bd1 instanceof PlayerModel && bd2 instanceof Platform
						&& !(bd1 instanceof  CloudPlatform) && !(bd2 instanceof  CloudPlatform))) {
					final PlayerModel dm;
					final Platform platform;
					if(bd1 instanceof PlayerModel){
						dm = (PlayerModel) bd1;
						platform = (Platform) bd2;
					}
					else {
						dm = (PlayerModel) bd2;
						platform = (Platform) bd1;
					}
					if(!(platform instanceof CloudPlatform)){
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								Vector2 newPlayerPos = dm.getPosition().add(platform.getVelocity());
								dm.setPosition(newPlayerPos);
							}
						});
					}

				}

				if(bd1 instanceof Crocodile && bd2 instanceof PlayerModel || bd2 instanceof  Crocodile
						&& bd1 instanceof  PlayerModel) {
					isMonsterCollision = true;
					//TODO: add cooldown
					super.serenity -= 5;
					if(super.serenity < 0) {
						super.serenity = 0;
					}
				}
				if(bd1 instanceof FlyingMonster && bd2 instanceof PlayerModel || bd2 instanceof  FlyingMonster
						&& bd1 instanceof  PlayerModel) {
					isMonsterCollision = true;
					super.serenity -= 5;
					if(super.serenity < 0) {
						super.serenity = 0;
					}
				}

				// Check object types for SpikedPlatform & PlayerModel collision
				if ((bd1 instanceof SpikedPlatform && bd2 instanceof PlayerModel)
						|| (bd1 instanceof PlayerModel && bd2 instanceof SpikedPlatform)) {
					// Find and assign the PlayerModel object and SpikedPlatform Object
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
						super.serenity -= 100;
						isSpikeCollision = true;
						if(super.serenity < 0) {
							super.serenity = 0;
						}
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
					else {
						isSpikeCollision = false;
					}
				}

			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void setAbility() {
		levelLoader.setPlayerTextures(abilityController);
		if(abilityController.isAbilityActive(FountainModel.FountainType.DASH)) {
			lastUsed = abilityController.getLastAbilityUsed();
		}
		else if(abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)) {
			lastUsed = abilityController.getLastAbilityUsed();
		}
		else if(abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY)) {
			lastUsed = abilityController.getLastAbilityUsed();
		}
	}

	public String createJsonString(String name, Boolean complete, Boolean unlock, int checkpts, int serenityLeft, String path, String abilQueue) {
//		Map<String, String> objectToReturn = new HashMap<String, String>();
//		objectToReturn.put("name", lvl.name);
//		objectToReturn.put("completed", Boolean.toString(false));
//		objectToReturn.put("unlocked", Boolean.toString(true));
//		objectToReturn.put("checkpoints-passed", Integer.toString(checkpointsPassed));
//		objectToReturn.put("serenity-left", Integer.toString(super.serenity));
//		objectToReturn.put("path", lvl.path);
//		objectToReturn.put("ability-queue", abilityController.getAbilityQueue().abilities.toString());

		String text = "{ \n \"name\": \"" + name + "\",\n \"completed\": " + complete + ",\n " + "\"unlocked\": " + unlock + ",\n \"checkpoints\": " + checkpts + ",\n \"serenity-left\": " + serenityLeft + ",\n \"path\": \"" + path + "\",\n \"ability-queue\": " + abilQueue + " \n}";

//		String text = "{ \n name: \"" + name + "\",\n completed: " + complete + ",\n " + "unlocked: " + unlock + ",\n checkpoints: " + checkpointsPassed + ",\n serenity-left: " + super.serenity + ",\n path: \"" + level.path + "\",\n ability-queue: " + abilityController.getAbilityQueue().abilities.toString() + " \n}";

//		Json json = new Json();
//		String jsonRaw = json.toJson(objectToReturn);
//		String text = json.prettyPrint(jsonRaw);
//		int strLen = text.length();
//		String replaceText = text.substring(1,strLen-1);
//		replaceText = replaceText.replaceAll("\\{", "");
//		replaceText = replaceText.replaceAll("\\}", "");
//		replaceText = replaceText.replaceAll("class: java.lang.String" , "");
//		replaceText = replaceText.replaceAll("value:" , "");
//		text = "{" + replaceText + "}";
		return text;
	}

	public void editSaveJson(Boolean comp) {
		String file = "jsons/saved_game.json";
		JsonReader jsonReader = new JsonReader();
		JsonValue savedGameFile = jsonReader.parse(Gdx.files.internal(file));
//		Array<Level> levels = new Array<Level>();


		String text = "{\"levels: \": [";
		JsonValue levelsJson = savedGameFile.get("levels");
		if (levelsJson != null) {
//			levels = new Array<Level>();
			int num = 0;
			for (JsonValue entry = levelsJson.child; entry != null; entry = entry.next) {
				Level newLevel = new Level();
				newLevel.name = entry.getString("name");
				newLevel.num = num;
				newLevel.complete = entry.getBoolean("completed");
				newLevel.available = entry.getBoolean("unlocked");
				newLevel.checkpointsPassed = entry.getInt("checkpoints");
				newLevel.currentSerenity = entry.getInt("serenity-left");
				newLevel.path = entry.getString("path");

				String lvlText;

				if (level != newLevel || comp == true) {
					lvlText = createJsonString(entry.getString("name"), entry.getBoolean("completed"), entry.getBoolean("unlocked"), entry.getInt("checkpoints"), entry.getInt("serenity-left"), entry.getString("path"), null);
				}
				else
					lvlText = createJsonString(level.name, level.complete, level.available, level.checkpointsPassed, (int) super.serenity, level.path, abilityController.getAbilityQueue().abilities.toString());
				text = text + "\n" + lvlText;
//				levels.add(newLevel);
				num++;
			}
		}
		text = text + "\n]\n}";
		FileHandle fileToPrint = Gdx.files.local("jsons/saved_game2.json");
		fileToPrint.writeString(text, false);
	}
}