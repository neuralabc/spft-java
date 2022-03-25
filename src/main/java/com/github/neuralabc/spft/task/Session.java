package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An experiment run. Composed of blocks
 */
public class Session implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private final SessionConfig config;
    private final List<Block> blocks;
    private ExperimentFrame.Binding uiBinding;

    public Session(File selectedFile) {
        Yaml yaml = new Yaml(new Constructor(SessionConfig.class));
        try {
            LOG.debug("Trying to load session with config '{}'", selectedFile);
            config = yaml.load(new FileInputStream(selectedFile));
            LOG.info("Successfully loaded session '{}'", config.getSessionName());
            config.setPath(selectedFile.getAbsolutePath());

            blocks = config.getBlocks().stream().map(x -> new Block(x, config.getSequences())).collect(Collectors.toList());
        } catch (FileNotFoundException ex) {
            throw new SessionException("Error opening configuration", ex);
        }
    }

    public SessionConfig getConfig() {
        return config;
    }

    public void start(ExperimentFrame.Binding binding) {
        LOG.info("Starting session '{}' from {}", config.getSessionName(), config.getPath());
        uiBinding = binding;
        new Thread(this, config.getSessionName().replaceAll(" ", "-")).start();

    }

    @Override
    public void run() {
        try {
            for (int currentBlock = 0; currentBlock < config.getBlocks().size(); currentBlock++) {
                blocks.get(currentBlock).run(uiBinding);
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted session {}", config.getSessionName(), e);
        }
    }
}
