package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.spots.ParkingSpot;

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

        floorListContainer.getChildren().addAll(floorListTitle, floorListView);

        // Spot Management Table
        VBox spotContainer = new VBox(20);
        spotContainer.setPadding(new Insets(20));
        spotContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label spotTitle = new Label("PARKING SPOTS CONFIGURATION");
        spotTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");

        TableView<ParkingSpot> spotTable = new TableView<>();
        
        TableColumn<ParkingSpot, String> numCol = new TableColumn<>("Spot ID");
        numCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        
        TableColumn<ParkingSpot, String> typeCol = new TableColumn<>("Category");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<ParkingSpot, Boolean> statusCol = new TableColumn<>("Availability");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("free"));

        spotTable.getColumns().addAll(numCol, typeCol, statusCol);
        spotTable.setPlaceholder(new Label("Select a floor from the left to manage its spots"));
        spotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(spotTable, Priority.ALWAYS);

        spotContainer.getChildren().addAll(spotTitle, spotTable);

        splitPane.getItems().addAll(floorListContainer, spotContainer);
        splitPane.setDividerPositions(0.3);

        root.getChildren().addAll(title, splitPane);
        return root;
    }
}
