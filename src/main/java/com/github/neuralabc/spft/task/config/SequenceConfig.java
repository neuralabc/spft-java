package com.github.neuralabc.spft.task.config;

import java.util.List;

/**
 * Configuration DTO for Sequences. Sequences hold presentation details
 */
public class SequenceConfig {
    private int frequency;
    private List<Double> valuesLeft;
    private List<Double> valuesRight;

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<Double> getValuesLeft() {
        return valuesLeft;
    }

    public void setValuesLeft(List<Double> valuesLeft) {
        this.valuesLeft = valuesLeft;
        if (valuesRight != null && valuesLeft.size() != valuesRight.size()) {
            throw new IllegalStateException("Left and right sequences are not the same length. Make them the same length by padding the shorter one");
        }
    }

    public List<Double> getValuesRight() {
        return valuesRight;
    }

    public void setValuesRight(List<Double> valuesRight) {
        this.valuesRight = valuesRight;
        if (valuesLeft != null && valuesLeft.size() != valuesRight.size()) {
            throw new IllegalStateException("Left and right sequences are not the same length. Make them the same length by padding the shorter one");
        }
    }

    public int getLength() {
        if (valuesLeft == null) {
            return valuesRight.size();
        } else {
            return valuesLeft.size();
        }
    }
}
