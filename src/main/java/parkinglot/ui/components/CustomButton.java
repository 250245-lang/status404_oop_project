package parkinglot.ui.components;

import javafx.scene.control.Button;

public class CustomButton extends Button {
    
    public CustomButton(String text, String color) {
        super(text);
        setPrefSize(85, 35);
        setStyle("-fx-background-color: " + color + "; -fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;");
        
        setOnMouseEntered(e -> setOpacity(0.8));
        setOnMouseExited(e -> setOpacity(1.0));
    }
}
