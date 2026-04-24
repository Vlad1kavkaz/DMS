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
import ru.vlad.satellitedb.model.PayloadType;
import ru.vlad.satellitedb.service.PayloadTypeService;
import ru.vlad.satellitedb.ui.dialog.PayloadTypeFormDialog;

import java.util.List;
import java.util.Optional;

public class PayloadTypeView extends BorderPane {

    private final PayloadTypeService service = new PayloadTypeService();
    private final TableView<PayloadType> table = new TableView<>();

    public PayloadTypeView() {
        setPadding(new Insets(16));

        Label title = new Label("Типы полезной нагрузки");
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
        TableColumn<PayloadType, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getType()))
        );

        TableColumn<PayloadType, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription()))
        );

        table.getColumns().addAll(typeCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<PayloadType> types = service.getAll();
            table.setItems(FXCollections.observableArrayList(types));
        } catch (Exception e) {
            showError("Ошибка загрузки типов полезной нагрузки", e.getMessage());
        }
    }

    private void onAdd() {
        PayloadTypeFormDialog dialog = new PayloadTypeFormDialog(null);
        Optional<PayloadType> result = dialog.showAndWaitForResult();

        result.ifPresent(type -> {
            try {
                service.create(type);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления типа полезной нагрузки", e.getMessage());
            }
        });
    }

    private void onEdit() {
        PayloadType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите тип полезной нагрузки для редактирования.");
            return;
        }

        String oldType = selected.getType();

        PayloadTypeFormDialog dialog = new PayloadTypeFormDialog(selected);
        Optional<PayloadType> result = dialog.showAndWaitForResult();

        result.ifPresent(type -> {
            try {
                service.update(type, oldType);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования типа полезной нагрузки", e.getMessage());
            }
        });
    }

    private void onDelete() {
        PayloadType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите тип полезной нагрузки для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить тип полезной нагрузки?");
        confirm.setContentText("Будет удалён тип: " + selected.getType());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getType());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления типа полезной нагрузки", e.getMessage());
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