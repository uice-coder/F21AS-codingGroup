package simulation;

import model.Order;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

/**
 * Thread-safe queue of customer orders.
 * Uses wait/notify for producer-consumer coordination.
 */
public class CustomerQueue {

    private final Queue<Order> queue = new LinkedList<>();
    private final Runnable onQueueChanged;
    private boolean closed = false;

    public CustomerQueue(Runnable onQueueChanged) {
        this.onQueueChanged = onQueueChanged;
    }

    public synchronized void enqueue(Order order) {
        queue.add(order);
        SimulationLog.getInstance().log("Customer " + order.getCustomerId()
                + " joined the queue (Order: " + order.getOrderId() + ", "
                + order.getItems().size() + " item(s))");
        notifyAll();
        fireQueueChanged();
    }

    /**
     * Blocks until an order is available, or returns null if closed and empty.
     */
    public synchronized Order dequeue() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait();
        }
        Order order = queue.poll();
        fireQueueChanged();
        return order;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
        fireQueueChanged();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized List<Order> snapshot() {
        return new ArrayList<>(queue);
    }

    private void fireQueueChanged() {
        if (onQueueChanged != null) {
            onQueueChanged.run();
        }
    }
}
