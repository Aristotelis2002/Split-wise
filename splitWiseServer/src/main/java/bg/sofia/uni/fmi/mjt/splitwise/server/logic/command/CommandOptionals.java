package bg.sofia.uni.fmi.mjt.splitwise.server.logic.command;

public enum CommandOptionals {

    REGISTER("register", 2),
    LOGIN("login", 2),
    LOGOUT("logout", 0),
    HELP("help", 0),
    ADD_FRIEND("add-friend", 1),
    CREATE_GROUP("create-group", 3),
    SPLIT("split", 3),
    SPLIT_GROUP("split-group", 3),
    GET_STATUS("get-status", 0),
    PAYED("payed", 2),
    GET_HISTORY("get-history", 1),
    DISCONNECT("disconnect", 0),
    SHUTDOWN_SERVER("shutdown-server", 0);

    private String value;
    private int arguments;

    private CommandOptionals(String value) {
        this.value = value;
    }

    private CommandOptionals(String value, int arguments) {
        this.value = value;
        this.arguments = arguments;
    }

    public static CommandOptionals fromString(String str) {
        for (CommandOptionals c : CommandOptionals.values()) {
            if (c.getString().equalsIgnoreCase(str)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Command name does not exist");
    }

    public String getString() {
        return this.value;
    }

    public int getArgumentsCount() {
        return this.arguments;
    }

}
