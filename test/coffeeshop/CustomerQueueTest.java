package coffeeshop;

import model.Order;
import org.junit.jupiter.api.Test;
import simulation.CustomerQueue;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 2 tests for {@link CustomerQueue}.
 *
 * Focuses on deterministic queue behaviour rather than timing-heavy thread use.
 */
public class CustomerQueueTest {

    @Test
    public void testEnqueueThenDequeueReturnsSameOrder() throws InterruptedException {
        CustomerQueue queue = new CustomerQueue(null);
        Order order = new Order("ORD-001", "CUST-001", LocalDateTime.now());

        queue.enqueue(order);

        assertEquals(1, queue.size());
        assertSame(order, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testQueuePreservesFifoOrder() throws InterruptedException {
        CustomerQueue queue = new CustomerQueue(null);
        Order first = new Order("ORD-001", "CUST-001", LocalDateTime.now());
        Order second = new Order("ORD-002", "CUST-002", LocalDateTime.now());

        queue.enqueue(first);
        queue.enqueue(second);

        assertSame(first, queue.dequeue());
        assertSame(second, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testDequeueReturnsNullWhenClosedAndEmpty() throws InterruptedException {
        CustomerQueue queue = new CustomerQueue(null);

        queue.close();

        assertTrue(queue.isClosed());
        assertNull(queue.dequeue());
    }
}
