package com.github.neuralabc.spft.hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.github.neuralabc.spft.task.exceptions.ForceGaugeException;
import com.github.neuralabc.spft.task.output.OutputSection;
// import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//*
// #after mamba install pyserial
// import serial
// port = "/dev/ttyACM0"
// rate = 115200
// s1 = serial.Serial(port,rate)
// s1.flushInput()
// s1.write(str.encode(str(1)+'/n'))

// this is the python version that works just fine
// {'baudrate': 115200,
//  'bytesize': 8,
//  'parity': 'N',
//  'stopbits': 1,
//  'xonxoff': False,
//  'dsrdtr': False,
//  'rtscts': False,
//  'timeout': None,
//  'write_timeout': None,
//  'inter_byte_timeout': None}


/**
 * A serial output device to write integers to serial port to indicate start/stop of trials
 */
public class TriggerSender implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerSender.class);
    private final String name;
    private final OutputSection output;
    private SerialPort commPort;
    private Thread thread;
    public static String DISABLED = "Disabled";
    public static final TriggerSender DISABLED_DEVICE = new TriggerSender(DISABLED, DISABLED);
    private static final byte[] TRIGGER_MESSAGE = {'1','\n'};
    private static final char START_TRIAL_TRIGGER = '0';
    private static final char STOP_TRIAL_TRIGGER = '1';
    private static final int baudRate = 115200; // could make a variable as required, but this is the baudrate for the arduino mega


    public TriggerSender(String deviceName, String portName) {
        this.name = deviceName;
        // this.binding = binding;
        if (!deviceName.equals(DISABLED) && !portName.equals(DISABLED)) {
            commPort = SerialPort.getCommPort(portName);
            commPort.setParity(SerialPort.NO_PARITY);
            commPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
            commPort.setNumDataBits(8);
            // commPort.addDataListener(this);
            commPort.setBaudRate(baudRate);
        } else {
            commPort = null;
        }
        if (this.commPort != null) {
            this.commPort.openPort();
            // //test write
            // send((byte) 1);
        }
        output = new OutputSection(1);
    }

    //created this function to be able to set portName separately from class instantiation
    public void setPort(String portName){
        if (!portName.equals(DISABLED)) {
            commPort = SerialPort.getCommPort(portName);
            // output = new OutputSection(1); //this could be used to write to the output xml file if you like
            // output.addEntry("- deviceName", deviceName);
            // output.addEntry("  portName", portName);
            commPort.setParity(SerialPort.NO_PARITY); //this should be the default already  
            commPort.setNumStopBits(SerialPort.ONE_STOP_BIT); // this should be the default already
            commPort.setNumDataBits(8);
            commPort.setBaudRate(baudRate);
        } 
        if (this.commPort != null) {
            this.commPort.openPort();
        }
        //** This is defined in setPort b/c before this point the portName had not been set */
        // output = new OutputSection(1); //this could be used to write to the output xml file if you like
        output.addEntry("- deviceName", this.name);
        output.addEntry("  portName", portName);
        //** */
        
    }

    public void start() {
        if (isEnabled()) {
            LOG.info("Starting trigger device {}", this);
            thread = new Thread(this, name + "-triggerDevice"); // could rename here?
            thread.start();
            LOG.info("Trigger device successfully started");
        } else {
            LOG.trace("Not starting device {} because it's disabled", this);
        }
    }

    public boolean isEnabled() {
        return commPort != null;
    }

    @Override
    public void run() {
        // try {
        //     // Displaying the thread that is running
        //     System.out.println(
        //         "Thread " + thread.currentThread().getId()
        //         + " is running");
        // }
        // catch (Exception e) {
        //     // Throwing an exception
        //     System.out.println("Exception is caught");
        // }
    }
    
    public void stop() {
        if (isEnabled()) {
            LOG.debug("Stopping device {}", this);
            thread.interrupt();
        }
    }
    
    public void sendStart() {
        if (this.isEnabled()){
            this.commPort.writeBytes(TRIGGER_MESSAGE, TRIGGER_MESSAGE.length);
            output.addSample(START_TRIAL_TRIGGER);
        }
    }
    public void sendStop() {
        if (this.isEnabled()){
            this.commPort.writeBytes(TRIGGER_MESSAGE, TRIGGER_MESSAGE.length);
            output.addSample(STOP_TRIAL_TRIGGER);
        }
    }
    public void writeOutput(Path outputFile) throws IOException {
        if (isEnabled()) {
            LOG.debug("Writing sent trigger times from {}", this);
            output.write(outputFile);
        }
    }
}