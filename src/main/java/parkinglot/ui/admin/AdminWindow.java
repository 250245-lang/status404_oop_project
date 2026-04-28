package parkinglot.ui.admin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.ui.components.TopBar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdminWindow {
    private final AppContext appContext;
    private ScheduledExecutorService scheduler;

    public AdminWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        TabPane tabPane = new TabPane();
        Tab dashboardTab = new Tab("Dashboard", new DashboardTab(appContext).getContent());
        Tab floorTab = new Tab("Floors", new FloorManagerTab(appContext).getContent());
        Tab userTab = new Tab("Users", new UsersTab(appContext).getContent());
        Tab rateTab = new Tab("Rates", new RatesTab(appContext).getContent());
        Tab ticketsTab = new Tab("Tickets", new TicketsTab(appContext).getContent());

        tabPane.getTabs().addAll(dashboardTab, floorTab, userTab, rateTab, ticketsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        TopBar topBar = new TopBar(appContext, "Admin Management Portal");

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(topBar);
        root.setPadding(new Insets(0, 0, 10, 0));

        appContext.resetToView(root, "Admin Panel - Parking Lot System", 1000, 700, true);
        
        refreshData();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (tabPane.getScene() != null && tabPane.getScene().getWindow().isShowing()) {
                    if (tabPane.getSelectionModel().getSelectedItem() == dashboardTab ||
                        tabPane.getSelectionModel().getSelectedItem() == ticketsTab) {
                        refreshData();
                    }
                } else {
                    scheduler.shutdown();
                }
            });
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void refreshData() {
        new Thread(() -> appContext.apiManager.syncData()).start();
    }

    private Node createPlaceholder(String text) {
        StackPane pane = new StackPane(new Label(text));
        pane.setAlignment(Pos.CENTER);
        return pane;
    }
}
