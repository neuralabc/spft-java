package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.BlockConfig;
import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A grouping of trials
 */
public class Block {
    private static final Logger LOG = LoggerFactory.getLogger(Block.class);
    private final BlockConfig config;
    private final List<Trial> trials;

    public Block(BlockConfig config, Map<String, SequenceConfig> sequencesPool) {
        this.config = config;
        trials = config.getTrials().stream().map(x -> new Trial(x, sequencesPool)).collect(Collectors.toList());
    }

    public void run(ExperimentFrame.Binding binding) throws InterruptedException {
        LOG.info("\tStarting block '{}'", config.getName());

        binding.showText(config.getInstructions());
        Thread.sleep(config.getInstructionsDuration());
        binding.showText("");

        for (int currentTrial = 0; currentTrial < config.getTrials().size(); currentTrial++) {
            trials.get(currentTrial).run(binding);
        }

        binding.showText(config.getFeedback());
        Thread.sleep(config.getFeedbackDuration());
        binding.showText("");

        LOG.info("\tBlock '{}' terminated", config.getName());
    }
}
