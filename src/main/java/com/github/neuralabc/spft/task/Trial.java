package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.task.config.TrialConfig;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * An actual task for subjects to perform
 */
public class Trial {
    private static final Logger LOG = LoggerFactory.getLogger(Trial.class);
    private final TrialConfig config;
    private final SequenceConfig sequence;
    private final CountDownLatch sync;

    public Trial(TrialConfig config, Map<String, SequenceConfig> sequencesPool) {
        this.config = config;
        this.sequence = sequencesPool.get(config.getSequenceRef());
        sync = new CountDownLatch(1);
    }

    public void run(ExperimentFrame.Binding binding, OutputSection trialOutput) throws InterruptedException {
        LOG.info("\t\tStarting trial '{}'", config.getName());

        int delay = 1000 / sequence.getFrequency();

        Presentation presentation = new Presentation(trialOutput, binding);
        Timer timer = new Timer(delay, presentation);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        timer.start();

        sync.await();
        timer.stop();
    }

    public String getName() {
        return config.getName();
    }

    private class Presentation implements ActionListener {
        private final OutputSection trialOutput;
        private final ExperimentFrame.Binding binding;
        private int sequencePos;

        public Presentation(OutputSection trialOutput, ExperimentFrame.Binding binding) {
            this.trialOutput = trialOutput;
            this.binding = binding;
            sequencePos = 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (sequencePos >= sequence.getLength()) {
                //just in case timer is not cancelled before a next tick
                return;
            }
            if (sequence.getValuesLeft() != null) {
                double leftReferenceValue = sequence.getValuesLeft().get(sequencePos);
                trialOutput.addSample("leftReference", leftReferenceValue);
                binding.setLeftReferenceValue(leftReferenceValue);
            }
            if (sequence.getValuesRight() != null) {
                double rightReferenceValue = sequence.getValuesRight().get(sequencePos);
                trialOutput.addSample("rightReference", rightReferenceValue);
                binding.setRightReferenceValue(rightReferenceValue);
            }
            sequencePos++;
            if (sequencePos >= sequence.getLength()) {
                sync.countDown();
            }
        }
    }
}
