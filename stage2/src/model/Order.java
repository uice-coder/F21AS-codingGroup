package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single customer order. May contain multiple items.
 * Call calculateFinalPrice() once the basket is finalised.
 *
 * Discount rules (best one wins, no stacking):
 *   - Combo: 1+ Beverage AND 2+ Food -> 20% off
 *   - Threshold: subtotal > £50 -> £5 off
 */
public class Order {

    private final String        orderId;
    private final String        customerId;
    private final LocalDateTime timestamp;
    private final List<Item>    items;
    private double discount;
    private double finalPrice;
    private double subtotal;    // cached after calculateFinalPrice()

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
        this.subtotal   = 0.0;
    }

    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        items.add(item);
    }

    public double calculateFinalPrice() {
        subtotal  = 0.0;
        int bevCount  = 0;
        int foodCount = 0;

        for (Item item : items) {
            subtotal += item.getPrice();
            String cat = item.getCategory();
            if ("Beverage".equalsIgnoreCase(cat)) bevCount++;
            else if ("Food".equalsIgnoreCase(cat)) foodCount++;
        }

        discount   = computeDiscount(subtotal, bevCount, foodCount);
        finalPrice = subtotal - discount;
        return finalPrice;
    }

    /**
     * Single source of truth for discount rules.
     * Called by Manager.calculateDiscount() for live basket preview.
     *
     * Rules (best wins, no stacking):
     *   - Combo: 1+ Beverage AND 2+ Food -> 20% off subtotal
     *   - Threshold: subtotal > £50 -> £5 off
     */
    public static double computeDiscount(double subtotal, int bevCount, int foodCount) {
        double comboDiscount     = (bevCount >= 1 && foodCount >= 2) ? subtotal * 0.20 : 0.0;
        double thresholdDiscount = (subtotal > 50.0) ? 5.0 : 0.0;
        return Math.max(comboDiscount, thresholdDiscount);
    }

    public String        getOrderId()    { return orderId; }
    public String        getCustomerId() { return customerId; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public List<Item>    getItems()      { return Collections.unmodifiableList(items); }
    public double        getDiscount()   { return discount; }
    public double        getFinalPrice() { return finalPrice; }

    /** Returns the cached subtotal set by the last call to calculateFinalPrice(). */
    public double getSubtotal() { return subtotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return Objects.equals(orderId, ((Order) o).orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("[%s] Customer %s – %d item(s) – Discount: £%.2f – Final: £%.2f",
                orderId, customerId, items.size(), discount, finalPrice);
    }
}
