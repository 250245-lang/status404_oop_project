package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardTab {
    private final AppContext appContext;
    
    private final Label occupancyValue = new Label("0/0");
    private final Label activeTicketsValue = new Label("0");
    private final Label staffValue = new Label("0");
    private final Label baseRateValue = new Label("$0.00");
    private final VBox floorStatusContainer = new VBox(15);
    private final Label lastUpdateLabel = new Label("Last updated: Never");

    public DashboardTab(AppContext appContext) {
        this.appContext = appContext;
        setupListeners();
    }

    private void setupListeners() {
        appContext.parkingLotProperty().addListener((obs, oldLot, newLot) -> {
            if (newLot != null) {
                Platform.runLater(() -> updateUI(newLot));
            }
        });
        
        if (appContext.getParkingLot() != null) {
            updateUI(appContext.getParkingLot());
        }
    }

    private void updateUI(ParkingLot lot) {
        long totalSpots = lot.getFloors().stream().flatMap(f -> f.getSpots().stream()).count();
        long occupiedSpots = lot.getFloors().stream().flatMap(f -> f.getSpots().stream()).filter(s -> !s.isFree()).count();
        
        occupancyValue.setText(occupiedSpots + "/" + totalSpots);
        activeTicketsValue.setText(String.valueOf(lot.getAllTickets().size()));
        
        new Thread(() -> {
            try {
                int count = appContext.apiManager.getAccounts().size();
                Platform.runLater(() -> staffValue.setText(String.valueOf(count)));
            } catch (Exception ignored) {}
        }).start();

        floorStatusContainer.getChildren().clear();
        for (ParkingFloor floor : lot.getFloors()) {
            floorStatusContainer.getChildren().add(createFloorStatusRow(floor));
        }

        lastUpdateLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public Node getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(5);
        Label title = new Label("Management Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        titleBox.getChildren().add(title);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox statusIndicator = new HBox(10, new Circle(5, Color.web("#27ae60")), new Label("LIVE SYSTEM STATUS"));
        statusIndicator.setAlignment(Pos.CENTER);
        header.getChildren().addAll(titleBox, spacer, statusIndicator);

        HBox statCards = new HBox(20);
        statCards.setAlignment(Pos.CENTER);
        statCards.getChildren().addAll(
                createStatCard("Occupancy", occupancyValue, "Total slots used", "#0984e3"),
                createStatCard("Active Tickets", activeTicketsValue, "Vehicles in lot", "#e17055"),
                createStatCard("Total Staff", staffValue, "Registered users", "#6c5ce7"),
                createStatCard("Base Rate", baseRateValue, "Initial per/hour", "#00b894")
        );

        HBox mainContent = new HBox(20);
        VBox floorSection = createContentCard("Floor Occupancy Breakdown");
        floorSection.getChildren().add(floorStatusContainer);
        HBox.setHgrow(floorSection, Priority.ALWAYS);
        
        VBox infoSection = createContentCard("System Information");
        infoSection.setMinWidth(300);
        infoSection.getChildren().addAll(new Label("Server IP: " + appContext.apiManager.getServerAddress().ip), new Separator(), lastUpdateLabel);

        mainContent.getChildren().addAll(floorSection, infoSection);
        root.getChildren().addAll(header, statCards, mainContent);
        
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f8f9fa;");
        return scroll;
    }

    private VBox createStatCard(String title, Label value, String desc, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        Label t = new Label(title.toUpperCase());
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");
        value.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        card.getChildren().addAll(t, value, new Label(desc));
        return card;
    }

    private VBox createContentCard(String title) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        card.getChildren().add(lbl);
        return card;
    }

    private HBox createFloorStatusRow(ParkingFloor floor) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(floor.getName());
        name.setMinWidth(100);
        long total = floor.getSpots().size();
        long occupied = floor.getSpots().stream().filter(s -> !s.isFree()).count();
        ProgressBar bar = new ProgressBar(total > 0 ? (double) occupied / total : 0);
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);
        row.getChildren().addAll(name, bar, new Label(occupied + "/" + total));
        return row;
    }
}
