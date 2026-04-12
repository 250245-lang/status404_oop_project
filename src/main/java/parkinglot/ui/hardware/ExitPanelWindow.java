package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

        // Payment Box
        VBox paymentBox = new VBox(15);
        paymentBox.setAlignment(Pos.CENTER);
        paymentBox.setStyle("-fx-background-color: #353b48; -fx-padding: 15; -fx-background-radius: 8;");
        paymentBox.setDisable(true);
        
        Label feeLabel = new Label("Amount Due: $0.00");
        feeLabel.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField ccField = new TextField();
        ccField.setPromptText("Card Number");
        
        HBox ccDetails = new HBox(10, new TextField() {{ setPromptText("MM/YY"); setPrefWidth(80); }}, new TextField() {{ setPromptText("CVV"); setPrefWidth(60); }});
        ccDetails.setAlignment(Pos.CENTER);

        Button payBtn = new Button("PAY WITH CREDIT CARD");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setStyle("-fx-background-color: #55efc4; -fx-text-fill: #2d3436; -fx-font-weight: bold;");

        paymentBox.getChildren().addAll(feeLabel, ccField, ccDetails, payBtn);

        Button openGateBtn = new Button("OPEN GATE");
        openGateBtn.setMaxWidth(Double.MAX_VALUE);
        openGateBtn.setPrefHeight(60);
        openGateBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        openGateBtn.setDisable(true);

        // Scan Logic with API connection
        scanBtn.setOnAction(e -> {
            String ticketNo = ticketField.getText().trim();
            if (ticketNo.isEmpty()) return;

            statusLabel.setText("Scanning...");
            scanBtn.setDisable(true);

            new Thread(() -> {
                try {
                    double fee = appContext.apiManager.calculateFee(ticketNo);
                    Platform.runLater(() -> {
                        statusLabel.setText("Scan Complete. Fee calculated.");
                        feeLabel.setText(String.format("Amount Due: $%.2f", fee));
                        paymentBox.setDisable(false);
                        scanBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: Invalid ticket.");
                        statusLabel.setStyle("-fx-text-fill: #d63031;");
                        scanBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // Logic for Gate Open and Reset
        openGateBtn.setOnAction(e -> {
            statusLabel.setText("Gate Opening... Goodbye!");
            statusLabel.setStyle("-fx-text-fill: #fdcb6e;");
            openGateBtn.setDisable(true);
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    ticketField.clear();
                    statusLabel.setText("System Ready. Please scan ticket.");
                    statusLabel.setStyle("-fx-text-fill: #00b894;");
                    paymentBox.setDisable(true);
                });
            }).start();
        });

        root.getChildren().addAll(title, statusLabel, ticketField, scanBtn, new Separator(), paymentBox, openGateBtn);

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
