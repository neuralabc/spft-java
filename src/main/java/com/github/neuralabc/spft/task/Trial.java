package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.task.config.TrialConfig;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * An actual task for subjects to perform
 */
public class Trial {
    private static final Logger LOG = LoggerFactory.getLogger(Trial.class);
    private static final double NANOS_IN_MILLI = 1e6;
    private final TrialConfig config;
    private final SequenceConfig sequence;

    public Trial(TrialConfig config, Map<String, SequenceConfig> sequencesPool) {
        this.config = config;
        this.sequence = sequencesPool.get(config.getSequenceRef());
    }

    public void run(ExperimentFrame.Binding binding, OutputSection trialOutput) throws InterruptedException {
        LOG.info("\t\tStarting trial '{}'", config.getName());
        for (int sequencePos = 0; sequencePos < sequence.getLength(); sequencePos++) {
            if (sequence.getValuesLeft() != null) {
                double referenceLeftValue = sequence.getValuesLeft().get(sequencePos);
                trialOutput.addEntryElement("referenceLeftValues", referenceLeftValue);
                String timestamp = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
                trialOutput.addEntryElement("referenceLeftTimes", timestamp); //TODO: confirm this. it is misleading since it's not the draw timestamp
                binding.setLeftReferenceValue(referenceLeftValue);
            }
            if (sequence.getValuesRight() != null) {
                double referenceRightValue = sequence.getValuesRight().get(sequencePos);
                trialOutput.addEntryElement("referenceRightValues", referenceRightValue);
                String timestamp = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
                trialOutput.addEntryElement("referenceRightTimes", timestamp);
                binding.setRightReferenceValue(referenceRightValue);
            }

            int delay = 1000 / sequence.getFrequency();
            Thread.sleep(delay);
        }
    }

    public String getName() {
        return config.getName();
    }
}
