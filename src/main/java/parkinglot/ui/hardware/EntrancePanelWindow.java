package parkinglot.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import parkinglot.constants.VehicleType;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingTicket;

public class EntrancePanelWindow {
    private final AppContext appContext;
    private final Stage stage;

    public EntrancePanelWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = new Stage();
    }

    public void show() {
        stage.setTitle("Entrance Panel Simulator");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d3436;");

        Label title = new Label("Welcome to Educative\nParking");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00b894;");
        title.setTextAlignment(TextAlignment.CENTER);

        Label statusLabel = new Label("Initializing System...");
        statusLabel.setStyle("-fx-text-fill: #dfe6e9; -fx-font-size: 14px;");

        // --- License Plate Input ---
        Label licenseLabel = new Label("Vehicle License Plate");
        licenseLabel.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextField licenseField = new TextField();
        licenseField.setPromptText("Enter License Plate (e.g. ABC-123)");
        licenseField.setPrefHeight(45);
        licenseField.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
        licenseField.setTextFormatter(AppContext.getUppercaseFormatter());
        licenseField.setDisable(true);

        VBox licenseBox = new VBox(5, licenseLabel, licenseField);
        licenseBox.setAlignment(Pos.CENTER_LEFT);

        // --- First Visit Section (initially hidden) ---
        Label firstVisitLabel = new Label("⭐  First visit! Please select your vehicle type.");
        firstVisitLabel.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 13px; -fx-font-weight: bold;");
        firstVisitLabel.setWrapText(true);
        firstVisitLabel.setVisible(false);
        firstVisitLabel.setManaged(false);

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);
        typeCombo.setPrefHeight(45);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-font-size: 14px;");
        typeCombo.setVisible(false);
        typeCombo.setManaged(false);

        // --- Main Button ---
        Button actionBtn = new Button("CHECK & ISSUE TICKET");
        actionBtn.setPrefHeight(70);
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 10; -fx-cursor: hand;");
        actionBtn.setDisable(true);

        root.getChildren().addAll(title, statusLabel, licenseBox, firstVisitLabel, typeCombo, actionBtn);

        Scene scene = new Scene(root, 440, 500);
        stage.setScene(scene);
        stage.show();

        // 1. Authenticate Hardware
        new Thread(() -> {
            try {
                appContext.apiManager.hardwareLogin();
                Platform.runLater(() -> {
                    statusLabel.setText("System Ready");
                    statusLabel.setStyle("-fx-text-fill: #00b894; -fx-font-size: 14px;");
                    licenseField.setDisable(false);
                    actionBtn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Network Error: Cannot connect to server.");
                    statusLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold;");
                });
            }
        }).start();

        // 2. Button Logic — handles both "check" and "confirm" steps
        actionBtn.setOnAction(e -> {
            String license = licenseField.getText().trim();
            if (license.isEmpty()) {
                showAlert("Error", "Please enter a license plate.");
                return;
            }

            // Determine which step we're on
            boolean isConfirmStep = typeCombo.isVisible();
            VehicleType type = isConfirmStep ? typeCombo.getValue() : null;

            actionBtn.setDisable(true);
            statusLabel.setText("Processing...");
            statusLabel.setStyle("-fx-text-fill: #fdcb6e;");

            new Thread(() -> {
                try {
                    ParkingTicket ticket = appContext.apiManager.issueTicket(license, type);
                    Platform.runLater(() -> {
                        // Reset UI
                        firstVisitLabel.setVisible(false);
                        firstVisitLabel.setManaged(false);
                        typeCombo.setVisible(false);
                        typeCombo.setManaged(false);
                        actionBtn.setText("CHECK & ISSUE TICKET");

                        if (ticket != null) {
                            showTicketReceipt(ticket);
                            licenseField.clear();
                            statusLabel.setText("Please take your ticket.");
                            statusLabel.setStyle("-fx-text-fill: #00b894;");
                        } else {
                            showAlert("Parking Full", "Sorry, no available spots for this vehicle type.");
                            statusLabel.setText("PARKING FULL");
                            statusLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 16px;");
                        }
                        actionBtn.setDisable(false);
                    });
                } catch (org.springframework.web.client.HttpClientErrorException.NotFound notFound) {
                    // 404 = VEHICLE_NOT_FOUND — reveal type selection
                    Platform.runLater(() -> {
                        firstVisitLabel.setVisible(true);
                        firstVisitLabel.setManaged(true);
                        typeCombo.setVisible(true);
                        typeCombo.setManaged(true);
                        actionBtn.setText("CONFIRM TYPE & ISSUE");
                        actionBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 10; -fx-cursor: hand;");
                        statusLabel.setText("First visit — select vehicle type.");
                        statusLabel.setStyle("-fx-text-fill: #fdcb6e;");
                        actionBtn.setDisable(false);
                    });
                } catch (org.springframework.web.client.HttpClientErrorException.Conflict conflict) {
                    // 409 = Vehicle already has an active ticket (currently parked)
                    Platform.runLater(() -> {
                        showAlert("Already Parked",
                            "Vehicle " + licenseField.getText().trim() + " is already inside the parking lot.\n" +
                            "Please exit first before issuing a new ticket.");
                        statusLabel.setText("System Ready");
                        statusLabel.setStyle("-fx-text-fill: #00b894;");
                        actionBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to issue ticket: " + ex.getMessage());
                        statusLabel.setText("System Ready");
                        statusLabel.setStyle("-fx-text-fill: #00b894;");
                        actionBtn.setDisable(false);
                    });
                }
            }).start();
        });
    }

    private void showTicketReceipt(ParkingTicket ticket) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Printed Ticket");
        alert.setHeaderText("Welcome to Educative Parking");
        
        String content = String.format(
            "Ticket No: %s\nVehicle: %s\nEntry Time: %s\nAssigned Spot: Spot %s",
            ticket.getTicketNumber(),
            ticket.getVehicleLicense(),
            ticket.getIssuedAt(),
            ticket.getSpotNumber()
        );
        
        alert.setContentText(content);
        alert.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
