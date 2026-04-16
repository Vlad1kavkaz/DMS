package ru.vlad.satellitedb.ui.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import ru.vlad.satellitedb.model.CrossSatellitePayload;
import ru.vlad.satellitedb.model.Organization;
import ru.vlad.satellitedb.model.Orbit;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.model.SatelliteSeries;
import ru.vlad.satellitedb.service.CrossSatellitePayloadService;
import ru.vlad.satellitedb.service.OrganizationService;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrbitMapView extends BorderPane {

    private static final Color ORBIT_COLOR = Color.web("#8f8f8f");
    private static final Color GEO_MARKER_COLOR = Color.DARKORANGE;
    private static final Color HEO_MARKER_COLOR = Color.MEDIUMPURPLE;
    private static final Color POLAR_MARKER_COLOR = Color.FORESTGREEN;
    private static final double EARTH_VERTICAL_SHIFT = -45;
    private static final double MAP_VERTICAL_SHIFT = 100;

    private final SatelliteService satelliteService = new SatelliteService();
    private final OrbitService orbitService = new OrbitService();
    private final OrbitUiService orbitUiService = new OrbitUiService();
    private final CrossSatellitePayloadService linkService = new CrossSatellitePayloadService();
    private final PayloadService payloadService = new PayloadService();
    private final SatelliteSeriesService seriesService = new SatelliteSeriesService();
    private final OrganizationService organizationService = new OrganizationService();

    private final Pane mapPane = new Pane();
    private final SatelliteInfoPane infoPane = new SatelliteInfoPane();

    private double centerX;
    private double centerY;
    private double earthRadius;

    private Circle selectedMarker;
    private Orbit selectedOrbit;
    private Satellite selectedSatellite;
    private final List<double[]> placedMarkerCenters = new ArrayList<>();
    private final List<double[]> placedLabelBounds = new ArrayList<>();

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

        mapPane.setScaleX(0.8);
        mapPane.setScaleY(0.8);
        mapPane.setTranslateX(-90);
        mapPane.setTranslateY(-55);

        mapPane.setPrefSize(940, 700);
        mapPane.setMinSize(820, 620);
        mapPane.setStyle("-fx-background-color: #f4f4f4;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mapPane.widthProperty());
        clip.heightProperty().bind(mapPane.heightProperty());
        mapPane.setClip(clip);

        infoPane.setPrefWidth(250);
        infoPane.setMinWidth(250);
        infoPane.setMaxWidth(250);

        infoPane.setOnEditSatellite(this::onEditSatellite);
        infoPane.setOnManagePayloads(this::onManagePayloads);
        infoPane.setOnEditOrbit(this::onEditOrbit);
        infoPane.setOnDeleteOrbit(this::onDeleteOrbit);

        setTop(topBox);
        setCenter(mapPane);
        setRight(infoPane);
        BorderPane.setAlignment(infoPane, Pos.TOP_LEFT);
        BorderPane.setMargin(infoPane, new Insets(0, 0, 0, 0));
        infoPane.setTranslateX(-120);
        infoPane.setTranslateY(-16);

        Platform.runLater(this::drawMap);
    }

    private void updateGeometry() {
        double width = mapPane.getWidth();
        double height = mapPane.getHeight();

        if (width <= 0) {
            width = 940;
        }
        if (height <= 0) {
            height = 700;
        }

        // Специально сдвигаем карту влево, чтобы справа было место под карточку
        centerX = width * 0.35;
        centerY = height * 0.49;
        earthRadius = Math.min(width, height) * 0.14;
    }

    private void drawMap() {
        try {
            updateGeometry();

            mapPane.getChildren().clear();
            selectedMarker = null;
            selectedOrbit = null;
            selectedSatellite = null;
            placedMarkerCenters.clear();
            placedLabelBounds.clear();
            infoPane.clearInfo();

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

            drawOrbitsAndSatellites(geoOrbits, heoOrbits, polarOrbits);
        } catch (Exception e) {
            showError("Ошибка построения карты орбит", e.getMessage());
        }
    }

    private void drawOrbitsAndSatellites(List<Orbit> geoOrbits, List<Orbit> heoOrbits, List<Orbit> polarOrbits) {
        Ellipse geoOrbit = buildGeoOrbitEllipse();
        Ellipse heoOrbit = buildHeoOrbitEllipse();
        Ellipse polarOrbit = buildPolarOrbitEllipse();

        if (!geoOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(geoOrbit, false, "geostationary"));
        }
        if (!heoOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(heoOrbit, false, "highly_elliptical"));
        }
        if (!polarOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(polarOrbit, false, "polar"));
        }

        drawEarth();

        if (!geoOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(geoOrbit, true, "geostationary"));
        }
        if (!heoOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(heoOrbit, true, "highly_elliptical"));
        }
        if (!polarOrbits.isEmpty()) {
            mapPane.getChildren().add(buildOrbitLayerPath(polarOrbit, true, "polar"));
        }

        addSatellitesToGeoOrbit(geoOrbits, geoOrbit);
        addSatellitesToHeoOrbit(heoOrbits);
        addSatellitesToPolarOrbit(polarOrbits);
    }

    private Path buildOrbitLayerPath(Ellipse ellipse, boolean visibleLayer, String orbitType) {
        Path path = new Path();
        path.setFill(Color.TRANSPARENT);
        path.setStroke(ORBIT_COLOR);
        path.setStrokeWidth(2.2);
        path.setMouseTransparent(true);

        double cx = ellipse.getCenterX();
        double cy = ellipse.getCenterY();
        double rx = ellipse.getRadiusX();
        double ry = ellipse.getRadiusY();
        double rotationDeg = ellipse.getRotate();

        boolean drawing = false;
        for (double angle = 0; angle <= 360; angle += 0.1) {
            double[] point = pointOnRotatedEllipse(cx, cy, rx, ry, angle, rotationDeg);
            boolean pointVisible = isOrbitFrontLayerPoint(orbitType, angle, point[0], point[1]);

            if (pointVisible == visibleLayer) {
                if (!drawing) {
                    path.getElements().add(new MoveTo(point[0], point[1]));
                    drawing = true;
                } else {
                    path.getElements().add(new LineTo(point[0], point[1]));
                }
            } else {
                drawing = false;
            }
        }

        return path;
    }

    private boolean isOrbitFrontLayerPoint(String orbitType, double angleDeg, double x, double y) {
        if ("highly_elliptical".equals(orbitType)) {
            // Для ВЭО передней считаем правую полуветвь орбиты,
            // чтобы она всегда шла поверх Земли, а левая уходила под Землю.
            return Math.cos(Math.toRadians(angleDeg)) >= 0;
        }

        return isHiddenBehindEarth(x, y);
    }

    private Ellipse buildGeoOrbitEllipse() {
        Ellipse ellipse = new Ellipse(centerX, centerY + MAP_VERTICAL_SHIFT, earthRadius + 185, earthRadius + 28);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(ORBIT_COLOR);
        ellipse.setStrokeWidth(2.2);
        ellipse.setMouseTransparent(true);
        return ellipse;
    }

    private Ellipse buildHeoOrbitEllipse() {
        Ellipse ellipse = new Ellipse(centerX - 45, centerY + EARTH_VERTICAL_SHIFT, earthRadius - 14, earthRadius + 105);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(ORBIT_COLOR);
        ellipse.setStrokeWidth(2.2);
        ellipse.setMouseTransparent(true);
        ellipse.setRotate(-18);
        return ellipse;
    }

    private Ellipse buildPolarOrbitEllipse() {
        Ellipse ellipse = new Ellipse(centerX - 20, centerY + EARTH_VERTICAL_SHIFT - 15 + MAP_VERTICAL_SHIFT, earthRadius - 8, earthRadius + 38);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(ORBIT_COLOR);
        ellipse.setStrokeWidth(2.2);
        ellipse.setMouseTransparent(true);
        ellipse.setRotate(16);
        return ellipse;
    }


    private void drawEarth() {
        double earthCenterY = centerY + EARTH_VERTICAL_SHIFT + MAP_VERTICAL_SHIFT;

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/earth.png")));

        ImageView earthView = new ImageView(image);
        earthView.setFitWidth(earthRadius * 2);
        earthView.setFitHeight(earthRadius * 2);
        earthView.setPreserveRatio(false);
        earthView.setLayoutX(centerX - earthRadius);
        earthView.setLayoutY(earthCenterY - earthRadius);

        // No clipping, render the image directly

        mapPane.getChildren().add(earthView);
    }

    private void addSatellitesToGeoOrbit(List<Orbit> orbits, Ellipse orbitEllipse) {
        if (orbits.isEmpty()) {
            return;
        }

        double rx = orbitEllipse.getRadiusX();
        double ry = orbitEllipse.getRadiusY();

        for (int i = 0; i < orbits.size(); i++) {
            double angleDeg = distributeAngle(i, orbits.size(), 204, 336);
            double[] point = findVisiblePointOnEllipse(centerX, centerY + MAP_VERTICAL_SHIFT, rx, ry, angleDeg);
            addSatelliteVisuals(orbits.get(i), point[0], point[1], GEO_MARKER_COLOR, i, "geostationary");
        }
    }

    private void addSatellitesToHeoOrbit(List<Orbit> orbits) {
        if (orbits.isEmpty()) {
            return;
        }

        double rx = earthRadius - 14;
        double ry = earthRadius + 105;
        double rotationDeg = -18;

        for (int i = 0; i < orbits.size(); i++) {
            double angleDeg = distributeAngle(i, orbits.size(), 318, 42);
            double[] point = findVisiblePointOnRotatedEllipse(centerX - 45, centerY + EARTH_VERTICAL_SHIFT, rx, ry, angleDeg, rotationDeg);
            addSatelliteVisuals(orbits.get(i), point[0], point[1], HEO_MARKER_COLOR, i, "highly_elliptical");
        }
    }

    private void addSatellitesToPolarOrbit(List<Orbit> orbits) {
        if (orbits.isEmpty()) {
            return;
        }

        double rx = earthRadius - 8;
        double ry = earthRadius + 38;
        double rotationDeg = 16;

        for (int i = 0; i < orbits.size(); i++) {
            double angleDeg = distributeAngle(i, orbits.size(), 328, 24);
            double[] point = findVisiblePointOnRotatedEllipse(centerX - 20, centerY + EARTH_VERTICAL_SHIFT - 15 + MAP_VERTICAL_SHIFT, rx, ry, angleDeg, rotationDeg);
            addSatelliteVisuals(orbits.get(i), point[0], point[1], POLAR_MARKER_COLOR, i, "polar");
        }
    }

    private double distributeAngle(int index, int total, double startDeg, double endDeg) {
        if (total <= 1) {
            if (startDeg <= endDeg) {
                return (startDeg + endDeg) / 2.0;
            }
            double normalizedEnd = endDeg + 360.0;
            return normalizeAngle((startDeg + normalizedEnd) / 2.0);
        }

        if (startDeg <= endDeg) {
            double step = (endDeg - startDeg) / (total - 1.0);
            return startDeg + index * step;
        }

        double normalizedEnd = endDeg + 360.0;
        double step = (normalizedEnd - startDeg) / (total - 1.0);
        return normalizeAngle(startDeg + index * step);
    }

    private double normalizeAngle(double angle) {
        double result = angle % 360.0;
        return result < 0 ? result + 360.0 : result;
    }

    private void addSatelliteVisuals(Orbit orbit, double x, double y, Color color, int index, String type) {
        double paneWidth = mapPane.getWidth() > 0 ? mapPane.getWidth() : 940;
        double paneHeight = mapPane.getHeight() > 0 ? mapPane.getHeight() : 700;

        x = Math.max(32, Math.min(x, paneWidth - 32));
        y = Math.max(32, Math.min(y, paneHeight - 32));

        Satellite satellite = satelliteService.getSatelliteById(orbit.getSatelliteId()).orElse(null);
        String satelliteName = satellite != null ? safe(satellite.getName()) : "Спутник-" + orbit.getSatelliteId();
        String displayName = abbreviateLabel(satelliteName);

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

        Label label = new Label(displayName);
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

        double[] basePos = calculateLabelPosition(x, y, label.getWidth(), label.getHeight(), index, type);
        double[] pos = resolveLabelPositionWithoutOverlap(x, y, label.getWidth(), label.getHeight(), basePos[0], basePos[1]);
        label.setLayoutX(pos[0]);
        label.setLayoutY(pos[1]);
        placedLabelBounds.add(new double[]{pos[0], pos[1], label.getWidth(), label.getHeight()});

        clickZone.setOnMouseClicked(event -> {
            selectedOrbit = orbitUiService.getFullOrbitById(orbit.getId());
            selectedSatellite = satelliteService.getSatelliteById(orbit.getSatelliteId()).orElse(null);

            highlightMarker(marker);
            if (selectedOrbit != null) {
                showSatelliteInfo(selectedOrbit);
            }
        });

        clickZone.setOnMouseEntered(event -> marker.setRadius(11));
        clickZone.setOnMouseExited(event -> {
            if (marker != selectedMarker) {
                marker.setRadius(9);
            }
        });

        mapPane.getChildren().addAll(clickZone, marker);
        placedMarkerCenters.add(new double[]{x, y});
    }

    private double[] calculateLabelPosition(double x, double y, double labelWidth, double labelHeight,
                                            int index, String type) {
        double paneWidth = mapPane.getWidth() > 0 ? mapPane.getWidth() : 940;
        double paneHeight = mapPane.getHeight() > 0 ? mapPane.getHeight() : 700;

        double labelX;
        double labelY;

        if ("geostationary".equals(type)) {
            labelX = x + 16;
            labelY = y - labelHeight / 2.0;
        } else if ("highly_elliptical".equals(type)) {
            labelX = x + 16;
            labelY = y - labelHeight / 2.0;
        } else {
            labelX = (index % 2 == 0) ? x + 14 : x - labelWidth - 14;
            labelY = y - labelHeight - 4;
        }

        if (labelX + labelWidth > paneWidth - 10) {
            labelX = x - labelWidth - 14;
        }
        if (labelX < 10) {
            labelX = x + 14;
        }

        if (labelY + labelHeight > paneHeight - 10) {
            labelY = paneHeight - labelHeight - 10;
        }
        if (labelY < 10) {
            labelY = 10;
        }

        return new double[]{labelX, labelY};
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

            String operatorName = resolveOrganizationName(satellite.getOperatorOrganizationId());
            String ownerName = resolveOrganizationName(satellite.getOwnerOrganizationId());
            String manufacturerName = resolveOrganizationName(satellite.getManufacturerOrganizationId());

            List<CrossSatellitePayload> links = linkService.getLinksBySatelliteId(satellite.getId());
            List<Payload> payloads = new ArrayList<>();

            for (CrossSatellitePayload link : links) {
                payloads.add(payloadService.requireById(link.getPayloadId()));
            }

            SatelliteDetailsDto dto = new SatelliteDetailsDto(
                    satellite,
                    seriesName,
                    operatorName,
                    ownerName,
                    manufacturerName,
                    orbit.getOrbitType(),
                    payloads
            );

            infoPane.showInfo(dto);
        } catch (Exception e) {
            showError("Ошибка загрузки информации о спутнике", e.getMessage());
        }
    }

    private String resolveOrganizationName(Integer organizationId) {
        if (organizationId == null) {
            return "-";
        }

        for (Organization organization : organizationService.getAll()) {
            if (organization.getId() != null && organization.getId().equals(organizationId)) {
                if (organization.getShortName() != null && !organization.getShortName().isBlank()) {
                    return organization.getShortName();
                }
                return organization.getName() != null ? organization.getName() : "-";
            }
        }

        return "-";
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

    private String abbreviateLabel(String value) {
        if (value == null) {
            return "-";
        }
        if (value.length() < 13) {
            return value;
        }
        return value.substring(0, 13 - 3) + "...";
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    private double[] pointOnRotatedEllipse(double cx, double cy, double rx, double ry,
                                           double angleDeg, double rotationDeg) {
        double angleRad = Math.toRadians(angleDeg);
        double rotationRad = Math.toRadians(rotationDeg);

        double localX = rx * Math.cos(angleRad);
        double localY = ry * Math.sin(angleRad);

        double rotatedX = localX * Math.cos(rotationRad) - localY * Math.sin(rotationRad);
        double rotatedY = localX * Math.sin(rotationRad) + localY * Math.cos(rotationRad);

        return new double[]{cx + rotatedX, cy + rotatedY};
    }

    private double[] findVisiblePointOnRotatedEllipse(double cx, double cy, double rx, double ry,
                                                      double preferredAngleDeg, double rotationDeg) {
        double[] directPoint = pointOnRotatedEllipse(cx, cy, rx, ry, preferredAngleDeg, rotationDeg);
        if (isHiddenBehindEarth(directPoint[0], directPoint[1])
                && isTooCloseToExistingMarker(directPoint[0], directPoint[1])) {
            return directPoint;
        }

        double[] offsets = { -18, 18, -30, 30, -45, 45, -60, 60, -78, 78, -96, 96, -120, 120, 150, -150, 180 };
        for (double offset : offsets) {
            double candidateAngle = normalizeAngle(preferredAngleDeg + offset);
            double[] candidatePoint = pointOnRotatedEllipse(cx, cy, rx, ry, candidateAngle, rotationDeg);
            if (isHiddenBehindEarth(candidatePoint[0], candidatePoint[1])
                    && isTooCloseToExistingMarker(candidatePoint[0], candidatePoint[1])) {
                return candidatePoint;
            }
        }

        return directPoint;
    }

    private boolean isHiddenBehindEarth(double x, double y) {
        double earthCenterY = centerY + EARTH_VERTICAL_SHIFT + MAP_VERTICAL_SHIFT;
        double dx = x - centerX;
        double dy = y - earthCenterY;
        double safeRadius = earthRadius + 18;

        return !(dx * dx + dy * dy <= safeRadius * safeRadius);
    }

    private double[] pointOnEllipse(double cx, double cy, double rx, double ry, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg);
        return new double[]{cx + rx * Math.cos(angleRad), cy + ry * Math.sin(angleRad)};
    }

    private double[] findVisiblePointOnEllipse(double cx, double cy, double rx, double ry, double preferredAngleDeg) {
        double[] directPoint = pointOnEllipse(cx, cy, rx, ry, preferredAngleDeg);
        if (isHiddenBehindEarth(directPoint[0], directPoint[1])
                && isTooCloseToExistingMarker(directPoint[0], directPoint[1])) {
            return directPoint;
        }

        double[] offsets = { -18, 18, -30, 30, -45, 45, -60, 60, -78, 78, -96, 96, -120, 120, 150, -150, 180 };
        for (double offset : offsets) {
            double candidateAngle = normalizeAngle(preferredAngleDeg + offset);
            double[] candidatePoint = pointOnEllipse(cx, cy, rx, ry, candidateAngle);
            if (isHiddenBehindEarth(candidatePoint[0], candidatePoint[1])
                    && isTooCloseToExistingMarker(candidatePoint[0], candidatePoint[1])) {
                return candidatePoint;
            }
        }

        return directPoint;
    }

    private boolean isTooCloseToExistingMarker(double x, double y) {
        double minDistance = 34.0;
        for (double[] center : placedMarkerCenters) {
            double dx = x - center[0];
            double dy = y - center[1];
            if (dx * dx + dy * dy < minDistance * minDistance) {
                return false;
            }
        }
        return true;
    }

    private double[] resolveLabelPositionWithoutOverlap(double markerX, double markerY,
                                                        double labelWidth, double labelHeight,
                                                        double baseX, double baseY) {
        double[][] candidates = new double[][] {
                {baseX, baseY},
                {markerX + 16, markerY - labelHeight / 2.0},
                {markerX - labelWidth - 16, markerY - labelHeight / 2.0},
                {markerX + 14, markerY - labelHeight - 8},
                {markerX - labelWidth - 14, markerY - labelHeight - 8},
                {markerX + 14, markerY + 10},
                {markerX - labelWidth - 14, markerY + 10},
                {markerX - labelWidth / 2.0, markerY - labelHeight - 14},
                {markerX - labelWidth / 2.0, markerY + 12}
        };

        for (double[] candidate : candidates) {
            double[] clamped = clampLabelPosition(candidate[0], candidate[1], labelWidth, labelHeight);
            if (!intersectsExistingLabel(clamped[0], clamped[1], labelWidth, labelHeight)
                    && !intersectsAnyMarker(clamped[0], clamped[1], labelWidth, labelHeight)) {
                return clamped;
            }
        }

        return clampLabelPosition(baseX, baseY, labelWidth, labelHeight);
    }

    private double[] clampLabelPosition(double x, double y, double labelWidth, double labelHeight) {
        double paneWidth = mapPane.getWidth() > 0 ? mapPane.getWidth() : 940;
        double paneHeight = mapPane.getHeight() > 0 ? mapPane.getHeight() : 700;

        double clampedX = Math.max(10, Math.min(x, paneWidth - labelWidth - 10));
        double clampedY = Math.max(10, Math.min(y, paneHeight - labelHeight - 10));
        return new double[]{clampedX, clampedY};
    }

    private boolean intersectsExistingLabel(double x, double y, double width, double height) {
        for (double[] bounds : placedLabelBounds) {
            if (rectanglesIntersect(x, y, width, height, bounds[0], bounds[1], bounds[2], bounds[3])) {
                return true;
            }
        }
        return false;
    }

    private boolean intersectsAnyMarker(double x, double y, double width, double height) {
        double markerBoxSize = 22.0;
        for (double[] center : placedMarkerCenters) {
            double markerX = center[0] - markerBoxSize / 2.0;
            double markerY = center[1] - markerBoxSize / 2.0;
            if (rectanglesIntersect(x, y, width, height, markerX, markerY, markerBoxSize, markerBoxSize)) {
                return true;
            }
        }
        return false;
    }

    private boolean rectanglesIntersect(double x1, double y1, double w1, double h1,
                                        double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
}