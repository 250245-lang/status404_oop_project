package parkinglot.ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingTicket;

public class TicketsTab {
    private final AppContext appContext;
    private final ObservableList<ParkingTicket> masterData = FXCollections.observableArrayList();
    private final FilteredList<ParkingTicket> filteredData = new FilteredList<>(masterData, p -> true);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final CheckBox activeOnly = new CheckBox("Active Only");

    public TicketsTab(AppContext appContext) {
        this.appContext = appContext;
        setupDataBinding();
    }

    private void setupDataBinding() {
        appContext.parkingLotProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(newVal.getAllTickets());
                });
            }
        });
        if (appContext.getParkingLot() != null) {
            masterData.setAll(appContext.getParkingLot().getAllTickets());
        }
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Tickets Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);
        statusFilter.getItems().setAll("ALL", "ACTIVE", "PAID", "COMPLETED");
        statusFilter.setValue("ALL");

        searchField.textProperty().addListener(obs -> updatePredicate());
        statusFilter.valueProperty().addListener(obs -> updatePredicate());
        activeOnly.selectedProperty().addListener(obs -> updatePredicate());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        
        filterBar.getChildren().addAll(new Label("Filter:"), searchField, statusFilter, activeOnly, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, deleteBtn);

        TableView<ParkingTicket> ticketTable = new TableView<>(filteredData);
        TableColumn<ParkingTicket, String> idCol = new TableColumn<>("Ticket Number");
        idCol.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        TableColumn<ParkingTicket, String> vehicleCol = new TableColumn<>("Vehicle License");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleLicense"));
        TableColumn<ParkingTicket, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        ticketTable.getColumns().addAll(idCol, vehicleCol, statusCol);
        ticketTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(ticketTable, Priority.ALWAYS);

        // Fixed Double-Click Handler
        ticketTable.setRowFactory(tv -> {
            TableRow<ParkingTicket> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ParkingTicket ticket = row.getItem();
                    showTicketInfo(ticket);
                }
            });
            return row;
        });

        deleteBtn.setOnAction(e -> {
            ParkingTicket selected = ticketTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete ticket: " + selected.getTicketNumber() + "?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                appContext.apiManager.deleteTicket(selected.getTicketNumber());
                                javafx.application.Platform.runLater(() -> appContext.apiManager.syncData());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    }
                });
            }
        });

        root.getChildren().addAll(title, filterBar, ticketTable);
        return root;
    }

    private void showTicketInfo(ParkingTicket ticket) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Ticket Details");
        info.setHeaderText("Ticket #" + ticket.getTicketNumber());
        info.setContentText("License Plate: " + ticket.getVehicleLicense() + "\n" +
                           "Entry Time: " + ticket.getEntryTime() + "\n" +
                           "Status: " + ticket.getStatus());
        info.show();
    }

    private void updatePredicate() {
        filteredData.setPredicate(ticket -> {
            String search = searchField.getText();
            if (search != null && !search.isEmpty()) {
                String lower = search.toLowerCase();
                if (!ticket.getTicketNumber().toLowerCase().contains(lower) && !ticket.getVehicleLicense().toLowerCase().contains(lower)) return false;
            }
            String status = statusFilter.getValue();
            if (status != null && !status.equals("ALL")) {
                if (!ticket.getStatus().toString().equals(status)) return false;
            }
            if (activeOnly.isSelected()) {
                if (!ticket.getStatus().toString().equals("ACTIVE")) return false;
            }
            return true;
        });
    }
}
