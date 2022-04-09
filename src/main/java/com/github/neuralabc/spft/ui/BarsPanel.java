package com.github.neuralabc.spft.ui;

import com.github.neuralabc.spft.task.config.SessionConfig;
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
    private final Component space1;
    private final Component space2;
    private final Component space3;

    public BarsPanel() {
        if (Boolean.getBoolean("debug")) {
            setBackground(Color.RED);
        }
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(boxLayout);

        Dimension barSize = new Dimension(WIDTH, MAX_HEIGHT);
        Dimension minSize = new Dimension(WIDTH, MIN_HEIGHT);

        leftForceBar = new JPanel();
        leftForceBar.setName("Left Force Bar");
        leftForceBar.setPreferredSize(barSize);
        leftForceBar.setMaximumSize(barSize);
        leftForceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        add(Box.createHorizontalGlue());
        add(leftForceBar);

        leftReferenceBar = new JPanel();
        leftReferenceBar.setName("Left Reference Bar");
        leftReferenceBar.setPreferredSize(minSize);
        leftReferenceBar.setMaximumSize(barSize);
        leftReferenceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space1 = Box.createHorizontalGlue();
        add(space1);
        add(leftReferenceBar);

        rightReferenceBar = new JPanel();
        rightReferenceBar.setName("Right Reference Bar");
        rightReferenceBar.setPreferredSize(minSize);
        rightReferenceBar.setMaximumSize(barSize);
        rightReferenceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space2 = Box.createHorizontalGlue();
        add(space2);
        add(rightReferenceBar);

        rightForceBar = new JPanel();
        rightForceBar.setName("Right Force Bar");
        rightForceBar.setPreferredSize(minSize);
        rightForceBar.setMaximumSize(barSize);
        rightForceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space3 = Box.createHorizontalGlue();
        add(space3);
        add(rightForceBar);
        add(Box.createHorizontalGlue());
    }

    public void setLeftReferenceValue(double normalizedValue) {
        changeReferenceHeight(leftReferenceBar, normalizedValue);
    }

    public void setRightReferenceValue(double normalizedValue) {
        changeReferenceHeight(rightReferenceBar, normalizedValue);
    }

    public void setLeftForceValue(double normalizedValue) {
        changeForceHeight(leftForceBar, normalizedValue);
    }

    public void setRightForceValue(double normalizedValue) {
        changeForceHeight(rightForceBar, normalizedValue);
    }

    private void changeReferenceHeight(JPanel bar, double normalizedValue) {
        if (normalizedValue < 0 || normalizedValue > 1) {
            throw new IllegalArgumentException("Sequence values have to be between 0.0 and 1.0");
        }
        LOG.trace("Changing reference height of '{}' to {}", bar.getName(), normalizedValue);

        final int barRange = MAX_HEIGHT - MIN_HEIGHT;
        int newHeight = (int) Math.round(barRange * normalizedValue);
        newHeight += MIN_HEIGHT;
        bar.setMaximumSize(new Dimension(WIDTH, newHeight));
        bar.revalidate();
    }

    private void changeForceHeight(JPanel bar, double normalizedValue) {
        LOG.trace("Changing force height of '{}' to {}", bar.getName(), normalizedValue);
        int newHeight = (int) Math.round(MAX_HEIGHT * normalizedValue);
        newHeight = Math.min(newHeight, MAX_HEIGHT);
        newHeight = Math.max(newHeight, MIN_HEIGHT);
        bar.setMaximumSize(new Dimension(WIDTH, newHeight));
        bar.revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        LOG.trace("Drawing bars");
    }

    public void setColours(SessionConfig.ColoursConfig colours) {
        leftForceBar.setBackground(Color.decode('#' + colours.getLeftForce()));
        leftReferenceBar.setBackground(Color.decode('#' + colours.getLeftReference()));
        rightReferenceBar.setBackground(Color.decode('#' + colours.getRightReference()));
        rightForceBar.setBackground(Color.decode('#' + colours.getRightForce()));
    }

    public void showLeftBars(boolean show) {
        leftReferenceBar.setVisible(show);
        leftForceBar.setVisible(show);
        space1.setVisible(show);
        space2.setVisible(show);
    }

    public void showRightBars(boolean show) {
        rightReferenceBar.setVisible(show);
        rightForceBar.setVisible(show);
        space2.setVisible(show);
        space3.setVisible(show);
    }
}
