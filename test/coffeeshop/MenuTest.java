package coffeeshop;

import model.Item;
import model.Menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for {@link Menu}.
 *
 * Uses JUnit's {@code @TempDir} to create temporary CSV files so that
 * no real file-system paths are hard-coded.
 *
 * Covers:
 * - Valid CSV loads items correctly
 * - Header row is skipped
 * - Comment lines are skipped
 * - Lines with bad item ID format are skipped (loading continues)
 * - Lines with non-numeric price are skipped (loading continues)
 * - Lines with negative price are skipped (loading continues)
 * - Empty CSV loads 0 items without throwing
 * - containsItem() and getItem() work after loading
 */
public class MenuTest {

    @TempDir
    Path tempDir;

    private Menu menu;

    @BeforeEach
    public void setUp() {
        menu = new Menu();
    }

    // ================================================================== //
    //  Helpers                                                             //
    // ================================================================== //

    /** Writes lines to a temp CSV file and returns its path string. */
    private String writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("menu_test.csv");
        Files.write(file, java.util.Arrays.asList(lines));
        return file.toString();
    }

    // ================================================================== //
    //  Valid loading                                                        //
    // ================================================================== //

    @Test
    public void testLoadFromCSV_validItems() throws IOException {
        String path = writeCsv(
                "itemId,name,description,price,category",
                "COF-001,Espresso,Strong black coffee,2.50,Beverage",
                "FOO-001,Croissant,Butter pastry,3.20,Food"
        );
        menu.loadFromCSV(path);
        assertEquals(2, menu.size());
        assertTrue(menu.containsItem("COF-001"));
        assertTrue(menu.containsItem("FOO-001"));
    }

    @Test
    public void testLoadFromCSV_itemFieldsCorrect() throws IOException {
        String path = writeCsv(
                "itemId,name,description,price,category",
                "COF-001,Espresso,Strong black coffee,2.50,Beverage"
        );
        menu.loadFromCSV(path);
        Item item = menu.getItem("COF-001");
        assertNotNull(item);
        assertEquals("COF-001",              item.getItemId());
        assertEquals("Espresso",             item.getName());
        assertEquals("Strong black coffee",  item.getDescription());
        assertEquals(2.50,                   item.getPrice(), 0.001);
        assertEquals("Beverage",             item.getCategory());
    }

    // ================================================================== //
    //  Header and comment skipping                                         //
    // ================================================================== //

    @Test
    public void testLoadFromCSV_headerRowSkipped() throws IOException {
        String path = writeCsv(
                "itemId,name,description,price,category",   // header
                "COF-001,Espresso,Desc,2.50,Beverage"
        );
        menu.loadFromCSV(path);
        // Only 1 real item — header must not have been loaded as an item
        assertEquals(1, menu.size());
    }

    @Test
    public void testLoadFromCSV_commentLinesSkipped() throws IOException {
        String path = writeCsv(
                "# This is a comment",
                "COF-001,Espresso,Desc,2.50,Beverage"
        );
        menu.loadFromCSV(path);
        assertEquals(1, menu.size());
    }

    // ================================================================== //
    //  Invalid lines are skipped, loading continues                        //
    // ================================================================== //

    @Test
    public void testLoadFromCSV_badItemId_skipped() throws IOException {
        // "abc-001" has lowercase prefix — fails validateItemId
        String path = writeCsv(
                "COF-001,Espresso,Desc,2.50,Beverage",
                "abc-001,BadItem,Desc,2.00,Beverage"   // invalid ID
        );
        menu.loadFromCSV(path);
        // Only the valid item should be loaded
        assertEquals(1, menu.size());
        assertTrue(menu.containsItem("COF-001"));
        assertFalse(menu.containsItem("abc-001"));
    }

    @Test
    public void testLoadFromCSV_nonNumericPrice_skipped() throws IOException {
        String path = writeCsv(
                "COF-001,Espresso,Desc,2.50,Beverage",
                "COF-002,Latte,Desc,FREE,Beverage"     // non-numeric price
        );
        menu.loadFromCSV(path);
        assertEquals(1, menu.size());
        assertFalse(menu.containsItem("COF-002"));
    }

    @Test
    public void testLoadFromCSV_negativePrice_skipped() throws IOException {
        String path = writeCsv(
                "COF-001,Espresso,Desc,2.50,Beverage",
                "COF-002,Latte,Desc,-1.00,Beverage"    // negative price
        );
        menu.loadFromCSV(path);
        assertEquals(1, menu.size());
        assertFalse(menu.containsItem("COF-002"));
    }

    @Test
    public void testLoadFromCSV_tooFewFields_skipped() throws IOException {
        String path = writeCsv(
                "COF-001,Espresso,Desc,2.50,Beverage",
                "COF-002,OnlyTwoFields"                 // only 2 fields
        );
        menu.loadFromCSV(path);
        assertEquals(1, menu.size());
    }

    // ================================================================== //
    //  Edge cases                                                          //
    // ================================================================== //

    @Test
    public void testLoadFromCSV_emptyFile_loadsZeroItems() throws IOException {
        String path = writeCsv(); // empty file
        menu.loadFromCSV(path);
        assertEquals(0, menu.size());
    }

    @Test
    public void testLoadFromCSV_fileNotFound_throwsIOException() {
        assertThrows(IOException.class,
                () -> menu.loadFromCSV("nonexistent/path/menu.csv"));
    }

    // ================================================================== //
    //  getItem() and getAllItems()                                          //
    // ================================================================== //

    @Test
    public void testGetItem_unknownId_returnsNull() throws IOException {
        String path = writeCsv("COF-001,Espresso,Desc,2.50,Beverage");
        menu.loadFromCSV(path);
        assertNull(menu.getItem("ZZZ-999"));
    }

    @Test
    public void testGetAllItems_returnsAllLoaded() throws IOException {
        String path = writeCsv(
                "COF-001,Espresso,Desc,2.50,Beverage",
                "FOO-001,Croissant,Desc,3.20,Food",
                "OTH-001,Cup,Desc,9.99,Other"
        );
        menu.loadFromCSV(path);
        Collection<Item> all = menu.getAllItems();
        assertEquals(3, all.size());
    }
}
