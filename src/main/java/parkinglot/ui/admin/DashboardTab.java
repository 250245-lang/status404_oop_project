package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingLot;

public class DashboardTab {
    private final AppContext appContext;
    
    private final Label occupancyLabel = new Label("0%");
    private final Label ticketLabel = new Label("0");
    private final Label staffLabel = new Label("0");

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
        staffLabel.setText("N/A"); // Account sync logic will be added later
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

        root.getChildren().addAll(welcome, statCards);
        return root;
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
}
