package bg.sofia.uni.fmi.mjt.splitwise.server.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ServerLogger {
    private static ServerLogger serverLogger = null;
    private static final Path SERVER_LOGS_PATH = Path.of("logs/SERVER_LOGS.log");

    private ServerLogger() {
        try {
            if (!Files.exists(SERVER_LOGS_PATH.getParent())) {
                Files.createDirectory(SERVER_LOGS_PATH.getParent());
            }
        } catch (IOException e) {
            System.err.println(String.format(
                    "Couldn't create logs directory: %s",
                    e));
        }

        try {
            if (!Files.exists(SERVER_LOGS_PATH)) {
                Files.createFile(SERVER_LOGS_PATH);
            }
        } catch (IOException e) {
            System.err.println(String.format(
                    "Couldn't create server logs file: %s",
                    e));
        }
    }

    public static ServerLogger getInstance() {
        if (ServerLogger.serverLogger == null) {
            ServerLogger.serverLogger = new ServerLogger();
        }

        return serverLogger;
    }

    public void log(String message) {
        try {
            Files.writeString(
                    SERVER_LOGS_PATH,
                    message + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(String.format(
                    "Couldn't append to logs file: %s",
                    e));
        }
    }

    public void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        try {
            Files.writeString(
                    SERVER_LOGS_PATH,
                    sw.toString() + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException exception) {
            System.err.println(String.format(
                    "Couldn't append to logs file: %s",
                    exception));
        }
    }
}
