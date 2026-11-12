package com.mbs.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Alerts and dialogs for user feedback. */
public final class Dialogs {
    private Dialogs() { }

    public static void info(String title, String msg) { show(Alert.AlertType.INFORMATION, title, msg); }
    public static void warn(String msg) { show(Alert.AlertType.WARNING, "Warning", msg); }
    public static void error(String msg) { show(Alert.AlertType.ERROR, "Error", msg); }

    public static boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle("Please confirm");
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private static void show(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
