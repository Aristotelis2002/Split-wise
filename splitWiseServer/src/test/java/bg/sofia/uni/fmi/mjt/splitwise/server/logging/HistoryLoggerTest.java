package bg.sofia.uni.fmi.mjt.splitwise.server.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class HistoryLoggerTest {
    @Test
    void testInitializerNotThrowing() {
        assertDoesNotThrow(() -> HistoryLogger.initializeLogger(null),
                "Should initialize with no exceptions");
    }

    @Test
    void testGettingInstanceShouldNotThrow() {
        HistoryLogger.initializeLogger(null);
        assertDoesNotThrow(() -> HistoryLogger.getInstance());
    }
}
