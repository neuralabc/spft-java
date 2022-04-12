package com.github.neuralabc.spft.task.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * An atomic part of the output
 */
public class OutputSection {
    private static final double NANOS_IN_MILLI = 1e6;
    private final String indentation;
    private final Map<String, Object> entries = new LinkedHashMap<>();
    private final Map<String, List<Object>> timeSeries = new LinkedHashMap<>();

    public OutputSection() {
        this(0);
    }

    public OutputSection(int indentationCount) {
        indentation = "  ".repeat(indentationCount);
    }

    public void addEntry(String key, Object value) {
        entries.put(key, value);
    }

    public void addSample(Object sampleValue) {
        if (timeSeries.containsKey("  times")) {
            Collection<Object> timesSeries = timeSeries.get("  times");
            String elapsedMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
            timesSeries.add(elapsedMillis);

            Collection<Object> valueSeries = timeSeries.get("  values");
            valueSeries.add(sampleValue);
        } else {
            List<Object> newArray = new ArrayList<>();
            String elapsedMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
            newArray.add(elapsedMillis);
            timeSeries.put("  times", newArray);

            List<Object> newArrayValue = new ArrayList<>();
            newArrayValue.add(sampleValue);
            timeSeries.put("  values", newArrayValue);
        }
    }

    public void write(Path outputFile) throws IOException {
        if (!Files.exists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (var entry : entries.entrySet()) {
                bufferedWriter.write(indentation + entry.getKey() + ": " + entry.getValue().toString());
                bufferedWriter.newLine();
            }
            for (var entry : timeSeries.entrySet()) {
                bufferedWriter.write(indentation + entry.getKey() + ": " + entry.getValue().toString());
                bufferedWriter.newLine();
            }
        }
    }
}
