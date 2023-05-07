package bg.sofia.uni.fmi.mjt.splitwise.server.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.UsernameEventPair;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.HistoryConstants;

public class HistoryLogger extends HistoryConstants {
    private static HistoryLogger historyLogger = null;
    private BlockingQueue<UsernameEventPair> blockingQueue;

    private HistoryLogger(BlockingQueue<UsernameEventPair> blQueue) {
        this.blockingQueue = blQueue;

        try {
            if (!Files.exists(HISTORY_FOLDER.getFileName())) {
                Files.createDirectory(HISTORY_FOLDER.getFileName());
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create history folder");
            ServerLogger.getInstance().log(e);
        }
    }

    public static void initializeLogger(BlockingQueue<UsernameEventPair> blQueue) {
        historyLogger = new HistoryLogger(blQueue);
    }

    public static HistoryLogger getInstance() {
        if (HistoryLogger.historyLogger == null) {
            ServerLogger.getInstance()
                    .log("History logger was not initialized before being called");
        }

        return historyLogger;
    }

    public void log(UsernameEventPair pair) {
        blockingQueue.add(pair);
    }

}
