package com.f1weather;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var label = new Label("Welcome to the F1 Weather App!");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        var scene = new Scene(new StackPane(label), 800, 600);
        
        stage.setScene(scene);
        stage.setTitle("F1 Weather App");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}