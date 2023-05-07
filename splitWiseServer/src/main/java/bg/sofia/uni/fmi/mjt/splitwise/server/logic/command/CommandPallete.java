package bg.sofia.uni.fmi.mjt.splitwise.server.logic.command;

public class CommandPallete {
    public static final int DEFAULT_HISTORY_LINES = 8;

    public static final String DISCONNECT_MESSAGE = "Disconnected from server";
    public static final String UNKNOWN_MESSAGE = "Unknown command, type \"help\" to see available commands";

    public static final String HELP_MESSAGE = "Possible commands:" + System.lineSeparator() +
            CommandOptionals.REGISTER.getString() + " <username> <password>" + System.lineSeparator() +
            CommandOptionals.LOGIN.getString() + " <username> <password>" + System.lineSeparator() +
            CommandOptionals.LOGOUT.getString() + System.lineSeparator() +
            CommandOptionals.HELP.getString() + System.lineSeparator() +
            CommandOptionals.ADD_FRIEND.getString() + " <person's username>" + System.lineSeparator() +
            CommandOptionals.CREATE_GROUP.getString() + " <group's_name> <participant1> <participant2> ..."
            + System.lineSeparator() +
            CommandOptionals.SPLIT.getString() + " <amount> <friend's_username> <reason>" + System.lineSeparator() +
            CommandOptionals.SPLIT_GROUP.getString() + " <amount> <group's_name> <reason>" + System.lineSeparator() +
            CommandOptionals.GET_STATUS.getString() + System.lineSeparator() +
            CommandOptionals.PAYED.getString() + " <amount> <person's username>" + System.lineSeparator() +
            CommandOptionals.GET_HISTORY.getString() + " <number of lines>(optional)" + System.lineSeparator() +
            CommandOptionals.DISCONNECT.getString();
}
