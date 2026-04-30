package parkinglot.managers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import parkinglot.models.ParkingLot;
import parkinglot.users.Account;

public class AppContext {
    public final Stage stage;
    public final APIManager apiManager = new APIManager();
    public Account account; // Currently logged-in user

    public static TextFormatter<String> getUppercaseFormatter() {
        return new TextFormatter<>((TextFormatter.Change change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        });
    }
    
    // --- Admin UI State (Mainly used in Admin Panel) ---
    private final ObjectProperty<ParkingLot> parkingLot = new SimpleObjectProperty<>();
    private final javafx.collections.ObservableList<Account> accounts = javafx.collections.FXCollections.observableArrayList();

    public AppContext(Stage stage) {
        this.stage = stage;
    }

    public ObjectProperty<ParkingLot> parkingLotProperty() { return parkingLot; }
    public ParkingLot getParkingLot() { return parkingLot.get(); }
    public void setParkingLot(ParkingLot lot) { this.parkingLot.set(lot); }

    public javafx.collections.ObservableList<Account> getAccounts() { return accounts; }

    /**
     * Synchronizes the application state with the server in the background.
     * Updates the parkingLot property and accounts list once fetched.
     */
    public void syncData() {
        new Thread(() -> {
            try {
                ParkingLot lot = apiManager.getStatus();
                java.util.List<Account> accountList = apiManager.getAllAccounts();

                javafx.application.Platform.runLater(() -> {
                    setParkingLot(lot);
                    getAccounts().setAll(accountList);
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Synchronization failed: " + e.getMessage());
            }
        }).start();
    }

    private final java.util.Stack<javafx.scene.Parent> history = new java.util.Stack<>();

    public void setAccount(Account account){
        this.account = account;
    }
    public void logOut(){
        setAccount(null);
        apiManager.clearToken();
    }

    /**
     * Pushes a new view onto the navigation stack and displays it.
     */
    public void pushView(javafx.scene.Parent view, String title, double width, double height, boolean resizable) {
        history.push(view);
        updateStage(view, title, width, height, resizable);
    }

    /**
     * Clears the navigation history and sets a new root view.
     */
    public void resetToView(javafx.scene.Parent view, String title, double width, double height, boolean resizable) {
        history.clear();
        history.push(view);
        updateStage(view, title, width, height, resizable);
    }

    /**
     * Navigates back to the previous view in the stack.
     */
    public void goBack(String title, double width, double height, boolean resizable) {
        if (history.size() > 1) {
            history.pop();
            updateStage(history.peek(), title, width, height, resizable);
        }
    }

    private void updateStage(javafx.scene.Parent root, String title, double width, double height, boolean resizable) {
        stage.setTitle(title);
        
        if (stage.getScene() == null) {
            stage.setScene(new javafx.scene.Scene(root, width, height));
        } else {
            stage.getScene().setRoot(root);
            // Adjust window size if it differs significantly or if we are switching modes
            stage.setWidth(width);
            stage.setHeight(height);
        }
        
        stage.setResizable(resizable);
        stage.centerOnScreen();
        if (!stage.isShowing()) stage.show();
    }
}
