package util;

import exception.InvalidDataException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class providing static validation methods and constants used across
 * the model layer.
 *
 * <p>All methods are stateless and operate on their arguments only.</p>
 */
public class Validator {

    /**
     * Regex pattern for valid item IDs: one or more uppercase letters,
     * a dash, then exactly three digits. Examples: COF-001, FOOD-042, A-001.
     */
    public static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("^[A-Z]+-\\d{3}$");

    /** Pattern for valid customer IDs, e.g. CUST-001. */
    private static final Pattern CUSTOMER_ID_PATTERN =
            Pattern.compile("^CUST-\\d{3}$");

    /** Expected timestamp format in CSV files. */
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Private constructor — this class is not meant to be instantiated.
    private Validator() {}

    // ------------------------------------------------------------------ //
    //  Validation methods                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Validates that an item ID is non-null, non-empty, and matches
     * {@link #ITEM_ID_PATTERN}.
     *
     * @param itemId the identifier to validate
     * @throws InvalidDataException if the ID is null, empty, or malformed
     */
    public static void validateItemId(String itemId) throws InvalidDataException {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new InvalidDataException(
                    "Item ID cannot be null or empty");
        }
        if (!ITEM_ID_PATTERN.matcher(itemId.trim()).matches()) {
            throw new InvalidDataException(
                    "Invalid item ID format: '" + itemId
                    + "' (expected e.g. COF-001, FOO-042)");
        }
    }

    /**
     * Validates that a price is greater than zero.
     *
     * @param price  the price value to check
     * @param itemId the associated item ID, used in the error message
     * @throws InvalidDataException if price is zero or negative
     */
    public static void validatePrice(double price, String itemId)
            throws InvalidDataException {
        if (price <= 0) {
            throw new InvalidDataException(
                    "Price must be > 0 for item '" + itemId
                    + "', got: " + price);
        }
    }

    /**
     * Validates that a customer ID matches {@link #CUSTOMER_ID_PATTERN}.
     *
     * @param customerId the customer identifier to validate
     * @throws InvalidDataException if the customer ID is null, empty, or malformed
     */
    public static void validateCustomerId(String customerId)
            throws InvalidDataException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidDataException(
                    "Customer ID cannot be null or empty");
        }
        if (!CUSTOMER_ID_PATTERN.matcher(customerId.trim()).matches()) {
            throw new InvalidDataException(
                    "Invalid customer ID format: '" + customerId
                    + "' (expected e.g. CUST-001)");
        }
    }

    /**
     * Parses a timestamp string in {@code yyyy-MM-dd HH:mm:ss} format.
     *
     * @param s the timestamp string
     * @return the parsed {@link LocalDateTime}
     * @throws InvalidDataException if the string is null, empty, or cannot be parsed
     */
    public static LocalDateTime parseTimestamp(String s)
            throws InvalidDataException {
        if (s == null || s.trim().isEmpty()) {
            throw new InvalidDataException(
                    "Timestamp cannot be null or empty");
        }
        try {
            return LocalDateTime.parse(s.trim(), TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException(
                    "Invalid timestamp format: '" + s
                    + "' (expected yyyy-MM-dd HH:mm:ss)", e);
        }
    }

    /**
     * Returns true if the first field of a CSV line matches the expected
     * header label (case-insensitive). Used to skip the header row during
     * CSV loading.
     *
     * @param parts      the split fields of a CSV line
     * @param firstField the expected header value for the first column
     * @return true if this is a header line, false otherwise
     */
    public static boolean isHeaderLine(String[] parts, String firstField) {
        return parts != null
                && parts.length > 0
                && parts[0].trim().equalsIgnoreCase(firstField);
    }
}
