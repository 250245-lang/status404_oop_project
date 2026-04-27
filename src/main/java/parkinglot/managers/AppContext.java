package parkinglot.managers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import parkinglot.models.ParkingLot;
import parkinglot.users.Account;
import java.util.Stack;

public class AppContext {
    public final Stage stage;
    public final APIManager apiManager = new APIManager();
    private Account account;
    
    private final ObjectProperty<ParkingLot> parkingLot = new SimpleObjectProperty<>();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final Stack<Parent> history = new Stack<>();

    public AppContext(Stage stage) {
        this.stage = stage;
        this.apiManager.setAppContext(this);
    }

    public ObjectProperty<ParkingLot> parkingLotProperty() { return parkingLot; }
    public ParkingLot getParkingLot() { return parkingLot.get(); }
    public void setParkingLot(ParkingLot lot) { this.parkingLot.set(lot); }
    public ObservableList<Account> getAccounts() { return accounts; }

    public void setAccount(Account account) { this.account = account; }
    public Account getAccount() { return account; }

    public void resetToView(Parent view, String title, double width, double height, boolean resizable) {
        history.clear();
        history.push(view);
        updateStage(view, title, width, height, resizable);
    }

    public void pushView(Parent view, String title, double width, double height, boolean resizable) {
        history.push(view);
        updateStage(view, title, width, height, resizable);
    }

    public void goBack(String title, double width, double height, boolean resizable) {
        if (history.size() > 1) {
            history.pop();
            updateStage(history.peek(), title, width, height, resizable);
        }
    }

    private void updateStage(Parent root, String title, double width, double height, boolean resizable) {
        stage.setTitle(title);
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, width, height));
        } else {
            stage.getScene().setRoot(root);
            stage.setWidth(width);
            stage.setHeight(height);
        }
        stage.setResizable(resizable);
        stage.centerOnScreen();
        if (!stage.isShowing()) stage.show();
    }
}
