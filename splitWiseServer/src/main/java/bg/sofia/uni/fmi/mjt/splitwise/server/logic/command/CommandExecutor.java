package bg.sofia.uni.fmi.mjt.splitwise.server.logic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.CommandValidationException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendAlreadyAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendNotAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupSizeException;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.AuthenticationSessionException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.Server;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validators.RegisterValidation;

public class CommandExecutor {
    private static final String ADMIN_USERNAME = "admin";
    private static final Pattern patternDouble = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Pattern patternInteger = Pattern.compile("\\d+");
    private final Map<Integer, String> sessionUsernames; // pair of token and username
    private final Map<String, Integer> userPasswordsTable; // pair of user and password
    private final Map<String, User> usersTable; // pair of username and its properties
    private Server currentServer;

    public CommandExecutor(Map<Integer, String> sessionUsernames, Map<String, Integer> userPasswordsTable,
            Map<String, User> usersTable) {
        this.sessionUsernames = sessionUsernames;
        this.userPasswordsTable = userPasswordsTable;
        this.usersTable = usersTable;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

    public String execute(Command cmd, int sessionToken) {
        try {
            CommandOptionals cmdEnum = CommandOptionals.fromString(cmd.command());

            return switch (cmdEnum) {
                case REGISTER -> register(cmd.args());
                case LOGIN -> login(cmd.args(), sessionToken);
                case LOGOUT -> logout(sessionToken);

                case HELP -> CommandPallete.HELP_MESSAGE;
                case ADD_FRIEND -> addFriend(cmd.args(), sessionToken);
                case CREATE_GROUP -> createGroup(cmd.args(), sessionToken);

                case SPLIT -> split(cmd.args(), sessionToken);
                case SPLIT_GROUP -> splitGroup(cmd.args(), sessionToken);
                case GET_STATUS -> getStatus(sessionToken);

                case PAYED -> payed(cmd.args(), sessionToken);
                case GET_HISTORY -> getHistory(cmd.args(), sessionToken);

                case DISCONNECT -> CommandPallete.DISCONNECT_MESSAGE;
                case SHUTDOWN_SERVER -> shutdown(sessionToken);
                default -> CommandPallete.UNKNOWN_MESSAGE;
            };

        } catch (IllegalArgumentException e) {
            return CommandPallete.UNKNOWN_MESSAGE;
        } catch (CommandValidationException | AuthenticationSessionException e) {
            return e.getMessage();
        }
    }

    private String register(String args[]) throws CommandValidationException {
        assertArguments(args, CommandOptionals.REGISTER.getArgumentsCount());

        if (!RegisterValidation.isUsernameValid(args[0])) {
            throw new CommandValidationException("Username is not a valid string");
        }
        if (!RegisterValidation.isPasswordValid(args[1])) {
            throw new CommandValidationException("Password must be at least 6 symbols and not contain white spaces");
        }

        assertUsernameDoesntExist(args[0]);

        userPasswordsTable.put(args[0], args[1].hashCode());
        usersTable.put(args[0], new User(args[0]));
        return "Registration was successful";
    }

    private String login(String args[], int sessionToken)
            throws CommandValidationException, AuthenticationSessionException {
        assertArguments(args, CommandOptionals.LOGIN.getArgumentsCount());
        assertNoAuthentication(sessionToken);
        if (!userPasswordsTable.containsKey(args[0]) || !userPasswordsTable.get(args[0]).equals(args[1].hashCode())) {
            return "Incorrect password or username";
        }

        sessionUsernames.put(sessionToken, args[0]);
        StringBuilder loginMessage = new StringBuilder("Login successful" + System.lineSeparator());
        loginMessage.append(usersTable.get(args[0]).getNotifications());
        return loginMessage.toString();
    }

    private String logout(int sessionToken) throws AuthenticationSessionException {
        assertAuthentication(sessionToken);
        sessionUsernames.put(sessionToken, null);
        return "Logout successful";
    }

    private String addFriend(String[] args, int sessionToken)
            throws CommandValidationException {
        assertLoggedIn(sessionToken);
        assertArguments(args, CommandOptionals.ADD_FRIEND.getArgumentsCount());
        assertUsernameExists(args[0]);

        String currentUsername = sessionUsernames.get(sessionToken);
        assertNotEqual(currentUsername, args[0], "You can not add yourself as a friend");

        try {
            usersTable.get(currentUsername).addFriend(args[0],
                    usersTable.get(args[0]));
        } catch (FriendAlreadyAddedException e) {
            return e.getMessage();
        }
        return args[0] + " was added successfuly to your friend list";
    }

    private String createGroup(String[] args, int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);
        assertGroupSize(args);
        for (int i = 1; i < args.length; i++) {
            assertUsernameExists(args[i]);
        }

        String currentUsername = sessionUsernames.get(sessionToken);
        List<User> members = new ArrayList<>(
                List.of(Arrays.copyOfRange(args, 1, args.length)).stream()
                        .distinct()
                        .map(s -> usersTable.get(s))
                        .toList());
        members.add(usersTable.get(currentUsername));

        try {
            usersTable.get(currentUsername).createGroup(args[0], members);
        } catch (GroupAlreadyExistsException | GroupSizeException e) {
            return e.getMessage();
        }
        return "Group created successfuly";
    }

    private String split(String[] args, int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);
        assertMinArguments(args, CommandOptionals.SPLIT.getArgumentsCount());
        assertDouble(args[0]);
        assertUsernameExists(args[1]);

        String currentUsername = sessionUsernames.get(sessionToken);
        String reason = List.of(Arrays.copyOfRange(args, 2, args.length)).stream()
                .collect(Collectors.joining(" "));

        try {
            usersTable.get(currentUsername).splitFriend(
                    usersTable.get(args[1]), Double.valueOf(args[0]), reason);
        } catch (FriendNotAddedException e) {
            return e.getMessage();
        }
        return "Bill splitted succesfully";
    }

    private String splitGroup(String[] args, int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);
        assertMinArguments(args, CommandOptionals.SPLIT_GROUP.getArgumentsCount());
        assertDouble(args[0]);

        String currentUsername = sessionUsernames.get(sessionToken);
        String reason = List.of(Arrays.copyOfRange(args, 2, args.length)).stream()
                .collect(Collectors.joining(" "));

        try {
            usersTable.get(currentUsername).splitGroup(args[1], Double.valueOf(args[0]), reason);
        } catch (GroupNotFoundException e) {
            return e.getMessage();
        }
        return "Bill splitted succesfully";
    }

    private String getStatus(int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);
        String currentUsername = sessionUsernames.get(sessionToken);
        return usersTable.get(currentUsername).getStatus();
    }

    private String payed(String args[], int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);
        assertArguments(args, CommandOptionals.PAYED.getArgumentsCount());
        assertDouble(args[0]);
        assertUsernameExists(args[1]);

        String currentUsername = sessionUsernames.get(sessionToken);
        assertNotEqual(currentUsername, args[1], "You can not pay to yourself");

        return usersTable.get(currentUsername).acceptPayment(
                usersTable.get(args[1]), Double.valueOf(args[0]));
    }

    private String getHistory(String[] args, int sessionToken) throws CommandValidationException {
        assertLoggedIn(sessionToken);

        String currentUsername = sessionUsernames.get(sessionToken);
        int linesOfHistory;

        if (args.length == 0) {
            linesOfHistory = CommandPallete.DEFAULT_HISTORY_LINES;
        } else {
            assertArguments(args, CommandOptionals.GET_HISTORY.getArgumentsCount());
            assertInteger(args[0]);
            linesOfHistory = Integer.valueOf(args[0]);
        }

        List<String> list = usersTable.get(currentUsername)
                .getHistory(linesOfHistory);
        if (list.isEmpty()) {
            return "No history to show";
        }

        return list.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private String shutdown(int sessionToken) {
        try {
            assertAuthentication(sessionToken);
        } catch (AuthenticationSessionException e) {
            return e.getMessage();
        }

        if (!sessionUsernames.get(sessionToken).equals(ADMIN_USERNAME)) {
            return "Unauthorized action";
        }

        currentServer.stop();
        return "Server is shutting down";
    }

    private void assertLoggedIn(int sessionToken) throws CommandValidationException {
        if (sessionUsernames.get(sessionToken) == null) {
            throw new CommandValidationException("You are not logged in yet");
        }
    }

    private void assertUsernameExists(String username) throws CommandValidationException {
        if (!usersTable.containsKey(username)) {
            throw new CommandValidationException("Account with the username " +
                    username + " does not exist");
        }
    }

    private void assertAuthentication(int sessionToken) throws AuthenticationSessionException {
        if (sessionUsernames.get(sessionToken) == null) {
            throw new AuthenticationSessionException("You are not logged in your account");
        }
    }

    private void assertNoAuthentication(int sessionToken) throws AuthenticationSessionException {
        if (sessionUsernames.get(sessionToken) != null) {
            throw new AuthenticationSessionException("You are already logged in to an account");
        }
    }

    private void assertUsernameDoesntExist(String username) throws CommandValidationException {
        if (usersTable.containsKey(username)) {
            throw new CommandValidationException("Account with the username " +
                    username + " already exists");
        }
    }

    private void assertMinArguments(String[] args, int minNum) throws CommandValidationException {
        if (args.length < minNum) {
            throw new CommandValidationException("Too few arguments");
        }
    }

    private void assertGroupSize(String[] args) throws CommandValidationException {
        if (args.length < CommandOptionals.CREATE_GROUP.getArgumentsCount()) {
            throw new CommandValidationException("Too few arguments");
        }
    }

    private void assertArguments(String[] args, int expected) throws CommandValidationException {
        if (args.length < expected) {
            throw new CommandValidationException("Too few arguments");
        }

        if (args.length > expected) {
            throw new CommandValidationException("Too many arguments");
        }
    }

    private void assertDouble(String num) throws CommandValidationException {
        Matcher matcher = patternDouble.matcher(num);
        if (!matcher.matches()) {
            throw new CommandValidationException("Amount argument is invalid");
        }
    }

    private void assertInteger(String num) throws CommandValidationException {
        Matcher matcher = patternInteger.matcher(num);
        if (!matcher.matches()) {
            throw new CommandValidationException("Lines argument is not an integer");
        }
    }

    private void assertNotEqual(String username1, String username2, String errMsg) throws CommandValidationException {
        if (username1.equals(username2)) {
            throw new CommandValidationException(errMsg);
        }
    }
}
