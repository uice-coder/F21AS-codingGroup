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
    private boolean closed = false;

    public synchronized void enqueue(Order order) {
        queue.add(order);
        SimulationLog.getInstance().log("Customer " + order.getCustomerId()
                + " joined the queue (Order: " + order.getOrderId() + ", "
                + order.getItems().size() + " item(s))");
        notifyAll();
    }

    /**
     * Blocks until an order is available, or returns null if closed and empty.
     */
    public synchronized Order dequeue() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait();
        }
        return queue.poll();
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
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
}
