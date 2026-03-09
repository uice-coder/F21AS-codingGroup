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

// CSV format: itemId,name,description,price,category
public class Menu {

    private final Map<String, Item> items;

    public Menu() {
        this.items = new HashMap<>();
    }

    public void loadFromCSV(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);

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

                try {
                    Validator.validateItemId(itemId);
                } catch (InvalidDataException e) {
                    logSkip("Menu", lineNumber, e.getMessage(), line);
                    continue;
                }

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

    public Item getItem(String itemId) {
        return items.get(itemId);
    }

    public Collection<Item> getAllItems() {
        return items.values();
    }

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
