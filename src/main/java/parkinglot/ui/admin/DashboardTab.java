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

    // Stat Values
    private final Label occupancyValue = new Label("0/0");
    private final Label activeTicketsValue = new Label("0");
    private final Label attendantsValue = new Label("0");
    private final Label baseRateValue = new Label("$0.00");
    
    private final VBox floorStatusContainer = new VBox(15);
    private final Label lastUpdateLabel = new Label("Last updated: Never");

    public DashboardTab(AppContext appContext) {
        this.appContext = appContext;
        setupListeners();
    }

    private void setupListeners() {
        // Update when parking lot status changes
        appContext.parkingLotProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Platform.runLater(() -> updateUI(newVal));
            }
        });

        // Update when accounts list changes (attendant count)
        appContext.getAccounts().addListener((javafx.collections.ListChangeListener<? super parkinglot.users.Account>) c -> {
            Platform.runLater(() -> {
                long count = appContext.getAccounts().stream()
                        .filter(acc -> acc instanceof parkinglot.users.ParkingAttendant)
                        .count();
                attendantsValue.setText(String.valueOf(count));
            });
        });

        // Initial update if data already exists
        if (appContext.getParkingLot() != null) {
            updateUI(appContext.getParkingLot());
        }
    }

    private void updateUI(ParkingLot lot) {
        // Calculate Occupancy
        long totalSpots = lot.getFloors().stream().flatMap(f -> f.getSpots().stream()).count();
        long occupiedSpots = lot.getActiveTickets().size();
        occupancyValue.setText(occupiedSpots + "/" + totalSpots);

        activeTicketsValue.setText(String.valueOf(lot.getActiveTickets().size()));
        
        if (lot.getParkingRate() != null) {
            baseRateValue.setText(String.format("$%.2f", lot.getParkingRate().getFirstHourRate()));
        }

        // Update Floor Status
        floorStatusContainer.getChildren().clear();
        for (ParkingFloor floor : lot.getFloors()) {
            floorStatusContainer.getChildren().add(createFloorStatusRow(floor));
        }

        lastUpdateLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public ScrollPane getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // --- Header ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("Management Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        Label subtitle = new Label("Real-time parking lot overview and statistics");
        subtitle.setStyle("-fx-text-fill: #636e72;");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox statusIndicator = new HBox(10);
        statusIndicator.setAlignment(Pos.CENTER);
        Circle dot = new Circle(5, Color.web("#27ae60"));
        Label liveLabel = new Label("LIVE SYSTEM STATUS");
        liveLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 10px;");
        statusIndicator.getChildren().addAll(dot, liveLabel);
        
        header.getChildren().addAll(titleBox, spacer, statusIndicator);

        // --- Stat Cards ---
        HBox statCards = new HBox(20);
        statCards.setAlignment(Pos.CENTER);
        
        statCards.getChildren().addAll(
                createStatCard("Occupancy", occupancyValue, "Total slots used", "#0984e3"),
                createStatCard("Active Tickets", activeTicketsValue, "Vehicles currently in", "#e17055"),
                createStatCard("Attendants", attendantsValue, "Active staff", "#6c5ce7"),
                createStatCard("Base Rate", baseRateValue, "Initial per/hour", "#00b894")
        );

        // --- Main Content Area ---
        HBox mainContent = new HBox(20);
        
        // Floor Occupancy Section
        VBox floorSection = createContentCard("Floor Occupancy Breakdown");
        floorSection.getChildren().add(floorStatusContainer);
        HBox.setHgrow(floorSection, Priority.ALWAYS);
        
        // System Info Section
        VBox infoSection = createContentCard("System Information");
        infoSection.setMinWidth(300);
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(15);
        
        addInfoRow(infoGrid, "Server IP:", appContext.apiManager.getServerAddress().ip, 0);
        addInfoRow(infoGrid, "Server Port:", String.valueOf(appContext.apiManager.getServerAddress().port), 1);
        addInfoRow(infoGrid, "Database:", "SQLite (Local)", 2);
        addInfoRow(infoGrid, "Auth Mode:", "JWT-token", 3);
        
        infoSection.getChildren().addAll(infoGrid, new Separator(), lastUpdateLabel);

        mainContent.getChildren().addAll(floorSection, infoSection);

        root.getChildren().addAll(header, statCards, mainContent);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f8f9fa;");
        return scroll;
    }

    private VBox createStatCard(String title, Label value, String desc, String colorHex) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");
        
        value.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
        
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #636e72;");
        
        card.getChildren().addAll(titleLbl, value, descLbl);
        return card;
    }

    private VBox createContentCard(String title) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2d3436;");
        card.getChildren().add(lbl);
        return card;
    }

    private HBox createFloorStatusRow(ParkingFloor floor) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label name = new Label(floor.getName());
        name.setMinWidth(100);
        name.setStyle("-fx-font-weight: bold;");
        
        long total = floor.getSpots().size();
        long occupied = floor.getSpots().stream().filter(s -> !s.isFree()).count();
        double progress = total > 0 ? (double) occupied / total : 0;
        
        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);
        
        // Dynamic color for progress bar
        if (progress > 0.9) bar.setStyle("-fx-accent: #e74c3c;");
        else if (progress > 0.7) bar.setStyle("-fx-accent: #f39c12;");
        else bar.setStyle("-fx-accent: #2ecc71;");
        
        Label count = new Label(occupied + "/" + total);
        count.setMinWidth(50);
        count.setStyle("-fx-text-fill: #636e72; -fx-font-family: monospace;");
        
        row.getChildren().addAll(name, bar, count);
        return row;
    }

    private void addInfoRow(GridPane grid, String key, String val, int row) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill: #636e72; -fx-font-size: 12px;");
        Label v = new Label(val);
        v.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2d3436;");
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }
}
