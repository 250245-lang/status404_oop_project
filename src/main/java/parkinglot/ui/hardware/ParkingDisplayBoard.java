package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import parkinglot.constants.ParkingSpotType;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;
import parkinglot.models.spots.ParkingSpot;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ParkingDisplayBoard {
    private final AppContext appContext;
    private final String floorName;
    private final Stage stage;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final HBox summaryRow = new HBox(20);
    private final Label availLabel = new Label("-- Available");
    private final Label occupiedLabel = new Label("-- Occupied");

    public ParkingDisplayBoard(AppContext appContext, String floorName) {
        this.appContext = appContext;
        this.floorName = floorName;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Display Board — Floor: " + floorName);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        // --- Header ---
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #111;");

        Label titleLabel = new Label("PARKING DISPLAY BOARD");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #636e72;");

        Label floorLabel = new Label("Floor: " + floorName);
        floorLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #dfe6e9;");

        HBox statsRow = new HBox(40);
        availLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        occupiedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d63031;");
        statsRow.getChildren().addAll(availLabel, occupiedLabel);

        summaryRow.setPadding(new Insets(10, 0, 0, 0));
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(titleLabel, floorLabel, statsRow, summaryRow);

        // --- Spot Grid ---
        FlowPane spotGrid = new FlowPane();
        spotGrid.setHgap(15);
        spotGrid.setVgap(15);
        spotGrid.setPadding(new Insets(30));
        spotGrid.setStyle("-fx-background-color: #0a0a0a;");

        root.setTop(header);
        root.setCenter(spotGrid);

        stage.setScene(new Scene(root, 800, 600));
        stage.setOnCloseRequest(e -> scheduler.shutdownNow());
        stage.show();

        // Start Periodic Refresh
        scheduler.scheduleAtFixedRate(() -> {
            try {
                appContext.apiManager.syncData();
                Platform.runLater(() -> {
                    ParkingLot lot = appContext.getParkingLot();
                    if (lot != null) {
                        ParkingFloor floor = lot.getFloors().stream().filter(f -> f.getName().equals(floorName)).findFirst().orElse(null);
                        if (floor != null) {
                            updateSummary(floor.getSpots());
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Refresh failed: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateSummary(List<ParkingSpot> spots) {
        summaryRow.getChildren().clear();
        int totalFree = (int) spots.stream().filter(ParkingSpot::isFree).count();
        int totalOccupied = spots.size() - totalFree;

        availLabel.setText(totalFree + " Available");
        occupiedLabel.setText(totalOccupied + " Occupied");

        for (ParkingSpotType type : ParkingSpotType.values()) {
            long freeOfType = spots.stream().filter(s -> s.getType() == type && s.isFree()).count();
            
            VBox box = new VBox(2);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(5, 10, 5, 10));
            box.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 5;");
            
            Label typeLbl = new Label(type.toString());
            typeLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #b2bec3;");
            
            Label countLbl = new Label(String.valueOf(freeOfType));
            countLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #81ecec;");
            
            box.getChildren().addAll(typeLbl, countLbl);
            summaryRow.getChildren().add(box);
        }
    }
}
