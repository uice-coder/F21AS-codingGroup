package coffeeshop.model;

import coffeeshop.exception.InvalidDataException;
import coffeeshop.util.Validator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core business layer for the Coffee Shop application.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load orders from CSV and group them into {@link Order} objects</li>
 *   <li>Apply discount rules via {@link #applyDiscount(Order)}</li>
 *   <li>Calculate total sales revenue</li>
 *   <li>Generate a plain-text sales report</li>
 *   <li>Provide data accessors for {@link coffeeshop.gui.ShopGUI}</li>
 * </ul>
 *
 * <h3>Discount rules (mutually exclusive – best one wins):</h3>
 * <ol>
 *   <li>Combo: &ge;1 Beverage AND &ge;2 Food &rarr; total &times; 0.8 (20&nbsp;% off)</li>
 *   <li>Threshold: total &gt; &pound;50 &rarr; total &minus; &pound;5</li>
 * </ol>
 */
public class Manager {

    private final Menu         menu;

    /** All fully processed orders (discount already applied). */
    private final List<Order>  orders;

    /** All unique customer IDs seen across loaded orders. */
    private final Set<String>  customerIds;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    public Manager(Menu menu) {
        this.menu        = menu;
        this.orders      = new ArrayList<>();
        this.customerIds = new HashSet<>();
    }

    // ------------------------------------------------------------------ //
    //  CSV loading                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Loads orders from a CSV file, grouping rows that share the same
     * {@code orderId} into a single {@link Order} with multiple items.
     *
     * <p>CSV column order: orderId, customerId, itemId, timestamp<br>
     * Example: {@code ORD-001,CUST-001,COF-001,2025-01-15 09:00:00}</p>
     *
     * <p>Lines starting with {@code #} are treated as comments and skipped.
     * A header row whose first field is {@code "orderId"} is also skipped.
     * Invalid rows are logged to {@code System.err}; loading continues.</p>
     *
     * <p>After all rows are read, {@link #applyDiscount(Order)} is called on
     * every assembled Order before it is stored.</p>
     *
     * @param filePath path to the orders CSV file
     * @throws IOException if the file cannot be opened or read
     */
    public void loadOrdersFromCSV(String filePath) throws IOException {
        // Use LinkedHashMap to preserve insertion order of orders
        Map<String, Order> orderMap = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip blank lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);

                // Skip header row
                if (Validator.isHeaderLine(parts, "orderId")) continue;

                if (parts.length < 4) {
                    logSkip(lineNumber, "not enough fields (expected 4)", line);
                    continue;
                }

                String orderId      = parts[0].trim();
                String customerId   = parts[1].trim();
                String itemId       = parts[2].trim();
                String timestampStr = parts[3].trim();

                // Validate orderId
                if (orderId.isEmpty()) {
                    logSkip(lineNumber, "orderId is empty", line);
                    continue;
                }

                // Validate customerId
                try {
                    Validator.validateCustomerId(customerId);
                } catch (InvalidDataException e) {
                    logSkip(lineNumber, e.getMessage(), line);
                    continue;
                }

                // Validate and parse timestamp
                LocalDateTime timestamp;
                try {
                    timestamp = Validator.parseTimestamp(timestampStr);
                } catch (InvalidDataException e) {
                    logSkip(lineNumber, e.getMessage(), line);
                    continue;
                }

                // Validate item exists in menu
                Item item = menu.getItem(itemId);
                if (item == null) {
                    logSkip(lineNumber, "unknown item ID '" + itemId + "' (not in menu)", line);
                    continue;
                }

                // Get or create the Order for this orderId
                Order order = orderMap.computeIfAbsent(
                        orderId,
                        k -> new Order(orderId, customerId, timestamp));

                order.addItem(item);
                item.incrementOrderCount();
            }
        }

        // Apply discount and register every assembled Order
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

    // ------------------------------------------------------------------ //
    //  Discount                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Applies discount rules to the given order by calling
     * {@link Order#calculateFinalPrice()}.
     *
     * @param order the order to process (must not be null)
     */
    public void applyDiscount(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        order.calculateFinalPrice();
    }

    /**
     * Calculates the discount amount for an arbitrary list of items.
     *
     * <p>Uses the same two-rule logic as {@link Order#calculateFinalPrice()}.
     * Intended for live basket previews in the GUI.</p>
     *
     * @param items list of items to evaluate
     * @return discount amount in GBP (0 if no rule applies)
     */
    public double calculateDiscount(List<Item> items) {
        if (items == null || items.isEmpty()) return 0.0;

        double subtotal  = calculateSubtotal(items);
        int    bevCount  = 0;
        int    foodCount = 0;

        for (Item item : items) {
            String cat = item.getCategory();
            if ("Beverage".equalsIgnoreCase(cat)) bevCount++;
            else if ("Food".equalsIgnoreCase(cat)) foodCount++;
        }

        double comboDiscount     = (bevCount >= 1 && foodCount >= 2) ? subtotal * 0.20 : 0.0;
        double thresholdDiscount = (subtotal > 50.0)                 ? 5.0             : 0.0;

        return Math.max(comboDiscount, thresholdDiscount);
    }

    /**
     * Returns the sum of all item prices in the list (before discount).
     *
     * @param items list of items
     * @return subtotal in GBP
     */
    public double calculateSubtotal(List<Item> items) {
        if (items == null) return 0.0;
        double total = 0.0;
        for (Item item : items) total += item.getPrice();
        return total;
    }

    /**
     * Returns the final payable amount for an arbitrary list of items.
     *
     * @param items list of items
     * @return subtotal minus best discount
     */
    public double calculateFinalTotal(List<Item> items) {
        return calculateSubtotal(items) - calculateDiscount(items);
    }

    // ------------------------------------------------------------------ //
    //  Sales analytics                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Calculates the total revenue from all stored orders.
     *
     * <p>Revenue is based on {@link Order#getFinalPrice()} (after discount),
     * reflecting actual money received.</p>
     *
     * @return total sales revenue in GBP
     */
    public double calculateTotalSales() {
        double total = 0.0;
        for (Order order : orders) total += order.getFinalPrice();
        return total;
    }

    // ------------------------------------------------------------------ //
    //  Report                                                               //
    // ------------------------------------------------------------------ //

    /**
     * Generates a plain-text sales report.
     *
     * <p>The report includes per-item sales counts and a revenue summary.</p>
     *
     * @return formatted report as a String
     */
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

    // ------------------------------------------------------------------ //
    //  Accessors for GUI                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Accepts a fully built order from the GUI (discount already applied).
     * Adds the order to the internal list and records the customer ID.
     *
     * @param order the completed order (must not be null)
     */
    public void addOrderFromGUI(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        orders.add(order);
        customerIds.add(order.getCustomerId());
    }

    public List<Order>  getOrders()      { return Collections.unmodifiableList(orders); }
    public Set<String>  getCustomerIds() { return Collections.unmodifiableSet(customerIds); }
    public Menu         getMenu()        { return menu; }
}
