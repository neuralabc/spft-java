package com.github.neuralabc.spft.task.exceptions;

/**
 * An error writing the output
 */
public class OutputException extends RuntimeException {
    public OutputException(String message, Exception exc) {
        super(message, exc);
    }
}
