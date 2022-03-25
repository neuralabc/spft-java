package com.github.neuralabc.spft.ui;

import com.github.neuralabc.spft.task.Session;
import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Tool-generated class for the control window
 */
public class ControlContent {
    private static final Logger LOG = LoggerFactory.getLogger(ControlContent.class);
    private static final String LAST_FOLDER = "lastFolder";

    private final Preferences prefs;
    private JButton loadButton;
    private JTextField configPathValue;
    private JTextField trialNameValue;
    private JTextField outputFileValue;
    private JPanel panel;
    private JButton startButton;
    private JTextField participantIdValue;
    private final JFileChooser fileChooser;
    private Session currentSession;

    public ControlContent(ExperimentFrame.Binding binding) {
        prefs = Preferences.userRoot().node(getClass().getName());
        fileChooser = new JFileChooser(prefs.get(LAST_FOLDER, new File(".").getAbsolutePath()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("YAML configurations", "yml", "yaml"));
        loadButton.addActionListener(e -> loadClicked());
        participantIdValue.addActionListener(e -> participantIdChanged(e.getActionCommand()));
        startButton.addActionListener(e -> startClicked(binding));
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
            LOG.warn("Output file {} already exists", outputFile);
            int override = JOptionPane.showConfirmDialog(panel, "Output file already exists. Do you want to overwrite it?", "Output exists", JOptionPane.YES_NO_OPTION);
            if (override == JOptionPane.NO_OPTION) {
                return;
            } else {
                LOG.warn("Overwriting file {}", outputFile);
            }
        }
        outputFileValue.setText(outputFile.toString());
        startButton.setEnabled(true);
    }

    private void startClicked(ExperimentFrame.Binding binding) {
        currentSession.start(binding);
    }

    public JPanel getPanel() {
        return panel;
    }

    private void setData(SessionConfig data) {
        configPathValue.setText(data.getPath());
        trialNameValue.setText(data.getSessionName());
        outputFileValue.setText("NOT SET");
    }
}
