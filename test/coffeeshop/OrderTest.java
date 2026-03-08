package coffeeshop;

import exception.InvalidDataException;
import model.Item;
import model.Order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for {@link Order}.
 *
 * Covers:
 * - Constructor validation (null/blank arguments)
 * - addItem() null guard
 * - calculateFinalPrice() for all three outcomes:
 *     combo discount (20% off), threshold discount (£5 off), no discount
 * - getSubtotal(), getDiscount(), getFinalPrice() after calculation
 */
public class OrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 15, 9, 0, 0);

    private Item bev;    // £5.00  Beverage
    private Item food1;  // £6.00  Food
    private Item food2;  // £7.00  Food
    private Item bigBev; // £55.00 Beverage  (triggers threshold alone)

    @BeforeEach
    public void setUp() throws InvalidDataException {
        bev    = new Item("COF-001", "Espresso",     "Short black",   5.00,  "Beverage");
        food1  = new Item("FOO-001", "Croissant",    "Butter pastry", 6.00,  "Food");
        food2  = new Item("FOO-002", "Muffin",       "Choc muffin",   7.00,  "Food");
        bigBev = new Item("COF-099", "Premium Roast","Rare blend",   55.00, "Beverage");
    }

    // ================================================================== //
    //  Constructor validation                                              //
    // ================================================================== //

    @Test
    public void testConstructor_valid() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        assertEquals("ORD-001",  order.getOrderId());
        assertEquals("CUST-001", order.getCustomerId());
        assertEquals(NOW,        order.getTimestamp());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    public void testConstructor_nullOrderId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order(null, "CUST-001", NOW));
    }

    @Test
    public void testConstructor_emptyOrderId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("", "CUST-001", NOW));
    }

    @Test
    public void testConstructor_nullCustomerId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("ORD-001", null, NOW));
    }

    @Test
    public void testConstructor_emptyCustomerId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("ORD-001", "", NOW));
    }

    @Test
    public void testConstructor_nullTimestamp_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("ORD-001", "CUST-001", null));
    }

    // ================================================================== //
    //  addItem()                                                           //
    // ================================================================== //

    @Test
    public void testAddItem_nullItem_throws() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        assertThrows(IllegalArgumentException.class,
                () -> order.addItem(null));
    }

    @Test
    public void testAddItem_addsToList() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);
        order.addItem(food1);
        assertEquals(2, order.getItems().size());
    }

    // ================================================================== //
    //  calculateFinalPrice() — no discount                                 //
    // ================================================================== //

    @Test
    public void testNoDiscount_singleBeverage() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);           // £5 — no combo, no threshold
        double finalPrice = order.calculateFinalPrice();
        assertEquals(0.0,  order.getDiscount(),  0.001);
        assertEquals(5.0,  order.getFinalPrice(), 0.001);
        assertEquals(5.0,  finalPrice,           0.001);
    }

    @Test
    public void testNoDiscount_oneBevOneFoodOnly() {
        // Combo requires 2+ food; 1 food is not enough
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);
        order.addItem(food1);        // £5 + £6 = £11
        order.calculateFinalPrice();
        assertEquals(0.0,  order.getDiscount(),  0.001);
        assertEquals(11.0, order.getFinalPrice(), 0.001);
    }

    // ================================================================== //
    //  calculateFinalPrice() — combo discount (20%)                        //
    // ================================================================== //

    @Test
    public void testComboDiscount_oneBevTwoFood() {
        // £5 + £6 + £7 = £18  →  20% off = £3.60 discount, £14.40 final
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);
        order.addItem(food1);
        order.addItem(food2);
        order.calculateFinalPrice();
        assertEquals(18.0 * 0.20, order.getDiscount(),   0.001);
        assertEquals(18.0 * 0.80, order.getFinalPrice(),  0.001);
    }

    @Test
    public void testComboDiscount_subtotalAndFinalConsistent() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);
        order.addItem(food1);
        order.addItem(food2);
        order.calculateFinalPrice();
        assertEquals(order.getSubtotal() - order.getDiscount(),
                     order.getFinalPrice(), 0.001);
    }

    // ================================================================== //
    //  calculateFinalPrice() — threshold discount (£5 off)                 //
    // ================================================================== //

    @Test
    public void testThresholdDiscount_over50() {
        // £55 beverage alone → threshold applies, no combo
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bigBev);
        order.calculateFinalPrice();
        assertEquals(5.0,  order.getDiscount(),   0.001);
        assertEquals(50.0, order.getFinalPrice(),  0.001);
    }

    // ================================================================== //
    //  getSubtotal()                                                        //
    // ================================================================== //

    @Test
    public void testGetSubtotal_multipleItems() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        order.addItem(bev);    // £5
        order.addItem(food1);  // £6
        assertEquals(11.0, order.getSubtotal(), 0.001);
    }

    @Test
    public void testGetSubtotal_emptyOrder() {
        Order order = new Order("ORD-001", "CUST-001", NOW);
        assertEquals(0.0, order.getSubtotal(), 0.001);
    }
}
