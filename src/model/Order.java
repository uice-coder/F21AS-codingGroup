package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete customer order (a single basket checkout).
 *
 * <p>One Order may contain multiple {@link Item}s. Discount rules are applied
 * by {@link #calculateFinalPrice()}, which stores the result in {@link #discount}
 * and {@link #finalPrice}.</p>
 *
 * <h3>Discount rules (mutually exclusive – best one wins, no stacking):</h3>
 * <ul>
 *   <li><b>Combo discount:</b> at least 1 Beverage AND at least 2 Food items
 *       &rarr; total &times; 0.8 (20&nbsp;% off)</li>
 *   <li><b>Threshold discount:</b> subtotal &gt; £50
 *       &rarr; subtotal &minus; £5</li>
 * </ul>
 */
public class Order {

    private final String        orderId;
    private final String        customerId;
    private final LocalDateTime timestamp;
    private final List<Item>    items;

    /** Discount amount applied to this order (set by {@link #calculateFinalPrice()}). */
    private double discount;

    /** Final payable price after discount (set by {@link #calculateFinalPrice()}). */
    private double finalPrice;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Creates an empty Order. Use {@link #addItem(Item)} to populate the basket.
     *
     * @param orderId    unique order identifier (e.g. ORD-001)
     * @param customerId customer identifier (must not be blank)
     * @param timestamp  time the order was placed (must not be null)
     * @throws IllegalArgumentException if any argument is null or blank
     */
    public Order(String orderId, String customerId, LocalDateTime timestamp) {
        if (orderId == null || orderId.trim().isEmpty())
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        if (timestamp == null)
            throw new IllegalArgumentException("Timestamp cannot be null");

        this.orderId    = orderId.trim();
        this.customerId = customerId.trim();
        this.timestamp  = timestamp;
        this.items      = new ArrayList<>();
        this.discount   = 0.0;
        this.finalPrice = 0.0;
    }

    // ------------------------------------------------------------------ //
    //  Basket management                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Adds an item to this order.
     *
     * @param item the menu item to add (must not be null)
     */
    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        items.add(item);
    }

    // ------------------------------------------------------------------ //
    //  Discount calculation                                                 //
    // ------------------------------------------------------------------ //

    /**
     * Calculates the subtotal, selects the best applicable discount, and
     * stores both {@link #discount} and {@link #finalPrice}.
     *
     * <p>Call this method once the basket is finalised (before storing the order).</p>
     *
     * @return the final payable price after discount
     */
    public double calculateFinalPrice() {
        double subtotal  = 0.0;
        int    bevCount  = 0;
        int    foodCount = 0;

        for (Item item : items) {
            subtotal += item.getPrice();
            String cat = item.getCategory();
            if ("Beverage".equalsIgnoreCase(cat)) bevCount++;
            else if ("Food".equalsIgnoreCase(cat)) foodCount++;
        }

        // Rule 1 – Combo: 1+ Beverage AND 2+ Food → 20% off (multiply by 0.8)
        double comboDiscount     = (bevCount >= 1 && foodCount >= 2)
                                   ? subtotal * 0.20 : 0.0;

        // Rule 2 – Threshold: subtotal > £50 → £5 off
        double thresholdDiscount = (subtotal > 50.0) ? 5.0 : 0.0;

        // Best discount wins; no stacking
        discount   = Math.max(comboDiscount, thresholdDiscount);
        finalPrice = subtotal - discount;
        return finalPrice;
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                              //
    // ------------------------------------------------------------------ //

    public String        getOrderId()    { return orderId; }
    public String        getCustomerId() { return customerId; }
    public LocalDateTime getTimestamp()  { return timestamp; }

    /** Returns an unmodifiable view of the item list. */
    public List<Item>    getItems()      { return Collections.unmodifiableList(items); }

    /** Discount amount (populated after {@link #calculateFinalPrice()} is called). */
    public double        getDiscount()   { return discount; }

    /** Final price (populated after {@link #calculateFinalPrice()} is called). */
    public double        getFinalPrice() { return finalPrice; }

    /** Convenience: sum of all item prices before any discount. */
    public double getSubtotal() {
        double total = 0.0;
        for (Item item : items) total += item.getPrice();
        return total;
    }

    @Override
    public String toString() {
        return String.format("[%s] Customer %s – %d item(s) – Discount: £%.2f – Final: £%.2f",
                orderId, customerId, items.size(), discount, finalPrice);
    }
}
