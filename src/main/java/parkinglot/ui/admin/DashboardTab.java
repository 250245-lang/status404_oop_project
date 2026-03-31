package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;

public class DashboardTab {
    private final AppContext appContext;

    public DashboardTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public ScrollPane getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Management Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        HBox statCards = new HBox(20);
        statCards.setAlignment(Pos.CENTER);
        statCards.getChildren().addAll(
                createStatCard("Occupancy", "0/0", "#0984e3"),
                createStatCard("Active Tickets", "0", "#e17055"),
                createStatCard("Staff", "0", "#6c5ce7")
        );

        root.getChildren().addAll(title, statCards);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private VBox createStatCard(String title, String value, String colorHex) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");
        
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
        
        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }
}
