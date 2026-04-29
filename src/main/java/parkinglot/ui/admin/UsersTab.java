package parkinglot.ui.admin;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import parkinglot.managers.AppContext;
import parkinglot.users.Account;

public class UsersTab {
    private final AppContext appContext;

    public UsersTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("User Accounts Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TableView<Account> userTable = new TableView<>();
        TableColumn<Account, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<Account, String> roleCol = new TableColumn<>("System Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTable.getColumns().addAll(userCol, roleCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        appContext.getAccounts().addListener((javafx.collections.ListChangeListener<? super Account>) c -> {
            userTable.setItems(FXCollections.observableArrayList(appContext.getAccounts()));
        });
        userTable.setItems(FXCollections.observableArrayList(appContext.getAccounts()));

        HBox actions = new HBox(15);
        Button deleteBtn = new Button("Delete Account");
        deleteBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button resetBtn = new Button("Reset Password");
        resetBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        resetBtn.setOnAction(e -> {
            Account selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showResetDialog(selected);
            }
        });

        actions.getChildren().addAll(resetBtn, deleteBtn);
        root.getChildren().addAll(title, userTable, actions);
        return root;
    }

    private void showResetDialog(Account account) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password - " + account.getUsername());
        dialog.setHeaderText("Set a new password for " + account.getUsername());

        ButtonType saveButtonType = new ButtonType("Update Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password");
        passwordField.setPrefWidth(250);
        
        Label hint = new Label("Password should be at least 8 characters.");
        hint.setStyle("-fx-font-size: 10px; -fx-text-fill: #636e72;");

        content.getChildren().addAll(new Label("New Password:"), passwordField, hint);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) return passwordField.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(newPass -> {
            if (!newPass.isEmpty()) {
                new Thread(() -> {
                    try {
                        appContext.apiManager.updatePassword(account.getUsername(), newPass);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
