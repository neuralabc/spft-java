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
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        textPanel = new TextPanel(screenSize.height / 4);
        add(textPanel, BorderLayout.NORTH);
        barsPanel = new BarsPanel();
        add(barsPanel, BorderLayout.CENTER);

        pack();
    }

    public Binding getBinding() {
        return binding;
    }

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
    }
}
