package org.example.controller;

import org.example.GameServer;
import org.example.model.Input;
import org.example.serializer.GameStateSerializer;
import org.example.utils.Logger;
import org.java_websocket.WebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * This class handles the communication between the server and a single client using WebSockets.
 *
 * Responsibilities include:
 * - Receiving input actions from the client
 * - Sending updated game state to the client
 * - Managing the WebSocket connection
 */

public class ClientHandler implements Runnable {
    private final WebSocket connection;
    private final GameServer server;
    private final int playerId;
    private final Logger logger;
    private volatile Input latestInput;
    private volatile boolean isConnected;
    private final ObjectMapper objectMapper;

    /**
     * Constructor to initialize the ClientHandler.
     *
     * @param connection The client's WebSocket connection.
     * @param server     Reference to the GameServer.
     * @param playerId   The unique identifier assigned to the player.
     * @param logger     The logger instance to be used.
     */
    public ClientHandler(WebSocket connection, GameServer server, int playerId, Logger logger) {
        this.connection = connection;
        this.server = server;
        this.playerId = playerId;
        this.logger = logger;
        this.latestInput = null;
        this.isConnected = true;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        // WebSocket connections are event-driven, so no need for a loop here
    }

    /**
     * Sends the serialized game state to the client.
     *
     * @param gameState The serialized game state as a JSON string.
     */
    public void sendGameState(String gameState) {
        if (isConnected) {
            connection.send(gameState);
            logger.logInfo("Sent game state to Player " + playerId);
        }
    }

    /**
     * Retrieves the latest input received from the client.
     *
     * @return The latest Input object, or null if none.
     */
    public Input getLatestInput() {
        return latestInput;
    }

    /**
     * Sets the latest input received from the client.
     *
     * @param input The Input object received.
     */
    public void setLatestInput(Input input) {
        this.latestInput = input;
        logger.logInfo("Received input from Player " + playerId + ": " + input);
    }

    /**
     * Clears the latest input after it has been processed.
     */
    public void clearLatestInput() {
        this.latestInput = null;
    }

    /**
     * Closes the client connection gracefully.
     */
    public void closeConnection() {
        isConnected = false;
        if (connection != null && !connection.isClosed()) {
            connection.close();
            logger.logInfo("Closed connection with Player " + playerId);
        }
        server.removeClientHandler(this);
    }

    /**
     * Handles incoming messages from the client.
     *
     * @param message The received message as a JSON string.
     */
    public void handleMessage(String message) {
        try {
            Input input = objectMapper.readValue(message, Input.class);
            if (input.getPlayerId() == playerId) {
                setLatestInput(input);
            } else {
                logger.logError("Player ID mismatch. Expected: " + playerId + ", Received: " + input.getPlayerId());
            }
        } catch (IOException e) {
            logger.logError("Error parsing input from Player " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the unique player ID associated with this handler.
     *
     * @return The player ID.
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the WebSocket connection.
     *
     * @return The WebSocket connection.
     */
    public WebSocket getConnection() {
        return connection;
    }
}