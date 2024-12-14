package org.example.controller;

import org.example.GameServer;
import org.example.model.Input;
import org.example.model.PlayerAction;
import org.example.serializer.GameStateSerializer;
import org.example.utils.Logger;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * This class handles the communication between the server and a single client.
 *
 * Responsibilities include:
 * - Receiving input actions from the client
 * - Sending updated game state to the client
 * - Managing the client socket connection
 */

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final GameServer server;
    private final int playerId;
    private final Logger logger;
    private final GameStateSerializer serializer;
    private volatile Input latestInput;
    private volatile boolean isConnected;

    /**
     * Constructor to initialize the ClientHandler.
     *
     * @param clientSocket The client's socket connection.
     * @param server       Reference to the GameServer.
     * @param playerId     The unique identifier assigned to the player.
     */
    public ClientHandler(Socket clientSocket, GameServer server, int playerId) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.playerId = playerId;
        this.logger = new Logger();
        this.serializer = new GameStateSerializer();
        this.latestInput = null;
        this.isConnected = true;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            // Send initial player information to the client
            sendInitialInfo(out);

            String message;
            while (isConnected && (message = in.readLine()) != null) {
                // Assuming the client sends inputs as JSON strings
                Input input = serializer.deserializeInput(message);
                if (input != null && input.getPlayerId() == playerId) {
                    latestInput = input;
                    logger.logInfo("Received input from Player " + playerId + ": " + input);
                }
            }
        } catch (IOException e) {
            logger.logError("Connection error with Player " + playerId + ": " + e.getMessage());
        } finally {
            closeConnection();
            server.removeClientHandler(this);
        }
    }

    /**
     * Sends the initial player information to the client.
     *
     * @param out The BufferedWriter to send data to the client.
     * @throws IOException If an I/O error occurs.
     */
    private void sendInitialInfo(BufferedWriter out) throws IOException {
        String initialInfo = serializer.serializeInitialPlayerInfo(playerId);
        out.write(initialInfo);
        out.newLine();
        out.flush();
        logger.logInfo("Sent initial info to Player " + playerId);
    }

    /**
     * Sends the serialized game state to the client.
     *
     * @param gameState The serialized game state as a JSON string.
     */
    public void sendGameState(String gameState) {
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()));
            out.write(gameState);
            out.newLine();
            out.flush();
            logger.logInfo("Sent game state to Player " + playerId);
        } catch (IOException e) {
            logger.logError("Error sending game state to Player " + playerId + ": " + e.getMessage());
            closeConnection();
            server.removeClientHandler(this);
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
     * Clears the latest input after it has been processed.
     */
    public void clearLatestInput() {
        latestInput = null;
    }

    /**
     * Closes the client connection gracefully.
     */
    public void closeConnection() {
        isConnected = false;
        try {
            clientSocket.close();
            logger.logInfo("Closed connection with Player " + playerId);
        } catch (IOException e) {
            logger.logError("Error closing connection with Player " + playerId + ": " + e.getMessage());
        }
    }
}