package com.mbs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads app.properties (properties configuration file). */
public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("Could not read app.properties, using defaults: " + e.getMessage());
        }
    }

    private AppConfig() { }

    public static String get(String key, String def) { return PROPS.getProperty(key, def); }

    public static int getInt(String key, int def) {
        try {
            return Integer.parseInt(PROPS.getProperty(key, String.valueOf(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double getDouble(String key, double def) {
        try {
            return Double.parseDouble(PROPS.getProperty(key, String.valueOf(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
