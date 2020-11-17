package edu.cornell.gdiac.amaris;

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
import edu.cornell.gdiac.amaris.objects.CloudPlatform;
import edu.cornell.gdiac.amaris.objects.Platform;
import edu.cornell.gdiac.amaris.objects.MoonShard;
import edu.cornell.gdiac.amaris.objects.SpikedPlatform;
import edu.cornell.gdiac.amaris.objects.*;
import edu.cornell.gdiac.amaris.platform.FountainModel;
import edu.cornell.gdiac.amaris.platform.PlayerModel;
import edu.cornell.gdiac.amaris.util.*;
import edu.cornell.gdiac.amaris.obstacle.*;
import edu.cornell.gdiac.amaris.platform.PlayerModel.playerState;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;

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
	private List<SignPost> signpostsList = new ArrayList<SignPost>();

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/** The set of platforms in the level */
	protected List<Obstacle> platforms = new ArrayList<Obstacle>();
	/** The set of windows in the level */
	protected List<RegularPlatform> windows = new ArrayList<RegularPlatform>();
	protected ArrayList<RegularPlatform> windowsStepped = new ArrayList<RegularPlatform>();
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
	/** Serenity when player reaches checkpoint */
	public float checkpointSerenity;

	private LevelLoader levelLoader;
//	private int level;

	/** The set of moving platforms in the level */
	protected List<Platform> movingPlatforms = new ArrayList<Platform>();
	/** Detecting collisions with obstacles */
	private boolean isSpikeCollision;
	private boolean isMonsterCollision;
	private boolean isRockCollision;
	private float rockTimer = 0;


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
	public GameplayController(LevelLoader levelLoader, Level level, boolean mute) {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY, levelLoader, level, mute);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		this.levelLoader = levelLoader;
		super.num_moons = moonShardsList.size();
		numMoonCollected = 0;
		//Start the music
		String s = this.levelLoader.level.background;
//		Music m = null;
//		if(i == 0) {
//			m = SoundController.level1Music();
//			SoundController.playMusic(m, 0.16f, true);
//		}
//		if(i == 1) {
//			m = SoundController.level2Music();
//			SoundController.playMusic(m, 0.16f, true);
//		}
//		if(i == 2) {
//			m = SoundController.level3Music();
//			SoundController.playMusic(m, 0.2f, true);
//		}
	}

	public void clear() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		for(Rock rock : rocks) {
			rock.deactivatePhysics(world);
		}
		for(Rock rock : removeRocks) {
			rock.deactivatePhysics(world);
		}
		for(MoonShard ms : removeMS) {
			ms.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		abilityController.reset();
		rocks.clear();
		removeMS.clear();
		removeRocks.clear();
		removeWindows.clear();
		smokesCoord.clear();
		fountainsList.clear();
		signpostsList.clear();
		movingPlatforms.clear();
		crocodiles.clear();
		flyingMonsters.clear();
		playerSmokeCoord.clear();
		windowsStepped.clear();
		moonShardsList.clear();
		windows.clear();
		lastpt = null;
		checkpointSerenity = level.maxSerenity;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		super.serenityOffset = 0;
		clear();

		super.setSerenity(MAX_SERENITY);

		//abilityTimer = 0;
		world = new World(gravity,false);
		world.setContactListener(this);
		lastpt = null;
		checkpointSerenity = level.maxSerenity;

		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	public boolean getCollisions() {
		return isMonsterCollision || isRockCollision || isSpikeCollision;
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		levelLoader.populateLevel(level.path, scale);
		Sound s = SoundController.startLevelSound();
		SoundController.playSound(s, 0.8f);

		for (Obstacle p : levelLoader.getPlatforms()) {
			addObject(p);
			p.setY(p.getY() + 4f);
			if (p instanceof Platform) {
				Platform platform = (Platform) p;
				((Platform) p).setOriginalPosition(new Vector2(p.getX(), p.getY()));
				movingPlatforms.add(platform);
			}
			platforms.add(p);
		}

		for(Obstacle o : levelLoader.getObstacles()) {
			addObject(o);
            o.setY(o.getY() + 4);
			if (o instanceof Crocodile) {
				crocodiles.add((Crocodile) o);
			} else if (o instanceof FlyingMonster) {
				flyingMonsters.add((FlyingMonster) o);
			} else if (o instanceof Rock) {
				((Rock) o).setOriginalPos(new Vector2(o.getX(), o.getY()));
				rocks.add((Rock) o);
			}
		}

		//windows
		for (RegularPlatform w: levelLoader.getWindows()) {
			addObject(w);
			w.setOriginalPosition(new Vector2(w.getX(), w.getY()));
			movingPlatforms.add(w);
			windows.add(w);
		}

		goalDoor = levelLoader.getGoalDoor();
		addObject(goalDoor);
		goalDoor.setY(goalDoor.getY() + 4);
		for (BoxObstacle sensor : goalDoor.sensors) {
			sensor.setY(sensor.getY() + 4);
			addObject(sensor);
		}


		for (FountainModel f : levelLoader.getFountains()) {
			addObject(f);
            f.setY(f.getY() + 4);
			fountainsList.add(f);
		}

		for (FountainModel f : levelLoader.getCheckpoints()) {
			addObject(f);
            f.setY(f.getY() + 4);
			fountainsList.add(f);
		}

		for (MoonShard shard : levelLoader.getMoonShards()) {
			addObject(shard);
            shard.setY(shard.getY() + 4);
            shard.setOriginalPosition(new Vector2(shard.getX(), shard.getY()));
			moonShardsList.add(shard);
			//super.num_moons += 1;
		}

		for (SignPost post : levelLoader.getSignposts()) {
			post.setY(post.getY() + 4);
			post.messagePos.set(post.messagePos.x, post.messagePos.y + 4*scale.y);
			addObject(post);
			signpostsList.add(post);
		}



		player = levelLoader.getPlayer();
		playerStartX = player.getX();
		playerStartY = player.getY();
		addObject(player);
		player.setY(playerStartY+4);
		playerStartY += 4;

		MAX_SERENITY = level.getMaxSerenity();
		super.MAX_SERENITY = MAX_SERENITY;
		super.THREE_STAR = level.threeStars;
		super.TWO_STAR = level.twoStars;
		super.ONE_STAR = level.oneStar;
		serenity = level.maxSerenity;
		abilityController = AbilityController.getInstance();
		levelLoader.setAbilityTextures(abilityController);
	}

	private FountainModel findFountain(String id) {
	    for (FountainModel fountain : fountainsList) {
	        if (fountain.getName().equals(id)) {
	            return fountain;
            }
        }
        return null;
    }

    private MoonShard findMoonShard(String id) {
        for (MoonShard m : moonShardsList) {
            if (m.getName().equals(id)) {
                return m;
            }
        }
        return null;
    }

	private RegularPlatform findWindow(String id) {
		for (RegularPlatform w : windows) {
			if (w.getName().equals(id)) {
				return w;
			}
		}
		return null;
	}


	private void respawn(){
		Sound s = SoundController.hurtSound();
		SoundController.playSound(s, 0.3f);
//        super.serenity -= MAX_SERENITY/4;
		super.serenity = checkpointSerenity;
        lastUsed = null;
        for(SignPost sign : levelLoader.getSignposts()) {
        	sign.hasRead = false;
		}
        if(lastpt != null) {
			// no abilities currently being used
			abilityController.reset();

			// set fountains to state at lastpt
			for (FountainModel f : fountainsList) {
				f.setAvailable(true);
			}
			for (String id : lastpt.getFountainsQueued()) {
				FountainModel fountain = findFountain(id);
				abilityController.addAbility(fountain);
				fountain.setAvailable(false);
			}
			lastpt.setAvailable(false);


			// set moon shards to state at lastpt
			for (MoonShard m : removeMS) {
				moonShardsList.add(m);
			}

			for (MoonShard m : moonShardsList) {
				m.setTaken(false);
			}
			removeMS = new Array<MoonShard>();
			for (String id : lastpt.getMoonShardsCollected()) {
				(findMoonShard(id)).setTaken(true);
				removeMS.add(findMoonShard(id));
			}

			super.num_moons = moonShardsList.size() - removeMS.size;

			// set player position to lastpt
			player.setX(lastpt.getX());
			player.setY(lastpt.getY());

			//set windows broken
			for (RegularPlatform w : windows) {
				if (lastpt.getWindowsBroken().contains(w.getName())) {
					w.setStepCount(w.getStepCount());
				} else {
					w.setStepCount(0);

				}
			}
		}


        else {
        	player.setY(-5);
        	setFailure(true);
//			// no abilities currently being used
//			abilityController.reset();
//
//			// reset all fountains
//			for (FountainModel f : fountainsList) {
//				f.setAvailable(true);
//			}
//
//			// reset all moon shards
//			for (MoonShard m : removeMS) {
//				moonShardsList.add(m);
//			}
//			for (MoonShard m : moonShardsList) {
//				m.setTaken(false);
//			}
//			removeMS = new Array<MoonShard>();
//
//			// set player position to start position
//            player.setX(playerStartX);
//            player.setY(playerStartY);

        }
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

		if(player.getY() < -5) {
			respawn();
		}
		return true;
	}

	public void resetRock(Rock rk) {
		rk.setPosition(rk.getOriginalPos());
		rk.setVY(rk.getOriginalVel().y);
		rk.setVX(rk.getOriginalVel().x);
//		Rock r = levelLoader.initRock(rk.getOriginalPos().x-1, rk.getOriginalPos().y, rk.type, rk.scale);
//		addObject(r);
//		rocks.add(r);
		rockTimer = 0;
		rk.setDraw(true);
		rk.setCollide(true);
		rk.setReset(false);

	}

	public float map(Vector2 originalRange, Vector2 newRange, float originalValue){
//		System.out.println("OV: " + originalValue + "    OR: " + originalRange );
		float fDiff = originalValue - originalRange.x;
//		System.out.println("FDIFF: " + fDiff);
		float fProp = fDiff / (originalRange.y - originalRange.x);
//		System.out.println("FPROP: " + fProp);
		float sMod = newRange.y - newRange.x;
//		System.out.println("SMOD: " + sMod);
		return (fProp * sMod) + newRange.x;
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
		if(numMoonCollected == moonShardsList.size()) {
			goalDoor.setComplete(true);
			//goalDoor.canComplete = true;
		}
		if(player.isTransparent()) {
			for(Obstacle pl : platforms) {
				if(pl instanceof CloudPlatform) {
					((CloudPlatform) pl).setTransparent(true);
				}
			}
		}
		else {
			for(Obstacle pl : platforms) {
				if(pl instanceof CloudPlatform) {
					((CloudPlatform) pl).setTransparent(false);
				}
			}
		}


		// Process actions in object model
		if (super.gameState == GameState.PLAY) {
			setAbility();
			if (inputController.didAbility()) {
				abilityController.useAbility(ABILITY_TIME);
//				abilityController.useAbility(ABILITY_TIME);
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
				if(InputController.getInstance().didDoubleDash() || abilityController.isApplyingDash()) {
					player.setDashState(true);
				}
				else if((player.ps == playerState.DASH && player.isGrounded())) {
					player.setDashState(false);
				}

			}
			player.setFlying(inputController.didDoubleJump() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT));
			if(inputController.didDoubleJump() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)) {
				player.setFlightState(true);
			}
			else if (player.ps == playerState.FLIGHT && player.isGrounded()) {
				player.setFlightState(false);
			}
			player.setTransparent(abilityController.isAbilityActive(FountainModel.FountainType.TRANSPARENCY));
			if(InputController.getInstance().didWalk() && player.ps != playerState.DASH && player.ps != playerState.JUMP && player.ps != playerState.FLIGHT) {
				player.setWalking(true);
			}
			else if (player.ps == playerState.WALK && player.isGrounded()) {
				player.setWalking(false);
			}


//		if (InputController.getInstance().didAbility() && abilityState != AbilityState.LAME) {
//			player.setPressedAbility(true);
//		}
			//player.setPressedAbility(InputController.getInstance().didAbility() && abilityState != AbilityState.LAME);

			player.applyForce();
			if (player.isJumping()) {
//			SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
			}

			levelLoader.setSignPostFile();
			levelLoader.setWindowTexture();

			if (player.isJumping() && player.isGrounded()) {
				//System.out.println("setting true");
				player.setJumpState(true);
				player.setGrounded(false);
				Sound s = SoundController.jumpSound();
				SoundController.playSound(s, 0.6f);
			}

			if (player.ps == playerState.JUMP && player.isGrounded()) {
				//System.out.println("setting false");
				player.setJumpState(false);
			}

			if (player.ps == playerState.JUMP) {
				for (int i = 0; i < windows.size(); i++) {
					RegularPlatform w = windows.get(i);
					w.setOnWindow(false);
				}
			}

			//System.out.println(player.ps == playerState.JUMP);
			//System.out.println(player.ps);

//			if (!inputController.didActivateAbility() && abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT))
//				player.setFlying(false);
			if (abilityController.isAbilityActive(FountainModel.FountainType.FLIGHT)
					&& inputController.didDoubleJump()) {
				player.setGrounded(false);
				player.setFlying(true);
				Sound s = SoundController.flightSound();
				SoundController.playSound(s, 0.8f);
			}

			if(abilityController.isAbilityActive(FountainModel.FountainType.DASH) &&
					inputController.didDoubleDash()){
				Sound s = SoundController.dashSound();
				SoundController.playSound(s, 0.7f);
			}

			//moving the platforms
			for(Platform platform : movingPlatforms){
				if(platform instanceof CloudPlatform){
					Vector2 newPos = platform.getPosition();
					Vector2 platformVel = platform.getVelocity().cpy();
					double mod;
					double c = 2;
					if (Double.isNaN((float)Math.sin((newPos.dst(platform.getOriginalPosition()) / Math.sqrt(Math.pow(platform.getHorizontalRadius(),2) + Math.pow(platform.getVerticalRadius(),2)))))) {
						mod = -1;
					} else {
						mod = map(new Vector2(0,1), new Vector2(0.25f, 1.6f), 1 - (float)Math.sin((newPos.dst(platform.getOriginalPosition()) /
								Math.sqrt(Math.pow(platform.getHorizontalRadius(),2) + Math.pow(platform.getVerticalRadius(),2)))
								* Math.PI / 2));
					}
					newPos.add(platformVel.scl((float)(mod * c)));
					if(Math.abs(newPos.x - platform.getOriginalPosition().x) > platform.getHorizontalRadius())
						platform.setVelocity(platform.getVelocity().scl(-1, 1));
					if(Math.abs(newPos.y - platform.getOriginalPosition().y) > platform.getVerticalRadius())
						platform.setVelocity(platform.getVelocity().scl(1,-1));
					platform.setPosition(newPos);
				} else {
					Vector2 newPos = platform.getPosition();
					newPos.add(platform.getVelocity());
					if(Math.abs(newPos.x - platform.getOriginalPosition().x) > platform.getHorizontalRadius())
						platform.setVelocity(platform.getVelocity().scl(-1, 1));
					if(Math.abs(newPos.y - platform.getOriginalPosition().y) > platform.getVerticalRadius())
						platform.setVelocity(platform.getVelocity().scl(1,-1));
					platform.setPosition(newPos);
				}
			}

			//moving the shards
			for(MoonShard shard: moonShardsList){

				Vector2 newPos = shard.getPosition();
				Vector2 shardVel = shard.getVelocity().cpy();
				double mod;
				double c = 2;
				if (Double.isNaN((float)Math.sin((newPos.dst(shard.getOriginalPosition()) / Math.sqrt(Math.pow(shard.getHorizontalRadius(),2) + Math.pow(shard.getVerticalRadius(),2)))))) {
					mod = -1;
				} else {
					mod = map(new Vector2(0,1), new Vector2(0.25f, 1.6f), 1 - (float)Math.sin((newPos.dst(shard.getOriginalPosition()) /
							Math.sqrt(Math.pow(shard.getHorizontalRadius(),2) + Math.pow(shard.getVerticalRadius(),2)))
							* Math.PI / 2));
				}
				newPos.add(shardVel.scl((float)(mod * c)));
				if(Math.abs(newPos.x - shard.getOriginalPosition().x) > shard.getHorizontalRadius())
					shard.setVelocity(shard.getVelocity().scl(-1, 1));
				if(Math.abs(newPos.y - shard.getOriginalPosition().y) > shard.getVerticalRadius())
					shard.setVelocity(shard.getVelocity().scl(1,-1));
				shard.setPosition(newPos);

//				Vector2 newPos = shard.getPosition();
//				newPos.add(shard.getVelocity());
//				if(Math.abs(newPos.x - shard.getOriginalPosition().x) > shard.getHorizontalRadius())
//					shard.setVelocity(shard.getVelocity().scl(-1, 1));
//				if(Math.abs(newPos.y - shard.getOriginalPosition().y) > shard.getVerticalRadius())
//					shard.setVelocity(shard.getVelocity().scl(1,-1));
//				shard.setPosition(newPos);
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

			for(int i = 0; i < rocks.size; i++) {
				Rock rk = rocks.get(i);
				if(rockTimer > 1.5) {
					rk.setReset(true);
				}
			}
			//resetting the rocks
			for(int i = 0; i < rocks.size; i++) {
				Rock rk = rocks.get(i);
//				Vector2 newPos = rk.getPosition();
//				newPos.add(new Vector2(0f, -0.05f));
//				rk.setPosition(newPos);
				if (rk.getReset()) {
					resetRock(rk);
					//rocks.removeIndex(i);
				}
			}

			rockTimer += Gdx.graphics.getDeltaTime();

//		if (player.isTimerDone()) {
//			abilityState = AbilityState.LAME;
//		}
			//System.out.println("ability state " + abilityState );
			// If we use sound, we must remember this.
//		SoundController.getInstance().update();

			//System.out.println(abilityController.getTimeLeftForAbility());
		}

		player.setOnMovingPlatform(false);
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
						for (int j = 0; j < windows.size(); j++) {
							RegularPlatform w = windows.get(j);
							w.setOnWindow(false);
						}
					}
				} else if (!(bd1 instanceof FountainModel || bd2 instanceof FountainModel
						|| bd1 instanceof MoonShard || bd2 instanceof MoonShard
						|| bd1 instanceof SignPost || bd2 instanceof SignPost ||
						(bd1 instanceof GoalDoor && !((GoalDoor) bd1).canComplete) ||
						(bd2 instanceof GoalDoor && !((GoalDoor) bd2).canComplete) ||
						(bd1 instanceof BoxObstacle && goalDoor.sensors.contains((BoxObstacle) bd1, true))
						|| (bd2 instanceof BoxObstacle && goalDoor.sensors.contains((BoxObstacle) bd2, true)))) {
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
						SoundController.playSound(s, 0.8f);
						touchedFountain.setAvailable(false);
						abilityController.addAbility(touchedFountain);
					}
					if(touchedFountain.getFountainType() == FountainModel.FountainType.RESTORE && touchedFountain.isAvailable()) {
						Sound s = SoundController.touchFountainSound();
						SoundController.playSound(s, 0.8f);
						//toRestore = true;
//						editSaveJson(false);
						lastpt = touchedFountain;
						level.checkpointsPassed++;
						editSaveJson(false);
						super.serenity = Math.min(super.serenity+(MAX_SERENITY/2),MAX_SERENITY);
						checkpointSerenity = super.serenity;
						touchedFountain.setAvailable(false);
						touchedFountain.setFountainsQueued(abilityController.getAbilityQueue());
						ArrayList<MoonShard> taken = new ArrayList<MoonShard>();
						for (MoonShard moon : removeMS) {
						    if (moon.isTaken()) {
						        taken.add(moon);
                            }
                        }
						touchedFountain.setMoonShardsCollected(taken);
//						ArrayList<RegularPlatform> brokenWindows = new ArrayList<RegularPlatform>();
//						for (RegularPlatform w: windowsStepped) {
//							if(w.getStepCount() )
//						}
						touchedFountain.setWindowsBroken(windowsStepped);
					}
				}
			}

			//check for moon shard collision
			for (int i = 0; i < moonShardsList.size(); i++) {
				MoonShard ms = moonShardsList.get(i);
				if ((bd1 == ms && bd2 == player)
						|| (bd1 == player && bd2 == ms)) {
                    if (!ms.isTaken()) {
                        ms.setTaken(true);
                        moonShardsList.remove(ms);
                        //ms.deactivatePhysics(world);
                        removeMS.add(ms);
                        Sound s = SoundController.moonShardSound();
                        SoundController.playSound(s, 0.6f);
                    }
                }
			}

			//detect rock collision with player
			for (int i = 0; i < rocks.size; i++) {
				Rock r = rocks.get(i);
				if (r.getCollide() == true) {
					if ((bd1 == r && bd2 == player)
							|| (bd1 == player && bd2 == r)) {
						r.setDraw(false);
						if (!player.isTransparent()) {
							isRockCollision = true;
							Sound s = SoundController.hurtSound();
							SoundController.playSound(s, 0.3f);
							super.serenity -= MAX_SERENITY / 5;
							if (super.serenity < 0) {
								super.serenity = 0;
							}
						}

						rx = r.getX() * r.getDrawScale().x;
						ry = r.getY() * r.getDrawScale().y;
						ox = r.getOriginalPos().x;
						oy = r.getOriginalPos().y;
						playerSmokeCoord.add(ox, oy, rx, ry);
						if (smokesCoord.size == rocks.size) {
							smokesCoord.removeIndex(0);
						}


						setSmoke(true);
						r.setCollide(false);
					}
				}
			}

			//detect rock collision with platforms
			for (int i = 0; i < rocks.size; i++) {
				Rock r = rocks.get(i);
				if (r.getCollide() == true) {
					for (int j = 0; j < platforms.size(); j++) {
						Obstacle p = platforms.get(j);
						if ((bd1 == r && bd2 == p)
								|| (bd1 == p && bd2 == r)) {
							r.setDraw(false);
							//r.setReset(true);
							//removeRocks.add(r);
							rx = r.getX() * r.getDrawScale().x;
							ry = r.getY() * r.getDrawScale().y;
							ox = r.getOriginalPos().x;
							oy = r.getOriginalPos().y;
							Array<Float> temp = new Array<Float>();
							temp.add(ox, oy, rx, ry);
							if (smokesCoord.size == rocks.size) {
								smokesCoord.removeIndex(0);
							}
							smokesCoord.add(temp);
							setSmoke(true);
							r.setCollide(false);

						}
					}
				}
			}


			//TODO: check rock collision with windows


			//detect window and player collision
			for (int i = 0; i < windows.size(); i++) {
				RegularPlatform w = windows.get(i);
				if (w.getStepCount() < 3) {
					if ((bd1 == w && bd2 == player)
							|| (bd1 == player && bd2 == w)) {
						if (player.isGrounded() && w.getOnWindow() == false) {
							windowsStepped.add(w);
							//System.out.println(w.getStepCount());
							w.setOnWindow(true);
							w.incrementStepCount();
						}
					}
				}
				else {
					windowsStepped.add(w);
					removeWindows.add(w);
				}
			}

			//check player and platform collision
			for (int i = 0; i < platforms.size(); i++) {
				Obstacle p = platforms.get(i);
				if ((bd1 == player && bd2 == p)
						|| (bd1 == p && bd2 == player)) {
					for (int j = 0; j < windows.size(); j++) {
						RegularPlatform w = windows.get(j);
						w.setOnWindow(false);
					}
				}
			}

			// Check for win condition
			if (((bd1 == player && (bd2 instanceof BoxObstacle && goalDoor.sensors.contains((BoxObstacle) bd2, true))) ||
					((bd1 instanceof BoxObstacle && goalDoor.sensors.contains((BoxObstacle) bd1, true) && bd2 == player)))) {
				numMoonCollected = 0;
				for (int i =0; i<moonShardsList.size(); i++) {
					if (moonShardsList.get(i).isTaken()) {
						numMoonCollected++;
					}
				}
				if (numMoonCollected == moonShardsList.size()) {
					lastpt = null;
					SoundController.pauseMusic();
					Sound s = SoundController.goalSound();
					SoundController.playSound(s, 0.1f);
					level.complete = true;
					if (level.nextLevel != null) {
					    level.nextLevel.available = true;
                    }
                    int stars = 0;
					if(serenity >= THREE_STAR) {
						stars = 3;
					}
					else if(serenity >= TWO_STAR) {
						stars = 2;
					}
					else if(serenity >= ONE_STAR) {
						stars = 1;
					}
					level.numStarsCollected = Math.max(stars, level.numStarsCollected);
					editSaveJson(true);
					setComplete(true);
				}
			}

			//check for signpost collision
			for (int i = 0; i < signpostsList.size(); i++) {
				SignPost post = signpostsList.get(i);
				// I did it this way because the other way was causing it to disappear
                // too soon
				if (player.getX() >= post.getX()-1 && player.getX() <= post.getX() + 1) {
				    if (player.getY() >= post.getY()-1 && player.getY() <= post.getY() + 1) {
				        post.setMessageVisible(true);
				        post.setHasRead(true);
                    } else {
				        post.setMessageVisible(false);
                    }
                } else {
				    post.setMessageVisible(false);
                }
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

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	private static boolean canPlaySound = true;
	private static Timer canPlaySoundTimer = new Timer();

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

				if(((bd1 instanceof Platform && bd2 instanceof PlayerModel)
						|| (bd1 instanceof PlayerModel && bd2 instanceof Platform))
						&& !(bd1 instanceof  CloudPlatform) && !(bd2 instanceof  CloudPlatform)) {
					final PlayerModel dm;
					final Platform platform;
					if(bd1 instanceof PlayerModel){
						dm = (PlayerModel) bd1;
//						if (bd2 instanceof RegularPlatform) {
//							platform = (RegularPlatform) bd2;
//						} else {
//							platform = (PolygonPlatform) bd2;
//						}
						platform = (Platform) bd2;
					}
					else {
						dm = (PlayerModel) bd2;
//						if (bd1 instanceof RegularPlatform) {
//							platform = (RegularPlatform) bd1;
//						} else {
//							platform = (PolygonPlatform) bd1;
//						}
						platform = (Platform) bd1;
					}

					if(!(platform instanceof CloudPlatform) && platform.isValidCollision(dm) && !dm.onMovingPlatform){
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								if (platform.getVelocity().x != 0) {
									float dx = platform.getVelocity().x;
									float x = dm.getPosition().x + dx;
									dm.setPosition(x, dm.getPosition().y);
									dm.setOnMovingPlatform(true);
								}
								if (platform.getVelocity().y != 0) {
									float dy = platform.getVelocity().y;
									if (dy > 0) dy = 0;
									float y = dm.getPosition().y + dy;
									dm.setPosition(dm.getPosition().x, y);
									dm.setOnMovingPlatform(true);

								}

							}
						});
					}

				}

				if(bd1 instanceof Crocodile && bd2 instanceof PlayerModel || bd2 instanceof  Crocodile
						&& bd1 instanceof  PlayerModel) {
					isMonsterCollision = true;
					//TODO: add cooldown
					if(canPlaySound){
						Sound s = SoundController.hurtSound();
						SoundController.playSound(s, 0.3f);
						canPlaySound = false;
						canPlaySoundTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								canPlaySound = true;
							}
						}, 400);
					}
					super.serenity -= MAX_SERENITY/2000;
					if(super.serenity < 0) {
						super.serenity = 0;
					}
				}
				if(bd1 instanceof FlyingMonster && bd2 instanceof PlayerModel || bd2 instanceof  FlyingMonster
						&& bd1 instanceof  PlayerModel) {
					isMonsterCollision = true;
					if(canPlaySound){
						Sound s = SoundController.hurtSound();
						SoundController.playSound(s, 0.3f);
						canPlaySound = false;
						canPlaySoundTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								canPlaySound = true;
							}
						}, 400);
					}
					super.serenity -= MAX_SERENITY/2000;
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
					if(sp.isValidCollision(dm) && !dm.isTransparent()){
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
						Sound s = SoundController.hurtSound();
						SoundController.playSound(s, 0.3f);
						super.serenity -= MAX_SERENITY/20;
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

	public String createJsonString(String name, Boolean complete, Boolean unlock, int checkpts, int serenityLeft, String path, String abilQueue, int numStars) {
//		Map<String, String> objectToReturn = new HashMap<String, String>();
//		objectToReturn.put("name", lvl.name);
//		objectToReturn.put("completed", Boolean.toString(false));
//		objectToReturn.put("unlocked", Boolean.toString(true));
//		objectToReturn.put("checkpoints-passed", Integer.toString(checkpointsPassed));
//		objectToReturn.put("serenity-left", Integer.toString(super.serenity));
//		objectToReturn.put("path", lvl.path);
//		objectToReturn.put("ability-queue", abilityController.getAbilityQueue().abilities.toString());

		String text = "{ \n \"name\": \"" + name + "\",\n \"completed\": " + complete + ",\n " + "\"unlocked\": " + unlock + ",\n \"checkpoints\": " + checkpts + ",\n \"serenity-left\": " + serenityLeft + ",\n \"path\": \"" + path + "\",\n \"ability-queue\": " + abilQueue + ",\n \"num-stars\": " + numStars + " \n},";

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


		String text = "{\"levels\": [";
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
				newLevel.numStarsCollected = entry.getInt("num-stars");

				if (newLevel.num == level.num+1) {
				    newLevel.available = level.nextLevel.available;
                }

				String lvlText;

				if (level.name.equals(newLevel.name)) {
					lvlText = createJsonString(level.name, level.complete, level.available, level.checkpointsPassed, (int) super.serenity, level.path, abilityController.getAbilityQueue().abilities.toString(), level.numStarsCollected);
				}
				else {
					lvlText = createJsonString(newLevel.name, newLevel.complete, newLevel.available, newLevel.checkpointsPassed, newLevel.currentSerenity, newLevel.path, null, newLevel.numStarsCollected);
				}
				text = text + "\n" + lvlText;
//				levels.add(newLevel);
				num++;
			}
		}
		text = text + "\n]\n}";
		FileHandle fileToPrint = Gdx.files.local("jsons/saved_game.json");
		fileToPrint.writeString(text, false);
	}

	public void newSaveJson() {
		String file = "jsons/saved_game.json";

		JsonReader jsonReader = new JsonReader();
		JsonValue savedGameFile = jsonReader.parse(Gdx.files.internal(file));
//		Array<Level> levels = new Array<Level>();


		String text = "{\"levels\": [";
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
				newLevel.numStarsCollected = entry.getInt("num-stars");

				if (newLevel.num == level.num+1) {
					newLevel.available = level.nextLevel.available;
				}

				String lvlText;

				if (level.name.equals(newLevel.name)) {
					lvlText = createJsonString(level.name, level.complete, level.available, level.checkpointsPassed, (int) super.serenity, level.path, abilityController.getAbilityQueue().abilities.toString(), level.numStarsCollected);
				}
				else {
					lvlText = createJsonString(newLevel.name, newLevel.complete, newLevel.available, newLevel.checkpointsPassed, newLevel.currentSerenity, newLevel.path, null, newLevel.numStarsCollected);
				}
				text = text + "\n" + lvlText;
//				levels.add(newLevel);
				num++;
			}
		}
		text = text + "\n]\n}";
		FileHandle fileToPrint = Gdx.files.local("jsons/saved_game.json");
		fileToPrint.writeString(text, false);
	}
}