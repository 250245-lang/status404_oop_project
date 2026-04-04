package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;

public class UsersTab {
    private final AppContext appContext;

    public UsersTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("System User Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // User List
        VBox listContainer = new VBox(15);
        listContainer.setPadding(new Insets(20));
        listContainer.setMinWidth(300);
        listContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label listTitle = new Label("REGISTERED USERS");
        listTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");

        ListView<String> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);
        listContainer.getChildren().addAll(listTitle, listView);

        // User Detail View
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(25));
        detailContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        detailContainer.getChildren().add(new Label("Select a user to view details"));

        splitPane.getItems().addAll(listContainer, detailContainer);
        splitPane.setDividerPositions(0.35);

        container.getChildren().addAll(title, splitPane);
        return container;
    }
}
