package util;

import controller.MainController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import model.Task;

import java.util.Optional;
import java.util.function.Consumer;

public class DialogHelper {

    private final MainController controller;

    public DialogHelper(MainController controller) {
        this.controller = controller;
    }

    public void showAddTaskDialog(Consumer<Task> callback) {
        Dialog<Task> dialog = createBaseDialog("New Task");

        TextField nameField = createTextField("Task Name");
        TextField costField = createTextField("Hours (e.g. 1.5)");
        TextField valueField = createTextField("Value");

        GridPane grid = createFormGrid();
        grid.addRow(0, createLabel("Name:"), nameField);
        grid.addRow(1, createLabel("Cost:"), costField);
        grid.addRow(2, createLabel("Value:"), valueField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    float duration = Float.parseFloat(costField.getText().trim());
                    int productivity = Integer.parseInt(valueField.getText().trim());

                    if (name.isEmpty() || duration <= 0 || productivity <= 0) {
                        return null;
                    }
                    if (duration % 0.5f != 0) {
                        return null;
                    }
                    return new Task(name, duration, productivity);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(callback);
    }

    public void showEditTaskDialog(Task task, Consumer<Boolean> callback) {
        Dialog<ButtonType> dialog = createBaseDialog("Edit Task");

        TextField nameField = createTextField("Task Name");
        nameField.setText(task.getName());

        TextField costField = createTextField("Hours");
        costField.setText(String.valueOf(task.getDuration()));

        TextField valueField = createTextField("Value");
        valueField.setText(String.valueOf(task.getProductivity()));

        GridPane grid = createFormGrid();
        grid.addRow(0, createLabel("Name:"), nameField);
        grid.addRow(1, createLabel("Hours:"), costField);
        grid.addRow(2, createLabel("Value:"), valueField);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String newName = nameField.getText().trim();
                float newDuration = Float.parseFloat(costField.getText().trim());
                int newValue = Integer.parseInt(valueField.getText().trim());

                if (newName.isEmpty() || newDuration <= 0 || newValue <= 0) {
                    callback.accept(false);
                    return;
                }
                if (newDuration % 0.5f != 0) {
                    callback.accept(false);
                    return;
                }

                task.setName(newName);
                task.setDuration(newDuration);
                task.setProductivity(newValue);
                callback.accept(true);
            } catch (NumberFormatException e) {
                callback.accept(false);
            }
        } else {
            callback.accept(false);
        }
    }

    private <T> Dialog<T> createBaseDialog(String title) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initStyle(StageStyle.UTILITY);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.getStyleClass().add("custom-dialog");

        // Apply app stylesheet
        try {
            pane.getStylesheets().add(
                getClass().getResource("/resources/styles.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        // Style buttons
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        if (okBtn != null) okBtn.getStyleClass().addAll("btn", "btn-success");
        if (cancelBtn != null) cancelBtn.getStyleClass().addAll("btn", "btn-secondary");

        return dialog;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input");
        field.setPrefWidth(200);
        return field;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("input-label");
        return label;
    }
}