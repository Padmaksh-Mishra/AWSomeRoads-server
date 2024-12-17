package org.example;

import org.example.config.Config;
import org.example.model.Game;
import org.example.model.Player;
import org.example.serializer.GameStateSerializer;
import org.example.utils.Logger;
import org.example.controller.ClientHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Tests for the [GameServer](src/main/java/org/example/GameServer.java) class.
 */
@ExtendWith(MockitoExtension.class)
public class GameServerTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private Game mockGame;

    @Mock
    private GameStateSerializer mockSerializer;

    @Mock
    private ClientHandler mockClientHandler;

    @Mock
    private WebSocket mockWebSocket;

    @Mock
    private ClientHandshake mockHandshake;

    @InjectMocks
    private GameServer gameServer;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Inject mocks into GameServer
        gameServer.setGame(mockGame);
        gameServer.setLogger(mockLogger);
        gameServer.setSerializer(mockSerializer);
        gameServer.setGameStarted(true);
    }

    /**
     * Tests handling a new client connection.
     */
    @Test
    void testOnOpen_NewClientAdded() {
        // Arrange
        when(mockWebSocket.getRemoteSocketAddress()).thenReturn(null); // Mock address as null for simplicity

        // Act
        gameServer.onOpen(mockWebSocket, mockHandshake);

        // Assert
        verify(mockLogger).logInfo("New client connected: null");
        verify(mockGame).addPlayer(any(Player.class));
        verify(mockLogger).logInfo(contains("Player "));
        // Further assertions can be added based on the implementation of ClientHandler and other interactions
    }

    /**
     * Tests handling client disconnection without triggering shutdown.
     */
    @Test
    void testOnClose_ClientDisconnected_NoShutdown() {
        // Arrange
        ClientHandler handler = mock(ClientHandler.class);
        when(handler.getConnection()).thenReturn(mockWebSocket);
        gameServer.getClientHandlers().add(handler);

        // Act
        gameServer.onClose(mockWebSocket, 1000, "Normal closure", false);

        // Assert
        verify(mockLogger).logInfo("Client disconnected: null");
        verify(gameServer, never()).shutdown();
    }

    /**
     * Tests handling incoming messages from clients.
     */
    @Test
    void testOnMessage_MessageHandledByClientHandler() {
        // Arrange
        String message = "{\"playerId\":1,\"actions\":[\"PUNCH_RIGHT\"]}";
        ClientHandler handler1 = mock(ClientHandler.class);
        ClientHandler handler2 = mock(ClientHandler.class);
        when(handler1.getConnection()).thenReturn(mockWebSocket);
        when(handler2.getConnection()).thenReturn(mockWebSocket);
        gameServer.getClientHandlers().add(handler1);
        gameServer.getClientHandlers().add(handler2);

        // Act
        gameServer.onMessage(mockWebSocket, message);

        // Assert
        verify(handler1).handleMessage(message);
        verify(handler2).handleMessage(message);
        // Since both handlers have the same connection, both will handle the message.
    }

    /**
     * Tests starting the server.
     */
    @Test
    void testStart_ServerStarted() {
        // Act
        gameServer.start();

        // Assert
        verify(mockLogger).logInfo("GameServer started on port " + Config.PORT);
        verify(gameServer, never()).startGameLoop(); // Assuming startGameLoop is called based on player connections
    }

    /**
     * Tests broadcasting game state to all clients.
     */
    @Test
    void testBroadcastGameState_StateBroadcast() {
        // Arrange
        String gameState = "{\"state\":\"running\"}";
        ClientHandler handler1 = mock(ClientHandler.class);
        ClientHandler handler2 = mock(ClientHandler.class);
        gameServer.getClientHandlers().add(handler1);
        gameServer.getClientHandlers().add(handler2);

        // Act
        gameServer.broadcastGameState(gameState);

        // Assert
        verify(handler1, times(1)).sendGameState(gameState);
        verify(handler2, times(1)).sendGameState(gameState);
    }

    /**
     * Tests shutting down the server gracefully.
     */
    @Test
    void testShutdown_ServerShutDown() throws Exception {
        // Arrange
        ClientHandler handler1 = mock(ClientHandler.class);
        ClientHandler handler2 = mock(ClientHandler.class);
        gameServer.getClientHandlers().add(handler1);
        gameServer.getClientHandlers().add(handler2);

        // Act
        gameServer.shutdown();

        // Assert
        verify(handler1).closeConnection();
        verify(handler2).closeConnection();
        verify(gameServer).stop();
        verify(mockLogger).logInfo("GameServer has been shut down.");
    }

    /**
     * Tests removing a client handler.
     */
    @Test
    void testRemoveClientHandler_HandlerRemoved() {
        // Arrange
        ClientHandler handler = mock(ClientHandler.class);
        when(handler.getPlayerId()).thenReturn(1);
        gameServer.getClientHandlers().add(handler);

        // Act
        gameServer.removeClientHandler(handler);

        // Assert
        verify(mockLogger).logInfo("Removed ClientHandler for Player 1");
        assertFalse(gameServer.getClientHandlers().contains(handler));
    }

    /**
     * Tests handling errors on the WebSocket connection.
     */
    @Test
    void testOnError_ErrorHandled() {
        // Arrange
        Exception exception = new Exception("Test exception");

        // Act
        gameServer.onError(mockWebSocket, exception);

        // Assert
        verify(mockLogger).logError("WebSocket error: Test exception");
        verify(mockWebSocket).close();
    }

    /**
     * Tests handling errors when connection is null.
     */
    @Test
    void testOnError_ErrorHandled_NullConnection() {
        // Arrange
        Exception exception = new Exception("Test exception");

        // Act
        gameServer.onError(null, exception);

        // Assert
        verify(mockLogger).logError("WebSocket error: Test exception");
        verify(mockWebSocket, never()).close();
    }

    /**
     * Tests checkGameOver with multiple active players and no winner.
     */
    @Test
    void testCheckGameOver_MultipleActivePlayers_NoWinner() throws JsonProcessingException {
        // Arrange
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);

        when(player1.isDisqualified()).thenReturn(false);
        when(player1.getId()).thenReturn(1);
        when(player1.getY()).thenReturn(3f);

        when(player2.isDisqualified()).thenReturn(false);
        when(player2.getId()).thenReturn(2);
        when(player2.getY()).thenReturn(5f);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        when(mockGame.getPlayers()).thenReturn(players);

        // Act
        gameServer.checkGameOver();

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, never()).serializeGameOver(anyString());
        verify(mockLogger, never()).logInfo(anyString());
        verify(gameServer, never()).broadcastGameState(anyString());
        verify(gameServer, never()).shutdown();
    }

    /**
     * Tests checkGameOver when the game hasn't started.
     */
    @Test
    void testCheckGameOver_GameNotStarted() throws JsonProcessingException {
        // Arrange
        gameServer.setGameStarted(false);

        // Act
        gameServer.checkGameOver();

        // Assert
        verify(mockGame, never()).getPlayers();
        verify(mockSerializer, never()).serializeGameOver(anyString());
        verify(mockLogger, never()).logInfo(anyString());
        verify(gameServer, never()).broadcastGameState(anyString());
        verify(gameServer, never()).shutdown();
    }

    /**
     * Tests checkGameOver with a single active player.
     */
    @Test
    void testCheckGameOver_SingleActivePlayer() throws JsonProcessingException {
        // Arrange
        Player activePlayer = mock(Player.class);
        when(activePlayer.isDisqualified()).thenReturn(false);
        when(activePlayer.getId()).thenReturn(1);

        List<Player> players = new ArrayList<>();
        players.add(activePlayer);

        when(mockGame.getPlayers()).thenReturn(players);
        when(mockSerializer.serializeGameOver("Player 1 wins the game!"))
                .thenReturn("{\"state\":\"Player 1 wins the game!\"}");

        // Act
        gameServer.checkGameOver();

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, times(1)).serializeGameOver("Player 1 wins the game!");
        verify(mockLogger, times(1)).logInfo("Player 1 wins the game!");
        verify(gameServer, times(1)).broadcastGameState("{\"state\":\"Player 1 wins the game!\"}");
        verify(gameServer, times(1)).shutdown();
    }

    /**
     * Tests checkGameOver when a player reaches y=0.
     */
    @Test
    void testCheckGameOver_PlayerReachesYZero() throws JsonProcessingException {
        // Arrange
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);

        when(player1.isDisqualified()).thenReturn(false);
        when(player1.getId()).thenReturn(1);
        when(player1.getY()).thenReturn(0f);

        when(player2.isDisqualified()).thenReturn(false);
        when(player2.getId()).thenReturn(2);
        when(player2.getY()).thenReturn(5f);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        when(mockGame.getPlayers()).thenReturn(players);
        when(mockSerializer.serializeGameOver("Player 1 has reached y=0 and wins the game!"))
                .thenReturn("{\"state\":\"Player 1 has reached y=0 and wins the game!\"}");

        // Act
        gameServer.checkGameOver();

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, times(1))
                .serializeGameOver("Player 1 has reached y=0 and wins the game!");
        verify(mockLogger, times(1))
                .logInfo("Player 1 has reached y=0 and wins the game!");
        verify(gameServer, times(1))
                .broadcastGameState("{\"state\":\"Player 1 has reached y=0 and wins the game!\"}");
        verify(gameServer, times(1)).shutdown();
    }

    /**
     * Tests checkGameOver when serialization throws an exception.
     */
    @Test
    void testCheckGameOver_SerializationException() throws JsonProcessingException {
        // Arrange
        Player activePlayer = mock(Player.class);
        when(activePlayer.isDisqualified()).thenReturn(false);
        when(activePlayer.getId()).thenReturn(1);

        List<Player> players = new ArrayList<>();
        players.add(activePlayer);

        when(mockGame.getPlayers()).thenReturn(players);
        when(mockSerializer.serializeGameOver("Player 1 wins the game!"))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        // Act
        try {
            gameServer.checkGameOver();
        } catch (JsonProcessingException e) {
            // Expected exception
        }

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, times(1)).serializeGameOver("Player 1 wins the game!");
        verify(mockLogger, times(1)).logInfo("Player 1 wins the game!");
        verify(gameServer, times(1)).shutdown();
    }

    /**
     * Tests checkGameOver when there are no active players.
     */
    @Test
    void testCheckGameOver_NoActivePlayers() throws JsonProcessingException {
        // Arrange
        List<Player> players = new ArrayList<>();
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        when(player1.isDisqualified()).thenReturn(true);
        when(player2.isDisqualified()).thenReturn(true);
        players.add(player1);
        players.add(player2);

        when(mockGame.getPlayers()).thenReturn(players);
        when(mockSerializer.serializeGameOver("No winners. All players disqualified."))
                .thenReturn("{\"state\":\"No winners. All players disqualified.\"}");

        // Act
        gameServer.checkGameOver();

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, times(1)).serializeGameOver("No winners. All players disqualified.");
        verify(mockLogger, times(1)).logInfo("No winners. All players disqualified.");
        verify(gameServer, times(1)).broadcastGameState("{\"state\":\"No winners. All players disqualified.\"}");
        verify(gameServer, times(1)).shutdown();
    }

    /**
     * Tests checkGameOver when a player reaches y=0 and serialization fails.
     */
    @Test
    void testCheckGameOver_PlayerReachesYZero_SerializationException() throws JsonProcessingException {
        // Arrange
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);

        when(player1.isDisqualified()).thenReturn(false);
        when(player1.getId()).thenReturn(1);
        when(player1.getY()).thenReturn(0f);

        when(player2.isDisqualified()).thenReturn(false);
        when(player2.getId()).thenReturn(2);
        when(player2.getY()).thenReturn(5f);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        when(mockGame.getPlayers()).thenReturn(players);
        when(mockSerializer.serializeGameOver("Player 1 has reached y=0 and wins the game!"))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        // Act
        try {
            gameServer.checkGameOver();
        } catch (JsonProcessingException e) {
            // Expected exception
        }

        // Assert
        verify(mockGame, times(1)).getPlayers();
        verify(mockSerializer, times(1))
                .serializeGameOver("Player 1 has reached y=0 and wins the game!");
        verify(mockLogger, times(1))
                .logInfo("Player 1 has reached y=0 and wins the game!");
        verify(gameServer, times(1)).shutdown();
    }

    // Additional tests can be added here as needed.

}