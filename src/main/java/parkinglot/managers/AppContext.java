package parkinglot.managers;

import javafx.stage.Stage;
import parkinglot.users.Account;

public class AppContext {
    public final Stage stage;
    public final APIManager apiManager = new APIManager();
    public Account account; 

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
