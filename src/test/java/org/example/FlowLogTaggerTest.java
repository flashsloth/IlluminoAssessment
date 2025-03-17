package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlowLogTaggerTest {

    private static final String LOOKUP_FILE = "src/test/resources/lookup.txt";
    private static final String FLOW_LOG_FILE = "src/test/resources/flow_logs.csv";
    private static final String OUTPUT_FILE = "src/test/resources/output.txt";

    @BeforeEach
    void setUp()
        throws IOException {
        // Create test lookup file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOOKUP_FILE))) {
            writer.write("80,TCP,tag1");
            writer.newLine();
            writer.write("443,TCP,tag2");
            writer.newLine();
        }

        // Create test flow log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FLOW_LOG_FILE))) {
            writer.write("2021-01-01 00:00:00 192.168.0.1 12345 192.168.0.2 0 80 6 0 0 0 0 0 0");
            writer.newLine();
            writer.write("2021-01-01 00:00:01 192.168.0.1 12345 192.168.0.2 0 443 6 0 0 0 0 0 0");
            writer.newLine();
        }
    }

    @AfterEach
    void cleanUp() {
        FlowLogTagger.getLookupTable().clear();
        FlowLogTagger.getPortProtocolCount().clear();
        FlowLogTagger.getTagCount().clear();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOOKUP_FILE));
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(FLOW_LOG_FILE));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLoadLookupTable()
        throws IOException {
        FlowLogTagger.loadLookupTable(LOOKUP_FILE);
        Map<String, List<String>> lookupTable = FlowLogTagger.getLookupTable();
        assertEquals(2, lookupTable.size());
        assertTrue(lookupTable.containsKey("80-tcp"));
        assertTrue(lookupTable.containsKey("443-tcp"));
    }

    @Test
    void testProcessFlowLogs()
        throws IOException {
        FlowLogTagger.loadLookupTable(LOOKUP_FILE);
        FlowLogTagger.processFlowLogs(FLOW_LOG_FILE);
        Map<String, Integer> tagCount = FlowLogTagger.getTagCount();
        Map<String, Integer> portProtocolCount = FlowLogTagger.getPortProtocolCount();

        assertEquals(2, tagCount.size());
        assertEquals(1, tagCount.get("tag1"));
        assertEquals(1, tagCount.get("tag2"));

        assertEquals(2, portProtocolCount.size());
        assertEquals(1, portProtocolCount.get("80-TCP"));
        assertEquals(1, portProtocolCount.get("443-TCP"));
    }

    @Test
    void testGenerateSummary()
        throws IOException {
        FlowLogTagger.loadLookupTable(LOOKUP_FILE);
        FlowLogTagger.processFlowLogs(FLOW_LOG_FILE);
        FlowLogTagger.generateSummary(OUTPUT_FILE);

        List<String> lines = Files.readAllLines(Paths.get(OUTPUT_FILE));
        assertTrue(lines.contains("Tag Counts:"));
        assertTrue(lines.contains("Tag,Count"));
        assertTrue(lines.contains("tag1,1"));
        assertTrue(lines.contains("tag2,1"));
        assertTrue(lines.contains("Port/Protocol Combination Counts:"));
        assertTrue(lines.contains("Port,Protocol,Count"));
        assertTrue(lines.contains("80,TCP,1"));
        assertTrue(lines.contains("443,TCP,1"));
    }
}