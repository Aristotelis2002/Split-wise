package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.ServerLogger;

public class DataBaseUpdater {
    private static final Path DATA_BASE_FILE_PATH = Path.of("database/DATA_BASE");
    private static final Path DATA_BASE_USER_GROUPS = Path.of("database/USER_GROUPS");
    private static final Path DATA_BASE_BACKUP_PATH = Path.of("database/DATA_BASE_BACKUP");

    public DataBaseUpdater() {
        initialize();
    }

    public synchronized Map<String, User> getMap() throws UnrecoverableException {
        Map<String, User> data = null;

        try (InputStream fileInputStream = Files.newInputStream(DATA_BASE_FILE_PATH);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            data = (Map<String, User>) objectInputStream.readObject();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt load database in local adress space. IO exception");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load database in local adress space. IO exception", e);
        } catch (ClassNotFoundException e) {
            ServerLogger.getInstance().log("Couldnt load database in local adress space");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load database in local adress space", e);
        }

        Map<String, Collection<Group>> res = getUserGroups();
        for (String username : res.keySet()) {
            data.get(username).setGroups(res.get(username));
        }

        return data;
    }

    public synchronized void updateFile(Map<String, User> data) throws UnrecoverableException {
        backup();

        try (OutputStream fileOutputStream = Files.newOutputStream(DATA_BASE_FILE_PATH);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt update database file");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt update database file", e);
        }

        try (OutputStream fileOutputStream = Files.newOutputStream(DATA_BASE_USER_GROUPS);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(makeUserGroups(data));
            objectOutputStream.flush();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt update database file");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt update database file", e);
        }

    }

    private Map<String, ArrayList<Group>> makeUserGroups(Map<String, User> data) {
        Map<String, ArrayList<Group>> res = new HashMap<>();
        data.entrySet().forEach(e -> res.put(e.getKey(),
                new ArrayList<>(e.getValue().getGroups())));
        return res;
    }

    private synchronized Map<String, Collection<Group>> getUserGroups() throws UnrecoverableException {
        Map<String, Collection<Group>> res = null;

        try (InputStream fileInputStream = Files.newInputStream(DATA_BASE_USER_GROUPS);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            res = (Map<String, Collection<Group>>) objectInputStream.readObject();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt load groups database in local adress space. IO exception");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load groups database in local adress space. IO exception", e);
        } catch (ClassNotFoundException e) {
            ServerLogger.getInstance().log("Couldnt load database in local adress space");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load database in local adress space", e);
        }

        return res;
    }

    private void backup() throws UnrecoverableException {
        try (OutputStream fileOutputStream = Files.newOutputStream(DATA_BASE_BACKUP_PATH);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(getMap());
            objectOutputStream.flush();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt update database backup");
            ServerLogger.getInstance().log(e);
        }
    }

    private void initialize() {
        try {
            if (!Files.exists(DATA_BASE_FILE_PATH.getParent())) {
                Files.createDirectory(DATA_BASE_FILE_PATH.getParent());
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create database folder");
            ServerLogger.getInstance().log(e);
        }

        try {
            if (!Files.exists(DATA_BASE_FILE_PATH)) {
                Files.createFile(DATA_BASE_FILE_PATH);
                try (OutputStream fileOutputStream = Files.newOutputStream(DATA_BASE_FILE_PATH);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

                    HashMap<String, User> dataBase = new HashMap<>();
                    dataBase.put("admin", new User("admin"));
                    objectOutputStream.writeObject(dataBase);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    ServerLogger.getInstance()
                            .log("IO exception when initializing database file");
                    ServerLogger.getInstance().log(e);
                }
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("IO exception when creating database file");
            ServerLogger.getInstance().log(e);
        }

        try {
            if (!Files.exists(DATA_BASE_USER_GROUPS)) {
                Files.createFile(DATA_BASE_USER_GROUPS);
                try (OutputStream fileOutputStream = Files.newOutputStream(DATA_BASE_USER_GROUPS);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

                    HashMap<String, ArrayList<Group>> dataBase = new HashMap<>();
                    objectOutputStream.writeObject(dataBase);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    ServerLogger.getInstance()
                            .log("IO exception when initializing database user/group file");
                    ServerLogger.getInstance().log(e);
                }
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("IO exception when creating database file");
            ServerLogger.getInstance().log(e);
        }

        try {
            if (!Files.exists(DATA_BASE_BACKUP_PATH)) {
                Files.createFile(DATA_BASE_BACKUP_PATH);
            }
        } catch (IOException e) {
            ServerLogger.getInstance()
                    .log("IO exception when creating database backup file");
            ServerLogger.getInstance().log(e);
        }
    }
}
