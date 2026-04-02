package simulation;

import model.Manager;
import model.Order;
import util.ProjectPaths;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central controller for the Stage 2 simulation.
 * Manages the CustomerQueue, StaffMember threads, and CustomerProducer.
 * Implements the observable side of the Observer pattern.
 */
public class SimulationController {
    public enum RunState { READY, RUNNING, COMPLETED }

    private static final String LOG_FILE_NAME = "simulation_log.txt";

    private final Manager manager;
    private final CustomerQueue queue = new CustomerQueue(this::notifyObservers);
    private final List<StaffMember> staff = new ArrayList<>();
    private final List<SimulationObserver> observers = new ArrayList<>();
    private final Object lifecycleLock = new Object();

    private CustomerProducer producer;
    private Thread monitorThread;

    private volatile boolean running = false;
    private volatile boolean started = false;
    private volatile boolean shutdownRequested = false;
    private volatile RunState runState = RunState.READY;
    private boolean completionHandled = false;

    private int numStaff = 2;
    private double speedMultiplier = 1.0;
    private int runOrderCount = 0;

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
        if (running || started) return;

        shutdownRequested = false;
        completionHandled = false;
        started = true;
        running = true;
        runState = RunState.RUNNING;

        List<Order> orders = new ArrayList<>(manager.getOrders());
        runOrderCount = orders.size();
        SimulationLog.getInstance().log("Simulation started with "
                + numStaff + " staff and " + orders.size() + " orders.");
        notifyObservers();

        staff.clear();
        for (int i = 1; i <= numStaff; i++) {
            StaffMember s = new StaffMember(i, queue, this);
            s.setSpeedMultiplier(speedMultiplier);
            staff.add(s);
            s.start();
        }

        producer = new CustomerProducer(orders, queue);
        producer.setSpeedMultiplier(speedMultiplier);
        producer.start();

        monitorThread = new Thread(() -> {
            try {
                producer.join();
                for (StaffMember s : snapshotStaff()) {
                    s.join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finishSimulation();
        }, "SimulationMonitor");
        monitorThread.start();
    }

    public void addStaff() {
        if (!running) return;
        int newId = staff.size() + 1;
        StaffMember s = new StaffMember(newId, queue, this);
        s.setSpeedMultiplier(speedMultiplier);
        staff.add(s);
        s.start();
        SimulationLog.getInstance().log("Staff " + newId + " added during simulation.");
        notifyObservers();
    }

    public void setSpeed(double multiplier) {
        this.speedMultiplier = multiplier;
        if (producer != null) producer.setSpeedMultiplier(multiplier);
        for (StaffMember s : staff) s.setSpeedMultiplier(multiplier);
        SimulationLog.getInstance().log("Simulation speed changed to " + multiplier + "x.");
    }

    public void shutdownAndAwaitTermination() {
        requestShutdown();
        waitForThreadsToFinish();
        finishSimulation();
    }

    private void requestShutdown() {
        shutdownRequested = true;
        queue.close();

        CustomerProducer currentProducer = producer;
        if (currentProducer != null && currentProducer.isAlive()) {
            currentProducer.interrupt();
        }

        for (StaffMember s : snapshotStaff()) {
            if (s.isAlive()) {
                s.interrupt();
            }
        }
    }

    private void waitForThreadsToFinish() {
        try {
            CustomerProducer currentProducer = producer;
            if (currentProducer != null && currentProducer.isAlive()) {
                currentProducer.join();
            }
            for (StaffMember s : snapshotStaff()) {
                if (s.isAlive()) {
                    s.join();
                }
            }
            if (monitorThread != null
                    && monitorThread.isAlive()
                    && Thread.currentThread() != monitorThread) {
                monitorThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<StaffMember> snapshotStaff() {
        return new ArrayList<>(staff);
    }

    private void finishSimulation() {
        synchronized (lifecycleLock) {
            if (completionHandled) return;
            completionHandled = true;
        }

        running = false;
        runState = RunState.COMPLETED;

        if (started) {
            if (shutdownRequested && (!queue.isClosed() || !queue.isEmpty())) {
                SimulationLog.getInstance().log("=== Application shutdown requested. Simulation stopped early. ===");
            } else if (shutdownRequested) {
                SimulationLog.getInstance().log("=== Application shutdown complete. ===");
            } else {
                SimulationLog.getInstance().log("=== Simulation complete. Coffee shop closing. ===");
            }

            String report = manager.generateSalesReport();
            SimulationLog.getInstance().log(report);
        }

        SimulationLog.getInstance().writeToFile(
                ProjectPaths.resolveOutput(LOG_FILE_NAME).toString());
        notifyObservers();
    }

    public CustomerQueue getQueue()            { return queue; }
    public List<StaffMember> getStaff()        { return Collections.unmodifiableList(staff); }
    public boolean isRunning()                 { return running; }
    public boolean hasSimulationStarted()      { return started; }
    public boolean canAcceptNewOrders()        { return !started; }
    public RunState getRunState()              { return runState; }
    public int getRunOrderCount()              { return runOrderCount; }
    public int getNumStaff()                   { return numStaff; }
    public void setNumStaff(int n)             { this.numStaff = n; }
}
