package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.UsernameEventPair;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.ServerLogger;

public class HistoryWriter extends HistoryConstants implements Runnable {
    private static final UsernameEventPair POISON_PILL = new UsernameEventPair("", "");
    private BlockingQueue<UsernameEventPair> blockingQueue;
    private HistoryReader historyReader;
    private boolean beActive;

    public HistoryWriter(BlockingQueue<UsernameEventPair> blockingQueue, HistoryReader historyReader) {
        this.blockingQueue = blockingQueue;
        this.historyReader = historyReader;
        beActive = true;
    }

    @Override
    public void run() {
        try {
            while (beActive) {
                UsernameEventPair pair;
                pair = blockingQueue.take();

                if (pair == POISON_PILL) { // poison pill is added by stopTask method
                    continue;
                }

                synchronized (historyReader) {
                    appendEvent(pair.username(), pair.event());
                }
            }
        } catch (InterruptedException e) {
            ServerLogger.getInstance().log("History writer thread was interrupted");
            ServerLogger.getInstance().log(e);
        }
    }

    public void stopTask() {
        beActive = false;
        blockingQueue.add(POISON_PILL);
    }

    private void appendEvent(String username, String event) {
        Path usernameHistory = Path.of(HISTORY_FOLDER_STRING.concat("/" + username + ".txt"));

        try {
            if (!Files.exists(usernameHistory)) {
                Files.createFile(usernameHistory);
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create history file " + usernameHistory);
            ServerLogger.getInstance().log(e);
            return;
        }

        append(usernameHistory, event);
    }

    private void append(Path path, String event) {
        try {
            Files.writeString(
                    path,
                    event + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't append to history file" + path);
            ServerLogger.getInstance().log(e);
        }
    }

}
