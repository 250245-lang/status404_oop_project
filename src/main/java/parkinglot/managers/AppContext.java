package parkinglot.managers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import parkinglot.models.ParkingLot;
import parkinglot.users.Account;

public class AppContext {
    public final Stage stage;
    public final APIManager apiManager = new APIManager();
    public Account account; 
    
    private final ObjectProperty<ParkingLot> parkingLot = new SimpleObjectProperty<>();

    public AppContext(Stage stage) {
        this.stage = stage;
    }

    public void setAccount(Account account){
        this.account = account;
    }

    public void logOut(){
        setAccount(null);
        apiManager.clearToken();
    }

    public ObjectProperty<ParkingLot> parkingLotProperty() { return parkingLot; }
    public ParkingLot getParkingLot() { return parkingLot.get(); }
    public void setParkingLot(ParkingLot lot) { this.parkingLot.set(lot); }

    public void resetToView(javafx.scene.Parent view, String title, double width, double height, boolean resizable) {
        stage.setTitle(title);
        if (stage.getScene() == null) {
            stage.setScene(new javafx.scene.Scene(view, width, height));
        } else {
            stage.getScene().setRoot(view);
            stage.setWidth(width);
            stage.setHeight(height);
        }
        stage.setResizable(resizable);
        stage.centerOnScreen();
        if (!stage.isShowing()) stage.show();
    }
}
