package parkinglot.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parkinglot.managers.AppContext;
import parkinglot.ui.login_system.LoginWindow;
import parkinglot.ui.login_system.ProfileWindow;
import parkinglot.users.Admin;
import parkinglot.users.ParkingAttendant;

public class TopBar extends BorderPane {
    private final AppContext appContext;

    public TopBar(AppContext appContext, String title) {
        this.appContext = appContext;
        
        // Left side: Title/Label to fill the gap
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Right side: User Info & Actions
        VBox rightSide = createRightSide();
        
        this.setLeft(titleLabel);
        this.setRight(rightSide);
        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(rightSide, Pos.CENTER_RIGHT);
        
        this.setPadding(new Insets(10, 20, 10, 20));
        // Simple and neat styling
        this.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
    }

    private VBox createRightSide() {
        String roleName = (appContext.account instanceof Admin) ? "Admin" : "Attendant";

        Label userLabel = new Label("Logged in as: " + appContext.account.getUserName() + " (" + roleName + ")");
        userLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        Button profileBtn = new Button("My Profile");
        profileBtn.setOnAction(e -> new ProfileWindow(appContext).show());

        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> new Thread(() -> appContext.syncData()).start());

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setOnAction(e -> {
            appContext.logOut();
            new LoginWindow(appContext).show();
        });

        HBox actions = new HBox(10, refreshBtn, profileBtn, logoutBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox container = new VBox(5, userLabel, actions);
        container.setAlignment(Pos.TOP_RIGHT);
        return container;
    }
}
