/*
    Its is an enum class that represents the possible actions that a player can take in the game.
    The possible actions are:
    - MOVE_LEFT: move the player to the left lane (x-coordinate decreases by 1)
    - MOVE_RIGHT: move the player to the right lane (x-coordinate increases by 1)
    - STEP_DOWN: move the player down one step (y-coordinate increases by 1)
    - PUNCH_LEFT: punch to the left (attack the player in the left lane)
    - PUNCH_RIGHT: punch to the right (attack the player in the right lane)
    - KICK_LEFT: kick to the left (attack the player in the left lane)
    - KICK_RIGHT: kick to the right (attack the player in the right lane)
 */


package org.example.model;

public enum PlayerAction {
    MOVE_LEFT,
    MOVE_RIGHT,
    STEP_DOWN,
    PUNCH_LEFT,
    PUNCH_RIGHT,
    KICK_LEFT,
    KICK_RIGHT, UNDEFINED_ACTION;
}
