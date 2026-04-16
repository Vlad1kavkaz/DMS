package ru.vlad.satellitedb.ui.dialog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.Organization;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.OrganizationService;
import ru.vlad.satellitedb.service.SatelliteSeriesService;
import ru.vlad.satellitedb.ui.UiTextUtil;
import ru.vlad.satellitedb.util.SatelliteImageUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SatelliteFormDialog {

    private final Dialog<Satellite> dialog = new Dialog<>();

    private final TextField nameField = new TextField();
    private final TextField codeField = new TextField();
    private final TextField internationalDesignatorField = new TextField();
    private final ComboBox<SatelliteSeries> seriesBox = new ComboBox<>();
    private final ComboBox<Organization> operatorBox = new ComboBox<>();
    private final ComboBox<Organization> ownerBox = new ComboBox<>();
    private final ComboBox<Organization> manufacturerBox = new ComboBox<>();
    private final ComboBox<String> purposeBox = new ComboBox<>();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker launchDatePicker = new DatePicker();
    private final DatePicker operationStartDatePicker = new DatePicker();
    private final DatePicker decommissionDatePicker = new DatePicker();
    private final TextArea descriptionArea = new TextArea();
    private final TextArea notesArea = new TextArea();

    private final ImageView photoPreview = new ImageView();
    private final Label photoInfoLabel = new Label("Фотография не выбрана");
    private final Button choosePhotoButton = new Button("Выбрать фотографию");
    private final Button clearPhotoButton = new Button("Удалить фотографию");

    private final SatelliteSeriesService seriesService = new SatelliteSeriesService();
    private final OrganizationService organizationService = new OrganizationService();
    private final Satellite satellite;

    public SatelliteFormDialog(Satellite satellite) {
        this.satellite = satellite != null ? satellite : new Satellite();

        dialog.setTitle(satellite == null ? "Добавление спутника" : "Редактирование спутника");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        ScrollPane scrollPane = new ScrollPane(buildForm());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(860);
        dialog.getDialogPane().setPrefHeight(760);

        fillChoices();
        initPhotoControls();
        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildSatelliteFromForm();
            }
            return null;
        });
    }

    public Optional<Satellite> showAndWaitForResult() {
        return dialog.showAndWait();
    }

    private VBox buildForm() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(16));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        descriptionArea.setPrefRowCount(3);
        notesArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        notesArea.setWrapText(true);

        photoPreview.setFitWidth(260);
        photoPreview.setFitHeight(160);
        photoPreview.setPreserveRatio(true);
        photoPreview.setSmooth(true);

        int row = 0;

        grid.add(new Label("Название:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Код:"), 0, row);
        grid.add(codeField, 1, row++);

        grid.add(new Label("Международное обозначение:"), 0, row);
        grid.add(internationalDesignatorField, 1, row++);

        grid.add(new Label("Серия:"), 0, row);
        grid.add(seriesBox, 1, row++);

        grid.add(new Label("Оператор:"), 0, row);
        grid.add(operatorBox, 1, row++);

        grid.add(new Label("Владелец:"), 0, row);
        grid.add(ownerBox, 1, row++);

        grid.add(new Label("Производитель:"), 0, row);
        grid.add(manufacturerBox, 1, row++);

        grid.add(new Label("Назначение:"), 0, row);
        grid.add(purposeBox, 1, row++);

        grid.add(new Label("Статус:"), 0, row);
        grid.add(statusBox, 1, row++);

        grid.add(new Label("Дата запуска:"), 0, row);
        grid.add(launchDatePicker, 1, row++);

        grid.add(new Label("Начало эксплуатации:"), 0, row);
        grid.add(operationStartDatePicker, 1, row++);

        grid.add(new Label("Дата списания:"), 0, row);
        grid.add(decommissionDatePicker, 1, row++);

        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        grid.add(new Label("Примечания:"), 0, row);
        grid.add(notesArea, 1, row++);

        HBox photoButtons = new HBox(10, choosePhotoButton, clearPhotoButton);

        VBox photoBox = new VBox(8,
                new Label("Фотография спутника"),
                photoPreview,
                photoInfoLabel,
                photoButtons
        );

        root.getChildren().addAll(grid, photoBox);
        return root;
    }

    private void fillChoices() {
        initSeriesBox();
        initOrganizationBoxes();
        initPurposeBox();
        initStatusBox();
    }

    private void initSeriesBox() {
        List<SatelliteSeries> seriesList = seriesService.getAll();
        seriesBox.setItems(FXCollections.observableArrayList(seriesList));
        seriesBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SatelliteSeries series) {
                if (series == null) {
                    return "";
                }
                String name = series.getName() != null ? series.getName() : "-";
                String code = series.getCode() != null ? series.getCode() : "-";
                return name + " [" + code + "]";
            }

            @Override
            public SatelliteSeries fromString(String string) {
                return null;
            }
        });
    }

    private void initOrganizationBoxes() {
        List<Organization> allOrganizations = organizationService.getAll();

        operatorBox.setItems(FXCollections.observableArrayList(
                filterOrganizations(allOrganizations, "operator", "agency", "other")
        ));
        ownerBox.setItems(FXCollections.observableArrayList(
                filterOrganizations(allOrganizations, "owner", "agency", "other")
        ));
        manufacturerBox.setItems(FXCollections.observableArrayList(
                filterOrganizations(allOrganizations, "manufacturer", "agency", "other")
        ));

        StringConverter<Organization> converter = new StringConverter<>() {
            @Override
            public String toString(Organization organization) {
                if (organization == null) {
                    return "";
                }
                if (organization.getShortName() != null && !organization.getShortName().isBlank()) {
                    return organization.getShortName() + " — " + organization.getName();
                }
                return organization.getName() != null ? organization.getName() : "";
            }

            @Override
            public Organization fromString(String string) {
                return null;
            }
        };

        operatorBox.setConverter(converter);
        ownerBox.setConverter(converter);
        manufacturerBox.setConverter(converter);
    }

    private List<Organization> filterOrganizations(List<Organization> organizations, String... allowedTypes) {
        List<String> allowed = List.of(allowedTypes);
        return organizations.stream()
                .filter(org -> org.getType() != null && allowed.contains(org.getType()))
                .collect(Collectors.toList());
    }

    private void initPurposeBox() {
        purposeBox.getItems().addAll(
                "meteorology",
                "hydrology",
                "remote_sensing",
                "climate_monitoring",
                "ocean_monitoring",
                "ice_monitoring",
                "environment_monitoring",
                "multi_purpose",
                "other"
        );
        purposeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return UiTextUtil.satellitePurpose(value);
            }

            @Override
            public String fromString(String string) {
                return findKeyByValue(string, satellitePurposeMap());
            }
        });
    }

    private void initStatusBox() {
        statusBox.getItems().addAll(
                "planned",
                "active",
                "reserve",
                "inactive",
                "lost",
                "retired"
        );
        statusBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return UiTextUtil.satelliteStatus(value);
            }

            @Override
            public String fromString(String string) {
                return findKeyByValue(string, satelliteStatusMap());
            }
        });
    }

    private void initPhotoControls() {
        choosePhotoButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Выбор фотографии спутника");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );

            File file = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file == null) {
                return;
            }

            try {
                satellite.setPhoto(SatelliteImageUtil.loadAndCompress(file));
                updatePhotoPreview();
            } catch (Exception e) {
                photoInfoLabel.setText("Ошибка загрузки изображения");
            }
        });

        clearPhotoButton.setOnAction(event -> {
            satellite.setPhoto(null);
            updatePhotoPreview();
        });
    }

    private void fillFields() {
        nameField.setText(satellite.getName());
        codeField.setText(satellite.getCode());
        internationalDesignatorField.setText(satellite.getInternationalDesignator());
        purposeBox.setValue(satellite.getPurpose());
        statusBox.setValue(satellite.getStatus());
        launchDatePicker.setValue(satellite.getLaunchDate());
        operationStartDatePicker.setValue(satellite.getOperationStartDate());
        decommissionDatePicker.setValue(satellite.getDecommissionDate());
        descriptionArea.setText(satellite.getDescription());
        notesArea.setText(satellite.getNotes());

        selectSeriesById(satellite.getSatelliteSeriesId());
        selectOrganizationById(operatorBox, satellite.getOperatorOrganizationId());
        selectOrganizationById(ownerBox, satellite.getOwnerOrganizationId());
        selectOrganizationById(manufacturerBox, satellite.getManufacturerOrganizationId());

        updatePhotoPreview();
    }

    private void updatePhotoPreview() {
        var image = SatelliteImageUtil.toFxImage(satellite.getPhoto());
        photoPreview.setImage(image);

        if (image == null) {
            photoInfoLabel.setText("Фотография не выбрана");
        } else {
            photoInfoLabel.setText("Фотография загружена");
        }
    }

    private void selectSeriesById(Integer seriesId) {
        if (seriesId == null) {
            return;
        }

        for (SatelliteSeries series : seriesBox.getItems()) {
            if (series.getId() != null && series.getId().equals(seriesId)) {
                seriesBox.setValue(series);
                return;
            }
        }
    }

    private void selectOrganizationById(ComboBox<Organization> comboBox, Integer organizationId) {
        if (organizationId == null) {
            return;
        }

        for (Organization organization : comboBox.getItems()) {
            if (organization.getId() != null && organization.getId().equals(organizationId)) {
                comboBox.setValue(organization);
                return;
            }
        }
    }

    private Satellite buildSatelliteFromForm() {
        SatelliteSeries selectedSeries = seriesBox.getValue();
        Organization operator = operatorBox.getValue();
        Organization owner = ownerBox.getValue();
        Organization manufacturer = manufacturerBox.getValue();

        satellite.setName(trimToNull(nameField.getText()));
        satellite.setCode(trimToNull(codeField.getText()));
        satellite.setInternationalDesignator(trimToNull(internationalDesignatorField.getText()));
        satellite.setSatelliteSeriesId(selectedSeries != null ? selectedSeries.getId() : null);
        satellite.setOperatorOrganizationId(operator != null ? operator.getId() : null);
        satellite.setOwnerOrganizationId(owner != null ? owner.getId() : null);
        satellite.setManufacturerOrganizationId(manufacturer != null ? manufacturer.getId() : null);
        satellite.setPurpose(purposeBox.getValue());
        satellite.setStatus(statusBox.getValue());
        satellite.setLaunchDate(launchDatePicker.getValue());
        satellite.setOperationStartDate(operationStartDatePicker.getValue());
        satellite.setDecommissionDate(decommissionDatePicker.getValue());
        satellite.setDescription(trimToNull(descriptionArea.getText()));
        satellite.setNotes(trimToNull(notesArea.getText()));
        return satellite;
    }

    private Map<String, String> satellitePurposeMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("meteorology", "Метеорология");
        map.put("hydrology", "Гидрология");
        map.put("remote_sensing", "Дистанционное зондирование");
        map.put("climate_monitoring", "Климатический мониторинг");
        map.put("ocean_monitoring", "Мониторинг океана");
        map.put("ice_monitoring", "Мониторинг ледовой обстановки");
        map.put("environment_monitoring", "Экологический мониторинг");
        map.put("multi_purpose", "Многоцелевой");
        map.put("other", "Другое");
        return map;
    }

    private Map<String, String> satelliteStatusMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("planned", "Планируется");
        map.put("active", "Активен");
        map.put("reserve", "Резервный");
        map.put("inactive", "Неактивен");
        map.put("lost", "Потерян");
        map.put("retired", "Выведен из эксплуатации");
        return map;
    }

    private String findKeyByValue(String value, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}