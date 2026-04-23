package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.ui.components.TopBar;

public class AdminWindow {
    private final AppContext appContext;

    public AdminWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        // Initial data sync
        new Thread(() -> appContext.apiManager.syncData()).start();

        TabPane tabPane = new TabPane();
        Tab dashboardTab = new Tab("Dashboard", new DashboardTab(appContext).getContent());
        Tab floorTab = new Tab("Floors", new FloorManagerTab(appContext).getContent());
        Tab userTab = new Tab("Users", new UsersTab(appContext).getContent());
        Tab rateTab = new Tab("Rates", new RatesTab(appContext).getContent());
        Tab ticketsTab = new Tab("Tickets", new TicketsTab(appContext).getContent());

        tabPane.getTabs().addAll(dashboardTab, floorTab, userTab, rateTab, ticketsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Sync logic: Refresh data when switching tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                new Thread(() -> appContext.apiManager.syncData()).start();
            }
        });

        TopBar topBar = new TopBar(appContext, "Admin Management Portal");

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(topBar);
        root.setPadding(new Insets(0, 0, 10, 0));

        appContext.resetToView(root, "Admin Panel - Parking Lot System", 1000, 700, true);
    }

    private StackPane createPlaceholder(String text) {
        StackPane pane = new StackPane(new Label(text));
        pane.setAlignment(Pos.CENTER);
        return pane;
    }
}
