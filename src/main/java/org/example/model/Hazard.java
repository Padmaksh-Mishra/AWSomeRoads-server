package org.example.model;

import org.example.config.Config;
import org.example.model.Player;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a hazard in the game.
 *
 * Attributes:
 * - id: a unique identifier for the hazard (incremented)
 * - x: the x-coordinate of the hazard
 * - y: the y-coordinate of the hazard
 * - damage: the amount of damage the hazard causes
 * - isActive: indicates whether the hazard is active in the game
 *
 * Responsibilities:
 * - Generating hazards on the game board with unique IDs
 * - Destroying the hazards when they reach the bottom of the game board or collide with a player
 * - Applying damage to a player when they collide with the hazard
 */
public class Hazard {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0); // Atomic counter for unique IDs
    private final int id;
    private int x;
    private float y; // Changed y from int to float
    private int damage;
    private boolean isActive;

    private static final Random RANDOM = new Random();

    /**
     * Constructor to initialize a Hazard object.
     *
     * @param x      X-coordinate of the hazard.
     * @param y      Y-coordinate of the hazard.
     * @param damage Amount of damage the hazard causes.
     */
    public Hazard(int x, float y, int damage) {
        this.id = ID_COUNTER.getAndIncrement(); // Assign unique incremental ID
        this.x = x;
        this.y = y;
        this.damage = damage;
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

    public int getDamage() {
        return damage;
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

    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Generates a new Hazard with random coordinates.
     *
     * @param currentHazardCount The current number of hazards on the game board.
     * @return A new Hazard instance or null if the maximum number of hazards exists.
     */
    public static Hazard generateHazard(int currentHazardCount) {
        if (currentHazardCount >= Config.MAX_HAZARDS) {
            return null;
        }
        int x = RANDOM.nextInt(Config.X_MAX + 1);
        float y = 0.0f; // Spawn at the top of the game board
        int damage = Config.HAZARD_DAMAGE;
        return new Hazard(x, y, damage);
    }

    /**
     * Updates the position of the hazard.
     * Should be called by the Simulator to move the hazard down the game board.
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
     * Destroys the hazard by deactivating it.
     */
    public void destroy() {
        this.isActive = false;
    }

    /**
     * Applies damage to a player upon collision.
     *
     * @param player The player to apply damage to.
     */
    public void applyToPlayer(Player player) {
        if (isActive) {
            player.setHealth(player.getHealth() - this.damage);
            destroy();
        }
    }

    @Override
    public String toString() {
        return "Hazard{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", damage=" + damage +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Resets the Hazard ID counter.
     * Intended for testing purposes only.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(0);
    }

}
