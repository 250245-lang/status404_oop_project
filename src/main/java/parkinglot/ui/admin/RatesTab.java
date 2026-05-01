package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingRate;

public class RatesTab {
    private final AppContext appContext;
    private final Runnable onRefresh;

    private final TextField firstHourField = new TextField();
    private final TextField secondHourField = new TextField();
    private final TextField thirdHourField = new TextField();
    private final TextField remainingHourField = new TextField();
    private final TextField electricChargingField = new TextField();

    // Preview Labels
    private final Label preview1h = new Label("$0.00");
    private final Label preview3h = new Label("$0.00");
    private final Label preview8h = new Label("$0.00");
    private final Label preview24h = new Label("$0.00");

    public RatesTab(AppContext appContext, Runnable onRefresh) {
        this.appContext = appContext;
        this.onRefresh = onRefresh;
        
        setupInitialValues();
        setupLivePreview();
    }

    private void setupInitialValues() {
        // Safe check for initial data
        if (appContext.getParkingLot() != null && appContext.getParkingLot().getParkingRate() != null) {
            ParkingRate rate = appContext.getParkingLot().getParkingRate();
            firstHourField.setText(String.valueOf(rate.getFirstHourRate()));
            secondHourField.setText(String.valueOf(rate.getSecondHourRate()));
            thirdHourField.setText(String.valueOf(rate.getThirdHourRate()));
            remainingHourField.setText(String.valueOf(rate.getRemainingHourRate()));
            electricChargingField.setText(String.valueOf(rate.getElectricChargingRate()));
            updatePreview();
        }
        
        // Also listen for lot changes (central refresh)
        appContext.parkingLotProperty().addListener((obs, oldLot, newLot) -> {
            if (newLot != null) {
                Platform.runLater(() -> {
                    ParkingRate r = newLot.getParkingRate();
                    firstHourField.setText(String.valueOf(r.getFirstHourRate()));
                    secondHourField.setText(String.valueOf(r.getSecondHourRate()));
                    thirdHourField.setText(String.valueOf(r.getThirdHourRate()));
                    remainingHourField.setText(String.valueOf(r.getRemainingHourRate()));
                    electricChargingField.setText(String.valueOf(r.getElectricChargingRate()));
                    updatePreview();
                });
            }
        });
    }

    private void setupLivePreview() {
        firstHourField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        secondHourField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        thirdHourField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        remainingHourField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        electricChargingField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    private void updatePreview() {
        try {
            double h1 = Double.parseDouble(firstHourField.getText());
            double h2 = Double.parseDouble(secondHourField.getText());
            double h3 = Double.parseDouble(thirdHourField.getText());
            double rem = Double.parseDouble(remainingHourField.getText());

            preview1h.setText(String.format("$%.2f", h1));
            preview3h.setText(String.format("$%.2f", h1 + h2 + h3));
            preview8h.setText(String.format("$%.2f", h1 + h2 + h3 + (5 * rem)));
            preview24h.setText(String.format("$%.2f", h1 + h2 + h3 + (21 * rem)));
        } catch (NumberFormatException e) {
            // Ignore during typing
        }
    }

    public ScrollPane getContent() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Rate Configuration");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.CENTER);

        // --- Left: Form ---
        VBox formCard = createCard("Rate Settings");
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        addRateField(grid, "First Hour Rate:", firstHourField, 0);
        addRateField(grid, "Second Hour Rate:", secondHourField, 1);
        addRateField(grid, "Third Hour Rate:", thirdHourField, 2);
        addRateField(grid, "Remaining Hours Rate (per hr):", remainingHourField, 3);
        addRateField(grid, "EV Charging Rate (per hr):", electricChargingField, 4);

        Button saveBtn = new Button("Update Parking Rates");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 25;");
        saveBtn.setOnAction(e -> handleSave());

        formCard.getChildren().addAll(grid, new Separator(), saveBtn);

        // --- Right: Preview ---
        VBox previewCard = createCard("Revenue Estimation / Preview");
        previewCard.setMinWidth(300);
        
        GridPane previewGrid = new GridPane();
        previewGrid.setHgap(30);
        previewGrid.setVgap(15);

        addPreviewRow(previewGrid, "1 Hour Stay", preview1h, 0);
        addPreviewRow(previewGrid, "3 Hours Stay", preview3h, 1);
        addPreviewRow(previewGrid, "Full Day (8h)", preview8h, 2);
        addPreviewRow(previewGrid, "Overnight (24h)", preview24h, 3);

        Label info = new Label("Prices are calculated based on the\nhourly rates entered on the left.");
        info.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-text-alignment: center;");
        
        previewCard.getChildren().addAll(previewGrid, new Separator(), info);

        mainLayout.getChildren().addAll(formCard, previewCard);
        root.getChildren().addAll(title, mainLayout);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f4f7f6;");
        return scrollPane;
    }

    private VBox createCard(String title) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #34495e;");
        card.getChildren().add(lbl);
        return card;
    }

    private void addRateField(GridPane grid, String label, TextField field, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");
        
        HBox inputWrapper = new HBox(5);
        inputWrapper.setAlignment(Pos.CENTER_LEFT);
        Label dollar = new Label("$");
        dollar.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        field.setPrefWidth(100);
        
        inputWrapper.getChildren().addAll(dollar, field);
        
        grid.add(lbl, 0, row);
        grid.add(inputWrapper, 1, row);
    }

    private void addPreviewRow(GridPane grid, String label, Label value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #7f8c8d;");
        
        value.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        grid.add(lbl, 0, row);
        grid.add(value, 1, row);
    }

    private void handleSave() {
        try {
            ParkingRate newRate = new ParkingRate();
            newRate.setFirstHourRate(Double.parseDouble(firstHourField.getText()));
            newRate.setSecondHourRate(Double.parseDouble(secondHourField.getText()));
            newRate.setThirdHourRate(Double.parseDouble(thirdHourField.getText()));
            newRate.setRemainingHourRate(Double.parseDouble(remainingHourField.getText()));
            newRate.setElectricChargingRate(Double.parseDouble(electricChargingField.getText()));

            new Thread(() -> {
                try {
                    appContext.apiManager.updateRates(newRate);
                    Platform.runLater(() -> {
                        onRefresh.run();
                        showInfo("Success", "Parking rates updated successfully.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showError("Update Failed", ex.getMessage()));
                }
            }).start();

        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter valid decimal numbers for all rates.");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Message");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }
}
