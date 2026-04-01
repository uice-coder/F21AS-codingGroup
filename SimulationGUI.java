package gui;

import model.Item;
import model.Order;
import simulation.SimulationController;
import simulation.SimulationLog;
import simulation.SimulationObserver;
import simulation.ServerWorker;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Stage 2 simulation window (MVC View).
 * Implements SimulationObserver (Observer pattern) — gets called by
 * SimulationController whenever simulation state changes.
 *
 * Layout:
 *   West   — Customer Queue panel
 *   Centre — Staff Status panels (one card per server)
 *   East   — Activity Log panel
 *   South  — Control bar (start, add/remove staff, speed)
 */
public class SimulationGUI extends JFrame implements SimulationObserver {

    private static final Color GREEN_DARK  = new Color(0, 98, 65);
    private static final Color GREEN_MAIN  = new Color(0, 112, 74);
    private static final Color GREEN_SOFT  = new Color(212, 233, 226);
    private static final Color CREAM_BG    = new Color(248, 245, 240);
    private static final Color CARD_BG     = new Color(255, 252, 248);
    private static final Color TEXT_DARK   = new Color(40, 40, 40);
    private static final Color TEXT_SOFT   = new Color(110, 110, 110);
    private static final Color BORDER_SOFT = new Color(223, 223, 218);

    private final SimulationController controller;

    private JPanel   staffPanel;
    private JTextArea queueArea;
    private JTextArea logArea;
    private JLabel   statusLabel;
    private JLabel   queueSizeLabel;
    private JButton  startBtn;
    private JButton  addStaffBtn;
    private JButton  removeStaffBtn;

    public SimulationGUI(SimulationController controller) {
        this.controller = controller;
        controller.addObserver(this);
        initUI();
        setVisible(true);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void initUI() {
        setTitle("Coffee Shop Simulation – Stage 2");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(CREAM_BG);

        add(buildBanner(),      BorderLayout.NORTH);
        add(buildMainPanel(),   BorderLayout.CENTER);
        add(buildControlBar(),  BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                onClose();
            }
        });
    }

    private JPanel buildBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GREEN_DARK);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Green Roast Coffee — Live Simulation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(220, 240, 230));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(title,       BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel buildMainPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 14, 0, 14));

        // West: queue panel
        JPanel queueCard = buildCard("Customer Queue");
        queueCard.setPreferredSize(new Dimension(280, 100));

        queueSizeLabel = new JLabel("Waiting: 0");
        queueSizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        queueSizeLabel.setForeground(GREEN_DARK);

        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        queueArea.setBackground(new Color(253, 251, 247));
        queueArea.setText("  (not started)");

        JScrollPane qs = new JScrollPane(queueArea);
        qs.setBorder(new LineBorder(BORDER_SOFT, 1));

        queueCard.add(queueSizeLabel, BorderLayout.NORTH);
        queueCard.add(qs,             BorderLayout.CENTER);
        p.add(queueCard, BorderLayout.WEST);

        // Centre: staff status
        staffPanel = new JPanel();
        staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
        staffPanel.setOpaque(false);

        JScrollPane staffScroll = new JScrollPane(staffPanel);
        staffScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        staffScroll.getViewport().setOpaque(false);
        staffScroll.setOpaque(false);

        JPanel staffCard = buildCard("Staff Status");
        staffCard.add(staffScroll, BorderLayout.CENTER);
        p.add(staffCard, BorderLayout.CENTER);

        // East: log panel
        JPanel logCard = buildCard("Activity Log");
        logCard.setPreferredSize(new Dimension(300, 100));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(253, 251, 247));
        logArea.setText("  (log will appear here)\n");

        JScrollPane ls = new JScrollPane(logArea);
        ls.setBorder(new LineBorder(BORDER_SOFT, 1));
        logCard.add(ls, BorderLayout.CENTER);
        p.add(logCard, BorderLayout.EAST);

        return p;
    }

    private JPanel buildControlBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.setBackground(CREAM_BG);
        bar.setBorder(new EmptyBorder(4, 14, 10, 14));

        startBtn = makeBtn("▶  Start Simulation", GREEN_MAIN, Color.WHITE);
        startBtn.addActionListener(e -> onStart());

        addStaffBtn = makeBtn("+ Add Server", GREEN_SOFT, GREEN_DARK);
        addStaffBtn.setEnabled(false);
        addStaffBtn.addActionListener(e -> controller.addStaff());

        removeStaffBtn = makeBtn("− Remove Server", new Color(240, 230, 220), new Color(160, 80, 40));
        removeStaffBtn.setEnabled(false);
        removeStaffBtn.addActionListener(e -> controller.removeStaff());

        JLabel staffLabel = new JLabel("  Initial servers:");
        staffLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        SpinnerNumberModel staffModel = new SpinnerNumberModel(2, 1, 8, 1);
        JSpinner staffSpinner = new JSpinner(staffModel);
        staffSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        staffSpinner.addChangeListener(e ->
                controller.setNumStaff((int) staffSpinner.getValue()));

        JLabel speedLabel = new JLabel("  Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Speed options: 0.5×, 1×, 2×, 3×  — all correctly scaled
        JComboBox<String> speedBox = new JComboBox<>(
                new String[]{"Normal (1×)", "Fast (2×)", "Faster (3×)", "Slow (0.5×)"});
        speedBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        speedBox.addActionListener(e -> {
            int idx = speedBox.getSelectedIndex();
            if      (idx == 0) controller.setSpeed(1.0);
            else if (idx == 1) controller.setSpeed(2.0);
            else if (idx == 2) controller.setSpeed(3.0);
            else               controller.setSpeed(0.5); // genuinely half speed
        });

        bar.add(startBtn);
        bar.add(addStaffBtn);
        bar.add(removeStaffBtn);
        bar.add(staffLabel);
        bar.add(staffSpinner);
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        bar.add(speedLabel);
        bar.add(speedBox);

        return bar;
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    private void onStart() {
        if (controller.isRunning()) return;
        startBtn.setEnabled(false);
        addStaffBtn.setEnabled(true);
        removeStaffBtn.setEnabled(true);
        statusLabel.setText("Status: Running...");
        controller.startSimulation();
    }

    private void onClose() {
        SimulationLog.getInstance().writeToFile("simulation_log.txt");
        int choice = JOptionPane.showConfirmDialog(this,
                "Exit simulation?", "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    // ── Observer callback ─────────────────────────────────────────────────────

    @Override
    public void onSimulationUpdate() {
        // Called on EDT via SwingUtilities.invokeLater in SimulationController
        refreshStaffPanel();
        refreshQueuePanel();
        refreshLog();

        if (!controller.isRunning()
                && controller.getQueue().isClosed()
                && controller.getQueue().isEmpty()) {
            statusLabel.setText("Status: Simulation Complete");
            addStaffBtn.setEnabled(false);
            removeStaffBtn.setEnabled(false);
        }
    }

    // ── Panel refresh helpers ─────────────────────────────────────────────────

    private void refreshStaffPanel() {
        staffPanel.removeAll();
        List<ServerWorker> members = controller.getStaff();
        if (members.isEmpty()) {
            JLabel lbl = new JLabel("  No servers yet. Press Start.");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(TEXT_SOFT);
            staffPanel.add(lbl);
        }
        for (ServerWorker s : members) {
            staffPanel.add(buildStaffCard(s));
            staffPanel.add(Box.createVerticalStrut(10));
        }
        staffPanel.revalidate();
        staffPanel.repaint();
    }

    private JPanel buildStaffCard(ServerWorker s) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel header = new JLabel("Server " + s.getStaffId());
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setForeground(GREEN_DARK);

        Color statusColor;
        String statusText;
        switch (s.getStatus()) {
            case PROCESSING:
                statusColor = new Color(0, 150, 80);
                statusText  = "● PROCESSING";
                break;
            case IDLE:
                statusColor = new Color(120, 120, 120);
                statusText  = "○ IDLE";
                break;
            default:
                statusColor = new Color(180, 60, 60);
                statusText  = "✗ DONE";
                break;
        }
        JLabel statusLbl = new JLabel(statusText);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLbl.setForeground(statusColor);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.WEST);
        top.add(statusLbl, BorderLayout.EAST);

        JTextArea details = new JTextArea(3, 30);
        details.setEditable(false);
        details.setFont(new Font("Consolas", Font.PLAIN, 12));
        details.setBackground(new Color(250, 248, 244));
        details.setBorder(new EmptyBorder(6, 8, 6, 8));

        Order cur = s.getCurrentOrder();
        if (cur != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Order: ").append(cur.getOrderId())
              .append("  Customer: ").append(cur.getCustomerId()).append("\n");
            for (Item item : cur.getItems()) {
                sb.append("  • ").append(item.getName())
                  .append(String.format(" (£%.2f)", item.getPrice())).append("\n");
            }
            sb.append(String.format("  Total: £%.2f", cur.getFinalPrice()));
            details.setText(sb.toString());
        } else {
            details.setText(s.getStatus() == ServerWorker.Status.DONE
                    ? "  Finished all work." : "  Waiting for next order...");
        }

        card.add(top,     BorderLayout.NORTH);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private void refreshQueuePanel() {
        List<Order> snapshot = controller.getQueue().snapshot();
        int size = snapshot.size();
        queueSizeLabel.setText("Waiting: " + size
                + (controller.getQueue().isClosed() ? " (closed)" : ""));

        if (snapshot.isEmpty()) {
            queueArea.setText("  Queue is empty.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(size).append(" customer(s) waiting:\n\n");
        for (Order o : snapshot) {
            sb.append(String.format("  %-14s  %d item(s)%n",
                    o.getCustomerId(), o.getItems().size()));
        }
        queueArea.setText(sb.toString());
    }

    private void refreshLog() {
        List<String> entries = SimulationLog.getInstance().getEntries();
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, entries.size() - 80);
        for (int i = start; i < entries.size(); i++) {
            sb.append(entries.get(i)).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel buildCard(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                new CompoundBorder(
                        new TitledBorder(new EmptyBorder(0, 0, 0, 0), title,
                                TitledBorder.LEFT, TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 15), GREEN_DARK),
                        new EmptyBorder(10, 10, 10, 10))));
        return p;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 18, 10, 18)));
        b.setOpaque(true);
        return b;
    }
}
