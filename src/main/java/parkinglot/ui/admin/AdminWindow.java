package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.ui.login_system.LoginWindow;
import parkinglot.ui.login_system.ProfileWindow;

public class AdminWindow {
    private final AppContext appContext;
    private final Stage stage;

    public AdminWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = appContext.stage;
    }

    public void show() {
        stage.setTitle("Admin Panel - Parking Lot System");

        TabPane tabPane = new TabPane();
        
        // Real tabs
        Tab dashboardTab = new Tab("Dashboard", new DashboardTab(appContext).getContent());
        Tab floorTab = new Tab("Floors", new FloorManagerTab(appContext, this::refreshData).getContent());
        Tab userTab = new Tab("Users", new UsersTab(appContext, this::refreshData).getContent());
        Tab ticketsTab = new Tab("Tickets", new TicketsTab(appContext, this::refreshData).getContent());
        Tab rateTab = new Tab("Rates", new RatesTab(appContext, this::refreshData).getContent());

        tabPane.getTabs().addAll(dashboardTab, floorTab, userTab, ticketsTab, rateTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Reusable TopBar
        parkinglot.ui.components.TopBar topBar = new parkinglot.ui.components.TopBar(appContext, "Admin Management Portal");

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(topBar);
        root.setPadding(new Insets(0, 0, 10, 0));

        appContext.resetToView(root, "Admin Panel - Parking Lot System", 1000, 700, true);
        
        // Initial data fetch
        refreshData();

        // Auto-refresh Dashboard if selected
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> javafx.application.Platform.runLater(() -> {
            System.out.println("Auto syncing...");
            System.out.println(tabPane.getSelectionModel());
            System.out.println(tabPane.getSelectionModel().getSelectedItem());
            if (tabPane.getScene() != null && (
                    tabPane.getSelectionModel().getSelectedItem() == dashboardTab ||
                            tabPane.getSelectionModel().getSelectedItem() == ticketsTab
            )) {
                refreshData();
            } else if (tabPane.getScene() == null) {
                System.out.println("Auto syncing stop...");
                scheduler.shutdown();
            }
        }), 3, 3, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void refreshData() {
        appContext.syncData();
    }

    private Node createPlaceholder(String text) {
        StackPane pane = new StackPane(new Label(text));
        pane.setAlignment(Pos.CENTER);
        return pane;
    }
}
