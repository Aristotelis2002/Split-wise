package bg.sofia.uni.fmi.mjt.splitwise.server.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class ServerLoggerTest {

    @Test
    void testServerLoggerConstructorGetInstance() {
        assertDoesNotThrow(() -> ServerLogger.getInstance(),
                "Should be able to create and open file/folders");
    }
}
