package ru.vlad.satellitedb.ui.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.vlad.satellitedb.model.SatelliteStatus;
import ru.vlad.satellitedb.service.SatelliteStatusService;
import ru.vlad.satellitedb.ui.dialog.SatelliteStatusFormDialog;

import java.util.List;
import java.util.Optional;

public class SatelliteStatusView extends BorderPane {

    private final SatelliteStatusService service = new SatelliteStatusService();
    private final TableView<SatelliteStatus> table = new TableView<>();

    public SatelliteStatusView() {
        setPadding(new Insets(16));

        Label title = new Label("Статусы спутников");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        createColumns();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Обновить");
        Button addButton = new Button("Добавить");
        Button editButton = new Button("Редактировать");
        Button deleteButton = new Button("Удалить");

        refreshButton.setOnAction(e -> loadData());
        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        deleteButton.setOnAction(e -> onDelete());

        HBox buttons = new HBox(10, refreshButton, addButton, editButton, deleteButton);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        setTop(title);
        setCenter(table);
        setBottom(buttons);

        loadData();
    }

    private void createColumns() {
        TableColumn<SatelliteStatus, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getStatus()))
        );

        TableColumn<SatelliteStatus, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription()))
        );

        table.getColumns().addAll(statusCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<SatelliteStatus> statuses = service.getAll();
            table.setItems(FXCollections.observableArrayList(statuses));
        } catch (Exception e) {
            showError("Ошибка загрузки статусов спутников", e.getMessage());
        }
    }

    private void onAdd() {
        SatelliteStatusFormDialog dialog = new SatelliteStatusFormDialog(null);
        Optional<SatelliteStatus> result = dialog.showAndWaitForResult();

        result.ifPresent(status -> {
            try {
                service.create(status);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления статуса спутника", e.getMessage());
            }
        });
    }

    private void onEdit() {
        SatelliteStatus selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите статус спутника для редактирования.");
            return;
        }

        String oldStatus = selected.getStatus();

        SatelliteStatusFormDialog dialog = new SatelliteStatusFormDialog(selected);
        Optional<SatelliteStatus> result = dialog.showAndWaitForResult();

        result.ifPresent(status -> {
            try {
                service.update(status, oldStatus);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования статуса спутника", e.getMessage());
            }
        });
    }

    private void onDelete() {
        SatelliteStatus selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите статус спутника для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить статус спутника?");
        confirm.setContentText("Будет удалён статус: " + selected.getStatus());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getStatus());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления статуса спутника", e.getMessage());
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