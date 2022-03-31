package com.github.neuralabc.spft.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * A graphical bar
 */
public class StimulusBar extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(StimulusBar.class);
    @Override
    public int getBaseline(int width, int height) {
        return getPreferredSize().height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        LOG.trace("Drawing bar {} with height {}", getName(), getPreferredSize().height);
    }
}
