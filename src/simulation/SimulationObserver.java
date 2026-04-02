package simulation;

/**
 * Observer interface (Observer pattern).
 * GUI implements this to get notified when simulation state changes.
 */
public interface SimulationObserver {
    void onSimulationUpdate();
}
