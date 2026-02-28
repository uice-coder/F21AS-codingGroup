package coffeeshop.model;

import coffeeshop.exception.InvalidDataException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the global menu collection.
 * Loads menu data from CSV and stores items in a HashMap for O(1) lookup by itemId.
 *
 * CSV format: itemId,name,description,price,category
 * Example:    BEV-001,Espresso,Strong black coffee,2.50,BEV
 */
public class Menu {

    /** Key: itemId (e.g. "BEV-001"), Value: Item object */
    private final Map<String, Item> items;

    public Menu() {
        this.items = new HashMap<>();
    }

    /**
     * Loads menu items from a CSV file.
     * Invalid lines are skipped and logged to System.err.
     *
     * @param filePath path to the CSV file
     * @throws IOException if the file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // skip comments/blanks

                String[] parts = line.split(",", -1);
                if (parts.length < 5) {
                    System.err.println("[Menu] Line " + lineNumber + ": not enough fields, skipping: " + line);
                    continue;
                }

                String itemId    = parts[0].trim();
                String name      = parts[1].trim();
                String desc      = parts[2].trim();
                String priceStr  = parts[3].trim();
                String category  = parts[4].trim();

                try {
                    double price = Double.parseDouble(priceStr);
                    Item item = new Item(itemId, name, desc, price, category);
                    items.put(itemId, item);
                } catch (NumberFormatException e) {
                    System.err.println("[Menu] Line " + lineNumber + ": invalid price '" + priceStr + "', skipping.");
                } catch (InvalidDataException e) {
                    System.err.println("[Menu] Line " + lineNumber + ": " + e.getMessage() + ", skipping.");
                }
            }
        }
        System.out.println("[Menu] Loaded " + items.size() + " menu items from " + filePath);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param itemId the item identifier
     * @return the Item, or null if not found
     */
    public Item getItem(String itemId) {
        return items.get(itemId);
    }

    /**
     * Returns all menu items.
     */
    public Collection<Item> getAllItems() {
        return items.values();
    }

    /**
     * Returns items belonging to a specific category.
     *
     * @param category category prefix, e.g. "BEV"
     */
    public List<Item> getItemsByCategory(String category) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getCategory().equalsIgnoreCase(category)) {
                result.add(item);
            }
        }
        return result;
    }

    public int size() {
        return items.size();
    }

    public boolean containsItem(String itemId) {
        return items.containsKey(itemId);
    }
}
