package ru.vlad.satellitedb.ui.view;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainView {

    private final Stage stage;
    private final BorderPane root = new BorderPane();

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void init() {
        ListView<String> menu = new ListView<>();
        menu.getItems().addAll(
                "Главная карта",
                "Спутники",
                "Организации",
                "Серии",
                "Полезные нагрузки",
                "Статусы спутников",
                "Назначения спутников",
                "Типы организаций",
                "Типы полезной нагрузки"
        );

        root.setLeft(menu);
        root.setCenter(new OrbitMapView());

        menu.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                return;
            }

            switch (newVal) {
                case "Главная карта" -> root.setCenter(new OrbitMapView());
                case "Спутники" -> root.setCenter(new SatelliteView());
                case "Организации" -> root.setCenter(new OrganizationView());
                case "Серии" -> root.setCenter(new SatelliteSeriesView());
                case "Полезные нагрузки" -> root.setCenter(new PayloadView());
                case "Статусы спутников" -> root.setCenter(new SatelliteStatusView());
                case "Назначения спутников" -> root.setCenter(new SatellitePurposeView());
                case "Типы организаций" -> root.setCenter(new OrganizationTypeView());
                case "Типы полезной нагрузки" -> root.setCenter(new PayloadTypeView());
            }
        });

        menu.getSelectionModel().selectFirst();

        stage.setScene(new Scene(root, 1300, 760));
        stage.setTitle("База спутников");
        stage.show();
    }
}