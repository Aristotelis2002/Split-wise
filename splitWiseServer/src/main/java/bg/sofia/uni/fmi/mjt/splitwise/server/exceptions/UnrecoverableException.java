package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class UnrecoverableException extends Exception {

    public UnrecoverableException(String message) {
        super(message);
    }

    public UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

}
