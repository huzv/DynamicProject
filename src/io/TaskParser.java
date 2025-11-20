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
    public ArrayList<Task> parse(Path path) throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = splitCSVLine(line);
                if (parts.length != 3)
                    throw new IOException("Invalid format at line " + lineNo + ": expected 3 fields");
                String name = unquote(parts[0].trim());
                String timeStr = parts[1].trim();
                String valStr = parts[2].trim();
                int time;
                int value;
                try {
                    time = Integer.parseInt(timeStr);
                    value = Integer.parseInt(valStr);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid integer at line " + lineNo + ": " + e.getMessage());
                }
                tasks.add(new Task(name, time, value));
            }
        }
        return tasks;
    }
    
    public void save(ArrayList<Task> tasks, Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Task task : tasks) {
                String name = task.getName();
                // Quote name if it contains comma or quotes
                if (name.contains(",") || name.contains("\"")) {
                    name = "\"" + name.replace("\"", "\"\"") + "\"";
                }
                bw.write(name + "," + task.getDuration() + "," + task.getProductivity());
                bw.newLine();
            }
        }
    }

    private static String[] splitCSVLine(String line) {
        ArrayList<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        parts.add(sb.toString());
        return parts.toArray(new String[0]);
    }

    private static String unquote(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}