package coffeeshop;

import gui.SimulationModel;
import gui.SimulationView;
import model.Manager;
import model.Menu;
import simulation.SimulationController;

import javax.swing.*;
import java.io.IOException;

/**
 * Main entry point for the Coffee Shop Simulation – Stage 2.
 *
 * <p>Usage (from project root):</p>
 * <pre>
 *   java -cp bin coffeeshop.Stage2App [menu.csv] [orders.csv] [staffCount] [arrivalIntervalMs]
 * </pre>
 *
 * <p>Default values are used when arguments are omitted:</p>
 * <ul>
 *   <li>menu     : {@code data/menu.csv}</li>
 *   <li>orders   : {@code data/orders.csv}</li>
 *   <li>staff    : 3</li>
 *   <li>interval : 3000 ms between customer arrivals</li>
 * </ul>
 *
 * <h3>Startup sequence</h3>
 * <ol>
 *   <li>Load Menu and Orders from CSV (reuses Stage 1 {@link Manager}).</li>
 *   <li>Create {@link SimulationModel} (MVC Model + Observer hub).</li>
 *   <li>Create {@link SimulationController} and wire observers.</li>
 *   <li>Launch {@link SimulationView} on the Swing EDT.</li>
 *   <li>Start the simulation (threads begin running).</li>
 * </ol>
 */
public class Stage2App {

    private static final String DEFAULT_MENU_PATH   = "data/menu.csv";
    private static final String DEFAULT_ORDERS_PATH = "data/orders.csv";

    public static void main(String[] args) {
        // ---- Parse arguments ---- //
        String menuPath   = args.length > 0 ? args[0] : DEFAULT_MENU_PATH;
        String ordersPath = args.length > 1 ? args[1] : DEFAULT_ORDERS_PATH;
        int staffCount    = SimulationController.DEFAULT_STAFF_COUNT;
        int intervalMs    = SimulationController.DEFAULT_ARRIVAL_INTERVAL_MS;

        if (args.length > 2) {
            try { staffCount = Integer.parseInt(args[2]); }
            catch (NumberFormatException e) {
                System.err.println("Invalid staff count '" + args[2] + "', using default: " + staffCount);
            }
        }
        if (args.length > 3) {
            try { intervalMs = Integer.parseInt(args[3]); }
            catch (NumberFormatException e) {
                System.err.println("Invalid interval '" + args[3] + "', using default: " + intervalMs + " ms");
            }
        }

        // ---- Load data (Stage 1 classes) ---- //
        Menu menu = new Menu();
        try {
            menu.loadFromCSV(menuPath);
        } catch (IOException e) {
            System.err.println("ERROR: Cannot load menu from: " + menuPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (menu.size() == 0) {
            System.err.println("WARNING: No valid menu items loaded from " + menuPath);
        }

        Manager manager = new Manager(menu);
        try {
            manager.loadOrdersFromCSV(ordersPath);
        } catch (IOException e) {
            System.err.println("ERROR: Cannot load orders from: " + ordersPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (manager.getOrders().isEmpty()) {
            System.err.println("ERROR: No orders to simulate. Exiting.");
            System.exit(1);
        }

        // ---- Build MVC + Observer graph ---- //
        SimulationModel      model      = new SimulationModel();
        SimulationController controller = new SimulationController(manager, staffCount, intervalMs);

        // Wire the model as observer of both queue and all staff threads
        controller.addQueueObserver(model);
        controller.addStaffObserver(model);

        // When simulation ends, notify the model (which forwards to the View)
        controller.setOnSimulationComplete(() ->
                model.onSimulationComplete(manager.generateSalesReport()));

        // ---- Launch GUI on EDT ---- //
        final SimulationController ctrl = controller;
        SwingUtilities.invokeLater(() -> {
            new SimulationView(model, ctrl);
            // Start simulation after the window is visible
            ctrl.start();
        });
    }
}
