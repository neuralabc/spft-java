package com.github.neuralabc.spft.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import java.awt.*;

/**
 * The window that controls the experiment
 */
public class ControlFrame extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(ControlFrame.class);

    private ControlFrame(ExperimentFrame.Binding binding) throws HeadlessException {
        super("Control");

        // Calculate scale factor based on DPI
        double scaleFactor = getScaleFactor();

        // Apply scaling factor to font and possibly other components
        setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, (int)(12 * scaleFactor)));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ControlContent controlContent = new ControlContent(binding);
        setContentPane(controlContent.getPanel());

        pack();
    }

    // Utility method to calculate scale factor
    private double getScaleFactor() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Get the current display mode
        DisplayMode displayMode = device.getDisplayMode();

        if (displayMode != null) {
            // Print out display mode details
            System.out.println("--Display mode details--");
            System.out.println("Width: " + displayMode.getWidth());
            System.out.println("Height: " + displayMode.getHeight());
            System.out.println("Bit Depth: " + displayMode.getBitDepth());
            System.out.println("Refresh Rate: " + displayMode.getRefreshRate());
        } else {
            System.out.println("Display Mode is not available");
        }

        int defaultDPI = 96;
        int currentDPI = Toolkit.getDefaultToolkit().getScreenResolution();
        System.out.println("Current system DPI: " + currentDPI);
        return (double) currentDPI / defaultDPI;
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
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

        int defaultDPI = 96;
        int currentDPI = Toolkit.getDefaultToolkit().getScreenResolution();
        System.out.println("Current system DPI: " + currentDPI);
        double scaleFactor = (double) currentDPI / defaultDPI;
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("Current OS: " + osName);


        
        // Get the current display mode to scale screen appropriately
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = device.getDisplayMode();
        int width = displayMode.getWidth();
        int height = displayMode.getHeight();

        if (osName.contains("windows")) {
            // Windows-specific code
            experimentFrame.setSize((int)(800),(int)(600)); // start it small so that we can maximize it
            // experimentFrame.setSize((int)(width),(int)(height));
        } else if (osName.contains("linux")) {
            // Linux-specific code
            experimentFrame.setSize((int)(width),(int)(height));
            // experimentFrame.setSize((int)(800),(int)(600)); // start it small so that we can maximize it
        } else {
            // Code for other operating systems
            System.out.println("Your system OS is currently not supported");
        }
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
