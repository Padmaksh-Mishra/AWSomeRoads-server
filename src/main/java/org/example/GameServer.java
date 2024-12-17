package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.config.Config;
import org.example.engine.GameEngine;
import org.example.engine.Simulator;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.Input;
import org.example.serializer.GameStateSerializer;
import org.example.utils.Logger;
import org.example.controller.ClientHandler;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * This class serves as the main server for the game using WebSockets.
 *
 * Responsibilities include:
 * - Initializing server components ([GameEngine](src/main/java/org/example/engine/GameEngine.java), [GameStateSerializer](src/main/java/org/example/serializer/GameStateSerializer.java))
 * - Accepting client connections and creating [ClientHandlers](src/main/java/org/example/controller/ClientHandler.java)
 * - Managing players and their inputs
 * - Running the main game loop to update and broadcast game state
 * - Declaring the winner when the game concludes
 */

public class GameServer extends WebSocketServer {
    private static final int PORT = Config.PORT; // Example port number
    private static final int TICK_RATE = Config.TICK_RATE; // Game update interval in milliseconds

    private Game game;
    private final Simulator simulator;
    private GameStateSerializer serializer;
    private Logger logger;
    private final ExecutorService clientThreadPool;
    private final List<ClientHandler> clientHandlers;
    private final ScheduledExecutorService gameLoopExecutor;
    private volatile boolean gameStarted = false;
    private int playerIdCounter;

    /**
     * Constructor to initialize the GameServer.
     */
    public GameServer() {
        super(new InetSocketAddress(PORT));
        this.game = new Game();
        logger = new Logger();
        GameEngine gameEngine = new GameEngine(logger);
        this.simulator = new Simulator(gameEngine);
        this.serializer = new GameStateSerializer();
        this.clientThreadPool = Executors.newCachedThreadPool();
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
        this.playerIdCounter = 0;
    }

    /**
     * Starts the WebSocket server.
     */
    @Override
    public void start() {
        super.start();
        logger.logInfo("GameServer started on port " + PORT);
        // Game loop starts based on player connections
    }

    /**
     * Handles new client connections.
     *
     * @param conn      The WebSocket connection.
     * @param handshake The handshake data.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.logInfo("New client connected: " + conn.getRemoteSocketAddress());
        // Assign a unique player ID
        int playerId = playerIdCounter++;
        // Create and start a new ClientHandler
        ClientHandler handler = new ClientHandler(conn, this, playerId, logger);
        clientHandlers.add(handler);
        clientThreadPool.execute(handler);

        // Create a new Player and add to the game
        Player player = new Player(playerId, playerId, Config.PLAYER_START_Y, Config.HEALTH_MAX);
        game.addPlayer(player);
        logger.logInfo("Player " + player.getId() + " added with starting position (x=" + player.getX() + ", y=" + player.getY() + ")");

        // Check if the game should start
        if (clientHandlers.size() >= Config.MAX_PLAYERS && !gameStarted) {
            gameStarted = true;
            startGameLoop();
            logger.logInfo("Game loop started.");
        }
    }

    /**
     * Handles client disconnections.
     *
     * @param conn   The WebSocket connection.
     * @param code   The disconnection code.
     * @param reason The reason for disconnection.
     * @param remote Whether the disconnection was initiated by the remote host.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.logInfo("Client disconnected: " + conn.getRemoteSocketAddress());
        // Find and remove the corresponding ClientHandler
        clientHandlers.removeIf(handler -> handler.getConnection().equals(conn));

        // Optionally, handle game termination if players drop below minimum
        if (gameStarted && clientHandlers.size() < Config.MIN_PLAYERS) {
            logger.logInfo("Not enough players to continue. Shutting down the game.");
            shutdown();
        }
    }

    /**
     * Handles incoming messages from clients.
     *
     * @param conn    The WebSocket connection.
     * @param message The received message.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Find the corresponding ClientHandler and pass the message
        for (ClientHandler handler : clientHandlers) {
            if (handler.getConnection().equals(conn)) {
                handler.handleMessage(message);
                break;
            }
        }
    }

    /**
     * Handles errors that occur on the WebSocket connection.
     *
     * @param conn The WebSocket connection.
     * @param ex   The exception that was thrown.
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.logError("WebSocket error: " + ex.getMessage());
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Handles the server starting.
     */
    @Override
    public void onStart() {
        logger.logInfo("WebSocket server started successfully.");
    }

    /**
     * Starts the main game loop which updates and broadcasts the game state at fixed intervals.
     */
    void startGameLoop() {
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
                logger.logError("Error in game loop: " + e.getMessage());
            }
        }, 0, TICK_RATE, TimeUnit.MILLISECONDS);
    }

    /**
     * Broadcasts the serialized game state to all connected clients.
     *
     * @param gameState The serialized game state as a JSON string.
     */
    void broadcastGameState(String gameState) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendGameState(gameState);
        }
    }

    /**
     * Checks if the game has concluded and declares the winner if conditions are met.
     */
    void checkGameOver() throws JsonProcessingException {
        if (!gameStarted) {
            // Do not check for game over if the game hasn't started
            return;
        }

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
            shutdown();
        } else {
            for (Player player : activePlayers) {
                if (player.getY() == 0) {
                    String winnerMessage = "Player " + player.getId() + " has reached y=0 and wins the game!";
                    logger.logInfo(winnerMessage);
                    broadcastGameState(serializer.serializeGameOver(winnerMessage));
                    shutdown();
                    break;
                }
            }
        }
    }

    /**
     * Shuts down the server gracefully.
     */
    void shutdown() {
        try {
            gameLoopExecutor.shutdown();
            clientThreadPool.shutdownNow();
            for (ClientHandler handler : clientHandlers) {
                handler.closeConnection();
            }
            this.stop();
            logger.logInfo("GameServer has been shut down.");
        } catch (Exception e) {
            logger.logError("Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Removes a client handler from the list of active handlers.
     *
     * @param handler The ClientHandler to remove.
     */
    
    public void removeClientHandler(ClientHandler handler) {
        clientHandlers.remove(handler);
        logger.logInfo("Removed ClientHandler for Player " + handler.getPlayerId());
    }

    // Setter methods for testing
    public void setGame(Game game) {
        this.game = game;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setSerializer(GameStateSerializer serializer) {
        this.serializer = serializer;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }
    /**
     * Main method to start the GameServer.
     *
     * @param args Command-line arguments.
     */

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}