package parkinglot;

import javafx.application.Application;
import javafx.stage.Stage;
import parkinglot.managers.AppContext;
import parkinglot.ui.login_system.LoginWindow;

// Start the Application from the Launcher class
public class Main extends Application {
    private AppContext appContext;

    @Override
    public void start(Stage primaryStage) {
        appContext = new AppContext(primaryStage);
        new LoginWindow(appContext).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


