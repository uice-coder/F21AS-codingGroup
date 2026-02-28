package coffeeshop.gui;

import coffeeshop.model.Item;
import coffeeshop.model.Manager;
import coffeeshop.model.Menu;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main GUI window for the Coffee Shop Simulation.
 *
 * Layout:
 *   LEFT  – Menu panel: items grouped by category, click to add to basket
 *   RIGHT – Basket panel: shows selected items, discount info, and total
 *   BOTTOM – Buttons: Place Order, Generate Report, Exit
 */
public class ShopGUI extends JFrame {

    private final Manager manager;
    private final Menu menu;

    // Basket: tracks selected items for the current customer
    private final List<Item> basket = new ArrayList<>();

    // GUI components
    private JList<String>  menuList;
    private DefaultListModel<String> menuListModel;

    private JTextArea      basketArea;
    private JLabel         subtotalLabel;
    private JLabel         discountLabel;
    private JLabel         totalLabel;
    private JTextArea      reportArea;

    // Map from display string → Item (for list selection)
    private final Map<String, Item> displayToItem = new LinkedHashMap<>();

    // -------------------------------------------------- //

    public ShopGUI(Manager manager) {
        this.manager = manager;
        this.menu    = manager.getMenu();
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setTitle("☕ Coffee Shop Simulation - Stage 1");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 235, 220));

        // Title bar
        JLabel titleLabel = new JLabel("☕ Coffee Shop Order System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(80, 40, 10));
        titleLabel.setBorder(new EmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Main split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildMenuPanel(), buildBasketPanel());
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(splitPane, BorderLayout.CENTER);

        // Bottom button bar
        add(buildButtonBar(), BorderLayout.SOUTH);

        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onExit(); }
        });
    }

    // -------------------------------------------------- //
    //  Menu panel                                         //
    // -------------------------------------------------- //

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(160, 100, 40), 2),
                "📋 Menu  (double-click or 'Add' to order)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(80, 40, 10)));
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
                if (e.getClickCount() == 2) addSelectedItemToBasket();
            }
        });

        JScrollPane scroll = new JScrollPane(menuList);
        panel.add(scroll, BorderLayout.CENTER);

        JButton addBtn = new JButton("➕ Add to Basket");
        styleButton(addBtn, new Color(60, 150, 60));
        addBtn.addActionListener(e -> addSelectedItemToBasket());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(252, 248, 240));
        btnPanel.add(addBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void populateMenuList() {
        menuListModel.clear();
        displayToItem.clear();

        // Group items by category
        Map<String, List<Item>> byCategory = new LinkedHashMap<>();
        String[] catOrder = {"BEV", "FOO", "OTH"};
        for (String cat : catOrder) byCategory.put(cat, new ArrayList<>());

        for (Item item : menu.getAllItems()) {
            byCategory.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        }

        Map<String, String> catNames = Map.of("BEV","☕ Beverages","FOO","🥐 Food","OTH","🛒 Other");

        for (Map.Entry<String, List<Item>> entry : byCategory.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            String header = "── " + catNames.getOrDefault(entry.getKey(), entry.getKey()) + " ──";
            menuListModel.addElement(header);
            displayToItem.put(header, null); // header row

            List<Item> items = entry.getValue();
            items.sort(Comparator.comparing(Item::getItemId));
            for (Item item : items) {
                String display = String.format("  %-8s %-22s £%.2f",
                        item.getItemId(), item.getName(), item.getPrice());
                menuListModel.addElement(display);
                displayToItem.put(display, item);
            }
        }
    }

    private void addSelectedItemToBasket() {
        String selected = menuList.getSelectedValue();
        if (selected == null) return;
        Item item = displayToItem.get(selected);
        if (item == null) return; // header row

        basket.add(item);
        refreshBasket();
    }

    // -------------------------------------------------- //
    //  Basket panel                                       //
    // -------------------------------------------------- //

    private JPanel buildBasketPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(160, 100, 40), 2),
                "🛒 Your Basket",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(80, 40, 10)));
        panel.setBackground(new Color(252, 248, 240));

        basketArea = new JTextArea(10, 30);
        basketArea.setEditable(false);
        basketArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        basketArea.setBackground(new Color(255, 253, 245));
        panel.add(new JScrollPane(basketArea), BorderLayout.CENTER);

        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 1, 3, 3));
        totalsPanel.setBackground(new Color(252, 248, 240));
        totalsPanel.setBorder(new EmptyBorder(8, 5, 5, 5));

        subtotalLabel = makeLabel("Subtotal:  £0.00");
        discountLabel = makeLabel("Discount:  £0.00  (no discount)");
        discountLabel.setForeground(new Color(180, 0, 0));
        totalLabel    = makeLabel("TOTAL:     £0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalLabel.setForeground(new Color(40, 110, 40));

        totalsPanel.add(subtotalLabel);
        totalsPanel.add(discountLabel);
        totalsPanel.add(totalLabel);
        panel.add(totalsPanel, BorderLayout.SOUTH);

        // Remove last / Clear buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(252, 248, 240));
        JButton removeBtn = new JButton("❌ Remove Last");
        JButton clearBtn  = new JButton("🗑 Clear Basket");
        styleButton(removeBtn, new Color(200, 80, 40));
        styleButton(clearBtn,  new Color(180, 60, 60));
        removeBtn.addActionListener(e -> { if (!basket.isEmpty()) { basket.remove(basket.size()-1); refreshBasket(); } });
        clearBtn.addActionListener(e -> { basket.clear(); refreshBasket(); });
        btnPanel.add(removeBtn);
        btnPanel.add(clearBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        return panel;
    }

    private void refreshBasket() {
        if (basket.isEmpty()) {
            basketArea.setText("  (empty – add items from the menu)");
            subtotalLabel.setText("Subtotal:  £0.00");
            discountLabel.setText("Discount:  £0.00  (no discount)");
            totalLabel.setText("TOTAL:     £0.00");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basket.size(); i++) {
            Item item = basket.get(i);
            sb.append(String.format("  %d. %-22s £%.2f%n", i + 1, item.getName(), item.getPrice()));
        }
        basketArea.setText(sb.toString());

        double subtotal  = manager.calculateSubtotal(basket);
        double discount  = manager.calculateDiscount(basket);
        double total     = manager.calculateFinalTotal(basket);

        subtotalLabel.setText(String.format("Subtotal:  £%.2f", subtotal));
        if (discount > 0) {
            String rule = discountDescription(basket, discount);
            discountLabel.setText(String.format("Discount:  -£%.2f  (%s)", discount, rule));
        } else {
            discountLabel.setText("Discount:  £0.00  (no discount)");
        }
        totalLabel.setText(String.format("TOTAL:     £%.2f", total));
    }

    private String discountDescription(List<Item> items, double discount) {
        int bev = 0, food = 0;
        double subtotal = manager.calculateSubtotal(items);
        for (Item item : items) {
            if ("BEV".equalsIgnoreCase(item.getCategory())) bev++;
            else if ("FOO".equalsIgnoreCase(item.getCategory())) food++;
        }
        if (bev >= 1 && food >= 2 && discount == subtotal * 0.15) return "15% combo deal";
        if (bev >= 1 && food >= 1 && discount == subtotal * 0.08) return "8% combo deal";
        if (subtotal > 50.0 && discount == 5.0) return "£5 off (order > £50)";
        return "discount applied";
    }

    // -------------------------------------------------- //
    //  Button bar                                         //
    // -------------------------------------------------- //

    private JPanel buildButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(235, 215, 185));
        panel.setBorder(new EmptyBorder(5, 10, 10, 10));

        JButton placeBtn  = new JButton("✅ Place Order");
        JButton reportBtn = new JButton("📊 Generate Report");
        JButton exitBtn   = new JButton("🚪 Exit");

        styleButton(placeBtn,  new Color(40, 140, 60));
        styleButton(reportBtn, new Color(60, 100, 180));
        styleButton(exitBtn,   new Color(140, 60, 40));

        placeBtn.addActionListener(e -> onPlaceOrder());
        reportBtn.addActionListener(e -> onGenerateReport());
        exitBtn.addActionListener(e -> onExit());

        panel.add(placeBtn);
        panel.add(reportBtn);
        panel.add(exitBtn);
        return panel;
    }

    // -------------------------------------------------- //
    //  Actions                                            //
    // -------------------------------------------------- //

    private void onPlaceOrder() {
        if (basket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your basket is empty! Please add items first.",
                    "Empty Basket", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = manager.calculateFinalTotal(basket);
        String msg   = String.format("Order placed!%nTotal charged: £%.2f%n%nThank you for your order! ☕",
                total);
        JOptionPane.showMessageDialog(this, msg, "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
        basket.clear();
        refreshBasket();
    }

    private void onGenerateReport() {
        String report = manager.generateReport();
        // Show in a new dialog with a scrollable text area
        JTextArea area = new JTextArea(report, 25, 55);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll, "Sales Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void onExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Generate sales report before exiting?",
                "Exit", JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            onGenerateReport();
            System.out.println(manager.generateReport()); // also print to console
        } else if (choice == JOptionPane.CANCEL_OPTION) {
            return;
        }
        dispose();
        System.exit(0);
    }

    // -------------------------------------------------- //
    //  Helpers                                            //
    // -------------------------------------------------- //

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setBorder(new EmptyBorder(2, 5, 2, 5));
        return lbl;
    }

    // Custom cell renderer to highlight category headers
    private class MenuCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            if (text.startsWith("──")) {
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(new Color(100, 50, 0));
                lbl.setBackground(new Color(235, 210, 170));
                lbl.setBorder(new EmptyBorder(4, 5, 4, 5));
            }
            return lbl;
        }
    }
}
