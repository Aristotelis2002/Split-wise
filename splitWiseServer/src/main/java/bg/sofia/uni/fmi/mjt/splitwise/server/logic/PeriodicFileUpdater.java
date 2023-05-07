package bg.sofia.uni.fmi.mjt.splitwise.server.logic;

import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.DataBaseUpdater;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.PasswordsUpdater;

public class PeriodicFileUpdater extends Thread {
    private final Map<String, Integer> userPasswords;
    private final Map<String, User> users;
    private final PasswordsUpdater passFileHandler;
    private final DataBaseUpdater dbUpdater;

    public PeriodicFileUpdater(Map<String, Integer> userPasswords,
            Map<String, User> users, PasswordsUpdater passFileHandler,
            DataBaseUpdater dbUpdater) {
        this.userPasswords = userPasswords;
        this.users = users;
        this.passFileHandler = passFileHandler;
        this.dbUpdater = dbUpdater;
    }

    @Override
    public void run() {
        try {
            updateFiles();
        } catch (UnrecoverableException e) {
            throw new RuntimeException("Periodic file updater thread suffered an " +
                    "unrecovable exception. Check logs", e);
        }
    }

    private void updateFiles() throws UnrecoverableException {
        passFileHandler.updateFile(userPasswords);
        dbUpdater.updateFile(users);
    }

    public void stopTask() throws UnrecoverableException {
        updateFiles();
    }
}
