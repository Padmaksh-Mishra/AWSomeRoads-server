package org.example.engine;

import org.example.config.Config;
import org.example.model.Game;
import org.example.model.Hazard;
import org.example.model.Boost;
import org.example.model.Player;
import org.example.model.PlayerAction;
import org.example.model.Input;
import org.example.utils.Logger;

import java.util.List;

/**
 * This is the game engine class. This class will be used to update the game state.
 *
 * It performs the following functions:
 * - Updates the players' positions based on the inputs received from the clients
 * - Updates the players' health based on the hazards and boosts {
 *     - Checks for collisions between players and hazards (if collisions occur, player health is reduced by HAZARD_DAMAGE)
 *     - Checks for collisions between players and boosts (if collisions occur, player y-coordinate is decreased by BOOST_POWER)
 *   }
 * - If the health of a player reaches 0, the player's y-coordinate is decreased by FALL_PENALTY
 *   - If the new y-coordinate >= Y_MAX, disqualify the player
 *   - Else, reset the player's health to HEALTH_MAX
 * - If the y-coordinate of a player reaches Y_MAX, the player is disqualified and removed from the game
 * - Checks if a player successfully punches another player (if so, the health of player being punched is reduced by PUNCH_DAMAGE)
 * - Checks if a player successfully kicks another player (if so, the x-coordinate of player being kicked is changed to left or right based on the direction of the kick)
 */

public class GameEngine {

    private final Logger logger = new Logger();

    /**
     * Updates the game state based on the inputs received from the clients.
     *
     * @param game   The current game state.
     * @param inputs A list of inputs from all players.
     */
    public void updateGameState(Game game, List<Input> inputs) {
        updatePlayerActions(game, inputs);
        handleCollisions(game);
        handleDisqualifications(game);
        handleAttacks(game);
    }

    /**
     * Updates the players' positions based on their actions.
     *
     * @param game   The current game state.
     * @param inputs A list of inputs from all players.
     */
    private void updatePlayerActions(Game game, List<Input> inputs) {
        for (Input input : inputs) {
            Player player = game.getPlayerById(input.getPlayerId());
            if (player == null || player.isDisqualified()) {
                continue;
            }

            for (PlayerAction action : input.getActions()) {
                switch (action) {
                    case MOVE_LEFT:
                        player.setX(Math.max(player.getX() - 1, 0));
                        logger.logInfo("Player " + player.getId() + " moved left to x=" + player.getX());
                        break;
                    case MOVE_RIGHT:
                        player.setX(Math.min(player.getX() + 1, Config.X_MAX));
                        logger.logInfo("Player " + player.getId() + " moved right to x=" + player.getX());
                        break;
                    case STEP_DOWN:
                        player.setY(Math.min(player.getY() + 1, Config.Y_MAX));
                        logger.logInfo("Player " + player.getId() + " stepped down to y=" + player.getY());
                        break;
                    default:
                        // Handle other actions like PUNCH and KICK in separate methods
                        break;
                }
            }
        }
    }

    /**
     * Handles collisions between players and hazards or boosts.
     *
     * @param game The current game state.
     */
    private void handleCollisions(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.isDisqualified()) {
                continue;
            }

            // Check collision with hazards
            for (Hazard hazard : game.getHazards()) {
                if (hazard.isActive() && isColliding(player, hazard)) {
                    hazard.applyToPlayer(player);
                    logger.logWarning("Player " + player.getId() + " collided with Hazard " + hazard.getId() +
                            " and took " + hazard.getDamage() + " damage.");
                }
            }

            // Check collision with boosts
            for (Boost boost : game.getBoosts()) {
                if (boost.isActive() && isColliding(player, boost)) {
                    boost.applyToPlayer(player);
                    logger.logInfo("Player " + player.getId() + " collected Boost " + boost.getId() +
                            " and gained boost power.");
                }
            }
        }
    }

    /**
     * Handles disqualifications and penalties for players.
     *
     * @param game The current game state.
     */
    private void handleDisqualifications(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.isDisqualified()) {
                continue;
            }

            // Check if player's health has reached 0
            if (player.getHealth() <= 0) {
                player.setY(Math.max(player.getY() - Config.FALL_PENALTY, 0));
                logger.logInfo("Player " + player.getId() + " has 0 health and experienced a fall penalty. New y=" + player.getY());

                if (player.getY() >= Config.Y_MAX) {
                    player.disqualify();
                    logger.logInfo("Player " + player.getId() + " has been disqualified for reaching Y_MAX.");
                } else {
                    player.setHealth(Config.HEALTH_MAX);
                    logger.logInfo("Player " + player.getId() + " has been healed to full health.");
                }
            }

            // Check if player's y-coordinate has reached Y_MAX
            if (player.getY() >= Config.Y_MAX) {
                player.disqualify();
                logger.logInfo("Player " + player.getId() + " has been disqualified for reaching Y_MAX.");
            }
        }
    }

    /**
     * Handles attack actions like punches and kicks between players.
     *
     * @param game The current game state.
     */
    private void handleAttacks(Game game) {
        for (Input input : game.getLatestInputs()) {
            Player attacker = game.getPlayerById(input.getPlayerId());
            if (attacker == null || attacker.isDisqualified()) {
                continue;
            }

            for (PlayerAction action : input.getActions()) {
                switch (action) {
                    case PUNCH_LEFT:
                        Player targetLeft = game.getPlayerAtPosition(attacker.getX() - 1, attacker.getY());
                        if (targetLeft != null && !targetLeft.isDisqualified()) {
                            targetLeft.setHealth(targetLeft.getHealth() - Config.PUNCH_DAMAGE);
                            logger.logWarning("Player " + attacker.getId() + " punched Player " + targetLeft.getId() +
                                    " reducing health by " + Config.PUNCH_DAMAGE + ".");
                        }
                        break;
                    case PUNCH_RIGHT:
                        Player targetRight = game.getPlayerAtPosition(attacker.getX() + 1, attacker.getY());
                        if (targetRight != null && !targetRight.isDisqualified()) {
                            targetRight.setHealth(targetRight.getHealth() - Config.PUNCH_DAMAGE);
                            logger.logWarning("Player " + attacker.getId() + " punched Player " + targetRight.getId() +
                                    " reducing health by " + Config.PUNCH_DAMAGE + ".");
                        }
                        break;
                    case KICK_LEFT:
                        Player kickedLeft = game.getPlayerAtPosition(attacker.getX() - 1, attacker.getY());
                        if (kickedLeft != null && !kickedLeft.isDisqualified()) {
                            kickedLeft.setX(Math.max(kickedLeft.getX() - 1, 0));
                            logger.logInfo("Player " + attacker.getId() + " kicked Player " + kickedLeft.getId() +
                                    " to the left. New x=" + kickedLeft.getX());
                        }
                        break;
                    case KICK_RIGHT:
                        Player kickedRight = game.getPlayerAtPosition(attacker.getX() + 1, attacker.getY());
                        if (kickedRight != null && !kickedRight.isDisqualified()) {
                            kickedRight.setX(Math.min(kickedRight.getX() + 1, Config.X_MAX));
                            logger.logInfo("Player " + attacker.getId() + " kicked Player " + kickedRight.getId() +
                                    " to the right. New x=" + kickedRight.getX());
                        }
                        break;
                    default:
                        // Other actions are handled elsewhere
                        break;
                }
            }
        }
    }

    /**
     * Checks if a player is colliding with a hazard or boost.
     *
     * @param player The player.
     * @param entity The hazard or boost.
     * @return True if colliding, false otherwise.
     */
    private boolean isColliding(Player player, Object entity) {
        if (entity instanceof Hazard) {
            Hazard hazard = (Hazard) entity;
            return player.getX() == hazard.getX() && player.getY() == hazard.getY();
        } else if (entity instanceof Boost) {
            Boost boost = (Boost) entity;
            return player.getX() == boost.getX() && player.getY() == boost.getY();
        }
        return false;
    }
}