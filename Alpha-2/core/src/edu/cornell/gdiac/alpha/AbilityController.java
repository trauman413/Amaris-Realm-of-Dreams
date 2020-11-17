package edu.cornell.gdiac.alpha;

import edu.cornell.gdiac.alpha.objects.FountainModel;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
        if(currentTimer != null){
            getCountdownTask().run();
            currentTimer = null;
        }
        if(timeLeftTimer != null){
            timeLeft = 0f;
            timeLeftTimer.cancel();
            timeLeftTimer = null;
        }
        currentTimer = new Timer();
        currentTimer.schedule(getCountdownTask(), milliseconds);
        timeLeftTimer = new Timer();
        timeLeft = (float) (milliseconds / 1000);
        timeLeftTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeft = Math.max(timeLeft - 0.1f, 0f);
            }
        }, 100, 100);
    }

    /** Returns the timertask to be called after the ability ends */
    private TimerTask getCountdownTask(){
        return new TimerTask() {
            @Override
            public void run() {
                isUsingAbility = false;
                currentTimer.cancel();
                currentTimer = null;
                timeLeftTimer.cancel();
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

    // END --------------------------------------------------------------------------------------------------

}
