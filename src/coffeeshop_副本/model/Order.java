package coffeeshop.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a single customer order (one item per order line in the CSV).
 * Multiple Order objects with the same customerId belong to the same transaction.
 */
public class Order {

    private final LocalDateTime timestamp;
    private final String customerId;
    private final Item item;

    /**
     * Constructs an Order.
     *
     * @param timestamp  when the order was placed
     * @param customerId unique customer identifier
     * @param item       the menu item ordered
     */
    public Order(LocalDateTime timestamp, String customerId, Item item) {
        if (timestamp == null)   throw new IllegalArgumentException("Timestamp cannot be null");
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        if (item == null)        throw new IllegalArgumentException("Item cannot be null");

        this.timestamp  = timestamp;
        this.customerId = customerId.trim();
        this.item       = item;
    }

    public LocalDateTime getTimestamp()  { return timestamp; }
    public String        getCustomerId() { return customerId; }
    public Item          getItem()       { return item; }

    @Override
    public String toString() {
        return String.format("[%s] Customer %s ordered %s", timestamp, customerId, item.getName());
    }
}
