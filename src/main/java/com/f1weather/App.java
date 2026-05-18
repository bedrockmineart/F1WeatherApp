package com.f1weather;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        
        scene = new Scene(new MainPage(), 400, 866);
        
        stage.setScene(scene);
        stage.setTitle("F1 Weather App");
        stage.show();
    }

    public static void setRoot(Parent page) {
        scene.setRoot(page);
    }

    public static void main(String[] args) {
        launch();
    }
}