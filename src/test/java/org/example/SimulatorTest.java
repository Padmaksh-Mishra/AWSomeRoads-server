package org.example;

import org.example.config.Config;
import org.example.engine.GameEngine;
import org.example.engine.Simulator;
import org.example.model.*;
import org.example.utils.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Simulator.
 */
public class SimulatorTest {

    @Mock
    private GameEngine mockGameEngine;

    @Mock
    private Logger mockLogger;

    @Mock
    private Input mockInput;

    @Mock
    private Player mockPlayer; // For player-related tests

    private Simulator simulator;

    private List<Hazard> realHazards;
    private List<Boost> realBoosts;
    private List<Player> realPlayers;

    private Game mockGame;

    @BeforeEach
    public void setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);

        // Initialize real lists for hazards, boosts, and players
        realHazards = new ArrayList<>();
        realBoosts = new ArrayList<>();
        realPlayers = new ArrayList<>();

        // Mock the Game object
        mockGame = mock(Game.class);

        // Configure getHazards(), getBoosts(), and getPlayers() to return the real lists
        when(mockGame.getHazards()).thenReturn(realHazards);
        when(mockGame.getBoosts()).thenReturn(realBoosts);
        when(mockGame.getPlayers()).thenReturn(realPlayers);

        // Configure addHazard() to add to the realHazards list
        doAnswer(invocation -> {
            Hazard hazard = invocation.getArgument(0);
            realHazards.add(hazard);
            return null;
        }).when(mockGame).addHazard(any(Hazard.class));

        // Configure addBoost() to add to the realBoosts list
        doAnswer(invocation -> {
            Boost boost = invocation.getArgument(0);
            realBoosts.add(boost);
            return null;
        }).when(mockGame).addBoost(any(Boost.class));

        // Configure addPlayer() if needed
        doAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            realPlayers.add(player);
            return null;
        }).when(mockGame).addPlayer(any(Player.class));

        // Reset Config to default values to ensure test isolation
        Config.HAZARD_RATE_PER_SECOND = 5.0f; // Default value
        Config.BOOST_RATE_PER_SECOND = 3.0f;  // Default value
        Config.SIMULATION_STEP_SIZE = 0.1f;   // Default step size (e.g., 100ms)
        Config.MAX_HAZARDS = 50;
        Config.MAX_BOOSTS = 50;
        Config.Y_MAX = (int) 100.0f; // Assuming Y_MAX is a float
        Config.X_MAX = 100;
        Config.HAZARD_DAMAGE = 10;

        // Reset ID counters for Hazard and Boost to ensure consistent IDs across tests
        Hazard.resetIdCounter();
        Boost.resetIdCounter();

        // Initialize Simulator with the mocked GameEngine and Logger
        simulator = new Simulator(mockGameEngine, mockLogger);
    }

    /**
     * Test that the Simulator generates Hazards at the correct rate.
     */
    @Test
    public void testHazardGenerationRate() {
        // Arrange
        Config.HAZARD_RATE_PER_SECOND = 2.0f; // 2 hazards per second
        Config.SIMULATION_STEP_SIZE = 0.5f;    // Each step is 0.5 seconds

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput)); // First step
        simulator.simulateStep(mockGame, List.of(mockInput)); // Second step

        // Assert
        // Expected: 2 hazards generated (2 steps * 2 hazards/sec * 0.5 sec/step)
        assertEquals(2, realHazards.size(), "Expected 2 hazards to be generated.");
    }

    /**
     * Test that the Simulator generates Boosts at the correct rate.
     */
    @Test
    public void testBoostGenerationRate() {
        // Arrange
        Config.BOOST_RATE_PER_SECOND = 1.0f; // 1 boost per second
        Config.SIMULATION_STEP_SIZE = 1.0f;   // Each step is 1 second

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput)); // First step
        simulator.simulateStep(mockGame, List.of(mockInput)); // Second step

        // Assert
        // Expected: 2 boosts generated (2 steps * 1 boost/sec * 1 sec/step)
        assertEquals(2, realBoosts.size(), "Expected 2 boosts to be generated.");
    }

    /**
     * Test that the Simulator updates positions of Hazards and Boosts correctly.
     */
    @Test
    public void testUpdatePositions() {
        // Arrange
        Config.SIMULATION_STEP_SIZE = 1.0f; // Each step is 1 second

        // Add one Hazard and one Boost at y=0.0f
        Hazard hazard = new Hazard(0, 0.0f, Config.HAZARD_DAMAGE);
        Boost boost = new Boost(0, 0); // y as float

        realHazards.add(hazard);
        realBoosts.add(boost);

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput)); // First step

        // Assert
        assertEquals(1.0f, hazard.getY(), 0.001, "Hazard Y position should be updated by 1.0f.");
        assertEquals(1.0f, boost.getY(), 0.001, "Boost Y position should be updated by 1.0f.");
    }

    /**
     * Test that inactive Hazards are removed from the game.
     */
    @Test
    public void testRemoveInactiveHazards() {
        // Arrange
        Hazard activeHazard = new Hazard(0, 0.0f, Config.HAZARD_DAMAGE);
        Hazard inactiveHazard = new Hazard(1, Config.Y_MAX, Config.HAZARD_DAMAGE);
        inactiveHazard.destroy(); // Make it inactive

        realHazards.add(activeHazard);
        realHazards.add(inactiveHazard);

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        assertEquals(1, realHazards.size(), "Inactive hazards should be removed.");
        assertTrue(realHazards.contains(activeHazard), "Active hazards should remain.");
        assertFalse(realHazards.contains(inactiveHazard), "Inactive hazards should be removed.");
    }

    /**
     * Test that inactive Boosts are removed from the game.
     */
    @Test
    public void testRemoveInactiveBoosts() {
        // Arrange
        Boost activeBoost = new Boost(0, 0);
        Boost inactiveBoost = new Boost(1, Config.Y_MAX);
        inactiveBoost.deactivate(); // Make it inactive

        realBoosts.add(activeBoost);
        realBoosts.add(inactiveBoost);

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        assertEquals(1, realBoosts.size(), "Inactive boosts should be removed.");
        assertTrue(realBoosts.contains(activeBoost), "Active boosts should remain.");
        assertFalse(realBoosts.contains(inactiveBoost), "Inactive boosts should be removed.");
    }

    /**
     * Test that disqualified players are removed from the game.
     */
    @Test
    public void testRemoveDisqualifiedPlayers() {
        // Arrange
        Player activePlayer = mock(Player.class);
        when(activePlayer.isDisqualified()).thenReturn(false);

        Player disqualifiedPlayer = mock(Player.class);
        when(disqualifiedPlayer.isDisqualified()).thenReturn(true);

        realPlayers.add(activePlayer);
        realPlayers.add(disqualifiedPlayer);

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        assertEquals(1, realPlayers.size(), "Disqualified players should be removed.");
        assertTrue(realPlayers.contains(activePlayer), "Active players should remain.");
        assertFalse(realPlayers.contains(disqualifiedPlayer), "Disqualified players should be removed.");
    }

    /**
     * Test that GameEngine.updateGameState is called with correct parameters.
     */
    @Test
    public void testUpdateGameStateCalled() {
        // Arrange
        // No specific arrangement needed as we are verifying the method call

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        verify(mockGameEngine, times(1)).updateGameState(mockGame, List.of(mockInput));
    }

    /**
     * Test that maximum number of Hazards is respected.
     */
    @Test
    public void testMaxHazardGeneration() {
        // Arrange
        Config.HAZARD_RATE_PER_SECOND = 10.0f; // High rate
        Config.SIMULATION_STEP_SIZE = 1.0f;    // Each step is 1 second
        Config.MAX_HAZARDS = 5;

        // Pre-fill with MAX_HAZARDS
        for (int i = 0; i < Config.MAX_HAZARDS; i++) {
            realHazards.add(new Hazard(i, 0.0f, Config.HAZARD_DAMAGE));
        }

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        // No new hazards should be added
        assertEquals(Config.MAX_HAZARDS, realHazards.size(), "Should not exceed maximum number of hazards.");
    }

    /**
     * Test that maximum number of Boosts is respected.
     */
    @Test
    public void testMaxBoostGeneration() {
        // Arrange
        Config.BOOST_RATE_PER_SECOND = 10.0f; // High rate
        Config.SIMULATION_STEP_SIZE = 1.0f;    // Each step is 1 second
        Config.MAX_BOOSTS = 5;

        // Pre-fill with MAX_BOOSTS
        for (int i = 0; i < Config.MAX_BOOSTS; i++) {
            realBoosts.add(new Boost(i, 0));
        }

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        // No new boosts should be added
        assertEquals(Config.MAX_BOOSTS, realBoosts.size(), "Should not exceed maximum number of boosts.");
    }

    /**
     * Test that multiple Hazards and Boosts are generated correctly over multiple steps.
     */
    @Test
    public void testMultipleGenerationsOverSteps() {
        // Arrange
        Config.HAZARD_RATE_PER_SECOND = 3.0f; // 3 hazards per second
        Config.BOOST_RATE_PER_SECOND = 2.0f;  // 2 boosts per second
        Config.SIMULATION_STEP_SIZE = 0.5f;    // Each step is 0.5 seconds

        // Act
        // Simulate 2 steps (1 second total)
        simulator.simulateStep(mockGame, List.of(mockInput)); // First step
        simulator.simulateStep(mockGame, List.of(mockInput)); // Second step

        // Assert
        // Expected: 3 hazards and 2 boosts
        // Explanation:
        // - Hazards: 3 per second * 1 second = 3 hazards
        // - Boosts: 2 per second * 1 second = 2 boosts
        assertEquals(3, realHazards.size(), "Should generate 3 hazards.");
        assertEquals(2, realBoosts.size(), "Should generate 2 boosts.");
    }

    /**
     * Test that Simulator correctly logs information.
     * This test assumes that the Logger is injected and can be mocked.
     */
    @Test
    public void testLogging() {
        // Arrange
        // For this test, we can verify that logInfo is called when a hazard or boost is generated or moved
        Config.HAZARD_RATE_PER_SECOND = 1.0f; // 1 hazard per second
        Config.BOOST_RATE_PER_SECOND = 1.0f;  // 1 boost per second
        Config.SIMULATION_STEP_SIZE = 1.0f;    // Each step is 1 second

        // Act
        simulator.simulateStep(mockGame, List.of(mockInput));

        // Assert
        // Verify that logger.logInfo is called for hazard generation
        verify(mockLogger, atLeastOnce()).logInfo(contains("Generated new Hazard"));

        // Verify that logger.logInfo is called for boost generation
        verify(mockLogger, atLeastOnce()).logInfo(contains("Generated new Boost"));

        // Additionally, verify that logger.logInfo is called for hazard and boost movement
        verify(mockLogger, atLeastOnce()).logInfo(contains("moved to y="));
    }
}
