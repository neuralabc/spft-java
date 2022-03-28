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
    private final String sectionName;
    private final Path outputFile;
    private final Map<String, Object> data = new LinkedHashMap<>();

    public OutputSection(String sectionName, Path outputFile) {
        this.sectionName = sectionName;
        this.outputFile = outputFile;
    }

    public void addEntry(String key, Object value) {
        data.put(key, value);
    }

    public void addEntryElement(String key, Object element) {
        if (data.containsKey(key)) {
            Collection<Object> list = (List<Object>) data.get(key);
            list.add(element);
        } else {
            Collection<Object> newArray = new ArrayList<>();
            newArray.add(element);
            data.put(key, newArray);
        }
    }

    public void write() throws IOException {
        if (!Files.exists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bufferedWriter.write("### " + sectionName + " ###");
            bufferedWriter.newLine();
            for (var entry : data.entrySet()) {
                bufferedWriter.write(entry.getKey() + ": " + entry.getValue().toString());
                bufferedWriter.newLine();
            }
        }
    }
}
