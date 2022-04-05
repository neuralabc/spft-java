package com.github.neuralabc.spft.task.config;

import java.util.List;
import java.util.Map;

/**
 * Configuration DTO for Session. Sessions are the root configurations and contain blocks
 */
public class SessionConfig {
    private String path;
    private String sessionName;
    private String outputSuffix;
    private int interBlockInterval;
    private List<BlockConfig> blocks;
    private Map<String, SequenceConfig> sequences;
    private ColoursConfig colours = new ColoursConfig(); // start with default colours

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(final String sessionName) {
        this.sessionName = sessionName;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void setOutputSuffix(final String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }

    public int getInterBlockInterval() {
        return interBlockInterval;
    }

    public void setInterBlockInterval(int interBlockInterval) {
        this.interBlockInterval = interBlockInterval;
    }

    public List<BlockConfig> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockConfig> blocks) {
        this.blocks = blocks;
    }

    public Map<String, SequenceConfig> getSequences() {
        return sequences;
    }

    public void setSequences(Map<String, SequenceConfig> sequences) {
        this.sequences = sequences;
    }

    public ColoursConfig getColours() {
        return colours;
    }

    public void setColours(ColoursConfig colours) {
        this.colours = colours;
    }

    public static class ColoursConfig {
        private String leftReference = "1D8348";
        private String rightReference = "76448A";
        private String leftForce = "82E0AA";
        private String rightForce = "D7BDE2";

        public String getLeftReference() {
            return leftReference;
        }

        public void setLeftReference(String leftReference) {
            this.leftReference = leftReference;
        }

        public String getRightReference() {
            return rightReference;
        }

        public void setRightReference(String rightReference) {
            this.rightReference = rightReference;
        }

        public String getLeftForce() {
            return leftForce;
        }

        public void setLeftForce(String leftForce) {
            this.leftForce = leftForce;
        }

        public String getRightForce() {
            return rightForce;
        }

        public void setRightForce(String rightForce) {
            this.rightForce = rightForce;
        }
    }
}