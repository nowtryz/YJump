package fr.ycraft.jump.exceptions;

public class ConfigurationInitializationException extends RuntimeException {
    private static final long serialVersionUID = 2267779579690790687L;

    public ConfigurationInitializationException(Throwable cause) {
        super("Unable to load available configuration keys", cause);
    }
}
