package parkinglot.ui.login_system;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parkinglot.managers.AppContext;
import parkinglot.models.Location;
import parkinglot.users.Account;
import parkinglot.users.Person;

public class ProfileWindow {
    private final AppContext appContext;

    public ProfileWindow(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("My Profile Settings");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Account current = appContext.getAccount();
        Person person = current.getPerson();
        if (person == null) person = new Person("", null, "", "");
        Location loc = person.address();
        if (loc == null) loc = new Location("", "", "", "", "");

        // Personal Info Card
        VBox personCard = new VBox(15);
        personCard.setPadding(new Insets(20));
        personCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField(person.name());
        TextField emailField = new TextField(person.email());
        TextField streetField = new TextField(loc.streetAddress());
        
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Street:"), 0, 2);
        grid.add(streetField, 1, 2);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    Location newLoc = new Location(streetField.getText(), loc.city(), loc.state(), loc.zipcode(), loc.country());
                    Person p = new Person(nameField.getText(), newLoc, emailField.getText(), person.phone());
                    appContext.apiManager.updatePerson(current.getUsername(), p);
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Profile updated!").show());
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Update failed").show());
                }
            }).start();
        });

        personCard.getChildren().addAll(new Label("PERSONAL INFORMATION"), grid, saveBtn);

        // Security Card
        VBox securityCard = new VBox(15);
        securityCard.setPadding(new Insets(20));
        securityCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        PasswordField passField = new PasswordField();
        Button passBtn = new Button("Change Password");
        passBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        passBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    appContext.apiManager.changePassword(passField.getText());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Password changed!").show());
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to change password").show());
                }
            }).start();
        });

        securityCard.getChildren().addAll(new Label("SECURITY"), new Label("New Password:"), passField, passBtn);

        Button backBtn = new Button("Back to Portal");
        backBtn.setOnAction(e -> appContext.goBack("Management Portal", 1000, 700, true));

        root.getChildren().addAll(title, personCard, securityCard, backBtn);
        appContext.pushView(root, "My Profile", 600, 600, false);
    }
}
