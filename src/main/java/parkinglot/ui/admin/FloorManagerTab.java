package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import parkinglot.constants.ParkingSpotType;
import parkinglot.managers.AppContext;
import parkinglot.models.spots.ParkingSpot;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;

import java.util.List;

public class FloorManagerTab {
    private final AppContext appContext;
    private final Runnable onRefresh;

    public FloorManagerTab(AppContext appContext, Runnable onRefresh) {
        this.appContext = appContext;
        this.onRefresh = onRefresh;
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Infrastructure Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Left: Floor Management ---
        VBox floorListContainer = new VBox(15);
        floorListContainer.setPadding(new Insets(20));
        floorListContainer.setMinWidth(280);
        floorListContainer.setMaxHeight(Double.MAX_VALUE);
        floorListContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label floorListTitle = new Label("FLOOR STRUCTURE");
        floorListTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        ListView<String> floorListView = new ListView<>();
        floorListView.setStyle("-fx-background-insets: 0; -fx-padding: 0;");
        floorListView.setMinHeight(0);
        VBox.setVgrow(floorListView, Priority.ALWAYS);

        TextField newFloorField = new TextField();
        newFloorField.setPromptText("New Floor Name...");
        newFloorField.setPrefHeight(35);
        
        Button addFloorBtn = new Button("+ Add Floor");
        addFloorBtn.setMaxWidth(Double.MAX_VALUE);
        addFloorBtn.setPrefHeight(35);
        addFloorBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");
        addFloorBtn.setOnAction(e -> {
            String name = newFloorField.getText().trim();
            if (!name.isEmpty()) {
                new Thread(() -> {
                    try {
                        appContext.apiManager.addFloor(name);
                        Platform.runLater(() -> {
                            newFloorField.clear();
                            onRefresh.run();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showError("Failed to add floor", ex.getMessage()));
                    }
                }).start();
            }
        });
        
        Button deleteFloorBtn = new Button("Delete Floor");
        deleteFloorBtn.setMaxWidth(Double.MAX_VALUE);
        deleteFloorBtn.setPrefHeight(35);
        deleteFloorBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteFloorBtn.setOnAction(e -> {
            String selected = floorListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (confirm("Delete Floor", "Are you sure you want to delete floor " + selected + "?")) {
                    new Thread(() -> {
                        try {
                            appContext.apiManager.deleteFloor(selected);
                            Platform.runLater(onRefresh);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Platform.runLater(() -> showError("Failed to delete floor", ex.getMessage()));
                        }
                    }).start();
                }
            }
        });

        Button displayBoardBtn = new Button("📺  Display Board");
        displayBoardBtn.setMaxWidth(Double.MAX_VALUE);
        displayBoardBtn.setPrefHeight(35);
        displayBoardBtn.setStyle("-fx-background-color: #6c5ce7; -fx-text-fill: white; -fx-font-weight: bold;");
        displayBoardBtn.setOnAction(e -> {
            String selected = floorListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new parkinglot.ui.hardware.ParkingDisplayBoard(appContext, selected).show();
            } else {
                showError("No Floor Selected", "Please select a floor from the list first.");
            }
        });

        floorListContainer.getChildren().addAll(floorListTitle, floorListView, new Separator(), newFloorField, addFloorBtn, deleteFloorBtn, displayBoardBtn);

        // --- Right: Spot Management ---
        VBox spotContainer = new VBox(20);
        spotContainer.setPadding(new Insets(20));
        spotContainer.setMaxHeight(Double.MAX_VALUE);
        spotContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label spotTitle = new Label("PARKING SPOTS CONFIGURATION");
        spotTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        TableView<ParkingSpot> spotTable = new TableView<>();
        spotTable.setStyle("-fx-background-insets: 0;");
        
        TableColumn<ParkingSpot, String> numCol = new TableColumn<>("Spot ID");
        numCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        
        TableColumn<ParkingSpot, String> typeCol = new TableColumn<>("Category");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<ParkingSpot, Boolean> statusCol = new TableColumn<>("Availability");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("free"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "AVAILABLE" : "OCCUPIED");
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + (item ? "#27ae60" : "#e17055") + ";");
                }
            }
        });

        TableColumn<ParkingSpot, String> ticketCol = new TableColumn<>("Ticket No.");
        ticketCol.setCellValueFactory(cellData -> {
            ParkingSpot spot = cellData.getValue();
            if (!spot.isFree() && spot.getCurrentVehicle() != null && spot.getCurrentVehicle().getTicket() != null) {
                return new javafx.beans.property.SimpleStringProperty(spot.getCurrentVehicle().getTicket().getTicketNumber());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        TableColumn<ParkingSpot, String> vehicleCol = new TableColumn<>("Vehicle License");
        vehicleCol.setCellValueFactory(cellData -> {
            ParkingSpot spot = cellData.getValue();
            if (!spot.isFree() && spot.getCurrentVehicle() != null) {
                return new javafx.beans.property.SimpleStringProperty(spot.getCurrentVehicle().getLicenseNumber());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        spotTable.getColumns().addAll(numCol, typeCol, statusCol, ticketCol, vehicleCol);
        spotTable.setPlaceholder(new Label("Select a floor from the left to manage its spots"));
        spotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        spotTable.setMinHeight(0);

        // Spot creation controls
        HBox spotControls = new HBox(15);
        spotControls.setAlignment(Pos.CENTER_LEFT);
        spotControls.setPadding(new Insets(15));
        spotControls.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #eee; -fx-border-radius: 8;");

        TextField spotNumField = new TextField();
        spotNumField.setPromptText("ID");
        spotNumField.setPrefWidth(80);
        spotNumField.setPrefHeight(35);

        ComboBox<ParkingSpotType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(ParkingSpotType.values());
        typeCombo.getSelectionModel().select(ParkingSpotType.COMPACT);
        typeCombo.setPrefHeight(35);

        Button addSpotBtn = new Button("Add Spot");
        addSpotBtn.setPrefHeight(35);
        addSpotBtn.setPrefWidth(100);
        addSpotBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        addSpotBtn.setOnAction(_ -> {
            String floor = floorListView.getSelectionModel().getSelectedItem();
            String num = spotNumField.getText().trim();
            ParkingSpotType type = typeCombo.getValue();
            if (floor != null && !num.isEmpty()) {
                new Thread(() -> {
                    try {
                        appContext.apiManager.addSpot(floor, num, type);
                        Platform.runLater(() -> {
                            spotNumField.clear();
                            onRefresh.run();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showError("Failed to add spot", ex.getMessage()));
                    }
                }).start();
            }
        });
        
        Button deleteSpotBtn = new Button("Delete Spot");
        deleteSpotBtn.setPrefHeight(35);
        deleteSpotBtn.setPrefWidth(150);
        deleteSpotBtn.setStyle("-fx-background-color: #636e72; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteSpotBtn.setOnAction(e -> {
            String floor = floorListView.getSelectionModel().getSelectedItem();
            ParkingSpot spot = spotTable.getSelectionModel().getSelectedItem();
            if (floor != null && spot != null) {
                if (confirm("Delete Spot", "Are you sure you want to delete spot " + spot.getNumber() + "?")) {
                    new Thread(() -> {
                        try {
                            appContext.apiManager.deleteSpot(floor, spot.getNumber());
                            Platform.runLater(onRefresh);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Platform.runLater(() -> showError("Failed to delete spot", ex.getMessage()));
                        }
                    }).start();
                }
            }
        });

        Button evPanelBtn = new Button("⚡ EV Panel");
        evPanelBtn.setPrefHeight(35);
        evPanelBtn.setStyle("-fx-background-color: #fab1a0; -fx-text-fill: #2d3436; -fx-font-weight: bold;");
        evPanelBtn.setVisible(false);
        evPanelBtn.setManaged(false);
        evPanelBtn.setOnAction(e -> {
            ParkingSpot selected = spotTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new parkinglot.ui.hardware.ElectricPanelWindow(appContext, selected.getNumber()).show();
            }
        });

        spotTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isElectric = (newVal != null && newVal.getType() == ParkingSpotType.ELECTRIC);
            evPanelBtn.setVisible(isElectric);
            evPanelBtn.setManaged(isElectric);
        });

        spotControls.getChildren().addAll(new Label("QUICK ADD:"), spotNumField, typeCombo, addSpotBtn, new Region() {{ setPrefWidth(20); }}, deleteSpotBtn, evPanelBtn);

        spotContainer.getChildren().addAll(spotTitle, spotTable, spotControls);
        VBox.setVgrow(spotTable, Priority.ALWAYS);

        splitPane.getItems().addAll(floorListContainer, spotContainer);
        splitPane.setDividerPositions(0.3);

        root.getChildren().addAll(title, splitPane);

        // --- Logic: Data Observation & Selection ---
        appContext.parkingLotProperty().addListener((obs, oldLot, newLot) -> {
            if (newLot != null) {
                Platform.runLater(() -> updateFloorList(floorListView, newLot));
            }
        });

        // Initialize if data already exists
        if (appContext.getParkingLot() != null) {
            updateFloorList(floorListView, appContext.getParkingLot());
        }

        floorListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && appContext.getParkingLot() != null) {
                appContext.getParkingLot().getFloors().stream()
                        .filter(f -> f.getName().equals(newVal))
                        .findFirst()
                        .ifPresent(floor -> spotTable.setItems(FXCollections.observableArrayList(floor.getSpots())));
            }
        });

        return root;
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }

    private void updateFloorList(ListView<String> floorListView, ParkingLot lot) {
        List<String> names = lot.getFloors().stream().map(ParkingFloor::getName).toList();
        floorListView.setItems(FXCollections.observableArrayList(names));
        if (floorListView.getSelectionModel().getSelectedIndex() < 0 && !names.isEmpty()) {
            floorListView.getSelectionModel().select(0);
        }
    }
}
