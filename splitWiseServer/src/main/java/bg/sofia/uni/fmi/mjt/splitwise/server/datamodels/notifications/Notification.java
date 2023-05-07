package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.notifications;

import java.io.Serializable;

public record Notification(String groupName, String body) implements Serializable {
}
