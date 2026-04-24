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
import ru.vlad.satellitedb.model.OrganizationType;
import ru.vlad.satellitedb.service.OrganizationTypeService;
import ru.vlad.satellitedb.ui.dialog.OrganizationTypeFormDialog;

import java.util.List;
import java.util.Optional;

public class OrganizationTypeView extends BorderPane {

    private final OrganizationTypeService service = new OrganizationTypeService();
    private final TableView<OrganizationType> table = new TableView<>();

    public OrganizationTypeView() {
        setPadding(new Insets(16));

        Label title = new Label("Типы организаций");
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
        TableColumn<OrganizationType, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getType()))
        );

        TableColumn<OrganizationType, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription()))
        );

        table.getColumns().addAll(typeCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<OrganizationType> types = service.getAll();
            table.setItems(FXCollections.observableArrayList(types));
        } catch (Exception e) {
            showError("Ошибка загрузки типов организаций", e.getMessage());
        }
    }

    private void onAdd() {
        OrganizationTypeFormDialog dialog = new OrganizationTypeFormDialog(null);
        Optional<OrganizationType> result = dialog.showAndWaitForResult();

        result.ifPresent(type -> {
            try {
                service.create(type);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления типа организации", e.getMessage());
            }
        });
    }

    private void onEdit() {
        OrganizationType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите тип организации для редактирования.");
            return;
        }

        String oldType = selected.getType();

        OrganizationTypeFormDialog dialog = new OrganizationTypeFormDialog(selected);
        Optional<OrganizationType> result = dialog.showAndWaitForResult();

        result.ifPresent(type -> {
            try {
                service.update(type, oldType);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования типа организации", e.getMessage());
            }
        });
    }

    private void onDelete() {
        OrganizationType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите тип организации для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить тип организации?");
        confirm.setContentText("Будет удалён тип: " + selected.getType());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getType());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления типа организации", e.getMessage());
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