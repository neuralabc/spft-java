package com.github.neuralabc.spft.ui;

import com.github.neuralabc.spft.hardware.ForceGauge;
import com.github.neuralabc.spft.task.Session;
import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;
import java.util.List;

/**
 * Tool-generated class for the control window
 */
public class ControlContent {
    private static final Logger LOG = LoggerFactory.getLogger(ControlContent.class);
    private static final String LAST_FOLDER = "lastFolder";
    private static final String LAST_LEFT_DEVICE = "lastLeftDevice";
    private static final String LAST_RIGHT_DEVICE = "lastRightDevice";

    private final Preferences prefs;
    private JButton loadButton;
    private JTextField configPathValue;
    private JTextField sessionNameValue;
    private JTextField outputFileValue;
    private JPanel panel;
    private JButton startButton;
    private JTextField participantIdValue;
    private JLabel versionLabel;
    private JComboBox<String> leftDevice;
    private JComboBox<String> rightDevice;
    private final JFileChooser fileChooser;
    private Session currentSession;

    public ControlContent(ExperimentFrame.Binding binding) {
        prefs = Preferences.userRoot().node(getClass().getName());
        fileChooser = new JFileChooser(prefs.get(LAST_FOLDER, new File(".").getAbsolutePath()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("YAML configurations", "yml", "yaml"));
        loadButton.addActionListener(e -> loadClicked());
        participantIdValue.addActionListener(e -> participantIdChanged(e.getActionCommand()));
        startButton.addActionListener(e -> startClicked(binding));
        versionLabel.setText("Version: " + ControlFrame.getVersion());

        leftDevice.addItem(ForceGauge.DISABLED);
        rightDevice.addItem(ForceGauge.DISABLED);

        List<String> availableDevices = ForceGauge.getDevices();
        availableDevices.forEach(device -> {
            leftDevice.addItem(device);
            rightDevice.addItem(device);
        });

        String lastLeftDevice = prefs.get(LAST_LEFT_DEVICE, ForceGauge.DISABLED);
        if (availableDevices.contains(lastLeftDevice)) {
            leftDevice.setSelectedItem(lastLeftDevice);
        }
        String lastRightDevice = prefs.get(LAST_RIGHT_DEVICE, ForceGauge.DISABLED);
        if (availableDevices.contains(lastRightDevice)) {
            rightDevice.setSelectedItem(lastRightDevice);
        }
    }

    private void loadClicked() {
        reset();
        int result = fileChooser.showOpenDialog(panel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentSession = new Session(selectedFile);
                setData(currentSession.getConfig());
                participantIdValue.setEnabled(true);
                participantIdValue.setText("");
                prefs.put(LAST_FOLDER, selectedFile.getParent());
            } catch (SessionException exc) {
                JOptionPane.showMessageDialog(panel, exc.getMessage(), "Error creating session", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reset() {
        setData(new SessionConfig());
        currentSession = null;
        startButton.setEnabled(false);
        participantIdValue.setEnabled(false);
        participantIdValue.setText("");
    }

    private void participantIdChanged(String participantId) {
        Path configPath = Path.of(configPathValue.getText());
        Path outputFile = configPath.getParent().getParent().resolve("output/" + participantId + "_" + currentSession.getConfig().getOutputSuffix());
        if (Files.exists(outputFile)) {
            LOG.info("Output file {} already exists", outputFile);
            int override = JOptionPane.showConfirmDialog(panel, "Output file already exists. Do you want to overwrite it?", "Output exists", JOptionPane.YES_NO_OPTION);
            if (override == JOptionPane.NO_OPTION) {
                return;
            }
        }
        outputFileValue.setText(outputFile.toString());
        startButton.setEnabled(true);
    }

    private void startClicked(ExperimentFrame.Binding binding) {
        try {
            if (leftDevice.getSelectedItem() == null || rightDevice.getSelectedItem() == null) {
                throw new IllegalStateException("Start should only be allowed when both left and right devices are set");
            }
            String leftItem = (String) leftDevice.getSelectedItem();
            prefs.put(LAST_LEFT_DEVICE, leftItem);
            String rightItem = (String) rightDevice.getSelectedItem();
            prefs.put(LAST_RIGHT_DEVICE, rightItem);

            List<String> usedDevices = List.of(leftItem, rightItem);
            currentSession.start(participantIdValue.getText(), outputFileValue.getText(), usedDevices, binding);
        } catch (IOException exc) {
            JOptionPane.showMessageDialog(panel, exc.toString(), "Error writing output", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    private void setData(SessionConfig data) {
        configPathValue.setText(data.getPath());
        sessionNameValue.setText(data.getSessionName());
        outputFileValue.setText("NOT SET");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(8, 2, new Insets(10, 10, 10, 10), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration");
        panel.add(label1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configPathValue = new JTextField();
        configPathValue.setEditable(false);
        configPathValue.putClientProperty("html.disable", Boolean.FALSE);
        panel.add(configPathValue, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Session Name");
        panel.add(label2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Output file");
        panel.add(label3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sessionNameValue = new JTextField();
        sessionNameValue.setEditable(false);
        panel.add(sessionNameValue, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        outputFileValue = new JTextField();
        outputFileValue.setEditable(false);
        panel.add(outputFileValue, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        panel1.add(loadButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setEnabled(false);
        startButton.setText("Start");
        panel1.add(startButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Participant Id");
        panel.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        participantIdValue = new JTextField();
        participantIdValue.setEnabled(false);
        panel.add(participantIdValue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        versionLabel = new JLabel();
        versionLabel.setText("Version:");
        panel.add(versionLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Left force device");
        panel.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Right force device");
        panel.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        leftDevice = new JComboBox();
        panel.add(leftDevice, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rightDevice = new JComboBox();
        panel.add(rightDevice, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
