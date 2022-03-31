package com.github.neuralabc.spft.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * The panel containing all the feedback bars
 */
public class BarsPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(BarsPanel.class);
    private static final int MAX_HEIGHT = 500; //TODO: make these values resolution-dependent
    private static final int WIDTH = 100;
    private static final int MIN_HEIGHT = 20;
    private final JPanel leftForceBar;
    private final JPanel leftReferenceBar;
    private final JPanel rightForceBar;
    private final JPanel rightReferenceBar;

    public BarsPanel() {
        if (Boolean.getBoolean("debug")) {
            setBackground(Color.RED);
        }
        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 200, 30); //TODO fix magic numbers
        flowLayout.setAlignOnBaseline(true);
        setLayout(flowLayout);

        Dimension barSize = new Dimension(WIDTH, MAX_HEIGHT);

        leftForceBar = new StimulusBar();
        leftForceBar.setName("Left Force Bar");
        leftForceBar.setBackground(Color.BLUE);
        leftForceBar.setPreferredSize(barSize);
        add(leftForceBar);

        leftReferenceBar = new StimulusBar();
        leftReferenceBar.setName("Left Reference Bar");
        leftReferenceBar.setBackground(Color.BLACK);
        leftReferenceBar.setPreferredSize(barSize);
        add(leftReferenceBar);

        rightReferenceBar = new StimulusBar();
        rightReferenceBar.setName("Right Reference Bar");
        rightReferenceBar.setBackground(Color.WHITE);
        rightReferenceBar.setPreferredSize(barSize);
        add(rightReferenceBar);

        rightForceBar = new StimulusBar();
        rightForceBar.setName("Right Force Bar");
        rightForceBar.setBackground(Color.ORANGE);
        rightForceBar.setPreferredSize(barSize);
        add(rightForceBar);
    }

    public void setLeftReferenceValue(double value) {
        changeReferenceHeight(leftReferenceBar, value);
    }

    public void setRightReferenceValue(double value) {
        changeReferenceHeight(rightReferenceBar, value);
    }

    public void setLeftForceValue(double value) {
    }

    public void setRightForceValue(double value) {
    }

    private void changeReferenceHeight(JPanel bar, double value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Sequence values have to be between 0.0 and 1.0");
        }
        LOG.trace("Changing reference height of '{}' to {}", bar.getName(), value);

        final int barRange = MAX_HEIGHT - MIN_HEIGHT;
        int newHeight = (int) Math.round(barRange * value);
        newHeight += MIN_HEIGHT;
        bar.setPreferredSize(new Dimension(WIDTH, newHeight));
        bar.revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        LOG.trace("Drawing bars");
    }
}
