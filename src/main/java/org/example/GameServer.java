package org.example;

import org.example.engine.GameEngine;
import org.example.engine.Simulator;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.Input;
import org.example.serializer.GameStateSerializer;
import org.example.utils.Logger;
import org.example.controller.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class serves as the main server for the game.
 *
 * Responsibilities include:
 * - Initializing server components (GameEngine, GameStateSerializer)
 * - Accepting client connections and creating ClientHandlers
 * - Managing players and their inputs
 * - Running the main game loop to update and broadcast game state
 * - Declaring the winner when the game concludes
 */
public class GameServer {
    private static final int PORT = 12345; // Example port number
    private static final int TICK_RATE = 100; // Game update interval in milliseconds

    private final Game game;
    private final GameEngine gameEngine;
    private final Simulator simulator;
    private final GameStateSerializer serializer;
    private final Logger logger;
    private final ExecutorService clientThreadPool;
    private final List<ClientHandler> clientHandlers;
    private final ScheduledExecutorService gameLoopExecutor;
    private volatile boolean isRunning;
    private int playerIdCounter;

    /**
     * Constructor to initialize the GameServer.
     */
    public GameServer() {
        this.game = new Game();
        this.gameEngine = new GameEngine();
        this.simulator = new Simulator(gameEngine);
        this.serializer = new GameStateSerializer();
        this.logger = new Logger();
        this.clientThreadPool = Executors.newCachedThreadPool();
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
        this.isRunning = true;
        this.playerIdCounter = 0;
    }

    /**
     * Starts the GameServer to accept client connections and run the game loop.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.logInfo("GameServer started on port " + PORT);

            // Start the game loop
            startGameLoop();

            // Accept client connections
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                logger.logInfo("New client connected: " + clientSocket.getInetAddress());

                // Create and start a new ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket, this, playerIdCounter);
                clientHandlers.add(handler);
                clientThreadPool.execute(handler);

                // Create a new Player and add to the game
                Player player = new Player(playerIdCounter, playerIdCounter, 70, Config.HEALTH_MAX);
                game.addPlayer(player);
                logger.logInfo("Player " + player.getId() + " added with starting position (x=" + player.getX() + ", y=" + player.getY() + ")");

                playerIdCounter++;
            }
        } catch (IOException e) {
            logger.logError("Server encountered an error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Starts the main game loop which updates and broadcasts the game state at fixed intervals.
     */
    private void startGameLoop() {
        gameLoopExecutor.scheduleAtFixedRate(() -> {
            try {
                // Gather all inputs from client handlers
                List<Input> allInputs = new ArrayList<>();
                synchronized (clientHandlers) {
                    for (ClientHandler handler : clientHandlers) {
                        Input input = handler.getLatestInput();
                        if (input != null) {
                            allInputs.add(input);
                            handler.clearLatestInput();
                        }
                    }
                }

                // Add inputs to the game state
                game.addInputs(allInputs);

                // Simulate game step
                simulator.simulateStep(game, allInputs);

                // Serialize the game state
                String serializedGameState = serializer.serialize(game);

                // Broadcast the game state to all clients
                broadcastGameState(serializedGameState);

                // Check for game over conditions
                checkGameOver();

                // Clear latest inputs after processing
                game.clearLatestInputs();
            } catch (Exception e) {
                logger.logError("Error during game loop: " + e.getMessage());
            }
        }, 0, TICK_RATE, TimeUnit.MILLISECONDS);
    }

    /**
     * Broadcasts the serialized game state to all connected clients.
     *
     * @param gameState The serialized game state as a JSON string.
     */
    private void broadcastGameState(String gameState) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendGameState(gameState);
            }
        }
    }

    /**
     * Checks if the game has concluded and declares the winner if conditions are met.
     */
    private void checkGameOver() {
        List<Player> activePlayers = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            if (!player.isDisqualified()) {
                activePlayers.add(player);
            }
        }

        if (activePlayers.size() <= 1) {
            String winnerMessage = activePlayers.isEmpty() ? "No winners. All players disqualified." :
                    "Player " + activePlayers.get(0).getId() + " wins the game!";
            logger.logInfo(winnerMessage);
            broadcastGameState(serializer.serializeGameOver(winnerMessage));
            isRunning = false;
            shutdown();
        } else {
            for (Player player : activePlayers) {
                if (player.getX() == 0) {
                    String winnerMessage = "Player " + player.getId() + " has reached x=0 and wins the game!";
                    logger.logInfo(winnerMessage);
                    broadcastGameState(serializer.serializeGameOver(winnerMessage));
                    isRunning = false;
                    shutdown();
                    break;
                }
            }
        }
    }

    /**
     * Shuts down the server and all its components gracefully.
     */
    private void shutdown() {
        try {
            isRunning = false;
            gameLoopExecutor.shutdown();
            clientThreadPool.shutdownNow();
            synchronized (clientHandlers) {
                for (ClientHandler handler : clientHandlers) {
                    handler.closeConnection();
                }
            }
            logger.logInfo("GameServer has been shut down.");
        } catch (Exception e) {
            logger.logError("Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Removes a ClientHandler from the list of active handlers.
     *
     * @param handler The ClientHandler to remove.
     */
    public void removeClientHandler(ClientHandler handler) {
        clientHandlers.remove(handler);
        logger.logInfo("ClientHandler removed. Active clients: " + clientHandlers.size());
    }

    /**
     * Entry point for the GameServer.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}
