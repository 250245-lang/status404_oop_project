package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;

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

        // Spot Placeholder
        StackPane spotContainer = new StackPane(new Label("Select a floor to manage spots"));
        spotContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        splitPane.getItems().addAll(floorListContainer, spotContainer);
        splitPane.setDividerPositions(0.3);

        root.getChildren().addAll(title, splitPane);
        return root;
    }
}
