package fr.ycraft.jump.exceptions;

import lombok.NonNull;
import org.apache.commons.lang3.Functions.FailableFunction;

import java.util.function.Function;

/**
 * An exception that we can use in stream to wrap actual exceptions
 */
public class StreamException extends RuntimeException {
    private static final long serialVersionUID = -5753869052175664535L;
    private final Exception cause;

    public StreamException(@NonNull Exception cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public synchronized Exception getCause() {
        return this.cause;
    }

    /**
     * Wrap a function throwing checked exception to a function throwing StreamException. Useful to use
     * in stream where lambda throwing checked exceptions is not allowed
     * @param originalFunction the function to wrap
     * @param <I> the input type of the function
     * @param <O> the output type of the function
     * @param <T> the Exception thrown by the original function
     * @return the same function thrown exceptions wrapped in a StreamException
     */
    public static <I, O, T extends Exception> Function<I, O> warp(FailableFunction<I, O, T> originalFunction) {
        return t -> {
            try {
                return originalFunction.apply(t);
            } catch (Exception exception) {
                throw new StreamException(exception);
            }
        };
    }
}
