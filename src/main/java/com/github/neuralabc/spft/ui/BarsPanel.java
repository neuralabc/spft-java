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
    private final int maxHeight;
    private final int barWidth;
    private static final int MIN_HEIGHT = 20;
    private final JPanel leftForceBar;
    private final JPanel leftReferenceBar;
    private final JPanel rightForceBar;
    private final JPanel rightReferenceBar;
    private final Component space1;
    private final Component space2;
    private final Component space3;
    private double forceRangeMin = 0;
    private double forceRangeMax = 1;

    public BarsPanel() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double heightFactor = 0.463;
        int calculatedMaxHeight = (int) Math.round(heightFactor * screenSize.height);
        maxHeight = Integer.getInteger("spft.ui.bars.maxHeight", calculatedMaxHeight);

        final double widthFactor = 0.052;
        int calculatedWidth = (int) Math.round(widthFactor * screenSize.width);
        barWidth = Integer.getInteger("spft.ui.bars.width", calculatedWidth);

        final double separationFactor = 0.08;
        int calculatedSeparation = (int) Math.round(separationFactor * screenSize.width);
        int barSeparation = Integer.getInteger("spft.ui.bars.separation", calculatedSeparation);
        Dimension separator = new Dimension(barSeparation, 0);

        JPanel panel = new JPanel();
        add(panel);

        if (Boolean.getBoolean("debug")) {
            setBackground(Color.RED);
            panel.setBackground(Color.ORANGE);
        }

        LayoutManager boxLayout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(boxLayout);

        Dimension barSize = new Dimension(barWidth, maxHeight);
        Dimension minSize = new Dimension(barWidth, MIN_HEIGHT);

        leftForceBar = new JPanel();
        leftForceBar.setName("Left Force Bar");
        leftForceBar.setPreferredSize(barSize);
        leftForceBar.setMaximumSize(barSize);
        leftForceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        panel.add(Box.createHorizontalGlue());

        leftReferenceBar = new JPanel();
        leftReferenceBar.setName("Left Reference Bar");
        leftReferenceBar.setPreferredSize(minSize);
        leftReferenceBar.setMaximumSize(barSize);
        leftReferenceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space1 = Box.createRigidArea(separator);

        boolean referenceOutside = Boolean.getBoolean("spft.ui.bars.referenceOutside");
        if (referenceOutside) {
            panel.add(leftReferenceBar);
        } else {
            panel.add(leftForceBar);
        }

        panel.add(space1);

        if (referenceOutside) {
            panel.add(leftForceBar);
        } else {
            panel.add(leftReferenceBar);
        }

        rightReferenceBar = new JPanel();
        rightReferenceBar.setName("Right Reference Bar");
        rightReferenceBar.setPreferredSize(minSize);
        rightReferenceBar.setMaximumSize(barSize);
        rightReferenceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space2 = Box.createRigidArea(new Dimension((int) Math.round(separator.width * 1.50), separator.height));
        panel.add(space2);

        rightForceBar = new JPanel();
        rightForceBar.setName("Right Force Bar");
        rightForceBar.setPreferredSize(minSize);
        rightForceBar.setMaximumSize(barSize);
        rightForceBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        space3 = Box.createRigidArea(separator);

        if (referenceOutside) {
            panel.add(rightForceBar);
        } else {
            panel.add(rightReferenceBar);
        }
        panel.add(space3);
        if (referenceOutside) {
            panel.add(rightReferenceBar);
        } else {
            panel.add(rightForceBar);
        }

        panel.add(Box.createHorizontalGlue());
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

        final int barRange = maxHeight - MIN_HEIGHT;
        int newHeight = (int) Math.round(barRange * normalizedValue);
        newHeight += MIN_HEIGHT;
        bar.setMaximumSize(new Dimension(barWidth, newHeight));
        bar.revalidate();
    }

    private void changeForceHeight(JPanel bar, double mvcNormalizedValue) {
        LOG.trace("Changing force height of '{}' to {}", bar.getName(), mvcNormalizedValue);

        final double barRange = forceRangeMax - forceRangeMin;
        double newValue = (mvcNormalizedValue - forceRangeMin) / barRange;
        int newHeight = (int) Math.round(maxHeight * newValue);
        newHeight = Math.min(newHeight, maxHeight);
        newHeight = Math.max(newHeight, MIN_HEIGHT);
        bar.setMaximumSize(new Dimension(barWidth, newHeight));
        bar.revalidate();
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

    public void setForceRange(SessionConfig.ForceRange forceRange) {
        this.forceRangeMin = forceRange.getMin();
        this.forceRangeMax = forceRange.getMax();
    }
}
