package coffeeshop.model;

import coffeeshop.exception.InvalidDataException;

/**
 * Represents a single item on the coffee shop menu.
 * Item IDs must follow the pattern: ^[A-Z]{3}-[0-9]{3}$
 * e.g., BEV-001, FOO-042, OTH-123
 */
public class Item {

    private final String itemId;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private int orderCount;

    /**
     * Constructs an Item. Throws InvalidDataException if itemId is invalid.
     *
     * @param itemId      unique identifier matching pattern [A-Z]{3}-[0-9]{3}
     * @param name        display name of the item
     * @param description short description
     * @param price       cost in GBP (must be > 0)
     * @param category    category name (BEV, FOO, OTH)
     * @throws InvalidDataException if any field is invalid
     */
    public Item(String itemId, String name, String description, double price, String category)
            throws InvalidDataException {

        if (itemId == null || !itemId.matches("^[A-Z]{3}-[0-9]{3}$")) {
            throw new InvalidDataException("Invalid item ID format: '" + itemId
                    + "'. Expected pattern [A-Z]{3}-[0-9]{3}, e.g. BEV-001");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Item name cannot be empty for ID: " + itemId);
        }
        if (price <= 0) {
            throw new InvalidDataException("Item price must be > 0, got: " + price + " for ID: " + itemId);
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidDataException("Category cannot be empty for ID: " + itemId);
        }

        this.itemId = itemId;
        this.name = name.trim();
        this.description = (description != null) ? description.trim() : "";
        this.price = price;
        this.category = category.trim().toUpperCase();
        this.orderCount = 0;
    }

    public String getItemId()      { return itemId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public String getCategory()    { return category; }
    public int    getOrderCount()  { return orderCount; }

    public void incrementOrderCount() { orderCount++; }

    @Override
    public String toString() {
        return String.format("%s - %s (£%.2f) [%s]", itemId, name, price, category);
    }
}
