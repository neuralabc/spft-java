package com.github.neuralabc.spft.task.exceptions;

/**
 * An error writing the output
 */
public class OutputException extends RuntimeException {
    public OutputException(String message, Throwable exc) {
        super(message, exc);
    }
}
