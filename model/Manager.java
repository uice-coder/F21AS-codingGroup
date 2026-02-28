package coffeeshop.model;

import coffeeshop.exception.InvalidDataException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Core business layer.
 * - Loads orders from CSV
 * - Applies discount rules to a list of items
 * - Calculates totals
 * - Generates the summary report
 *
 * Discount rules (mutually exclusive; the better one is applied):
 *   Rule 1: 1 Beverage + 1 Food item  → 8% off
 *   Rule 2: 1 Beverage + 2 Food items → 15% off
 *   Rule 3: Pre-discount total > £50  → £5 off
 */
public class Manager {

    private final Menu menu;

    /** All orders loaded from the CSV file */
    private final List<Order> orders;

    /** Customer IDs seen (used to prevent duplicate processing in report) */
    private final Set<String> customerIds;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Manager(Menu menu) {
        this.menu        = menu;
        this.orders      = new ArrayList<>();
        this.customerIds = new HashSet<>();
    }

    // ------------------------------------------------------------------ //
    //  File loading                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Loads orders from a CSV file.
     * CSV format: timestamp,customerId,itemId
     * Example:    2025-01-15 09:30:00,CUST-001,BEV-001
     *
     * Invalid lines are skipped and logged.
     *
     * @param filePath path to the orders CSV file
     * @throws IOException if the file cannot be read
     */
    public void loadOrdersFromCSV(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    System.err.println("[Manager] Line " + lineNumber + ": not enough fields, skipping: " + line);
                    continue;
                }

                String timestampStr = parts[0].trim();
                String customerId   = parts[1].trim();
                String itemId       = parts[2].trim();

                // Validate timestamp
                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    System.err.println("[Manager] Line " + lineNumber + ": invalid timestamp '" + timestampStr + "', skipping.");
                    continue;
                }

                // Validate customerId format (e.g. CUST-001)
                if (!customerId.matches("^CUST-[0-9]{3}$")) {
                    System.err.println("[Manager] Line " + lineNumber + ": invalid customer ID '" + customerId + "', skipping.");
                    continue;
                }

                // Validate item exists in menu
                Item item = menu.getItem(itemId);
                if (item == null) {
                    System.err.println("[Manager] Line " + lineNumber + ": unknown item ID '" + itemId + "', skipping.");
                    continue;
                }

                Order order = new Order(timestamp, customerId, item);
                orders.add(order);
                customerIds.add(customerId);
                item.incrementOrderCount();
            }
        }
        System.out.println("[Manager] Loaded " + orders.size() + " orders from " + filePath);
    }

    // ------------------------------------------------------------------ //
    //  Discount logic                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Calculates the discount amount for a list of items.
     * The most favourable of the three rules is applied (not cumulative).
     *
     * @param items list of items in the order
     * @return discount amount in GBP (0 if no rule matches)
     */
    public double calculateDiscount(List<Item> items) {
        if (items == null || items.isEmpty()) return 0.0;

        double total       = calculateSubtotal(items);
        int    bevCount    = 0;
        int    foodCount   = 0;

        for (Item item : items) {
            if ("BEV".equalsIgnoreCase(item.getCategory())) bevCount++;
            else if ("FOO".equalsIgnoreCase(item.getCategory())) foodCount++;
        }

        double discount1 = 0.0; // 8%  - 1 bev + 1 food
        double discount2 = 0.0; // 15% - 1 bev + 2 food
        double discount3 = 0.0; // £5  - total > £50

        if (bevCount >= 1 && foodCount >= 1) discount1 = total * 0.08;
        if (bevCount >= 1 && foodCount >= 2) discount2 = total * 0.15;
        if (total > 50.0)                    discount3 = 5.0;

        // Apply best discount only
        return Math.max(discount1, Math.max(discount2, discount3));
    }

    /**
     * Calculates the subtotal (before discount) for a list of items.
     *
     * @param items list of items
     * @return sum of item prices
     */
    public double calculateSubtotal(List<Item> items) {
        if (items == null) return 0.0;
        double total = 0.0;
        for (Item item : items) total += item.getPrice();
        return total;
    }

    /**
     * Calculates the final payable amount (after discount).
     *
     * @param items list of items in the order
     * @return subtotal minus discount
     */
    public double calculateFinalTotal(List<Item> items) {
        double subtotal  = calculateSubtotal(items);
        double discount  = calculateDiscount(items);
        return subtotal - discount;
    }

    // ------------------------------------------------------------------ //
    //  Report                                                               //
    // ------------------------------------------------------------------ //

    /**
     * Generates a summary report as a String.
     * Lists all menu items, their order counts, and the overall total revenue.
     *
     * @return formatted report string
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          COFFEE SHOP SALES REPORT      \n");
        sb.append("========================================\n\n");

        sb.append(String.format("%-10s %-25s %-8s %-10s%n",
                "Item ID", "Name", "Price", "Orders"));
        sb.append("-".repeat(58)).append("\n");

        double totalRevenue = 0.0;
        List<Item> allItems = new ArrayList<>(menu.getAllItems());
        allItems.sort(Comparator.comparing(Item::getItemId));

        for (Item item : allItems) {
            double revenue = item.getPrice() * item.getOrderCount();
            totalRevenue  += revenue;
            sb.append(String.format("%-10s %-25s £%-7.2f %-10d%n",
                    item.getItemId(), item.getName(), item.getPrice(), item.getOrderCount()));
        }

        sb.append("-".repeat(58)).append("\n");
        sb.append(String.format("Total orders: %d%n", orders.size()));
        sb.append(String.format("Total revenue: £%.2f%n", totalRevenue));
        sb.append("========================================\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                            //
    // ------------------------------------------------------------------ //

    public List<Order>    getOrders()     { return Collections.unmodifiableList(orders); }
    public Set<String>    getCustomerIds(){ return Collections.unmodifiableSet(customerIds); }
    public Menu           getMenu()       { return menu; }
}
