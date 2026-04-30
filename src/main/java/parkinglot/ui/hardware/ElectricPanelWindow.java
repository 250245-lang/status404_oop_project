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
        stage.setTitle("Electric Charging Panel - Spot " + spotNumber);

        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d3436;");

        Label title = new Label("⚡ EV CHARGING STATION");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #fab1a0;");

        Label spotLabel = new Label("Station ID: " + spotNumber);
        spotLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dfe6e9;");

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 12;");

        Label vehicleLabel = new Label("Searching for Vehicle...");
        vehicleLabel.setStyle("-fx-text-fill: #b2bec3;");
        Label statusLabel = new Label("IDLE");
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        Button startBtn = new Button("START CHARGING");
        startBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");
        Button stopBtn = new Button("STOP CHARGING");
        stopBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        actions.getChildren().addAll(startBtn, stopBtn);
        actions.setDisable(true);

        infoBox.getChildren().addAll(vehicleLabel, statusLabel);
        root.getChildren().addAll(title, spotLabel, infoBox, actions);

        stage.setScene(new Scene(root, 400, 400));
        stage.show();

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

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                ParkingLot lot = appContext.getParkingLot();
                if (lot == null) return;
                ElectricSpot spot = lot.getFloors().stream()
                        .flatMap(f -> f.getSpots().stream())
                        .filter(s -> s.getNumber().equals(spotNumber) && s instanceof ElectricSpot)
                        .map(s -> (ElectricSpot) s)
                        .findFirst().orElse(null);

                if (spot != null) {
                    if (spot.isFree()) {
                        vehicleLabel.setText("No Vehicle Detected");
                        statusLabel.setText("IDLE");
                        actions.setDisable(true);
                    } else {
                        vehicleLabel.setText("Connected: " + spot.getCurrentVehicle().getLicenseNumber());
                        boolean charging = spot.getElectricPanel().isCharging();
                        statusLabel.setText(charging ? "CHARGING..." : "CONNECTED");
                        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + (charging ? "#00b894" : "white") + ";");
                        actions.setDisable(false);
                        startBtn.setDisable(charging);
                        stopBtn.setDisable(!charging);
                    }
                }
            });
        }, 0, 2, TimeUnit.SECONDS);

        stage.setOnCloseRequest(e -> scheduler.shutdownNow());
    }
}
