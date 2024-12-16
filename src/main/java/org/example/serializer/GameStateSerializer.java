package org.example.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.model.Game;

import java.util.HashMap;
import java.util.Map;

/**
 * This class serializes the game state and related information to JSON strings.
 *
 * Responsibilities include:
 * - Converting the Game object to a JSON string.
 * - Serializing initial player information.
 * - Serializing game-over messages.
 */
public class GameStateSerializer {
    private final ObjectMapper objectMapper;

    /**
     * Constructor to initialize the ObjectMapper.
     */
    public GameStateSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Serializes the entire game state to a JSON string.
     *
     * @param game The current game state.
     * @return The serialized game state as a JSON string.
     * @throws JsonProcessingException If serialization fails.
     */
    public String serialize(Game game) throws JsonProcessingException {
        // Configure ObjectMapper to format numbers as strings if needed
        //objectMapper.configure(SerializationFeature.WRITE_NUMBERS_AS_STRINGS, false);
        // Use custom serializer if needed to format y-coordinates
        return objectMapper.writeValueAsString(game);
    }

    /**
     * Serializes the initial player information to a JSON string.
     *
     * @param playerId The unique identifier of the player.
     * @return The serialized initial player information as a JSON string.
     * @throws JsonProcessingException If serialization fails.
     */
    public String serializeInitialPlayerInfo(int playerId) throws JsonProcessingException {
        Map<String, Object> initialInfo = new HashMap<>();
        initialInfo.put("type", "INITIAL_INFO");
        initialInfo.put("playerId", playerId);

        // Optionally, add more initial information like starting position
        // For example:
        // Player player = game.getPlayerById(playerId);
        // initialInfo.put("x", player.getX());
        // initialInfo.put("y", player.getY());

        return objectMapper.writeValueAsString(initialInfo);
    }

    /**
     * Serializes a game-over message to a JSON string.
     *
     * @param message The game-over message.
     * @return The serialized game-over message as a JSON string.
     * @throws JsonProcessingException If serialization fails.
     */
    public String serializeGameOver(String message) throws JsonProcessingException {
        Map<String, Object> gameOverInfo = new HashMap<>();
        gameOverInfo.put("type", "GAME_OVER");
        gameOverInfo.put("message", message);
        return objectMapper.writeValueAsString(gameOverInfo);
    }

    /**
     * Deserializes a JSON string to an Input object.
     *
     * @param json The JSON string representing the Input.
     * @return The deserialized Input object, or null if deserialization fails.
     */
    public org.example.model.Input deserializeInput(String json) {
        try {
            return objectMapper.readValue(json, org.example.model.Input.class);
        } catch (JsonProcessingException e) {
            // Handle deserialization error (e.g., log the error)
            return null;
        }
    }
}