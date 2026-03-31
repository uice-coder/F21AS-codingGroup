package simulation;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton logger. Thread-safe via synchronized methods.
 */
public class SimulationLog {

    private static SimulationLog instance;
    private final List<String> entries = new ArrayList<>();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private SimulationLog() {}

    public static synchronized SimulationLog getInstance() {
        if (instance == null) instance = new SimulationLog();
        return instance;
    }

    public synchronized void log(String message) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + message;
        entries.add(entry);
        System.out.println(entry);
    }

    public synchronized List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized void writeToFile(String path) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (String e : entries) pw.println(e);
            System.out.println("[Log] Written to " + path);
        } catch (IOException ex) {
            System.err.println("[Log] Failed to write: " + ex.getMessage());
        }
    }
}
