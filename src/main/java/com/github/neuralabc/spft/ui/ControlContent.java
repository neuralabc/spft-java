package com.github.neuralabc.spft.ui;

import com.github.neuralabc.spft.hardware.ForceGauge;
import com.github.neuralabc.spft.hardware.TriggerSender;
import com.github.neuralabc.spft.task.Session;
import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Tool-generated class for the control window
 */
@SuppressWarnings("ClassWithTooManyFields")
public class ControlContent {
    private static final Logger LOG = LoggerFactory.getLogger(ControlContent.class);
    private static final String LAST_FOLDER = "lastFolder";
    private static final String LAST_LEFT_DEVICE = "lastLeftDevice";
    private static final String LAST_RIGHT_DEVICE = "lastRightDevice";
    private static final String LAST_TRIGGER_DEVICE = "lastTriggerDevice";

    private final Preferences prefs;
    private final ExperimentFrame.Binding experimentFrameBinding;
    private JButton loadButton;
    private JTextField configPathValue;
    private JTextField sessionNameValue;
    private JTextField outputFileValue;
    private JPanel panel;
    private JButton startButton;
    private JFormattedTextField participantIdValue;
    private JLabel versionLabel;
    private JComboBox<String> leftDevice;
    private JComboBox<String> rightDevice;
    private JComboBox<String> triggerDevice;
    private JFormattedTextField maximumLeftContractionValue;
    private JFormattedTextField maximumRightContractionValue;
    private final JFileChooser fileChooser;
    private Session currentSession;

    public ControlContent(ExperimentFrame.Binding binding) {
        experimentFrameBinding = binding;
        prefs = Preferences.userRoot().node(getClass().getName());
        fileChooser = new JFileChooser(prefs.get(LAST_FOLDER, new File(".").getAbsolutePath()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("YAML configurations (*.yml, *.yaml)", "yml", "yaml"));
        loadButton.addActionListener(e -> loadClicked());
        leftDevice.addActionListener(this::deviceSelectionChanged);
        rightDevice.addActionListener(this::deviceSelectionChanged);
        triggerDevice.addActionListener(this::deviceSelectionChanged);

        participantIdValue.addActionListener(e -> participantIdChanged(e.getActionCommand()));
        DefaultFormatter defaultFormatter = new DefaultFormatter();
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory(defaultFormatter);
        participantIdValue.setFormatterFactory(defaultFormatterFactory);

        maximumLeftContractionValue.addActionListener(e -> mvcChanged(e.getActionCommand()));
        maximumRightContractionValue.addActionListener(e -> mvcChanged(e.getActionCommand()));
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.CANADA);
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
        maximumLeftContractionValue.setFormatterFactory(factory);
        maximumRightContractionValue.setFormatterFactory(factory);
        startButton.addActionListener(e -> startClicked());
        versionLabel.setText("Version: " + ControlFrame.getVersion());

        leftDevice.addItem(ForceGauge.DISABLED);
        rightDevice.addItem(ForceGauge.DISABLED);
        triggerDevice.addItem(TriggerSender.DISABLED); // we do not initialize the triggerDevice

        List<String> availableDevices = ForceGauge.getDevices();
        availableDevices.forEach(device -> {
            leftDevice.addItem(device);
            rightDevice.addItem(device);
            triggerDevice.addItem(device);
        });

        String lastLeftDevice = prefs.get(LAST_LEFT_DEVICE, ForceGauge.DISABLED);
        if (availableDevices.contains(lastLeftDevice)) {
            leftDevice.setSelectedItem(lastLeftDevice);
        }
        String lastRightDevice = prefs.get(LAST_RIGHT_DEVICE, ForceGauge.DISABLED);
        if (availableDevices.contains(lastRightDevice)) {
            rightDevice.setSelectedItem(lastRightDevice);
        }
        // consider disabling getting the last trigger device, as this has been causing issues 
        String lastTriggerDevice = prefs.get(LAST_TRIGGER_DEVICE, TriggerSender.DISABLED);
        if (availableDevices.contains(lastTriggerDevice)) {
            triggerDevice.setSelectedItem(lastTriggerDevice);
        }
    }

    private void loadClicked() {
        if (currentSession != null) {
            currentSession.cancel();
        }
        reset();
        int result = fileChooser.showOpenDialog(panel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentSession = new Session(selectedFile);
                setData(currentSession.getConfig());
                participantIdValue.setEnabled(true);
                participantIdValue.setText("");
                leftDevice.setEnabled(true);
                maximumLeftContractionValue.setEnabled(true);
                maximumLeftContractionValue.setText("");
                rightDevice.setEnabled(true);
                maximumRightContractionValue.setEnabled(true);
                maximumRightContractionValue.setText("");
                triggerDevice.setEnabled(true);
                prefs.put(LAST_FOLDER, selectedFile.getParent());
                experimentFrameBinding.setColours(currentSession.getConfig().getColours());
                experimentFrameBinding.setForceRange(currentSession.getConfig().getForceProportionRange());
            } catch (SessionException exc) {
                JOptionPane.showMessageDialog(panel, exc.getMessage(), "Error creating session", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deviceSelectionChanged(ActionEvent event) {
        if (event.getSource() == leftDevice) {
            boolean editable = !ForceGauge.DISABLED.equals(leftDevice.getSelectedItem());
            maximumLeftContractionValue.setEditable(editable);
            if (!editable) {
                maximumLeftContractionValue.setText("");
            }
        } else if (event.getSource() == rightDevice) {
            boolean editable = !ForceGauge.DISABLED.equals(rightDevice.getSelectedItem());
            maximumRightContractionValue.setEditable(editable);
            if (!editable) {
                maximumRightContractionValue.setText("");
            }
        // this does nothing, can be removed
        // } else {
        //     boolean editable = !TriggerSender.DISABLED.equals(triggerDevice.getSelectedItem());
        }
    }

    private void reset() {
        setData(new SessionConfig());
        currentSession = null;
        startButton.setEnabled(false);
        participantIdValue.setEnabled(false);
        participantIdValue.setText("");
        maximumLeftContractionValue.setEnabled(false);
        maximumLeftContractionValue.setText("");
        maximumRightContractionValue.setEnabled(false);
        maximumRightContractionValue.setText("");
    }

    private void participantIdChanged(String participantId) {
        Path configPath = Path.of(configPathValue.getText());
        Path outputFile = configPath.getParent().getParent().resolve("output/" + participantId + "_" + currentSession.getConfig().getOutputSuffix());
        if (Files.exists(outputFile)) {
            LOG.info("Output file {} already exists", outputFile);
            int override = JOptionPane.showConfirmDialog(panel, "Output file already exists. Do you want to overwrite it?", "Output exists", JOptionPane.YES_NO_OPTION);
            if (override == JOptionPane.NO_OPTION) {
                participantIdValue.setText("");
                return;
            }
        }
        outputFileValue.setText(outputFile.toString());
        verifyIfComplete();
    }

    private void verifyIfComplete() {
        boolean ready = false;
        if (participantIdValue.getText().isEmpty()) {
            participantIdValue.requestFocusInWindow();
        } else if (!ForceGauge.DISABLED.equals(leftDevice.getSelectedItem()) && maximumLeftContractionValue.getText().isEmpty()) {
            maximumLeftContractionValue.requestFocusInWindow();
        } else if (!ForceGauge.DISABLED.equals(rightDevice.getSelectedItem()) && maximumRightContractionValue.getText().isEmpty()) {
            maximumRightContractionValue.requestFocusInWindow();
        // this should not be necessary for the triggerDevice, as it does not have any text associated with it
        // } else if (!TriggerSender.DISABLED.equals(triggerDevice.getSelectedItem())) {
        //     //TODO: test removing this check to see if it fixes issue with no start button if order wrong
        //     triggerDevice.requestFocusInWindow();
        } else {
            ready = true;
            startButton.requestFocusInWindow();
        }
        startButton.setEnabled(ready);
    }

    private void mvcChanged(String maximumContractionValue) {
        verifyIfComplete();
    }

    private void startClicked() {
        try {
            if (leftDevice.getSelectedItem() == null || rightDevice.getSelectedItem() == null) { //TODO: add check here for trigger?
                throw new IllegalStateException("Start should only be allowed when both left and right devices are set");
            }
            String leftItem = (String) leftDevice.getSelectedItem();
            prefs.put(LAST_LEFT_DEVICE, leftItem);
            String rightItem = (String) rightDevice.getSelectedItem();
            prefs.put(LAST_RIGHT_DEVICE, rightItem);
            String triggerItem = (String) triggerDevice.getSelectedItem();
            prefs.put(LAST_TRIGGER_DEVICE, triggerItem);

            List<String> usedDevices = List.of(leftItem, rightItem);
            String usedTrigger = triggerItem;
            int maximumLeftContraction = maximumLeftContractionValue.getText().isEmpty() ? -1 : Integer.parseInt(maximumLeftContractionValue.getText());
            int maximumRightContraction = maximumRightContractionValue.getText().isEmpty() ? -1 : Integer.parseInt(maximumRightContractionValue.getText());
            Session.SessionParameters sessionParameters = new Session.SessionParameters(participantIdValue.getText(), outputFileValue.getText(), usedDevices, maximumLeftContraction, maximumRightContraction, usedTrigger);
            currentSession.start(sessionParameters, experimentFrameBinding);

            // ?? disables buttons while the session is being conducted ?? 
            startButton.setEnabled(false);
            participantIdValue.setEnabled(false);
            leftDevice.setEnabled(false);
            maximumLeftContractionValue.setEnabled(false);
            rightDevice.setEnabled(false);
            maximumRightContractionValue.setEnabled(false);
            triggerDevice.setEnabled(false);
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
        panel.setLayout(new GridLayoutManager(12, 2, new Insets(10, 10, 10, 10), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration");
        panel.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configPathValue = new JTextField();
        configPathValue.setEditable(false);
        configPathValue.putClientProperty("html.disable", Boolean.FALSE);
        panel.add(configPathValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Session Name");
        panel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Output file");
        panel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sessionNameValue = new JTextField();
        sessionNameValue.setEditable(false);
        panel.add(sessionNameValue, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        outputFileValue = new JTextField();
        outputFileValue.setEditable(false);
        panel.add(outputFileValue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        versionLabel = new JLabel();
        versionLabel.setText("Version:");
        panel.add(versionLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Participant Id");
        panel.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        participantIdValue = new JFormattedTextField();
        participantIdValue.setDropMode(DropMode.INSERT);
        participantIdValue.setEnabled(false);
        participantIdValue.setFocusLostBehavior(2);
        panel.add(participantIdValue, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Left MVC");
        panel.add(label5, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maximumLeftContractionValue = new JFormattedTextField();
        maximumLeftContractionValue.setColumns(5);
        maximumLeftContractionValue.setDropMode(DropMode.INSERT);
        maximumLeftContractionValue.setEnabled(false);
        maximumLeftContractionValue.setFocusLostBehavior(2);
        panel.add(maximumLeftContractionValue, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Right MVC");
        panel.add(label6, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maximumRightContractionValue = new JFormattedTextField();
        maximumRightContractionValue.setColumns(5);
        maximumRightContractionValue.setDropMode(DropMode.INSERT);
        maximumRightContractionValue.setEnabled(false);
        maximumRightContractionValue.setFocusLostBehavior(2);
        panel.add(maximumRightContractionValue, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Left force device");
        panel.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        leftDevice = new JComboBox();
        panel.add(leftDevice, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Right force device");
        panel.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rightDevice = new JComboBox();
        panel.add(rightDevice, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Trigger device");
        panel.add(label9, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        triggerDevice = new JComboBox();
        panel.add(triggerDevice, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }
    

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
