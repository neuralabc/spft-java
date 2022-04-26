package com.github.neuralabc.spft.task.exceptions;

/**
 * An error running a session
 */
public class SessionException extends RuntimeException {
    public SessionException(String message, Throwable exc) {
        super(message, exc);
    }

    public SessionException(String message) {
        super(message);
    }
}
