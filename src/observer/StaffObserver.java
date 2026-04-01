package observer;

import model.Order;

/**
 * Observer interface for staff status changes.
 *
 * <p>Implemented by any class that needs to react when a member of serving
 * staff starts or finishes processing an order.</p>
 */
public interface StaffObserver {

    /**
     * Called whenever a staff member's status changes.
     *
     * @param staffName    the display name of the staff member
     * @param currentOrder the order currently being processed,
     *                     or {@code null} if the staff member is idle
     * @param idle         {@code true} if the staff member is waiting for work
     */
    void onStaffStatusChanged(String staffName, Order currentOrder, boolean idle);
}
