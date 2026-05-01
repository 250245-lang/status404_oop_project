package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingTicket;

public class CustomerInfoPortalWindow {
    private final AppContext appContext;
    private final Stage stage;
    private ParkingTicket currentTicket;

    public CustomerInfoPortalWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Customer Info Portal");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;"); // Deep blue-gray theme for info portal

        Label title = new Label("Pre-Payment Kiosk");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #81ecec;");

        Label statusLabel = new Label("Initializing System...");
        statusLabel.setStyle("-fx-text-fill: #dfe6e9; -fx-font-size: 14px;");

        // --- Step 1: Scan Ticket ---
        Label ticketLabel = new Label("Parking Ticket Number");
        ticketLabel.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextField ticketField = new TextField();
        ticketField.setPromptText("Enter Ticket No.");
        ticketField.setPrefHeight(40);
        ticketField.setStyle("-fx-font-size: 14px; -fx-alignment: center;");
        ticketField.setTextFormatter(AppContext.getUppercaseFormatter());
        ticketField.setDisable(true);

        VBox ticketBox = new VBox(5, ticketLabel, ticketField);
        ticketBox.setAlignment(Pos.CENTER_LEFT);

        Button scanBtn = new Button("SCAN TICKET");
        scanBtn.setMaxWidth(Double.MAX_VALUE);
        scanBtn.setPrefHeight(40);
        scanBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        scanBtn.setDisable(true);

        // --- Step 2: Payment ---
        VBox paymentBox = new VBox(15);
        paymentBox.setAlignment(Pos.CENTER);
        paymentBox.setDisable(true);
        paymentBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        
        Label feeLabel = new Label("Amount Due: $0.00");
        feeLabel.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField ccField = new TextField();
        ccField.setPromptText("Card Number (e.g. 1234-5678-9012-3456)");
        ccField.setStyle("-fx-font-size: 14px;");

        HBox ccDetailsBox = new HBox(10);
        TextField expField = new TextField();
        expField.setPromptText("MM/YY");
        expField.setPrefWidth(80);
        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");
        cvvField.setPrefWidth(60);
        ccDetailsBox.getChildren().addAll(expField, cvvField);
        ccDetailsBox.setAlignment(Pos.CENTER);

        Button payCreditBtn = new Button("PAY WITH CREDIT CARD");
        payCreditBtn.setMaxWidth(Double.MAX_VALUE);
        payCreditBtn.setPrefHeight(40);
        payCreditBtn.setStyle("-fx-background-color: #55efc4; -fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-cursor: hand;");
        
        paymentBox.getChildren().addAll(feeLabel, ccField, ccDetailsBox, payCreditBtn);

        // Reset Button
        Button resetBtn = new Button("Cancel / Start Over");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b2bec3; -fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;");
        resetBtn.setOnAction(e -> {
            ticketField.clear();
            currentTicket = null;
            paymentBox.setDisable(true);
            statusLabel.setText("System Ready. Please scan ticket.");
            statusLabel.setStyle("-fx-text-fill: #00b894; -fx-font-size: 14px;");
            scanBtn.setDisable(false);
            ccField.clear();
            expField.clear();
            cvvField.clear();
        });

        root.getChildren().addAll(title, statusLabel, ticketBox, scanBtn, new Separator(), paymentBox, resetBtn);

        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();

        // 1. Authenticate Hardware
        new Thread(() -> {
            try {
                appContext.apiManager.hardwareLogin();
                Platform.runLater(() -> {
                    statusLabel.setText("System Ready. Please scan ticket.");
                    statusLabel.setStyle("-fx-text-fill: #00b894; -fx-font-size: 14px;");
                    ticketField.setDisable(false);
                    scanBtn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Network Error: Cannot connect.");
                    statusLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold;");
                });
            }
        }).start();

        // 2. Scan Logic
        scanBtn.setOnAction(e -> {
            String tNo = ticketField.getText().trim();
            if (tNo.isEmpty()) return;
            scanBtn.setDisable(true);
            statusLabel.setText("Reading ticket...");
            
            new Thread(() -> {
                try {
                    ParkingTicket t = appContext.apiManager.getTicket(tNo);
                    double fee = t.isPaid() ? t.getPayedAmount() : appContext.apiManager.calculateFee(tNo);
                    Platform.runLater(() -> {
                        currentTicket = t;
                        if (t.isPaid()) {
                            statusLabel.setText("Ticket already paid. You may exit.");
                            statusLabel.setStyle("-fx-text-fill: #00b894;");
                            paymentBox.setDisable(true);
                        } else {
                            statusLabel.setText("Please complete your payment.");
                            statusLabel.setStyle("-fx-text-fill: #fdcb6e;");
                            paymentBox.setDisable(false);
                            feeLabel.setText(String.format("Amount Due: $%.2f", fee));
                        }
                        scanBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Invalid ticket or server error.");
                        statusLabel.setText("Scan failed.");
                        scanBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // 3. Pay Logic
        payCreditBtn.setOnAction(e -> {
            if (ccField.getText().isEmpty() || expField.getText().isEmpty() || cvvField.getText().isEmpty()) {
                showAlert("Error", "Please enter all credit card details.");
                return;
            }
            paymentBox.setDisable(true);
            statusLabel.setText("Processing CREDIT...");
            double fee = appContext.apiManager.calculateFee(currentTicket.getTicketNumber());
            
            new Thread(() -> {
                try {
                    String res = appContext.apiManager.payTicket(currentTicket.getTicketNumber(), fee, "CREDIT");
                    Platform.runLater(() -> {
                        if (res != null && res.startsWith("Failed:")) {
                            showAlert("Payment Required", res.substring(7).trim());
                            paymentBox.setDisable(false);
                            statusLabel.setText("Payment failed.");
                            statusLabel.setStyle("-fx-text-fill: #d63031;");
                        } else {
                            showAlert("Payment Success", "Your ticket has been validated. You have 15 minutes to exit the parking lot.");
                            statusLabel.setText("Payment complete. Thank you!");
                            statusLabel.setStyle("-fx-text-fill: #00b894;");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showAlert("Payment Failed", ex.getMessage());
                        paymentBox.setDisable(false);
                        statusLabel.setText("Please try again.");
                    });
                }
            }).start();
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
