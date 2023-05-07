package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels;

import java.io.Serializable;

public record UsernameEventPair(String username, String event) implements Serializable {
}
