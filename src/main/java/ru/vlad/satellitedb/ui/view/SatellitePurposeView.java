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
import ru.vlad.satellitedb.model.SatellitePurpose;
import ru.vlad.satellitedb.service.SatellitePurposeService;
import ru.vlad.satellitedb.ui.dialog.SatellitePurposeFormDialog;

import java.util.List;
import java.util.Optional;

public class SatellitePurposeView extends BorderPane {

    private final SatellitePurposeService service = new SatellitePurposeService();
    private final TableView<SatellitePurpose> table = new TableView<>();

    public SatellitePurposeView() {
        setPadding(new Insets(16));

        Label title = new Label("Назначения спутников");
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
        TableColumn<SatellitePurpose, String> purposeCol = new TableColumn<>("Назначение");
        purposeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getPurpose()))
        );

        TableColumn<SatellitePurpose, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription()))
        );

        table.getColumns().addAll(purposeCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<SatellitePurpose> purposes = service.getAll();
            table.setItems(FXCollections.observableArrayList(purposes));
        } catch (Exception e) {
            showError("Ошибка загрузки назначений спутников", e.getMessage());
        }
    }

    private void onAdd() {
        SatellitePurposeFormDialog dialog = new SatellitePurposeFormDialog(null);
        Optional<SatellitePurpose> result = dialog.showAndWaitForResult();

        result.ifPresent(purpose -> {
            try {
                service.create(purpose);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления назначения спутника", e.getMessage());
            }
        });
    }

    private void onEdit() {
        SatellitePurpose selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите назначение спутника для редактирования.");
            return;
        }

        String oldPurpose = selected.getPurpose();

        SatellitePurposeFormDialog dialog = new SatellitePurposeFormDialog(selected);
        Optional<SatellitePurpose> result = dialog.showAndWaitForResult();

        result.ifPresent(purpose -> {
            try {
                service.update(purpose, oldPurpose);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования назначения спутника", e.getMessage());
            }
        });
    }

    private void onDelete() {
        SatellitePurpose selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите назначение спутника для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить назначение спутника?");
        confirm.setContentText("Будет удалено назначение: " + selected.getPurpose());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getPurpose());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления назначения спутника", e.getMessage());
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