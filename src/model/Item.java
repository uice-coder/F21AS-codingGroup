package model;

import exception.InvalidDataException;
import util.Validator;

/**
 * Represents a single item available on the coffee shop menu.
 *
 * <p>Item IDs must satisfy {@link Validator#ITEM_ID_PATTERN}: {@code ^[A-Z]+-\d{3}$}
 * Examples: COF-001, FOO-042, FOOD-002, TEA-001</p>
 */
public class Item {

    private final String itemId;   // immutable – used as HashMap key
    private String name;
    private String description;
    private double price;
    private String category;
    private int    orderCount;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Constructs an Item with full validation.
     *
     * @param itemId      unique identifier matching {@link Validator#ITEM_ID_PATTERN}
     * @param name        display name (must not be blank)
     * @param description short description (may be empty)
     * @param price       price in GBP (must be &gt; 0)
     * @param category    category label, e.g. "Beverage", "Food", "Other"
     * @throws InvalidDataException if itemId format is wrong, name is blank, or price &lt;= 0
     */
    public Item(String itemId, String name, String description,
                double price, String category) throws InvalidDataException {

        // Delegate format and range checks to Validator (single source of truth)
        Validator.validateItemId(itemId);
        Validator.validatePrice(price, itemId);

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Item name cannot be blank for ID: " + itemId);
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidDataException("Category cannot be blank for ID: " + itemId);
        }

        this.itemId      = itemId.trim();
        this.name        = name.trim();
        this.description = (description != null) ? description.trim() : "";
        this.price       = price;
        this.category    = category.trim();   // preserve original case ("Beverage", "Food")
        this.orderCount  = 0;
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                              //
    // ------------------------------------------------------------------ //

    public String getItemId()      { return itemId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public String getCategory()    { return category; }
    public int    getOrderCount()  { return orderCount; }

    // ------------------------------------------------------------------ //
    //  Setters                                                              //
    // ------------------------------------------------------------------ //

    /** Updates the display name (must not be blank). */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = name.trim();
    }

    /** Updates the description (null is treated as empty string). */
    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    /**
     * Updates the price.
     * @throws IllegalArgumentException if price &lt;= 0
     */
    public void setPrice(double price) {
        if (price <= 0)
            throw new IllegalArgumentException("Price must be > 0, got: " + price);
        this.price = price;
    }

    /** Updates the category label. */
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Category cannot be blank");
        this.category = category.trim();
    }

    /** Directly sets the order count (useful for testing). */
    public void setOrderCount(int orderCount) {
        if (orderCount < 0)
            throw new IllegalArgumentException("Order count cannot be negative");
        this.orderCount = orderCount;
    }

    /** Increments the order count by 1 each time this item is ordered. */
    public void incrementOrderCount() { orderCount++; }

    // ------------------------------------------------------------------ //
    //  Utility                                                              //
    // ------------------------------------------------------------------ //

    @Override
    public String toString() {
        return String.format("%s - %s (£%.2f) [%s]", itemId, name, price, category);
    }
}
