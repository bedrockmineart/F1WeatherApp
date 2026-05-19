package com.f1weather;

import javafx.scene.layout.VBox;

import javafx.scene.control.Label;

public class RaceDetails extends VBox {

    private final String sessionKey;
    private OpenF1Client client = new OpenF1Client();
    
    public RaceDetails(String sessionKey) {
        this.sessionKey = sessionKey;

        this.getStylesheets().add("page");

        buildUI();
        loadData();
    }

    private void buildUI(){
        Label title = new Label("Race Weather");
        title.getStyleClass().add("title");
    }

    private void loadData() {}
}
