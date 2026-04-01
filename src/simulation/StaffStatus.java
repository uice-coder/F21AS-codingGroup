package simulation;

import model.Order;

/**
 * Immutable snapshot of a staff member's current state.
 *
 * <p>Instances are created by {@link Staff} and passed to observers
 * whenever the staff member's status changes.</p>
 */
public class StaffStatus {

    private final String staffName;
    private final Order  currentOrder; // null when idle
    private final boolean idle;

    public StaffStatus(String staffName, Order currentOrder, boolean idle) {
        this.staffName    = staffName;
        this.currentOrder = currentOrder;
        this.idle         = idle;
    }

    public String  getStaffName()    { return staffName; }
    public Order   getCurrentOrder() { return currentOrder; }
    public boolean isIdle()          { return idle; }
}
