package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import parkinglot.constants.VehicleType;
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

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);
        typeCombo.setPrefHeight(45);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setVisible(false);
        typeCombo.setManaged(false);

        Button actionBtn = new Button("CHECK & ISSUE TICKET");
        actionBtn.setPrefHeight(70);
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        actionBtn.setDisable(true);

        actionBtn.setOnAction(e -> {
            if (licenseField.getText().trim().isEmpty()) return;

            if (!typeCombo.isVisible()) {
                // First step: simulated "not found" or "first time"
                statusLabel.setText("First visit? Select vehicle type.");
                typeCombo.setVisible(true);
                typeCombo.setManaged(true);
                actionBtn.setText("CONFIRM & ISSUE");
                actionBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                // Second step: issue ticket (API call will be integrated later)
                statusLabel.setText("Issuing ticket...");
                actionBtn.setDisable(true);
            }
        });

        root.getChildren().addAll(title, statusLabel, licenseField, typeCombo, actionBtn);

        stage.setScene(new javafx.scene.Scene(root, 440, 500));
        stage.show();

        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                statusLabel.setText("System Ready");
                statusLabel.setStyle("-fx-text-fill: #00b894;");
                licenseField.setDisable(false);
                actionBtn.setDisable(false);
            });
        }).start();
    }
}
