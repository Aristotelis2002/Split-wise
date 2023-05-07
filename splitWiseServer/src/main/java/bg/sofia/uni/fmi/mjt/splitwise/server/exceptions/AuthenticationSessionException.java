package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class AuthenticationSessionException extends Exception {

    public AuthenticationSessionException(String message) {
        super(message);
    }

    public AuthenticationSessionException(String message, Throwable cause) {
        super(message, cause);
    }

}
