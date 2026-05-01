package parkinglot.ui.login_system;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.server.ServerUI;
import parkinglot.ui.admin.AdminWindow;
import parkinglot.users.Account;
import parkinglot.users.Admin;

import java.util.Objects;


public class LoginWindow {

    private final AppContext appContext;
    private final Stage stage;
    private final TextField ipField = new TextField();
    private final TextField portField = new TextField();

    public LoginWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = appContext.stage;
        ipField.setText(appContext.apiManager.getServerAddress().ip);
        portField.setText(String.valueOf(appContext.apiManager.getServerAddress().port));
    }

    public void show() {
        stage.setTitle("Login");

        Node topRight = getTopRightPane();
        StackPane root = new StackPane();
        root.setPadding(new Insets(20, 40, 40, 40));

        if (appContext.apiManager.isLoggedIn()) {
            showAutoLoginState(root, topRight);
        } else {
            showStandardLoginState(root, topRight);
        }

        appContext.resetToView(root, "Login", 635, 650, false);

        // Add icon (only once or every time resetToView is called?)
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login_icon.png")));
        stage.getIcons().setAll(icon);
    }

    private void showAutoLoginState(StackPane root, Node topRight) {
        Label statusLabel = new Label("Restoring session...");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        ProgressIndicator progress = new ProgressIndicator();

        VBox loadingBox = new VBox(20, statusLabel, progress);
        loadingBox.setAlignment(Pos.CENTER);

        root.getChildren().setAll(loadingBox, topRight);
        StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

        autoLogin(root, statusLabel, loadingBox, topRight);
    }

    private void autoLogin(StackPane root, Label statusLabel, VBox loadingBox, Node topRight) {
        new Thread(() -> {
            try {
                Account account = appContext.apiManager.getCurrentAccount();
                if (account != null) {
                    appContext.setAccount(account);
                    Platform.runLater(() -> {
                        if (account instanceof Admin) {
                            new AdminWindow(appContext).show();
                        } else if (account instanceof parkinglot.users.ParkingAttendant) {
                            new parkinglot.ui.attendant.AttendantPortalWindow(appContext).show();
                        }
                    });
                } else {
                    throw new Exception("Session expired or invalid.");
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to restore session.");
                    statusLabel.setTextFill(Color.RED);
                    loadingBox.getChildren().remove(1); // Remove progress indicator

                    Button retryBtn = new Button("Try Again");
                    retryBtn.setOnAction(e -> showAutoLoginState(root, topRight));

                    Button logoutBtn = new Button("Log Out");
                    logoutBtn.setOnAction(e -> {
                        appContext.logOut();
                        showStandardLoginState(root, topRight);
                    });

                    HBox actions = new HBox(10, retryBtn, logoutBtn);
                    actions.setAlignment(Pos.CENTER);
                    loadingBox.getChildren().add(actions);
                });
            }
        }).start();
    }

    private void showStandardLoginState(StackPane root, Node topRight) {
        root.setStyle("-fx-background-color: #f4f7f6;"); // Consistent light gray background

        VBox mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40, 40, 0, 40));

        // --- Login Card ---
        VBox loginCard = new VBox(20);
        loginCard.setMaxWidth(400);
        loginCard.setPadding(new Insets(35, 40, 35, 40));
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

        Label loginLabel = new Label("Staff Authentication");
        loginLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox formBox = new VBox(12);
        
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-background-radius: 5;");

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 12px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 5;");

        CheckBox rememberMeCheckBox = new CheckBox("Stay logged in");
        rememberMeCheckBox.setSelected(true);
        rememberMeCheckBox.setStyle("-fx-text-fill: #95a5a6;");

        formBox.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, rememberMeCheckBox);

        Button loginButton = new Button("LOG IN");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(45);
        loginButton.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;");

        loginCard.getChildren().addAll(loginLabel, formBox, loginButton);

        // --- Simulator Card ---
        VBox simCard = new VBox(15);
        simCard.setMaxWidth(400);
        simCard.setPadding(new Insets(20));
        simCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label simLabel = new Label("HARDWARE SIMULATORS");
        simLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        HBox simButtons = new HBox(10);
        simButtons.setAlignment(Pos.CENTER);

        Button entranceBtn = createSimButton("Entry", "#55efc4");
        entranceBtn.setOnAction(e -> new parkinglot.ui.hardware.EntrancePanelWindow(appContext).show());
        
        Button exitBtn = createSimButton("Exit", "#fab1a0");
        exitBtn.setOnAction(e -> new parkinglot.ui.hardware.ExitPanelWindow(appContext).show());
        
        Button infoBtn = createSimButton("Kiosk", "#81ecec");
        infoBtn.setOnAction(e -> new parkinglot.ui.hardware.CustomerInfoPortalWindow(appContext).show());

        simButtons.getChildren().addAll(entranceBtn, exitBtn, infoBtn);
        simCard.getChildren().addAll(simLabel, simButtons);

        mainLayout.getChildren().addAll(loginCard, simCard);

        root.getChildren().setAll(mainLayout, topRight);
        StackPane.setAlignment(topRight, Pos.TOP_RIGHT);

        // Login Logic
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                loginLabel.setText("Missing Credentials");
                loginLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 20px;");
                return;
            }
            
            loginButton.setDisable(true);
            loginLabel.setText("Verifying...");
            loginLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 20px;");
            
            boolean rememberMe = rememberMeCheckBox.isSelected();
            new Thread(() -> {
                try {
                    Account account = appContext.apiManager.login(username, password, rememberMe);
                    if (account == null) {
                        Platform.runLater(() -> {
                            loginLabel.setText("Access Denied");
                            loginLabel.setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-font-size: 20px;");
                            loginButton.setDisable(false);
                        });
                        return;
                    }
                    appContext.setAccount(account);
                    Platform.runLater(() -> {
                        if (account instanceof Admin) {
                            new AdminWindow(appContext).show();
                        } else if (account instanceof parkinglot.users.ParkingAttendant) {
                            new parkinglot.ui.attendant.AttendantPortalWindow(appContext).show();
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
    }

    private Button createSimButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(85, 35);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }

    private Pane getTopRightPane() {
        ipField.setPromptText("Server IP Addr.");
        ipField.setPrefWidth(120);
        ipField.textProperty().addListener((_, _, newValue) -> {appContext.apiManager.setServerIp(newValue);});

        portField.setPromptText("Port");
        portField.setPrefWidth(50);
        portField.textProperty().addListener((_, _, newValue) -> {
            try{
                appContext.apiManager.setServerPort(Integer.parseInt(newValue));
            } catch (NumberFormatException _) {}
        });

        Hyperlink button = new Hyperlink("Host Server");
        button.setOnMouseClicked(_->{
            new ServerUI(appContext).show();
        });

        HBox hBox = new HBox(10, ipField, portField);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(5, hBox, button);
        content.setAlignment(Pos.TOP_RIGHT);
        content.setVisible(false);
        content.setManaged(false);

        Hyperlink toggleBtn = new Hyperlink("Connect...");
        toggleBtn.setOnAction(e -> {
            boolean isVisible = content.isVisible();
            content.setVisible(!isVisible);
            content.setManaged(!isVisible);
            toggleBtn.setText(isVisible ? "Connect..." : "Hide");
        });

        VBox container = new VBox(5, toggleBtn, content);
        container.setAlignment(Pos.TOP_RIGHT);
        container.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        return container;
    }
}