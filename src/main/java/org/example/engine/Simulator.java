package org.example.engine;

import org.example.config.Config;
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
    private Logger logger = new Logger();

    // Time accumulator to track elapsed time for generation
    private float hazardTimeAccumulator = 0.0f;
    private float boostTimeAccumulator = 0.0f;

    /**
     * Constructor to initialize the Simulator with a GameEngine instance.
     *
     * @param gameEngine The GameEngine to handle game state updates.
     */
    public Simulator(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Constructor to initialize the Simulator with a GameEngine instance and a Logger.
     *
     * @param gameEngine The GameEngine to handle game state updates.
     * @param logger     The Logger for logging information.
     */
    public Simulator(GameEngine gameEngine, Logger logger) {
        this.gameEngine = gameEngine;
        this.logger = logger;
    }

    /**
     * Simulates the next step of the game.
     *
     * @param game   The current game state.
     * @param inputs A list of inputs from all players.
     */
    public void simulateStep(Game game, List<Input> inputs) {
        float stepDuration = Config.SIMULATION_STEP_SIZE; // Assuming step size is in seconds (e.g., 0.1 for 100ms)

        // Update time accumulators
        hazardTimeAccumulator += stepDuration;
        boostTimeAccumulator += stepDuration;

        // Generate hazards based on HAZARD_RATE_PER_SECOND
        float hazardInterval = 1.0f / Config.HAZARD_RATE_PER_SECOND;
        while (hazardTimeAccumulator >= hazardInterval) {
            hazardTimeAccumulator -= hazardInterval;
            generateHazard(game);
        }

        // Generate boosts based on BOOST_RATE_PER_SECOND
        float boostInterval = 1.0f / Config.BOOST_RATE_PER_SECOND;
        while (boostTimeAccumulator >= boostInterval) {
            boostTimeAccumulator -= boostInterval;
            generateBoost(game);
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

    /**
     * Generates a new Hazard and adds it to the game if possible.
     *
     * @param game The current game state.
     */
    private void generateHazard(Game game) {
        Hazard newHazard = Hazard.generateHazard(game.getHazards().size());
        if (newHazard != null) {
            game.addHazard(newHazard);
            logger.logInfo("Generated new Hazard " + newHazard.getId() + " at (x=" + newHazard.getX() + ", y=" + newHazard.getY() + ")");
        } else {
            logger.logInfo("Maximum number of Hazards reached. Cannot generate more.");
        }
    }

    /**
     * Generates a new Boost and adds it to the game if possible.
     *
     * @param game The current game state.
     */
    private void generateBoost(Game game) {
        Boost newBoost = Boost.generateBoost(game.getBoosts().size());
        if (newBoost != null) {
            game.addBoost(newBoost);
            logger.logInfo("Generated new Boost " + newBoost.getId() + " at (x=" + newBoost.getX() + ", y=" + newBoost.getY() + ")");
        } else {
            logger.logInfo("Maximum number of Boosts reached. Cannot generate more.");
        }
    }
}
