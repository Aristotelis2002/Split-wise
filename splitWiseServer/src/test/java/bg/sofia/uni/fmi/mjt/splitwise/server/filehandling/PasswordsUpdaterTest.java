package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;

public class PasswordsUpdaterTest {
    @Test
    void testPasswordsInitializer() {
        assertDoesNotThrow(() -> new PasswordsUpdater());
    }

    @Test
    void testInitializedPasswordsGetting() throws UnrecoverableException {
        var psUpdater = new PasswordsUpdater();
        assertDoesNotThrow(() -> psUpdater.getMap());
    }

    @Test
    void testReadingAndUpdatingPasswords() throws UnrecoverableException {
        var psUpdater = new PasswordsUpdater();
        var map = psUpdater.getMap();
        assertDoesNotThrow(() -> psUpdater.updateFile(map));
    }
}
