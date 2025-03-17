package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowLogTagger {

    private static final String LOOKUP_FILE = "src/main/data/lookup.txt";
    private static final String FLOW_LOG_FILE = "src/main/data/flow_logs.csv";
    private static final String OUTPUT_FILE = "src/main/data/output.txt";

    // Map to store lookup table (key = "dstport-protocol", value = List of tags)
    private static final Map<String, List<String>> lookupTable = new HashMap<>();
    // Map to store tag counts
    private static final Map<String, Integer> tagCount = new HashMap<>();
    // Map to store port/protocol combination counts
    private static final Map<String, Integer> portProtocolCount = new HashMap<>();

    public static void main(String[] args) {
        try {
            loadLookupTable(LOOKUP_FILE);
            processFlowLogs(FLOW_LOG_FILE);
            generateSummary(OUTPUT_FILE);
            System.out.println("Processing complete. Output written to " + OUTPUT_FILE);
        }
        catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    /**
     * Load lookup table from text file into a map (supporting multiple tags per key)
     * @throws IOException Throws exception if there is an issue reading the file
     */
    public static void loadLookupTable(String lookupFile)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(lookupFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    continue; // Ignore invalid lines
                }
                String key = (parts[0] + "-" + parts[1]).toLowerCase(); // Normalize key (case-insensitive)
                lookupTable.computeIfAbsent(key, k -> new ArrayList<>()).add(parts[2]);
            }
        }
        System.out.println("Lookup table loaded with " + lookupTable.size() + " entries.");
    }

    /**
     * Process flow logs and tag them based on the lookup table
     * @throws IOException Throws exception if there is an issue reading/writing files
     */
    public static void processFlowLogs(String flowLogFile)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(flowLogFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length != 14) {
                    continue; // Skip malformed lines
                }

                String dstPort = parts[6];
                String protocol = IpProtocols.getProtocolName(Integer.parseInt(parts[7]));
                String key = (dstPort + "-" + protocol).toLowerCase();
                List<String> tags = lookupTable.getOrDefault(key, Collections.singletonList("Untagged"));

                for (String tag : tags) {
                    tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                }

                String portProtocolKey = dstPort + "-" + protocol;
                portProtocolCount.put(portProtocolKey, portProtocolCount.getOrDefault(portProtocolKey, 0) + 1);
            }
        }
    }

    /**
     * Generate summary of tag counts and port/protocol combinations
     * @throws IOException Throws exception if there is an issue writing the summary to file
     */
    static void generateSummary(String outputFile)
        throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            writer.newLine();
            writer.write("Tag Counts:");
            writer.newLine();
            writer.write("Tag,Count");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : tagCount.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            writer.newLine();
            writer.write("Port/Protocol Combination Counts:");
            writer.newLine();
            writer.write("Port,Protocol,Count");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : portProtocolCount.entrySet()) {
                String[] parts = entry.getKey().split("-");
                writer.write(parts[0] + "," + parts[1] + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    public static Map<String, Integer> getTagCount() {
        return tagCount;
    }

    protected static Map<String, List<String>> getLookupTable() {
        return lookupTable;
    }

    public static Map<String, Integer> getPortProtocolCount() {
        return portProtocolCount;
    }
}