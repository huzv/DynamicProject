package controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import model.Task;
import service.SchedulerService;
import service.TaskService;
import ui.UIBuilder;
import util.ArrayList;
import util.DialogHelper;

public class MainController {

    private final Stage primaryStage;
    private Scene scene;
    private BorderPane rootLayout;

    private final ArrayList<Task> allTasks = new ArrayList<>();

    private final TaskService taskService;
    private final SchedulerService schedulerService;
    private final DialogHelper dialogHelper;

    // UI Components
    private TextField hoursField;
    private TextField searchField;
    private ListView<Task> taskListView;
    private Label statusLabel;
    private TextArea resultsArea;
    private ScrollPane vizContainer;

    private Label dpValueLabel, dpTimeLabel;
    private Label greedyValueLabel, greedyTimeLabel;
    private Label taskCountLabel, totalDurationLabel, avgProductivityLabel;

    private PauseTransition filterDebounce;

    public MainController(Stage stage) {
        this.primaryStage = stage;
        this.taskService = new TaskService(allTasks);
        this.schedulerService = new SchedulerService();
        this.dialogHelper = new DialogHelper(this);
    }

    public Scene createScene() {
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("app-root");

        filterDebounce = new PauseTransition(Duration.millis(300));
        filterDebounce.setOnFinished(e -> performFilter());

        UIBuilder builder = new UIBuilder(this);
        rootLayout.setTop(builder.buildTop());
        rootLayout.setLeft(builder.buildLeft());
        rootLayout.setCenter(builder.buildCenter());
        rootLayout.setBottom(builder.buildBottom());

        scene = new Scene(rootLayout, 1360, 850);
        applyStylesheet();
        setupKeyboardShortcuts();

        return scene;
    }

    private void applyStylesheet() {
        try {
            scene.getStylesheets().add(
                getClass().getResource("/resources/styles.css").toExternalForm()
            );
        } catch (Exception ignored) {}
    }

    private void setupKeyboardShortcuts() {
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case L -> loadTasks();
                    case S -> saveTasks();
                    case R -> runScheduler();
                    default -> {}
                }
            }
        });
    }

    public void playWelcomeAnimation() {
        rootLayout.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), rootLayout);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public void loadTasks() {
        TaskService.LoadResult result = taskService.loadFromFile(primaryStage);
        if (result == null) return;

        if (result.error != null) {
            statusLabel.setText("Load Error: " + result.error);
        } else {
            hoursField.setText(formatDuration(result.capacity));
            refreshList();
            statusLabel.setText("Loaded " + result.count + " tasks");
        }
    }

    public void saveTasks() {
        if (allTasks.isEmpty()) {
            statusLabel.setText("No tasks to save");
            return;
        }

        String input = hoursField.getText().trim();
        float capacity = 8.0f;
        if (!input.isEmpty() && input.matches("\\d+(\\.\\d+)?")) {
            capacity = Float.parseFloat(input);
        }

        TaskService.SaveResult result = taskService.saveToFile(primaryStage, capacity);
        if (result == null) return;

        statusLabel.setText(result.success ? "File Saved" : "Save Error: " + result.error);
    }

    public void runScheduler() {
        String input = hoursField.getText().trim();

        if (input.isEmpty() || !input.matches("\\d+(\\.\\d+)?") || Float.parseFloat(input) <= 0) {
            statusLabel.setText("Invalid Capacity");
            hoursField.requestFocus();
            return;
        }

        float capacity = Float.parseFloat(input);
        if (capacity % 0.5f != 0) {
            statusLabel.setText("Capacity must be in 0.5 increments");
            return;
        }

        if (allTasks.isEmpty()) {
            statusLabel.setText("No tasks to schedule");
            return;
        }

        statusLabel.setText("Running...");

        SchedulerService.SchedulerResult result = schedulerService.execute(allTasks, capacity);

        dpValueLabel.setText(String.valueOf(result.dpValue));
        dpTimeLabel.setText(result.dpTimeLabel);
        greedyValueLabel.setText(String.valueOf(result.greedyValue));
        greedyTimeLabel.setText(result.greedyTimeLabel);

        resultsArea.setText(result.logs);
        vizContainer.setContent(result.vizNode);

        statusLabel.setText("Optimization Complete");
    }

    public void showAddTaskDialog() {
        dialogHelper.showAddTaskDialog(task -> {
            if (task != null) {
                allTasks.add(task);
                refreshList();
                statusLabel.setText("Task Added");
            }
        });
    }

    public void editTask(Task task) {
        dialogHelper.showEditTaskDialog(task, success -> {
            if (success) {
                refreshList();
                updateStats();
                statusLabel.setText("Task Updated");
            }
        });
    }

    public void deleteSelectedTasks() {
        var selected = taskListView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) return;

        int count = taskService.deleteMultiple(selected);
        refreshList();
        statusLabel.setText("Deleted " + count + " tasks");
    }

    public void duplicateSelectedTask() {
        Task selected = getSelectedTask();
        if (selected != null) {
            taskService.duplicate(selected);
            refreshList();
            statusLabel.setText("Task Duplicated");
        }
    }

    public void triggerFilterDebounce() {
        filterDebounce.playFromStart();
    }

    private void performFilter() {
        String query = searchField != null ? searchField.getText() : "";
        taskListView.getItems().clear();

        if (query == null || query.isEmpty()) {
            for (Task t : allTasks) taskListView.getItems().add(t);
        } else {
            String q = query.toLowerCase();
            for (Task t : allTasks) {
                if (t.getName().toLowerCase().contains(q)) {
                    taskListView.getItems().add(t);
                }
            }
        }
        updateStats();
    }

    public void refreshList() {
        performFilter();
    }

    public void updateStats() {
        int count = allTasks.size();
        float duration = 0;
        int productivity = 0;

        for (Task t : allTasks) {
            duration += t.getDuration();
            productivity += t.getProductivity();
        }

        taskCountLabel.setText(String.valueOf(count));
        totalDurationLabel.setText(formatDuration(duration) + "h");
        avgProductivityLabel.setText(count > 0 ? String.format("%.1f", (float) productivity / count) : "0");
    }

    public Task getSelectedTask() {
        return taskListView.getSelectionModel().getSelectedItem();
    }

    public ArrayList<Task> getAllTasks() { return allTasks; }
    public Stage getPrimaryStage() { return primaryStage; }

    public String formatDuration(float d) {
        return d == (long) d ? String.format("%d", (long) d) : String.valueOf(d);
    }

    // Setters for UI components
    public void setHoursField(TextField f) { this.hoursField = f; }
    public void setSearchField(TextField f) { this.searchField = f; }
    public void setTaskListView(ListView<Task> lv) { this.taskListView = lv; }
    public void setStatusLabel(Label l) { this.statusLabel = l; }
    public void setResultsArea(TextArea a) { this.resultsArea = a; }
    public void setVizContainer(ScrollPane c) { this.vizContainer = c; }
    public void setDpValueLabel(Label l) { this.dpValueLabel = l; }
    public void setDpTimeLabel(Label l) { this.dpTimeLabel = l; }
    public void setGreedyValueLabel(Label l) { this.greedyValueLabel = l; }
    public void setGreedyTimeLabel(Label l) { this.greedyTimeLabel = l; }
    public void setTaskCountLabel(Label l) { this.taskCountLabel = l; }
    public void setTotalDurationLabel(Label l) { this.totalDurationLabel = l; }
    public void setAvgProductivityLabel(Label l) { this.avgProductivityLabel = l; }
}