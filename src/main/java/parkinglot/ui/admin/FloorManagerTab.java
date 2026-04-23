package parkinglot.ui.admin;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import parkinglot.constants.ParkingSpotType;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingFloor;
import parkinglot.models.spots.ParkingSpot;
import parkinglot.ui.hardware.ParkingDisplayBoard;

public class FloorManagerTab {
    private final AppContext appContext;

    public FloorManagerTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Infrastructure Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Floor List
        VBox floorListContainer = new VBox(15);
        floorListContainer.setPadding(new Insets(20));
        floorListContainer.setMinWidth(280);
        floorListContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label floorListTitle = new Label("FLOOR STRUCTURE");
        floorListTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");

        ListView<String> floorListView = new ListView<>();
        VBox.setVgrow(floorListView, Priority.ALWAYS);

        HBox floorActions = new HBox(10);
        Button addFloorBtn = new Button("Add Floor");
        addFloorBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button deleteFloorBtn = new Button("Delete Floor");
        deleteFloorBtn.setStyle("-fx-background-color: #636e72; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button openDisplayBtn = new Button("Open Display");
        openDisplayBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");

        floorActions.getChildren().addAll(addFloorBtn, deleteFloorBtn, openDisplayBtn);
        floorListContainer.getChildren().addAll(floorListTitle, floorListView, floorActions);

        // Spot Management Table
        VBox spotContainer = new VBox(20);
        spotContainer.setPadding(new Insets(20));
        spotContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableView<ParkingSpot> spotTable = new TableView<>();
        TableColumn<ParkingSpot, String> numCol = new TableColumn<>("Spot ID");
        numCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        TableColumn<ParkingSpot, String> typeCol = new TableColumn<>("Category");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<ParkingSpot, Boolean> statusCol = new TableColumn<>("Availability");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("free"));

        spotTable.getColumns().addAll(numCol, typeCol, statusCol);
        VBox.setVgrow(spotTable, Priority.ALWAYS);

        // Selection Listener with Null Check
        floorListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                spotTable.setItems(FXCollections.emptyObservableList());
                return;
            }
            if (appContext.getParkingLot() != null) {
                ParkingFloor floor = appContext.getParkingLot().getFloors().stream()
                        .filter(f -> f.getName().equals(newVal))
                        .findFirst()
                        .orElse(null);
                if (floor != null) {
                    spotTable.setItems(FXCollections.observableArrayList(floor.getSpots()));
                }
            }
        });

        spotContainer.getChildren().addAll(new Label("PARKING SPOTS"), spotTable);
        splitPane.getItems().addAll(floorListContainer, spotContainer);
        root.getChildren().addAll(title, splitPane);
        return root;
    }
}
