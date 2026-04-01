package log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton event logger for the Stage 2 simulation.
 *
 * <p>All simulation events (customer joins queue, order processed, etc.)
 * are recorded here with a timestamp. The log can be written to a file
 * when the simulation exits.</p>
 *
 * <h3>Design pattern: Singleton</h3>
 * <p>Only one EventLog instance exists throughout the application lifetime.
 * {@link #getInstance()} is {@code synchronized} to be thread-safe under
 * concurrent access from multiple Staff and OrderProducer threads.</p>
 */
public class EventLog {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The single instance – created lazily on first call to getInstance(). */
    private static EventLog instance;

    /** Ordered list of log entries (timestamp + message). */
    private final List<String> entries = new ArrayList<>();

    /** Private constructor – prevents external instantiation. */
    private EventLog() {}

    // ------------------------------------------------------------------ //
    //  Singleton accessor                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Returns the sole EventLog instance, creating it if necessary.
     *
     * @return the singleton EventLog
     */
    public static synchronized EventLog getInstance() {
        if (instance == null) {
            instance = new EventLog();
        }
        return instance;
    }

    // ------------------------------------------------------------------ //
    //  Logging                                                              //
    // ------------------------------------------------------------------ //

    /**
     * Records an event with the current timestamp and prints it to stdout.
     *
     * <p>This method is {@code synchronized} so it is safe to call from
     * multiple threads simultaneously.</p>
     *
     * @param event human-readable description of the event
     */
    public synchronized void log(String event) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + event;
        entries.add(entry);
        System.out.println(entry);
    }

    /**
     * Returns an unmodifiable view of all recorded log entries.
     *
     * @return list of log entries in chronological order
     */
    public synchronized List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    // ------------------------------------------------------------------ //
    //  File output                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Writes all recorded log entries to the specified file, one entry per line.
     *
     * @param filePath path of the output file (created or overwritten)
     * @throws IOException if the file cannot be written
     */
    public synchronized void writeToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String entry : entries) {
                writer.write(entry);
                writer.newLine();
            }
        }
        System.out.println("[EventLog] Log written to: " + filePath);
    }
}
