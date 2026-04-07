package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;

public class RatesTab {
    private final AppContext appContext;

    public RatesTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public ScrollPane getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Rate Configuration");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setMaxWidth(500);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        addRateField(grid, "First Hour Rate:", 0);
        addRateField(grid, "Second Hour Rate:", 1);
        addRateField(grid, "Remaining Hours Rate:", 2);

        Button saveBtn = new Button("Update Parking Rates");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        formCard.getChildren().addAll(grid, new Separator(), saveBtn);

        root.getChildren().addAll(title, formCard);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private void addRateField(GridPane grid, String label, int row) {
        grid.add(new Label(label), 0, row);
        TextField field = new TextField();
        field.setPrefWidth(100);
        grid.add(field, 1, row);
    }
}
