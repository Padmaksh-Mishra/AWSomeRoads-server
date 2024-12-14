package org.example.model;

import java.util.List;

/**
 * This class represents the input actions sent by a player.
 *
 * Attributes:
 * - playerId: the unique identifier of the player.
 * - actions: a list of actions the player has taken.
 *
 * Responsibilities:
 * - Encapsulate the player actions to be sent to the server.
 */
public class Input {
    private final int playerId;
    private final List<PlayerAction> actions;

    /**
     * Constructor to initialize an Input object.
     *
     * @param playerId The unique identifier of the player.
     * @param actions  The list of actions taken by the player.
     */
    public Input(int playerId, List<PlayerAction> actions) {
        this.playerId = playerId;
        this.actions = actions;
    }

    /**
     * Gets the player ID.
     *
     * @return The player ID.
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the list of player actions.
     *
     * @return The list of player actions.
     */
    public List<PlayerAction> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "Input{" +
                "playerId=" + playerId +
                ", actions=" + actions +
                '}';
    }
}