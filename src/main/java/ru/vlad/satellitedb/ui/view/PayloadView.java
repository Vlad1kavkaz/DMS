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
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.service.PayloadService;
import ru.vlad.satellitedb.ui.dialog.PayloadFormDialog;

import java.util.List;
import java.util.Optional;

public class PayloadView extends BorderPane {

    private final PayloadService service = new PayloadService();
    private final TableView<Payload> table = new TableView<>();

    public PayloadView() {
        setPadding(new Insets(16));

        Label title = new Label("Полезные нагрузки");
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
        TableColumn<Payload, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getCode())));

        TableColumn<Payload, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getName())));

        TableColumn<Payload, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getType()))
        );

        TableColumn<Payload, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription())));

        table.getColumns().addAll(codeCol, nameCol, typeCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<Payload> payloads = service.getAll();
            table.setItems(FXCollections.observableArrayList(payloads));
        } catch (Exception e) {
            showError("Ошибка загрузки полезных нагрузок", e.getMessage());
        }
    }

    private void onAdd() {
        PayloadFormDialog dialog = new PayloadFormDialog(null);
        Optional<Payload> result = dialog.showAndWaitForResult();

        result.ifPresent(payload -> {
            try {
                service.create(payload);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления полезной нагрузки", e.getMessage());
            }
        });
    }

    private void onEdit() {
        Payload selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите полезную нагрузку для редактирования.");
            return;
        }

        Payload editableCopy = copyPayload(selected);

        PayloadFormDialog dialog = new PayloadFormDialog(editableCopy);
        Optional<Payload> result = dialog.showAndWaitForResult();

        result.ifPresent(payload -> {
            try {
                service.update(payload);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования полезной нагрузки", e.getMessage());
            }
        });
    }

    private void onDelete() {
        Payload selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите полезную нагрузку для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить полезную нагрузку?");
        confirm.setContentText("Будет удалена полезная нагрузка: " + selected.getName());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getId());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления полезной нагрузки", e.getMessage());
            }
        }
    }

    private Payload copyPayload(Payload source) {
        Payload copy = new Payload();
        copy.setId(source.getId());
        copy.setCode(source.getCode());
        copy.setName(source.getName());
        copy.setType(source.getType());
        copy.setManufacturerOrganizationId(source.getManufacturerOrganizationId());
        copy.setDescription(source.getDescription());
        return copy;
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