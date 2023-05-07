package bg.sofia.uni.fmi.mjt.splitwise.server.logic.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterValidation {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,15}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])*(?=.*[a-z])*(?=.*[A-Z])*(?=\\S+$).{6,}$";

    private static Pattern usernamePattern = Pattern.compile(USERNAME_PATTERN);
    private static Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isUsernameValid(String username) {
        Matcher matcher = usernamePattern.matcher(username);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }
}