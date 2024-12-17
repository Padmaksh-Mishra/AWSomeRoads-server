package org.example.model;

import org.example.config.Config;

/**
 * This class represents a player in the game.
 *
 * It contains the following attributes:
 * - id: a unique identifier for the player (number of players till now) (0-MAX_PLAYERS)
 * - x: the x-coordinate of the player
 * - y: the y-coordinate of the player
 * - health: the health of the player (max HEALTH_MAX)
 * - disqualified: a boolean indicating whether the player has been disqualified
 *
 * Responsibilities of this class include:
 * - Receiving the initial position of the player for the GameServer
 * - Updating the player's position as directed by the GameEngine
 * - Updating the player's health as directed by the GameEngine
 * - Providing the current player state to the GameEngine
 */

public class Player {
    private final int id;
    private int x;
    private float y; // Changed y from int to float
    private int health;
    private boolean disqualified;

    /**
     * Constructor to initialize a Player object.
     *
     * @param id     The unique identifier of the player.
     * @param x      The initial x-coordinate of the player.
     * @param y      The initial y-coordinate of the player.
     * @param health The initial health of the player.
     */
    public Player(int id, int x, float y, int health) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
        this.disqualified = false;
    }

    /**
     * Gets the player ID.
     *
     * @return The player ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the x-coordinate of the player.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the player.
     *
     * @param x The new x-coordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the player.
     *
     * @return The y-coordinate.
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the player.
     *
     * @param y The new y-coordinate.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Gets the health of the player.
     *
     * @return The player's health.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the health of the player.
     *
     * @param health The new health value.
     */
    public void setHealth(int health) {
        this.health = Math.max(health, 0); // Ensure health doesn't go below 0
    }

    /**
     * Checks if the player is disqualified.
     *
     * @return True if disqualified, false otherwise.
     */
    public boolean isDisqualified() {
        return disqualified;
    }

    /**
     * Disqualifies the player.
     */
    public void disqualify() {
        this.disqualified = true;
    }

    /**
     * Resets the player's health to maximum.
     */
    public void resetHealth() {
        this.health = Config.HEALTH_MAX;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", health=" + health +
                ", disqualified=" + disqualified +
                '}';
    }

    // Equals and hashCode methods can be added if needed
}