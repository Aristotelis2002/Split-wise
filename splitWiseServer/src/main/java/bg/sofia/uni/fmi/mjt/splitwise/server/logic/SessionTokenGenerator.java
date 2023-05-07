package bg.sofia.uni.fmi.mjt.splitwise.server.logic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import bg.sofia.uni.fmi.mjt.splitwise.server.logging.ServerLogger;

public class SessionTokenGenerator {
    private static Set<Integer> tokens = new HashSet<>();
    private static final int MAX_TOKENS = 50_000;

    public int generateToken() {
        if (tokens.size() >= MAX_TOKENS - 1) {
            ServerLogger.getInstance().log("Server's capacity for users was exceeded");
            throw new RuntimeException("Server's capacity for users is exceeded");
        }
        int token = ThreadLocalRandom.current().nextInt(1, MAX_TOKENS);
        while (tokens.contains(token)) {
            token = ThreadLocalRandom.current().nextInt(1, MAX_TOKENS);
        }
        tokens.add(token);
        return token;
    }

    public void freeToken(int token) {
        if (!tokens.contains(token)) {
            ServerLogger.getInstance().log("Token couldnt be freed");
        }
        tokens.remove(token);
    }
}
