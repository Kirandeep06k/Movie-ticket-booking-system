package com.mbs.ui;

import com.mbs.model.Person;
import com.mbs.service.BookingService;
import com.mbs.service.CatalogService;
import com.mbs.service.UserService;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Holds the window, the logged-in user and the shared services (static members). */
public final class Session {
    public static final UserService USERS = new UserService();
    public static final CatalogService CATALOG = new CatalogService();
    public static final BookingService BOOKINGS = new BookingService();

    private static Stage stage;
    private static Scene scene;
    private static Person user;
    private static HostServices hostServices;
    private static String state;
    private static String city;

    private Session() { }

    public static void init(Stage s, Scene sc) { stage = s; scene = sc; }
    public static Stage stage() { return stage; }
    public static Person user() { return user; }
    public static void setUser(Person p) { user = p; }
    public static boolean isAdmin() { return user != null && "ADMIN".equals(user.getRole()); }
    public static void setRoot(Parent root) { scene.setRoot(root); }
    public static String state() { return state; }
    public static String city() { return city; }
    public static String cityLabel() { return city == null ? "Select city" : city; }
    public static void setLocation(String s, String c) { state = s; city = c; }
    public static void setHostServices(HostServices hs) { hostServices = hs; }

    /** Opens a web link in the user's default browser (used for trailers). */
    public static void openLink(String url) {
        if (hostServices == null) {
            Dialogs.error("Cannot open the browser from here. Link: " + url);
        } else {
            hostServices.showDocument(url);
        }
    }
}
