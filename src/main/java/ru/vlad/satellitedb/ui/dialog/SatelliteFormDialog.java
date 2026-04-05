package ru.vlad.satellitedb.ui.dialog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.SatelliteSeriesService;
import ru.vlad.satellitedb.ui.UiTextUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SatelliteFormDialog {

    private final Dialog<Satellite> dialog = new Dialog<>();

    private final TextField nameField = new TextField();
    private final TextField codeField = new TextField();
    private final TextField internationalDesignatorField = new TextField();
    private final ComboBox<SatelliteSeries> seriesBox = new ComboBox<>();
    private final ComboBox<String> purposeBox = new ComboBox<>();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker launchDatePicker = new DatePicker();
    private final DatePicker operationStartDatePicker = new DatePicker();
    private final DatePicker decommissionDatePicker = new DatePicker();
    private final TextArea descriptionArea = new TextArea();
    private final TextArea notesArea = new TextArea();

    private final SatelliteSeriesService seriesService = new SatelliteSeriesService();
    private final Satellite satellite;

    public SatelliteFormDialog(Satellite satellite) {
        this.satellite = satellite != null ? satellite : new Satellite();

        dialog.setTitle(satellite == null ? "Добавление спутника" : "Редактирование спутника");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillChoices();
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

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        descriptionArea.setPrefRowCount(3);
        notesArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        notesArea.setWrapText(true);

        int row = 0;

        grid.add(new Label("Название:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Код:"), 0, row);
        grid.add(codeField, 1, row++);

        grid.add(new Label("Международное обозначение:"), 0, row);
        grid.add(internationalDesignatorField, 1, row++);

        grid.add(new Label("Серия:"), 0, row);
        grid.add(seriesBox, 1, row++);

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
        grid.add(notesArea, 1, row);

        return grid;
    }

    private void fillChoices() {
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

    private Satellite buildSatelliteFromForm() {
        SatelliteSeries selectedSeries = seriesBox.getValue();

        satellite.setName(trimToNull(nameField.getText()));
        satellite.setCode(trimToNull(codeField.getText()));
        satellite.setInternationalDesignator(trimToNull(internationalDesignatorField.getText()));
        satellite.setSatelliteSeriesId(selectedSeries != null ? selectedSeries.getId() : null);
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