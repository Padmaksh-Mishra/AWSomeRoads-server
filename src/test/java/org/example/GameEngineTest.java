// src/test/java/org/example/engine/GameEngineTest.java

package org.example;

import org.example.config.Config;
import org.example.engine.GameEngine;
import org.example.model.Game;
import org.example.model.Input;
import org.example.model.Player;
import org.example.model.PlayerAction;
import org.example.utils.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameEngineTest {

    @Mock
    private Logger mockLogger;

    @InjectMocks
    private GameEngine gameEngine;

    /**
     * Tests updating player actions with MOVE_LEFT action.
     */
    @Test
    void testUpdatePlayerActions_MoveLeft() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(1);
        when(input.getActions()).thenReturn(List.of(PlayerAction.MOVE_LEFT));
        when(mockGame.getPlayerById(1)).thenReturn(mockPlayer);
        when(mockPlayer.getX()).thenReturn(5);
        when(mockPlayer.getId()).thenReturn(1);

        List<Input> inputs = List.of(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        verify(mockPlayer).setX(4); // 5 - 1 = 4
        verify(mockLogger).logInfo("Player 1 moved left to x=4");
    }

    /**
     * Tests updating player actions with MOVE_RIGHT action.
     */
    @Test
    void testUpdatePlayerActions_MoveRight() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(2);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_RIGHT));
        when(mockGame.getPlayerById(2)).thenReturn(mockPlayer);
        when(mockPlayer.getX()).thenReturn(3);
        when(mockPlayer.getId()).thenReturn(2);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        int expectedX = Math.min(3 + 1, Config.X_MAX); // 3 + 1 = 4, Config.X_MAX = 5
        verify(mockPlayer).setX(expectedX);
        verify(mockLogger).logInfo("Player 2 moved right to x=4");
    }

    /**
     * Tests updating player actions with STEP_DOWN action.
     */
    @Test
    void testUpdatePlayerActions_StepDown() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(3);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.STEP_DOWN));
        when(mockGame.getPlayerById(3)).thenReturn(mockPlayer);
        when(mockPlayer.getY()).thenReturn(7.0f);
        when(mockPlayer.getId()).thenReturn(3);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        float expectedY = Math.min(7.0f + Config.STEP_DOWN_SIZE, Config.Y_MAX); // 7.0 + 2.5 = 9.5
        verify(mockPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 3 stepped down to y=7.5");
    }

    /**
     * Tests updating player actions with STEP_DOWN action exceeding Y_MAX.
     */
    @Test
    void testUpdatePlayerActions_StepDownExceedsYMax() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(4);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.STEP_DOWN));
        when(mockGame.getPlayerById(4)).thenReturn(mockPlayer);
        when(mockPlayer.getY()).thenReturn(98.0f); // Close to Y_MAX = 100
        when(mockPlayer.getId()).thenReturn(4);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        float expectedY = Math.min(98.0f + Config.STEP_DOWN_SIZE, Config.Y_MAX); // 98.0 + 2.5 = 100.0 (capped)
        verify(mockPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 4 stepped down to y=100.0");
    }

    /**
     * Tests updating player actions with multiple actions.
     */
    @Test
    void testUpdatePlayerActions_MultipleActions() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(5);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_LEFT, PlayerAction.STEP_DOWN));
        when(mockGame.getPlayerById(5)).thenReturn(mockPlayer);
        when(mockPlayer.getX()).thenReturn(2);
        when(mockPlayer.getY()).thenReturn(4.0f);
        when(mockPlayer.getId()).thenReturn(5);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        int expectedX = Math.max(2 - 1, 0); // 2 - 1 = 1
        float expectedY = Math.min(4.0f + Config.STEP_DOWN_SIZE, Config.Y_MAX); // 4.0 + 2.5 = 6.5
        verify(mockPlayer).setX(expectedX);
        verify(mockLogger).logInfo("Player 5 moved left to x=1");
        verify(mockPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 5 stepped down to y=6.5");
    }

    /**
     * Tests updating player actions for a disqualified player.
     */
    @Test
    void testUpdatePlayerActions_DisqualifiedPlayer() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(6);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_RIGHT));
        when(mockGame.getPlayerById(6)).thenReturn(mockPlayer);
        when(mockPlayer.isDisqualified()).thenReturn(true);
        when(mockPlayer.getId()).thenReturn(6);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        verify(mockPlayer, never()).setX(anyInt());
        verify(mockLogger, never()).logInfo(anyString());
    }

    /**
     * Tests updating player actions for a non-existent player.
     */
    @Test
    void testUpdatePlayerActions_NonExistentPlayer() {
        // Arrange
        Game mockGame = mock(Game.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(7);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_LEFT));
        when(mockGame.getPlayerById(7)).thenReturn(null);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        verify(mockGame, times(1)).getPlayerById(7);
        // Ensure no further interactions
        verifyNoMoreInteractions(mockGame);
    }

    /**
     * Tests updating player actions with an unsupported action.
     */
    @Test
    void testUpdatePlayerActions_UnsupportedAction() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(8);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.PUNCH_LEFT)); // Assuming PUNCH_LEFT is not handled here
        when(mockGame.getPlayerById(8)).thenReturn(mockPlayer);
        when(mockPlayer.getId()).thenReturn(8);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        // No position changes should occur
        verify(mockPlayer, never()).setX(anyInt());
        verify(mockPlayer, never()).setY(anyFloat());
        verify(mockLogger, never()).logInfo(anyString());
    }

    /**
     * Tests handling multiple players with various actions.
     */
    @Test
    void testUpdatePlayerActions_MultiplePlayers() {
        // Arrange
        Game mockGame = mock(Game.class);

        Player mockPlayer1 = mock(Player.class);
        when(mockPlayer1.getId()).thenReturn(9);
        when(mockPlayer1.getX()).thenReturn(5);
        when(mockPlayer1.getY()).thenReturn(5.0f);
        when(mockPlayer1.isDisqualified()).thenReturn(false);

        Player mockPlayer2 = mock(Player.class);
        when(mockPlayer2.getId()).thenReturn(10);
        when(mockPlayer2.getX()).thenReturn(3);
        when(mockPlayer2.getY()).thenReturn(7.0f);
        when(mockPlayer2.isDisqualified()).thenReturn(false);

        when(mockGame.getPlayerById(9)).thenReturn(mockPlayer1);
        when(mockGame.getPlayerById(10)).thenReturn(mockPlayer2);

        Input input1 = mock(Input.class);
        when(input1.getPlayerId()).thenReturn(9);
        when(input1.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_LEFT, PlayerAction.STEP_DOWN));

        Input input2 = mock(Input.class);
        when(input2.getPlayerId()).thenReturn(10);
        when(input2.getActions()).thenReturn(Arrays.asList(PlayerAction.MOVE_RIGHT));

        List<Input> inputs = Arrays.asList(input1, input2);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        // Player 9
        int expectedX1 = Math.max(5 - 1, 0); // 5 - 1 = 4
        float expectedY1 = Math.min(5.0f + Config.STEP_DOWN_SIZE, Config.Y_MAX); // 5.0 + 2.5 = 7.5
        verify(mockPlayer1).setX(expectedX1);
        verify(mockLogger).logInfo("Player 9 moved left to x=4");
        verify(mockPlayer1).setY(expectedY1);
        verify(mockLogger).logInfo("Player 9 stepped down to y=7.5");

        // Player 10
        int expectedX2 = Math.min(3 + 1, Config.X_MAX); // 3 + 1 = 4
        verify(mockPlayer2).setX(expectedX2);
        verify(mockLogger).logInfo("Player 10 moved right to x=4");
    }

    /**
     * Tests updating player actions with STEP_DOWN action reaching Y_MAX.
     */
    @Test
    void testUpdatePlayerActions_StepDownReachesYMax() {
        // Arrange
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);
        Input input = mock(Input.class);

        when(input.getPlayerId()).thenReturn(11);
        when(input.getActions()).thenReturn(Arrays.asList(PlayerAction.STEP_DOWN));
        when(mockGame.getPlayerById(11)).thenReturn(mockPlayer);
        when(mockPlayer.getY()).thenReturn(96.0f); // 96 + 5.0 = 101 -> capped at 100
        when(mockPlayer.getId()).thenReturn(11);

        List<Input> inputs = Arrays.asList(input);

        // Act
        gameEngine.updatePlayerActions(mockGame, inputs);

        // Assert
        float expectedY = Math.min(96.0f + Config.STEP_DOWN_SIZE, Config.Y_MAX); // 96.0 + 2.5 = 98.5
        verify(mockPlayer).setY(expectedY);
        verify(mockLogger).logInfo("Player 11 stepped down to y=98.5");
    }
}