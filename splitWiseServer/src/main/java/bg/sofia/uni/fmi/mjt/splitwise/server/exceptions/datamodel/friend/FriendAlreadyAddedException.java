package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend;

public class FriendAlreadyAddedException extends Exception {

    public FriendAlreadyAddedException(String message) {
        super(message);
    }

    public FriendAlreadyAddedException(String message, Throwable cause) {
        super(message, cause);
    }

}
