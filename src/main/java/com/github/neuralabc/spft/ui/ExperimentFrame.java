package com.github.neuralabc.spft.ui;

import com.github.neuralabc.spft.task.config.SessionConfig;

import javax.swing.*;
import java.awt.*;

/**
 * The window seen by participants
 */
public class ExperimentFrame extends JFrame {
    private final Binding binding;
    private final TextPanel textPanel;
    private final BarsPanel barsPanel;

    public ExperimentFrame() throws HeadlessException {
        binding = new Binding();


        setResizable(true);
        // setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //this was the original way using Toolkit, which also worked fine
        // this provides acess to more information
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        // Get the current display mode
        DisplayMode displayMode = device.getDisplayMode();

        textPanel = new TextPanel(displayMode.getHeight() / 4);
        add(textPanel, BorderLayout.NORTH);
        barsPanel = new BarsPanel();
        add(barsPanel, BorderLayout.CENTER);
        pack();
    }

    public Binding getBinding() {
        return binding;
    }

    // // Utility method to calculate scale factor
    // private double getScaleFactor() {
    //     GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

    //     // Get the current display mode
    //     DisplayMode displayMode = device.getDisplayMode();

    //     if (displayMode != null) {
    //         // Print out display mode details
    //         System.out.println("--Display mode details--");
    //         System.out.println("Width: " + displayMode.getWidth());
    //         System.out.println("Height: " + displayMode.getHeight());
    //         System.out.println("Bit Depth: " + displayMode.getBitDepth());
    //         System.out.println("Refresh Rate: " + displayMode.getRefreshRate());
    //     } else {
    //         System.out.println("Display Mode is not available");
    //     }

    //     int defaultDPI = 96;
    //     int currentDPI = Toolkit.getDefaultToolkit().getScreenResolution();
    //     System.out.println("Current system DPI: " + currentDPI);
    //     return (double) currentDPI / defaultDPI;
    // }
    
    public class Binding {

        public void showText(String text) {
            textPanel.setText(text);
        }

        public void setLeftReferenceValue(double normalizedValue) {
            barsPanel.setLeftReferenceValue(normalizedValue);
        }

        public void setRightReferenceValue(double normalizedValue) {
            barsPanel.setRightReferenceValue(normalizedValue);
        }

        public void setLeftForceValue(double normalizedValue) {
            barsPanel.setLeftForceValue(normalizedValue);
        }

        public void setRightForceValue(double normalizedValue) {
            barsPanel.setRightForceValue(normalizedValue);
        }

        public void setColours(SessionConfig.ColoursConfig colours) {
            barsPanel.setColours(colours);
        }

        public void showLeftBars(boolean show) {
            barsPanel.showLeftBars(show);
        }

        public void showRightBars(boolean show) {
            barsPanel.showRightBars(show);
        }

        public void setForceRange(SessionConfig.ForceRange forceRange) {
            barsPanel.setForceRange(forceRange);
        }
    }
}
