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
    - HAZARD_PROBABILITY: the probability of generating a new hazard
    - BOOST_PROBABILITY: the probability of generating a new boost

    - SIMULATION_STEP_SIZE: the time step for the simulation
    - STEP_DOWN_SIZE: the step size for moving down

    - PORT: the port number for the server
    - TICK_RATE: the tick rate for the server
 */

public class Config {

    public static final int PORT = 8080;
    public static final int TICK_RATE = 1000;   // 3 seconds for testing purposes

    public static final int MAX_PLAYERS = 2;
    public static final int MIN_PLAYERS = 2;
    public static final int Y_MAX = 100;
    public static final int X_MAX = 5;
    public static final int MAX_HAZARDS = 10;
    public static final int MAX_BOOSTS = 5;
    public static final int HAZARD_DAMAGE = 20;
    public static final int BOOST_POWER = 15;

    public static final int HEALTH_MAX = 100;
    public static final int PUNCH_DAMAGE = 5;
    public static final int FALL_PENALTY = 10;
    public static final int PLAYER_START_Y = 70;
    public static final int WINNING_X = 0;

    public static final float HAZARD_PROBABILITY = 0.01f; // Probability of generating a hazard per simulation step
    public static final float BOOST_PROBABILITY = 0.005f; // Probability of generating a boost per simulation step

    public static final float SIMULATION_STEP_SIZE = 0.10f; // Time step as float

    public static final float STEP_DOWN_SIZE = 0.5f; // Step size for moving down
}