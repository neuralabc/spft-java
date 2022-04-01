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
    public static final ForceGauge DISABLED_DEVICE = new ForceGauge(DISABLED, DISABLED, 1, null);
    private static final int MAX_ERROR_COUNT = 100;
    private final String name;
    private final SerialPort commPort;
    private final OutputSection output;
    private Thread thread;
    private int errorCount;
    private final int normalizationFactor;
    private final ExperimentFrame.Binding binding;

    public ForceGauge(String deviceName, String portName, int normalizationFactor, ExperimentFrame.Binding binding) {
        name = deviceName;
        this.normalizationFactor = normalizationFactor;
        this.binding = binding;
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
            int largestValue = -1;
            if (!commPort.openPort()) {
                throw new ForceGaugeException(commPort.getLastErrorCode(), "Error opening port " + commPort.getSystemPortName() + " for device " + name);
            }
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            StringBuilder builder = new StringBuilder(8);
            while (!thread.isInterrupted()) {
                byte[] sample = new byte[1];
                int readBytes = commPort.readBytes(sample, sample.length);
                if (readBytes == sample.length) {
                    if (sample[0] == '\r' || sample[0] == '\n') {
                        if (!builder.isEmpty()) {
                            int sampleValue = Integer.parseInt(builder.toString());
                            if (sampleValue > largestValue) {
                                largestValue = sampleValue;
                            }

                            double normalizedValue = sampleValue / (double) normalizationFactor;
                            if (name.contains("left")) {
                                binding.setLeftForceValue(normalizedValue);
                            } else {
                                binding.setRightForceValue(normalizedValue);
                            }

                            output.addSample(name, normalizedValue);
                            builder = new StringBuilder(8);
                        }
                    } else {
                        builder.append((char) sample[0]);
                    }
                } else if (readBytes == -1) {
                    if (errorCount >= 0 && errorCount < MAX_ERROR_COUNT) {
                        LOG.error("Error #{} reading from device {}. Error code: {}", errorCount, this, commPort.getLastErrorCode());
                    }
                    errorCount++;
                }
            }
            if (name.contains("left")) {
                binding.setLeftForceValue(1.0);
            } else {
                binding.setRightForceValue(1.0);
            }

            LOG.info("Largest raw value in device '{}': {}", name, largestValue);
            System.out.println("######\nLargest raw value in device '" + name + "': " + largestValue + "\n######");
            LOG.debug("Terminating device thread cleanly");
        } catch (ForceGaugeException exc) {
            LOG.error("Data acquisition for {} crashed. Comm port error code = {}", this, exc.getDeviceErrorCode(), exc);
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
