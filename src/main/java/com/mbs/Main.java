package com.mbs;

import com.mbs.ui.Dialogs;
import com.mbs.ui.LoginView;
import com.mbs.ui.Session;
import com.mbs.ui.Theme;
import com.mbs.util.AppConfig;
import com.mbs.util.AppLogger;
import com.mbs.util.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/** JavaFX application: lifecycle = init() -> start() -> stop(). */
public class Main extends Application {
    private String startupError;

    @Override
    public void init() {
        AppLogger.get().info("Application starting (init)");
        try {
            DatabaseManager.init();
        } catch (SQLException e) {
            startupError = "Database error: " + e.getMessage();
            AppLogger.get().severe(startupError);
        }
    }

    @Override
    public void start(Stage stage) {
        Theme.loadFonts();
        Scene scene = new Scene(new LoginView().build(), 1200, 760);
        scene.getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        Session.init(stage, scene);
        Session.setHostServices(getHostServices());
        stage.setTitle(AppConfig.get("app.title", "Theatrely"));
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
        if (startupError != null) {
            Dialogs.error(startupError);
        }
    }

    @Override
    public void stop() {
        AppLogger.get().info("Application closed (stop)");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
