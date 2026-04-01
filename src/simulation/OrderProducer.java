package simulation;

import log.EventLog;
import model.Order;

import java.util.List;

/**
 * Producer thread that feeds pre-loaded orders into the {@link CustomerQueue}
 * one at a time, simulating customers arriving at the coffee shop.
 *
 * <h3>Behaviour</h3>
 * <ol>
 *   <li>Sleeps for {@code arrivalIntervalMs} milliseconds.</li>
 *   <li>Enqueues the next order into the shared {@link CustomerQueue}.</li>
 *   <li>Repeats until all orders have been added.</li>
 *   <li>Calls {@link CustomerQueue#close()} to signal that no further orders
 *       will arrive, allowing Staff threads to exit when the queue empties.</li>
 * </ol>
 *
 * <p>The arrival interval can be scaled via the {@link #setSpeedMultiplier(double)}
 * method (extension: runtime speed control).</p>
 */
public class OrderProducer implements Runnable {

    private final List<Order>  orders;
    private final CustomerQueue queue;

    /** Base delay between customer arrivals in milliseconds. */
    private final int arrivalIntervalMs;

    /** Speed multiplier – shared with Staff threads for consistent fast-forward. */
    private volatile double speedMultiplier = 1.0;

    // ------------------------------------------------------------------ //
    //  Constructor                                                          //
    // ------------------------------------------------------------------ //

    /**
     * @param orders              ordered list of pre-loaded orders to enqueue
     * @param queue               the shared customer queue
     * @param arrivalIntervalMs   base delay (ms) between consecutive arrivals
     */
    public OrderProducer(List<Order> orders, CustomerQueue queue, int arrivalIntervalMs) {
        if (orders == null) throw new IllegalArgumentException("Orders list cannot be null");
        if (queue  == null) throw new IllegalArgumentException("CustomerQueue cannot be null");
        if (arrivalIntervalMs < 0)
            throw new IllegalArgumentException("Arrival interval must be >= 0");

        this.orders             = orders;
        this.queue              = queue;
        this.arrivalIntervalMs  = arrivalIntervalMs;
    }

    // ------------------------------------------------------------------ //
    //  Runnable                                                             //
    // ------------------------------------------------------------------ //

    @Override
    public void run() {
        EventLog.getInstance().log("OrderProducer started – will add "
                + orders.size() + " order(s) to the queue.");

        for (Order order : orders) {
            try {
                int sleepMs = (int) (arrivalIntervalMs / speedMultiplier);
                if (sleepMs > 0) Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                EventLog.getInstance().log("OrderProducer interrupted – stopping early.");
                break;
            }
            queue.enqueue(order);
        }

        queue.close();
    }

    // ------------------------------------------------------------------ //
    //  Accessor                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Adjusts the arrival rate. A multiplier of 2.0 halves the inter-arrival
     * delay, so customers arrive twice as quickly.
     *
     * @param multiplier must be &gt; 0
     */
    public void setSpeedMultiplier(double multiplier) {
        if (multiplier <= 0) throw new IllegalArgumentException("Speed multiplier must be > 0");
        this.speedMultiplier = multiplier;
    }
}
