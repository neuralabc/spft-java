package com.github.neuralabc.spft.ui;

import javax.swing.*;

/**
 * A graphical bar
 */
public class StimulusBar extends JPanel {
    @Override
    public int getBaseline(int width, int height) {
        return getPreferredSize().height;
    }
}
