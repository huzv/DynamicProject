package util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TableRenderer {
    
    private static final int MAX_DISPLAY_ROWS = 100;
    private static final int MAX_DISPLAY_COLS = 100;
    
    public static VBox renderDPTable(int[][] dp, boolean[][] take) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(8));
        
        int rows = dp.length;
        int cols = dp[0].length;
        
        Label header = new Label(String.format("DP Table Analysis (%d tasks × %d hours)", rows, cols));
        header.getStyleClass().add("dp-header");
        container.getChildren().add(header);
        
        HBox modeBox = new HBox(8);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        Label modeLabel = new Label("View:");
        modeLabel.getStyleClass().add("secondary");
        
        ToggleGroup viewGroup = new ToggleGroup();
        ToggleButton pathBtn = new ToggleButton("Solution Path");
        ToggleButton fullBtn = new ToggleButton("Full Table");
        
        pathBtn.setToggleGroup(viewGroup);
        fullBtn.setToggleGroup(viewGroup);
        
        pathBtn.getStyleClass().add("toggle-group");
        fullBtn.getStyleClass().add("toggle-group");
        
        pathBtn.setSelected(true);
        
        modeBox.getChildren().addAll(modeLabel, pathBtn, fullBtn);
        container.getChildren().add(modeBox);
        

        StackPane contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);
        
        VBox pathView = createSolutionPathView(dp, take);
        VBox fullView = createFullTableView(dp, take);
        
        contentStack.getChildren().addAll(pathView, fullView);
        
        pathView.setVisible(true);
        pathView.setManaged(true);
        fullView.setVisible(false);
        fullView.setManaged(false);
        
        viewGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean showPath = (newVal == pathBtn);
            pathView.setVisible(showPath);
            pathView.setManaged(showPath);
            fullView.setVisible(!showPath);
            fullView.setManaged(!showPath);
        });
        
        container.getChildren().add(contentStack);
        
        return container;
    }
    
    private static VBox createSolutionPathView(int[][] dp, boolean[][] take) {
        VBox view = new VBox(8);
        
        Label title = new Label("📍 Solution Path (Backtracking)");
        title.getStyleClass().add("heading");
        view.getChildren().add(title);

        var path = extractSolutionPath(dp, take);
        
        if (path.isEmpty()) {
            Label empty = new Label("No solution path available");
            empty.getStyleClass().add("secondary");
            view.getChildren().add(empty);
            return view;
        }
        
        GridPane pathGrid = new GridPane();
        pathGrid.setHgap(8);
        pathGrid.setVgap(8);
        pathGrid.setPadding(new Insets(8));
        pathGrid.setStyle("-fx-background-color: #F2F5F9; -fx-border-color: #C4D5E5; " +
                         "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2;");
        
        pathGrid.add(createHeaderLabel("Step"), 0, 0);
        pathGrid.add(createHeaderLabel("Task"), 1, 0);
        pathGrid.add(createHeaderLabel("Time"), 2, 0);
        pathGrid.add(createHeaderLabel("Value"), 3, 0);
        pathGrid.add(createHeaderLabel("Decision"), 4, 0);
        
        int row = 1;
        for (PathStep step : path) {
            pathGrid.add(createPathCell(String.valueOf(row)), 0, row);
            pathGrid.add(createPathCell("Task " + step.taskIndex), 1, row);
            pathGrid.add(createPathCell(String.valueOf(step.time)), 2, row);
            pathGrid.add(createPathCell(String.valueOf(step.value)), 3, row);
            
            Label decision = createPathCell(step.taken ? "✓ TAKE" : "✗ Skip");
            if (step.taken) {
                decision.setStyle(decision.getStyle() + "-fx-text-fill: #83B876; -fx-font-weight: bold;");
            }
            pathGrid.add(decision, 4, row);
            row++;
        }
        
        ScrollPane scroll = new ScrollPane(pathGrid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.getStyleClass().add("scroll");
        
        view.getChildren().add(scroll);
        
        return view;
    }
    
    private static VBox createFullTableView(int[][] dp, boolean[][] take) {
        VBox view = new VBox(8);
        
        int rows = dp.length;
        int cols = dp[0].length;
        
        Label title = new Label("📋 Full Table");
        title.getStyleClass().add("heading");
        view.getChildren().add(title);

        VBox infoBox = new VBox(4);
        infoBox.setPadding(new Insets(8));
        infoBox.setStyle("-fx-background-color: #E7EEF6; -fx-border-color: #C4D5E5; " +
                         "-fx-border-width: 1; -fx-border-radius: 2; -fx-background-radius: 2;");
        
        int displayRows = Math.min(rows, MAX_DISPLAY_ROWS);
        int displayCols = Math.min(cols, MAX_DISPLAY_COLS);
        
        Label dimLabel = new Label(String.format("Table Dimensions: %d × %d", rows, cols));
        Label displayLabel = new Label(String.format("Displaying: %d × %d cells", displayRows, displayCols));
        
        if (rows > MAX_DISPLAY_ROWS || cols > MAX_DISPLAY_COLS) {
            Label warning = new Label("⚠ Table truncated for performance. Showing first " + 
                                     displayRows + " rows and " + displayCols + " columns.");
            warning.setStyle("-fx-text-fill: #C59B00; -fx-font-weight: bold;");
            infoBox.getChildren().addAll(dimLabel, displayLabel, warning);
        } else {
            infoBox.getChildren().addAll(dimLabel, displayLabel);
        }
        
        view.getChildren().add(infoBox);
        

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setPrefViewportWidth(700);
        scrollPane.getStyleClass().add("scroll");

        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setPadding(new Insets(4));
        grid.setStyle("-fx-background-color: #E7EEF6;");

        grid.add(createSmallHeaderCell("i\\t"), 0, 0);
        
        // Column headers: only display what we need
        for (int c = 0; c < displayCols; c++) {
            grid.add(createSmallHeaderCell(String.valueOf(c)), c + 1, 0);
        }
        
        // Rows: only display what we need
        for (int r = 0; r < displayRows; r++) {
            grid.add(createSmallHeaderCell(String.valueOf(r)), 0, r + 1);
            
            for (int c = 0; c < displayCols; c++) {
                boolean highlight = take != null && r > 0 && r < take.length && 
                                   c < take[r].length && take[r][c];
                Label cell = createSmallCell(String.valueOf(dp[r][c]), highlight);
                grid.add(cell, c + 1, r + 1);
            }
        }
        
        scrollPane.setContent(grid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        view.getChildren().add(scrollPane);
        
        if (rows > MAX_DISPLAY_ROWS || cols > MAX_DISPLAY_COLS) {
            Label navHint = new Label("💡 Tip: Use Solution Path view for detailed step-by-step analysis");
            navHint.getStyleClass().add("secondary");
            navHint.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
            view.getChildren().add(navHint);
        }
        
        return view;
    }
    
    // Helper methods
    private static Label createHeaderLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        lbl.setMinWidth(80);
        return lbl;
    }
    
    private static Label createPathCell(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("dp-cell");
        lbl.setMinWidth(100);
        lbl.setPadding(new Insets(6));
        return lbl;
    }
    
    private static Label createSmallHeaderCell(String text) {
        Label lbl = new Label(text);
        lbl.setMinWidth(40);
        lbl.setMaxWidth(40);
        lbl.setMinHeight(28);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-background-color: #2F5D83; -fx-text-fill: white; " +
                     "-fx-font-size: 9px; -fx-font-weight: bold; " +
                     "-fx-border-color: #1E1E24; -fx-border-width: 0.5;");
        return lbl;
    }
    
    private static Label createSmallCell(String text, boolean highlight) {
        Label lbl = new Label(text);
        lbl.setMinWidth(40);
        lbl.setMaxWidth(40);
        lbl.setMinHeight(28);
        lbl.setMaxHeight(28);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-font-size: 9px; -fx-background-color: white; " +
                     "-fx-border-color: #D0D0D0; -fx-border-width: 0.5;");
        
        if (highlight) {
            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #BEE7A5; -fx-font-weight: bold;");
        }
        return lbl;
    }
    
    private static java.util.List<PathStep> extractSolutionPath(int[][] dp, boolean[][] take) {
        java.util.List<PathStep> path = new java.util.ArrayList<>();
        if (take == null || take.length == 0) return path;
        
        int rows = dp.length;
        int cols = dp[0].length;
        
        int t = cols - 1;
        for (int i = rows - 1; i >= 0; i--) {
            if (t < 0 || t >= take[i].length) continue;
            boolean taken = i > 0 && take[i][t];
            path.add(new PathStep(i, t, dp[i][t], taken));
        }
        
        java.util.Collections.reverse(path);
        return path;
    }
    
    static class PathStep {
        int taskIndex;
        int time;
        int value;
        boolean taken;
        
        PathStep(int taskIndex, int time, int value, boolean taken) {
            this.taskIndex = taskIndex;
            this.time = time;
            this.value = value;
            this.taken = taken;
        }
    }
}