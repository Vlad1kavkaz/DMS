package ru.vlad.satellitedb.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.Satellite;
import ru.vlad.satellitedb.util.SatelliteImageUtil;
import ru.vlad.satellitedb.util.UiTextUtil;

public class SatelliteInfoPane extends VBox {

    private static final double BASE_WIDTH = 340.0;
    private static final double BASE_HEIGHT = 700.0;
    private static final double MIN_SCALE = 0.72;

    private final Scale responsiveScale = new Scale(1.0, 1.0, 0.0, 0.0);

    private final ImageView photoView = new ImageView();
    private final Label photoPlaceholderLabel = new Label("Фотография отсутствует");
    private final VBox photoBox = new VBox(8);

    private final Label nameLabel = new Label("Название: -");
    private final Label codeLabel = new Label("Код: -");
    private final Label seriesLabel = new Label("Серия: -");
    private final Label operatorLabel = new Label("Оператор: -");
    private final Label ownerLabel = new Label("Владелец: -");
    private final Label manufacturerLabel = new Label("Производитель: -");
    private final Label statusLabel = new Label("Статус: -");
    private final Label purposeLabel = new Label("Назначение: -");
    private final Label orbitTypeLabel = new Label("Тип орбиты: -");
    private final Label launchDateLabel = new Label("Дата запуска: -");
    private final Label payloadsLabel = new Label("Полезные нагрузки: -");

    private final Button editSatelliteButton = new Button("Редактировать спутник");
    private final Button managePayloadsButton = new Button("Управлять нагрузками");
    private final Button editOrbitButton = new Button("Редактировать орбиту");
    private final Button deleteOrbitButton = new Button("Удалить орбиту");

    public SatelliteInfoPane() {
        setSpacing(10);
        setPadding(new Insets(0, 16, 16, 16));
        setPrefWidth(BASE_WIDTH);
        setMinWidth(BASE_WIDTH);
        setMaxWidth(BASE_WIDTH);
        setPickOnBounds(false);
        getTransforms().add(responsiveScale);
        widthProperty().addListener((obs, oldValue, newValue) -> updateResponsiveScale());
        heightProperty().addListener((obs, oldValue, newValue) -> updateResponsiveScale());

        Label titleLabel = new Label("Информация о спутнике");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        photoView.setFitWidth(300);
        photoView.setFitHeight(180);
        photoView.setPreserveRatio(true);
        photoView.setSmooth(true);

        photoPlaceholderLabel.setStyle("-fx-text-fill: #666666;");

        nameLabel.setWrapText(true);
        codeLabel.setWrapText(true);
        seriesLabel.setWrapText(true);
        operatorLabel.setWrapText(true);
        ownerLabel.setWrapText(true);
        manufacturerLabel.setWrapText(true);
        statusLabel.setWrapText(true);
        purposeLabel.setWrapText(true);
        orbitTypeLabel.setWrapText(true);
        launchDateLabel.setWrapText(true);
        payloadsLabel.setWrapText(true);

        nameLabel.setMaxWidth(Double.MAX_VALUE);
        codeLabel.setMaxWidth(Double.MAX_VALUE);
        seriesLabel.setMaxWidth(Double.MAX_VALUE);
        operatorLabel.setMaxWidth(Double.MAX_VALUE);
        ownerLabel.setMaxWidth(Double.MAX_VALUE);
        manufacturerLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        purposeLabel.setMaxWidth(Double.MAX_VALUE);
        orbitTypeLabel.setMaxWidth(Double.MAX_VALUE);
        launchDateLabel.setMaxWidth(Double.MAX_VALUE);
        payloadsLabel.setMaxWidth(Double.MAX_VALUE);

        photoBox.getChildren().setAll(photoView, photoPlaceholderLabel);

        Pane buttonBox = new Pane();
        buttonBox.setManaged(false);
        buttonBox.setPickOnBounds(false);
        buttonBox.setPrefHeight(46);
        buttonBox.setMinHeight(46);
        buttonBox.setMaxHeight(46);
        buttonBox.setPrefWidth(1180);
        buttonBox.setMinWidth(1180);
        buttonBox.setMaxWidth(1180);

        editSatelliteButton.setPrefWidth(165);
        editSatelliteButton.setMinWidth(165);
        editSatelliteButton.setMaxWidth(165);

        managePayloadsButton.setPrefWidth(170);
        managePayloadsButton.setMinWidth(170);
        managePayloadsButton.setMaxWidth(170);

        editOrbitButton.setPrefWidth(155);
        editOrbitButton.setMinWidth(155);
        editOrbitButton.setMaxWidth(155);

        deleteOrbitButton.setPrefWidth(125);
        deleteOrbitButton.setMinWidth(125);
        deleteOrbitButton.setMaxWidth(125);

        editSatelliteButton.relocate(0, 0);
        managePayloadsButton.relocate(176, 0);
        editOrbitButton.relocate(362, 0);
        deleteOrbitButton.relocate(527, 0);

        buttonBox.getChildren().addAll(
                editSatelliteButton,
                managePayloadsButton,
                editOrbitButton,
                deleteOrbitButton
        );

        buttonBox.setLayoutX(-275);
        buttonBox.layoutYProperty().bind(heightProperty().subtract(10));

        disableActions(true);


        getChildren().addAll(
                titleLabel,
                photoBox,
                nameLabel,
                codeLabel,
                seriesLabel,
                operatorLabel,
                ownerLabel,
                manufacturerLabel,
                statusLabel,
                purposeLabel,
                orbitTypeLabel,
                launchDateLabel,
                payloadsLabel,
                buttonBox
        );

        clearInfo();
    }

    private void updateResponsiveScale() {
        double availableWidth = getWidth();
        double availableHeight = getHeight();

        if (availableWidth <= 0 || availableHeight <= 0) {
            return;
        }

        double scale = Math.min(availableWidth / BASE_WIDTH, availableHeight / BASE_HEIGHT);
        scale = Math.min(scale, 1.0);
        scale = Math.max(scale, MIN_SCALE);

        responsiveScale.setX(scale);
        responsiveScale.setY(scale);
    }

    public void setOnEditSatellite(Runnable action) {
        editSatelliteButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnManagePayloads(Runnable action) {
        managePayloadsButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnEditOrbit(Runnable action) {
        editOrbitButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnDeleteOrbit(Runnable action) {
        deleteOrbitButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void clearInfo() {
        photoView.setImage(null);
        photoView.setVisible(false);
        photoView.setManaged(false);
        photoPlaceholderLabel.setVisible(false);
        photoPlaceholderLabel.setManaged(false);
        photoBox.setVisible(false);
        photoBox.setManaged(false);

        nameLabel.setText("Название: -");
        codeLabel.setText("Код: -");
        seriesLabel.setText("Серия: -");
        operatorLabel.setText("Оператор: -");
        ownerLabel.setText("Владелец: -");
        manufacturerLabel.setText("Производитель: -");
        statusLabel.setText("Статус: -");
        purposeLabel.setText("Назначение: -");
        orbitTypeLabel.setText("Тип орбиты: -");
        launchDateLabel.setText("Дата запуска: -");
        payloadsLabel.setText("Полезные нагрузки: -");

        disableActions(true);
    }

    public void showInfo(SatelliteDetailsDto dto) {
        Satellite s = dto.getSatellite();

        updatePhoto(s.getPhoto());

        nameLabel.setText("Название: " + safe(s.getName()));
        codeLabel.setText("Код: " + safe(s.getCode()));
        seriesLabel.setText("Серия: " + safe(dto.getSeriesName()));
        operatorLabel.setText("Оператор: " + safe(dto.getOperatorName()));
        ownerLabel.setText("Владелец: " + safe(dto.getOwnerName()));
        manufacturerLabel.setText("Производитель: " + safe(dto.getManufacturerName()));
        statusLabel.setText("Статус: " + safe(s.getStatus()));
        purposeLabel.setText("Назначение: " + safe(s.getPurpose()));
        orbitTypeLabel.setText("Тип орбиты: " + UiTextUtil.orbitType(dto.getOrbitType()));
        launchDateLabel.setText("Дата запуска: " + (s.getLaunchDate() != null ? s.getLaunchDate().toString() : "-"));

        if (dto.getPayloads() == null || dto.getPayloads().isEmpty()) {
            payloadsLabel.setText("Полезные нагрузки: -");
        } else {
            StringBuilder sb = new StringBuilder("Полезные нагрузки:\n");
            for (Payload p : dto.getPayloads()) {
                sb.append("• ").append(safe(p.getName()));
                if (p.getType() != null && !p.getType().isBlank()) {
                    sb.append(" (").append(p.getType()).append(")");
                }
                sb.append("\n");
            }
            payloadsLabel.setText(sb.toString().trim());
        }

        disableActions(false);
    }

    private void updatePhoto(byte[] photoBytes) {
        Image image = SatelliteImageUtil.toFxImage(photoBytes);
        boolean hasPhoto = image != null;

        photoView.setImage(image);
        photoView.setVisible(hasPhoto);
        photoView.setManaged(hasPhoto);

        photoPlaceholderLabel.setVisible(false);
        photoPlaceholderLabel.setManaged(false);

        photoBox.setVisible(hasPhoto);
        photoBox.setManaged(hasPhoto);
    }

    private void disableActions(boolean disabled) {
        editSatelliteButton.setDisable(disabled);
        managePayloadsButton.setDisable(disabled);
        editOrbitButton.setDisable(disabled);
        deleteOrbitButton.setDisable(disabled);
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }
}