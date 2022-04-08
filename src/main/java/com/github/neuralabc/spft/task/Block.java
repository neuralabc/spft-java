package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.BlockConfig;
import com.github.neuralabc.spft.task.config.SequenceConfig;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
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
        trials = config.getTrials().stream().map(trialConfig -> new Trial(trialConfig, sequencesPool)).collect(Collectors.toList());
    }

    public void run(ExperimentFrame.Binding binding, Path outputFile) throws InterruptedException, IOException {
        LOG.info("\tStarting block '{}'", config.getName());

        binding.showLeftBars(trials.get(0).hasLeftSequence());
        binding.showRightBars(trials.get(0).hasRightSequence());
        binding.showText(config.getInstructions());
        Thread.sleep(config.getInstructionsDuration());
        binding.showText("");

        for (int currentTrial = 0; currentTrial < config.getTrials().size(); currentTrial++) {
            Trial nextTrial = trials.get(currentTrial);

            OutputSection trialMetadataOutput = new OutputSection(2);
            trialMetadataOutput.addEntry("- trialName", nextTrial.getName());
            trialMetadataOutput.write(outputFile);
            nextTrial.run(binding, outputFile);
            if (currentTrial < config.getTrials().size() - 1) {
                binding.showLeftBars(trials.get(currentTrial + 1).hasLeftSequence());
                binding.showRightBars(trials.get(currentTrial + 1).hasRightSequence());
                LOG.debug("Starting inter-trial interval");
                Thread.sleep(config.getInterTrialInterval());
            }
        }

        binding.showText(config.getFeedback());
        Thread.sleep(config.getFeedbackDuration());
        binding.showText("");

        LOG.info("\tBlock '{}' terminated", config.getName());
    }

    public String getName() {
        return config.getName();
    }
}
