package com.github.neuralabc.spft.ui;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * The window that controls the experiment
 */
public class ControlFrame extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(ControlFrame.class);

    private ControlFrame(ExperimentFrame.Binding binding) throws HeadlessException {
        super("Control");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ControlContent controlContent = new ControlContent(binding);
        setContentPane(controlContent.getPanel());

        pack();
    }

    public static void main(String[] args) {
        if (Boolean.getBoolean("debug")) {
            ch.qos.logback.classic.Logger ourLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.github.neuralabc");
            ourLogger.setLevel(Level.TRACE);
        }
        if (args.length != 0) {
            if ("--version".equalsIgnoreCase(args[0])) {
                System.out.println("Version: " + getVersion());
            }
        } else {
            LOG.info("Starting application version {}", getVersion());
            SwingUtilities.invokeLater(ControlFrame::createAndShowGui);
        }
    }

    private static void createAndShowGui() {
        ExperimentFrame experimentFrame = new ExperimentFrame();
        experimentFrame.setVisible(true);

        JFrame controlFrame = new ControlFrame(experimentFrame.getBinding());
        controlFrame.setVisible(true);
    }

    public static String getVersion() {
        Package aPackage = ControlFrame.class.getPackage();
        String result = aPackage.getImplementationVersion();
        if (result == null || result.isEmpty()) {
            result = "UNKNOWN";
        }

        return result;
    }
}
