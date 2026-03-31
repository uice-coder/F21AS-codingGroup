package model;

import exception.InvalidDataException;
import util.Validator;

// Single menu item. itemId is immutable and used as the map key in Menu.
public class Item {

    private final String itemId;
    private String name;
    private String description;
    private double price;
    private String category;
    private int    orderCount;

    public Item(String itemId, String name, String description,
                double price, String category) throws InvalidDataException {

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
        this.category    = category.trim();
        this.orderCount  = 0;
    }

    public String getItemId()      { return itemId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public String getCategory()    { return category; }
    public int    getOrderCount()  { return orderCount; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = name.trim();
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    public void setPrice(double price) {
        if (price <= 0)
            throw new IllegalArgumentException("Price must be > 0, got: " + price);
        this.price = price;
    }

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

    public void incrementOrderCount() { orderCount++; }

    @Override
    public String toString() {
        return String.format("%s - %s (£%.2f) [%s]", itemId, name, price, category);
    }
}
