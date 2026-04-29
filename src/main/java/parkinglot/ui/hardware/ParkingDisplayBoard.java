package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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
    private final FlowPane spotGrid = new FlowPane();
    private final Label floorLabel = new Label();
    private final ProgressBar occupancyBar = new ProgressBar(0);

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
        VBox header = new VBox(15);
        header.setPadding(new Insets(25, 30, 25, 30));
        header.setStyle("-fx-background-color: #111; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("PARKING DISPLAY BOARD");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #636e72;");

        floorLabel.setText("Floor: " + floorName);
        floorLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #dfe6e9;");

        HBox statsRow = new HBox(40);
        availLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        occupiedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d63031;");
        statsRow.getChildren().addAll(availLabel, occupiedLabel);

        occupancyBar.setMaxWidth(Double.MAX_VALUE);
        occupancyBar.setPrefHeight(12);
        occupancyBar.setStyle("-fx-accent: #00b894;");

        summaryRow.setPadding(new Insets(5, 0, 0, 0));
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(titleLabel, floorLabel, occupancyBar, statsRow, summaryRow);

        // --- Responsive Spot Grid ---
        spotGrid.setHgap(15);
        spotGrid.setVgap(15);
        spotGrid.setPadding(new Insets(30));
        spotGrid.setStyle("-fx-background-color: #0a0a0a;");

        ScrollPane scroll = new ScrollPane(spotGrid);
        scroll.setStyle("-fx-background: #0a0a0a; -fx-background-color: #0a0a0a; -fx-border-width: 0;");
        scroll.setFitToWidth(true);
        
        // Responsive binding
        spotGrid.prefWidthProperty().bind(scroll.widthProperty().subtract(60));

        root.setTop(header);
        root.setCenter(scroll);

        stage.setScene(new Scene(root, 900, 750));
        stage.setOnCloseRequest(e -> scheduler.shutdownNow());
        stage.show();

        // Periodic Refresh
        scheduler.scheduleAtFixedRate(() -> {
            try {
                appContext.apiManager.syncData();
                Platform.runLater(() -> {
                    ParkingLot lot = appContext.getParkingLot();
                    if (lot != null) {
                        ParkingFloor floor = lot.getFloors().stream().filter(f -> f.getName().equals(floorName)).findFirst().orElse(null);
                        if (floor != null) {
                            updateSummary(floor.getSpots());
                            rebuildGrid(floor.getSpots());
                            double progress = (double) (floor.getSpots().size() - floor.getSpots().stream().filter(ParkingSpot::isFree).count()) / floor.getSpots().size();
                            occupancyBar.setProgress(progress);
                            if (progress > 0.9) occupancyBar.setStyle("-fx-accent: #d63031;");
                            else if (progress > 0.7) occupancyBar.setStyle("-fx-accent: #f39c12;");
                            else occupancyBar.setStyle("-fx-accent: #00b894;");
                        }
                    }
                });
            } catch (Exception ignored) {}
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateSummary(List<ParkingSpot> spots) {
        summaryRow.getChildren().clear();
        int totalFree = (int) spots.stream().filter(ParkingSpot::isFree).count();
        availLabel.setText(totalFree + " Available");
        occupiedLabel.setText((spots.size() - totalFree) + " Occupied");

        for (ParkingSpotType type : ParkingSpotType.values()) {
            long totalOfType = spots.stream().filter(s -> s.getType() == type).count();
            if (totalOfType == 0) continue;
            long freeOfType = spots.stream().filter(s -> s.getType() == type && s.isFree()).count();
            VBox box = new VBox(2);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(5, 12, 5, 12));
            box.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6;");
            Label typeLbl = new Label(type.toString());
            typeLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #b2bec3;");
            String countText = (freeOfType == 0) ? "FULL" : String.valueOf(freeOfType);
            String countColor = (freeOfType == 0) ? "#d63031" : "#81ecec";
            Label countLbl = new Label(countText);
            countLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + countColor + ";");
            box.getChildren().addAll(typeLbl, countLbl);
            summaryRow.getChildren().add(box);
        }
    }

    private void rebuildGrid(List<ParkingSpot> spots) {
        spotGrid.getChildren().clear();
        for (ParkingSpot spot : spots) {
            spotGrid.getChildren().add(buildSpotCard(spot));
        }
    }

    private VBox buildSpotCard(ParkingSpot spot) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(130, 100);
        card.setPadding(new Insets(10));
        boolean free = spot.isFree();
        String color = free ? "#00b894" : "#d63031";
        card.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        Label num = new Label(spot.getNumber());
        num.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label status = new Label(free ? "FREE" : "OCCUPIED");
        status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        card.getChildren().addAll(num, status);
        return card;
    }
}
