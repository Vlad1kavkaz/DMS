package ru.vlad.satellitedb.ui.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
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
import ru.vlad.satellitedb.model.Organization;
import ru.vlad.satellitedb.service.OrganizationService;
import ru.vlad.satellitedb.ui.UiTextUtil;
import ru.vlad.satellitedb.ui.dialog.OrganizationFormDialog;

import java.util.List;
import java.util.Optional;

public class OrganizationView extends BorderPane {

    private final OrganizationService service = new OrganizationService();
    private final TableView<Organization> table = new TableView<>();

    public OrganizationView() {
        setPadding(new Insets(16));

        Label title = new Label("Организации");
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
        TableColumn<Organization, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Organization, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getName())));

        TableColumn<Organization, String> shortNameCol = new TableColumn<>("Краткое название");
        shortNameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getShortName())));

        TableColumn<Organization, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiTextUtil.organizationType(data.getValue().getType()))
        );

        TableColumn<Organization, String> countryCol = new TableColumn<>("Страна");
        countryCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getCountryCode())));

        TableColumn<Organization, String> websiteCol = new TableColumn<>("Сайт");
        websiteCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getWebsite())));

        table.getColumns().addAll(idCol, nameCol, shortNameCol, typeCol, countryCol, websiteCol);
    }

    private void loadData() {
        try {
            List<Organization> organizations = service.getAll();
            table.setItems(FXCollections.observableArrayList(organizations));
        } catch (Exception e) {
            showError("Ошибка загрузки организаций", e.getMessage());
        }
    }

    private void onAdd() {
        OrganizationFormDialog dialog = new OrganizationFormDialog(null);
        Optional<Organization> result = dialog.showAndWaitForResult();

        result.ifPresent(organization -> {
            try {
                service.create(organization);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления организации", e.getMessage());
            }
        });
    }

    private void onEdit() {
        Organization selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите организацию для редактирования.");
            return;
        }

        Organization editableCopy = copyOrganization(selected);

        OrganizationFormDialog dialog = new OrganizationFormDialog(editableCopy);
        Optional<Organization> result = dialog.showAndWaitForResult();

        result.ifPresent(organization -> {
            try {
                service.update(organization);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования организации", e.getMessage());
            }
        });
    }

    private void onDelete() {
        Organization selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите организацию для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить организацию?");
        confirm.setContentText("Будет удалена организация: " + selected.getName());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getId());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления организации", e.getMessage());
            }
        }
    }

    private Organization copyOrganization(Organization source) {
        Organization copy = new Organization();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setShortName(source.getShortName());
        copy.setType(source.getType());
        copy.setCountryCode(source.getCountryCode());
        copy.setWebsite(source.getWebsite());
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