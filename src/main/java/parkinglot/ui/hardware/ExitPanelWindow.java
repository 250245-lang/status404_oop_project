package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;

public class ExitPanelWindow {
    private final AppContext appContext;
    private final Stage stage;

    public ExitPanelWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Automated Exit Panel");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d3436;");

        Label title = new Label("Exit Gate Terminal");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #fab1a0;");

        Label statusLabel = new Label("Initializing System...");
        statusLabel.setStyle("-fx-text-fill: #dfe6e9; -fx-font-size: 14px;");

        TextField ticketField = new TextField();
        ticketField.setPromptText("Enter Ticket No.");
        ticketField.setPrefHeight(40);
        ticketField.setDisable(true);

        Button scanBtn = new Button("SCAN TICKET");
        scanBtn.setMaxWidth(Double.MAX_VALUE);
        scanBtn.setPrefHeight(40);
        scanBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        scanBtn.setDisable(true);

        Label feeLabel = new Label("Amount Due: $0.00");
        feeLabel.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button openGateBtn = new Button("OPEN GATE");
        openGateBtn.setMaxWidth(Double.MAX_VALUE);
        openGateBtn.setPrefHeight(60);
        openGateBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        openGateBtn.setDisable(true);

        root.getChildren().addAll(title, statusLabel, ticketField, scanBtn, new Separator(), feeLabel, openGateBtn);

        stage.setScene(new javafx.scene.Scene(root, 400, 550));
        stage.show();

        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                statusLabel.setText("System Ready. Please scan ticket.");
                statusLabel.setStyle("-fx-text-fill: #00b894;");
                ticketField.setDisable(false);
                scanBtn.setDisable(false);
            });
        }).start();
    }
}
