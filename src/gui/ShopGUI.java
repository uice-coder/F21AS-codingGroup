package gui;

import model.Item;
import model.Manager;
import model.Menu;
import model.Order;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * Main GUI window for the Coffee Shop Order System.
 *
 * <h3>Layout:</h3>
 * <pre>
 *  NORTH  – title bar
 *  CENTER – left: menu panel | right: basket panel (split pane)
 *  SOUTH  – customer ID field + action buttons
 * </pre>
 *
 * <h3>Discount rules displayed in real time:</h3>
 * <ul>
 *   <li>Combo: 1+ Beverage + 2+ Food &rarr; 20&nbsp;% off</li>
 *   <li>Threshold: total &gt; &pound;50 &rarr; &pound;5 off</li>
 * </ul>
 */
public class ShopGUI extends JFrame {

    // ---- Model references ---- //
    private final Manager      manager;
    private final Menu         menu;

    // ---- Basket state ---- //
    private final List<Item>   basket = new ArrayList<>();
    private int                orderCounter = 0;   // auto-increments for GUI orders

    // ---- Menu panel ---- //
    private DefaultListModel<String> menuListModel;
    private JList<String>            menuList;
    private final Map<String, Item>  displayToItem = new LinkedHashMap<>();

    // ---- Basket panel ---- //
    private JTextArea basketArea;
    private JLabel    subtotalLabel;
    private JLabel    discountLabel;
    private JLabel    totalLabel;

    // ---- Customer ID input ---- //
    private JTextField customerIdField;

    // ------------------------------------------------------------------ //
    //  Construction                                                         //
    // ------------------------------------------------------------------ //

    public ShopGUI(Manager manager) {
        this.manager = manager;
        this.menu    = manager.getMenu();
        initUI();
        setVisible(true);
    }

    // ------------------------------------------------------------------ //
    //  UI initialisation                                                    //
    // ------------------------------------------------------------------ //

    private void initUI() {
        setTitle("Coffee Shop Order System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(245, 235, 220));

        add(buildTitleBar(),   BorderLayout.NORTH);
        add(buildMainPanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onExit(); }
        });
    }

    // ---- Title bar ---- //
    private JLabel buildTitleBar() {
        JLabel lbl = new JLabel("Coffee Shop Order System", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl.setForeground(new Color(80, 40, 10));
        lbl.setBorder(new EmptyBorder(14, 10, 8, 10));
        return lbl;
    }

    // ---- Centre split pane ---- //
    private JSplitPane buildMainPanel() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildMenuPanel(),
                buildBasketPanel());
        split.setDividerLocation(510);
        split.setResizeWeight(0.55);
        split.setBorder(new EmptyBorder(4, 10, 4, 10));
        return split;
    }

    // ------------------------------------------------------------------ //
    //  Menu panel                                                           //
    // ------------------------------------------------------------------ //

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(160, 100, 40), 2),
                "Menu  (double-click or press Add)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(80, 40, 10)));
        panel.setBackground(new Color(252, 248, 240));

        menuListModel = new DefaultListModel<>();
        populateMenuList();

        menuList = new JList<>(menuListModel);
        menuList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setBackground(new Color(252, 248, 240));
        menuList.setCellRenderer(new MenuCellRenderer());
        menuList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addSelectedItem();
            }
        });

        JButton addBtn = new JButton("Add to Basket");
        styleButton(addBtn, new Color(60, 150, 60));
        addBtn.addActionListener(e -> addSelectedItem());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        btnRow.setBackground(new Color(252, 248, 240));
        btnRow.add(addBtn);

        panel.add(new JScrollPane(menuList), BorderLayout.CENTER);
        panel.add(btnRow,                   BorderLayout.SOUTH);
        return panel;
    }

    /** Populates the menu list grouped by category (Beverage, Food, Other). */
    private void populateMenuList() {
        menuListModel.clear();
        displayToItem.clear();

        // Fixed display order for known categories; unknown categories appended last
        String[] catOrder = {"Beverage", "Food", "Other"};
        Map<String, List<Item>> byCategory = new LinkedHashMap<>();
        for (String c : catOrder) byCategory.put(c, new ArrayList<>());

        for (Item item : menu.getAllItems()) {
            // Category stored as original case in Item; match case-insensitively
            String cat = findCategoryKey(item.getCategory(), catOrder);
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<String, List<Item>> entry : byCategory.entrySet()) {
            List<Item> list = entry.getValue();
            if (list.isEmpty()) continue;

            // Section header
            String header = "-- " + entry.getKey() + " --";
            menuListModel.addElement(header);
            displayToItem.put(header, null);

            list.sort(Comparator.comparing(Item::getItemId));
            for (Item item : list) {
                String row = String.format("  %-8s %-23s £%.2f",
                        item.getItemId(), item.getName(), item.getPrice());
                menuListModel.addElement(row);
                displayToItem.put(row, item);
            }
        }
    }

    /** Case-insensitive lookup of a category key; returns the raw value if not found. */
    private String findCategoryKey(String rawCategory, String[] knownKeys) {
        for (String key : knownKeys) {
            if (key.equalsIgnoreCase(rawCategory)) return key;
        }
        return rawCategory;
    }

    private void addSelectedItem() {
        String sel = menuList.getSelectedValue();
        if (sel == null) return;
        Item item = displayToItem.get(sel);
        if (item == null) return;  // header row
        basket.add(item);
        refreshBasket();
    }

    // ------------------------------------------------------------------ //
    //  Basket panel                                                         //
    // ------------------------------------------------------------------ //

    private JPanel buildBasketPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(160, 100, 40), 2),
                "Your Basket",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(80, 40, 10)));
        panel.setBackground(new Color(252, 248, 240));

        basketArea = new JTextArea(12, 30);
        basketArea.setEditable(false);
        basketArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        basketArea.setBackground(new Color(255, 253, 245));
        panel.add(new JScrollPane(basketArea), BorderLayout.CENTER);

        // Totals panel
        JPanel totals = new JPanel(new GridLayout(3, 1, 2, 2));
        totals.setBackground(new Color(252, 248, 240));
        totals.setBorder(new EmptyBorder(6, 5, 4, 5));
        subtotalLabel = makeLabel("Subtotal:  £0.00");
        discountLabel = makeLabel("Discount:  £0.00  (no discount)");
        discountLabel.setForeground(new Color(170, 0, 0));
        totalLabel    = makeLabel("TOTAL:     £0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalLabel.setForeground(new Color(30, 110, 30));
        totals.add(subtotalLabel);
        totals.add(discountLabel);
        totals.add(totalLabel);
        panel.add(totals, BorderLayout.SOUTH);

        // Remove / Clear buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        btnRow.setBackground(new Color(252, 248, 240));
        JButton removeBtn = new JButton("Remove Last");
        JButton clearBtn  = new JButton("Clear Basket");
        styleButton(removeBtn, new Color(200, 80, 40));
        styleButton(clearBtn,  new Color(180, 60, 60));
        removeBtn.addActionListener(e -> {
            if (!basket.isEmpty()) { basket.remove(basket.size() - 1); refreshBasket(); }
        });
        clearBtn.addActionListener(e -> { basket.clear(); refreshBasket(); });
        btnRow.add(removeBtn);
        btnRow.add(clearBtn);
        panel.add(btnRow, BorderLayout.NORTH);

        return panel;
    }

    /** Recomputes and displays basket contents, discount, and total. */
    private void refreshBasket() {
        if (basket.isEmpty()) {
            basketArea.setText("  (empty - add items from the menu)");
            subtotalLabel.setText("Subtotal:  £0.00");
            discountLabel.setText("Discount:  £0.00  (no discount)");
            totalLabel.setText("TOTAL:     £0.00");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basket.size(); i++) {
            Item it = basket.get(i);
            sb.append(String.format("  %d. %-24s £%.2f%n",
                    i + 1, it.getName(), it.getPrice()));
        }
        basketArea.setText(sb.toString());

        double subtotal = manager.calculateSubtotal(basket);
        double discount = manager.calculateDiscount(basket);
        double total    = manager.calculateFinalTotal(basket);

        subtotalLabel.setText(String.format("Subtotal:  £%.2f", subtotal));
        if (discount > 0) {
            discountLabel.setText(String.format("Discount:  -£%.2f  (%s)",
                    discount, discountLabel(basket, subtotal, discount)));
        } else {
            discountLabel.setText("Discount:  £0.00  (no discount)");
        }
        totalLabel.setText(String.format("TOTAL:     £%.2f", total));
    }

    /** Returns a human-readable description of which discount rule was applied. */
    private String discountLabel(List<Item> items, double subtotal, double discount) {
        int bev = 0, food = 0;
        for (Item it : items) {
            if ("Beverage".equalsIgnoreCase(it.getCategory())) bev++;
            else if ("Food".equalsIgnoreCase(it.getCategory())) food++;
        }
        if (bev >= 1 && food >= 2 && Math.abs(discount - subtotal * 0.20) < 0.001)
            return "20% combo (1 drink + 2+ food)";
        if (subtotal > 50.0 && Math.abs(discount - 5.0) < 0.001)
            return "£5 off (order > £50)";
        return "discount applied";
    }

    // ------------------------------------------------------------------ //
    //  South panel: customer ID + action buttons                           //
    // ------------------------------------------------------------------ //

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(new Color(235, 215, 185));
        panel.setBorder(new EmptyBorder(6, 12, 10, 12));

        // Customer ID row
        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        custRow.setBackground(new Color(235, 215, 185));
        JLabel custLbl = new JLabel("Customer ID:");
        custLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        customerIdField = new JTextField(14);
        customerIdField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        customerIdField.setToolTipText("Enter customer ID, e.g. CUST-001");
        custRow.add(custLbl);
        custRow.add(customerIdField);
        panel.add(custRow, BorderLayout.NORTH);

        // Action buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 4));
        btnRow.setBackground(new Color(235, 215, 185));

        JButton placeBtn  = new JButton("Place Order");
        JButton reportBtn = new JButton("Generate Report");
        JButton exitBtn   = new JButton("Exit");

        styleButton(placeBtn,  new Color(40, 140, 60));
        styleButton(reportBtn, new Color(60, 100, 180));
        styleButton(exitBtn,   new Color(140, 60, 40));

        placeBtn.addActionListener(e  -> onPlaceOrder());
        reportBtn.addActionListener(e -> onGenerateReport());
        exitBtn.addActionListener(e   -> onExit());

        btnRow.add(placeBtn);
        btnRow.add(reportBtn);
        btnRow.add(exitBtn);
        panel.add(btnRow, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Actions                                                              //
    // ------------------------------------------------------------------ //

    /** Validates input, creates an Order, applies discount, stores in Manager. */
    private void onPlaceOrder() {
        if (basket.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your basket is empty. Please add items first.",
                    "Empty Basket", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerId = customerIdField.getText().trim();
        if (customerId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Customer ID before placing an order.",
                    "Missing Customer ID", JOptionPane.WARNING_MESSAGE);
            customerIdField.requestFocus();
            return;
        }

        // Build the Order
        orderCounter++;
        String orderId = String.format("GUI-%03d", orderCounter);
        Order order = new Order(orderId, customerId, LocalDateTime.now());
        for (Item item : basket) {
            order.addItem(item);
            item.incrementOrderCount();
        }

        // Apply discount (stores discount + finalPrice in the Order object)
        manager.applyDiscount(order);

        // Store in Manager (direct field access via Manager.getOrders() is read-only,
        // so we expose an addOrder method — or use reflection. Simplest: store here
        // by calling the private list through the public accessor that adds.)
        // Manager exposes orders only as unmodifiable list, so we use a helper.
        manager.addOrderFromGUI(order);

        double final_ = order.getFinalPrice();
        double disc   = order.getDiscount();
        String discTxt = disc > 0
                ? String.format("Discount applied: -£%.2f%n", disc)
                : "No discount applied.\n";

        JOptionPane.showMessageDialog(this,
                String.format("Order %s placed!%n%sTotal charged: £%.2f%n%nThank you, %s!",
                        orderId, discTxt, final_, customerId),
                "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);

        basket.clear();
        refreshBasket();
    }

    /** Shows the current sales report in a scrollable dialog. */
    private void onGenerateReport() {
        String report = manager.generateSalesReport();
        JTextArea area = new JTextArea(report, 26, 58);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this,
                new JScrollPane(area), "Sales Report", JOptionPane.PLAIN_MESSAGE);
    }

    /** Prompts the user to generate a report, then exits. */
    private void onExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Generate sales report before exiting?",
                "Exit Application", JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.CANCEL_OPTION) return;
        if (choice == JOptionPane.YES_OPTION) {
            onGenerateReport();
            System.out.println(manager.generateSalesReport());
        }
        dispose();
        System.exit(0);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                              //
    // ------------------------------------------------------------------ //

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setBorder(new EmptyBorder(2, 5, 2, 5));
        return lbl;
    }

    /** Cell renderer that visually distinguishes category headers from item rows. */
    private static class MenuCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value.toString().startsWith("--")) {
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(new Color(100, 50, 0));
                lbl.setBackground(new Color(235, 210, 170));
                lbl.setBorder(new EmptyBorder(4, 5, 4, 5));
            }
            return lbl;
        }
    }
}
