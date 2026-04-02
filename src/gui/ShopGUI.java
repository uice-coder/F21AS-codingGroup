package gui;

import coffeeshop.AppLifecycleManager;
import model.Item;
import model.Manager;
import model.Menu;
import model.Order;
import simulation.SimulationController;
import simulation.SimulationObserver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import util.ProjectPaths;

public class ShopGUI extends JFrame implements SimulationObserver {

    // colors
    private static final Color GREEN_DARK   = new Color(0, 98, 65);
    private static final Color GREEN_MAIN   = new Color(0, 112, 74);
    private static final Color GREEN_SOFT   = new Color(212, 233, 226);
    private static final Color CREAM_BG     = new Color(248, 245, 240);
    private static final Color CARD_BG      = new Color(255, 252, 248);
    private static final Color TEXT_DARK    = new Color(40, 40, 40);
    private static final Color TEXT_SOFT    = new Color(110, 110, 110);
    private static final Color GOLD_SOFT    = new Color(198, 169, 105);
    private static final Color BORDER_SOFT  = new Color(223, 223, 218);
    private static final Color BUTTON_EXIT  = new Color(120, 108, 96);

    private final Manager manager;
    private final Menu menu;
    private final SimulationController simulationController;
    private final AppLifecycleManager lifecycleManager;

    private final List<Item> basket = new ArrayList<>();
    private int orderCounter = 0;

    private String currentCategory = "All";

    private JPanel productsGrid;
    private JScrollPane productScrollPane;

    private final Map<String, JButton> categoryButtons = new LinkedHashMap<>();

    private JTextArea orderArea;
    private JLabel itemCountLabel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private JButton placeOrderButton;
    private JButton removeLastButton;
    private JButton clearBasketButton;
    private JLabel simulationPolicyLabel;

    private JTextField customerIdField;
    private final List<JButton> productAddButtons = new ArrayList<>();

    public ShopGUI(Manager manager, SimulationController simulationController,
                   AppLifecycleManager lifecycleManager) {
        this.manager = manager;
        this.menu = manager.getMenu();
        this.simulationController = simulationController;
        this.lifecycleManager = lifecycleManager;
        simulationController.addObserver(this);
        initUI();
        refreshProducts();
        refreshBasket();
        updateOrderEntryState();
        setVisible(true);
    }

    private void initUI() {
        setTitle("Coffee Shop Order System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1320, 820);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(CREAM_BG);

        add(buildBanner(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    private JPanel buildBanner() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(GREEN_DARK);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(96, 96));

        JLabel logoPlaceholder = new JLabel("[ Brand Logo ]", SwingConstants.CENTER);
        logoPlaceholder.setOpaque(true);
        logoPlaceholder.setBackground(new Color(255, 255, 255, 35));
        logoPlaceholder.setForeground(Color.WHITE);
        logoPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoPlaceholder.setBorder(new LineBorder(new Color(255, 255, 255, 90), 1, true));
        logoPanel.add(logoPlaceholder, BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Green Roast Coffee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Brewed Daily • Crafted with Care");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(235, 245, 241));

        JLabel imageHint = new JLabel("Top banner image placeholder: logo.png");
        imageHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        imageHint.setForeground(new Color(225, 235, 231));

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(subtitle);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(imageHint);
        textPanel.add(Box.createVerticalGlue());

        panel.add(logoPanel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 16, 0, 16));

        panel.add(buildCategoryPanel(), BorderLayout.WEST);
        panel.add(buildProductsPanel(), BorderLayout.CENTER);
        panel.add(buildOrderPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel buildCategoryPanel() {
        JPanel outer = createCardPanel("Categories");
        outer.setPreferredSize(new Dimension(190, 100));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("<html>Optional icons folder:<br>assets/images/categories/</html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_SOFT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(hint);
        inner.add(Box.createVerticalStrut(14));

        addCategoryButton(inner, "All");
        addCategoryButton(inner, "Beverage");
        addCategoryButton(inner, "Food");
        addCategoryButton(inner, "Other");

        inner.add(Box.createVerticalGlue());

        outer.add(inner, BorderLayout.CENTER);
        updateCategoryButtonStyles();

        return outer;
    }

    private void addCategoryButton(JPanel parent, String category) {
        JButton btn = new JButton(category);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        btn.addActionListener(e -> {
            currentCategory = category;
            updateCategoryButtonStyles();
            refreshProducts();
        });

        categoryButtons.put(category, btn);
        parent.add(btn);
        parent.add(Box.createVerticalStrut(10));
    }

    private void updateCategoryButtonStyles() {
        for (Map.Entry<String, JButton> entry : categoryButtons.entrySet()) {
            JButton btn = entry.getValue();
            boolean selected = entry.getKey().equalsIgnoreCase(currentCategory);

            if (selected) {
                btn.setBackground(GREEN_MAIN);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(GREEN_DARK, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));
            } else {
                btn.setBackground(CARD_BG);
                btn.setForeground(TEXT_DARK);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_SOFT, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));
            }
            btn.setOpaque(true);
        }
    }

    private JPanel buildProductsPanel() {
        JPanel outer = createCardPanel("Menu");
        outer.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel sectionTitle = new JLabel("Today's Selection");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_DARK);

        JLabel hint = new JLabel("Image placeholders shown now • later add files in assets/images/products/");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_SOFT);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(sectionTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(hint);

        top.add(titleBlock, BorderLayout.WEST);
        outer.add(top, BorderLayout.NORTH);

        productsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        productsGrid.setOpaque(false);

        productScrollPane = new JScrollPane(productsGrid);
        productScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        productScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        productScrollPane.getViewport().setBackground(CARD_BG);

        outer.add(productScrollPane, BorderLayout.CENTER);

        return outer;
    }

    private void refreshProducts() {
        productsGrid.removeAll();
        productAddButtons.clear();

        List<Item> items = new ArrayList<>(menu.getAllItems());
        items.sort(Comparator.comparing(Item::getCategory, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Item::getItemId));

        int count = 0;
        for (Item item : items) {
            if (matchesCurrentCategory(item)) {
                productsGrid.add(new ProductCardPanel(item));
                count++;
            }
        }

        if (count == 0) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);

            JLabel lbl = new JLabel("No items in this category.", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lbl.setForeground(TEXT_SOFT);
            lbl.setBorder(new EmptyBorder(40, 10, 40, 10));

            empty.add(lbl, BorderLayout.CENTER);
            productsGrid.add(empty);
        }

        productsGrid.revalidate();
        productsGrid.repaint();
    }

    private boolean matchesCurrentCategory(Item item) {
        return "All".equalsIgnoreCase(currentCategory)
                || item.getCategory().equalsIgnoreCase(currentCategory);
    }

    private JPanel buildOrderPanel() {
        JPanel outer = createCardPanel("Your Order");
        outer.setPreferredSize(new Dimension(330, 100));
        outer.setLayout(new BorderLayout(0, 12));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);

        itemCountLabel = new JLabel("Items in basket: 0");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemCountLabel.setForeground(TEXT_SOFT);

        JLabel discountInfo = new JLabel("<html>Best discount only:<br>Combo or Threshold</html>");
        discountInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        discountInfo.setForeground(TEXT_SOFT);
        discountInfo.setHorizontalAlignment(SwingConstants.RIGHT);

        topInfo.add(itemCountLabel, BorderLayout.WEST);
        topInfo.add(discountInfo, BorderLayout.EAST);

        orderArea = new JTextArea(14, 24);
        orderArea.setEditable(false);
        orderArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        orderArea.setForeground(TEXT_DARK);
        orderArea.setBackground(new Color(253, 251, 247));
        orderArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane orderScroll = new JScrollPane(orderArea);
        orderScroll.setBorder(new LineBorder(BORDER_SOFT, 1, true));
        orderScroll.getViewport().setBackground(new Color(253, 251, 247));

        JPanel totals = new JPanel(new GridLayout(3, 1, 4, 6));
        totals.setBackground(new Color(250, 248, 244));
        totals.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        subtotalLabel = makeInfoLabel("Subtotal   £0.00");
        discountLabel = makeInfoLabel("Discount   £0.00 (none)");
        discountLabel.setForeground(new Color(150, 92, 54));

        totalLabel = makeInfoLabel("TOTAL      £0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(GREEN_DARK);

        totals.add(subtotalLabel);
        totals.add(discountLabel);
        totals.add(totalLabel);

        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);

        removeLastButton  = new JButton("Remove Last");
        clearBasketButton = new JButton("Clear");
        placeOrderButton  = new JButton("Place Order");

        styleSoftActionButton(removeLastButton);
        styleSoftActionButton(clearBasketButton);
        stylePrimaryButton(placeOrderButton);

        removeLastButton.addActionListener(e -> {
            if (!basket.isEmpty()) {
                basket.remove(basket.size() - 1);
                refreshBasket();
                updateOrderEntryState();
            }
        });

        clearBasketButton.addActionListener(e -> {
            basket.clear();
            refreshBasket();
            updateOrderEntryState();
        });

        placeOrderButton.addActionListener(e -> onPlaceOrder());

        actions.add(removeLastButton);
        actions.add(clearBasketButton);
        actions.add(placeOrderButton);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(orderScroll, BorderLayout.CENTER);
        center.add(totals, BorderLayout.SOUTH);

        outer.add(topInfo, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);
        outer.add(actions, BorderLayout.SOUTH);

        return outer;
    }

    private void refreshBasket() {
        if (basket.isEmpty()) {
            itemCountLabel.setText("Items in basket: 0");
            orderArea.setText(
                    "  Your order is empty.\n\n" +
                    "  Add products from the menu cards\n" +
                    "  in the middle column.\n\n" +
                    "  Suggested future images:\n" +
                    "  - coffee cup photos for beverages\n" +
                    "  - pastry / cake photos for food\n" +
                    "  - tea / snack icons for other items"
            );
            subtotalLabel.setText("Subtotal   £0.00");
            discountLabel.setText("Discount   £0.00 (none)");
            totalLabel.setText("TOTAL      £0.00");
            return;
        }

        itemCountLabel.setText("Items in basket: " + basket.size());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basket.size(); i++) {
            Item it = basket.get(i);
            sb.append(String.format("  %d. %-18s £%.2f%n", i + 1, it.getName(), it.getPrice()));
        }

        orderArea.setText(sb.toString());

        double subtotal = manager.calculateSubtotal(basket);
        double discount = manager.calculateDiscount(basket);
        double total    = manager.calculateFinalTotal(basket);

        subtotalLabel.setText(String.format("Subtotal   £%.2f", subtotal));
        if (discount > 0) {
            discountLabel.setText(String.format("Discount   -£%.2f (%s)",
                    discount, discountLabelText(basket, subtotal, discount)));
        } else {
            discountLabel.setText("Discount   £0.00 (none)");
        }
        totalLabel.setText(String.format("TOTAL      £%.2f", total));
    }

    // figures out which discount label to show
    private String discountLabelText(List<Item> items, double subtotal, double discount) {
        int bev = 0, food = 0;
        for (Item it : items) {
            if ("Beverage".equalsIgnoreCase(it.getCategory())) bev++;
            else if ("Food".equalsIgnoreCase(it.getCategory())) food++;
        }
        if (bev >= 1 && food >= 2 && Math.abs(discount - subtotal * 0.20) < 0.001) return "Combo";
        if (subtotal > 50.0 && Math.abs(discount - 5.0) < 0.001) return "Threshold";
        return "Applied";
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(CREAM_BG);
        panel.setBorder(new EmptyBorder(0, 16, 16, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel custLabel = new JLabel("Customer ID");
        custLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        custLabel.setForeground(TEXT_DARK);

        customerIdField = new JTextField(16);
        customerIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        customerIdField.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        customerIdField.setToolTipText("Example: CUST-001");

        JLabel hint = new JLabel("Format: CUST-001");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_SOFT);

        simulationPolicyLabel = new JLabel();
        simulationPolicyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        simulationPolicyLabel.setForeground(TEXT_SOFT);

        left.add(custLabel);
        left.add(customerIdField);
        left.add(hint);
        left.add(simulationPolicyLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton reportBtn = new JButton("Generate Report");
        JButton exitBtn   = new JButton("Exit");

        styleSecondaryButton(reportBtn);
        styleNeutralButton(exitBtn);

        reportBtn.addActionListener(e -> onGenerateReport());
        exitBtn.addActionListener(e -> onExit());

        right.add(reportBtn);
        right.add(exitBtn);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private void onPlaceOrder() {
        if (!simulationController.canAcceptNewOrders()) {
            JOptionPane.showMessageDialog(this,
                    "Order entry is locked because the simulation has already started.\n"
                            + "This run uses the placed orders that existed when Start was pressed.",
                    "Simulation Already Started",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (basket.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your basket is empty. Please add items first.",
                    "Empty Basket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerId = customerIdField.getText().trim();
        if (customerId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Customer ID before placing an order.",
                    "Missing Customer ID",
                    JOptionPane.WARNING_MESSAGE);
            customerIdField.requestFocus();
            return;
        }

        orderCounter++;
        String orderId = String.format("GUI-%03d", orderCounter);

        Order order = new Order(orderId, customerId, LocalDateTime.now());
        for (Item item : basket) {
            order.addItem(item);
            item.incrementOrderCount();
        }

        manager.applyDiscount(order);
        manager.addOrderFromGUI(order);

        double finalTotal = order.getFinalPrice();
        double disc = order.getDiscount();
        String discText = disc > 0
                ? String.format("Discount applied: -£%.2f%n", disc)
                : "No discount applied.%n";

        JOptionPane.showMessageDialog(this,
                String.format(
                        "Order %s placed successfully.%n%n%sTotal charged: £%.2f%n%nThank you, %s!",
                        orderId, discText, finalTotal, customerId
                ),
                "Order Confirmed",
                JOptionPane.INFORMATION_MESSAGE);

        basket.clear();
        customerIdField.setText("");
        refreshBasket();
        updateOrderEntryState();
    }

    @Override
    public void onSimulationUpdate() {
        SwingUtilities.invokeLater(this::updateOrderEntryState);
    }

    private void updateOrderEntryState() {
        boolean orderingOpen = simulationController.canAcceptNewOrders();

        customerIdField.setEnabled(orderingOpen);
        placeOrderButton.setEnabled(orderingOpen);
        removeLastButton.setEnabled(orderingOpen && !basket.isEmpty());
        clearBasketButton.setEnabled(orderingOpen && !basket.isEmpty());

        for (JButton btn : categoryButtons.values()) {
            btn.setEnabled(orderingOpen);
        }
        for (JButton btn : productAddButtons) {
            btn.setEnabled(orderingOpen);
        }

        if (simulationPolicyLabel != null) {
            if (orderingOpen) {
                simulationPolicyLabel.setText(
                        "Orders placed now will be included if you start the simulation.");
            } else {
                simulationPolicyLabel.setText(
                        "Order entry locked: the current simulation is using a fixed snapshot.");
            }
        }
    }

    private void onGenerateReport() {
        String report = manager.generateSalesReport();

        JTextArea area = new JTextArea(report, 26, 60);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(new Color(253, 251, 247));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(new LineBorder(BORDER_SOFT, 1, true));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Sales Report",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void onExit() {
        if (lifecycleManager.isShutdownInProgress()) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Generate sales report before exiting?",
                "Exit Application",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.CANCEL_OPTION) return;
        if (choice == JOptionPane.YES_OPTION) {
            onGenerateReport();
            System.out.println(manager.generateSalesReport());
        }
        lifecycleManager.shutdownApplication();
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new CompoundBorder(
                        new TitledBorder(
                                new EmptyBorder(0, 0, 0, 0),
                                title,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 16),
                                GREEN_DARK
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        ));
        return panel;
    }

    private JLabel makeInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(GREEN_MAIN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(GREEN_DARK, 1, true),
                new EmptyBorder(11, 14, 11, 14)
        ));
        btn.setOpaque(true);
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(GREEN_SOFT);
        btn.setForeground(GREEN_DARK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 210, 200), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        btn.setOpaque(true);
    }

    private void styleNeutralButton(JButton btn) {
        btn.setBackground(new Color(236, 232, 226));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        btn.setOpaque(true);
    }

    private void styleSoftActionButton(JButton btn) {
        btn.setBackground(new Color(244, 240, 234));
        btn.setForeground(BUTTON_EXIT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        btn.setOpaque(true);
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        String clean = s.trim();
        if (clean.length() <= max) return clean;
        return clean.substring(0, max - 3) + "...";
    }

    // inner class for product cards
    private class ProductCardPanel extends JPanel {
        private final Item item;

        ProductCardPanel(Item item) {
            this.item = item;
            build();
        }

        private void build() {
            setLayout(new BorderLayout(0, 10));
            setBackground(CARD_BG);
            setBorder(new CompoundBorder(
                    new LineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));

            JPanel imagePlaceholder = new JPanel(new BorderLayout());
            imagePlaceholder.setPreferredSize(new Dimension(100, 120));
            imagePlaceholder.setBackground(new Color(246, 243, 237));
            imagePlaceholder.setBorder(new CompoundBorder(
                    new LineBorder(new Color(229, 226, 220), 1, true),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            Path imageFile = ProjectPaths.resolveOptional(
                    "assets/images/products/" + item.getItemId() + ".png");
            JLabel imageLabel;
            if (Files.exists(imageFile)) {
                ImageIcon icon = new ImageIcon(imageFile.toAbsolutePath().toString());
                Image scaled = icon.getImage().getScaledInstance(130, 110, Image.SCALE_SMOOTH);
                imageLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
            } else {
                String placeholderText =
                        "<html><div style='text-align:center;'>"
                                + "[ IMAGE ]<br><br>"
                                + item.getItemId()
                                + "<br><br>"
                                + "Suggested file:<br>"
                                + "assets/images/products/" + item.getItemId() + ".png"
                                + "</div></html>";
                imageLabel = new JLabel(placeholderText, SwingConstants.CENTER);
                imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                imageLabel.setForeground(TEXT_SOFT);
            }
            imagePlaceholder.add(imageLabel, BorderLayout.CENTER);

            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(TEXT_DARK);

            JLabel categoryLabel = new JLabel(item.getCategory());
            categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            categoryLabel.setForeground(GREEN_DARK);
            categoryLabel.setOpaque(true);
            categoryLabel.setBackground(new Color(239, 246, 242));
            categoryLabel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(215, 231, 224), 1, true),
                    new EmptyBorder(3, 8, 3, 8)
            ));

            String description = item.getDescription() == null || item.getDescription().isBlank()
                    ? "No description available."
                    : item.getDescription();

            JLabel descLabel = new JLabel("<html><div style='width:200px;'>"
                    + shorten(description, 75)
                    + "</div></html>");
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLabel.setForeground(TEXT_SOFT);

            JLabel priceLabel = new JLabel(String.format("£%.2f", item.getPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            priceLabel.setForeground(GREEN_DARK);

            JButton addBtn = new JButton("Add to Basket");
            stylePrimaryButton(addBtn);
            addBtn.addActionListener(e -> {
                basket.add(item);
                refreshBasket();
                updateOrderEntryState();
            });
            productAddButtons.add(addBtn);
            addBtn.setEnabled(simulationController.canAcceptNewOrders());

            JPanel metaTop = new JPanel(new BorderLayout());
            metaTop.setOpaque(false);
            metaTop.add(nameLabel, BorderLayout.WEST);
            metaTop.add(categoryLabel, BorderLayout.EAST);

            JPanel textSection = new JPanel();
            textSection.setOpaque(false);
            textSection.setLayout(new BoxLayout(textSection, BoxLayout.Y_AXIS));
            textSection.add(metaTop);
            textSection.add(Box.createVerticalStrut(8));
            textSection.add(descLabel);
            textSection.add(Box.createVerticalStrut(10));
            textSection.add(priceLabel);

            add(imagePlaceholder, BorderLayout.NORTH);
            add(textSection, BorderLayout.CENTER);
            add(addBtn, BorderLayout.SOUTH);
        }
    }
}
