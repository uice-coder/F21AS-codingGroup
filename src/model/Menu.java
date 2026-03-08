package model;

import exception.InvalidDataException;

import util.Validator;

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
     *
     * <p>Expected CSV columns (in order): itemId, name, description, price, category</p>
     * <p>Lines that begin with {@code #} are treated as comments and skipped.
     * A header row whose first field is {@code "itemId"} (case-insensitive) is also skipped.
     * Any line that fails validation is logged to {@code System.err} and skipped;
     * loading continues with the remaining lines.</p>
     *
     * @param filePath path to the menu CSV file
     * @throws IOException if the file cannot be opened or read
     */
    public void loadFromCSV(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip blank lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);

                // Skip header row (first field == "itemId")
                if (Validator.isHeaderLine(parts, "itemId")) continue;

                if (parts.length < 5) {
                    logSkip("Menu", lineNumber, "not enough fields", line);
                    continue;
                }

                String itemId   = parts[0].trim();
                String name     = parts[1].trim();
                String desc     = parts[2].trim();
                String priceStr = parts[3].trim();
                String category = parts[4].trim();

                // Validate itemId format before attempting Item construction
                try {
                    Validator.validateItemId(itemId);
                } catch (InvalidDataException e) {
                    logSkip("Menu", lineNumber, e.getMessage(), line);
                    continue;
                }

                // Validate price is a parseable positive number
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                    Validator.validatePrice(price, itemId);
                } catch (NumberFormatException e) {
                    logSkip("Menu", lineNumber,
                            "price is not a valid number: '" + priceStr + "'", line);
                    continue;
                } catch (InvalidDataException e) {
                    logSkip("Menu", lineNumber, e.getMessage(), line);
                    continue;
                }

                // Construct Item (performs remaining field validation)
                try {
                    Item item = new Item(itemId, name, desc, price, category);
                    items.put(itemId, item);
                } catch (InvalidDataException e) {
                    logSkip("Menu", lineNumber, e.getMessage(), line);
                }
            }
        }
        System.out.println("[Menu] Loaded " + items.size() + " menu items from " + filePath);
    }

    private static void logSkip(String source, int lineNum, String reason, String line) {
        System.err.println("[" + source + "] Line " + lineNum + ": " + reason + " — skipping: " + line);
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
