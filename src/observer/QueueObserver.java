package observer;

import model.Order;
import java.util.List;

/**
 * Observer interface for changes to the customer queue.
 *
 * <p>Implemented by any class that needs to react when the queue contents
 * change (e.g. an order is enqueued or dequeued).</p>
 */
public interface QueueObserver {

    /**
     * Called whenever the queue is modified.
     *
     * @param snapshot an immutable snapshot of the current queue contents
     *                 (front of queue at index 0)
     */
    void onQueueChanged(List<Order> snapshot);
}
