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

        // Search Bar
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by Ticket or License...");
        searchField.setPrefWidth(250);
        
        statusFilter.getItems().setAll("ALL", "ACTIVE", "PAID", "COMPLETED");
        statusFilter.setValue("ALL");

        searchField.textProperty().addListener(obs -> updatePredicate());
        statusFilter.valueProperty().addListener(obs -> updatePredicate());
        activeOnly.selectedProperty().addListener(obs -> updatePredicate());

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, statusFilter, activeOnly);

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

        root.getChildren().addAll(title, filterBar, ticketTable);
        return root;
    }

    private void updatePredicate() {
        filteredData.setPredicate(ticket -> {
            // Search filter
            String search = searchField.getText();
            if (search != null && !search.isEmpty()) {
                String lower = search.toLowerCase();
                boolean matchesSearch = ticket.getTicketNumber().toLowerCase().contains(lower) ||
                                      ticket.getVehicleLicense().toLowerCase().contains(lower);
                if (!matchesSearch) return false;
            }

            // Status filter
            String status = statusFilter.getValue();
            if (status != null && !status.equals("ALL")) {
                if (!ticket.getStatus().toString().equals(status)) return false;
            }

            // Active only filter
            if (activeOnly.isSelected()) {
                if (!ticket.getStatus().toString().equals("ACTIVE")) return false;
            }

            return true;
        });
    }
}
