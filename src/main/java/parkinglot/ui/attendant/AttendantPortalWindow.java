package parkinglot.ui.attendant;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import parkinglot.constants.VehicleType;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingLot;
import parkinglot.models.ParkingTicket;

public class AttendantPortalWindow {

    private final AppContext appContext;

    public AttendantPortalWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        // --- TopBar (reused component) ---
        parkinglot.ui.components.TopBar topBar = new parkinglot.ui.components.TopBar(appContext, "Parking Attendant Portal");

        // --- Split Content ---
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: #f4f7f6;");

        splitPane.getItems().addAll(buildIssuePanel(), buildPaymentPanel());
        splitPane.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(splitPane);

        appContext.resetToView(root, "Parking Attendant Portal", 900, 600, true);

        // Sync data into AppContext so the lot info is fresh
        new Thread(() -> appContext.syncData()).start();
    }

    // ─── Left Panel: Issue Ticket ─────────────────────────────────────────────

    private Node buildIssuePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f4f7f6;");

        Label sectionTitle = new Label("ISSUE TICKET");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        VBox card = new VBox(18);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        // License field
        Label licenseLabel = new Label("License Plate");
        licenseLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField licenseField = new TextField();
        licenseField.setPromptText("e.g. ABC-123");
        licenseField.setPrefHeight(38);
        licenseField.setTextFormatter(AppContext.getUppercaseFormatter());
        VBox licenseBox = new VBox(5, licenseLabel, licenseField);

        // First-visit notice (hidden initially)
        Label firstVisitNote = new Label("⭐  First visit — select vehicle type below.");
        firstVisitNote.setStyle("-fx-text-fill: #e17055; -fx-font-size: 12px; -fx-font-weight: bold;");
        firstVisitNote.setVisible(false);
        firstVisitNote.setManaged(false);

        // Vehicle type
        Label typeLabel = new Label("Vehicle Type");
        typeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        VBox typeBox = new VBox(5, typeLabel, typeCombo);
        typeBox.setVisible(false);
        typeBox.setManaged(false);

        // Status
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #636e72;");
        statusLabel.setWrapText(true);

        // Ticket receipt area (hidden initially)
        VBox receiptBox = new VBox(6);
        receiptBox.setPadding(new Insets(12));
        receiptBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-radius: 6;");
        receiptBox.setVisible(false);
        receiptBox.setManaged(false);

        // Action button
        Button actionBtn = new Button("CHECK & ISSUE TICKET");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setPrefHeight(42);
        actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        card.getChildren().addAll(licenseBox, firstVisitNote, typeBox, statusLabel, receiptBox, actionBtn);
        panel.getChildren().addAll(sectionTitle, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Reset state when license changes
        licenseField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                firstVisitNote.setVisible(false);
                firstVisitNote.setManaged(false);
                typeBox.setVisible(false);
                typeBox.setManaged(false);
                actionBtn.setText("CHECK & ISSUE TICKET");
                actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                statusLabel.setText("");
            }
        });

        // --- Button Logic ---
        actionBtn.setOnAction(e -> {
            String license = licenseField.getText().trim();
            if (license.isEmpty()) { statusLabel.setText("Please enter a license plate."); return; }

            boolean isConfirm = typeBox.isVisible();
            VehicleType type = isConfirm ? typeCombo.getValue() : null;

            actionBtn.setDisable(true);
            statusLabel.setText("Processing...");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #636e72;");
            receiptBox.setVisible(false);
            receiptBox.setManaged(false);

            new Thread(() -> {
                try {
                    ParkingTicket ticket = appContext.apiManager.issueTicket(license, type);
                    Platform.runLater(() -> {
                        // Hide type selector — reset to step 1
                        firstVisitNote.setVisible(false); firstVisitNote.setManaged(false);
                        typeBox.setVisible(false); typeBox.setManaged(false);
                        actionBtn.setText("CHECK & ISSUE TICKET");
                        actionBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

                        if (ticket != null) {
                            statusLabel.setText("✅  Ticket issued successfully.");
                            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00b894; -fx-font-weight: bold;");
                            showReceipt(receiptBox, ticket);
                            licenseField.clear();
                        } else {
                            statusLabel.setText("❌  No spots available for this vehicle type.");
                            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031;");
                        }
                        actionBtn.setDisable(false);
                    });
                } catch (org.springframework.web.client.HttpClientErrorException.NotFound notFound) {
                    Platform.runLater(() -> {
                        firstVisitNote.setVisible(true); firstVisitNote.setManaged(true);
                        typeBox.setVisible(true); typeBox.setManaged(true);
                        actionBtn.setText("CONFIRM & ISSUE");
                        actionBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                        statusLabel.setText("First visit — select vehicle type and confirm.");
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e17055;");
                        actionBtn.setDisable(false);
                    });
                } catch (org.springframework.web.client.HttpClientErrorException.Conflict conflict) {
                    // 409 = vehicle is already parked
                    Platform.runLater(() -> {
                        statusLabel.setText("\u26a0\ufe0f  This vehicle is already inside the lot.");
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031; -fx-font-weight: bold;");
                        actionBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: " + ex.getMessage());
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031;");
                        actionBtn.setDisable(false);
                    });
                }
            }).start();
        });

        return panel;
    }

    private void showReceipt(VBox receiptBox, ParkingTicket ticket) {
        receiptBox.getChildren().clear();
        Label h = new Label("TICKET ISSUED");
        h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");
        receiptBox.getChildren().addAll(
                h,
                receiptRow("Ticket No.", ticket.getTicketNumber()),
                receiptRow("License",    ticket.getVehicleLicense()),
                receiptRow("Spot",       ticket.getSpotNumber()),
                receiptRow("Entry Time", ticket.getIssuedAt().toString())
        );
        receiptBox.setVisible(true);
        receiptBox.setManaged(true);
    }

    // ─── Right Panel: Process Payment ────────────────────────────────────────

    private Node buildPaymentPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f4f7f6;");

        Label sectionTitle = new Label("PROCESS PAYMENT");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        VBox card = new VBox(18);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        // Ticket number field
        Label ticketLabel = new Label("Ticket Number");
        ticketLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField ticketField = new TextField();
        ticketField.setPromptText("Enter Ticket No.");
        ticketField.setPrefHeight(38);
        ticketField.setTextFormatter(AppContext.getUppercaseFormatter());
        VBox ticketBox = new VBox(5, ticketLabel, ticketField);

        Button lookupBtn = new Button("Look Up Ticket");
        lookupBtn.setMaxWidth(Double.MAX_VALUE);
        lookupBtn.setPrefHeight(38);
        lookupBtn.setStyle("-fx-background-color: #636e72; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        // Ticket info box (shown after lookup)
        VBox ticketInfoBox = new VBox(6);
        ticketInfoBox.setPadding(new Insets(12));
        ticketInfoBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-radius: 6;");
        ticketInfoBox.setVisible(false);
        ticketInfoBox.setManaged(false);

        // Amount field
        Label amountLabel = new Label("Amount ($)");
        amountLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField amountField = new TextField("15.00");
        amountField.setPrefHeight(38);
        VBox amountBox = new VBox(5, amountLabel, amountField);
        amountBox.setVisible(false);
        amountBox.setManaged(false);

        // Payment method
        Label methodLabel = new Label("Payment Method");
        methodLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        ToggleGroup methodGroup = new ToggleGroup();
        RadioButton cashBtn = new RadioButton("Cash");
        cashBtn.setToggleGroup(methodGroup);
        cashBtn.setSelected(true);
        RadioButton creditBtn = new RadioButton("Credit Card");
        creditBtn.setToggleGroup(methodGroup);
        HBox methodRow = new HBox(20, cashBtn, creditBtn);
        methodRow.setAlignment(Pos.CENTER_LEFT);
        VBox methodBox = new VBox(5, methodLabel, methodRow);
        methodBox.setVisible(false);
        methodBox.setManaged(false);

        // Credit card fields (shown when credit is selected)
        VBox ccBox = new VBox(10);
        TextField ccField = new TextField();
        ccField.setPromptText("Card Number (e.g. 1234-5678-9012-3456)");
        HBox ccDetails = new HBox(10);
        TextField expField = new TextField();
        expField.setPromptText("MM/YY");
        expField.setPrefWidth(80);
        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");
        cvvField.setPrefWidth(70);
        ccDetails.getChildren().addAll(expField, cvvField);
        ccBox.getChildren().addAll(ccField, ccDetails);
        ccBox.setVisible(false);
        ccBox.setManaged(false);

        creditBtn.selectedProperty().addListener((obs, oldVal, selected) -> {
            ccBox.setVisible(selected);
            ccBox.setManaged(selected);
        });

        // Pay button
        Button payBtn = new Button("PROCESS PAYMENT");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setPrefHeight(42);
        payBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        payBtn.setVisible(false);
        payBtn.setManaged(false);

        Label payStatusLabel = new Label("");
        payStatusLabel.setStyle("-fx-font-size: 12px;");
        payStatusLabel.setWrapText(true);

        card.getChildren().addAll(ticketBox, lookupBtn, ticketInfoBox,
                amountBox, methodBox, ccBox, payBtn, payStatusLabel);
        panel.getChildren().addAll(sectionTitle, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Track current ticket
        final ParkingTicket[] currentTicket = {null};

        // --- Look Up Logic ---
        lookupBtn.setOnAction(e -> {
            String tNo = ticketField.getText().trim();
            if (tNo.isEmpty()) { payStatusLabel.setText("Please enter a ticket number."); return; }

            lookupBtn.setDisable(true);
            new Thread(() -> {
                try {
                    ParkingTicket t = appContext.apiManager.getTicket(tNo);
                    double fee = t.isPaid() ? t.getPayedAmount() : appContext.apiManager.calculateFee(tNo);
                    Platform.runLater(() -> {
                        currentTicket[0] = t;
                        ticketInfoBox.getChildren().clear();
                        Label h = new Label("TICKET DETAILS");
                        h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b2bec3;");
                        ticketInfoBox.getChildren().addAll(h,
                                receiptRow("Ticket No.", t.getTicketNumber()),
                                receiptRow("License",    t.getVehicleLicense()),
                                receiptRow("Spot",       t.getSpotNumber()),
                                receiptRow("Status",     t.getStatus().toString()),
                                receiptRow("Duration",   t.getParkingDurationMinutes() + " min"),
                                receiptRow("Fee Due",    String.format("$%.2f", fee)));
                        ticketInfoBox.setVisible(true);
                        ticketInfoBox.setManaged(true);

                        if (t.isPaid()) {
                            payStatusLabel.setText("✅  This ticket is already paid.");
                            payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00b894; -fx-font-weight: bold;");
                            amountBox.setVisible(false); amountBox.setManaged(false);
                            methodBox.setVisible(false); methodBox.setManaged(false);
                            ccBox.setVisible(false); ccBox.setManaged(false);
                            payBtn.setVisible(false); payBtn.setManaged(false);
                        } else {
                            payStatusLabel.setText("");
                            amountField.setText(String.format("%.2f", fee));
                            amountBox.setVisible(true); amountBox.setManaged(true);
                            methodBox.setVisible(true); methodBox.setManaged(true);
                            payBtn.setVisible(true); payBtn.setManaged(true);
                            payBtn.setDisable(false); // Always reset — may still be disabled from a previous payment
                        }
                        lookupBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        payStatusLabel.setText("Ticket not found: " + ex.getMessage());
                        payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031;");
                        lookupBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // --- Pay Logic ---
        payBtn.setOnAction(e -> {
            if (currentTicket[0] == null) return;
            String method = creditBtn.isSelected() ? "CREDIT" : "CASH";
            if (method.equals("CREDIT") && (ccField.getText().isEmpty() || expField.getText().isEmpty() || cvvField.getText().isEmpty())) {
                payStatusLabel.setText("Please fill in all credit card details.");
                payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031;");
                return;
            }
            double amount;
            try { amount = Double.parseDouble(amountField.getText().trim()); }
            catch (NumberFormatException ex) { payStatusLabel.setText("Invalid amount."); return; }

            payBtn.setDisable(true);
            payStatusLabel.setText("Processing...");

            new Thread(() -> {
                try {
                    String result = appContext.apiManager.payTicket(currentTicket[0].getTicketNumber(), amount, method);
                    Platform.runLater(() -> {
                        if (result != null && result.startsWith("Failed:")) {
                            payStatusLabel.setText("❌  " + result.substring(7).trim());
                            payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031; -fx-font-weight: bold;");
                            payBtn.setDisable(false);
                        } else {
                            payStatusLabel.setText("✅  " + result);
                            payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00b894; -fx-font-weight: bold;");
                            payBtn.setVisible(false); payBtn.setManaged(false);
                            amountBox.setVisible(false); amountBox.setManaged(false);
                            methodBox.setVisible(false); methodBox.setManaged(false);
                            ccBox.setVisible(false); ccBox.setManaged(false);
                            ticketField.clear();
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        payStatusLabel.setText("❌  Payment failed: " + ex.getMessage());
                        payStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d63031;");
                        payBtn.setDisable(false);
                    });
                }
            }).start();
        });

        return panel;
    }

    private HBox receiptRow(String label, String value) {
        Label k = new Label(label + ":");
        k.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-min-width: 90px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        HBox row = new HBox(8, k, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
