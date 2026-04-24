package ru.vlad.satellitedb.ui.dialog;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.vlad.satellitedb.model.CrossSatellitePayload;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.service.CrossSatellitePayloadService;
import ru.vlad.satellitedb.service.PayloadService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SatellitePayloadLinkDialog {

    private final Dialog<Void> dialog = new Dialog<>();

    private final Integer satelliteId;
    private final String satelliteName;

    private final CrossSatellitePayloadService linkService = new CrossSatellitePayloadService();
    private final PayloadService payloadService = new PayloadService();

    private final TableView<CrossSatellitePayload> table = new TableView<>();
    private final Map<Integer, Payload> payloadCache = new HashMap<>();

    public SatellitePayloadLinkDialog(Integer satelliteId, String satelliteName) {
        this.satelliteId = satelliteId;
        this.satelliteName = satelliteName;

        dialog.setTitle("Полезные нагрузки спутника");

        ButtonType closeButtonType = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setPrefWidth(900);
        dialog.getDialogPane().setPrefHeight(600);

        loadPayloadCache();
        createColumns();
        loadData();
    }

    public void showAndWait() {
        dialog.showAndWait();
    }

    private BorderPane buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        Label title = new Label("Полезные нагрузки спутника: " + (satelliteName != null ? satelliteName : "-"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Обновить");
        Button addButton = new Button("Добавить");
        Button editButton = new Button("Редактировать");
        Button deleteButton = new Button("Удалить");

        refreshButton.setOnAction(e -> {
            loadPayloadCache();
            loadData();
        });
        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        deleteButton.setOnAction(e -> onDelete());

        HBox buttons = new HBox(10, refreshButton, addButton, editButton, deleteButton);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        root.setTop(title);
        root.setCenter(table);
        root.setBottom(buttons);

        return root;
    }

    private void createColumns() {
        TableColumn<CrossSatellitePayload, String> payloadCol = new TableColumn<>("Полезная нагрузка");
        payloadCol.setCellValueFactory(data -> {
            Payload payload = payloadCache.get(data.getValue().getPayloadId());
            String value = payload != null ? nullSafe(payload.getName()) : "Неизвестно";
            return new ReadOnlyStringWrapper(value);
        });

        TableColumn<CrossSatellitePayload, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data -> {
            Payload payload = payloadCache.get(data.getValue().getPayloadId());
            String value = payload != null ? nullSafe(payload.getType()) : "";
            return new ReadOnlyStringWrapper(value);
        });

        TableColumn<CrossSatellitePayload, String> primaryCol = new TableColumn<>("Основная");
        primaryCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(Boolean.TRUE.equals(data.getValue().getIsPrimary()) ? "Да" : "Нет")
        );

        TableColumn<CrossSatellitePayload, String> notesCol = new TableColumn<>("Примечания");
        notesCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getNotes())));

        table.getColumns().setAll(payloadCol, typeCol, primaryCol, notesCol);
    }

    private void loadPayloadCache() {
        payloadCache.clear();
        List<Payload> payloads = payloadService.getAll();
        for (Payload payload : payloads) {
            if (payload.getId() != null) {
                payloadCache.put(payload.getId(), payload);
            }
        }
    }

    private void loadData() {
        try {
            List<CrossSatellitePayload> links = linkService.getLinksBySatelliteId(satelliteId);
            table.setItems(FXCollections.observableArrayList(links));
        } catch (Exception e) {
            showError("Ошибка загрузки связей", e.getMessage());
        }
    }

    private void onAdd() {
        try {
            List<Payload> payloads = payloadService.getAll();
            SatellitePayloadLinkFormDialog formDialog =
                    new SatellitePayloadLinkFormDialog(satelliteId, null, payloads);

            Optional<CrossSatellitePayload> result = formDialog.showAndWaitForResult();
            result.ifPresent(link -> {
                try {
                    linkService.createLink(link);
                    loadData();
                } catch (Exception e) {
                    showError("Ошибка добавления связи", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
        }
    }

    private void onEdit() {
        CrossSatellitePayload selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите связь для редактирования.");
            return;
        }

        try {
            List<Payload> payloads = payloadService.getAll();
            SatellitePayloadLinkFormDialog formDialog =
                    new SatellitePayloadLinkFormDialog(satelliteId, selected, payloads);

            Optional<CrossSatellitePayload> result = formDialog.showAndWaitForResult();
            result.ifPresent(link -> {
                try {
                    linkService.updateLink(link);
                    loadData();
                } catch (Exception e) {
                    showError("Ошибка редактирования связи", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
        }
    }

    private void onDelete() {
        CrossSatellitePayload selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите связь для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить связь полезной нагрузки со спутником?");
        confirm.setContentText("Связь будет удалена.");

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                linkService.deleteLink(selected.getId());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления связи", e.getMessage());
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message != null ? message : "Неизвестная ошибка");
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}