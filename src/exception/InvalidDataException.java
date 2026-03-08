package exception;

/**
 * Custom checked exception for data validation errors.
 * Thrown when data loaded from CSV files does not meet validation rules.
 */
public class InvalidDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}