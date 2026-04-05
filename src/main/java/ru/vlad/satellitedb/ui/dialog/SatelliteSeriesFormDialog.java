package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.vlad.satellitedb.model.SatelliteSeries;

import java.util.Optional;

public class SatelliteSeriesFormDialog {

    private final Dialog<SatelliteSeries> dialog = new Dialog<>();

    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    private final SatelliteSeries series;

    public SatelliteSeriesFormDialog(SatelliteSeries series) {
        this.series = series != null ? series : new SatelliteSeries();

        dialog.setTitle(series == null ? "Добавление серии спутников" : "Редактирование серии спутников");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildSeriesFromForm();
            }
            return null;
        });
    }

    public Optional<SatelliteSeries> showAndWaitForResult() {
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

        grid.add(new Label("Код:"), 0, row);
        grid.add(codeField, 1, row++);

        grid.add(new Label("Название:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row);

        return grid;
    }

    private void fillFields() {
        codeField.setText(series.getCode());
        nameField.setText(series.getName());
        descriptionArea.setText(series.getDescription());
    }

    private SatelliteSeries buildSeriesFromForm() {
        series.setCode(trimToNull(codeField.getText()));
        series.setName(trimToNull(nameField.getText()));
        series.setDescription(trimToNull(descriptionArea.getText()));
        return series;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}