package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingRate;

public class RatesTab {
    private final AppContext appContext;

    public RatesTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Rate Configuration");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        TextField hourlyRateField = new TextField();
        TextField maxRateField = new TextField();
        
        grid.add(new Label("Hourly Rate ($):"), 0, 0);
        grid.add(hourlyRateField, 1, 0);
        grid.add(new Label("Maximum Daily Cap ($):"), 0, 1);
        grid.add(maxRateField, 1, 1);

        Button saveBtn = new Button("Apply New Rates");
        saveBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setPrefWidth(200);

        saveBtn.setOnAction(e -> {
            try {
                double hourly = Double.parseDouble(hourlyRateField.getText());
                double max = Double.parseDouble(maxRateField.getText());
                ParkingRate rate = new ParkingRate(hourly, max);
                
                new Thread(() -> {
                    try {
                        appContext.apiManager.updateRates(rate);
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Rates updated successfully!");
                            alert.show();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update rates.");
                            alert.show();
                        });
                    }
                }).start();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Please enter valid numeric values.").show();
            }
        });

        card.getChildren().addAll(grid, saveBtn);
        root.getChildren().addAll(title, card);

        return root;
    }
}
