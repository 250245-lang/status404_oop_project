package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;

public class EntrancePanelWindow {
    private final AppContext appContext;
    private final Stage stage;

    public EntrancePanelWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Entrance Panel Simulator");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d3436;");

        Label title = new Label("Welcome to Educative\nParking");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        title.setTextAlignment(TextAlignment.CENTER);

        Label statusLabel = new Label("Initializing System...");
        statusLabel.setStyle("-fx-text-fill: #dfe6e9; -fx-font-size: 14px;");

        TextField licenseField = new TextField();
        licenseField.setPromptText("Enter License Plate");
        licenseField.setPrefHeight(45);
        licenseField.setDisable(true);

        Button actionBtn = new Button("CHECK & ISSUE TICKET");
        actionBtn.setPrefHeight(70);
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        actionBtn.setDisable(true);

        root.getChildren().addAll(title, statusLabel, licenseField, actionBtn);

        stage.setScene(new javafx.scene.Scene(root, 440, 500));
        stage.show();

        // Simulate Hardware Boot
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    statusLabel.setText("System Ready");
                    statusLabel.setStyle("-fx-text-fill: #00b894;");
                    licenseField.setDisable(false);
                    actionBtn.setDisable(false);
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
