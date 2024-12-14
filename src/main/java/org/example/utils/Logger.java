package org.example.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class represents a logger that will be used to log messages to the console.
 *
 * It provides different log levels:
 * - INFO: General informational messages.
 * - WARNING: Indicates potential issues.
 * - ERROR: Indicates serious problems.
 */
public class Logger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message.
     *
     * @param message The message to log.
     */
    public void logInfo(String message) {
        System.out.println("[INFO] " + getCurrentTimeStamp() + " - " + message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     */
    public void logWarning(String message) {
        System.out.println("[WARNING] " + getCurrentTimeStamp() + " - " + message);
    }

    /**
     * Logs an error message.
     *
     * @param message The message to log.
     */
    public void logError(String message) {
        System.err.println("[ERROR] " + getCurrentTimeStamp() + " - " + message);
    }

    /**
     * Gets the current timestamp in a readable format.
     *
     * @return The current timestamp as a string.
     */
    private String getCurrentTimeStamp() {
        return LocalDateTime.now().format(formatter);
    }
}