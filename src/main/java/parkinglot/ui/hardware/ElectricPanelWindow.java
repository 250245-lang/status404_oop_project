package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingLot;
import parkinglot.models.spots.ElectricSpot;
import parkinglot.models.spots.ParkingSpot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElectricPanelWindow {
    private final AppContext appContext;
    private final String spotNumber;
    private final Stage stage;
    private ScheduledExecutorService scheduler;

    public ElectricPanelWindow(AppContext appContext, String spotNumber) {
        this.appContext = appContext;
        this.spotNumber = spotNumber;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Electric Panel - Spot " + spotNumber);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d3436;"); // Dark theme

        Label title = new Label("⚡ ELECTRIC PANEL");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #fab1a0;");

        Label spotLabel = new Label("Spot: " + spotNumber);
        spotLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #dfe6e9;");

        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(20));
        statusBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label vehicleLabel = new Label("No Vehicle Detected");
        vehicleLabel.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 16px;");

        Label chargingStatus = new Label("Status: IDLE");
        chargingStatus.setStyle("-fx-text-fill: #dfe6e9; -fx-font-weight: bold;");

        Label timerLabel = new Label("Charging: 0 min");
        timerLabel.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 20px; -fx-font-weight: bold;");
        timerLabel.setVisible(false);

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        
        Button startBtn = new Button("START CHARGING");
        startBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        startBtn.setPrefHeight(40);
        
        Button stopBtn = new Button("STOP CHARGING");
        stopBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        stopBtn.setPrefHeight(40);
        
        controls.getChildren().addAll(startBtn, stopBtn);
        controls.setDisable(true);

        statusBox.getChildren().addAll(vehicleLabel, chargingStatus, timerLabel);
        root.getChildren().addAll(title, spotLabel, statusBox, controls);

        Scene scene = new Scene(root, 400, 450);
        stage.setScene(scene);
        stage.show();

        // Logic
        startBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    appContext.apiManager.startCharging(spotNumber);
                    appContext.syncData();
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });

        stopBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    appContext.apiManager.stopCharging(spotNumber);
                    appContext.syncData();
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });

        // Polling loop
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> refreshUI(vehicleLabel, chargingStatus, timerLabel, controls, startBtn, stopBtn));
        }, 0, 1, TimeUnit.SECONDS);

        stage.setOnCloseRequest(e -> scheduler.shutdownNow());
    }

    private void refreshUI(Label vehicleLabel, Label chargingStatus, Label timerLabel, HBox controls, Button startBtn, Button stopBtn) {
        ParkingLot lot = appContext.getParkingLot();
        if (lot == null) return;

        ElectricSpot spot = lot.getFloors().stream()
                .flatMap(f -> f.getSpots().stream())
                .filter(s -> s.getNumber().equals(spotNumber) && s instanceof ElectricSpot)
                .map(s -> (ElectricSpot) s)
                .findFirst()
                .orElse(null);

        if (spot == null) return;

        if (spot.isFree()) {
            vehicleLabel.setText("No Vehicle Detected");
            vehicleLabel.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 16px;");
            chargingStatus.setText("Status: IDLE");
            chargingStatus.setStyle("-fx-text-fill: #dfe6e9;");
            timerLabel.setVisible(false);
            controls.setDisable(true);
        } else {
            vehicleLabel.setText("Vehicle: " + spot.getCurrentVehicle().getLicenseNumber());
            vehicleLabel.setStyle("-fx-text-fill: #81ecec; -fx-font-size: 16px; -fx-font-weight: bold;");
            controls.setDisable(false);

            parkinglot.models.ParkingTicket ticket = spot.getCurrentVehicle().getTicket();
            boolean isTicketPaid = ticket != null && (ticket.isPaid() || ticket.getStatus() == parkinglot.constants.ParkingTicketStatus.COMPLETED);

            boolean isCharging = spot.getElectricPanel().isCharging();
            if (isCharging) {
                chargingStatus.setText("Status: CHARGING...");
                chargingStatus.setStyle("-fx-text-fill: #00b894; -fx-font-weight: bold;");
                int currentSession = spot.getElectricPanel().getCurrentlyChargingMinutes();
                int total = spot.getElectricPanel().getTotalChargedMinutes() + currentSession;
                timerLabel.setText("Session: " + total + " min");
                timerLabel.setVisible(true);
                startBtn.setDisable(true);
                stopBtn.setDisable(false);
            } else {
                if (isTicketPaid) {
                    chargingStatus.setText("Status: PAID (Charging Prohibited)");
                    chargingStatus.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold;");
                    startBtn.setDisable(true);
                } else {
                    chargingStatus.setText("Status: CONNECTED (Stopped)");
                    chargingStatus.setStyle("-fx-text-fill: #dfe6e9; -fx-font-weight: bold;");
                    startBtn.setDisable(false);
                }
                timerLabel.setText("Charged: " + spot.getElectricPanel().getTotalChargedMinutes() + " min");
                timerLabel.setVisible(true);
                stopBtn.setDisable(true);
            }
        }
    }
}
