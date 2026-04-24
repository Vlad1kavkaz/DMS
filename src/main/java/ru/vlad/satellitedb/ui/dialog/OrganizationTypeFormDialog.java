package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.vlad.satellitedb.model.OrganizationType;

import java.util.Optional;

public class OrganizationTypeFormDialog {

    private final Dialog<OrganizationType> dialog = new Dialog<>();

    private final TextField typeField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    private final OrganizationType type;

    public OrganizationTypeFormDialog(OrganizationType type) {
        this.type = type != null ? copyType(type) : new OrganizationType();

        dialog.setTitle(type == null ? "Добавление типа организации" : "Редактирование типа организации");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildTypeFromForm();
            }
            return null;
        });
    }

    public Optional<OrganizationType> showAndWaitForResult() {
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

        grid.add(new Label("Тип:"), 0, row);
        grid.add(typeField, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row);

        return grid;
    }

    private void fillFields() {
        typeField.setText(type.getType());
        descriptionArea.setText(type.getDescription());
    }

    private OrganizationType buildTypeFromForm() {
        type.setType(trimToNull(typeField.getText()));
        type.setDescription(trimToNull(descriptionArea.getText()));
        return type;
    }

    private OrganizationType copyType(OrganizationType source) {
        OrganizationType copy = new OrganizationType();
        copy.setType(source.getType());
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