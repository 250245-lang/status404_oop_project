package parkinglot.ui.admin;

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

    public TicketsTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Tickets Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Search and Filters
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by Ticket or License...");
        searchField.setPrefWidth(250);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().setAll("ALL STATUSES", "ACTIVE", "PAID", "COMPLETED");
        statusFilter.setValue("ALL STATUSES");

        CheckBox activeOnly = new CheckBox("Active Only");
        activeOnly.setSelected(false);

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, statusFilter, activeOnly);

        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        TableView<ParkingTicket> ticketTable = new TableView<>();
        
        TableColumn<ParkingTicket, String> idCol = new TableColumn<>("Ticket Number");
        idCol.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        
        TableColumn<ParkingTicket, String> vehicleCol = new TableColumn<>("Vehicle License");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleLicense"));
        
        TableColumn<ParkingTicket, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        ticketTable.getColumns().addAll(idCol, vehicleCol, statusCol);
        ticketTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(ticketTable, Priority.ALWAYS);

        tableContainer.getChildren().add(ticketTable);
        root.getChildren().addAll(title, filterBar, tableContainer);

        return root;
    }
}
