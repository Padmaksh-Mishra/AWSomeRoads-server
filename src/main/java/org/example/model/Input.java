package org.example.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the input actions sent by a player.
 * <p>
 * Attributes:
 * - playerId: the unique identifier of the player.
 * - actions: a list of actions the player has taken.
 * <p>
 * Responsibilities:
 * - Encapsulate the player actions to be sent to the server.
 */

public record Input(int playerId, List<PlayerAction> actions) {
    @JsonCreator
    public Input(
            @JsonProperty("playerId") int playerId,
            @JsonProperty("actions") List<PlayerAction> actions
    ) {
        this.playerId = playerId;
        this.actions = actions;
    }

    /**
     * Gets the player ID.
     *
     * @return The player ID.
     */
    @Override
    public int playerId() {
        return playerId;
    }

    /**
     * Gets the list of player actions.
     *
     * @return The list of player actions.
     */
    @Override
    public List<PlayerAction> actions() {
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