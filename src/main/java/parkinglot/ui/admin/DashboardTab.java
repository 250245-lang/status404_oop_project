package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;

public class DashboardTab {
    private final AppContext appContext;
    
    private final Label occupancyLabel = new Label("0%");
    private final Label ticketLabel = new Label("0");
    private final Label staffLabel = new Label("0");
    private final VBox floorContainer = new VBox(15);

    public DashboardTab(AppContext appContext) {
        this.appContext = appContext;
        setupDataBinding();
    }

    private void setupDataBinding() {
        appContext.parkingLotProperty().addListener((obs, oldLot, newLot) -> {
            if (newLot != null) {
                Platform.runLater(() -> updateStats(newLot));
            }
        });
        
        if (appContext.getParkingLot() != null) {
            updateStats(appContext.getParkingLot());
        }
    }

    private void updateStats(ParkingLot lot) {
        int totalSpots = lot.getFloors().stream().mapToInt(f -> f.getSpots().size()).sum();
        int occupiedSpots = (int) lot.getFloors().stream()
                .flatMap(f -> f.getSpots().stream())
                .filter(s -> !s.isFree())
                .count();
        
        double percent = totalSpots > 0 ? (occupiedSpots * 100.0 / totalSpots) : 0;
        occupancyLabel.setText(String.format("%.1f%%", percent));
        ticketLabel.setText(String.valueOf(lot.getAllTickets().size()));
        staffLabel.setText("N/A");

        floorContainer.getChildren().clear();
        for (ParkingFloor floor : lot.getFloors()) {
            floorContainer.getChildren().add(createFloorRow(floor));
        }
    }

    public Node getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label welcome = new Label("System Overview Dashboard");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox statCards = new HBox(25);
        statCards.setAlignment(Pos.CENTER);
        statCards.getChildren().addAll(
                createStatCard("CURRENT OCCUPANCY", occupancyLabel, "#0984e3"),
                createStatCard("ACTIVE TICKETS", ticketLabel, "#00b894"),
                createStatCard("TOTAL STAFF", staffLabel, "#6c5ce7")
        );

        VBox bottomSection = new VBox(15);
        Label floorTitle = new Label("FLOOR UTILIZATION");
        floorTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #b2bec3; -fx-font-size: 11px;");
        
        floorContainer.setPadding(new Insets(5, 0, 0, 0));
        bottomSection.getChildren().addAll(floorTitle, floorContainer);

        root.getChildren().addAll(welcome, statCards, bottomSection);
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f4f7f6;");
        return scrollPane;
    }

    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setPrefSize(220, 120);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b2bec3;");
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private HBox createFloorRow(ParkingFloor floor) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label name = new Label(floor.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        name.setPrefWidth(150);

        int total = floor.getSpots().size();
        int free = (int) floor.getSpots().stream().filter(s -> s.isFree()).count();
        
        Label status = new Label(String.format("%d / %d Spots Available", free, total));
        status.setStyle("-fx-text-fill: #636e72;");

        row.getChildren().addAll(name, status);
        return row;
    }
}
