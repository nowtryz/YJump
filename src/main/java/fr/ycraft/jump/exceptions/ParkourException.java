package fr.ycraft.jump.exceptions;

/**
 * An exception that can occur during the plugin life
 */
public class ParkourException extends Exception {
    private static final long serialVersionUID = 839127373861894196L;

    public ParkourException() {
    }

    public ParkourException(String message) {
        super(message);
    }

    public ParkourException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParkourException(Throwable cause) {
        super(cause);
    }

    public ParkourException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
