package org.example.model;

import org.example.config.Config;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a boost in the game.
 *
 * Attributes:
 * - id: a unique identifier for the boost (incremented)
 * - x: the x-coordinate of the boost
 * - y: the y-coordinate of the boost
 *
 * Responsibilities:
 * - Generating boosts on the game board with unique IDs
 * - Destroying the boosts when they reach the bottom of the game board or collide with a player
 * - Applying the boost to a player when they collide with it
 */
public class Boost {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0); // Atomic counter for unique IDs
    private final int id;
    private int x;
    private float y; // Changed y from int to float
    private boolean isActive;

    private static final Random RANDOM = new Random();

    /**
     * Constructor to initialize a Boost object.
     *
     * @param x X-coordinate of the boost.
     * @param y Y-coordinate of the boost.
     */
    public Boost(int x, int y) {
        this.id = ID_COUNTER.getAndIncrement(); // Assign unique incremental ID
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

    public float getY() {
        return y;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setX(int x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Generates a new Boost with random coordinates.
     *
     * @param currentBoostCount The current number of active boosts.
     * @return A new Boost instance or null if the maximum number of boosts exists.
     */
    public static Boost generateBoost(int currentBoostCount) {
        if (currentBoostCount >= Config.MAX_BOOSTS) {
            return null;
        }
        int x = RANDOM.nextInt(Config.X_MAX + 1);
        int y = RANDOM.nextInt(Config.Y_MAX + 1);
        return new Boost(x, y);
    }

    /**
     * Updates the position of the boost.
     * Should be called by the Simulator to move the boost down the game board.
     */
    public void updatePosition() {
        if (isActive) {
            this.y += Config.SIMULATION_STEP_SIZE;
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

    /**
     * Resets the Boost ID counter.
     * Intended for testing purposes only.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(0);
    }
}
