package simulation;

import log.EventLog;
import model.Item;
import model.Order;
import observer.StaffObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents one member of serving staff – runs as its own thread.
 *
 * <h3>Behaviour</h3>
 * <ol>
 *   <li>Calls {@link CustomerQueue#dequeue()} – blocks if the queue is empty.</li>
 *   <li>Processes each item in the order by sleeping for a random duration
 *       that depends on the item's category.</li>
 *   <li>Repeats until {@code dequeue()} returns {@code null} (queue closed
 *       and empty), then exits.</li>
 * </ol>
 *
 * <h3>Processing times (simulated)</h3>
 * <ul>
 *   <li>Beverage : 2 – 4 seconds</li>
 *   <li>Food     : 6 – 10 seconds</li>
 *   <li>Other    : 3 – 5 seconds</li>
 * </ul>
 *
 * <h3>Observer pattern</h3>
 * <p>Registered {@link StaffObserver}s are notified whenever this staff
 * member starts or finishes processing an order.</p>
 */
public class Staff implements Runnable {

    // Processing time ranges in milliseconds
    private static final int BEV_MIN   = 2000, BEV_MAX   = 4000;
    private static final int FOOD_MIN  = 6000, FOOD_MAX  = 10000;
    private static final int OTHER_MIN = 3000, OTHER_MAX = 5000;

    private final String        name;
    private final CustomerQueue queue;
    private final List<StaffObserver> observers = new ArrayList<>();
    private final Random        random = new Random();

    /** Speed multiplier – set via SimulationController to fast-forward the simulation. */
    private volatile double speedMultiplier = 1.0;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Creates a new Staff member.
     *
     * @param name  display name shown in the GUI (e.g. "Server 1")
     * @param queue the shared customer queue to pull orders from
     */
    public Staff(String name, CustomerQueue queue) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Staff name cannot be blank");
        if (queue == null)
            throw new IllegalArgumentException("CustomerQueue cannot be null");
        this.name  = name;
        this.queue = queue;
    }

    // ------------------------------------------------------------------ //
    //  Observer management                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Registers a {@link StaffObserver} to receive status updates.
     *
     * @param observer the observer to add (ignored if null)
     */
    public void addObserver(StaffObserver observer) {
        if (observer != null) observers.add(observer);
    }

    /** Notifies all registered observers of the current state. */
    private void notifyObservers(Order currentOrder, boolean idle) {
        for (StaffObserver obs : observers) {
            obs.onStaffStatusChanged(name, currentOrder, idle);
        }
    }

    // ------------------------------------------------------------------ //
    //  Runnable                                                             //
    // ------------------------------------------------------------------ //

    @Override
    public void run() {
        EventLog.getInstance().log(name + " is ready and waiting for orders.");
        notifyObservers(null, true);

        while (true) {
            Order order;
            try {
                order = queue.dequeue(); // blocks until an order is available or queue closes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                EventLog.getInstance().log(name + " was interrupted and is shutting down.");
                break;
            }

            if (order == null) {
                // Queue is closed and empty – this thread's work is done
                break;
            }

            // --- Begin processing ---
            notifyObservers(order, false);
            EventLog.getInstance().log(name + " started processing Order "
                    + order.getOrderId() + " for " + order.getCustomerId()
                    + " (" + order.getItems().size() + " item(s)).");

            try {
                for (Item item : order.getItems()) {
                    int baseMs = processingTimeMs(item.getCategory());
                    int sleepMs = (int) (baseMs / speedMultiplier);
                    Thread.sleep(sleepMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                EventLog.getInstance().log(name + " was interrupted mid-order.");
                notifyObservers(null, true);
                break;
            }

            // --- Order complete ---
            EventLog.getInstance().log(name + " completed Order "
                    + order.getOrderId() + " for " + order.getCustomerId()
                    + ". Total: £" + String.format("%.2f", order.getFinalPrice()) + ".");
            notifyObservers(null, true);
        }

        EventLog.getInstance().log(name + " has finished serving.");
        notifyObservers(null, true);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                              //
    // ------------------------------------------------------------------ //

    /**
     * Returns a random processing time (ms) for the given item category.
     *
     * @param category the item category string (case-insensitive)
     * @return milliseconds to sleep when processing one item of this type
     */
    private int processingTimeMs(String category) {
        if ("Beverage".equalsIgnoreCase(category)) {
            return BEV_MIN + random.nextInt(BEV_MAX - BEV_MIN + 1);
        } else if ("Food".equalsIgnoreCase(category)) {
            return FOOD_MIN + random.nextInt(FOOD_MAX - FOOD_MIN + 1);
        } else {
            return OTHER_MIN + random.nextInt(OTHER_MAX - OTHER_MIN + 1);
        }
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                            //
    // ------------------------------------------------------------------ //

    public String getName() { return name; }

    /**
     * Adjusts the simulation speed. A multiplier of 2.0 halves all sleep
     * durations, making the simulation run twice as fast.
     *
     * @param multiplier must be &gt; 0
     */
    public void setSpeedMultiplier(double multiplier) {
        if (multiplier <= 0) throw new IllegalArgumentException("Speed multiplier must be > 0");
        this.speedMultiplier = multiplier;
    }
}
