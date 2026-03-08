package model;

import java.time.LocalDateTime;

/**
 * Represents a single transaction (1 item per order).
 * Grouping and discount logic are handled by the Manager class.
 */
public class Order {
    private final String        customerId;
    private final LocalDateTime timestamp;
    private final Item          item; // Only ONE item per order row

    // These will be calculated and set by the Manager class
    private double discount;
    private double finalPrice;

    /**
     * Constructs a single item order.
     */
    public Order(String customerId, LocalDateTime timestamp, Item item) {
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("Customer ID cannot be empty");
        if (timestamp == null || item == null)
            throw new IllegalArgumentException("Timestamp and Item cannot be null");

        this.customerId = customerId.trim();
        this.timestamp  = timestamp;
        this.item       = item;
        this.discount   = 0.0;
        this.finalPrice = item.getPrice(); // Default to original price before Manager applies discount
    }

    // --- Getters ---
    public String getCustomerId() { return customerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Item getItem() { return item; }
    public double getDiscount() { return discount; }
    public double getFinalPrice() { return finalPrice; }

    // --- Setters (To be used by Manager during calculation) ---
    public void setDiscount(double discount) { 
        this.discount = discount; 
    }
    
    public void setFinalPrice(double finalPrice) { 
        this.finalPrice = finalPrice; 
    }

    @Override
    public String toString() {
        return String.format("Customer %s ordered %s – Discount: £%.2f – Final: £%.2f",
                customerId, item.getItemId(), discount, finalPrice);
    }
}