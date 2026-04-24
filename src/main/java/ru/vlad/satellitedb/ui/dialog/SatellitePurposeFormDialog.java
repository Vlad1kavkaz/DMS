package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.vlad.satellitedb.model.SatellitePurpose;

import java.util.Optional;

public class SatellitePurposeFormDialog {

    private final Dialog<SatellitePurpose> dialog = new Dialog<>();

    private final TextField purposeField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    private final SatellitePurpose purpose;

    public SatellitePurposeFormDialog(SatellitePurpose purpose) {
        this.purpose = purpose != null ? copyPurpose(purpose) : new SatellitePurpose();

        dialog.setTitle(purpose == null ? "Добавление назначения спутника" : "Редактирование назначения спутника");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildPurposeFromForm();
            }
            return null;
        });
    }

    public Optional<SatellitePurpose> showAndWaitForResult() {
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

        grid.add(new Label("Назначение:"), 0, row);
        grid.add(purposeField, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row);

        return grid;
    }

    private void fillFields() {
        purposeField.setText(purpose.getPurpose());
        descriptionArea.setText(purpose.getDescription());
    }

    private SatellitePurpose buildPurposeFromForm() {
        purpose.setPurpose(trimToNull(purposeField.getText()));
        purpose.setDescription(trimToNull(descriptionArea.getText()));
        return purpose;
    }

    private SatellitePurpose copyPurpose(SatellitePurpose source) {
        SatellitePurpose copy = new SatellitePurpose();
        copy.setPurpose(source.getPurpose());
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