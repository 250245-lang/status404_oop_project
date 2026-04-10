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

        TextField newFloorField = new TextField();
        newFloorField.setPromptText("New Floor Name...");
        Button addFloorBtn = new Button("+ Add Floor");
        addFloorBtn.setMaxWidth(Double.MAX_VALUE);
        addFloorBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");

        floorListContainer.getChildren().addAll(floorListTitle, floorListView, newFloorField, addFloorBtn);

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

        spotTable.getColumns().addAll(numCol, typeCol, statusCol);
        spotTable.setPlaceholder(new Label("Select a floor from the left to manage its spots"));
        spotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(spotTable, Priority.ALWAYS);

        // Spot creation controls
        HBox spotControls = new HBox(10);
        spotControls.setAlignment(Pos.CENTER_LEFT);
        TextField spotNumField = new TextField();
        spotNumField.setPromptText("Spot ID");
        spotNumField.setPrefWidth(100);

        ComboBox<ParkingSpotType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(ParkingSpotType.values());
        typeCombo.setValue(ParkingSpotType.COMPACT);

        Button addSpotBtn = new Button("Add Spot");
        addSpotBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");

        spotControls.getChildren().addAll(spotNumField, typeCombo, addSpotBtn);

        spotContainer.getChildren().addAll(spotTitle, spotTable, spotControls);

        splitPane.getItems().addAll(floorListContainer, spotContainer);
        splitPane.setDividerPositions(0.3);

        root.getChildren().addAll(title, splitPane);
        return root;
    }
}
