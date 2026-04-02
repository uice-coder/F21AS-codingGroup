package simulation;

import model.Order;
import java.util.List;

/**
 * Producer thread: adds orders to the CustomerQueue at intervals.
 */
public class CustomerProducer extends Thread {

    private final List<Order> orders;
    private final CustomerQueue queue;
    private volatile double speedMultiplier = 1.0;

    public CustomerProducer(List<Order> orders, CustomerQueue queue) {
        super("CustomerProducer");
        this.orders = orders;
        this.queue = queue;
    }

    @Override
    public void run() {
        for (Order order : orders) {
            if (isInterrupted()) break;
            queue.enqueue(order);
            try {
                long baseSleepTime = 1500 + (long) (Math.random() * 1000);
                long delay = Math.max(1L,
                        Math.round(baseSleepTime / Math.max(0.1, speedMultiplier)));
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        queue.close();
        SimulationLog.getInstance().log("All customers have joined the queue. Queue now closed.");
    }

    public void setSpeedMultiplier(double m) { this.speedMultiplier = m; }
}
