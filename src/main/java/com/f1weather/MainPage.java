package com.f1weather;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainPage extends StackPane {
    
    public MainPage() {
        
        var label = new Label("Welcome to the F1 Weather App!");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        this.getChildren().add(label);
        
        
    }
}
