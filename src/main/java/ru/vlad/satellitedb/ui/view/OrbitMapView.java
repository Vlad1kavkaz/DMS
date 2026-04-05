package ru.vlad.satellitedb.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import ru.vlad.satellitedb.model.CrossSatellitePayload;
import ru.vlad.satellitedb.model.GeoOrbit;
import ru.vlad.satellitedb.model.HeoOrbit;
import ru.vlad.satellitedb.model.Orbit;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.PolarOrbit;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.CrossSatellitePayloadService;
import ru.vlad.satellitedb.service.OrbitService;
import ru.vlad.satellitedb.service.OrbitUiService;
import ru.vlad.satellitedb.service.PayloadService;
import ru.vlad.satellitedb.service.SatelliteSeriesService;
import ru.vlad.satellitedb.service.SatelliteService;
import ru.vlad.satellitedb.ui.SatelliteDetailsDto;
import ru.vlad.satellitedb.ui.SatelliteInfoPane;
import ru.vlad.satellitedb.ui.dialog.OrbitFormDialog;
import ru.vlad.satellitedb.ui.dialog.SatelliteFormDialog;
import ru.vlad.satellitedb.ui.dialog.SatellitePayloadLinkDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrbitMapView extends BorderPane {

    private final SatelliteService satelliteService = new SatelliteService();
    private final OrbitService orbitService = new OrbitService();
    private final OrbitUiService orbitUiService = new OrbitUiService();
    private final CrossSatellitePayloadService linkService = new CrossSatellitePayloadService();
    private final PayloadService payloadService = new PayloadService();

    private final Pane mapPane = new Pane();
    private final SatelliteInfoPane infoPane = new SatelliteInfoPane();

    private final Map<Integer, Shape> orbitShapes = new HashMap<>();
    private Shape selectedOrbitShape;

    private final SatelliteSeriesService seriesService = new SatelliteSeriesService();

    private double centerX;
    private double centerY;
    private double earthRadius;

    private Circle selectedMarker;
    private Orbit selectedOrbit;
    private Satellite selectedSatellite;

    public OrbitMapView() {
        setPadding(new Insets(16));

        Label title = new Label("Орбитальная карта");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button refreshButton = new Button("Обновить карту");
        Button addOrbitButton = new Button("Добавить орбиту");

        refreshButton.setOnAction(e -> drawMap());
        addOrbitButton.setOnAction(e -> onAddOrbit());

        HBox toolbar = new HBox(10, refreshButton, addOrbitButton);
        toolbar.setPadding(new Insets(8, 0, 12, 0));

        VBox topBox = new VBox(title, toolbar);

        mapPane.setPrefSize(900, 680);
        mapPane.setStyle("-fx-background-color: #f4f4f4;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mapPane.widthProperty());
        clip.heightProperty().bind(mapPane.heightProperty());
        mapPane.setClip(clip);

        infoPane.setOnEditSatellite(this::onEditSatellite);
        infoPane.setOnManagePayloads(this::onManagePayloads);
        infoPane.setOnEditOrbit(this::onEditOrbit);
        infoPane.setOnDeleteOrbit(this::onDeleteOrbit);

        setTop(topBox);
        setCenter(mapPane);
        setRight(infoPane);

        mapPane.widthProperty().addListener((obs, oldVal, newVal) -> drawMap());
        mapPane.heightProperty().addListener((obs, oldVal, newVal) -> drawMap());

        drawMap();
    }

    private void updateGeometry() {
        double width = mapPane.getWidth();
        double height = mapPane.getHeight();

        if (width <= 0) {
            width = 900;
        }
        if (height <= 0) {
            height = 680;
        }

        centerX = width * 0.40;
        centerY = height * 0.52;
        earthRadius = Math.min(width, height) * 0.12;
    }

    private void drawMap() {
        try {
            updateGeometry();

            mapPane.getChildren().clear();
            infoPane.clearInfo();
            selectedMarker = null;
            selectedOrbit = null;
            selectedSatellite = null;

            drawEarth();

            List<Orbit> allOrbits = orbitService.getAllOrbits().stream()
                    .sorted(Comparator.comparing(o -> o.getId() == null ? Integer.MAX_VALUE : o.getId()))
                    .toList();

            List<Orbit> geoOrbits = allOrbits.stream()
                    .filter(o -> "geostationary".equals(o.getOrbitType()))
                    .toList();

            List<Orbit> heoOrbits = allOrbits.stream()
                    .filter(o -> "highly_elliptical".equals(o.getOrbitType()))
                    .toList();

            List<Orbit> polarOrbits = allOrbits.stream()
                    .filter(o -> "polar".equals(o.getOrbitType()))
                    .toList();

            for (int i = 0; i < geoOrbits.size(); i++) {
                drawGeoOrbit(geoOrbits.get(i), i, geoOrbits.size());
            }

            for (int i = 0; i < heoOrbits.size(); i++) {
                drawHeoOrbit(heoOrbits.get(i), i, heoOrbits.size());
            }

            for (int i = 0; i < polarOrbits.size(); i++) {
                drawPolarOrbit(polarOrbits.get(i), i, polarOrbits.size());
            }
        } catch (Exception e) {
            showError("Ошибка построения карты орбит", e.getMessage());
        }
    }

    private void drawEarth() {
        Circle ocean = new Circle(centerX, centerY, earthRadius);
        ocean.setFill(Color.web("#1e88d9"));
        ocean.setStroke(Color.web("#0d4f7c"));
        ocean.setStrokeWidth(2.5);

        Circle glow = new Circle(centerX - 18, centerY - 18, earthRadius * 0.78);
        glow.setFill(Color.web("#46b3ff"));
        glow.setOpacity(0.18);

        Ellipse continent1 = new Ellipse(centerX - 28, centerY - 12, 22, 27);
        continent1.setFill(Color.web("#12b886"));
        continent1.setOpacity(0.85);

        Ellipse continent2 = new Ellipse(centerX + 18, centerY + 24, 19, 22);
        continent2.setFill(Color.web("#0ca678"));
        continent2.setOpacity(0.82);

        Ellipse continent3 = new Ellipse(centerX + 8, centerY - 34, 13, 15);
        continent3.setFill(Color.web("#38d9a9"));
        continent3.setOpacity(0.8);

        Ellipse cloud1 = new Ellipse(centerX - 8, centerY + 36, 26, 10);
        cloud1.setFill(Color.WHITE);
        cloud1.setOpacity(0.18);

        Ellipse cloud2 = new Ellipse(centerX + 24, centerY - 6, 18, 8);
        cloud2.setFill(Color.WHITE);
        cloud2.setOpacity(0.16);

        mapPane.getChildren().addAll(glow, ocean, continent1, continent2, continent3, cloud1, cloud2);
    }

    private void drawGeoOrbit(Orbit orbit, int index, int total) {
        GeoOrbit geo = orbitService.getGeoOrbitById(orbit.getId());
        if (geo == null) {
            return;
        }

        double orbitRadius = earthRadius + 110 + index * 18.0;

        Circle orbitCircle = new Circle(centerX, centerY, orbitRadius);
        orbitCircle.setFill(Color.TRANSPARENT);
        orbitCircle.setStroke(Color.web("#8b8b8b"));
        orbitCircle.setStrokeWidth(2);
        orbitCircle.setMouseTransparent(true);

        double baseLongitude = geo.getStationLongitudeDeg() != null ? geo.getStationLongitudeDeg() : 0.0;
        double spread = total > 1 ? (index - (total - 1) / 2.0) * 10.0 : 0.0;
        double angle = Math.toRadians(normalizeLongitude(baseLongitude + spread));

        double satX = centerX + orbitRadius * Math.cos(angle);
        double satY = centerY + orbitRadius * Math.sin(angle);

        Line redLine = new Line(
                satX, satY,
                centerX + earthRadius * Math.cos(angle),
                centerY + earthRadius * Math.sin(angle)
        );
        redLine.setStroke(Color.RED);
        redLine.setStrokeWidth(1.5);
        redLine.setMouseTransparent(true);

        mapPane.getChildren().addAll(orbitCircle, redLine);
        addSatelliteVisuals(orbit, satX, satY, Color.DARKORANGE, index, "geostationary");

        orbitShapes.put(orbit.getId(), orbitCircle);
    }

    private void drawHeoOrbit(Orbit orbit, int index, int total) {
        HeoOrbit heo = orbitService.getHeoOrbitById(orbit.getId());
        if (heo == null) {
            return;
        }

        double radiusX = earthRadius + 210 + index * 14.0;
        double radiusY = earthRadius + 40 + index * 8.0;
        double ellipseCenterX = centerX + 70;
        double ellipseCenterY = centerY;

        Ellipse ellipse = new Ellipse(ellipseCenterX, ellipseCenterY, radiusX, radiusY);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(Color.web("#a0a0a0"));
        ellipse.setStrokeWidth(2);
        ellipse.setMouseTransparent(true);

        double baseAngleDeg;
        if (total == 1) {
            baseAngleDeg = 0;
        } else {
            double start = -28.0;
            double end = 28.0;
            double step = (end - start) / (total - 1);
            baseAngleDeg = start + index * step;
        }

        double angle = Math.toRadians(baseAngleDeg);

        double satX = ellipseCenterX + radiusX * Math.cos(angle);
        double satY = ellipseCenterY + radiusY * Math.sin(angle);

        mapPane.getChildren().add(ellipse);
        addSatelliteVisuals(orbit, satX, satY, Color.MEDIUMPURPLE, index, "highly_elliptical");

        orbitShapes.put(orbit.getId(), ellipse);
    }

    private void drawPolarOrbit(Orbit orbit, int index, int total) {
        PolarOrbit polar = orbitService.getPolarOrbitById(orbit.getId());
        if (polar == null) {
            return;
        }

        double radiusX = earthRadius + 35 + index * 12.0;
        double radiusY = earthRadius + 180 + index * 14.0;
        double ellipseCenterX = centerX;
        double ellipseCenterY = centerY;

        Ellipse ellipse = new Ellipse(ellipseCenterX, ellipseCenterY, radiusX, radiusY);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(Color.web("#8e8e8e"));
        ellipse.setStrokeWidth(2);
        ellipse.setMouseTransparent(true);

        double baseAngleDeg;
        if (total == 1) {
            baseAngleDeg = -90;
        } else {
            double start = -115.0;
            double end = -65.0;
            double step = (end - start) / (total - 1);
            baseAngleDeg = start + index * step;
        }

        double angle = Math.toRadians(baseAngleDeg);

        double satX = ellipseCenterX + radiusX * Math.cos(angle);
        double satY = ellipseCenterY + radiusY * Math.sin(angle);

        mapPane.getChildren().add(ellipse);
        addSatelliteVisuals(orbit, satX, satY, Color.FORESTGREEN, index, "polar");

        orbitShapes.put(orbit.getId(), ellipse);
    }

    private void addSatelliteVisuals(Orbit orbit, double x, double y, Color color, int index, String type) {
        Satellite satellite = satelliteService.getSatelliteById(orbit.getSatelliteId()).orElse(null);
        String satelliteName = satellite != null ? safe(satellite.getName()) : "Спутник-" + orbit.getSatelliteId();

        Circle clickZone = new Circle(x, y, 22);
        clickZone.setFill(Color.TRANSPARENT);
        clickZone.setStroke(Color.TRANSPARENT);
        clickZone.setCursor(Cursor.HAND);
        clickZone.setFocusTraversable(false);

        Circle marker = new Circle(x, y, 9);
        marker.setFill(color);
        marker.setStroke(Color.BLACK);
        marker.setStrokeWidth(1.2);
        marker.setMouseTransparent(true);

        Label label = new Label(satelliteName);
        label.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(255,255,255,0.88); " +
                        "-fx-padding: 2 6 2 6;"
        );
        label.setMouseTransparent(true);

        mapPane.getChildren().add(label);
        label.applyCss();
        label.autosize();

        double[] pos = calculateSimpleLabelPosition(x, y, label.getWidth(), label.getHeight(), index, type);
        label.setLayoutX(pos[0]);
        label.setLayoutY(pos[1]);

        clickZone.setOnMouseClicked(event -> {
            selectedOrbit = orbitUiService.getFullOrbitById(orbit.getId());
            selectedSatellite = satelliteService.getSatelliteById(orbit.getSatelliteId()).orElse(null);

            highlightMarker(marker);
            if (selectedOrbit != null) {
                showSatelliteInfo(selectedOrbit);
            }
            highlightOrbit(orbit);
        });

        clickZone.setOnMouseEntered(event -> marker.setRadius(11));
        clickZone.setOnMouseExited(event -> {
            if (marker != selectedMarker) {
                marker.setRadius(9);
            }
        });

        mapPane.getChildren().addAll(clickZone, marker);
    }

    private void highlightMarker(Circle marker) {
        if (selectedMarker != null) {
            selectedMarker.setStroke(Color.BLACK);
            selectedMarker.setStrokeWidth(1.2);
            selectedMarker.setRadius(9);
        }

        selectedMarker = marker;
        selectedMarker.setStroke(Color.RED);
        selectedMarker.setStrokeWidth(2.5);
        selectedMarker.setRadius(11);
    }

    private void showSatelliteInfo(Orbit orbit) {
        try {
            Satellite satellite = satelliteService.getSatelliteById(orbit.getSatelliteId())
                    .orElseThrow(() -> new IllegalArgumentException("Спутник не найден, id=" + orbit.getSatelliteId()));

            String seriesName = "-";
            if (satellite.getSatelliteSeriesId() != null) {
                for (SatelliteSeries series : seriesService.getAll()) {
                    if (series.getId() != null && series.getId().equals(satellite.getSatelliteSeriesId())) {
                        seriesName = series.getName() != null ? series.getName() : "-";
                        break;
                    }
                }
            }

            List<CrossSatellitePayload> links = linkService.getLinksBySatelliteId(satellite.getId());
            List<Payload> payloads = new ArrayList<>();

            for (CrossSatellitePayload link : links) {
                payloads.add(payloadService.requireById(link.getPayloadId()));
            }

            SatelliteDetailsDto dto = new SatelliteDetailsDto(
                    satellite,
                    seriesName,
                    orbit.getOrbitType(),
                    payloads
            );

            infoPane.showInfo(dto);
        } catch (Exception e) {
            showError("Ошибка загрузки информации о спутнике", e.getMessage());
        }
    }

    private void onAddOrbit() {
        try {
            List<Satellite> satellitesWithoutOrbit = orbitUiService.getSatellitesWithoutOrbit();
            if (satellitesWithoutOrbit.isEmpty()) {
                showWarning("Добавление орбиты", "Нет спутников без орбиты.");
                return;
            }

            OrbitFormDialog dialog = new OrbitFormDialog(null, satellitesWithoutOrbit);
            Optional<Orbit> result = dialog.showAndWaitForResult();

            result.ifPresent(orbit -> {
                try {
                    orbitUiService.createOrbit(orbit);
                    drawMap();
                } catch (Exception e) {
                    showError("Ошибка добавления орбиты", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
        }
    }

    private void onEditSatellite() {
        if (selectedSatellite == null) {
            showWarning("Редактирование спутника", "Сначала выберите спутник на карте.");
            return;
        }

        Satellite editableCopy = copySatellite(selectedSatellite);

        SatelliteFormDialog dialog = new SatelliteFormDialog(editableCopy);
        Optional<Satellite> result = dialog.showAndWaitForResult();

        result.ifPresent(satellite -> {
            try {
                satelliteService.updateSatellite(satellite);
                drawMap();
            } catch (Exception e) {
                showError("Ошибка редактирования спутника", e.getMessage());
            }
        });
    }

    private void onEditOrbit() {
        if (selectedOrbit == null || selectedSatellite == null) {
            showWarning("Редактирование орбиты", "Сначала выберите спутник на карте.");
            return;
        }

        Orbit orbitToEdit = orbitUiService.getFullOrbitById(selectedOrbit.getId());
        if (orbitToEdit == null) {
            showError("Ошибка", "Не удалось загрузить орбиту для редактирования.");
            return;
        }

        OrbitFormDialog dialog = new OrbitFormDialog(orbitToEdit, List.of(selectedSatellite));
        Optional<Orbit> result = dialog.showAndWaitForResult();

        result.ifPresent(orbit -> {
            try {
                orbitUiService.updateOrbit(orbit);
                drawMap();
            } catch (Exception e) {
                showError("Ошибка редактирования орбиты", e.getMessage());
            }
        });
    }

    private void onDeleteOrbit() {
        if (selectedOrbit == null) {
            showWarning("Удаление орбиты", "Сначала выберите спутник на карте.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить орбиту?");
        confirm.setContentText("Будет удалена орбита спутника: " +
                (selectedSatellite != null ? safe(selectedSatellite.getName()) : "неизвестный спутник"));

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                orbitUiService.deleteOrbit(selectedOrbit.getId());
                drawMap();
            } catch (Exception e) {
                showError("Ошибка удаления орбиты", e.getMessage());
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
        return copy;
    }

    private double normalizeLongitude(Double longitude) {
        if (longitude == null) {
            return 0;
        }
        return longitude - 90.0;
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

    private String safe(String value) {
        return value != null ? value : "-";
    }

    private double[] calculateSimpleLabelPosition(double x, double y, double labelWidth, double labelHeight,
                                                  int index, String type) {
        double paneWidth = mapPane.getWidth() > 0 ? mapPane.getWidth() : 900;
        double paneHeight = mapPane.getHeight() > 0 ? mapPane.getHeight() : 680;

        double labelX = x + 14;
        double labelY = y - labelHeight - 4;

        if ("polar".equals(type)) {
            labelX = (index % 2 == 0) ? x + 14 : x - labelWidth - 14;
            labelY = y - labelHeight / 2.0;
        } else if ("highly_elliptical".equals(type)) {
            labelX = x + 14;
            labelY = y - labelHeight / 2.0;
        } else if ("geostationary".equals(type)) {
            labelX = x + 14;
            labelY = y - labelHeight - 4;
        }

        if (labelX + labelWidth > paneWidth - 8) {
            labelX = x - labelWidth - 14;
        }
        if (labelX < 8) {
            labelX = x + 14;
        }

        if (labelY + labelHeight > paneHeight - 8) {
            labelY = paneHeight - labelHeight - 8;
        }
        if (labelY < 8) {
            labelY = 8;
        }

        return new double[]{labelX, labelY};
    }

    private void highlightOrbit(Orbit orbit) {
        if (selectedOrbitShape != null) {
            selectedOrbitShape.setStrokeWidth(2);
            selectedOrbitShape.setStroke(Color.web("#8b8b8b"));
        }

        Shape shape = orbitShapes.get(orbit.getId());
        if (shape != null) {
            shape.setStroke(Color.RED);
            shape.setStrokeWidth(3);
            selectedOrbitShape = shape;
        }
    }

    private void onManagePayloads() {
        if (selectedSatellite == null || selectedSatellite.getId() == null) {
            showWarning("Управление нагрузками", "Сначала выберите спутник на карте.");
            return;
        }

        SatellitePayloadLinkDialog dialog = new SatellitePayloadLinkDialog(
                selectedSatellite.getId(),
                selectedSatellite.getName()
        );
        dialog.showAndWait();

        if (selectedOrbit != null) {
            showSatelliteInfo(selectedOrbit);
        }
    }
}