package simulation;

import log.EventLog;
import model.Order;
import observer.QueueObserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread-safe customer queue for the Stage 2 simulation.
 *
 * <h3>Concurrency</h3>
 * <p>All public methods are {@code synchronized} on {@code this}, so the
 * queue is safe to access from the OrderProducer thread and multiple Staff
 * threads simultaneously. {@link #dequeue()} blocks (via {@code wait()})
 * when the queue is empty and more orders may still arrive. Staff threads
 * exit their loop when {@link #dequeue()} returns {@code null}, which
 * happens only after {@link #close()} is called <em>and</em> the queue
 * is empty.</p>
 *
 * <p>Note: Java's built-in thread-safe collections (e.g.
 * {@code ConcurrentLinkedQueue}) are intentionally <strong>not</strong>
 * used here, as per the software engineering requirements.</p>
 *
 * <h3>Observer pattern</h3>
 * <p>Registered {@link QueueObserver}s are notified (with a snapshot of the
 * current queue) on every enqueue and dequeue operation.</p>
 */
public class CustomerQueue {

    /** Underlying storage – LinkedList gives O(1) add-to-tail / remove-from-head. */
    private final LinkedList<Order> queue = new LinkedList<>();

    /** Observers notified on every queue mutation. */
    private final List<QueueObserver> observers = new ArrayList<>();

    /**
     * Set to {@code true} by {@link #close()} when the OrderProducer has
     * finished adding orders. Staff threads use this flag to know when to exit.
     */
    private boolean closed = false;

    // ------------------------------------------------------------------ //
    //  Observer management                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Registers a {@link QueueObserver} to be notified on queue changes.
     *
     * @param observer the observer to add (must not be null)
     */
    public synchronized void addObserver(QueueObserver observer) {
        if (observer != null) observers.add(observer);
    }

    /** Notifies all registered observers with an immutable snapshot of the queue. */
    private void notifyObservers() {
        List<Order> snapshot = new ArrayList<>(queue);
        for (QueueObserver obs : observers) {
            obs.onQueueChanged(snapshot);
        }
    }

    // ------------------------------------------------------------------ //
    //  Queue operations                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Adds an order to the back of the queue and wakes up any Staff threads
     * that are waiting for work.
     *
     * @param order the order to enqueue (must not be null)
     */
    public synchronized void enqueue(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        queue.addLast(order);
        EventLog.getInstance().log("Queue: " + order.getCustomerId()
                + " joined (Order " + order.getOrderId()
                + ", " + order.getItems().size() + " item(s)). Queue size: " + queue.size());
        notifyObservers();
        notifyAll(); // wake blocked Staff threads
    }

    /**
     * Removes and returns the order at the front of the queue.
     *
     * <p>If the queue is currently empty but not yet closed, this method
     * blocks until either an order is enqueued or the queue is closed.</p>
     *
     * @return the next order, or {@code null} if the queue is closed and empty
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public synchronized Order dequeue() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait(); // release lock and sleep until enqueue() or close() calls notifyAll()
        }
        if (queue.isEmpty()) {
            return null; // closed and empty -> staff thread should exit
        }
        Order order = queue.removeFirst();
        notifyObservers();
        return order;
    }

    /**
     * Signals that no further orders will be enqueued.
     *
     * <p>Wakes all Staff threads blocked in {@link #dequeue()} so they can
     * detect the closed+empty condition and exit.</p>
     */
    public synchronized void close() {
        closed = true;
        notifyAll();
        EventLog.getInstance().log("Queue closed – no more orders will be added.");
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                            //
    // ------------------------------------------------------------------ //

    /** @return {@code true} if the queue contains no orders */
    public synchronized boolean isEmpty()  { return queue.isEmpty(); }

    /** @return the number of orders currently in the queue */
    public synchronized int size()         { return queue.size(); }

    /** @return {@code true} if {@link #close()} has been called */
    public synchronized boolean isClosed() { return closed; }

    /**
     * Returns a snapshot of the current queue contents without modifying it.
     *
     * @return new list containing queue elements (front at index 0)
     */
    public synchronized List<Order> snapshot() {
        return new ArrayList<>(queue);
    }
}
