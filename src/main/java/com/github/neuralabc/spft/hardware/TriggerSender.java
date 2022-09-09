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

    /**
     * The name of a disabled device
     */
    public static String DISABLED = "Disabled";

    @Override
    public void run() {
    }
    public void stop() {
    }
    public void send() {
    }
}