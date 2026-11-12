package com.mbs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Logging with java.util.logging (writes to logs/app.log and console). */
public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("Theatrely");
    private static boolean ready = false;

    private AppLogger() { }

    public static synchronized Logger get() {
        if (!ready) {
            try {
                String file = AppConfig.get("log.file", "logs/app.log");
                Path parent = Paths.get(file).getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                FileHandler handler = new FileHandler(file, true);
                handler.setFormatter(new SimpleFormatter());
                LOGGER.addHandler(handler);
            } catch (IOException e) {
                System.err.println("Logging to file disabled: " + e.getMessage());
            }
            ready = true;
        }
        return LOGGER;
    }
}
