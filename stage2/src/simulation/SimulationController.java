package simulation;

import model.Manager;
import model.Order;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central controller for the Stage 2 simulation.
 * Manages the CustomerQueue, StaffMember threads, and CustomerProducer.
 * Implements the observable side of the Observer pattern.
 */
public class SimulationController {

    private final Manager manager;
    private final CustomerQueue queue = new CustomerQueue();
    private final List<StaffMember> staff = new CopyOnWriteArrayList<>();
    private CustomerProducer producer;

    private final List<SimulationObserver> observers = new ArrayList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private int numStaff = 2;
    private int speedMultiplier = 1; // 1=normal,2=fast,3=slow

    public SimulationController(Manager manager) {
        this.manager = manager;
    }

    public void addObserver(SimulationObserver obs) {
        observers.add(obs);
    }

    public synchronized void notifyObservers() {
        for (SimulationObserver obs : observers) {
            SwingUtilities.invokeLater(obs::onSimulationUpdate);
        }
    }

    public void startSimulation() {
        if (!running.compareAndSet(false, true)) return;

        List<Order> orders = new ArrayList<>(manager.getOrders());
        SimulationLog.getInstance().log("Simulation started with "
                + numStaff + " staff and " + orders.size() + " orders.");

        // Start staff threads
        staff.clear();
        for (int i = 1; i <= numStaff; i++) {
            StaffMember s = new StaffMember(i, queue, this);
            s.setSpeedMultiplier(speedMultiplier);
            staff.add(s);
            s.start();
        }

        // Start producer thread
        producer = new CustomerProducer(orders, queue);
        producer.setSpeedMultiplier(speedMultiplier);
        producer.start();

        // Monitor thread to detect when simulation ends
        Thread monitor = new Thread(() -> {
            try {
                producer.join();
                for (StaffMember s : staff) s.join();
            } catch (InterruptedException ignored) {}
            running.set(false);
            SimulationLog.getInstance().log("=== Simulation complete. Coffee shop closing. ===");
            String report = manager.generateSalesReport();
            SimulationLog.getInstance().log(report);
            SimulationLog.getInstance().writeToFile("simulation_log.txt");
            notifyObservers();
        }, "SimulationMonitor");
        monitor.setDaemon(true);
        monitor.start();
    }

    public void addStaff() {
        if (!running.get()) return;
        int newId = staff.size() + 1;
        StaffMember s = new StaffMember(newId, queue, this);
        s.setSpeedMultiplier(speedMultiplier);
        staff.add(s);
        s.start();
        SimulationLog.getInstance().log("Staff " + newId + " added during simulation.");
        notifyObservers();
    }

    public void setSpeed(int multiplier) {
        this.speedMultiplier = multiplier;
        if (producer != null) producer.setSpeedMultiplier(multiplier);
        for (StaffMember s : staff) s.setSpeedMultiplier(multiplier);
        SimulationLog.getInstance().log("Simulation speed changed to " + multiplier + "x.");
    }

    public CustomerQueue getQueue()            { return queue; }
    public List<StaffMember> getStaff()        { return Collections.unmodifiableList(staff); }
    public boolean isRunning()                 { return running.get(); }
    public int getNumStaff()                   { return numStaff; }
    public void setNumStaff(int n)             { this.numStaff = n; }
}
