package edu.cornell.gdiac.amaris;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.amaris.platform.FountainModel;

import java.util.LinkedList;
import java.util.Queue;

/**The queue that stores the abilities the player collected until used */

public class AbilityQueue {
    /** The texture for the dash ability. */
    protected TextureRegion dashAbilityTexture;
    /** The texture for the flight ability. */
    protected TextureRegion flightAbilityTexture;
    /** The texture for the transparency ability. */
    protected TextureRegion transparentAbilityTexture;

    // CONSTANTS
    protected int CARD_WIDTH = 50;
    protected int CARD_HEIGHT = 50;

    protected int QUEUE_START_X = 277;
    protected int QUEUE_START_Y = 20;

    /** The queue containing the abilities gained from fountains */
    public Queue<FountainModel> abilities = new LinkedList<FountainModel>();

    public AbilityQueue(Queue a) {
        a = abilities;

    }

    /**
     * Adds the specific ability to the queue
     * @Return the new queue
     * @param fountain the ability being added to the queue */
//    public Queue add(FountainModel.FountainType ability) {
//        if (ability == FountainModel.FountainType.RESTORE) {
//            return abilities;
//        }
//        abilities.add(ability);
//        return abilities;
//    }
    public Queue add(FountainModel fountain) {
        if (fountain.getFountainType() == FountainModel.FountainType.RESTORE) {
            return abilities;
        }
        abilities.add(fountain);
        return abilities;
    }

    /**
     * Sets the dash card texture
     * @param dashTexture the texture for the dash card */
    public void setDashTexture(TextureRegion dashTexture) {
        dashAbilityTexture = dashTexture;
    }

    /**
     * Sets the flight card texture
     * @param flightTexture the texture for the flight card */
    public void setFlightTexture(TextureRegion flightTexture) {
        flightAbilityTexture = flightTexture;
    }

    /**
     * Sets the transparent card texture
     * @param transparentTexture the texture for the transparent card */
    public void setTransparentTexture(TextureRegion transparentTexture) {
        transparentAbilityTexture = transparentTexture;
    }

    /**
     * Removes the top element of the queue
     * @Return the removed element */
    public FountainModel remove() {
        return abilities.poll();

    }

    public void clear() {abilities.clear();}

    /**
     * Draws the ability queue on the game screen
     */
    public void draw(GameCanvas canvas) {
        Color color = Color.WHITE;
        int x = QUEUE_START_X;
        int y = QUEUE_START_Y;
        for (FountainModel fountain : abilities) {
            if (fountain.getFountainType() == FountainModel.FountainType.DASH) {
                canvas.draw(dashAbilityTexture, color, x, y, CARD_WIDTH, CARD_HEIGHT, true);
                x -= (CARD_WIDTH);
            } else if (fountain.getFountainType() == FountainModel.FountainType.TRANSPARENCY) {
                canvas.draw(transparentAbilityTexture, color, x, y, CARD_WIDTH, CARD_HEIGHT, true);
                x -= (CARD_WIDTH);
            } else if (fountain.getFountainType() == FountainModel.FountainType.FLIGHT){
                canvas.draw(flightAbilityTexture, color, x, y, CARD_WIDTH, CARD_HEIGHT, true);
                x -= (CARD_WIDTH);
            }
        }
    }
}

