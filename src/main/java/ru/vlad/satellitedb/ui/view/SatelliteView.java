package ru.vlad.satellitedb.ui.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.vlad.satellitedb.model.Organization;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.OrganizationService;
import ru.vlad.satellitedb.service.SatelliteSeriesService;
import ru.vlad.satellitedb.service.SatelliteService;
import ru.vlad.satellitedb.ui.UiTextUtil;
import ru.vlad.satellitedb.ui.dialog.SatelliteFormDialog;
import ru.vlad.satellitedb.util.SatelliteImageUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SatelliteView extends BorderPane {

    private final SatelliteService satelliteService = new SatelliteService();
    private final SatelliteSeriesService seriesService = new SatelliteSeriesService();
    private final OrganizationService organizationService = new OrganizationService();

    private final TableView<Satellite> table = new TableView<>();

    private final Map<Integer, String> seriesNameCache = new HashMap<>();
    private final Map<Integer, String> organizationNameCache = new HashMap<>();

    public SatelliteView() {
        setPadding(new Insets(16));

        Label title = new Label("Спутники");
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

        loadCaches();
        loadData();
    }

    private void createColumns() {
        TableColumn<Satellite, byte[]> photoCol = new TableColumn<>("Фото");
        photoCol.setPrefWidth(110);
        photoCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPhoto()));
        photoCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label emptyLabel = new Label("Нет фото");

            {
                imageView.setFitWidth(90);
                imageView.setFitHeight(54);
                imageView.setPreserveRatio(true);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(byte[] item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                if (item == null || item.length == 0) {
                    setGraphic(emptyLabel);
                    return;
                }

                imageView.setImage(SatelliteImageUtil.toFxImage(item));
                setGraphic(imageView);
            }
        });

        TableColumn<Satellite, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Satellite, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getName())));

        TableColumn<Satellite, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullSafe(data.getValue().getCode())));

        TableColumn<Satellite, String> seriesCol = new TableColumn<>("Серия");
        seriesCol.setCellValueFactory(data -> {
            Integer seriesId = data.getValue().getSatelliteSeriesId();
            String value = seriesId != null ? seriesNameCache.getOrDefault(seriesId, "") : "";
            return new ReadOnlyStringWrapper(value);
        });

        TableColumn<Satellite, String> manufacturerCol = new TableColumn<>("Производитель");
        manufacturerCol.setCellValueFactory(data -> {
            Integer orgId = data.getValue().getManufacturerOrganizationId();
            String value = orgId != null ? organizationNameCache.getOrDefault(orgId, "") : "";
            return new ReadOnlyStringWrapper(value);
        });

        TableColumn<Satellite, String> purposeCol = new TableColumn<>("Назначение");
        purposeCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiTextUtil.satellitePurpose(data.getValue().getPurpose()))
        );

        TableColumn<Satellite, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiTextUtil.satelliteStatus(data.getValue().getStatus()))
        );

        TableColumn<Satellite, LocalDate> launchDateCol = new TableColumn<>("Дата запуска");
        launchDateCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLaunchDate()));

        table.getColumns().addAll(
                photoCol,
                idCol,
                nameCol,
                codeCol,
                seriesCol,
                manufacturerCol,
                purposeCol,
                statusCol,
                launchDateCol
        );
    }

    private void loadCaches() {
        seriesNameCache.clear();
        for (SatelliteSeries series : seriesService.getAll()) {
            if (series.getId() != null) {
                seriesNameCache.put(series.getId(), nullSafe(series.getName()));
            }
        }

        organizationNameCache.clear();
        for (Organization organization : organizationService.getAll()) {
            if (organization.getId() != null) {
                String displayName = organization.getShortName() != null && !organization.getShortName().isBlank()
                        ? organization.getShortName()
                        : nullSafe(organization.getName());
                organizationNameCache.put(organization.getId(), displayName);
            }
        }
    }

    private void loadData() {
        try {
            loadCaches();
            List<Satellite> satellites = satelliteService.getAllSatellites();
            table.setItems(FXCollections.observableArrayList(satellites));
        } catch (Exception e) {
            showError("Ошибка загрузки спутников", e.getMessage());
        }
    }

    private void onAdd() {
        SatelliteFormDialog dialog = new SatelliteFormDialog(null);
        Optional<Satellite> result = dialog.showAndWaitForResult();

        result.ifPresent(satellite -> {
            try {
                satelliteService.createSatellite(satellite);
                loadData();
            } catch (Exception e) {
                showError("Ошибка добавления спутника", e.getMessage());
            }
        });
    }

    private void onEdit() {
        Satellite selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Редактирование", "Выберите спутник для редактирования.");
            return;
        }

        Satellite editableCopy = copySatellite(selected);

        SatelliteFormDialog dialog = new SatelliteFormDialog(editableCopy);
        Optional<Satellite> result = dialog.showAndWaitForResult();

        result.ifPresent(satellite -> {
            try {
                satelliteService.updateSatellite(satellite);
                loadData();
            } catch (Exception e) {
                showError("Ошибка редактирования спутника", e.getMessage());
            }
        });
    }

    private void onDelete() {
        Satellite selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Удаление", "Выберите спутник для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить спутник?");
        confirm.setContentText("Будет удалён спутник: " + selected.getName());

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                satelliteService.deleteSatellite(selected.getId());
                loadData();
            } catch (Exception e) {
                showError("Ошибка удаления спутника", e.getMessage());
            }
        }
    }

    private Satellite copySatellite(Satellite source) {
        Satellite copy = new Satellite();
        copy.setId(source.getId());
        copy.setSatelliteSeriesId(source.getSatelliteSeriesId());
        copy.setName(source.getName());
        copy.setCode(source.getCode());
        copy.setInternationalDesignator(source.getInternationalDesignator());
        copy.setNoradCatalogNumber(source.getNoradCatalogNumber());
        copy.setPurpose(source.getPurpose());
        copy.setStatus(source.getStatus());
        copy.setLaunchDate(source.getLaunchDate());
        copy.setOperationStartDate(source.getOperationStartDate());
        copy.setDecommissionDate(source.getDecommissionDate());
        copy.setOperatorOrganizationId(source.getOperatorOrganizationId());
        copy.setOwnerOrganizationId(source.getOwnerOrganizationId());
        copy.setManufacturerOrganizationId(source.getManufacturerOrganizationId());
        copy.setDescription(source.getDescription());
        copy.setNotes(source.getNotes());
        copy.setPhoto(source.getPhoto());
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