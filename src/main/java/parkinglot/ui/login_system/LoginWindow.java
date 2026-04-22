package parkinglot.ui.login_system;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parkinglot.managers.AppContext;
import parkinglot.ui.admin.AdminWindow;
import parkinglot.users.Account;

public class LoginWindow {
    private final AppContext appContext;

    public LoginWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label("Parking Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #2d3436;");

        VBox form = new VBox(15);
        form.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);

        Button loginBtn = new Button("SIGN IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(45);
        loginBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d63031;");

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            
            loginBtn.setDisable(true);
            errorLabel.setText("Authenticating...");

            new Thread(() -> {
                try {
                    Account account = appContext.apiManager.login(user, pass, false);
                    Platform.runLater(() -> {
                        if (account != null) {
                            appContext.setAccount(account);
                            appContext.apiManager.syncData();
                            new AdminWindow(appContext).show();
                        } else {
                            errorLabel.setText("Invalid credentials");
                            loginBtn.setDisable(false);
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Connection Error");
                        loginBtn.setDisable(false);
                    });
                }
            }).start();
        });

        form.getChildren().addAll(usernameField, passwordField, loginBtn, errorLabel);
        root.getChildren().addAll(title, form);

        appContext.resetToView(root, "Login - Parking Lot System", 450, 500, false);
    }
}
