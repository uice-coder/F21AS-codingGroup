package coffeeshop;

import gui.ShopGUI;
import gui.SimulationGUI;
import model.Manager;
import model.Menu;
import simulation.SimulationController;
import util.ProjectPaths;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Main entry point for Stage 2.
 * Launches the original Stage 1 GUI and the new simulation GUI.
 */
public class CoffeeShopApp {

    private static final String DEFAULT_MENU_PATH   = "data/menu.csv";
    private static final String DEFAULT_ORDERS_PATH = "data/orders.csv";

    public static void main(String[] args) {
        Path menuPath = (args.length > 0)
                ? ProjectPaths.resolveExisting(args[0])
                : ProjectPaths.resolveExisting(DEFAULT_MENU_PATH);
        Path ordersPath = (args.length > 1)
                ? ProjectPaths.resolveExisting(args[1])
                : ProjectPaths.resolveExisting(DEFAULT_ORDERS_PATH);

        Menu menu = new Menu();
        try {
            menu.loadFromCSV(menuPath.toString());
        } catch (IOException e) {
            System.err.println("ERROR: Could not load menu: " + menuPath);
            final String message = "Could not load menu file: " + menuPath;
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, message,
                            "Startup Error", JOptionPane.ERROR_MESSAGE));
            return;
        }

        Manager manager = new Manager(menu);
        try {
            manager.loadOrdersFromCSV(ordersPath.toString());
        } catch (IOException e) {
            System.err.println("WARNING: Could not load orders: " + ordersPath);
        }

        SimulationController simController = new SimulationController(manager);
        AppLifecycleManager lifecycleManager = new AppLifecycleManager(simController);

        SwingUtilities.invokeLater(() -> {
            ShopGUI shopGUI = new ShopGUI(manager, simController, lifecycleManager);
            SimulationGUI simulationGUI = new SimulationGUI(simController, lifecycleManager);
            lifecycleManager.registerWindows(shopGUI, simulationGUI);
        });
    }
}
