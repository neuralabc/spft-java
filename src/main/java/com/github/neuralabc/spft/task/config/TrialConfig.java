package com.github.neuralabc.spft.task.config;

/**
 * Configuration DTO for Trials. Trials contain the presentation sequences
 */
public class TrialConfig {
    private String name;
    private String sequenceRef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequenceRef() {
        return sequenceRef;
    }

    public void setSequenceRef(String sequenceRef) {
        this.sequenceRef = sequenceRef;
    }
}
