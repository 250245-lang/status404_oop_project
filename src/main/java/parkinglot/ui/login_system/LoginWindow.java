package parkinglot.ui.login_system;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import parkinglot.managers.AppContext;

public class LoginWindow {

    private final AppContext appContext;

    public LoginWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        StackPane root = new StackPane();
        root.setPadding(new Insets(20, 40, 40, 40));
        root.setStyle("-fx-background-color: #f4f7f6;");

        VBox mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.CENTER);

        Label loginLabel = new Label("Staff Authentication");
        loginLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        mainLayout.getChildren().add(loginLabel);
        root.getChildren().add(mainLayout);

        appContext.resetToView(root, "Login", 635, 650, false);
    }
}
