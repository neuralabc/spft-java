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
    private final String sectionName;
    private final Map<String, Object> entries = new LinkedHashMap<>();
    private final Map<String, List<Object>> timeSeries = new LinkedHashMap<>();

    public OutputSection(String sectionName) {
        this.sectionName = sectionName;
    }

    public void addEntry(String key, Object value) {
        entries.put(key, value);
    }

    public void addSample(String key, double sampleValue) {
        if (timeSeries.containsKey(key + "Times")) {
            Collection<Object> timesSeries = timeSeries.get(key + "Times");
            String elapsedMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
            timesSeries.add(elapsedMillis);

            Collection<Object> valueSeries = timeSeries.get(key + "Values");
            valueSeries.add(sampleValue);
        } else {
            List<Object> newArray = new ArrayList<>();
            String elapsedMillis = String.format("%.2f", System.nanoTime() / NANOS_IN_MILLI);
            newArray.add(elapsedMillis);
            timeSeries.put(key + "Times", newArray);

            List<Object> newArrayValue = new ArrayList<>();
            newArrayValue.add(sampleValue);
            timeSeries.put(key + "Values", newArrayValue);
        }
    }

    public void write(Path outputFile) throws IOException {
        if (!Files.exists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bufferedWriter.write("### " + sectionName + " ###");
            bufferedWriter.newLine();
            for (var entry : entries.entrySet()) {
                bufferedWriter.write(entry.getKey() + ": " + entry.getValue().toString());
                bufferedWriter.newLine();
            }
            for (var entry : timeSeries.entrySet()) {
                bufferedWriter.write(entry.getKey() + ": " + entry.getValue().toString());
                bufferedWriter.newLine();
            }
        }
    }
}
