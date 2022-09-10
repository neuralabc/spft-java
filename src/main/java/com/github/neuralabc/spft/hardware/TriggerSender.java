package com.github.neuralabc.spft.hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.github.neuralabc.spft.task.exceptions.ForceGaugeException;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A serial output device to output strings to indicate start/stop of trials
 */
public class TriggerSender implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerSender.class);
    public static final TriggerSender DISABLED_DEVICE = new TriggerSender(DISABLED, DISABLED, null);
    private final String name;
    private final SerialPort commPort;
    private Thread thread;
    private final ExperimentFrame.Binding binding;

    /**
     * The name of a disabled device
     */
    public static String DISABLED = "Disabled";

    public TriggerSender(String deviceName, String portName, ExperimentFrame.Binding binding) {
        name = deviceName;
        this.binding = binding;
        if (!deviceName.equals(DISABLED) && !portName.equals(DISABLED)) {
            commPort = SerialPort.getCommPort(portName);
            output = new OutputSection(1);
            output.addEntry("- deviceName", deviceName);
            output.addEntry("  portName", portName);
        } else {
            commPort = null;
        }
    }

    public void start() {
        if (isEnabled()) {
            LOG.info("Starting device {}", this);
            thread = new Thread(this, name + "-device"); // could rename here?
            thread.start();
        } else {
            LOG.trace("Not starting device {} because it's disabled", this);
        }
    }

    public boolean isEnabled() {
        return commPort != null;
    }

    @Override
    public void run() {
        
        if (!commPort.openPort()) {
            throw new TriggerSenderException(commPort.getLastErrorCode(), "Error opening port " + commPort.getSystemPortName() + " for device " + name);
        }
    }
    
    public void stop() {
        if (isEnabled()) {
            LOG.debug("Stopping device {}", this);
            thread.interrupt();
        }
    }
    
    public void send() {
    }
}