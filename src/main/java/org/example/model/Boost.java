package org.example.model;

import org.example.config.Config;
import java.util.Random;

/**
 * This class represents a boost in the game.
 *
 * Attributes:
 * - id: a unique identifier for the boost (generated randomly)
 * - x: the x-coordinate of the boost
 * - y: the y-coordinate of the boost
 *
 * Responsibilities:
 * - Randomly generating boosts on the game board
 * - Destroying the boosts when they reach the bottom of the game board or collide with a player
 * - Applying the boost to a player when they collide with it
 */

public class Boost {
    private final int id;
    private int x;
    private int y;
    private boolean isActive;

    private static final Random RANDOM = new Random();

    /**
     * Constructor to initialize a Boost object.
     *
     * @param id Unique identifier for the boost.
     * @param x  X-coordinate of the boost.
     * @param y  Y-coordinate of the boost.
     */
    public Boost(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isActive = true;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Generates a new Boost with random coordinates.
     *
     * @return A new Boost instance or null if the maximum number of boosts exists.
     */
    public static Boost generateBoost(int currentBoostCount) {
        if (currentBoostCount >= Config.MAX_BOOSTS) {
            return null;
        }
        int id = RANDOM.nextInt(Integer.MAX_VALUE);
        int x = RANDOM.nextInt(Config.X_MAX + 1);
        int y = RANDOM.nextInt(Config.Y_MAX + 1);
        return new Boost(id, x, y);
    }

    /**
     * Updates the position of the boost.
     * Should be called by the Simulator to move the boost down the game board.
     */
    public void updatePosition() {
        if (isActive) {
            this.y += 1;
            if (this.y >= Config.Y_MAX) {
                destroy();
            }
        }
    }

    /**
     * Destroys the boost by deactivating it.
     */
    public void destroy() {
        this.isActive = false;
        currentBoostCount--;
        // Additional logic to remove the boost from the game board can be added here
    }

    /**
     * Applies the boost effect to a player upon collision.
     *
     * @param player The player to apply the boost to.
     */
    public void applyToPlayer(Player player) {
        if (isActive) {
            player.setY(Math.max(player.getY() - Config.BOOST_POWER, 0));
            destroy();
        }
    }

    @Override
    public String toString() {
        return "Boost{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", isActive=" + isActive +
                '}';
    }

    // Equals and hashCode methods can be added if needed
}