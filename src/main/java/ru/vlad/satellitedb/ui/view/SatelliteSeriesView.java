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
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.SatelliteSeriesService;
import ru.vlad.satellitedb.ui.dialog.SatelliteSeriesFormDialog;

import java.util.List;
import java.util.Optional;

public class SatelliteSeriesView extends BorderPane {

    private final SatelliteSeriesService service = new SatelliteSeriesService();
    private final TableView<SatelliteSeries> table = new TableView<>();

    public SatelliteSeriesView() {
        setPadding(new Insets(16));

        Label title = new Label("Серии спутников");
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
        TableColumn<SatelliteSeries, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getCode())));

        TableColumn<SatelliteSeries, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getName())));

        TableColumn<SatelliteSeries, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getDescription())));

        table.getColumns().addAll(codeCol, nameCol, descriptionCol);
    }

    private void loadData() {
        try {
            List<SatelliteSeries> seriesList = service.getAll();
            table.setItems(FXCollections.observableArrayList(seriesList));
        } catch (Exception e) {
            showError("Ошибка загрузки серий спутников", e.getMessage());
        }
    }

    private void onAdd() {
        SatelliteSeriesFormDialog dialog = new SatelliteSeriesFormDialog(null);
        Optional<SatelliteSeries> result = dialog.showAndWaitForResult();

        result.ifPresent(series -> {
            try {
                service.create(series);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления серии спутников", e.getMessage());
            }
        });
    }

    private void onEdit() {
        SatelliteSeries selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите серию спутников для редактирования.");
            return;
        }

        SatelliteSeries editableCopy = copySeries(selected);

        SatelliteSeriesFormDialog dialog = new SatelliteSeriesFormDialog(editableCopy);
        Optional<SatelliteSeries> result = dialog.showAndWaitForResult();

        result.ifPresent(series -> {
            try {
                service.update(series);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования серии спутников", e.getMessage());
            }
        });
    }

    private void onDelete() {
        SatelliteSeries selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите серию спутников для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить серию спутников?");
        confirm.setContentText("Будет удалена серия: " + selected.getName());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                service.delete(selected.getId());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления серии спутников", e.getMessage());
            }
        }
    }

    private SatelliteSeries copySeries(SatelliteSeries source) {
        SatelliteSeries copy = new SatelliteSeries();
        copy.setId(source.getId());
        copy.setCode(source.getCode());
        copy.setName(source.getName());
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