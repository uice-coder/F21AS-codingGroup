package simulation;

import model.Order;
import java.util.List;

/**
 * Producer thread: adds orders to the CustomerQueue at intervals.
 */
public class CustomerProducer extends Thread {

    private final List<Order> orders;
    private final CustomerQueue queue;
    private volatile int speedMultiplier = 1;

    public CustomerProducer(List<Order> orders, CustomerQueue queue) {
        super("CustomerProducer");
        this.orders = orders;
        this.queue = queue;
        setDaemon(true);
    }

    @Override
    public void run() {
        for (Order order : orders) {
            if (isInterrupted()) break;
            queue.enqueue(order);
            try {
                long delay = (1500 + (long)(Math.random() * 1000)) / Math.max(1, speedMultiplier);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        queue.close();
        SimulationLog.getInstance().log("All customers have joined the queue. Queue now closed.");
    }

    public void setSpeedMultiplier(int m) { this.speedMultiplier = m; }
}
