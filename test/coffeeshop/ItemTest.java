package coffeeshop;

import coffeeshop.exception.InvalidDataException;
import coffeeshop.model.Item;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Item class.
 * Covers constructor validation and all code paths.
 */
public class ItemTest {

    // ---- valid construction ---- //

    @Test
    public void testValidItemCreation() throws InvalidDataException {
        Item item = new Item("BEV-001", "Espresso", "Strong black coffee", 2.50, "BEV");
        assertEquals("BEV-001", item.getItemId());
        assertEquals("Espresso", item.getName());
        assertEquals("Strong black coffee", item.getDescription());
        assertEquals(2.50, item.getPrice(), 0.001);
        assertEquals("BEV", item.getCategory());
        assertEquals(0, item.getOrderCount());
    }

    @Test
    public void testIncrementOrderCount() throws InvalidDataException {
        Item item = new Item("FOO-042", "Croissant", "Butter croissant", 3.00, "FOO");
        item.incrementOrderCount();
        item.incrementOrderCount();
        assertEquals(2, item.getOrderCount());
    }

    // ---- invalid IDs ---- //

    @Test
    public void testInvalidItemId_null() {
        assertThrows(InvalidDataException.class,
                () -> new Item(null, "Espresso", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_tooShortPrefix() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BV-001", "Item", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_tooLongPrefix() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEVV-001", "Item", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_lowercasePrefix() {
        assertThrows(InvalidDataException.class,
                () -> new Item("bev-001", "Item", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_wrongSuffixLength() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-01", "Item", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_letterInSuffix() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-0A1", "Item", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidItemId_noDash() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV001", "Item", "Desc", 2.50, "BEV"));
    }

    // ---- invalid other fields ---- //

    @Test
    public void testInvalidName_empty() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-001", "", "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidName_null() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-001", null, "Desc", 2.50, "BEV"));
    }

    @Test
    public void testInvalidPrice_zero() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-001", "Espresso", "Desc", 0.0, "BEV"));
    }

    @Test
    public void testInvalidPrice_negative() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-001", "Espresso", "Desc", -1.0, "BEV"));
    }

    @Test
    public void testInvalidCategory_empty() {
        assertThrows(InvalidDataException.class,
                () -> new Item("BEV-001", "Espresso", "Desc", 2.50, ""));
    }

    // ---- edge cases ---- //

    @Test
    public void testDescriptionCanBeEmpty() throws InvalidDataException {
        Item item = new Item("OTH-001", "Napkin", "", 0.10, "OTH");
        assertEquals("", item.getDescription());
    }

    @Test
    public void testToStringNotNull() throws InvalidDataException {
        Item item = new Item("FOO-001", "Muffin", "Chocolate muffin", 2.00, "FOO");
        assertNotNull(item.toString());
    }
}
