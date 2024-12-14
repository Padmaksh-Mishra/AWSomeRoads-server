package org.example.config;

/*
    Contains the configuration for the application and some constants

    Constants:
    - MAX_PLAYERS: the maximum number of players allowed in the game
    - Y_MAX: the maximum y-coordinate allowed for a player
    - X_MAX: the maximum x-coordinate allowed for a player
    - MAX_HAZARDS: the maximum number of hazards allowed on the game board
    - MAX_BOOSTS: the maximum number of boosts allowed on the game board
    - HAZARD_DAMAGE: the amount of damage a hazard causes to a player
    - HAZARD_PENALTY: the penalty for a player when they collide with a hazard
    - BOOST_POWER: the power of the boost
    - HEALTH_MAX: the maximum health of a player
    - PUNCH_DAMAGE: the damage caused by a punch
    - FALL_PENALTY: the penalty applied when a player falls
    - PLAYER_START_Y: the starting y-coordinate of a player
    - WINNING_X: the x-coordinate that a player needs to reach to win
 */

public class Config {
    public static final int MAX_PLAYERS = 4;
    public static final int Y_MAX = 100;
    public static final int X_MAX = 5;
    public static final int MAX_HAZARDS = 10;
    public static final int MAX_BOOSTS = 5;
    public static final int HAZARD_DAMAGE = 20;
    public static final int HAZARD_PENALTY = 10;
    public static final int BOOST_POWER = 15;

    public static final int HEALTH_MAX = 100;
    public static final int PUNCH_DAMAGE = 15;
    public static final int FALL_PENALTY = 10;
    public static final int PLAYER_START_Y = 70;
    public static final int WINNING_X = 0;
}