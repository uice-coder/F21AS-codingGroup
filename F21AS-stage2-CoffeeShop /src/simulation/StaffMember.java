package simulation;

import model.Item;
import model.Order;

/**
 * Represents a serving staff member. Runs as a Thread.
 * Pulls orders from CustomerQueue and processes them.
 */
public class StaffMember extends Thread {

    public enum Status { IDLE, PROCESSING, DONE }

    private final int staffId;
    private final CustomerQueue queue;
    private final SimulationController controller;

    private volatile Status status = Status.IDLE;
    private volatile Order currentOrder = null;
    private volatile int speedMultiplier = 1; // 1=normal,2=fast,3=slow

    public StaffMember(int staffId, CustomerQueue queue, SimulationController controller) {
        super("Staff-" + staffId);
        this.staffId = staffId;
        this.queue = queue;
        this.controller = controller;
        setDaemon(true);
    }

    @Override
    public void run() {
        SimulationLog.getInstance().log("Staff " + staffId + " is ready.");
        try {
            while (true) {
                Order order = queue.dequeue();
                if (order == null) {
                    // queue closed and empty
                    break;
                }
                processOrder(order);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        status = Status.DONE;
        currentOrder = null;
        controller.notifyObservers();
        SimulationLog.getInstance().log("Staff " + staffId + " finished work.");
    }

    private void processOrder(Order order) throws InterruptedException {
        currentOrder = order;
        status = Status.PROCESSING;
        order.calculateFinalPrice();

        SimulationLog.getInstance().log("Staff " + staffId
                + " started processing order " + order.getOrderId()
                + " for " + order.getCustomerId());
        controller.notifyObservers();

        // Calculate processing time based on item types
        long totalMs = calculateProcessingTime(order);
        long divided = totalMs / Math.max(1, speedMultiplier);
        Thread.sleep(divided);

        SimulationLog.getInstance().log("Staff " + staffId
                + " COMPLETED order " + order.getOrderId()
                + " for " + order.getCustomerId()
                + String.format(" (£%.2f)", order.getFinalPrice()));

        status = Status.IDLE;
        currentOrder = null;
        controller.notifyObservers();
    }

    private long calculateProcessingTime(Order order) {
        long ms = 0;
        for (Item item : order.getItems()) {
            String cat = item.getCategory();
            if ("Beverage".equalsIgnoreCase(cat) || "Tea".equalsIgnoreCase(cat)) {
                // 2-4 seconds
                ms += 2000 + (long)(Math.random() * 2000);
            } else if ("Food".equalsIgnoreCase(cat)) {
                // 6-10 seconds
                ms += 6000 + (long)(Math.random() * 4000);
            } else {
                ms += 3000 + (long)(Math.random() * 2000);
            }
        }
        return Math.max(2000, ms);
    }

    public int getStaffId()        { return staffId; }
    public Status getStatus()      { return status; }
    public Order getCurrentOrder() { return currentOrder; }
    public void setSpeedMultiplier(int m) { this.speedMultiplier = m; }
}
