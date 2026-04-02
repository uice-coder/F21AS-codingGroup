package coffeeshop;

import model.Manager;
import model.Menu;
import model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulation.SimulationController;
import util.ProjectPaths;

import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 2 tests for {@link SimulationController}.
 *
 * These tests cover the fixed-snapshot run model and state transitions
 * without depending on Swing rendering.
 */
public class SimulationControllerTest {

    private Manager manager;
    private SimulationController controller;

    @BeforeEach
    public void setUp() {
        manager = new Manager(new Menu());
        controller = new SimulationController(manager);
    }

    @AfterEach
    public void tearDown() throws Exception {
        controller.shutdownAndAwaitTermination();
        Files.deleteIfExists(ProjectPaths.resolveOutput("simulation_log.txt"));
    }

    @Test
    public void testInitialStateIsReadyAndAcceptingOrders() {
        assertEquals(SimulationController.RunState.READY, controller.getRunState());
        assertFalse(controller.hasSimulationStarted());
        assertFalse(controller.isRunning());
        assertTrue(controller.canAcceptNewOrders());
        assertEquals(0, controller.getRunOrderCount());
    }

    @Test
    public void testStartSimulationCapturesFixedOrderSnapshotAndLocksNewOrders() {
        manager.addOrderFromGUI(new Order("ORD-001", "CUST-001", LocalDateTime.now()));

        controller.startSimulation();

        assertTrue(controller.hasSimulationStarted());
        assertEquals(SimulationController.RunState.RUNNING, controller.getRunState());
        assertFalse(controller.canAcceptNewOrders());
        assertEquals(1, controller.getRunOrderCount());

        manager.addOrderFromGUI(new Order("ORD-002", "CUST-002", LocalDateTime.now()));

        assertEquals(1, controller.getRunOrderCount(),
                "Current run should keep using the original order snapshot");
    }

    @Test
    public void testZeroOrderSimulationCompletesCleanly() throws Exception {
        controller.startSimulation();

        boolean completed = waitUntil(
                () -> controller.getRunState() == SimulationController.RunState.COMPLETED
                        && !controller.isRunning(),
                3000
        );

        assertTrue(completed, "Zero-order simulation should complete quickly");
        assertEquals(0, controller.getRunOrderCount());
        assertFalse(controller.canAcceptNewOrders(),
                "Restart is intentionally unsupported after a run has started");
    }

    private boolean waitUntil(Check check, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (check.evaluate()) return true;
            Thread.sleep(25);
        }
        return check.evaluate();
    }

    @FunctionalInterface
    private interface Check {
        boolean evaluate() throws Exception;
    }
}
