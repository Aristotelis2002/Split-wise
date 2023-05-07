package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel;

public class UsernameNotFoundException extends Exception {

    public UsernameNotFoundException(String message) {
        super(message);
    }

    public UsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
