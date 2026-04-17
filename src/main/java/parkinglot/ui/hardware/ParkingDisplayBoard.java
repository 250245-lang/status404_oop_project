package parkinglot.ui.hardware;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;

public class ParkingDisplayBoard {
    private final AppContext appContext;
    private final String floorName;
    private final Stage stage;

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
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #111;");

        Label titleLabel = new Label("PARKING DISPLAY BOARD");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #636e72;");

        Label floorLabel = new Label("Floor: " + floorName);
        floorLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #dfe6e9;");

        HBox statsRow = new HBox(40);
        Label availLabel = new Label("-- Available");
        availLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        Label occupiedLabel = new Label("-- Occupied");
        occupiedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d63031;");
        
        statsRow.getChildren().addAll(availLabel, occupiedLabel);
        header.getChildren().addAll(titleLabel, floorLabel, statsRow);

        // --- Spot Grid ---
        FlowPane spotGrid = new FlowPane();
        spotGrid.setHgap(15);
        spotGrid.setVgap(15);
        spotGrid.setPadding(new Insets(30));
        spotGrid.setStyle("-fx-background-color: #0a0a0a;");

        root.setTop(header);
        root.setCenter(spotGrid);

        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}
