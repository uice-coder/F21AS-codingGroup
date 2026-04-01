package gui;

import model.Order;
import observer.ModelObserver;
import observer.QueueObserver;
import observer.StaffObserver;
import simulation.StaffStatus;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MVC Model for the Stage 2 simulation GUI.
 *
 * <h3>Design patterns used</h3>
 * <ul>
 *   <li><b>MVC Model</b> – holds all display state (queue snapshot + staff
 *       statuses). The View ({@link SimulationView}) queries this class and
 *       is notified via the ModelObserver interface when state changes.</li>
 *   <li><b>Observer (subject side)</b> – implements {@link QueueObserver}
 *       and {@link StaffObserver} so it can receive notifications from
 *       {@link simulation.CustomerQueue} and {@link simulation.Staff} running
 *       on background threads. It then forwards updates to all registered
 *       {@link ModelObserver}s on the Swing EDT.</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * <p>All state mutations are {@code synchronized}. Notifications to
 * {@link ModelObserver}s are dispatched via
 * {@link SwingUtilities#invokeLater} so the View can update Swing
 * components without risk of EDT violations.</p>
 */
public class SimulationModel implements QueueObserver, StaffObserver {

    // ---- Model state ---- //
    private List<Order>               queueSnapshot = new ArrayList<>();
    private final Map<String, StaffStatus> staffStatuses = new LinkedHashMap<>();

    /** True once the simulation has finished and all staff threads have exited. */
    private boolean simulationComplete = false;

    /** Final sales report text – populated on simulation end. */
    private String  finalReport = "";

    // ---- Observers (the View) ---- //
    private final List<ModelObserver> observers = new ArrayList<>();

    // ------------------------------------------------------------------ //
    //  Observer registration                                                //
    // ------------------------------------------------------------------ //

    /**
     * Registers a {@link ModelObserver} (typically the View) to receive
     * model update notifications.
     *
     * @param observer the observer to add (ignored if null)
     */
    public synchronized void addObserver(ModelObserver observer) {
        if (observer != null) observers.add(observer);
    }

    // ------------------------------------------------------------------ //
    //  QueueObserver implementation                                         //
    // ------------------------------------------------------------------ //

    /**
     * Called by {@link simulation.CustomerQueue} (on a background thread)
     * whenever the queue changes.
     */
    @Override
    public synchronized void onQueueChanged(List<Order> snapshot) {
        this.queueSnapshot = new ArrayList<>(snapshot);
        dispatchToEDT();
    }

    // ------------------------------------------------------------------ //
    //  StaffObserver implementation                                         //
    // ------------------------------------------------------------------ //

    /**
     * Called by {@link simulation.Staff} (on that staff's background thread)
     * whenever a staff member starts or finishes an order.
     */
    @Override
    public synchronized void onStaffStatusChanged(String staffName,
                                                   Order currentOrder,
                                                   boolean idle) {
        staffStatuses.put(staffName, new StaffStatus(staffName, currentOrder, idle));
        dispatchToEDT();
    }

    // ------------------------------------------------------------------ //
    //  Simulation-end notification                                          //
    // ------------------------------------------------------------------ //

    /**
     * Called by {@link simulation.SimulationController} (on the monitor thread)
     * when all orders have been processed.
     *
     * @param report the final sales report text
     */
    public synchronized void onSimulationComplete(String report) {
        this.simulationComplete = true;
        this.finalReport        = report;
        dispatchToEDT();
    }

    // ------------------------------------------------------------------ //
    //  EDT dispatch                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Posts a notification to all registered {@link ModelObserver}s on the
     * Swing Event Dispatch Thread.
     *
     * <p>Must be called from within a {@code synchronized} block (this method
     * takes a snapshot of mutable state before leaving the lock).</p>
     */
    private void dispatchToEDT() {
        // Capture snapshots while still holding the lock
        final List<Order>               qSnap  = new ArrayList<>(queueSnapshot);
        final Map<String, StaffStatus>  sSnap  = new LinkedHashMap<>(staffStatuses);
        final List<ModelObserver>       obsSnap = new ArrayList<>(observers);
        final boolean                   done   = simulationComplete;
        final String                    report = finalReport;

        // Post to EDT – invokeLater returns immediately (no deadlock risk)
        SwingUtilities.invokeLater(() -> {
            for (ModelObserver obs : obsSnap) {
                obs.onModelUpdated(qSnap, sSnap);
                if (done) obs.onSimulationComplete(report);
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Read-only accessors for the View                                     //
    // ------------------------------------------------------------------ //

    /** @return current snapshot of the customer queue (front at index 0) */
    public synchronized List<Order> getQueueSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(queueSnapshot));
    }

    /** @return map of staff name → status (insertion-ordered) */
    public synchronized Map<String, StaffStatus> getStaffStatuses() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(staffStatuses));
    }

    /** @return {@code true} if the simulation has finished */
    public synchronized boolean isSimulationComplete() { return simulationComplete; }
}
