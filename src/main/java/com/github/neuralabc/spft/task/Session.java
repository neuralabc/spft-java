package com.github.neuralabc.spft.task;

import com.github.neuralabc.spft.hardware.ForceGauge;
import com.github.neuralabc.spft.hardware.TriggerSender;
import com.github.neuralabc.spft.hardware.TriggerTracker;
import com.github.neuralabc.spft.task.config.SessionConfig;
import com.github.neuralabc.spft.task.exceptions.OutputException;
import com.github.neuralabc.spft.task.exceptions.SessionException;
import com.github.neuralabc.spft.task.output.OutputSection;
import com.github.neuralabc.spft.ui.ExperimentFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

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
    private static final double NANOS_IN_MILLI = 1e6;
    private final SessionConfig config;
    private final List<Block> blocks;
    private ExperimentFrame.Binding uiBinding;
    private Path outputFile;
    private ForceGauge leftDevice;
    private ForceGauge rightDevice;
    private final TriggerTracker triggerTracker;
    private TriggerSender triggerSender;
    private Thread thread;

    public Session(File selectedFile) {
        Yaml yaml = new Yaml(new Constructor(SessionConfig.class));
        try {
            LOG.debug("Trying to load session with config '{}'", selectedFile);
            config = yaml.load(new FileInputStream(selectedFile));
            config.validate();
            LOG.info("Successfully loaded session '{}'", config.getSessionName());
            config.setPath(selectedFile.getAbsolutePath());
            if (isTriggerStarted()) {
                triggerTracker = new TriggerTracker(config.getTriggers());
            } else {
                triggerTracker = TriggerTracker.NO_TRIGGERS;
            }
            triggerSender = new TriggerSender("triggerDevice","Disabled"); // create a dummy instance of triggerSender that is disabled, before we know what port to look in
            blocks = config.getBlocks().stream().map(blockConfig -> new Block(blockConfig, config.getSequences(), triggerSender)).collect(Collectors.toList());
        } catch (FileNotFoundException ex) {
            throw new SessionException("Error opening configuration", ex);
        } catch (YAMLException ex) {
            throw new SessionException("Error loading session configuration. See output log for more details", ex);
        }
    }

    public SessionConfig getConfig() {
        return config;
    }

    public void start(SessionParameters sessionParameters, ExperimentFrame.Binding binding) throws IOException {
        LOG.info("Starting session '{}' from {}", config.getSessionName(), config.getPath());
        leftDevice = new ForceGauge("leftDevice", sessionParameters.forceDevicesPorts().get(0), sessionParameters.maximumLeftContraction, binding);
        if (sessionParameters.forceDevicesPorts().get(1).equals(sessionParameters.forceDevicesPorts().get(0))) {
            rightDevice = ForceGauge.DISABLED_DEVICE;
        } else {
            rightDevice = new ForceGauge("rightDevice", sessionParameters.forceDevicesPorts().get(1), sessionParameters.maximumRightContraction, binding);
        }
        if (!leftDevice.isEnabled() && !rightDevice.isEnabled()) {
            LOG.warn("All devices are disabled. There will be no force data");
        }
        // triggerSender = new TriggerSender("triggerDevice",sessionParameters.usedTriggerPort(),binding);
        triggerSender.setPort(sessionParameters.usedTriggerPort()); // update the port for the triggerSender
        if (!triggerSender.isEnabled()) {
            LOG.warn("Trigger device is not present. No triggering to external device.");
        } else {
            LOG.info("Trigger device is present and available.");
            LOG.info("\t{}",triggerSender);
        }
        this.outputFile = Path.of(sessionParameters.outputFile());
        writeSessionMetadata(sessionParameters);
        uiBinding = binding;
        thread = new Thread(this, config.getSessionName().replaceAll(" ", "-"));
        thread.start();
    }

    public void cancel() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void writeSessionMetadata(SessionParameters sessionParameters) throws IOException {
        if (Files.exists(outputFile)) {
            LOG.warn("Overwriting file {}", outputFile);
            Files.delete(outputFile);
        }
        OutputSection output = new OutputSection();
        output.addEntry("sessionName", config.getSessionName());
        output.addEntry("startTime", Instant.now());
        output.addEntry("configurationFile", config.getPath());
        output.addEntry("configurationChecksum", computeConfigChecksum());
        output.addEntry("participantId", sessionParameters.participantId);
        if (sessionParameters.maximumLeftContraction != -1) {
            output.addEntry("maximumLeftVoluntaryContraction", sessionParameters.maximumLeftContraction);
        }
        if (sessionParameters.maximumRightContraction != -1) {
            output.addEntry("maximumRightVoluntaryContraction", sessionParameters.maximumRightContraction);
        }
        output.addEntry("forceProportionRange", "");
        output.addEntry("  min", config.getForceProportionRange().getMin());
        output.addEntry("  max", config.getForceProportionRange().getMax());
        output.addEntry("blocks", "");
        output.write(outputFile);
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
            triggerTracker.start();
            triggerTracker.waitNext();
            leftDevice.start();
            rightDevice.start();
            triggerSender.start();
            for (int currentBlock = 0; currentBlock < config.getBlocks().size(); currentBlock++) {
                Block nextBlock = blocks.get(currentBlock);

                writeBlockMetadata(nextBlock);
                nextBlock.run(uiBinding, outputFile);
                writeBlockEndMetadata();
                if (currentBlock < config.getBlocks().size() - 1) {
                    LOG.debug("Starting inter-block interval");
                    Thread.sleep(config.getInterBlockInterval());
                }
            }
            leftDevice.stop();
            rightDevice.stop();
            triggerTracker.stop();
            uiBinding.showLeftBars(true);
            uiBinding.showRightBars(true);

            if (leftDevice.isEnabled() || rightDevice.isEnabled()) {
                OutputSection devices = new OutputSection();
                devices.addEntry("devices", "");
                devices.write(outputFile);
                leftDevice.writeOutput(outputFile);
                rightDevice.writeOutput(outputFile);
            }

            if (triggerTracker.isEnabled()) {
                OutputSection triggers = new OutputSection();
                triggers.addEntry("triggers", "");
                triggers.write(outputFile);
                triggerTracker.writeOutput(outputFile);
            }

            LOG.info("Session '{}' ended successfully", getConfig().getSessionName());
        } catch (InterruptedException e) {
            LOG.warn("Interrupted session '{}'", config.getSessionName(), e);
        } catch (IOException e) {
            LOG.error("Problem writing output to {}", outputFile, e);
        } finally {
            triggerSender.stop();
            LOG.info("Trigger Sender stopped successfully.");
        }
    }

    private void writeBlockMetadata(Block nextBlock) throws IOException {
        OutputSection blockOutput = new OutputSection(1);
        blockOutput.addEntry("- blockName", nextBlock.getName());
        String startMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
        blockOutput.addEntry("  startTimestamp", startMillis);
        blockOutput.addEntry("  trials", "");
        blockOutput.write(outputFile);
    }

    private void writeBlockEndMetadata() throws IOException {
        OutputSection blockOutput = new OutputSection(1);
        String endMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
        blockOutput.addEntry("  endTimestamp", endMillis);
        blockOutput.write(outputFile);
    }

    private boolean isTriggerStarted() {
        List<String> triggers = config.getTriggers();
        return triggers != null && !triggers.isEmpty();
    }

    public record SessionParameters(String participantId, String outputFile, List<String> forceDevicesPorts,
                                    int maximumLeftContraction, int maximumRightContraction, String usedTriggerPort) {
    }
}
