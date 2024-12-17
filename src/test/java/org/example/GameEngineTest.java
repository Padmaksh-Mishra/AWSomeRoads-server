package org.example;


import org.example.config.Config;
import org.example.engine.GameEngine;
import org.example.model.*;
import org.example.utils.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine gameEngine;

    @Mock
    private Logger mockLogger;

    @Mock
    private Game mockGame;

    @Mock
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameEngine = new GameEngine(mockLogger);

        // Reset Boost.idCounter to 0 before each test for consistent ID assignments
        Boost.resetIdCounter();
    }

    // Helper method for creating mock players
    private Player createMockPlayer(int id, int x, int y, int health) {
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(id);
        when(player.getX()).thenReturn(x);
        when(player.getY()).thenReturn((float) y);
        when(player.getHealth()).thenReturn(health);
        when(player.isDisqualified()).thenReturn(false);
        return player;
    }

    /**
     * Test Case: Player moves left within bounds.
     * Use a Spy to allow real behavior (state change) while verifying interactions.
     */
    @Test
    void testUpdatePlayerActions_MoveLeft() {
        // Arrange
        Player realPlayer = new Player(1, 5, 5, 10); // Real Player object
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayerById(1)).thenReturn(spyPlayer);

        Input input = new Input(1, Arrays.asList(PlayerAction.MOVE_LEFT));
        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        assertEquals(4, spyPlayer.getX(), "Player should have moved left to x=4");
        verify(spyPlayer).setX(4); // Verify setX was called with 4
        verify(mockLogger).logInfo("Player 1 moved left to x=4");
    }

    /**
     * Test Case: Player moves right within bounds.
     * Use a Spy to verify state change.
     */
    @Test
    void testUpdatePlayerActions_MoveRight() {
        // Arrange
        Player realPlayer = new Player(3, 4, 5, 10);
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayerById(3)).thenReturn(spyPlayer);

        Input input = new Input(3, List.of(PlayerAction.MOVE_RIGHT));
        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        assertEquals(5, spyPlayer.getX(), "Player should have moved right to x=5");
        verify(spyPlayer).setX(5);
        verify(mockLogger).logInfo("Player 3 moved right to x=5");
    }

    /**
     * Test Case: Player steps down.
     * Use a Spy to verify y-coordinate update.
     */
    @Test
    void testUpdatePlayerActions_StepDown() {
        // Arrange
        Player realPlayer = new Player(5, 5, 10, 10);
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayerById(5)).thenReturn(spyPlayer);

        Input input = new Input(5, Arrays.asList(PlayerAction.STEP_DOWN));
        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        float expectedY = Math.min(10 + Config.STEP_DOWN_SIZE, Config.Y_MAX);
        assertEquals(expectedY, spyPlayer.getY(), "Player should have stepped down to new y-coordinate");
        verify(spyPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 5 stepped down to y=" + expectedY);
    }

    @Test
    void testHandleCollisions_PlayerCollectsBoost() throws Exception {
        // Arrange
        Player mockPlayer = createMockPlayer(6, 3, 3, 10); // Using Mock

        // Create a spy of Boost
        Boost realBoost = new Boost(3, 3); // Initialize with known coordinates
        Boost spyBoost = spy(realBoost); // Create a spy

        // Stub methods on the spy
        when(spyBoost.isActive()).thenReturn(true);
        when(spyBoost.getId()).thenReturn(0); // Assuming this is the first boost

        // Set up the game mock
        when(mockGame.getPlayers()).thenReturn(List.of(mockPlayer));
        when(mockGame.getBoosts()).thenReturn(List.of(spyBoost));

        // Act
        gameEngine.handleCollisions(mockGame);

        // Assert
        verify(spyBoost).applyToPlayer(mockPlayer);
        verify(mockLogger).logInfo("Player 6 collected Boost 0 and gained boost power.");
    }


    /**
     * Test Case: Player's health reaches zero and y-coordinate penalty applied.
     * Use a Spy to verify state changes.
     */
    @Test
    void testHandleDisqualifications_PlayerHealthZero_YPenalty() {
        // Arrange
        Player realPlayer = new Player(7, 5, 5, 0); // Health zero
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayers()).thenReturn(List.of(spyPlayer));
        when(spyPlayer.isDisqualified()).thenReturn(false);

        // Act
        gameEngine.handleDisqualifications(mockGame);

        // Assert
        float expectedY = Math.max(5 - Config.FALL_PENALTY, 0);
        assertEquals(expectedY, spyPlayer.getY(), "Player y should have decreased by FALL_PENALTY");
        verify(spyPlayer).setY(expectedY);
        verify(mockLogger).logInfo(contains("fall penalty"));

        // Since new y < Y_MAX, health should reset
        verify(spyPlayer).setHealth(Config.HEALTH_MAX);
        verify(mockLogger).logInfo("Player 7 has been healed to full health.");
    }

    /**
     * Test Case: Player's y-coordinate reaches Y_MAX and gets disqualified.
     * Use a Spy to verify state changes.
     */
    @Test
    void testHandleDisqualifications_PlayerReachesYMax() {
        // Arrange
        Player realPlayer = new Player(8, 5, Config.Y_MAX, 10); // y = Y_MAX
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayers()).thenReturn(List.of(spyPlayer));
        when(spyPlayer.isDisqualified()).thenReturn(false);

        // Act
        gameEngine.handleDisqualifications(mockGame);

        // Assert
        verify(spyPlayer).disqualify();
        verify(mockLogger).logInfo("Player 8 has been disqualified for reaching Y_MAX.");
    }

    /**
     * Test Case: Player is already disqualified.
     * Use a Mock to verify that no actions are taken.
     */
    @Test
    void testHandleDisqualifications_PlayerAlreadyDisqualified() {
        // Arrange
        Player mockPlayer = createMockPlayer(9, 5, 5, 10);
        when(mockPlayer.isDisqualified()).thenReturn(true);
        when(mockGame.getPlayers()).thenReturn(List.of(mockPlayer));

        // Act
        gameEngine.handleDisqualifications(mockGame);

        // Assert
        verify(mockPlayer, never()).setY(anyFloat());
        verify(mockPlayer, never()).setHealth(anyInt());
        verify(mockLogger, never()).logInfo(contains("fall penalty"));
        verify(mockLogger, never()).logInfo(contains("healed"));
        verify(mockLogger, never()).logInfo(contains("disqualified"));
    }

    /**
     * Test Case: Player punches left successfully.
     * Use Mocks since we don't need state changes in Players.
     */
    @Test
    void testHandleAttacks_PlayerPunchesLeft() {
        // Arrange
        Player attacker = createMockPlayer(10, 5, 50, 10);
        Player target = createMockPlayer(11, 4, 50, 10); // Player to the left

        Input input = new Input(10, List.of(PlayerAction.PUNCH_LEFT));

        when(mockGame.getLatestInputs()).thenReturn(List.of(input));
        when(mockGame.getPlayerById(10)).thenReturn(attacker);
        when(mockGame.getPlayerAtPosition(4, 50)).thenReturn(target);

        // Act
        gameEngine.handleAttacks(mockGame);

        // Assert
        verify(target).setHealth(5); // Assuming PUNCH_DAMAGE = 5
        verify(mockLogger).logWarning(contains("punched Player 11"));
    }

    /**
     * Test Case: Player kicks right successfully.
     * Use a Spy to verify state changes on the kicked player.
     */
    @Test
    void testHandleAttacks_PlayerKicksRight() {
        // Arrange
        Player attacker = createMockPlayer(12, 5, 50, 10);
        Player realTarget = new Player(13, 6, 50, 10); // Player to the right
        Player spyTarget = spy(realTarget); // Using Spy
        when(mockGame.getPlayerAtPosition(6, 50)).thenReturn(spyTarget);
        when(mockGame.getPlayerById(12)).thenReturn(attacker);

        Input input = new Input(12, List.of(PlayerAction.KICK_RIGHT));
        when(mockGame.getLatestInputs()).thenReturn(List.of(input));

        // Act
        gameEngine.handleAttacks(mockGame);

        // Assert
        int expectedX = Math.min(6 + 1, Config.X_MAX);
        assertEquals(expectedX, spyTarget.getX(), "Player 13 should have been kicked right to x=" + expectedX);
        verify(spyTarget).setX(expectedX);
        verify(mockLogger).logInfo("Player 12 kicked Player 13 to the right. New x=" + expectedX);
    }

    /**
     * Test Case: Player kicks left successfully.
     * Use a Spy to verify state changes on the kicked player.
     */
    @Test
    void testHandleAttacks_PlayerKicksLeft() {
        // Arrange
        Player attacker = createMockPlayer(14, 5, 50, 10);
        Player realTarget = new Player(15, 4, 50, 10); // Player to the left
        Player spyTarget = spy(realTarget); // Using Spy
        when(mockGame.getPlayerAtPosition(4, 50)).thenReturn(spyTarget);
        when(mockGame.getPlayerById(14)).thenReturn(attacker);

        Input input = new Input(14, List.of(PlayerAction.KICK_LEFT));
        when(mockGame.getLatestInputs()).thenReturn(List.of(input));

        // Act
        gameEngine.handleAttacks(mockGame);

        // Assert
        int expectedX = Math.max(4 - 1, 0);
        assertEquals(expectedX, spyTarget.getX(), "Player 15 should have been kicked left to x=" + expectedX);
        verify(spyTarget).setX(expectedX);
        verify(mockLogger).logInfo("Player 14 kicked Player 15 to the left. New x=" + expectedX);
    }

    /**
     * Test Case: Multiple actions in a single input.
     * Use a Spy to verify multiple state changes.
     */
    @Test
    void testUpdatePlayerActions_MultipleActions() {
        // Arrange
        Player realPlayer = new Player(16, 5, 5, 10);
        Player spyPlayer = spy(realPlayer); // Using Spy
        when(mockGame.getPlayerById(16)).thenReturn(spyPlayer);

        Input input = new Input(16, Arrays.asList(PlayerAction.MOVE_LEFT, PlayerAction.STEP_DOWN));
        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        assertEquals(4, spyPlayer.getX(), "Player should have moved left to x=4");
        float expectedY = Math.min(5 + Config.STEP_DOWN_SIZE, Config.Y_MAX);
        assertEquals(expectedY, spyPlayer.getY(), "Player should have stepped down");
        verify(spyPlayer).setX(4);
        verify(spyPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 16 moved left to x=4");
        verify(mockLogger).logInfo("Player 16 stepped down to y=" + expectedY);
    }

    /**
     * Test Case: Player performs an undefined action.
     * Use a Mock to ensure no unintended interactions occur.
     */
    @Test
    void testUpdatePlayerActions_UndefinedAction() {
        // Arrange
        Player mockPlayer = createMockPlayer(17, 5, 5, 10); // Using Mock
        when(mockGame.getPlayerById(17)).thenReturn(mockPlayer);

        // Assuming UNDEFINED_ACTION is an enum value not handled in switch
        // If such an action does not exist, create a placeholder or skip this test
        // For demonstration, let's assume it exists
        PlayerAction undefinedAction = PlayerAction.UNDEFINED_ACTION;
        Input input = new Input(17, Arrays.asList(undefinedAction));
        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        verify(mockPlayer, never()).setX(anyInt());
        verify(mockPlayer, never()).setY(anyFloat());
        verify(mockLogger, never()).logInfo(anyString());
    }

    /**
     * Test Case: Handling multiple players with various inputs.
     * Use Spies for players to verify state changes.
     */
    @Test
    void testUpdateGameState_MultiplePlayers() {
        // Arrange
        Player realPlayer1 = new Player(18, 5, 5, 10);
        Player spyPlayer1 = spy(realPlayer1);
        Player realPlayer2 = new Player(19, 10, 10, 10);
        Player spyPlayer2 = spy(realPlayer2);

        when(mockGame.getPlayerById(18)).thenReturn(spyPlayer1);
        when(mockGame.getPlayerById(19)).thenReturn(spyPlayer2);

        Input input1 = new Input(18, Arrays.asList(PlayerAction.MOVE_LEFT));
        Input input2 = new Input(19, Arrays.asList(PlayerAction.STEP_DOWN));
        List<Input> inputs = List.of(input1, input2);

        // Act
        gameEngine.updateGameState(mockGame, inputs);

        // Assert
        // Player 18 moved left
        assertEquals(4, spyPlayer1.getX(), "Player 18 should have moved left to x=4");
        verify(spyPlayer1).setX(4);
        verify(mockLogger).logInfo("Player 18 moved left to x=4");

        // Player 19 stepped down
        float expectedY = Math.min(10 + Config.STEP_DOWN_SIZE, Config.Y_MAX);
        assertEquals(expectedY, spyPlayer2.getY(), "Player 19 should have stepped down");
        verify(spyPlayer2).setY(expectedY);
        verify(mockLogger).logInfo("Player 19 stepped down to y=" + expectedY);
    }

    /**
     * Test Case: Player performs multiple attacks in a single input.
     * Use Spies for some targets to verify state changes.
     */
    @Test
    void testHandleAttacks_MultipleAttacks() {
        // Arrange
        Player attacker = createMockPlayer(20, 5, 50, 10);
        Player targetPunch = createMockPlayer(21, 6, 50, 10);
        Player targetKick = spy(new Player(22, 4, 50, 10)); // Using Spy for kicked player

        when(mockGame.getLatestInputs()).thenReturn(List.of(
                new Input(20, Arrays.asList(PlayerAction.PUNCH_RIGHT, PlayerAction.KICK_LEFT))
        ));
        when(mockGame.getPlayerById(20)).thenReturn(attacker);
        when(mockGame.getPlayerAtPosition(6, 50)).thenReturn(targetPunch);
        when(mockGame.getPlayerAtPosition(4, 50)).thenReturn(targetKick);

        // Act
        gameEngine.handleAttacks(mockGame);

        // Assert
        // Verify punch
        verify(targetPunch).setHealth(5);
        verify(mockLogger).logWarning(contains("punched Player 21"));

        // Verify kick
        int expectedX = Math.max(4 - 1, 0);
        assertEquals(expectedX, targetKick.getX(), "Player 22 should have been kicked left to x=3");
        verify(targetKick).setX(expectedX);
        verify(mockLogger).logInfo("Player 20 kicked Player 22 to the left. New x=" + expectedX);
    }

    /**
     * Test Case: No inputs received.
     * Use Mocks to ensure no actions are taken.
     */
    @Test
    void testUpdateGameState_NoInputs() {
        // Arrange
        when(mockGame.getLatestInputs()).thenReturn(List.of());

        // Act
        gameEngine.updateGameState(mockGame, List.of());

        // Assert
        // Ensure no interactions occur since there are no inputs
        verifyNoInteractions(mockLogger);
    }
}
