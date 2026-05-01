package parkinglot.ui.login_system;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.models.Location;
import parkinglot.users.Account;
import parkinglot.users.Person;

public class ProfileWindow {
    private final AppContext appContext;
    private final Stage stage;

    public ProfileWindow(AppContext appContext) {
        this.appContext = appContext;
        this.stage = appContext.stage;
    }

    public void show() {
        stage.setTitle("My Profile");

        // Header
        Label titleLabel = new Label("Profile Settings");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Account currentAcc = appContext.account;
        Person currentPerson = currentAcc.getPerson();
        Location currentLoc = currentPerson.address();

        // --- Person Details Section ---
        Label personHeader = new Label("Personal Information");
        personHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        GridPane personGrid = new GridPane();
        personGrid.setHgap(10);
        personGrid.setVgap(10);
        personGrid.setPadding(new Insets(10));

        TextField nameField = new TextField(currentPerson.name());
        TextField emailField = new TextField(currentPerson.email());
        TextField phoneField = new TextField(currentPerson.phone());
        TextField streetField = new TextField(currentLoc != null ? currentLoc.streetAddress() : "");
        TextField cityField = new TextField(currentLoc != null ? currentLoc.city() : "");
        TextField stateField = new TextField(currentLoc != null ? currentLoc.state() : "");
        TextField zipField = new TextField(currentLoc != null ? currentLoc.zipcode() : "");
        TextField countryField = new TextField(currentLoc != null ? currentLoc.country() : "");

        personGrid.add(new Label("Name:"), 0, 0);
        personGrid.add(nameField, 1, 0);
        personGrid.add(new Label("Email:"), 0, 1);
        personGrid.add(emailField, 1, 1);
        personGrid.add(new Label("Phone:"), 0, 2);
        personGrid.add(phoneField, 1, 2);
        personGrid.add(new Label("Street:"), 0, 3);
        personGrid.add(streetField, 1, 3);
        personGrid.add(new Label("City:"), 0, 4);
        personGrid.add(cityField, 1, 4);
        personGrid.add(new Label("State:"), 0, 5);
        personGrid.add(stateField, 1, 5);
        personGrid.add(new Label("Zip:"), 0, 6);
        personGrid.add(zipField, 1, 6);
        personGrid.add(new Label("Country:"), 0, 7);
        personGrid.add(countryField, 1, 7);

        Button updatePersonBtn = new Button("Update Info");
        updatePersonBtn.setPrefWidth(150);
        Label personMsg = new Label();

        VBox personBox = new VBox(10, personHeader, personGrid, updatePersonBtn, personMsg);
        personBox.setPadding(new Insets(15));
        personBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        // --- Password Section ---
        Label passwordHeader = new Label("Security");
        passwordHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        VBox passwordForm = new VBox(10);
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        passwordForm.getChildren().addAll(
                new Label("New Password"), newPasswordField,
                new Label("Confirm Password"), confirmPasswordField
        );

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setPrefWidth(150);
        Label passwordMsg = new Label();

        VBox passwordBox = new VBox(15, passwordHeader, passwordForm, changePasswordBtn, passwordMsg);
        passwordBox.setPadding(new Insets(15));
        passwordBox.setPrefWidth(250);
        passwordBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        // Layout
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> appContext.goBack("Management Portal", 1000, 700, true));

        HBox mainContent = new HBox(30, personBox, passwordBox);
        mainContent.setAlignment(Pos.CENTER);

        VBox root = new VBox(25, titleLabel, mainContent, backBtn);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        appContext.pushView(root, "My Profile", 750, 650, true);

        // --- Actions ---
        updatePersonBtn.setOnAction(e -> {
            updatePersonBtn.setDisable(true);
            personMsg.setText("Updating...");
            personMsg.setStyle("-fx-text-fill: black;");

            new Thread(() -> {
                try {
                    Location loc = new Location(streetField.getText(), cityField.getText(),
                            stateField.getText(), zipField.getText(), countryField.getText());
                    Person p = new Person(nameField.getText(), loc, emailField.getText(), phoneField.getText());
                    appContext.apiManager.updatePerson(currentAcc.getUserName(), p);

                    // Refresh local context
                    Account updatedAcc = appContext.apiManager.getCurrentAccount();
                    appContext.setAccount(updatedAcc);

                    Platform.runLater(() -> {
                        personMsg.setText("Info updated successfully!");
                        personMsg.setStyle("-fx-text-fill: green;");
                        updatePersonBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        personMsg.setText("Error: " + ex.getMessage());
                        personMsg.setStyle("-fx-text-fill: red;");
                        updatePersonBtn.setDisable(false);
                    });
                }
            }).start();
        });

        changePasswordBtn.setOnAction(e -> {
            String pass = newPasswordField.getText();
            String confirm = confirmPasswordField.getText();

            if (pass.isEmpty()) {
                passwordMsg.setText("Password cannot be empty.");
                passwordMsg.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!pass.equals(confirm)) {
                passwordMsg.setText("Passwords do not match.");
                passwordMsg.setStyle("-fx-text-fill: red;");
                return;
            }

            changePasswordBtn.setDisable(true);
            passwordMsg.setText("Updating password...");
            passwordMsg.setStyle("-fx-text-fill: black;");

            new Thread(() -> {
                try {
                    appContext.apiManager.changePassword(pass);
                    Platform.runLater(() -> {
                        passwordMsg.setText("Password changed!");
                        passwordMsg.setStyle("-fx-text-fill: green;");
                        newPasswordField.clear();
                        confirmPasswordField.clear();
                        changePasswordBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        passwordMsg.setText("Error: " + ex.getMessage());
                        passwordMsg.setStyle("-fx-text-fill: red;");
                        changePasswordBtn.setDisable(false);
                    });
                }
            }).start();
        });
    }
}
