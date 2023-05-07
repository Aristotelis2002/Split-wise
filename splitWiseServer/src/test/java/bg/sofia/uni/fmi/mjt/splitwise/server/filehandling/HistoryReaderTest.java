package bg.sofia.uni.fmi.mjt.splitwise.server.filehandling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.UnrecoverableException;

public class HistoryReaderTest {
    @Test
    void testHistoryReaderInitializer() {
        assertDoesNotThrow(() -> HistoryReader.getInstance());
    }

    @Test
    void testInitializedHistoryReaderReadingLines() throws UnrecoverableException {
        assertDoesNotThrow(() -> HistoryReader.getInstance().readLines("null", 1));
    }
}
