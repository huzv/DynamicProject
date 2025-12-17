package ui;

import controller.MainController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.Task;
import util.ArrayList;

public class UIBuilder {

    private final MainController controller;

    public UIBuilder(MainController controller) {
        this.controller = controller;
    }

    // ==================== TOP SECTION ====================

    public VBox buildTop() {
        MenuBar menuBar = createMenuBar();
        HBox toolbar = createToolbar();
        return new VBox(menuBar, toolbar);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem loadItem = new MenuItem("Load Data (Ctrl+L)");
        MenuItem saveItem = new MenuItem("Save Data (Ctrl+S)");
        MenuItem exitItem = new MenuItem("Exit");

        loadItem.setOnAction(e -> controller.loadTasks());
        saveItem.setOnAction(e -> controller.saveTasks());
        exitItem.setOnAction(e -> controller.getPrimaryStage().close());

        fileMenu.getItems().addAll(loadItem, saveItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 15, 10, 15));

        Button loadBtn = createButton("Load", "btn-secondary");
        Button saveBtn = createButton("Save", "btn-secondary");
        Button addBtn = createButton("Add Task", "btn-success");
        Button deleteBtn = createButton("Delete", "btn-danger");

        loadBtn.setOnAction(e -> controller.loadTasks());
        saveBtn.setOnAction(e -> controller.saveTasks());
        addBtn.setOnAction(e -> controller.showAddTaskDialog());
        deleteBtn.setOnAction(e -> controller.deleteSelectedTasks());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox capacityBox = createCapacityBox();

        toolbar.getChildren().addAll(
            loadBtn, saveBtn, new Separator(Orientation.VERTICAL),
            addBtn, deleteBtn, spacer, capacityBox
        );

        return toolbar;
    }

    private HBox createCapacityBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Capacity (Hrs):");
        label.getStyleClass().add("input-label");

        TextField hoursField = new TextField();
        hoursField.setPromptText("e.g. 8 or 4.5");
        hoursField.setPrefWidth(120);
        hoursField.getStyleClass().add("input");
        controller.setHoursField(hoursField);

        Button runBtn = createButton("RUN", "btn-run");
        runBtn.setMinWidth(80);
        runBtn.setOnAction(e -> controller.runScheduler());

        box.getChildren().addAll(label, hoursField, runBtn);
        return box;
    }

    // ==================== LEFT PANEL ====================

    public VBox buildLeft() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(340);
        panel.setPadding(new Insets(15));

        HBox header = createLeftHeader();
        TextField searchField = createSearchField();
        ListView<Task> listView = createTaskListView();
        VBox statsBox = createStatsBox();

        panel.getChildren().addAll(header, searchField, listView, statsBox);
        return panel;
    }

    private HBox createLeftHeader() {
        HBox header = new HBox();
        Label title = new Label("Input Tasks");
        title.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label("0");
        countLabel.getStyleClass().add("badge");
        controller.setTaskCountLabel(countLabel);

        header.getChildren().addAll(title, spacer, countLabel);
        return header;
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Filter tasks...");
        field.getStyleClass().add("search-input");
        field.textProperty().addListener((obs, o, n) -> controller.triggerFilterDebounce());
        controller.setSearchField(field);
        return field;
    }

    private ListView<Task> createTaskListView() {
        ListView<Task> listView = new ListView<>();
        listView.setCellFactory(lv -> new TaskCell());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getStyleClass().add("task-list");
        listView.setPlaceholder(new Label("No tasks loaded.\nLoad or Add a task!"));
        VBox.setVgrow(listView, Priority.ALWAYS);

        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem duplicate = new MenuItem("Duplicate");
        MenuItem delete = new MenuItem("Delete");

        edit.setOnAction(e -> {
            Task t = controller.getSelectedTask();
            if (t != null) controller.editTask(t);
        });
        duplicate.setOnAction(e -> controller.duplicateSelectedTask());
        delete.setOnAction(e -> controller.deleteSelectedTasks());

        menu.getItems().addAll(edit, duplicate, new SeparatorMenuItem(), delete);
        listView.setContextMenu(menu);

        controller.setTaskListView(listView);
        return listView;
    }

    private VBox createStatsBox() {
        VBox box = new VBox(10);
        box.getStyleClass().add("stats-panel");
        box.setPadding(new Insets(10));

        Label durationLabel = new Label("0h");
        durationLabel.getStyleClass().add("stat-value");
        controller.setTotalDurationLabel(durationLabel);

        Label avgLabel = new Label("0");
        avgLabel.getStyleClass().add("stat-value");
        controller.setAvgProductivityLabel(avgLabel);

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            createStatItem("Duration", durationLabel),
            createStatItem("Avg Value", avgLabel)
        );

        box.getChildren().add(row);
        return box;
    }

    private VBox createStatItem(String title, Label valueLabel) {
        VBox item = new VBox(2);
        item.setAlignment(Pos.CENTER);
        item.getChildren().addAll(new Label(title), valueLabel);
        return item;
    }

    // ==================== CENTER PANEL ====================

    public SplitPane buildCenter() {
        SplitPane split = new SplitPane();

        VBox resultsSection = createResultsSection();
        VBox vizSection = createVisualizationSection();

        split.getItems().addAll(resultsSection, vizSection);
        split.setDividerPositions(0.45);

        return split;
    }

    private VBox createResultsSection() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        HBox cards = createAlgorithmCards();

        Label logsTitle = new Label("Detailed Logs:");
        logsTitle.getStyleClass().add("heading");

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.getStyleClass().add("code-area");
        resultsArea.setPromptText("Run to see details...");
        VBox.setVgrow(resultsArea, Priority.ALWAYS);
        controller.setResultsArea(resultsArea);

        container.getChildren().addAll(cards, logsTitle, resultsArea);
        return container;
    }

    private HBox createAlgorithmCards() {
        HBox cards = new HBox(15);

        VBox dpCard = createAlgoCard("Dynamic Programming", "Optimal Solution", "dp-card");
        Label dpVal = (Label) dpCard.lookup(".card-value");
        Label dpTime = (Label) dpCard.lookup(".card-time");
        controller.setDpValueLabel(dpVal);
        controller.setDpTimeLabel(dpTime);

        VBox greedyCard = createAlgoCard("Greedy Solution", "Best Ratio First", "greedy-card");
        Label greedyVal = (Label) greedyCard.lookup(".card-value");
        Label greedyTime = (Label) greedyCard.lookup(".card-time");
        controller.setGreedyValueLabel(greedyVal);
        controller.setGreedyTimeLabel(greedyTime);

        cards.getChildren().addAll(dpCard, greedyCard);
        return cards;
    }

    private VBox createAlgoCard(String title, String subtitle, String cssClass) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("algorithm-card", cssClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descLabel = new Label(subtitle);
        descLabel.getStyleClass().add("card-desc");

        Label valueLabel = new Label("0");
        valueLabel.getStyleClass().add("card-value");

        Label timeLabel = new Label("Time: 0ms");
        timeLabel.getStyleClass().add("card-time");

        card.getChildren().addAll(titleLabel, descLabel, valueLabel, timeLabel);
        return card;
    }

    private VBox createVisualizationSection() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label title = new Label("DP Table Visualization");
        title.getStyleClass().add("panel-title");

        ScrollPane vizContainer = new ScrollPane();
        vizContainer.setFitToWidth(true);
        vizContainer.getStyleClass().add("content-scroll");
        VBox.setVgrow(vizContainer, Priority.ALWAYS);

        Label placeholder = new Label("Run to generate DP Table");
        placeholder.getStyleClass().add("placeholder-text");
        placeholder.setAlignment(Pos.CENTER);

        StackPane placeholderPane = new StackPane(placeholder);
        placeholderPane.setPrefHeight(200);
        vizContainer.setContent(placeholderPane);

        controller.setVizContainer(vizContainer);

        panel.getChildren().addAll(title, vizContainer);
        return panel;
    }

    // ==================== BOTTOM PANEL ====================

    public HBox buildBottom() {
        HBox bottom = new HBox(15);
        bottom.getStyleClass().add("status-bar");
        bottom.setPadding(new Insets(5, 15, 5, 15));
        bottom.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(4);
        statusDot.getStyleClass().add("status-dot");

        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-text");
        controller.setStatusLabel(statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label info = new Label("DP: O(N×T) | dp[i][t] = max(dp[i-1][t], dp[i-1][t-w] + v)");
        info.getStyleClass().add("secondary");

        bottom.getChildren().addAll(statusDot, statusLabel, spacer, info);
        return bottom;
    }

    // ==================== UTILITY ====================

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn", styleClass);
        return button;
    }

    // ==================== TASK CELL (Inner Class) ====================

    private class TaskCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            HBox root = new HBox(10);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(8));
            root.getStyleClass().add("task-cell");

            VBox textBox = new VBox(2);
            Label nameLabel = new Label(item.getName());
            nameLabel.getStyleClass().add("task-name");

            Label detailLabel = new Label(controller.formatDuration(item.getDuration()) + "h");
            detailLabel.getStyleClass().add("secondary");
            textBox.getChildren().addAll(nameLabel, detailLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label("Val: " + item.getProductivity());
            badge.getStyleClass().add("productivity-badge");

            root.getChildren().addAll(textBox, spacer, badge);
            setGraphic(root);

            setupDragDrop(item);
        }

        private void setupDragDrop(Task item) {
            setOnDragDetected(e -> {
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(item.getName());
                db.setContent(cc);
                e.consume();
            });

            setOnDragOver(e -> {
                if (e.getGestureSource() != this && e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    String name = db.getString();
                    Task source = findTask(name);
                    if (source != null) {
                        ArrayList<Task> tasks = controller.getAllTasks();
                        tasks.remove(source);
                        tasks.add(isEmpty() ? tasks.size() : getIndex(), source);
                        controller.refreshList();
                        success = true;
                    }
                }
                e.setDropCompleted(success);
                e.consume();
            });
        }

        private Task findTask(String name) {
            for (Task t : controller.getAllTasks()) {
                if (t.getName().equals(name)) return t;
            }
            return null;
        }
    }
}