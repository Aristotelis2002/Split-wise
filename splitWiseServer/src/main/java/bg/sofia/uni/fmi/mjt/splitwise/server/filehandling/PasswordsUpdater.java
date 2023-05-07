package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.ServerLogger;

public class PasswordsUpdater {
    private static final Path PASSWORDS_FILE_PATH = Path.of("users/PASSWORDS_FILE");
    private static final Path PASSWORDS_BACKUP_PATH = Path.of("users/PASSWORDS_BACKUP");

    public PasswordsUpdater() throws UnrecoverableException {
        initialize();
    }

    public synchronized Map<String, Integer> getMap() throws UnrecoverableException {
        Map<String, Integer> data = null;

        try (InputStream fileInputStream = Files.newInputStream(PASSWORDS_FILE_PATH);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            data = (Map<String, Integer>) objectInputStream.readObject();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt load passwords in local adress space. IO exception");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load passwords in local adress space. IO exception", e);
        } catch (ClassNotFoundException e) {
            ServerLogger.getInstance().log("Couldnt load passwords in local adress space");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt load passwords in local adress space", e);
        }
        return data;
    }

    public synchronized void updateFile(Map<String, Integer> data) throws UnrecoverableException {
        backup();

        try (OutputStream fileOutputStream = Files.newOutputStream(PASSWORDS_FILE_PATH);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt update passwords file");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt update passwords file", e);
        }
    }

    private void backup() throws UnrecoverableException {
        try (OutputStream fileOutputStream = Files.newOutputStream(PASSWORDS_BACKUP_PATH);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(getMap());
            objectOutputStream.flush();
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldnt update passwords backup");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt update passwords backup file", e);
        }
    }

    private void initialize() throws UnrecoverableException {
        try {
            if (!Files.exists(PASSWORDS_FILE_PATH.getParent())) {
                Files.createDirectory(PASSWORDS_FILE_PATH.getParent());
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create passwords folder");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt create passwords folder", e);
        }

        try {
            if (!Files.exists(PASSWORDS_FILE_PATH)) {
                Files.createFile(PASSWORDS_FILE_PATH);
                try (OutputStream fileOutputStream = Files.newOutputStream(PASSWORDS_FILE_PATH);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

                    HashMap<String, Integer> table = new HashMap<>();
                    table.put("admin", "123456".hashCode()); // hard coded admin
                    objectOutputStream.writeObject(table);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    ServerLogger.getInstance().log("Couldn't init passwords file");
                    ServerLogger.getInstance().log(e);

                    throw new UnrecoverableException("Couldnt init passwords file", e);
                }
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create passwords file");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt create passwords file", e);
        }

        try {
            if (!Files.exists(PASSWORDS_BACKUP_PATH)) {
                Files.createFile(PASSWORDS_BACKUP_PATH);
            }
        } catch (IOException e) {
            ServerLogger.getInstance().log("Couldn't create passwords backup file");
            ServerLogger.getInstance().log(e);

            throw new UnrecoverableException("Couldnt create passwords backup file", e);
        }
    }
}
