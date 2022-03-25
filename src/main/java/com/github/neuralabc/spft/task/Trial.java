package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.task.config.TrialConfig;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * An actual task for subjects to perform
 */
public class Trial {
    private static final Logger LOG = LoggerFactory.getLogger(Trial.class);
    private final TrialConfig config;
    private final SequenceConfig sequence;

    public Trial(TrialConfig config, Map<String, SequenceConfig> sequencesPool) {
        this.config = config;
        this.sequence = sequencesPool.get(config.getSequenceRef());
    }

    public void run(ExperimentFrame.Binding binding) throws InterruptedException {
        LOG.info("\t\tStarting trial '{}'", config.getName());
        for (int sequencePos = 0; sequencePos < sequence.getLength(); sequencePos++) {
            if (sequence.getValuesLeft() != null) {
                binding.setLeftReferenceValue(sequence.getValuesLeft().get(sequencePos));
            }
            if (sequence.getValuesRight() != null) {
                binding.setRightReferenceValue(sequence.getValuesRight().get(sequencePos));
            }

            int delay = 1000 / sequence.getFrequency();
            Thread.sleep(delay);
        }
    }
}
