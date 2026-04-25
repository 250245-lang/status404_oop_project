package parkinglot.ui.components;

import javafx.application.Platform;
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
import parkinglot.ui.hardware.EntrancePanelWindow;
import parkinglot.ui.hardware.ExitPanelWindow;
import parkinglot.ui.login_system.LoginWindow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TopBar extends BorderPane {
    private final AppContext appContext;
    private final Label clockLabel = new Label();
    private final ScheduledExecutorService clockScheduler = Executors.newSingleThreadScheduledExecutor();

    public TopBar(AppContext appContext, String title) {
        this.appContext = appContext;
        
        VBox leftBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        clockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        leftBox.getChildren().addAll(titleLabel, clockLabel);
        
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button entranceBtn = new Button("Open Entrance");
        entranceBtn.setOnAction(e -> new EntrancePanelWindow(appContext).show());
        
        Button exitBtn = new Button("Open Exit");
        exitBtn.setOnAction(e -> new ExitPanelWindow(appContext).show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> {
            clockScheduler.shutdownNow();
            appContext.apiManager.clearToken();
            appContext.setAccount(null);
            new LoginWindow(appContext).show();
        });

        actions.getChildren().addAll(entranceBtn, exitBtn, logoutBtn);

        this.setLeft(leftBox);
        this.setRight(actions);
        
        BorderPane.setAlignment(leftBox, Pos.CENTER_LEFT);
        
        this.setPadding(new Insets(10, 20, 10, 20));
        this.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        startClock();
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy  HH:mm:ss");
        clockScheduler.scheduleAtFixedRate(() -> {
            String time = LocalDateTime.now().format(formatter);
            Platform.runLater(() -> clockLabel.setText(time));
        }, 0, 1, TimeUnit.SECONDS);
    }
}
