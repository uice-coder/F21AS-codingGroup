package coffeeshop;

import exception.InvalidDataException;
import model.Item;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the {@link Item} class.
 *
 * <p>Covers constructor validation (valid and invalid inputs), getter/setter
 * behaviour, and the orderCount helper.</p>
 *
 * <p>itemId regex in effect: {@code ^[A-Z]+-\d{3}$} (one or more uppercase
 * letters, a dash, exactly three digits).</p>
 */
public class ItemTest {

    // ================================================================== //
    //  Valid construction                                                   //
    // ================================================================== //

    @Test
    public void testValidItem_standard() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Strong black coffee", 2.50, "Beverage");
        assertEquals("COF-001",          item.getItemId());
        assertEquals("Espresso",         item.getName());
        assertEquals("Strong black coffee", item.getDescription());
        assertEquals(2.50, item.getPrice(), 0.001);
        assertEquals("Beverage",         item.getCategory());
        assertEquals(0,                  item.getOrderCount());
    }

    @Test
    public void testValidItem_singleLetterPrefix() throws InvalidDataException {
        // Regex ^[A-Z]+-\d{3}$ allows a single uppercase letter as prefix
        Item item = new Item("A-001", "Item A", "Single letter prefix", 1.00, "Other");
        assertEquals("A-001", item.getItemId());
    }

    @Test
    public void testValidItem_twoLetterPrefix() throws InvalidDataException {
        // Two uppercase letters are also valid
        Item item = new Item("BV-001", "Item BV", "Two letter prefix", 1.50, "Other");
        assertEquals("BV-001", item.getItemId());
    }

    @Test
    public void testValidItem_fourLetterPrefix() throws InvalidDataException {
        // Category prefixes with 4+ letters are valid (e.g. FOOD-002)
        Item item = new Item("FOOD-002", "Toast", "Avocado toast", 4.50, "Food");
        assertEquals("FOOD-002", item.getItemId());
    }

    @Test
    public void testValidItem_emptyDescription() throws InvalidDataException {
        // Empty description is permitted
        Item item = new Item("OTH-001", "Napkin", "", 0.10, "Other");
        assertEquals("", item.getDescription());
    }

    @Test
    public void testValidItem_nullDescriptionTreatedAsEmpty() throws InvalidDataException {
        Item item = new Item("OTH-002", "Cup", null, 5.00, "Other");
        assertEquals("", item.getDescription());
    }

    @Test
    public void testIncrementOrderCount() throws InvalidDataException {
        Item item = new Item("FOO-042", "Croissant", "Butter croissant", 3.00, "Food");
        item.incrementOrderCount();
        item.incrementOrderCount();
        assertEquals(2, item.getOrderCount());
    }

    @Test
    public void testToStringNotNull() throws InvalidDataException {
        Item item = new Item("FOO-001", "Muffin", "Chocolate muffin", 2.00, "Food");
        assertNotNull(item.toString());
        assertTrue(item.toString().contains("FOO-001"));
    }

    // ================================================================== //
    //  Invalid itemId                                                       //
    // ================================================================== //

    @Test
    public void testInvalidItemId_null() {
        assertThrows(InvalidDataException.class,
                () -> new Item(null, "Espresso", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_empty() {
        assertThrows(InvalidDataException.class,
                () -> new Item("", "Espresso", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_lowercasePrefix() {
        // Lowercase letters do NOT match [A-Z]+
        assertThrows(InvalidDataException.class,
                () -> new Item("cof-001", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_mixedCasePrefix() {
        // Mixed case does NOT match [A-Z]+
        assertThrows(InvalidDataException.class,
                () -> new Item("Cof-001", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_noDash() {
        // Missing separator dash
        assertThrows(InvalidDataException.class,
                () -> new Item("COF001", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_wrongSuffixLength_two() {
        // Only 2 digits in suffix (needs exactly 3)
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-01", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_wrongSuffixLength_four() {
        // 4 digits in suffix (needs exactly 3)
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-0001", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_letterInSuffix() {
        // Suffix contains a letter — not all digits
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-0A1", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_noPrefix() {
        // Starts with a dash — prefix is empty
        assertThrows(InvalidDataException.class,
                () -> new Item("-001", "Item", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidItemId_noSuffix() {
        // Nothing after the dash
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-", "Item", "Desc", 2.50, "Beverage"));
    }

    // ================================================================== //
    //  Invalid other fields                                                 //
    // ================================================================== //

    @Test
    public void testInvalidName_empty() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", "", "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidName_null() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", null, "Desc", 2.50, "Beverage"));
    }

    @Test
    public void testInvalidPrice_zero() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", "Espresso", "Desc", 0.0, "Beverage"));
    }

    @Test
    public void testInvalidPrice_negative() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", "Espresso", "Desc", -1.0, "Beverage"));
    }

    @Test
    public void testInvalidCategory_empty() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", "Espresso", "Desc", 2.50, ""));
    }

    @Test
    public void testInvalidCategory_null() {
        assertThrows(InvalidDataException.class,
                () -> new Item("COF-001", "Espresso", "Desc", 2.50, null));
    }

    // ================================================================== //
    //  Setters                                                              //
    // ================================================================== //

    @Test
    public void testSetName() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Desc", 2.50, "Beverage");
        item.setName("Double Espresso");
        assertEquals("Double Espresso", item.getName());
    }

    @Test
    public void testSetPrice_valid() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Desc", 2.50, "Beverage");
        item.setPrice(3.00);
        assertEquals(3.00, item.getPrice(), 0.001);
    }

    @Test
    public void testSetPrice_invalid() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Desc", 2.50, "Beverage");
        assertThrows(IllegalArgumentException.class, () -> item.setPrice(0.0));
    }

    @Test
    public void testSetOrderCount() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Desc", 2.50, "Beverage");
        item.setOrderCount(5);
        assertEquals(5, item.getOrderCount());
    }

    @Test
    public void testSetOrderCount_negative_throws() throws InvalidDataException {
        Item item = new Item("COF-001", "Espresso", "Desc", 2.50, "Beverage");
        assertThrows(IllegalArgumentException.class, () -> item.setOrderCount(-1));
    }
}
