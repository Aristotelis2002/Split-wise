package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels;

import java.io.Serializable;
import java.util.List;

public record Group(String groupName, List<User> members) implements Serializable {
}
