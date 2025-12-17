package service;

import io.TaskParser;
import io.TaskParser.ParseResult;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Task;
import util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class TaskService {

    private final ArrayList<Task> allTasks;
    private final TaskParser parser;
    private float lastLoadedCapacity = 8.0f;

    public TaskService(ArrayList<Task> allTasks) {
        this.allTasks = allTasks;
        this.parser = new TaskParser();
    }

    // Simple data class for load result
    public static class LoadResult {
        public int count;
        public float capacity;
        public String error;
        
        public LoadResult(int count, float capacity, String error) {
            this.count = count;
            this.capacity = capacity;
            this.error = error;
        }
    }
    
    // Simple data class for save result
    public static class SaveResult {
        public boolean success;
        public String error;
        
        public SaveResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
    }

    public LoadResult loadFromFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fc.showOpenDialog(stage);

        if (file == null) return null;

        try {
            allTasks.clear();
            ParseResult result = parser.parse(Path.of(file.toURI()));
            allTasks.addAll(result.tasks);
            lastLoadedCapacity = result.capacity;
            return new LoadResult(allTasks.size(), result.capacity, null);
        } catch (Exception e) {
            return new LoadResult(0, 0, e.getMessage());
        }
    }

    public SaveResult saveToFile(Stage stage, float capacity) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("tasks.txt");
        File file = fc.showSaveDialog(stage);

        if (file == null) return null;

        try {
            parser.save(allTasks, capacity, Path.of(file.toURI()));
            return new SaveResult(true, null);
        } catch (IOException e) {
            return new SaveResult(false, e.getMessage());
        }
    }

    public int deleteMultiple(List<Task> tasks) {
        List<Task> toRemove = new java.util.ArrayList<>(tasks);
        for (Task t : toRemove) {
            allTasks.remove(t);
        }
        return toRemove.size();
    }

    public void duplicate(Task task) {
        Task copy = new Task(
            task.getName() + " (Copy)",
            task.getDuration(),
            task.getProductivity()
        );
        allTasks.add(copy);
    }

    public float getLastLoadedCapacity() {
        return lastLoadedCapacity;
    }
}