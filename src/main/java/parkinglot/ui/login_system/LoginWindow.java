package parkinglot.ui.login_system;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import parkinglot.managers.AppContext;
import parkinglot.users.Account;

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

        VBox loginCard = new VBox(20);
        loginCard.setMaxWidth(400);
        loginCard.setPadding(new Insets(35, 40, 35, 40));
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label loginLabel = new Label("Staff Authentication");
        loginLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);

        Button loginButton = new Button("LOG IN");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(45);
        loginButton.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                loginLabel.setText("Missing Credentials");
                loginLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 20px;");
                return;
            }

            loginButton.setDisable(true);
            loginLabel.setText("Verifying...");
            loginLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 20px;");

            new Thread(() -> {
                try {
                    Account account = appContext.apiManager.login(user, pass, false);
                    Platform.runLater(() -> {
                        if (account != null) {
                            appContext.setAccount(account);
                            loginLabel.setText("Success!");
                            loginLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 20px;");
                            // Navigation logic will be added when windows are ready
                        } else {
                            loginLabel.setText("Access Denied");
                            loginLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 20px;");
                            loginButton.setDisable(false);
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        loginLabel.setText("Connection Error");
                        loginLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 20px;");
                        loginButton.setDisable(false);
                    });
                }
            }).start();
        });

        loginCard.getChildren().addAll(loginLabel, usernameField, passwordField, loginButton);
        mainLayout.getChildren().add(loginCard);
        root.getChildren().add(mainLayout);

        appContext.resetToView(root, "Login", 635, 650, false);
    }
}
