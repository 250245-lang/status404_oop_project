package parkinglot.ui.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import parkinglot.constants.AccountStatus;
import parkinglot.managers.AppContext;

public class UsersTab {
    private final AppContext appContext;

    public UsersTab(AppContext appContext) {
        this.appContext = appContext;
    }

    public Node getContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("System User Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // User List
        VBox listContainer = new VBox(15);
        listContainer.setPadding(new Insets(20));
        listContainer.setMinWidth(300);
        listContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label listTitle = new Label("REGISTERED USERS");
        listTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b2bec3;");

        ListView<String> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        Button addNewBtn = new Button("+ Add New User");
        addNewBtn.setMaxWidth(Double.MAX_VALUE);
        addNewBtn.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-font-weight: bold;");

        listContainer.getChildren().addAll(listTitle, listView, addNewBtn);

        // User Detail View
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(25));
        detailContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label detailTitle = new Label("User Details");
        detailTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        TextField usernameField = new TextField();
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().setAll("ADMIN", "ATTENDANT");
        ComboBox<AccountStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().setAll(AccountStatus.values());

        addFormField(form, "Username:", usernameField, 0);
        addFormField(form, "Role:", roleCombo, 1);
        addFormField(form, "Status:", statusCombo, 2);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");

        detailContainer.getChildren().addAll(detailTitle, new Separator(), form, saveBtn);

        splitPane.getItems().addAll(listContainer, detailContainer);
        splitPane.setDividerPositions(0.35);

        container.getChildren().addAll(title, splitPane);
        return container;
    }

    private void addFormField(GridPane grid, String label, Node field, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        if (field instanceof Region) {
            ((Region) field).setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(field, Priority.ALWAYS);
        }
    }
}
