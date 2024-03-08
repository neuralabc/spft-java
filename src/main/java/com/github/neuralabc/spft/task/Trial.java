package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.hardware.TriggerSender;
import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.task.config.TrialConfig;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * An actual task for subjects to perform
 */
public class Trial {
    private static final Logger LOG = LoggerFactory.getLogger(Trial.class);
    private final TrialConfig config;
    private final TriggerSender triggerSender;
    private final SequenceConfig sequence;
    private final CountDownLatch sync;
    private OutputSection leftReferenceOutput;
    private OutputSection rightReferenceOutput;

    public Trial(TrialConfig config, Map<String, SequenceConfig> sequencesPool, TriggerSender triggerSender) {
        this.config = config;
        this.triggerSender = triggerSender;
        this.sequence = sequencesPool.get(config.getSequenceRef());
        if (hasLeftSequence()) {
            String sectionName = "leftReference";
            leftReferenceOutput = new OutputSection(3);
            leftReferenceOutput.addEntry(sectionName, "");
        }
        if (hasRightSequence()) {
            String sectionName = "rightReference";
            rightReferenceOutput = new OutputSection(3);
            rightReferenceOutput.addEntry(sectionName, "");
        }

        sync = new CountDownLatch(1);
    }

    public void run(ExperimentFrame.Binding binding, Path outputFile) throws InterruptedException, IOException {
        LOG.info("\t\tStarting trial '{}'", config.getName());
        LOG.info("\t\t\tPresentation frequency: {} ms/value, {} Hz", sequence.getFrequency(), String.format("%.2f",1f/sequence.getFrequency()*1000));
        
        int delay = sequence.getFrequency();
        Presentation presentation = new Presentation(binding);
        Timer timer = new Timer(delay, presentation);
        timer.setInitialDelay(0);
        timer.setRepeats(true);

        triggerSender.sendStart(); // trigger to indicate start of trial

        timer.start();

        sync.await();

        triggerSender.sendStop(); // trigger to indicate stop (no different from start trigger)
        
        timer.stop();
        writeOutput(outputFile);

        // at the end of every trial we also reset the reference values to the middle (0.5)
        // to indicate the trial has ended and ensure that the participant does not move
        // to the last position of the previous trial
        binding.setLeftReferenceValue(0.5); //TODO: could implement a flash to indicate the end of the block
        binding.setRightReferenceValue(0.5);
    }

    private void writeOutput(Path outputFile) throws IOException {
        if (leftReferenceOutput != null) {
            leftReferenceOutput.write(outputFile);
        }
        if (rightReferenceOutput != null) {
            rightReferenceOutput.write(outputFile);
        }
    }

    public String getName() {
        return config.getName();
    }

    public boolean hasLeftSequence() {
        return sequence.getValuesLeft() != null;
    }

    public boolean hasRightSequence() {
        return sequence.getValuesRight() != null;
    }

    private class Presentation implements ActionListener {
        private final ExperimentFrame.Binding binding;
        private int sequencePos;

        public Presentation(ExperimentFrame.Binding binding) {
            this.binding = binding;
            sequencePos = 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (sequencePos < sequence.getLength()) {
                if (hasLeftSequence()) {
                    double leftReferenceValue = sequence.getValuesLeft().get(sequencePos);
                    leftReferenceOutput.addSample(leftReferenceValue);
                    binding.setLeftReferenceValue(leftReferenceValue);
                }
                if (hasRightSequence()) {
                    double rightReferenceValue = sequence.getValuesRight().get(sequencePos);
                    rightReferenceOutput.addSample(rightReferenceValue);
                    binding.setRightReferenceValue(rightReferenceValue);
                }
                sequencePos++;
            } else {
                // Stop the timer and signal the end of the trial
                // ((Timer) e.getSource()).stop(); //removed timer stop here, it is handled in the run above 
                sync.countDown();
            }
        }
        // original code, to be removed AFTER testing confirms that there is no additional change
        // @Override
        // public void actionPerformed(ActionEvent e) {
        //     if (sequencePos >= sequence.getLength()) {
        //         //just in case timer is not cancelled before a next tick
        //         return;
        //     }
        //     if (hasLeftSequence()) {
        //         double leftReferenceValue = sequence.getValuesLeft().get(sequencePos);
        //         leftReferenceOutput.addSample(leftReferenceValue);
        //         binding.setLeftReferenceValue(leftReferenceValue);
        //     }
        //     if (hasRightSequence()) {
        //         double rightReferenceValue = sequence.getValuesRight().get(sequencePos);
        //         rightReferenceOutput.addSample(rightReferenceValue);
        //         binding.setRightReferenceValue(rightReferenceValue);
        //     }
        //     sequencePos++;
        //     if (sequencePos >= sequence.getLength()) {
        //         sync.countDown();
        //     }
        // }
    }
}
