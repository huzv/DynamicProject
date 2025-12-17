package util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class TableRenderer {

    public static Node renderDPTable(int[][] dp, boolean[][] take) {
        int rows = dp.length;
        int cols = dp[0].length;
        int maxVal = dp[rows - 1][cols - 1];

        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);

        // Time headers
        for (int c = 0; c < cols; c++) {
            double hours = c / 2.0;
            String hStr = (hours % 1 == 0) ? String.format("%.0f", hours) : String.format("%.1f", hours);

            Label lbl = new Label(hStr);
            lbl.getStyleClass().add("dp-header");
            lbl.setMinWidth(35);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, c + 1, 0);
        }

        // Task rows
        for (int r = 0; r < rows; r++) {
            Label rowHead = new Label(r == 0 ? "Init" : "Task " + r);
            rowHead.getStyleClass().add("dp-header");
            rowHead.setMinWidth(60);
            grid.add(rowHead, 0, r + 1);

            for (int c = 0; c < cols; c++) {
                int val = dp[r][c];
                Label cell = new Label(String.valueOf(val));
                cell.getStyleClass().add("dp-cell");

                // Heatmap
                if (maxVal > 0 && val > 0) {
                    double ratio = (double) val / maxVal;
                    if (ratio > 0.8) cell.getStyleClass().add("heat-high");
                    else if (ratio > 0.4) cell.getStyleClass().add("heat-med");
                    else cell.getStyleClass().add("heat-low");
                }

                // Highlight the changes only
                if (r > 0 && c > 0 && dp[r][c] != dp[r - 1][c]) {
                    cell.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2px;");
                }

                grid.add(cell, c + 1, r + 1);
            }
        }
        return grid;
    }
}