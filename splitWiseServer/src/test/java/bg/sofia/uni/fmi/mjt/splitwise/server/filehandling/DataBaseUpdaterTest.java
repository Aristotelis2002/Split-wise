package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;

public class DataBaseUpdaterTest {

    @Test
    void testDataBaseInitializer() {
        assertDoesNotThrow(() -> new DataBaseUpdater());
    }

    @Test
    void testInitializedDataBaseGetting() {
        var dbUpdater = new DataBaseUpdater();
        assertDoesNotThrow(() -> dbUpdater.getMap());
    }

    @Test
    void testReadingAndUpdatingDataBase() throws UnrecoverableException {
        var dbUpdater = new DataBaseUpdater();
        var map = dbUpdater.getMap();
        assertDoesNotThrow(() -> dbUpdater.updateFile(map));
    }
}
