package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.PayloadType;
import ru.vlad.satellitedb.service.PayloadTypeService;

import java.util.Optional;
import java.util.stream.Collectors;

public class PayloadFormDialog {

    private final Dialog<Payload> dialog = new Dialog<>();

    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextArea descriptionArea = new TextArea();

    private final PayloadTypeService payloadTypeService = new PayloadTypeService();
    private final Payload payload;

    public PayloadFormDialog(Payload payload) {
        this.payload = payload != null ? payload : new Payload();

        dialog.setTitle(payload == null ? "Добавление полезной нагрузки" : "Редактирование полезной нагрузки");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillChoices();
        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildPayloadFromForm();
            }
            return null;
        });
    }

    public Optional<Payload> showAndWaitForResult() {
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

        grid.add(new Label("Тип:"), 0, row);
        grid.add(typeBox, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row);

        return grid;
    }

    private void fillChoices() {
        typeBox.getItems().setAll(
                payloadTypeService.getAll().stream()
                        .map(PayloadType::getType)
                        .collect(Collectors.toList())
        );

        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return value != null ? value : "";
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    private void fillFields() {
        codeField.setText(payload.getCode());
        nameField.setText(payload.getName());
        typeBox.setValue(payload.getType());
        descriptionArea.setText(payload.getDescription());
    }

    private Payload buildPayloadFromForm() {
        payload.setCode(trimToNull(codeField.getText()));
        payload.setName(trimToNull(nameField.getText()));
        payload.setType(typeBox.getValue());
        payload.setDescription(trimToNull(descriptionArea.getText()));
        return payload;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}