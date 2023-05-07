package bg.sofia.uni.fmi.mjt.splitwise.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.UsernameEventPair;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.DataBaseUpdater;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.HistoryReader;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.HistoryWriter;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.PasswordsUpdater;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.HistoryLogger;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.PeriodicFileUpdater;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.Server;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.SessionTokenGenerator;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.command.CommandExecutor;

public class ServerMain {
    private static final int PORT = 6757;
    private static final int INTERVAL_FOR_UPDATES = 5;
    private static final int INITIAL_DELAY_SEC = 1;

    public static void main(String[] args) throws UnrecoverableException {
        PasswordsUpdater passwordsHandler = new PasswordsUpdater();
        DataBaseUpdater dbUpdater = new DataBaseUpdater();
        SessionTokenGenerator tokenGenerator = new SessionTokenGenerator();

        Map<Integer, String> sessionUsernames = new HashMap<>();
        Map<String, Integer> userPasswords = passwordsHandler.getMap();
        Map<String, User> users = dbUpdater.getMap();

        BlockingQueue<UsernameEventPair> blQueue = new LinkedBlockingQueue<>();
        HistoryLogger.initializeLogger(blQueue);

        HistoryWriter historyWriter = new HistoryWriter(blQueue, HistoryReader.getInstance());
        new Thread(historyWriter).start();

        PeriodicFileUpdater periodicFileUpdater = new PeriodicFileUpdater(userPasswords,
                users, passwordsHandler, dbUpdater);

        CommandExecutor cmdExec = new CommandExecutor(sessionUsernames, userPasswords, users);

        Server server = new Server(PORT, tokenGenerator, sessionUsernames, cmdExec);
        cmdExec.setCurrentServer(server);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(periodicFileUpdater, INITIAL_DELAY_SEC,
            INTERVAL_FOR_UPDATES, TimeUnit.SECONDS);
        server.start();
        historyWriter.stopTask();
        executorService.schedule(periodicFileUpdater, INTERVAL_FOR_UPDATES, TimeUnit.MILLISECONDS);
        executorService.close();
    }
}
