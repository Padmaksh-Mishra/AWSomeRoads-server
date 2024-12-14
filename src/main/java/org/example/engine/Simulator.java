package org.example.engine;

import org.example.model.Game;
import org.example.model.Hazard;
import org.example.model.Boost;
import org.example.model.Player;
import org.example.model.Input;
import org.example.utils.Logger;

import java.util.List;

/**
 * This class will simulate the next 100ms of the game board.
 *
 * Responsibilities include:
 * - Updating the position of all hazards and boosts (increasing their y-coordinate by 1)
 * - Removing inactive hazards and boosts
 * - Removing disqualified players
 * - Checking for collisions between players and hazards or boosts
 */
public class Simulator {
    private final GameEngine gameEngine;
    private final Logger logger = new Logger();

    /**
     * Constructor to initialize the Simulator with a GameEngine instance.
     *
     * @param gameEngine The GameEngine to handle game state updates.
     */
    public Simulator(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Simulates the next step of the game.
     *
     * @param game   The current game state.
     * @param inputs A list of inputs from all players.
     */
    public void simulateStep(Game game, List<Input> inputs) {
        // Update positions of all hazards
        for (Hazard hazard : game.getHazards()) {
            hazard.updatePosition();
            logger.logInfo("Hazard " + hazard.getId() + " moved to y=" + hazard.getY());
        }

        // Update positions of all boosts
        for (Boost boost : game.getBoosts()) {
            boost.updatePosition();
            logger.logInfo("Boost " + boost.getId() + " moved to y=" + boost.getY());
        }

        // Remove inactive hazards
        boolean removedHazards = game.getHazards().removeIf(hazard -> !hazard.isActive());
        if (removedHazards) {
            logger.logInfo("Inactive hazards removed from the game.");
        }

        // Remove inactive boosts
        boolean removedBoosts = game.getBoosts().removeIf(boost -> !boost.isActive());
        if (removedBoosts) {
            logger.logInfo("Inactive boosts removed from the game.");
        }

        // Remove disqualified players
        boolean removedPlayers = game.getPlayers().removeIf(Player::isDisqualified);
        if (removedPlayers) {
            logger.logInfo("Disqualified players removed from the game.");
        }

        // Run the GameEngine to update the game state based on player inputs
        gameEngine.updateGameState(game, inputs);
    }
}