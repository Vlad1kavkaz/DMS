package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.vlad.satellitedb.model.SatelliteStatus;

import java.util.Optional;

public class SatelliteStatusFormDialog {

    private final Dialog<SatelliteStatus> dialog = new Dialog<>();

    private final TextField statusField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    private final SatelliteStatus status;

    public SatelliteStatusFormDialog(SatelliteStatus status) {
        this.status = status != null ? copyStatus(status) : new SatelliteStatus();

        dialog.setTitle(status == null ? "Добавление статуса спутника" : "Редактирование статуса спутника");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildStatusFromForm();
            }
            return null;
        });
    }

    public Optional<SatelliteStatus> showAndWaitForResult() {
        return dialog.showAndWait();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        int row = 0;

        grid.add(new Label("Статус:"), 0, row);
        grid.add(statusField, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row);

        return grid;
    }

    private void fillFields() {
        statusField.setText(status.getStatus());
        descriptionArea.setText(status.getDescription());
    }

    private SatelliteStatus buildStatusFromForm() {
        status.setStatus(trimToNull(statusField.getText()));
        status.setDescription(trimToNull(descriptionArea.getText()));
        return status;
    }

    private SatelliteStatus copyStatus(SatelliteStatus source) {
        SatelliteStatus copy = new SatelliteStatus();
        copy.setStatus(source.getStatus());
        copy.setDescription(source.getDescription());
        return copy;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}