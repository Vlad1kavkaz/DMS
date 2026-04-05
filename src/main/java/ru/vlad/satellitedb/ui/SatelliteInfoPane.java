package ru.vlad.satellitedb.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.Satellite;

public class SatelliteInfoPane extends VBox {

    private final Label nameLabel = new Label("Название: -");
    private final Label codeLabel = new Label("Код: -");
    private final Label seriesLabel = new Label("Серия: -");
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
        setPadding(new Insets(16));
        setPrefWidth(320);

        Label titleLabel = new Label("Информация о спутнике");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        payloadsLabel.setWrapText(true);

        disableActions(true);

        getChildren().addAll(
                titleLabel,
                nameLabel,
                codeLabel,
                seriesLabel,
                statusLabel,
                purposeLabel,
                orbitTypeLabel,
                launchDateLabel,
                payloadsLabel,
                editSatelliteButton,
                managePayloadsButton,
                editOrbitButton,
                deleteOrbitButton
        );
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
        nameLabel.setText("Название: -");
        codeLabel.setText("Код: -");
        seriesLabel.setText("Серия: -");
        statusLabel.setText("Статус: -");
        purposeLabel.setText("Назначение: -");
        orbitTypeLabel.setText("Тип орбиты: -");
        launchDateLabel.setText("Дата запуска: -");
        payloadsLabel.setText("Полезные нагрузки: -");

        disableActions(true);
    }

    public void showInfo(SatelliteDetailsDto dto) {
        Satellite s = dto.getSatellite();

        nameLabel.setText("Название: " + safe(s.getName()));
        codeLabel.setText("Код: " + safe(s.getCode()));
        seriesLabel.setText("Серия: " + safe(dto.getSeriesName()));
        statusLabel.setText("Статус: " + UiTextUtil.satelliteStatus(s.getStatus()));
        purposeLabel.setText("Назначение: " + UiTextUtil.satellitePurpose(s.getPurpose()));
        orbitTypeLabel.setText("Тип орбиты: " + UiTextUtil.orbitType(dto.getOrbitType()));
        launchDateLabel.setText("Дата запуска: " + (s.getLaunchDate() != null ? s.getLaunchDate().toString() : "-"));

        if (dto.getPayloads() == null || dto.getPayloads().isEmpty()) {
            payloadsLabel.setText("Полезные нагрузки: -");
        } else {
            StringBuilder sb = new StringBuilder("Полезные нагрузки:\n");
            for (Payload p : dto.getPayloads()) {
                sb.append("• ").append(safe(p.getName()));
                if (p.getType() != null && !p.getType().isBlank()) {
                    sb.append(" (").append(UiTextUtil.payloadType(p.getType())).append(")");
                }
                sb.append("\n");
            }
            payloadsLabel.setText(sb.toString().trim());
        }

        disableActions(false);
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