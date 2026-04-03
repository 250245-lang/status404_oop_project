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
        TabPane tabPane = new TabPane();
        
        Tab dashboardTab = new Tab("Dashboard", new DashboardTab(appContext).getContent());
        Tab floorTab = new Tab("Floors", new FloorManagerTab(appContext).getContent());
        Tab userTab = new Tab("Users", createPlaceholder("User Management"));

        tabPane.getTabs().addAll(dashboardTab, floorTab, userTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

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
