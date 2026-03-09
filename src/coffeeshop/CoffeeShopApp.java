package coffeeshop;

import gui.ShopGUI;
import model.Manager;
import model.Menu;

import javax.swing.*;
import java.io.IOException;

// Usage: java -cp bin coffeeshop.CoffeeShopApp [menuCsv] [ordersCsv]
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
            System.err.println("ERROR: Could not load menu file: " + menuPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (menu.size() == 0) {
            System.err.println("WARNING: No valid menu items were loaded from " + menuPath);
        }

        Manager manager = new Manager(menu);
        try {
            manager.loadOrdersFromCSV(ordersPath);
        } catch (IOException e) {
            System.err.println("WARNING: Could not load orders file: " + ordersPath);
            System.err.println("Continuing without pre-loaded orders.");
        }

        SwingUtilities.invokeLater(() -> new ShopGUI(manager));
    }
}
