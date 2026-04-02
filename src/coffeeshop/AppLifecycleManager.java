package coffeeshop;

import gui.ShopGUI;
import gui.SimulationGUI;
import simulation.SimulationController;

import javax.swing.SwingUtilities;

/**
 * Coordinates clean shutdown across both GUI windows and the simulation.
 */
public class AppLifecycleManager {

    private final SimulationController simulationController;
    private volatile boolean shutdownInProgress = false;

    private ShopGUI shopGUI;
    private SimulationGUI simulationGUI;

    public AppLifecycleManager(SimulationController simulationController) {
        this.simulationController = simulationController;
    }

    public synchronized void registerWindows(ShopGUI shopGUI, SimulationGUI simulationGUI) {
        this.shopGUI = shopGUI;
        this.simulationGUI = simulationGUI;
    }

    public boolean isShutdownInProgress() {
        return shutdownInProgress;
    }

    public void shutdownApplication() {
        synchronized (this) {
            if (shutdownInProgress) return;
            shutdownInProgress = true;
        }

        simulationController.shutdownAndAwaitTermination();

        Runnable disposeTask = () -> {
            if (shopGUI != null && shopGUI.isDisplayable()) {
                shopGUI.dispose();
            }
            if (simulationGUI != null && simulationGUI.isDisplayable()) {
                simulationGUI.dispose();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            disposeTask.run();
        } else {
            SwingUtilities.invokeLater(disposeTask);
        }
    }
}
