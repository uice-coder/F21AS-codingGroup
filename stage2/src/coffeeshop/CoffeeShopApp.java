package coffeeshop;

import gui.ShopGUI;
import gui.SimulationGUI;
import model.Manager;
import model.Menu;
import simulation.SimulationController;

import javax.swing.*;
import java.io.IOException;

/**
 * Main entry point for Stage 2.
 * Launches the original Stage 1 GUI and the new simulation GUI.
 */
public class CoffeeShopApp {

    private static final String DEFAULT_MENU_PATH   = "data/menu.csv";
    private static final String DEFAULT_ORDERS_PATH = "data/orders.csv";

    public static void main(String[] args) {
        String menuPath   = (args.length > 0) ? args[0] : DEFAULT_MENU_PATH;
        String ordersPath = (args.length > 1) ? args[1] : DEFAULT_ORDERS_PATH;

        Menu menu = new Menu();
        try {
            menu.loadFromCSV(menuPath);
        } catch (IOException e) {
            System.err.println("ERROR: Could not load menu: " + menuPath);
            System.exit(1);
        }

        Manager manager = new Manager(menu);
        try {
            manager.loadOrdersFromCSV(ordersPath);
        } catch (IOException e) {
            System.err.println("WARNING: Could not load orders: " + ordersPath);
        }

        SimulationController simController = new SimulationController(manager);

        SwingUtilities.invokeLater(() -> {
            // Stage 1: ordering GUI
            new ShopGUI(manager);
            // Stage 2: simulation GUI
            new SimulationGUI(simController);
        });
    }
}
