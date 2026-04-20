package parkinglot.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parkinglot.managers.AppContext;
import parkinglot.ui.hardware.EntrancePanelWindow;
import parkinglot.ui.hardware.ExitPanelWindow;

public class TopBar extends BorderPane {
    private final AppContext appContext;

    public TopBar(AppContext appContext, String title) {
        this.appContext = appContext;
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        HBox simulators = new HBox(10);
        simulators.setAlignment(Pos.CENTER_RIGHT);

        Button entranceBtn = new Button("Open Entrance");
        entranceBtn.setOnAction(e -> new EntrancePanelWindow(appContext).show());
        
        Button exitBtn = new Button("Open Exit");
        exitBtn.setOnAction(e -> new ExitPanelWindow(appContext).show());

        simulators.getChildren().addAll(entranceBtn, exitBtn);

        this.setLeft(titleLabel);
        this.setRight(simulators);
        
        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        
        this.setPadding(new Insets(10, 20, 10, 20));
        this.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
    }
}
