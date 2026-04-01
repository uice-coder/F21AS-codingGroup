package gui;

import model.Item;
import model.Order;
import observer.ModelObserver;
import simulation.SimulationController;
import simulation.StaffStatus;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Stage 2 simulation GUI – the MVC View.
 *
 * <h3>Layout</h3>
 * <pre>
 *  NORTH  – title bar
 *  CENTER – left: customer queue | right: staff panels
 *  SOUTH  – speed controls + status bar
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><b>MVC View</b> – displays state held by {@link SimulationModel};
 *       never modifies the model directly.</li>
 *   <li><b>Observer</b> – implements {@link ModelObserver}; receives update
 *       callbacks from {@link SimulationModel} (always on the EDT).</li>
 * </ul>
 *
 * <p>All Swing updates happen on the EDT because
 * {@link SimulationModel} dispatches via {@link SwingUtilities#invokeLater}.</p>
 */
public class SimulationView extends JFrame implements ModelObserver {

    // ---- Colours ---- //
    private static final Color BG_MAIN   = new Color(245, 235, 220);
    private static final Color BG_PANEL  = new Color(252, 248, 240);
    private static final Color BG_SOUTH  = new Color(235, 215, 185);
    private static final Color BORDER_C  = new Color(160, 100, 40);
    private static final Color TITLE_C   = new Color(80,  40,  10);
    private static final Color GREEN     = new Color(30,  130,  30);

    // ---- Controller (for speed control) ---- //
    private final SimulationController controller;

    // ---- Queue panel components ---- //
    private JLabel    queueCountLabel;
    private JTextArea queueArea;

    // ---- Staff panel ---- //
    private JPanel    staffContainer;   // holds one StaffPanel per server

    // ---- Status bar ---- //
    private JLabel    statusLabel;

    // ---- Speed slider ---- //
    private JSlider   speedSlider;
    private JLabel    speedValueLabel;

    // ------------------------------------------------------------------ //
    //  Construction                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Creates and displays the simulation window.
     *
     * @param model      the MVC model (this view registers itself as observer)
     * @param controller the simulation controller (used for speed control)
     */
    public SimulationView(SimulationModel model, SimulationController controller) {
        this.controller = controller;
        model.addObserver(this);
        initUI();
        setVisible(true);
    }

    // ------------------------------------------------------------------ //
    //  UI construction                                                      //
    // ------------------------------------------------------------------ //

    private void initUI() {
        setTitle("Coffee Shop Simulation – Stage 2");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG_MAIN);

        add(buildTitleBar(),   BorderLayout.NORTH);
        add(buildCentrePanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);
    }

    // ---- Title bar ---- //
    private JLabel buildTitleBar() {
        JLabel lbl = new JLabel("Coffee Shop Simulation", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl.setForeground(TITLE_C);
        lbl.setBorder(new EmptyBorder(14, 10, 8, 10));
        return lbl;
    }

    // ---- Centre: queue + staff split pane ---- //
    private JSplitPane buildCentrePanel() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildQueuePanel(),
                buildStaffPanel());
        split.setDividerLocation(380);
        split.setResizeWeight(0.38);
        split.setBorder(new EmptyBorder(4, 10, 4, 10));
        return split;
    }

    // ---- Queue panel ---- //
    private JPanel buildQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(BG_PANEL);
        panel.setBorder(titledBorder("Customer Queue"));

        queueCountLabel = new JLabel("No customers waiting.");
        queueCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        queueCountLabel.setForeground(TITLE_C);
        queueCountLabel.setBorder(new EmptyBorder(4, 6, 4, 6));

        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        queueArea.setBackground(new Color(255, 253, 245));
        queueArea.setText("  (simulation not yet started)");

        panel.add(queueCountLabel,          BorderLayout.NORTH);
        panel.add(new JScrollPane(queueArea), BorderLayout.CENTER);
        return panel;
    }

    // ---- Staff panel ---- //
    private JPanel buildStaffPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_PANEL);
        wrapper.setBorder(titledBorder("Serving Staff"));

        staffContainer = new JPanel();
        staffContainer.setLayout(new BoxLayout(staffContainer, BoxLayout.Y_AXIS));
        staffContainer.setBackground(BG_PANEL);

        JScrollPane scroll = new JScrollPane(staffContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_PANEL);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ---- South panel: speed control + status bar ---- //
    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 4));
        panel.setBackground(BG_SOUTH);
        panel.setBorder(new EmptyBorder(6, 12, 10, 12));

        // Speed controls
        JPanel speedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        speedRow.setBackground(BG_SOUTH);
        JLabel speedLbl = new JLabel("Simulation Speed:");
        speedLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Slider: 1 = 0.5×, 2 = 1× (normal), 4 = 2×, 8 = 4×
        speedSlider = new JSlider(1, 8, 2);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.setPaintTicks(true);
        speedSlider.setBackground(BG_SOUTH);
        speedSlider.setPreferredSize(new Dimension(200, 40));

        speedValueLabel = new JLabel("1.0x");
        speedValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        speedValueLabel.setPreferredSize(new Dimension(40, 20));

        speedSlider.addChangeListener(e -> {
            double multiplier = speedSlider.getValue() / 2.0;
            speedValueLabel.setText(multiplier + "x");
            if (!speedSlider.getValueIsAdjusting()) {
                controller.setSpeedMultiplier(multiplier);
            }
        });

        speedRow.add(speedLbl);
        speedRow.add(speedSlider);
        speedRow.add(speedValueLabel);
        panel.add(speedRow, BorderLayout.NORTH);

        // Status bar
        statusLabel = new JLabel("Simulation starting...");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(80, 80, 80));
        statusLabel.setBorder(new EmptyBorder(4, 2, 0, 2));
        panel.add(statusLabel, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  ModelObserver implementation (called on EDT)                         //
    // ------------------------------------------------------------------ //

    /**
     * Updates the queue display and all staff panels.
     * Called on the Swing EDT by {@link SimulationModel}.
     */
    @Override
    public void onModelUpdated(List<Order> queue, Map<String, StaffStatus> staffStatuses) {
        refreshQueuePanel(queue);
        refreshStaffPanel(staffStatuses);
    }

    /**
     * Called when the simulation has ended. Shows the final report and
     * offers to exit.
     */
    @Override
    public void onSimulationComplete(String finalReport) {
        statusLabel.setText("Simulation complete. All orders processed.");
        statusLabel.setForeground(GREEN);

        // Show final report in a scrollable dialog
        JTextArea reportArea = new JTextArea(finalReport, 24, 60);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        int choice = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(reportArea),
                "Simulation Complete – Sales Report",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    // ------------------------------------------------------------------ //
    //  Refresh helpers                                                      //
    // ------------------------------------------------------------------ //

    /** Rebuilds the queue text area from the current queue snapshot. */
    private void refreshQueuePanel(List<Order> queue) {
        int n = queue.size();
        if (n == 0) {
            queueCountLabel.setText("Queue is empty.");
            queueArea.setText("  (no customers waiting)");
        } else {
            queueCountLabel.setText("There are currently " + n
                    + " customer" + (n == 1 ? "" : "s") + " waiting in the queue:");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < queue.size(); i++) {
                Order o = queue.get(i);
                int items = o.getItems().size();
                sb.append(String.format("  %2d. %-16s %d item%s%n",
                        i + 1,
                        o.getCustomerId(),
                        items,
                        items == 1 ? "" : "s"));
            }
            queueArea.setText(sb.toString());
        }
    }

    /**
     * Rebuilds the staff container to reflect the current set of staff statuses.
     * Adds a {@link StaffPanel} for any new staff member; updates existing ones.
     */
    private void refreshStaffPanel(Map<String, StaffStatus> staffStatuses) {
        // Simple approach: rebuild all panels each update
        staffContainer.removeAll();
        for (StaffStatus status : staffStatuses.values()) {
            staffContainer.add(new StaffPanel(status));
            staffContainer.add(Box.createVerticalStrut(6));
        }
        staffContainer.revalidate();
        staffContainer.repaint();
    }

    // ------------------------------------------------------------------ //
    //  Inner class: StaffPanel                                              //
    // ------------------------------------------------------------------ //

    /**
     * A small panel showing one staff member's current state.
     *
     * <pre>
     *  ┌─────────────────────────────────────────┐
     *  │ Server 1                   [PROCESSING] │
     *  │ Order: ORD-003 | CUST-003               │
     *  │   COF-003  Cappuccino          £3.80     │
     *  │   FOO-001  Croissant           £2.50     │
     *  │ Total: £6.30                            │
     *  └─────────────────────────────────────────┘
     * </pre>
     */
    private static class StaffPanel extends JPanel {

        StaffPanel(StaffStatus status) {
            setLayout(new BorderLayout(4, 2));
            setBackground(BG_PANEL);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_C, 1),
                    new EmptyBorder(6, 8, 6, 8)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

            // Header row: name + status badge
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BG_PANEL);

            JLabel nameLbl = new JLabel(status.getStaffName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameLbl.setForeground(TITLE_C);

            JLabel badge = status.isIdle()
                    ? makeBadge("IDLE",       new Color(130, 130, 130))
                    : makeBadge("PROCESSING", GREEN);

            header.add(nameLbl, BorderLayout.WEST);
            header.add(badge,   BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            // Order detail
            JTextArea detail = new JTextArea();
            detail.setEditable(false);
            detail.setFont(new Font("Monospaced", Font.PLAIN, 12));
            detail.setBackground(BG_PANEL);
            detail.setBorder(null);

            Order order = status.getCurrentOrder();
            if (order == null) {
                detail.setText("  Waiting for next order...");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("  Order: %-10s  Customer: %s%n",
                        order.getOrderId(), order.getCustomerId()));
                for (Item item : order.getItems()) {
                    sb.append(String.format("    %-8s %-22s £%.2f%n",
                            item.getItemId(), item.getName(), item.getPrice()));
                }
                double disc = order.getDiscount();
                if (disc > 0) {
                    sb.append(String.format("  Subtotal: £%.2f  Discount: -£%.2f%n",
                            order.getSubtotal(), disc));
                    sb.append(String.format("  Total:    £%.2f%n", order.getFinalPrice()));
                } else {
                    sb.append(String.format("  Total: £%.2f  (no discount)%n",
                            order.getFinalPrice()));
                }
                detail.setText(sb.toString());
            }
            add(detail, BorderLayout.CENTER);
        }

        private static JLabel makeBadge(String text, Color bg) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(bg);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
            return lbl;
        }
    }

    // ------------------------------------------------------------------ //
    //  Utility                                                              //
    // ------------------------------------------------------------------ //

    private static TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_C, 2),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                TITLE_C);
    }
}
