package edu.cornell.gdiac.alpha;


import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.alpha.platform.FountainModel;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.alpha.platform.PlayerModel;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class AbilityController {

    private static AbilityController instance = null;

    public static AbilityController getInstance(){
        if (instance == null){
            instance = new AbilityController();
        }
        return instance;
    }

    // ABILITY QUEUE METHODS AND VARIABLES ------------------------------------------------------------------

    private AbilityQueue abilityQueue = new AbilityQueue(new LinkedList<FountainModel>());

    /** Returns the ability queue (for use in saving at checkpoints)*/
    public AbilityQueue getAbilityQueue() { return abilityQueue; }

    /** Returns the next ability in the queue and removes it from the queue */
    private FountainModel getNextAbility(){
        return abilityQueue.remove();
    }

    /** Returns the next ability in the queue without removing it from the queue */
    public FountainModel.FountainType peekNextAbility(){
        return abilityQueue.abilities.peek().getFountainType();
    }

    /** Adds [ability] to the queue and returns the ability queue */
    public AbilityQueue addAbility(FountainModel fountain){
        abilityQueue.add(fountain);
        return abilityQueue;
    }

    public void reset() {
        if (timeLeftTimer != null) timeLeftTimer.cancel();
        if (currentTimer != null) currentTimer.cancel();
        timeLeftTimer = null;
        abilityQueue.clear();
        lastAbilityUsed = null;
        isUsingAbility = false;
        currentTimer = null;
        timeLeft = 0;
    }

    /** Sets [dashTexture] as the dash texture for the queue */
    public void setDashTexture(TextureRegion dashTexture) {
        abilityQueue.setDashTexture(dashTexture);
    }

    /** Sets [flightTexture] as the flight texture for the queue */
    public void setFlightTexture(TextureRegion flightTexture) {
        abilityQueue.setFlightTexture(flightTexture);
    }

    /** Sets [transparentTexture] as the transparent texture for the queue */
    public void setTransparentTexture(TextureRegion transparentTexture) {
        abilityQueue.setTransparentTexture(transparentTexture);
    }

    /** Draws the ability queue with [canvas] */
    public void drawQueue(GameCanvas canvas) {
        abilityQueue.draw(canvas);
    }

    // END --------------------------------------------------------------------------------------------------

    // ABILITY TIMING METHODS AND VARIABLES -----------------------------------------------------------------

    public static boolean isUsingAbility = false;
    private static FountainModel lastAbilityUsed = null;
    private static Timer currentTimer = null;
    private static Timer timeLeftTimer = null;
    private static float timeLeft = 0f;
    public boolean startedAbility = false;

    /** Uses an ability by updating the ability queue and setting the ability as active for [activeForMilliseconds] */
    public void useAbility(long activeForMilliseconds){
        if(abilityQueue.abilities.size() > 0){
            lastAbilityUsed = getNextAbility();
            startCountdown(activeForMilliseconds);
            isUsingAbility = true;
        }
    }

    /** Starts a new timer for another ability, executing and overriding any other timers previously made
     * by use of previous abilities. */
    private void startCountdown(long milliseconds){
        startedAbility = true;
        if(currentTimer != null){
            getCountdownTask().run();
            currentTimer = null;
        }
        if(timeLeftTimer != null){
            timeLeft = 0f;
            timeLeftTimer.cancel();
            timeLeftTimer = null;
        }
        timeLeft = (float) (milliseconds / 1000);
        startTimers();
    }

    /** Returns the timertask to be called after the ability ends */
    private TimerTask getCountdownTask(){
        return new TimerTask() {
            @Override
            public void run() {
                isUsingAbility = false;
                cancelTimers();
                currentTimer = null;
                timeLeftTimer = null;
                timeLeft = 0f;
            }
        };
    }

    /** Returns whether an ability is currently active */
    public boolean isUsingAbility(){
        return isUsingAbility;
    }

    /** Returns the fountain corresponding to the ability the player last activated */
    public FountainModel getLastAbilityUsed(){
        return lastAbilityUsed;
    }

    /** Returns whether the ability [type] is currently active */
    public boolean isAbilityActive(FountainModel.FountainType type){
        return isUsingAbility && getLastAbilityUsed().getFountainType() == type;
    }

    /** Returns a float representing the number of seconds left for the current ability to the nearest
     * tenth of a second. */
    public float getTimeLeftForAbility(){
        return timeLeft;
    }

    public void setTimer(int val) {
        timeLeft = val;
    }

    /** Cancels the timers (if running) needed for timing abilites. */
    public boolean cancelTimers() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        if (timeLeftTimer != null) {
            timeLeftTimer.cancel();
            return true;
        }
        return false;
    }

    /** Starts the timers needed for timing abilites. */
    public void startTimers() {
        currentTimer = new Timer();
        currentTimer.schedule(getCountdownTask(), (long) timeLeft *  1000);
        timeLeftTimer = new Timer();
        timeLeftTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeft = Math.max(timeLeft - 0.1f, 0f);
            }
        }, 100, 100);
    }

    // END --------------------------------------------------------------------------------------------------

    // DASH PHYSICS HANDLING HERE ---------------------------------------------------------------------------

    public static Timer dashTimer = new Timer();
    public static Timer dashTimerTimer = new Timer();
    public boolean isApplyingDash = false;

    public void doDash(final PlayerModel playerModel, final long milliseconds){
        final PlayerModel player = playerModel;
        isApplyingDash = true;
        player.giveFreeMovement(milliseconds);
        dashTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                player.applyDashForce();
            }
        },0, 30);
        dashTimerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dashTimer.cancel();
                dashTimerTimer.cancel();
                dashTimer = new Timer();
                dashTimerTimer = new Timer();
                isApplyingDash = false;
            }
        }, milliseconds);
    }

    public void doSkip(PlayerModel player){
        Vector2 skip = new Vector2(0, 35 / 2f);
        player.getBody().applyLinearImpulse(skip, player.getPosition(), true);
        player.setGrounded(false);
    }

    public void resetDash(){
        dashTimer.cancel();
        dashTimerTimer.cancel();
        dashTimer = new Timer();
        dashTimerTimer = new Timer();
        isApplyingDash = false;
    }

    public boolean isApplyingDash(){
        return isApplyingDash;
    }

}

