package com.github.neuralabc.spft.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * The panel to show instructions to participants
 */
public class TextPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(TextPanel.class);
    private final JLabel text;

    public TextPanel(int height) {
        setPreferredSize(new Dimension(0, height));
        setLayout(new BorderLayout());

        if (Boolean.getBoolean("debug")) {
            setBackground(Color.BLUE);
            text = new JLabel("Some message");
        } else {
            text = new JLabel();
        }

        Font font = new Font("Dialog", Font.PLAIN, 50); //TODO: make font size more dynamic
        text.setFont(font);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        add(text, BorderLayout.CENTER);
    }

    public void setText(String text) {
        LOG.trace("Changing text to '{}'", text);
        this.text.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        LOG.trace("Drawing text");
    }
}
