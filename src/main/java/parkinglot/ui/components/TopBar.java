package parkinglot.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parkinglot.managers.AppContext;

public class TopBar extends BorderPane {
    private final AppContext appContext;

    public TopBar(AppContext appContext, String title) {
        this.appContext = appContext;
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        this.setLeft(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        
        this.setPadding(new Insets(10, 20, 10, 20));
        this.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
    }
}
