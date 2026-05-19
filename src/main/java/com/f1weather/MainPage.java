package com.f1weather;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

public class MainPage extends StackPane {
    
    public MainPage() {
//
//        var label = new Label("Welcome to the F1 Weather App!");
//        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//
//        this.getChildren().add(label);
//

        // TODO: Fix font
        String fontUrl = "https://fonts.gstatic.com/s/fasterone/v19/H4clBX6Ar_vka0DcxO46pE7Wiw.ttf";
        Font fasterOne = Font.loadFont(fontUrl, 24);


        Label header = new Label("Race Selector");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ddd;");
        if (fasterOne != null) {
            header.setFont(fasterOne);
        }
        header.setMaxWidth(Double.MAX_VALUE);
        header.setPadding(new Insets(10));


         // TODO:
        // get race list here
        // then pass in nicely
        VBox contentBox = new VBox(10); // 10px spacing between items
        for (int i = 1; i <= 50; i++) {
            Button b = new Button();
            Label nameLabel = new Label("FILLER NAME");
            //nameLabel.setStyle("-fx-background-color: ")
            HBox innerLayout = new HBox(10);
            HBox dateBox = new HBox(10);
            dateBox.setStyle("-fx-background-color: #2A2A2E; -fx-alignment: centre-right");
            //dateBox.setAlignment();
            Label date = new Label("FILLER DATE");
            date.setStyle("-fx-background-color: #FFFFFF;");
            dateBox.getChildren().addAll(date);
            innerLayout.getChildren().addAll(nameLabel, dateBox);
            b.setMaxWidth(Double.MAX_VALUE);
            b.getStyleClass().add("main-page-button");
            b.setGraphic(innerLayout);
            String sessionKey = "latest"; // later replace with real race id
            b.setOnAction(e -> {
                App.setRoot(new RaceDetails(sessionKey));
            });
            contentBox.getChildren().add(b);
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(0, 20, 0, 20));



        VBox rootLayout = new VBox(scrollPane); // ScrollPane in root box
        rootLayout.getChildren().add(0, header);

        this.getChildren().add(rootLayout);

    }
}
