package com.github.neuralabc.spft.task.exceptions;

/**
 * An error with the Force Gauge device
 */
public class ForceGaugeException extends RuntimeException {
    private int errorCode;

    public ForceGaugeException(int errorCode, String message) {
        this(message);
        this.errorCode = errorCode;
    }

    public ForceGaugeException(String message, Throwable exc) {
        super(message, exc);
    }

    public ForceGaugeException(String message) {
        super(message);
    }

    public int getDeviceErrorCode() {
        return errorCode;
    }
}
