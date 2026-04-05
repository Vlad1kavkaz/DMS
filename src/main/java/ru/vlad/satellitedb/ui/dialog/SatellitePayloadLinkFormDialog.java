package ru.vlad.satellitedb.ui.dialog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.CrossSatellitePayload;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.ui.UiTextUtil;

import java.util.List;
import java.util.Optional;

public class SatellitePayloadLinkFormDialog {

    private final Dialog<CrossSatellitePayload> dialog = new Dialog<>();

    private final ComboBox<Payload> payloadBox = new ComboBox<>();
    private final CheckBox primaryCheckBox = new CheckBox("Основная полезная нагрузка");
    private final TextArea notesArea = new TextArea();

    private final CrossSatellitePayload link;
    private final Integer satelliteId;
    private final boolean editMode;

    public SatellitePayloadLinkFormDialog(
            Integer satelliteId,
            CrossSatellitePayload link,
            List<Payload> availablePayloads
    ) {
        this.satelliteId = satelliteId;
        this.editMode = link != null;
        this.link = link != null ? copyLink(link) : new CrossSatellitePayload();

        dialog.setTitle(editMode ? "Редактирование связи со спутником" : "Добавление полезной нагрузки к спутнику");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        initPayloadBox(availablePayloads);
        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    return buildResult();
                } catch (Exception e) {
                    showValidationError(e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    public Optional<CrossSatellitePayload> showAndWaitForResult() {
        return dialog.showAndWait();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);

        int row = 0;

        grid.add(new Label("Полезная нагрузка:"), 0, row);
        grid.add(payloadBox, 1, row++);

        grid.add(primaryCheckBox, 1, row++);

        grid.add(new Label("Примечания:"), 0, row);
        grid.add(notesArea, 1, row);

        return grid;
    }

    private void initPayloadBox(List<Payload> availablePayloads) {
        payloadBox.setItems(FXCollections.observableArrayList(availablePayloads));
        payloadBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Payload payload) {
                if (payload == null) {
                    return "";
                }
                String name = payload.getName() != null ? payload.getName() : "-";
                String type = payload.getType() != null ? UiTextUtil.payloadType(payload.getType()) : "-";
                return name + " (" + type + ")";
            }

            @Override
            public Payload fromString(String string) {
                return null;
            }
        });
    }

    private void fillFields() {
        if (!editMode) {
            return;
        }

        selectPayloadById(link.getPayloadId());
        primaryCheckBox.setSelected(Boolean.TRUE.equals(link.getIsPrimary()));
        notesArea.setText(link.getNotes());
    }

    private void selectPayloadById(Integer payloadId) {
        if (payloadId == null) {
            return;
        }

        for (Payload payload : payloadBox.getItems()) {
            if (payload.getId() != null && payload.getId().equals(payloadId)) {
                payloadBox.setValue(payload);
                return;
            }
        }
    }

    private CrossSatellitePayload buildResult() {
        if (satelliteId == null || satelliteId <= 0) {
            throw new IllegalArgumentException("Не выбран спутник");
        }

        Payload payload = payloadBox.getValue();
        if (payload == null || payload.getId() == null) {
            throw new IllegalArgumentException("Выберите полезную нагрузку");
        }

        link.setSatelliteId(satelliteId);
        link.setPayloadId(payload.getId());
        link.setIsPrimary(primaryCheckBox.isSelected());
        link.setNotes(trimToNull(notesArea.getText()));

        return link;
    }

    private CrossSatellitePayload copyLink(CrossSatellitePayload source) {
        CrossSatellitePayload copy = new CrossSatellitePayload();
        copy.setId(source.getId());
        copy.setSatelliteId(source.getSatelliteId());
        copy.setPayloadId(source.getPayloadId());
        copy.setIsPrimary(source.getIsPrimary());
        copy.setNotes(source.getNotes());
        return copy;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText("Не удалось сохранить связь");
        alert.setContentText(message != null ? message : "Некорректные данные");
        alert.showAndWait();
    }
}