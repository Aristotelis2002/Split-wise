package bg.sofia.uni.fmi.mjt.splitwise.server.logic.command;

import java.util.Arrays;

public class CommandFactory {
    private CommandFactory() {
    }

    public static Command of(String clientInput) {
        String[] args = clientInput.split(" ");
        return new Command(args[0], Arrays.copyOfRange(args, 1, args.length));
    }
}