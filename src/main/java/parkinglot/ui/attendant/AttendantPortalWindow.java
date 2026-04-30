package parkinglot.ui.attendant;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.constants.VehicleType;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingTicket;
import parkinglot.ui.components.TopBar;

public class AttendantPortalWindow {
    private final AppContext appContext;

    public AttendantPortalWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        TopBar topBar = new TopBar(appContext, "Parking Attendant Portal");
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(buildIssuePanel(), buildPaymentPanel());
        splitPane.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(splitPane);

        appContext.resetToView(root, "Parking Attendant Portal", 1000, 700, true);
        new Thread(() -> appContext.syncData()).start();
    }

    private Node buildIssuePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f4f7f6;");
        Label title = new Label("MANUAL TICKET ISSUANCE");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #b2bec3;");

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TextField licenseField = new TextField();
        licenseField.setPromptText("License Plate");
        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);

        Button issueBtn = new Button("ISSUE TICKET");
        issueBtn.setMaxWidth(Double.MAX_VALUE);
        issueBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label status = new Label();

        issueBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    ParkingTicket ticket = appContext.apiManager.issueTicket(licenseField.getText(), typeCombo.getValue());
                    Platform.runLater(() -> {
                        if (ticket != null) {
                            status.setText("Ticket Issued: " + ticket.getTicketNumber());
                            licenseField.clear();
                        } else {
                            status.setText("No spots available!");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> status.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        card.getChildren().addAll(new Label("License Plate:"), licenseField, new Label("Vehicle Type:"), typeCombo, issueBtn, status);
        panel.getChildren().addAll(title, card);
        return panel;
    }

    private Node buildPaymentPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f4f7f6;");
        Label title = new Label("PROCESS ON-SITE PAYMENT");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #b2bec3;");

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TextField ticketField = new TextField();
        ticketField.setPromptText("Ticket Number");
        Button payBtn = new Button("PROCESS CASH PAYMENT");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");

        Label status = new Label();

        payBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    double fee = appContext.apiManager.calculateFee(ticketField.getText());
                    String res = appContext.apiManager.payTicket(ticketField.getText(), fee, "CASH");
                    Platform.runLater(() -> status.setText(res));
                } catch (Exception ex) {
                    Platform.runLater(() -> status.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        card.getChildren().addAll(new Label("Ticket Number:"), ticketField, payBtn, status);
        panel.getChildren().addAll(title, card);
        return panel;
    }
}
