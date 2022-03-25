package com.github.neuralabc.spft.ui;

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
        LOG.info("Starting application version {}", getVersion());
        SwingUtilities.invokeLater(ControlFrame::createAndShowGui);
    }

    private static void createAndShowGui() {
        ExperimentFrame experimentFrame = new ExperimentFrame();
        experimentFrame.setVisible(true);

        JFrame controlFrame = new ControlFrame(experimentFrame.getBinding());
        controlFrame.setVisible(true);
    }

    private static String getVersion() {
        Package aPackage = ExperimentFrame.class.getPackage();
        String result = aPackage.getImplementationVersion();
        if (result == null || result.isEmpty()) {
            result = "UNKNOWN";
        }

        return result;
    }
}
