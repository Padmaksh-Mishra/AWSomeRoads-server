package org.example.engine;

import org.example.config.Config;
import org.example.model.Game;
import org.example.model.Hazard;
import org.example.model.Boost;
import org.example.model.Player;
import org.example.model.Input;
import org.example.utils.Logger;

import java.util.List;
import java.util.Random;

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
    private final Random random = new Random();

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
        // Generate a new hazard based on HAZARD_PROBABILITY
        if (random.nextFloat() < Config.HAZARD_PROBABILITY) {
            Hazard newHazard = Hazard.generateHazard(game.getHazards().size());
            if (newHazard != null) {
                game.addHazard(newHazard);
                logger.logInfo("Generated new Hazard " + newHazard.getId());
            }
        }

        // Generate a new boost based on BOOST_PROBABILITY
        if (random.nextFloat() < Config.BOOST_PROBABILITY) {
            Boost newBoost = Boost.generateBoost(game.getBoosts().size());
            if (newBoost != null) {
                game.addBoost(newBoost);
                logger.logInfo("Generated new Boost " + newBoost.getId());
            }
        }

        // Update positions of all hazards
        for (Hazard hazard : game.getHazards()) {
            hazard.updatePosition();
            // Format y-coordinate to two decimal places
            String formattedY = String.format("%.2f", hazard.getY());
            logger.logInfo("Hazard " + hazard.getId() + " moved to y=" + formattedY);
        }

        // Update positions of all boosts
        for (Boost boost : game.getBoosts()) {
            boost.updatePosition();
            String formattedY = String.format("%.2f", boost.getY());
            logger.logInfo("Boost " + boost.getId() + " moved to y=" + formattedY);
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