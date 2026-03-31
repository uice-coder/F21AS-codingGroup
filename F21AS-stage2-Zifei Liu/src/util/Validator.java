package util;

import exception.InvalidDataException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class Validator {

    // e.g. COF-001, FOOD-042, A-001 — one or more uppercase letters, dash, 3 digits
    public static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("^[A-Z]+-\\d{3}$");

    private static final Pattern CUSTOMER_ID_PATTERN =
            Pattern.compile("^CUST-\\d{3}$");

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Validator() {}

    public static void validateItemId(String itemId) throws InvalidDataException {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new InvalidDataException("Item ID cannot be null or empty");
        }
        if (!ITEM_ID_PATTERN.matcher(itemId.trim()).matches()) {
            throw new InvalidDataException(
                    "Invalid item ID format: '" + itemId
                    + "' (expected e.g. COF-001, FOO-042)");
        }
    }

    public static void validatePrice(double price, String itemId)
            throws InvalidDataException {
        if (price <= 0) {
            throw new InvalidDataException(
                    "Price must be > 0 for item '" + itemId
                    + "', got: " + price);
        }
    }

    public static void validateCustomerId(String customerId)
            throws InvalidDataException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidDataException("Customer ID cannot be null or empty");
        }
        if (!CUSTOMER_ID_PATTERN.matcher(customerId.trim()).matches()) {
            throw new InvalidDataException(
                    "Invalid customer ID format: '" + customerId
                    + "' (expected e.g. CUST-001)");
        }
    }

    public static LocalDateTime parseTimestamp(String s)
            throws InvalidDataException {
        if (s == null || s.trim().isEmpty()) {
            throw new InvalidDataException("Timestamp cannot be null or empty");
        }
        try {
            return LocalDateTime.parse(s.trim(), TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException(
                    "Invalid timestamp format: '" + s
                    + "' (expected yyyy-MM-dd HH:mm:ss)", e);
        }
    }

    // Returns true if the first CSV field matches the expected header label.
    // Used to skip the header row when reading CSV files.
    public static boolean isHeaderLine(String[] parts, String firstField) {
        return parts != null
                && parts.length > 0
                && parts[0].trim().equalsIgnoreCase(firstField);
    }
}
