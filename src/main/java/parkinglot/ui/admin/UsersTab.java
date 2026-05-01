package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.constants.AccountStatus;
import parkinglot.managers.AppContext;
import parkinglot.models.Location;
import parkinglot.users.ParkingAttendant;
import parkinglot.users.Admin;
import parkinglot.users.Person;

import javafx.application.Platform;
import parkinglot.users.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsersTab {
    private final AppContext appContext;
    private final Runnable onRefresh;
    private final ListView<Account> listView = new ListView<>();

    // Form fields
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final ComboBox<AccountStatus> statusCombo = new ComboBox<>();
    private final ComboBox<String> roleCombo = new ComboBox<>();
    private final TextField streetField = new TextField();
    private final TextField cityField = new TextField();
    private final TextField stateField = new TextField();
    private final TextField zipField = new TextField();

    // Header and Buttons to update labels
    private final Label detailTitle = new Label("User Details");
    private final Button saveBtn = new Button("Save Changes");
    private final Button deleteBtn = new Button("Delete Account");

    private boolean isAddMode = false;

    public UsersTab(AppContext appContext, Runnable onRefresh) {
        this.appContext = appContext;
        this.onRefresh = onRefresh;
        setupListeners();
    }

    private void setupListeners() {
        appContext.getAccounts().addListener((javafx.collections.ListChangeListener<Account>) c -> updateList());
        updateList();
    }

    private void updateList() {
        List<Account> accounts = new ArrayList<>(appContext.getAccounts());

        Platform.runLater(() -> {
            Account selected = listView.getSelectionModel().getSelectedItem();
            listView.getItems().setAll(accounts);

            if (selected != null) {
                accounts.stream()
                        .filter(a -> a.getUserName().equals(selected.getUserName()))
                        .findFirst()
                        .ifPresentOrElse(
                                a -> listView.getSelectionModel().select(a),
                                () -> selectDefault()
                        );
            } else {
                selectDefault();
            }
        });
    }

    private void selectDefault() {
        if (!listView.getItems().isEmpty()) {
            listView.getSelectionModel().select(0);
        }
    }

    public Node getContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("System User Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Left Pane: User List ---
        VBox listContainer = new VBox(15);
        listContainer.setPadding(new Insets(20));
        listContainer.setMinWidth(300);
        listContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label listTitle = new Label("REGISTERED USERS");
        listTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3; -fx-letter-spacing: 1px;");

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    VBox cell = new VBox(2);
                    Label nameLbl = new Label(item.getPerson() != null ? item.getPerson().name() : "N/A");
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436;");
                    Label userLbl = new Label("@" + item.getUserName());
                    userLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #636e72;");
                    cell.getChildren().addAll(nameLbl, userLbl);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String role = (item instanceof Admin) ? "ADMIN" : "STAFF";
                    Label roleLbl = new Label(role);
                    String color = (item instanceof Admin) ? "#0984e3" : "#e17055";
                    roleLbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 2 6; -fx-background-radius: 4;");

                    box.getChildren().addAll(cell, spacer, roleLbl);
                    setGraphic(box);
                }
            }
        });
        listView.setStyle("-fx-background-insets: 0; -fx-padding: 0;");
        listView.setMinHeight(0);
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button addNewBtn = new Button("+ Add New User");
        addNewBtn.setMaxWidth(Double.MAX_VALUE);
        addNewBtn.setPrefHeight(40);
        addNewBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addNewBtn.setOnAction(e -> enterAddMode());

        listContainer.getChildren().addAll(listTitle, listView, addNewBtn);

        // --- Right Pane: Detail View ---
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(25));
        detailContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        detailContainer.setMaxHeight(Double.MAX_VALUE);

        detailTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2d3436;");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);
        form.setPadding(new Insets(10, 0, 10, 0));

        statusCombo.getItems().setAll(AccountStatus.values());
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        roleCombo.getItems().setAll("ADMIN", "ATTENDANT");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        int row = 0;
        addFormField(form, "Username:", usernameField, row++);
        addFormField(form, "Password:", passwordField, row++);
        addFormField(form, "Role:", roleCombo, row++);
        addFormField(form, "Full Name:", nameField, row++);
        addFormField(form, "Email:", emailField, row++);
        addFormField(form, "Phone:", phoneField, row++);
        addFormField(form, "Status:", statusCombo, row++);

        form.add(new Separator(), 0, row, 2, 1);
        row++;

        Label addrLabel = new Label("Address Information");
        addrLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #b2bec3; -fx-font-size: 12px;");
        form.add(addrLabel, 0, row++, 2, 1);

        addFormField(form, "Street:", streetField, row++);
        addFormField(form, "City:", cityField, row++);
        addFormField(form, "State:", stateField, row++);
        addFormField(form, "Zip Code:", zipField, row++);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        saveBtn.setPrefWidth(140);
        saveBtn.setPrefHeight(35);
        saveBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        saveBtn.setOnAction(e -> handleSave());

        deleteBtn.setPrefWidth(140);
        deleteBtn.setPrefHeight(35);
        deleteBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> handleDelete());

        actions.getChildren().addAll(deleteBtn, saveBtn);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: white;");
        formScroll.getStylesheets().add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; } .scroll-pane { -fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; }");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        detailContainer.getChildren().addAll(detailTitle, new Separator(), formScroll, actions);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                exitAddMode(newVal);
            }
        });

        splitPane.getItems().addAll(listContainer, detailContainer);
        splitPane.setDividerPositions(0.35);

        container.getChildren().addAll(title, splitPane);

        return container;
    }

    private void handleSave() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String role = roleCombo.getValue();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        AccountStatus status = statusCombo.getValue();

        Location loc = new Location(
                streetField.getText().trim(),
                cityField.getText().trim(),
                stateField.getText().trim(),
                zipField.getText().trim(),
                "Uzbekistan"
        );

        if (user.isEmpty() || name.isEmpty() || (isAddMode && pass.isEmpty()) || role == null) {
            showError("Validation Error", "Username, Role, Name, and Password (for new users) are required.");
            return;
        }

        Person p = new Person(name, loc, email, phone);
        Account acc;
        if ("ADMIN".equals(role)) {
            acc = new Admin(user, pass, p);
        } else {
            acc = new ParkingAttendant(user, pass, p);
        }
        acc.setStatus(status);

        new Thread(() -> {
            try {
                if (isAddMode) {
                    appContext.apiManager.addAccount(acc);
                } else {
                    appContext.apiManager.updateAccount(acc);
                }
                Platform.runLater(() -> {
                    onRefresh.run();
                    showInfo("Success", isAddMode ? "User created successfully." : "User updated successfully.");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showError("Failed to save user", ex.getMessage()));
            }
        }).start();
    }

    private void handleDelete() {
        Account selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (confirm("Delete Account", "Are you sure you want to delete account: " + selected.getUserName() + "?")) {
            new Thread(() -> {
                try {
                    appContext.apiManager.deleteAccount(selected.getUserName());
                    Platform.runLater(() -> {
                        onRefresh.run();
                        showInfo("Success", "Account deleted successfully.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showError("Failed to delete account", ex.getMessage()));
                }
            }).start();
        }
    }

    private void enterAddMode() {
        isAddMode = true;
        listView.getSelectionModel().clearSelection();

        detailTitle.setText("Add New System User");
        saveBtn.setText("Create Account");
        deleteBtn.setDisable(true);

        usernameField.clear();
        usernameField.setEditable(true);
        passwordField.clear();
        passwordField.setDisable(false);
        roleCombo.setDisable(false);
        roleCombo.setValue("ATTENDANT");
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        statusCombo.setValue(AccountStatus.ACTIVE);
        streetField.clear();
        cityField.clear();
        stateField.clear();
        zipField.clear();

        usernameField.requestFocus();
    }

    private void exitAddMode(Account acc) {
        isAddMode = false;

        detailTitle.setText("User Details: " + acc.getUserName());
        saveBtn.setText("Save Changes");
        deleteBtn.setDisable(acc.getUserName().equals(appContext.account.getUserName())); // Prevent self-deletion

        usernameField.setText(acc.getUserName());
        usernameField.setEditable(false);
        passwordField.clear();
        passwordField.setDisable(true);
        roleCombo.setValue(acc instanceof Admin ? "ADMIN" : "ATTENDANT");
        roleCombo.setDisable(true); // Don't allow changing role for existing users for simplicity

        nameField.setText(acc.getPerson() != null ? acc.getPerson().name() : "");
        emailField.setText(acc.getPerson() != null ? acc.getPerson().email() : "");
        phoneField.setText(acc.getPerson() != null ? acc.getPerson().phone() : "");
        statusCombo.setValue(acc.getStatus());

        if (acc.getPerson() != null && acc.getPerson().address() != null) {
            Location loc = acc.getPerson().address();
            streetField.setText(loc.streetAddress());
            cityField.setText(loc.city());
            stateField.setText(loc.state());
            zipField.setText(loc.zipcode());
        } else {
            streetField.clear();
            cityField.clear();
            stateField.clear();
            zipField.clear();
        }
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }

    private void addFormField(GridPane grid, String label, javafx.scene.Node field, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        if (field instanceof Region) {
            ((Region) field).setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(field, Priority.ALWAYS);
        }
    }
}
