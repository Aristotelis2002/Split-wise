package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import bg.sofia.uni.fmi.mjt.splitwise.server.logging.ServerLogger;

public class HistoryReader extends HistoryConstants {
    private static HistoryReader historyReader = null;

    private HistoryReader() {
        if (!Files.exists(HISTORY_FOLDER.getFileName())) {
            ServerLogger.getInstance().log("History folder missing during reader initialization");
        }
    }

    public static HistoryReader getInstance() {
        if (historyReader == null) {
            historyReader = new HistoryReader();
        }
        return historyReader;
    }

    public synchronized List<String> readLines(String username, int linesToRead) {
        Path usernameHistory = Path.of(HISTORY_FOLDER_STRING.concat("/" + username + ".txt"));

        if (!Files.exists(usernameHistory)) {
            return new LinkedList<>();
        }

        LinkedList<String> buffer = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(usernameHistory.toFile()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.addLast(line);
                if (buffer.size() > linesToRead) {
                    buffer.removeFirst();
                }
            }

        } catch (FileNotFoundException e) {
            ServerLogger.getInstance().log("History file missing during reading");
            ServerLogger.getInstance().log(e);
        } catch (IOException e) {
            ServerLogger.getInstance().log("IOexception for reader");
            ServerLogger.getInstance().log(e);
        }

        return buffer;
    }

}
