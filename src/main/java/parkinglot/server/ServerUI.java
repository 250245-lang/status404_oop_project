package parkinglot.server;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import parkinglot.managers.AppContext;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;

public class ServerUI {
    private ConfigurableApplicationContext springContext;
    private final AppContext appContext;

    public ServerUI(AppContext appContext) {
        this.appContext = appContext;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Parking Lot Server Control Panel");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'monospace'; -fx-background-color: #1e1e1e; -fx-text-fill: #d4d4d4;");

        PrintStream originalOut = System.out;
        OutputStream textAreaStream = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> logArea.appendText(String.valueOf((char) b)));
            }
        };
        System.setOut(new PrintStream(new CustomOutputStream(originalOut, textAreaStream)));

        Button stopBtn = new Button("Shutdown Server");
        stopBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setOnAction(e -> {
            if (springContext != null) springContext.close();
            stage.close();
        });

        HBox actions = new HBox(stopBtn);
        actions.setPadding(new Insets(10));

        BorderPane root = new BorderPane(logArea);
        root.setBottom(actions);

        stage.setScene(new Scene(root, 700, 500));
        stage.show();

        new Thread(() -> {
            try {
                springContext = SpringApplication.run(ServerApplication.class);
                String ip = InetAddress.getLocalHost().getHostAddress();
                int port = ((WebServerApplicationContext) springContext).getWebServer().getPort();
                System.out.println(">>> Server Online at " + ip + ":" + port);
            } catch (Exception ex) {
                System.err.println("Server start failed: " + ex.getMessage());
            }
        }).start();

        stage.setOnCloseRequest(e -> {
            if (springContext != null) springContext.close();
        });
    }
}
