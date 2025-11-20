import javafx.application.Application;
import io.TaskParser;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Task;
import algorithms.*;
import util.TableRenderer;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.geometry.Pos;

import java.io.IOException;
import java.nio.file.Path;
import util.ArrayList;

public class App extends Application {
    private ArrayList<Task> allTasks = new ArrayList<>();
    private TextField searchField;
    private ListView<Task> taskListView;
    
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));
        Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Monocraft.ttf"), 16);
        
        // Top toolbar with buttons
        HBox top = new HBox(8);
        Button loadBtn = new Button("Load");
        Button saveBtn = new Button("Save");
        TextField hoursField = new TextField();
        hoursField.setPromptText("Total hours (e.g., 8)");
        Button runBtn = new Button("Run");
        Button addBtn = new Button("+");
        Button deleteBtn = new Button("Delete");
        Label hoursLabel = new Label("Total hours:");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        top.getChildren().addAll(loadBtn, saveBtn, hoursLabel, hoursField, runBtn, addBtn, deleteBtn);

        taskListView = new ListView<>();
        taskListView.setPrefWidth(350);
        taskListView.setEditable(true);
        taskListView.setCellFactory(lv -> new TaskCell());
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Left panel with search bar and task list
        VBox leftPanel = new VBox(8);
        Label taskListLabel = new Label("Tasks");
        taskListLabel.getStyleClass().add("heading");
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("🔍 Search tasks...");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.getStyleClass().add("input");
        
        leftPanel.getChildren().addAll(taskListLabel, searchField, taskListView);
        VBox.setVgrow(taskListView, Priority.ALWAYS);
        root.setLeft(leftPanel);

        VBox centerBox = new VBox(8);
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefRowCount(10);
        Label resultsLabel = new Label("Results");
        centerBox.getChildren().addAll(resultsLabel, resultsArea);
        root.setCenter(centerBox);

        ScrollPane rightScroll = new ScrollPane();
        rightScroll.setFitToWidth(true);
        rightScroll.setPrefViewportHeight(600);
        root.setRight(rightScroll);

        Label recurrence = new Label("dp[i][t] = max(dp[i-1][t], value[i] + dp[i-1][t-time[i]]) if time[i] <= t\n" +
                "dp[i][t] = dp[i-1][t] otherwise");
        recurrence.setWrapText(true);
        recurrence.setMaxWidth(Double.MAX_VALUE);
        HBox formulaBox = new HBox(recurrence);
        formulaBox.getStyleClass().add("formula-panel");
        formulaBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(recurrence, Priority.ALWAYS);
        root.setBottom(formulaBox);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
        stage.setTitle("Task Scheduler");
        stage.setScene(scene);
        stage.show();

        TaskParser parser = new TaskParser();

        root.getStyleClass().add("app-root");
        top.getStyleClass().add("toolbar");
        loadBtn.getStyleClass().addAll("btn", "btn-secondary");
        saveBtn.getStyleClass().addAll("btn", "btn-secondary");
        runBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.getStyleClass().addAll("btn", "btn-success");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        hoursField.getStyleClass().add("input");
        taskListView.getStyleClass().add("list");
        resultsArea.getStyleClass().add("code-area");
        rightScroll.getStyleClass().add("scroll");
        hoursLabel.getStyleClass().add("secondary");
        resultsLabel.getStyleClass().add("heading");
        taskListLabel.getStyleClass().add("heading");
        leftPanel.getStyleClass().add("panel");

        ImageView iconLoad = loadIcon("/resources/icons/downloadIcon.png");
        loadBtn.setGraphic(iconLoad);
        loadBtn.setContentDisplay(ContentDisplay.LEFT);

        ImageView iconRun = loadIcon("/resources/icons/runIcon.png");
        runBtn.setGraphic(iconRun);
        runBtn.setContentDisplay(ContentDisplay.LEFT);

        Region iconResults = new Region();
        iconResults.getStyleClass().add("icon-space");
        resultsLabel.setGraphic(iconResults);
        resultsLabel.setContentDisplay(ContentDisplay.RIGHT);

        hoursLabel.setLabelFor(hoursField);
        loadBtn.setAccessibleText("Load tasks from file");
        saveBtn.setAccessibleText("Save tasks to file");
        hoursField.setAccessibleText("Total hours input");
        searchField.setAccessibleText("Search tasks");
        runBtn.setAccessibleText("Run scheduling algorithms");
        taskListView.setAccessibleText("Loaded tasks list");
        resultsArea.setAccessibleText("Results output");
        rightScroll.setAccessibleText("Dynamic programming table view");
        deleteBtn.setAccessibleText("Delete selected tasks");

        BorderPane.setMargin(leftPanel, new Insets(16));
        BorderPane.setMargin(centerBox, new Insets(16));
        BorderPane.setMargin(rightScroll, new Insets(16));
        BorderPane.setMargin(recurrence, new Insets(16));

        applyHoverAnimation(loadBtn);
        applyHoverAnimation(saveBtn);
        applyHoverAnimation(runBtn);
        applyHoverAnimation(deleteBtn);
        applyPressAnimation(loadBtn);
        applyPressAnimation(saveBtn);
        applyPressAnimation(runBtn);
        applyPressAnimation(addBtn);
        applyPressAnimation(deleteBtn);

        MenuBar menuBar = createMenuBar(stage, hoursField, runBtn, loadBtn, saveBtn, addBtn, deleteBtn, scene, parser);
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, top);
        root.setTop(topContainer);
        menuBar.getStyleClass().add("menu-bar");
        topContainer.getStyleClass().add("toolbar");

        // Context menu for task list
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem duplicateItem = new MenuItem("Duplicate");
        MenuItem editItem = new MenuItem("Edit");
        contextMenu.getItems().addAll(editItem, duplicateItem, new SeparatorMenuItem(), deleteItem);
        
        taskListView.setContextMenu(contextMenu);
        
        deleteItem.setOnAction(e -> deleteSelectedTasks());
        duplicateItem.setOnAction(e -> duplicateSelectedTask());
        editItem.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editTask(selected);
            }
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterTasks(newVal);
        });

        // Load button action
        loadBtn.setOnAction(e -> loadTasks(stage, parser, loadBtn));

        // Save button action
        saveBtn.setOnAction(e -> saveTasks(stage, parser, saveBtn));

        // Delete button action
        deleteBtn.setOnAction(e -> deleteSelectedTasks());

        // Add keyboard shortcut for delete
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.DELETE && 
                taskListView.isFocused() && 
                !taskListView.getSelectionModel().getSelectedItems().isEmpty()) {
                deleteSelectedTasks();
                e.consume();
            }
        });

        addBtn.setOnAction(e -> {
            Dialog<Task> dlg = new Dialog<>();
            dlg.setTitle("Add Task");
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            GridPane gp = new GridPane();
            gp.setHgap(8);
            gp.setVgap(8);
            gp.setPadding(new Insets(8));
            TextField name = new TextField();
            TextField dur = new TextField();
            TextField prod = new TextField();
            name.getStyleClass().add("input");
            dur.getStyleClass().add("input");
            prod.getStyleClass().add("input");
            gp.addRow(0, new Label("Name"), name);
            gp.addRow(1, new Label("Duration"), dur);
            gp.addRow(2, new Label("Productivity"), prod);
            dlg.getDialogPane().setContent(gp);
            dlg.setResultConverter(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        int d = Integer.parseInt(dur.getText().trim());
                        int p = Integer.parseInt(prod.getText().trim());
                        return new Task(name.getText().trim(), d, p);
                    } catch (Exception ex) {
                        showError("Invalid values for duration/productivity");
                        return null;
                    }
                }
                return null;
            });
            var res = dlg.showAndWait();
            res.ifPresent(t -> {
                allTasks.add(t);
                refreshTaskList();
                bounce(addBtn);
            });
        });

        runBtn.setOnAction(e -> {
            String hoursText = hoursField.getText().trim();
            int totalHours;
            try {
                totalHours = Integer.parseInt(hoursText);
                if (totalHours < 0) throw new NumberFormatException("negative");
            } catch (NumberFormatException ex) {
                showError("Total hours must be a non-negative integer");
                return;
            }
            if (allTasks.isEmpty()) {
                showError("Please load tasks first");
                return;
            }

            Dynamic dynamic = new Dynamic();
            var dpResult = dynamic.solve(allTasks, totalHours);

            Greedy greedySolver = new Greedy();
            var greedyResult = greedySolver.solve(allTasks, totalHours);

            StringBuilder sb = new StringBuilder();
            sb.append("Dynamic Programming:\n");
            int dpTime = 0;
            for (Task t : dpResult.chosen) {
                sb.append(" - ").append(t.getName()).append(" (").append(t.getDuration()).append("h)\n");
                dpTime += t.getDuration();
            }
            sb.append("Time: ").append(dpTime).append(", Productivity: ").append(dpResult.totalValue).append("\n\n");

            sb.append("Greedy:\n");
            for (Task t : greedyResult.chosen) {
                sb.append(" - ").append(t.getName()).append(" (").append(t.getDuration()).append("h)\n");
            }
            sb.append("Time: ").append(greedyResult.totalTime).append(", Productivity: ").append(greedyResult.totalValue).append("\n");
            resultsArea.setText(sb.toString());

            var tableView = TableRenderer.renderDPTable(dpResult.dp, dpResult.take);
            rightScroll.setContent(tableView);
            bounce(runBtn);
        });
    }

private void deleteSelectedTasks() {
    var selected = new ArrayList<Task>();
    // Manually add all selected items to our custom ArrayList
    for (Task task : taskListView.getSelectionModel().getSelectedItems()) {
        selected.add(task);
    }
    
    if (selected.isEmpty()) {
        showError("Please select tasks to delete");
        return;
    }
    
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Delete Tasks");
    confirmAlert.setHeaderText("Delete " + selected.size() + " task(s)?");
    confirmAlert.setContentText("This action cannot be undone.");
    
    var result = confirmAlert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        for (int i = 0; i < selected.size(); i++) {
            allTasks.remove(selected.get(i));
        }
        refreshTaskList();
        showInfo("Deleted " + selected.size() + " task(s)");
    }
}

    private void duplicateSelectedTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task duplicate = new Task(
                selected.getName() + " (Copy)", 
                selected.getDuration(), 
                selected.getProductivity()
            );
            allTasks.add(duplicate);
            refreshTaskList();
        }
    }

    private void editTask(Task task) {
        Dialog<Task> dlg = new Dialog<>();
        dlg.setTitle("Edit Task");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);
        gp.setPadding(new Insets(8));
        TextField name = new TextField(task.getName());
        TextField dur = new TextField(String.valueOf(task.getDuration()));
        TextField prod = new TextField(String.valueOf(task.getProductivity()));
        name.getStyleClass().add("input");
        dur.getStyleClass().add("input");
        prod.getStyleClass().add("input");
        gp.addRow(0, new Label("Name"), name);
        gp.addRow(1, new Label("Duration"), dur);
        gp.addRow(2, new Label("Productivity"), prod);
        dlg.getDialogPane().setContent(gp);
        dlg.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    int d = Integer.parseInt(dur.getText().trim());
                    int p = Integer.parseInt(prod.getText().trim());
                    task.setName(name.getText().trim());
                    task.setDuration(d);
                    task.setProductivity(p);
                    return task;
                } catch (Exception ex) {
                    showError("Invalid values for duration/productivity");
                    return null;
                }
            }
            return null;
        });
        var res = dlg.showAndWait();
        res.ifPresent(t -> refreshTaskList());
    }

    private void loadTasks(Stage stage, TaskParser parser, Button loadBtn) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open tasks.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        var file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                allTasks.clear();
                allTasks.addAll(parser.parse(Path.of(file.toURI())));
                refreshTaskList();
                bounce(loadBtn);
                showInfo("Successfully loaded " + allTasks.size() + " tasks");
            } catch (IOException ex) {
                showError("Failed to parse tasks: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showError("Invalid task: " + ex.getMessage());
            }
        }
    }

    private void saveTasks(Stage stage, TaskParser parser, Button saveBtn) {
        if (allTasks.isEmpty()) {
            showError("No tasks to save");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save tasks");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        chooser.setInitialFileName("tasks.txt");
        var file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                parser.save(allTasks, Path.of(file.toURI()));
                bounce(saveBtn);
                showInfo("Successfully saved " + allTasks.size() + " tasks");
            } catch (IOException ex) {
                showError("Failed to save tasks: " + ex.getMessage());
            }
        }
    }

    private void filterTasks(String searchText) {
        taskListView.getItems().clear();
        
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all tasks
            for (Task task : allTasks) {
                taskListView.getItems().add(task);
            }
        } else {
            // Filter tasks by search text
            String lowerSearch = searchText.toLowerCase().trim();
            for (Task task : allTasks) {
                if (task.getName().toLowerCase().contains(lowerSearch) ||
                    String.valueOf(task.getDuration()).contains(lowerSearch) ||
                    String.valueOf(task.getProductivity()).contains(lowerSearch)) {
                    taskListView.getItems().add(task);
                }
            }
        }
    }

    private void refreshTaskList() {
        String currentSearch = searchField.getText();
        filterTasks(currentSearch);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Success");
        alert.showAndWait();
    }

    private void applyHoverAnimation(Button b) {
        ColorAdjust effect = new ColorAdjust();
        b.setEffect(effect);
        Timeline enter = new Timeline(new KeyFrame(Duration.millis(100), new KeyValue(effect.brightnessProperty(), 0.08)));
        Timeline exit = new Timeline(new KeyFrame(Duration.millis(100), new KeyValue(effect.brightnessProperty(), 0.0)));
        b.setOnMouseEntered(ev -> enter.playFromStart());
        b.setOnMouseExited(ev -> exit.playFromStart());
    }

    private void bounce(Node n) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), n);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.06);
        st.setToY(1.06);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    private void applyPressAnimation(Button b) {
        b.setOnMousePressed(ev -> {
            b.setScaleX(0.98);
            b.setScaleY(0.98);
        });
        b.setOnMouseReleased(ev -> {
            b.setScaleX(1.0);
            b.setScaleY(1.0);
        });
    }

    private ImageView loadIcon(String path) {
        var is = getClass().getResourceAsStream(path);
        if (is == null) return null;
        javafx.scene.image.Image img = new javafx.scene.image.Image(is, 32, 32, true, false);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(32);
        iv.setFitHeight(32);
        iv.setSmooth(false);
        iv.setPreserveRatio(true);
        iv.getStyleClass().add("icon-space");
        return iv;
    }

    private MenuBar createMenuBar(Stage stage, TextField hoursField, Button runBtn, 
                                   Button loadBtn, Button saveBtn, Button addBtn, Button deleteBtn,
                                   Scene scene, TaskParser parser) {
        Menu file = new Menu("File");
        MenuItem miLoad = new MenuItem("Load Tasks");
        miLoad.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+L"));
        MenuItem miSave = new MenuItem("Save Tasks");
        miSave.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        MenuItem miExit = new MenuItem("Exit");
        miExit.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Alt+F4"));
        file.getItems().addAll(miLoad, miSave, new SeparatorMenuItem(), miExit);

        Menu edit = new Menu("Edit");
        MenuItem miFind = new MenuItem("Find");
        miFind.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+F"));
        MenuItem miClearSearch = new MenuItem("Clear Search");
        miClearSearch.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Esc"));
        MenuItem miDelete = new MenuItem("Delete Selected");
        miDelete.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Delete"));
        edit.getItems().addAll(miFind, miClearSearch, new SeparatorMenuItem(), miDelete);

        Menu view = new Menu("View");
        CheckMenuItem miFull = new CheckMenuItem("Fullscreen");
        miFull.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F11"));
        view.getItems().add(miFull);

        Menu actions = new Menu("Actions");
        MenuItem miRun = new MenuItem("Run");
        miRun.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+R"));
        actions.getItems().add(miRun);

        MenuBar mb = new MenuBar(file, edit, view, actions);

        Tooltip.install(runBtn, new Tooltip("Run (Ctrl+R)"));
        Tooltip.install(hoursField, new Tooltip("Total hours (Tab to focus)"));
        Tooltip.install(loadBtn, new Tooltip("Load Tasks (Ctrl+L)"));
        Tooltip.install(saveBtn, new Tooltip("Save Tasks (Ctrl+S)"));
        Tooltip.install(addBtn, new Tooltip("Add Task (+)"));
        Tooltip.install(deleteBtn, new Tooltip("Delete Selected (Delete)"));
        Tooltip.install(searchField, new Tooltip("Search tasks (Ctrl+F)"));

        miLoad.setOnAction(e -> loadBtn.fire());
        miSave.setOnAction(e -> saveBtn.fire());
        miRun.setOnAction(e -> runBtn.fire());
        miDelete.setOnAction(e -> deleteBtn.fire());
        miExit.setOnAction(e -> stage.close());
        miFull.setOnAction(e -> stage.setFullScreen(miFull.isSelected()));
        miFind.setOnAction(e -> {
            searchField.requestFocus();
            searchField.selectAll();
        });
        miClearSearch.setOnAction(e -> {
            searchField.clear();
            taskListView.requestFocus();
        });

        // Keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.F) {
                searchField.requestFocus();
                searchField.selectAll();
                e.consume();
            } else if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                searchField.clear();
                e.consume();
            }
        });

        return mb;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class TaskCell extends ListCell<Task> {
        private final Label nameLabel = new Label();
        private final Label durationLabel = new Label();
        private final Label productivityLabel = new Label();
        private final TextField editor = new TextField();
        private final HBox displayBox = new HBox(12);
        private final Region spacer = new Region();

        TaskCell() {
            nameLabel.setMinWidth(180);
            nameLabel.setMaxWidth(180);
            durationLabel.setMinWidth(80);
            productivityLabel.setMinWidth(80);
            
            durationLabel.getStyleClass().add("secondary");
            productivityLabel.getStyleClass().add("secondary");
            
            HBox.setHgrow(spacer, Priority.ALWAYS);
            displayBox.getChildren().addAll(nameLabel, spacer, durationLabel, productivityLabel);
            displayBox.setAlignment(Pos.CENTER_LEFT);
            displayBox.setPadding(new Insets(4, 8, 4, 8));
            
            editor.getStyleClass().add("input");
            editor.setVisible(false);
            
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            nameLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && getItem() != null) {
                    editor.setText(getItem().getName());
                    editor.setVisible(true);
                    displayBox.setVisible(false);
                    setGraphic(editor);
                    editor.requestFocus();
                }
            });
            
            editor.setOnAction(e -> {
                if (getItem() != null) {
                    getItem().setName(editor.getText());
                    updateDisplay();
                    editor.setVisible(false);
                    displayBox.setVisible(true);
                    setGraphic(displayBox);
                }
            });
            
            editor.focusedProperty().addListener((obs, old, focused) -> {
                if (!focused && getItem() != null && editor.isVisible()) {
                    getItem().setName(editor.getText());
                    updateDisplay();
                    editor.setVisible(false);
                    displayBox.setVisible(true);
                    setGraphic(displayBox);
                }
            });

            setOnDragDetected(e -> {
                if (getItem() == null) return;
                var db = startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                var content = new javafx.scene.input.ClipboardContent();
                content.putString(getItem().getName());
                db.setContent(content);
                e.consume();
            });
            
            setOnDragOver(e -> {
                if (e.getGestureSource() != this && e.getDragboard().hasString()) {
                    e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                }
                e.consume();
            });
            
            setOnDragDropped(e -> {
                var lv = getListView();
                int draggedIdx = lv.getItems().indexOf(lv.getItems().stream().filter(t -> t.getName().equals(e.getDragboard().getString())).findFirst().orElse(null));
                int thisIdx = getIndex();
                if (draggedIdx >= 0 && thisIdx >= 0 && draggedIdx != thisIdx) {
                    var items = lv.getItems();
                    Task t = items.remove(draggedIdx);
                    items.add(thisIdx, t);
                    lv.getItems().setAll(items);
                }
                e.setDropCompleted(true);
                e.consume();
            });
        }

        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                updateDisplay();
                setGraphic(displayBox);
            }
        }

        private void updateDisplay() {
            Task item = getItem();
            if (item != null) {
                nameLabel.setText(item.getName());
                durationLabel.setText("Duration: " + item.getDuration() + "h");
                productivityLabel.setText("Productivity: " + item.getProductivity());
            }
        }
    }
}