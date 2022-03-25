package com.github.neuralabc.spft.ui;

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

        public void setLeftReferenceValue(double value) {
            barsPanel.setLeftReferenceValue(value);
        }

        public void setRightReferenceValue(double value) {
            barsPanel.setRightReferenceValue(value);
        }
    }
}