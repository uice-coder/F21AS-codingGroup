package model;

import exception.InvalidDataException;
import util.Validator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core business layer. Loads orders from CSV, applies discounts,
 * tracks revenue, and generates the sales report.
 */
public class Manager {

    private final Menu        menu;
    private final List<Order> orders;
    private final Set<String> customerIds;

    public Manager(Menu menu) {
        this.menu        = menu;
        this.orders      = new ArrayList<>();
        this.customerIds = new HashSet<>();
    }

    /**
     * Loads orders from CSV. Rows with the same orderId are grouped into one Order.
     * Columns: orderId, customerId, itemId, timestamp
     */
    public void loadOrdersFromCSV(String filePath) throws IOException {
        // LinkedHashMap preserves the order orders were first seen
        Map<String, Order> orderMap = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);

                if (Validator.isHeaderLine(parts, "orderId")) continue;

                if (parts.length < 4) {
                    logSkip(lineNumber, "not enough fields (expected 4)", line);
                    continue;
                }

                String orderId      = parts[0].trim();
                String customerId   = parts[1].trim();
                String itemId       = parts[2].trim();
                String timestampStr = parts[3].trim();

                if (orderId.isEmpty()) {
                    logSkip(lineNumber, "orderId is empty", line);
                    continue;
                }

                try {
                    Validator.validateCustomerId(customerId);
                } catch (InvalidDataException e) {
                    logSkip(lineNumber, e.getMessage(), line);
                    continue;
                }

                LocalDateTime timestamp;
                try {
                    timestamp = Validator.parseTimestamp(timestampStr);
                } catch (InvalidDataException e) {
                    logSkip(lineNumber, e.getMessage(), line);
                    continue;
                }

                Item item = menu.getItem(itemId);
                if (item == null) {
                    logSkip(lineNumber, "unknown item ID '" + itemId + "' (not in menu)", line);
                    continue;
                }

                Order order = orderMap.computeIfAbsent(
                        orderId,
                        k -> new Order(orderId, customerId, timestamp));

                order.addItem(item);
                item.incrementOrderCount();
            }
        }

        for (Order order : orderMap.values()) {
            applyDiscount(order);
            orders.add(order);
            customerIds.add(order.getCustomerId());
        }

        System.out.println("[Manager] Loaded " + orders.size()
                + " order(s) from " + filePath);
    }

    private static void logSkip(int lineNum, String reason, String line) {
        System.err.println("[Manager] Line " + lineNum + ": "
                + reason + " - skipping: " + line);
    }

    public void applyDiscount(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        order.calculateFinalPrice();
    }

    // Used for live basket preview in the GUI — delegates to Order.computeDiscount()
    public double calculateDiscount(List<Item> items) {
        if (items == null || items.isEmpty()) return 0.0;

        double subtotal  = 0.0;
        int    bevCount  = 0;
        int    foodCount = 0;

        for (Item item : items) {
            subtotal += item.getPrice();
            String cat = item.getCategory();
            if ("Beverage".equalsIgnoreCase(cat)) bevCount++;
            else if ("Food".equalsIgnoreCase(cat)) foodCount++;
        }

        return Order.computeDiscount(subtotal, bevCount, foodCount);
    }

    public double calculateSubtotal(List<Item> items) {
        if (items == null) return 0.0;
        double total = 0.0;
        for (Item item : items) total += item.getPrice();
        return total;
    }

    public double calculateFinalTotal(List<Item> items) {
        return calculateSubtotal(items) - calculateDiscount(items);
    }

    public double calculateTotalSales() {
        double total = 0.0;
        for (Order order : orders) total += order.getFinalPrice();
        return total;
    }

    public String generateSalesReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("       COFFEE SHOP - SALES REPORT       \n");
        sb.append("========================================\n\n");

        sb.append(String.format("%-10s %-26s %8s %8s%n",
                "Item ID", "Name", "Price", "Sold"));
        sb.append("------------------------------------------------------------\n");

        List<Item> allItems = new ArrayList<>(menu.getAllItems());
        allItems.sort(Comparator.comparing(Item::getItemId));

        for (Item item : allItems) {
            sb.append(String.format("%-10s %-26s %7s %8d%n",
                    item.getItemId(),
                    item.getName(),
                    String.format("£%.2f", item.getPrice()),
                    item.getOrderCount()));
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("Total orders:  %d%n", orders.size()));
        sb.append(String.format("Total revenue: £%.2f%n", calculateTotalSales()));
        sb.append("========================================\n");
        return sb.toString();
    }

    // Called by GUI after the order is built
    public void addOrderFromGUI(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        orders.add(order);
        customerIds.add(order.getCustomerId());
    }

    public List<Order> getOrders()      { return Collections.unmodifiableList(orders); }
    public Set<String> getCustomerIds() { return Collections.unmodifiableSet(customerIds); }
    public Menu        getMenu()        { return menu; }
}
