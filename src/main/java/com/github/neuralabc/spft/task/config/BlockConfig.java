package com.github.neuralabc.spft.task.config;

import java.util.List;

/**
 * Configuration DTO for Blocks. Blocks contain trials
 */
public class BlockConfig {
    private String name;
    private int interTrialInterval;
    private String instructions;
    private int instructionsDuration;
    private String feedback;
    private int feedbackDuration;
    private List<TrialConfig> trials;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterTrialInterval() {
        return interTrialInterval;
    }

    public void setInterTrialInterval(int interTrialInterval) {
        this.interTrialInterval = interTrialInterval;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public List<TrialConfig> getTrials() {
        return trials;
    }

    public void setTrials(List<TrialConfig> trials) {
        this.trials = trials;
    }

    public int getInstructionsDuration() {
        return instructionsDuration;
    }

    public void setInstructionsDuration(int instructionsDuration) {
        this.instructionsDuration = instructionsDuration;
    }

    public int getFeedbackDuration() {
        return feedbackDuration;
    }

    public void setFeedbackDuration(int feedbackDuration) {
        this.feedbackDuration = feedbackDuration;
    }
}
