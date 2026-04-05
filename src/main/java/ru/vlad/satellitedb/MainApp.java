package ru.vlad.satellitedb;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.vlad.satellitedb.ui.view.MainView;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        new MainView(stage).init();
    }
}