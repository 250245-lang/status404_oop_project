package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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

    // Local copy — completely isolated from Admin Panel's parkingLotProperty
    private ParkingLot localLot;

    public ParkingDisplayBoard(AppContext appContext, String floorName) {
        this.appContext = appContext;
        this.floorName = floorName;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Display Board — Floor: " + floorName);
        stage.setResizable(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        // --- Header ---
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 30, 15, 30));
        header.setStyle("-fx-background-color: #111;");

        Label titleLabel = new Label("PARKING DISPLAY BOARD");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #636e72; -fx-letter-spacing: 3px;");

        Label floorLabel = new Label("Floor: " + floorName);
        floorLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #dfe6e9;");

        Label refreshLabel = new Label();
        refreshLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #636e72;");

        // Stats row
        HBox statsRow = new HBox(30);
        statsRow.setPadding(new Insets(10, 0, 0, 0));

        Label availableCount = new Label("-- Available");
        availableCount.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        Label occupiedCount = new Label("-- Occupied");
        occupiedCount.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63031;");
        Label totalCount = new Label("-- Total");
        totalCount.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #b2bec3;");

        statsRow.getChildren().addAll(availableCount, occupiedCount, totalCount);

        // Summary of types
        HBox summaryRow = new HBox(20);
        summaryRow.setPadding(new Insets(10, 0, 0, 0));
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(titleLabel, floorLabel, statsRow, summaryRow, refreshLabel);

        // --- Spot Grid ---
        FlowPane spotGrid = new FlowPane();
        spotGrid.setHgap(12);
        spotGrid.setVgap(12);
        spotGrid.setPadding(new Insets(25));
        spotGrid.setPrefWrapLength(600);

        ScrollPane scroll = new ScrollPane(spotGrid);
        scroll.setStyle("-fx-background: #0a0a0a; -fx-background-color: #0a0a0a;");
        scroll.setFitToWidth(true);

        root.setTop(header);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 750, 600);
        stage.setScene(scene);
        stage.show();

        // Shutdown scheduler when window closes
        stage.setOnCloseRequest(e -> scheduler.shutdownNow());

        // Initial load + periodic refresh every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ParkingLot fetched = appContext.apiManager.getStatus();
                localLot = fetched;

                ParkingFloor floor = findFloor(fetched);
                if (floor == null) return;

                List<ParkingSpot> spots = floor.getSpots();
                long free = spots.stream().filter(ParkingSpot::isFree).count();
                long occupied = spots.size() - free;

                Platform.runLater(() -> {
                    boolean isLotFull = fetched.getTotalFreeSpots() == 0;
                    if (isLotFull) {
                        floorLabel.setText("Floor: " + floorName + " (PARKING FULL)");
                        floorLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d63031;");
                    } else {
                        floorLabel.setText("Floor: " + floorName);
                        floorLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #dfe6e9;");
                    }

                    availableCount.setText(free + " Available");
                    occupiedCount.setText(occupied + " Occupied");
                    totalCount.setText(spots.size() + " Total");
                    refreshLabel.setText("");
                    
                    updateSummary(summaryRow, spots);
                    rebuildGrid(spotGrid, spots);
                });
            } catch (Exception e) {
                Platform.runLater(() -> refreshLabel.setText("Connection error — retrying..."));
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateSummary(HBox summaryRow, List<ParkingSpot> spots) {
        summaryRow.getChildren().clear();
        for (parkinglot.constants.ParkingSpotType type : parkinglot.constants.ParkingSpotType.values()) {
            long totalOfType = spots.stream().filter(s -> s.getType() == type).count();
            long freeOfType = spots.stream().filter(s -> s.getType() == type && s.isFree()).count();
            
            VBox box = new VBox(2);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(5, 15, 5, 15));
            box.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 5; -fx-border-color: #333; -fx-border-radius: 5;");
            
            Label typeLbl = new Label(type.toString());
            typeLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #b2bec3;");
            
            String text;
            String color;
            
            if (totalOfType == 0) {
                text = "N/A";
                color = "#636e72";
            } else if (freeOfType == 0) {
                text = "FULL";
                color = "#d63031";
            } else {
                text = String.valueOf(freeOfType);
                color = "#81ecec";
            }
            
            Label countLbl = new Label(text);
            countLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            
            box.getChildren().addAll(typeLbl, countLbl);
            summaryRow.getChildren().add(box);
        }
    }

    private void rebuildGrid(FlowPane grid, List<ParkingSpot> spots) {
        grid.getChildren().clear();
        for (ParkingSpot spot : spots) {
            VBox card = buildSpotCard(spot);
            grid.getChildren().add(card);
        }
    }

    private VBox buildSpotCard(ParkingSpot spot) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(110, 90);
        card.setPadding(new Insets(10));

        boolean free = spot.isFree();
        String bgColor = free ? "#1e3a2f" : "#3d1515";
        String borderColor = free ? "#00b894" : "#d63031";
        String textColor = free ? "#00b894" : "#d63031";

        card.setStyle(String.format(
            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-border-width: 2;", bgColor, borderColor));

        Label spotNum = new Label(spot.getNumber());
        spotNum.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Label typeLabel = new Label(spot.getType().toString());
        typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #b2bec3;");

        Label statusLabel = new Label(free ? "FREE" : "OCCUPIED");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        card.getChildren().addAll(spotNum, typeLabel, statusLabel);

        // If occupied, show license plate
        if (!free && spot.getCurrentVehicle() != null) {
            Label plateLabel = new Label(spot.getCurrentVehicle().getLicenseNumber());
            plateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #fdcb6e;");
            card.getChildren().add(plateLabel);
        }

        return card;
    }

    private ParkingFloor findFloor(ParkingLot lot) {
        return lot.getFloors().stream()
                .filter(f -> f.getName().equals(floorName))
                .findFirst()
                .orElse(null);
    }
}
