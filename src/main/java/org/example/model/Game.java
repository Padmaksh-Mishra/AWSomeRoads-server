package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the game state.
 *
 * It contains the following attributes:
 * - players: a list of Player objects representing the players in the game
 * - hazards: a list of Hazard objects representing the hazards in the game
 * - boosts: a list of Boost objects representing the boosts in the game
 * - latestInputs: a list of Input objects representing the latest actions from all players
 *
 * Responsibilities of this class include:
 * - Managing the collections of players, hazards, and boosts
 * - Handling the addition and retrieval of players, hazards, boosts, and inputs
 * - Providing methods to query the game state for specific conditions
 */
public class Game {
    private final List<Player> players;
    private final List<Hazard> hazards;
    private final List<Boost> boosts;
    private final List<Input> latestInputs;

    /**
     * Constructor to initialize the Game object.
     * Initializes empty lists for players, hazards, boosts, and latest inputs.
     */
    public Game() {
        this.players = new ArrayList<>();
        this.hazards = new ArrayList<>();
        this.boosts = new ArrayList<>();
        this.latestInputs = new ArrayList<>();
    }

    /**
     * Gets the list of players in the game.
     *
     * @return A list of Player objects.
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Gets the list of hazards in the game.
     *
     * @return A list of Hazard objects.
     */
    public List<Hazard> getHazards() {
        return hazards;
    }

    /**
     * Gets the list of boosts in the game.
     *
     * @return A list of Boost objects.
     */
    public List<Boost> getBoosts() {
        return boosts;
    }

    /**
     * Gets the latest inputs from all players.
     *
     * @return A list of Input objects.
     */
    public List<Input> getLatestInputs() {
        return latestInputs;
    }

    /**
     * Adds a new player to the game.
     *
     * @param player The Player object to add.
     */
    public void addPlayer(Player player) {
        if (players.size() >= Config.MAX_PLAYERS) {
            throw new IllegalStateException("Maximum number of players reached.");
        }
        players.add(player);
    }

    /**
     * Adds a new hazard to the game.
     *
     * @param hazard The Hazard object to add.
     */
    public void addHazard(Hazard hazard) {
        if (hazards.size() >= Config.MAX_HAZARDS) {
            throw new IllegalStateException("Maximum number of hazards reached.");
        }
        hazards.add(hazard);
    }

    /**
     * Adds a new boost to the game.
     *
     * @param boost The Boost object to add.
     */
    public void addBoost(Boost boost) {
        if (boosts.size() >= Config.MAX_BOOSTS) {
            throw new IllegalStateException("Maximum number of boosts reached.");
        }
        boosts.add(boost);
    }

    /**
     * Adds a list of inputs to the latestInputs list.
     *
     * @param inputs A list of Input objects to add.
     */
    public void addInputs(List<Input> inputs) {
        latestInputs.addAll(inputs);
    }

    /**
     * Retrieves a player by their unique identifier.
     *
     * @param playerId The unique identifier of the player.
     * @return The Player object if found, otherwise null.
     */
    public Player getPlayerById(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }

    /**
     * Retrieves a player at a specific position.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The Player object at the specified position if found, otherwise null.
     */
    public Player getPlayerAtPosition(int x, int y) {
        for (Player player : players) {
            if (player.getX() == x && player.getY() == y && !player.isDisqualified()) {
                return player;
            }
        }
        return null;
    }

    /**
     * Clears the latestInputs list.
     * Should be called after the game engine has processed the inputs.
     */
    public void clearLatestInputs() {
        latestInputs.clear();
    }

    /**
     * Removes a player from the game.
     *
     * @param player The Player object to remove.
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Removes a hazard from the game.
     *
     * @param hazard The Hazard object to remove.
     */
    public void removeHazard(Hazard hazard) {
        hazards.remove(hazard);
    }

    /**
     * Removes a boost from the game.
     *
     * @param boost The Boost object to remove.
     */
    public void removeBoost(Boost boost) {
        boosts.remove(boost);
    }

    @Override
    public String toString() {
        return "Game{" +
                "players=" + players +
                ", hazards=" + hazards +
                ", boosts=" + boosts +
                '}';
    }
}