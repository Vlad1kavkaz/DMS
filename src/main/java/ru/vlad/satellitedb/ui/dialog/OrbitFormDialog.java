package ru.vlad.satellitedb.ui.dialog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.GeoOrbit;
import ru.vlad.satellitedb.model.HeoOrbit;
import ru.vlad.satellitedb.model.Orbit;
import ru.vlad.satellitedb.model.PolarOrbit;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.util.UiTextUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrbitFormDialog {

    private final Dialog<Orbit> dialog = new Dialog<>();

    private final ComboBox<Satellite> satelliteBox = new ComboBox<>();
    private final ComboBox<String> orbitTypeBox = new ComboBox<>();

    private final Label satelliteValueLabel = new Label("-");
    private final Label orbitTypeValueLabel = new Label("-");

    private final TextField inclinationField = new TextField();
    private final DatePicker validFromPicker = new DatePicker();
    private final DatePicker validToPicker = new DatePicker();
    private final CheckBox currentCheckBox = new CheckBox("Орбита является текущей");
    private final TextArea notesArea = new TextArea();

    private final VBox geoBox = new VBox(8);
    private final TextField stationLongitudeField = new TextField();
    private final TextField geoAltitudeField = new TextField();
    private final TextField geoPeriodField = new TextField();

    private final VBox heoBox = new VBox(8);
    private final TextField eccentricityField = new TextField();
    private final TextField perigeeField = new TextField();
    private final TextField apogeeField = new TextField();
    private final TextField heoPeriodField = new TextField();

    private final VBox polarBox = new VBox(8);
    private final TextField meanAltitudeField = new TextField();
    private final TextField polarPeriodField = new TextField();
    private final CheckBox sunSyncCheckBox = new CheckBox("Солнечно-синхронная орбита");

    private final Orbit orbit;
    private final boolean editMode;

    public OrbitFormDialog(Orbit orbit, List<Satellite> availableSatellites) {
        this.editMode = orbit != null;
        this.orbit = orbit != null ? copyOrbit(orbit) : null;

        dialog.setTitle(editMode ? "Редактирование орбиты" : "Добавление орбиты");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        VBox formRoot = buildForm();
        formRoot.setPrefWidth(760);
        formRoot.setMaxWidth(760);

        ScrollPane scrollPane = new ScrollPane(formRoot);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setPrefViewportWidth(780);
        scrollPane.setPrefViewportHeight(520);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(820);
        dialog.getDialogPane().setPrefHeight(640);

        initSatelliteBox(availableSatellites);
        initOrbitTypeBox();

        currentCheckBox.setTooltip(new Tooltip("Снимите галку, если орбита является исторической и больше не используется"));

        fillFields();
        updateSpecificSectionsVisibility();

        orbitTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSpecificSectionsVisibility());

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    return buildOrbitFromForm();
                } catch (Exception e) {
                    showValidationError(e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    public Optional<Orbit> showAndWaitForResult() {
        return dialog.showAndWait();
    }

    private VBox buildForm() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(16));

        GridPane commonGrid = new GridPane();
        commonGrid.setHgap(12);
        commonGrid.setVgap(12);

        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        satelliteValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        orbitTypeValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        int row = 0;

        if (editMode) {
            commonGrid.add(new Label("Спутник:"), 0, row);
            commonGrid.add(satelliteValueLabel, 1, row++);

            commonGrid.add(new Label("Тип орбиты:"), 0, row);
            commonGrid.add(orbitTypeValueLabel, 1, row++);
        } else {
            commonGrid.add(new Label("Спутник:"), 0, row);
            commonGrid.add(satelliteBox, 1, row++);

            commonGrid.add(new Label("Тип орбиты:"), 0, row);
            commonGrid.add(orbitTypeBox, 1, row++);
        }

        commonGrid.add(new Label("Наклонение (градусы):"), 0, row);
        commonGrid.add(inclinationField, 1, row++);

        commonGrid.add(new Label("Дата начала:"), 0, row);
        commonGrid.add(validFromPicker, 1, row++);

        commonGrid.add(new Label("Дата окончания:"), 0, row);
        commonGrid.add(validToPicker, 1, row++);

        commonGrid.add(currentCheckBox, 1, row++);

        commonGrid.add(new Label("Примечания:"), 0, row);
        commonGrid.add(notesArea, 1, row);

        geoBox.getChildren().addAll(
                sectionTitle("Параметры геостационарной орбиты"),
                formRow("Долгота стояния (градусы):", stationLongitudeField),
                formRow("Высота (км):", geoAltitudeField),
                formRow("Период обращения (минуты):", geoPeriodField)
        );

        heoBox.getChildren().addAll(
                sectionTitle("Параметры высокоэллиптической орбиты"),
                formRow("Эксцентриситет:", eccentricityField),
                formRow("Перигей (км):", perigeeField),
                formRow("Апогей (км):", apogeeField),
                formRow("Период обращения (минуты):", heoPeriodField)
        );

        polarBox.getChildren().addAll(
                sectionTitle("Параметры полярной орбиты"),
                formRow("Средняя высота (км):", meanAltitudeField),
                formRow("Период обращения (минуты):", polarPeriodField),
                sunSyncCheckBox
        );

        root.getChildren().addAll(commonGrid, geoBox, heoBox, polarBox);
        return root;
    }

    private void initSatelliteBox(List<Satellite> satellites) {
        satelliteBox.setItems(FXCollections.observableArrayList(satellites));
        satelliteBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Satellite satellite) {
                if (satellite == null) {
                    return "";
                }
                String name = satellite.getName() != null ? satellite.getName() : "-";
                String code = satellite.getCode() != null ? satellite.getCode() : "-";
                return name + " [" + code + "]";
            }

            @Override
            public Satellite fromString(String string) {
                return null;
            }
        });
    }

    private void initOrbitTypeBox() {
        orbitTypeBox.getItems().addAll(
                "geostationary",
                "highly_elliptical",
                "polar"
        );
        orbitTypeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return UiTextUtil.orbitType(value);
            }

            @Override
            public String fromString(String string) {
                return findKeyByValue(string, orbitTypeMap());
            }
        });
    }

    private void fillFields() {
        if (!editMode || orbit == null) {
            currentCheckBox.setSelected(true);
            return;
        }

        selectSatelliteById(orbit.getSatelliteId());

        Satellite selectedSatellite = satelliteBox.getValue();
        if (selectedSatellite != null) {
            String satName = selectedSatellite.getName() != null ? selectedSatellite.getName() : "-";
            String satCode = selectedSatellite.getCode() != null ? selectedSatellite.getCode() : "-";
            satelliteValueLabel.setText(satName + " [" + satCode + "]");
        } else {
            satelliteValueLabel.setText("Спутник не найден");
        }

        orbitTypeValueLabel.setText(orbit.getOrbitType() != null ? UiTextUtil.orbitType(orbit.getOrbitType()) : "-");
        orbitTypeBox.setValue(orbit.getOrbitType());

        inclinationField.setText(toText(orbit.getInclinationDeg()));
        validFromPicker.setValue(orbit.getValidFrom());
        validToPicker.setValue(orbit.getValidTo());
        currentCheckBox.setSelected(Boolean.TRUE.equals(orbit.getCurrent()));
        notesArea.setText(orbit.getNotes());

        if (orbit instanceof GeoOrbit geoOrbit) {
            stationLongitudeField.setText(toText(geoOrbit.getStationLongitudeDeg()));
            geoAltitudeField.setText(toText(geoOrbit.getNominalAltitudeKm()));
            geoPeriodField.setText(toText(geoOrbit.getOrbitalPeriodMin()));
        } else if (orbit instanceof HeoOrbit heoOrbit) {
            eccentricityField.setText(toText(heoOrbit.getEccentricity()));
            perigeeField.setText(toText(heoOrbit.getPerigeeAltitudeKm()));
            apogeeField.setText(toText(heoOrbit.getApogeeAltitudeKm()));
            heoPeriodField.setText(toText(heoOrbit.getOrbitalPeriodMin()));
        } else if (orbit instanceof PolarOrbit polarOrbit) {
            meanAltitudeField.setText(toText(polarOrbit.getMeanAltitudeKm()));
            polarPeriodField.setText(toText(polarOrbit.getOrbitalPeriodMin()));
            sunSyncCheckBox.setSelected(Boolean.TRUE.equals(polarOrbit.getSunSynchronous()));
        }
    }

    private void selectSatelliteById(Integer satelliteId) {
        if (satelliteId == null) {
            return;
        }

        for (Satellite satellite : satelliteBox.getItems()) {
            if (satellite.getId() != null && satellite.getId().equals(satelliteId)) {
                satelliteBox.setValue(satellite);
                return;
            }
        }
    }

    private void updateSpecificSectionsVisibility() {
        String type = editMode && orbit != null ? orbit.getOrbitType() : orbitTypeBox.getValue();

        boolean geoVisible = "geostationary".equals(type);
        boolean heoVisible = "highly_elliptical".equals(type);
        boolean polarVisible = "polar".equals(type);

        geoBox.setVisible(geoVisible);
        geoBox.setManaged(geoVisible);

        heoBox.setVisible(heoVisible);
        heoBox.setManaged(heoVisible);

        polarBox.setVisible(polarVisible);
        polarBox.setManaged(polarVisible);
    }

    private Orbit buildOrbitFromForm() {
        Satellite satellite = satelliteBox.getValue();
        String orbitType = editMode && orbit != null ? orbit.getOrbitType() : orbitTypeBox.getValue();

        if (satellite == null) {
            throw new IllegalArgumentException("Выберите спутник");
        }
        if (orbitType == null || orbitType.isBlank()) {
            throw new IllegalArgumentException("Выберите тип орбиты");
        }

        Orbit result = switch (orbitType) {
            case "geostationary" -> buildGeoOrbit();
            case "highly_elliptical" -> buildHeoOrbit();
            case "polar" -> buildPolarOrbit();
            default -> throw new IllegalArgumentException("Некорректный тип орбиты");
        };

        if (editMode && orbit != null) {
            result.setId(orbit.getId());
        }

        result.setSatelliteId(satellite.getId());
        result.setOrbitType(orbitType);
        result.setInclinationDeg(parseNullableDouble(inclinationField.getText()));
        result.setValidFrom(validFromPicker.getValue());
        result.setValidTo(validToPicker.getValue());
        result.setCurrent(currentCheckBox.isSelected());
        result.setNotes(trimToNull(notesArea.getText()));

        return result;
    }

    private GeoOrbit buildGeoOrbit() {
        GeoOrbit orbit = new GeoOrbit();
        orbit.setStationLongitudeDeg(parseRequiredDouble(stationLongitudeField.getText(), "Введите долготу стояния"));
        orbit.setNominalAltitudeKm(parseNullableDouble(geoAltitudeField.getText()));
        orbit.setOrbitalPeriodMin(parseNullableDouble(geoPeriodField.getText()));
        return orbit;
    }

    private HeoOrbit buildHeoOrbit() {
        HeoOrbit orbit = new HeoOrbit();
        orbit.setEccentricity(parseRequiredDouble(eccentricityField.getText(), "Введите эксцентриситет"));
        orbit.setPerigeeAltitudeKm(parseRequiredDouble(perigeeField.getText(), "Введите перигей"));
        orbit.setApogeeAltitudeKm(parseRequiredDouble(apogeeField.getText(), "Введите апогей"));
        orbit.setOrbitalPeriodMin(parseNullableDouble(heoPeriodField.getText()));
        return orbit;
    }

    private PolarOrbit buildPolarOrbit() {
        PolarOrbit orbit = new PolarOrbit();
        orbit.setMeanAltitudeKm(parseRequiredDouble(meanAltitudeField.getText(), "Введите среднюю высоту"));
        orbit.setOrbitalPeriodMin(parseNullableDouble(polarPeriodField.getText()));
        orbit.setSunSynchronous(sunSyncCheckBox.isSelected());
        return orbit;
    }

    private GridPane formRow(String labelText, Control control) {
        GridPane row = new GridPane();
        row.setHgap(10);
        row.add(new Label(labelText), 0, 0);
        row.add(control, 1, 0);
        return row;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return label;
    }

    private Map<String, String> orbitTypeMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("geostationary", "Геостационарная");
        map.put("highly_elliptical", "Высокоэллиптическая");
        map.put("polar", "Полярная");
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

    private String toText(Double value) {
        return value != null ? String.valueOf(value) : "";
    }

    private Double parseRequiredDouble(String value, String errorMessage) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число: " + value);
        }
    }

    private Double parseNullableDouble(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число: " + value);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText("Не удалось сохранить орбиту");
        alert.setContentText(message != null ? message : "Некорректные данные");
        alert.showAndWait();
    }

    private Orbit copyOrbit(Orbit source) {
        if (source instanceof GeoOrbit geoOrbit) {
            GeoOrbit copy = new GeoOrbit();
            copyBaseOrbit(source, copy);
            copy.setStationLongitudeDeg(geoOrbit.getStationLongitudeDeg());
            copy.setNominalAltitudeKm(geoOrbit.getNominalAltitudeKm());
            copy.setOrbitalPeriodMin(geoOrbit.getOrbitalPeriodMin());
            return copy;
        }

        if (source instanceof HeoOrbit heoOrbit) {
            HeoOrbit copy = new HeoOrbit();
            copyBaseOrbit(source, copy);
            copy.setEccentricity(heoOrbit.getEccentricity());
            copy.setPerigeeAltitudeKm(heoOrbit.getPerigeeAltitudeKm());
            copy.setApogeeAltitudeKm(heoOrbit.getApogeeAltitudeKm());
            copy.setOrbitalPeriodMin(heoOrbit.getOrbitalPeriodMin());
            return copy;
        }

        if (source instanceof PolarOrbit polarOrbit) {
            PolarOrbit copy = new PolarOrbit();
            copyBaseOrbit(source, copy);
            copy.setMeanAltitudeKm(polarOrbit.getMeanAltitudeKm());
            copy.setOrbitalPeriodMin(polarOrbit.getOrbitalPeriodMin());
            copy.setSunSynchronous(polarOrbit.getSunSynchronous());
            return copy;
        }

        Orbit copy = new Orbit();
        copyBaseOrbit(source, copy);
        return copy;
    }

    private void copyBaseOrbit(Orbit source, Orbit target) {
        target.setId(source.getId());
        target.setSatelliteId(source.getSatelliteId());
        target.setOrbitType(source.getOrbitType());
        target.setInclinationDeg(source.getInclinationDeg());
        target.setValidFrom(source.getValidFrom());
        target.setValidTo(source.getValidTo());
        target.setCurrent(source.getCurrent());
        target.setNotes(source.getNotes());
    }
}