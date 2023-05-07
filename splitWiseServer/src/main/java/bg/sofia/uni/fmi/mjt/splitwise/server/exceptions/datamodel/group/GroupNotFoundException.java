package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group;

public class GroupNotFoundException extends Exception {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
