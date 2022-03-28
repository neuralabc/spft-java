package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.OutputException;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * An experiment run. Composed of blocks
 */
public class Session implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private final SessionConfig config;
    private final List<Block> blocks;
    private ExperimentFrame.Binding uiBinding;
    private Path outputFile;

    public Session(File selectedFile) {
        Yaml yaml = new Yaml(new Constructor(SessionConfig.class));
        try {
            LOG.debug("Trying to load session with config '{}'", selectedFile);
            config = yaml.load(new FileInputStream(selectedFile));
            LOG.info("Successfully loaded session '{}'", config.getSessionName());
            config.setPath(selectedFile.getAbsolutePath());

            blocks = config.getBlocks().stream().map(blockConfig -> new Block(blockConfig, config.getSequences())).collect(Collectors.toList());
        } catch (FileNotFoundException ex) {
            throw new SessionException("Error opening configuration", ex);
        }
    }

    public SessionConfig getConfig() {
        return config;
    }

    public void start(String participantId, String outputFile, ExperimentFrame.Binding binding) throws IOException {
        LOG.info("Starting session '{}' from {}", config.getSessionName(), config.getPath());
        this.outputFile = Path.of(outputFile);
        writeSessionMetadata(participantId);
        uiBinding = binding;
        new Thread(this, config.getSessionName().replaceAll(" ", "-")).start();

    }

    private void writeSessionMetadata(String participantId) throws IOException {
        if (Files.exists(outputFile)) {
            LOG.warn("Overwriting file {}", outputFile);
            Files.delete(outputFile);
        }
        OutputSection output = new OutputSection("Session", outputFile);
        output.addEntry("sessionName", config.getSessionName());
        output.addEntry("startTime", Instant.now());
        output.addEntry("configurationFile", config.getPath());
        output.addEntry("configurationChecksum", computeConfigChecksum());
        output.addEntry("participantId", participantId);
        output.write();
    }

    private String computeConfigChecksum() {
        try (InputStream inputStream = Files.newInputStream(Path.of(config.getPath()))) {

            final int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            MessageDigest complete = MessageDigest.getInstance("MD5");

            int bytesRead;
            do {
                bytesRead = inputStream.read(buffer);
                if (bytesRead > 0) {
                    complete.update(buffer, 0, bytesRead);
                }
            } while (bytesRead != -1);


            StringBuilder builder = new StringBuilder();
            for (byte b : complete.digest()) {
                builder.append(String.format("%02X", b).toLowerCase(Locale.ROOT));
            }

            return builder.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new OutputException("Error creating session output", e);
        }
    }

    @Override
    public void run() {
        try {
            for (int currentBlock = 0; currentBlock < config.getBlocks().size(); currentBlock++) {
                Block nextBlock = blocks.get(currentBlock);

                writeBlockMetadata(nextBlock, currentBlock + 1);
                nextBlock.run(uiBinding, outputFile);
            }
            LOG.info("Session '{}' ended successfully", getConfig().getSessionName());
        } catch (InterruptedException e) {
            LOG.error("Interrupted session {}", config.getSessionName(), e);
        } catch (IOException e) {
            LOG.error("Problem writing output to {}", outputFile, e);
        }
    }

    private void writeBlockMetadata(Block nextBlock, int blockPosition) throws IOException {
        OutputSection blockOutput = new OutputSection("Block " + blockPosition, outputFile);
        blockOutput.addEntry("blockName", nextBlock.getName());
        blockOutput.write();
    }
}
