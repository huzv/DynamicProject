package io;

import model.Task;
import util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TaskParser {
    
    // Simple data class instead of record
    public static class ParseResult {
        public ArrayList<Task> tasks;
        public float capacity;
        
        public ParseResult(ArrayList<Task> tasks, float capacity) {
            this.tasks = tasks;
            this.capacity = capacity;
        }
    }
    
    public ParseResult parse(Path path) throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        float capacity;
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            int expectedTasks;
            
            // Line 1: Number of tasks
            line = br.readLine();
            lineNo++;
            if (line == null || line.trim().isEmpty()) {
                throw new IOException("Missing task count on line 1");
            }
            try {
                expectedTasks = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Invalid task count on line 1: " + line);
            }
            
            // Line 2: Capacity (hours)
            line = br.readLine();
            lineNo++;
            if (line == null || line.trim().isEmpty()) {
                throw new IOException("Missing capacity on line 2");
            }
            try {
                capacity = Float.parseFloat(line.trim());
                if ((capacity % 0.5f) > 0.001f && (capacity % 0.5f) < 0.499f) {
                    throw new IOException("Capacity must be in 0.5 increments");
                }
            } catch (NumberFormatException e) {
                throw new IOException("Invalid capacity on line 2: " + line);
            }
            
            // Lines 3+: Tasks
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                
                short firstComma = -1;
                short secondComma = -1;
                boolean inQuote = false;
                
                for (short i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == '"') inQuote = !inQuote;
                    else if (c == ',' && !inQuote) {
                        if (firstComma == -1) firstComma = i;
                        else {
                            secondComma = i;
                            break;
                        }
                    }
                }

                if (firstComma == -1 || secondComma == -1)
                    throw new IOException("Invalid format at line " + lineNo + ": expected 3 fields");
                
                String namePart = line.substring(0, firstComma).trim();
                String timeStr = line.substring(firstComma + 1, secondComma).trim();
                String valStr = line.substring(secondComma + 1).trim();
                
                String name = unquote(namePart);
                
                float time;
                int value;
                try {
                    time = Float.parseFloat(timeStr);
                    if ((time % 0.5f) > 0.001f && (time % 0.5f) < 0.499f) {
                        throw new IOException("Invalid duration at line " + lineNo + ": must be in 0.5 increments");
                    }
                    value = Integer.parseInt(valStr);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number at line " + lineNo + ": " + e.getMessage());
                }
                tasks.add(new Task(name, time, value));
            }
            
            if (tasks.size() != expectedTasks) {
                throw new IOException("Expected " + expectedTasks + " tasks but found " + tasks.size());
            }
        }
        return new ParseResult(tasks, capacity);
    }

    public void save(ArrayList<Task> tasks, float capacity, Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Line 1: Task count
            bw.write(String.valueOf(tasks.size()));
            bw.newLine();
            
            // Line 2: Capacity
            bw.write(formatFloat(capacity));
            bw.newLine();
            
            // Lines 3+: Tasks
            for (Task task : tasks) {
                String name = task.getName();
                boolean quote = name.contains(",") || name.contains("\"");
                if (quote) {
                    bw.write('"');
                    bw.write(name.replace("\"", "\"\""));
                    bw.write('"');
                } else {
                    bw.write(name);
                }
                bw.write(',');
                bw.write(formatFloat(task.getDuration()));
                bw.write(',');
                bw.write(String.valueOf(task.getProductivity()));
                bw.newLine();
            }
        }
    }
    
    private String formatFloat(float f) {
        return f == (int) f ? String.valueOf((int) f) : String.valueOf(f);
    }

    private static String unquote(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}