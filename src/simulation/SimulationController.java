package simulation;

import log.EventLog;
import model.Manager;
import model.Order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the Stage 2 simulation.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates and owns the {@link CustomerQueue}.</li>
 *   <li>Starts the {@link OrderProducer} thread and all {@link Staff} threads.</li>
 *   <li>Runs a monitor thread that waits for all Staff to finish, then triggers
 *       shutdown (report generation + event log file write).</li>
 *   <li>Provides a {@link #setSpeedMultiplier(double)} method so the GUI can
 *       adjust simulation speed at runtime (extension requirement).</li>
 * </ul>
 */
public class SimulationController {

    // ---- Configuration defaults ---- //
    /** Default delay between customer arrivals (ms). */
    public static final int DEFAULT_ARRIVAL_INTERVAL_MS = 3000;
    /** Default number of serving staff. */
    public static final int DEFAULT_STAFF_COUNT         = 3;

    // ---- Core objects ---- //
    private final CustomerQueue   queue;
    private final Manager         manager;
    private final OrderProducer   producer;
    private final List<Staff>     staffList = new ArrayList<>();
    private final List<Thread>    staffThreads = new ArrayList<>();

    /** Callback invoked on the monitor thread when the simulation ends. */
    private Runnable onSimulationComplete;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Builds the simulation with default settings (3 staff, 3s arrival interval).
     *
     * @param manager    the Stage 1 Manager (provides pre-loaded orders and
     *                   generates the final sales report)
     * @param staffCount number of serving staff threads to create
     * @param arrivalIntervalMs delay between consecutive customer arrivals
     */
    public SimulationController(Manager manager, int staffCount, int arrivalIntervalMs) {
        if (manager == null)  throw new IllegalArgumentException("Manager cannot be null");
        if (staffCount < 1)   throw new IllegalArgumentException("Must have at least 1 staff member");

        this.manager = manager;
        this.queue   = new CustomerQueue();

        // Create staff threads
        for (int i = 1; i <= staffCount; i++) {
            Staff staff = new Staff("Server " + i, queue);
            staffList.add(staff);
        }

        // Create producer – uses orders already loaded by Manager
        List<Order> orders = new ArrayList<>(manager.getOrders());
        this.producer = new OrderProducer(orders, queue, arrivalIntervalMs);
    }

    // ------------------------------------------------------------------ //
    //  Observer wiring                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Registers a {@link observer.QueueObserver} on the shared queue.
     * Call before {@link #start()}.
     */
    public void addQueueObserver(observer.QueueObserver obs) {
        queue.addObserver(obs);
    }

    /**
     * Registers a {@link observer.StaffObserver} on every Staff thread.
     * Call before {@link #start()}.
     */
    public void addStaffObserver(observer.StaffObserver obs) {
        for (Staff staff : staffList) staff.addObserver(obs);
    }

    /**
     * Sets a callback to be invoked (on the monitor thread) when the
     * simulation finishes – i.e. all staff threads have exited.
     */
    public void setOnSimulationComplete(Runnable callback) {
        this.onSimulationComplete = callback;
    }

    // ------------------------------------------------------------------ //
    //  Start / Stop                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Starts the simulation:
     * <ol>
     *   <li>Launches the OrderProducer thread.</li>
     *   <li>Launches one thread per Staff member.</li>
     *   <li>Launches a monitor thread that joins all Staff threads and then
     *       calls {@link #shutdown()}.</li>
     * </ol>
     */
    public void start() {
        EventLog.getInstance().log("=== Simulation starting with "
                + staffList.size() + " server(s) ===");

        // Start staff threads
        for (Staff staff : staffList) {
            Thread t = new Thread(staff, staff.getName());
            staffThreads.add(t);
            t.start();
        }

        // Start producer thread
        Thread producerThread = new Thread(producer, "OrderProducer");
        producerThread.start();

        // Monitor thread – waits for all staff to finish then triggers shutdown
        Thread monitor = new Thread(() -> {
            for (Thread t : staffThreads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            shutdown();
        }, "SimulationMonitor");
        monitor.setDaemon(true);
        monitor.start();
    }

    /** Called by the monitor thread once all Staff threads have exited. */
    private void shutdown() {
        EventLog.getInstance().log("=== All orders processed. Coffee shop closing. ===");

        // Print sales report to console
        String report = manager.generateSalesReport();
        System.out.println(report);
        EventLog.getInstance().log("Sales report generated.");

        // Write event log to file
        try {
            EventLog.getInstance().writeToFile("data/simulation_log.txt");
        } catch (IOException e) {
            System.err.println("[SimulationController] Could not write event log: " + e.getMessage());
        }

        // Notify the GUI that the simulation has ended
        if (onSimulationComplete != null) {
            onSimulationComplete.run();
        }
    }

    // ------------------------------------------------------------------ //
    //  Runtime controls (extension: simulation speed)                      //
    // ------------------------------------------------------------------ //

    /**
     * Changes the speed of the simulation at runtime.
     *
     * <p>A multiplier of 1.0 is real-time; 2.0 is double speed; 0.5 is half speed.
     * Propagated immediately to both the OrderProducer and all Staff threads.</p>
     *
     * @param multiplier must be &gt; 0
     */
    public void setSpeedMultiplier(double multiplier) {
        producer.setSpeedMultiplier(multiplier);
        for (Staff staff : staffList) staff.setSpeedMultiplier(multiplier);
        EventLog.getInstance().log("Simulation speed set to " + multiplier + "x.");
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                            //
    // ------------------------------------------------------------------ //

    public CustomerQueue  getQueue()     { return queue; }
    public List<Staff>    getStaffList() { return staffList; }
    public Manager        getManager()  { return manager; }
}
