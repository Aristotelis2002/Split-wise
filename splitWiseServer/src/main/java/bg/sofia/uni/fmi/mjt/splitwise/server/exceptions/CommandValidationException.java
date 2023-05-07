package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class CommandValidationException extends Exception {

    public CommandValidationException(String message) {
        super(message);
    }

    public CommandValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
