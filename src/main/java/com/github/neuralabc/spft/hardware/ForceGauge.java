package com.github.neuralabc.spft.hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.github.neuralabc.spft.task.output.OutputSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A device measuring force recognized as a COMM device
 */
public class ForceGauge implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ForceGauge.class);

    /**
     * The name of a disabled device
     */
    public static String DISABLED = "Disabled";
    /**
     * A device that doesn't do anything
     */
    public static final ForceGauge DISABLED_DEVICE = new ForceGauge(DISABLED, DISABLED);
    private static final int MAX_ERROR_COUNT = 100;
    private final String name;
    private final SerialPort commPort;
    private final OutputSection output;
    private Thread thread;
    private int errorCount;

    public ForceGauge(String deviceName, String portName) {
        name = deviceName;
        if (!deviceName.equals(DISABLED) && !portName.equals(DISABLED)) {
            commPort = SerialPort.getCommPort(portName);
            commPort.allowElevatedPermissionsRequest();
            output = new OutputSection("Force gauge");
            output.addEntry("deviceName", deviceName);
            output.addEntry("portName", portName);
        } else {
            commPort = null;
            output = null;
        }
    }

    public static List<String> getDevices() {
        return Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).collect(Collectors.toList());
    }

    public void start() {
        if (isEnabled()) {
            LOG.info("Starting device {}", this);
            thread = new Thread(this, name + "-reader");
            thread.start();
        } else {
            LOG.trace("Not starting device {} because it's disabled", this);
        }
    }

    public boolean isEnabled() {
        return commPort != null;
    }

    public void stop() {
        if (isEnabled()) {
            LOG.debug("Stopping device {}", this);
            thread.interrupt();
        }
    }

    public void writeOutput(Path outputFile) throws IOException {
        if (isEnabled()) {
            LOG.debug("Writing acquired data of {}", this);
            output.write(outputFile);
        }
    }

    @Override
    public void run() {
        try {
            double largestValue = -1;
            commPort.openPort();
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            StringBuilder builder = new StringBuilder(8);
            while (!thread.isInterrupted()) {
                byte[] sample = new byte[1];
                int readBytes = commPort.readBytes(sample, sample.length);
                if (readBytes == sample.length) {
                    if (sample[0] == '\r' || sample[0] == '\n') {
                        if (!builder.isEmpty()) {
                            double sampleValue = Double.parseDouble(builder.toString());
                            if (sampleValue > largestValue) {
                                largestValue = sampleValue;
                            }
                            output.addSample(name, sampleValue);
                            builder = new StringBuilder(8);
                        }
                    } else {
                        builder.append((char) sample[0]);
                    }
                } else if (readBytes == -1) {
                    if (errorCount >= 0 && errorCount < MAX_ERROR_COUNT) {
                        LOG.error("Error #{} reading from device {}", errorCount, this);
                    }
                    errorCount++;
                }
            }
            LOG.info("Largest raw value in session: {}", largestValue);
            System.out.println("######\nLargest raw value in session: " + largestValue + "\n######");
            LOG.debug("Terminating device thread cleanly");
        } catch (Exception exc) {
            LOG.error("Data acquisition crashed on {}", this, exc);
        } finally {
            commPort.closePort();
        }
    }

    @Override
    public String toString() {
        return "ForceGauge{" +
                "name='" + name + '\'' +
                ", portName=" + (isEnabled() ? commPort.getSystemPortName() : DISABLED) +
                '}';
    }
}
