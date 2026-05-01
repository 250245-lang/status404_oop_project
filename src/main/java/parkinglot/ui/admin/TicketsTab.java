package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import parkinglot.managers.AppContext;
import parkinglot.models.ParkingTicket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketsTab {
    private final AppContext appContext;
    private final Runnable onRefresh;

    public TicketsTab(AppContext appContext, Runnable onRefresh) {
        this.appContext = appContext;
        this.onRefresh = onRefresh;
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("Parking Tickets Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        javafx.scene.control.CheckBox activeOnlyCheckbox = new javafx.scene.control.CheckBox("Show Active Tickets Only");
        activeOnlyCheckbox.setSelected(true);
        activeOnlyCheckbox.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        // --- Ticket Table Container ---
        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setMaxHeight(Double.MAX_VALUE);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        TableView<ParkingTicket> ticketTable = new TableView<>();
        ticketTable.setStyle("-fx-background-insets: 0;");
        VBox.setVgrow(ticketTable, Priority.ALWAYS);

        // Columns
        TableColumn<ParkingTicket, String> idCol = new TableColumn<>("Ticket Number");
        idCol.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));

        TableColumn<ParkingTicket, String> vehicleCol = new TableColumn<>("Vehicle License");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleLicense"));

        TableColumn<ParkingTicket, String> spotCol = new TableColumn<>("Assigned Spot");
        spotCol.setCellValueFactory(new PropertyValueFactory<>("spotNumber"));

        TableColumn<ParkingTicket, LocalDateTime> issuedCol = new TableColumn<>("Entry Time");
        issuedCol.setCellValueFactory(new PropertyValueFactory<>("issuedAt"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        issuedCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        TableColumn<ParkingTicket, Enum<?>> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Enum<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item.toString().equals("ACTIVE")) {
                        setStyle("-fx-text-fill: #e17055; -fx-font-weight: bold;");
                    } else if (item.toString().equals("PAID")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2d3436; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<ParkingTicket, Double> amountCol = new TableColumn<>("Amount Paid ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("payedAmount"));

        ticketTable.getColumns().addAll(idCol, vehicleCol, spotCol, issuedCol, statusCol, amountCol);
        ticketTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableContainer.getChildren().add(ticketTable);
        root.getChildren().addAll(title, activeOnlyCheckbox, tableContainer);

        // Data binding
        Runnable refreshTable = () -> {
            if (appContext.getParkingLot() != null) {
                Platform.runLater(() -> {
                    if (activeOnlyCheckbox.isSelected()) {
                        ticketTable.setItems(FXCollections.observableArrayList(appContext.getParkingLot().getActiveTickets()));
                    } else {
                        ticketTable.setItems(FXCollections.observableArrayList(appContext.getParkingLot().getAllTickets()));
                    }
                });
            }
        };

        appContext.parkingLotProperty().addListener((obs, oldLot, newLot) -> refreshTable.run());
        activeOnlyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> refreshTable.run());

        refreshTable.run();

        return root;
    }
}
