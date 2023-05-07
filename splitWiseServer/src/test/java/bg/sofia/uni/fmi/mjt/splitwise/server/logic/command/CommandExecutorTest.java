package bg.sofia.uni.fmi.mjt.splitwise.server.logic.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.Server;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "checkstyle:linelength", "checkstyle:magicnumber" })
public class CommandExecutorTest {
    private final int sessionToken = 5;
    private final String username = "User";
    private final String friendName = "Friend";
    private final int hashedPassword = "123456".hashCode();
    private CommandExecutor cmdExecutor;

    @Mock
    private User userMock;

    @Mock
    private Map<Integer, String> sessionUsernames;

    @Mock
    private Map<String, Integer> userPasswordsTable;

    @Mock
    private Map<String, User> usersTable;

    @BeforeEach
    void initCmdObject() {
        cmdExecutor = new CommandExecutor(sessionUsernames, userPasswordsTable, usersTable);
    }

    @Test
    void testLoggingInExistingAccount() {
        doReturn(null).when(sessionUsernames).get(sessionToken);
        when(sessionUsernames.put(sessionToken, username)).thenReturn(null);
        when(userPasswordsTable.containsKey(username)).thenReturn(true);
        doReturn(hashedPassword).when(userPasswordsTable).get(username);
        doReturn(userMock).when(usersTable).get(username);
        when(userMock.getNotifications()).thenReturn("");

        String expected = "Login successful" + System.lineSeparator();
        assertEquals(expected,
                cmdExecutor.execute(CommandFactory.of("login User 123456"),
                        sessionToken));
    }

    @Test
    void testLoggingInNonExistingAccount() {
        doReturn(null).when(sessionUsernames).get(sessionToken);
        when(userPasswordsTable.containsKey(username)).thenReturn(false);

        String expected = "Incorrect password or username";
        assertEquals(expected,
                cmdExecutor.execute(CommandFactory.of("login User 123456"),
                        sessionToken));
    }

    @Test
    void testLoggingWhenAlreadyLogged() {
        doReturn(username).when(sessionUsernames).get(sessionToken);

        String expected = "You are already logged in to an account";
        assertEquals(expected,
                cmdExecutor.execute(CommandFactory.of("login User 123456"),
                        sessionToken));
    }

    @Test
    void testRegisterValidCommand() {
        when(userPasswordsTable.put(username, hashedPassword)).thenReturn(null);
        when(usersTable.put(eq(username), any(User.class))).thenReturn(null);

        String expected = "Registration was successful";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("register User 123456"), sessionToken));
    }

    @Test
    void testAddFriendWhoIsNewAndValid() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.containsKey(friendName)).thenReturn(true);
        when(usersTable.get(username)).thenReturn(userMock);
        when(usersTable.get(friendName)).thenReturn(userMock);
        String expected = friendName + " was added successfuly to your friend list";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("add-friend Friend"), sessionToken));
    }

    @Test
    void testCreateGroupValidFriends() {
        String friendName1 = "Friend1";
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.containsKey(friendName)).thenReturn(true);
        when(usersTable.containsKey(friendName1)).thenReturn(true);
        when(usersTable.get(username)).thenReturn(userMock);
        when(usersTable.get(friendName)).thenReturn(userMock);
        when(usersTable.get(friendName1)).thenReturn(userMock);

        String expected = "Group created successfuly";
        assertEquals(expected, cmdExecutor.execute(
                CommandFactory.of("create-group name Friend Friend1"), sessionToken));
    }

    @Test
    void testSplitWithFriendValidSumAndFriend() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.containsKey(friendName)).thenReturn(true);
        when(usersTable.get(username)).thenReturn(userMock);
        when(usersTable.get(friendName)).thenReturn(userMock);

        String expected = "Bill splitted succesfully";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("split 5 Friend reason!"), sessionToken));
    }

    @Test
    void testSplitWithGroupValidGroupMembersAndSum() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.get(username)).thenReturn(userMock);

        String expected = "Bill splitted succesfully";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("split-group 5 grName reason!"), sessionToken));
    }

    @Test
    void testAcceptValidPaymentFromFriend() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.containsKey(friendName)).thenReturn(true);
        when(usersTable.get(username)).thenReturn(userMock);
        when(usersTable.get(friendName)).thenReturn(userMock);
        when(userMock.acceptPayment(userMock, 5.00)).thenReturn("Accepting payment was successful");
        
        String expected = "Accepting payment was successful";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("payed 5.00 Friend"), sessionToken));
    }

    @Test
    void testGetHistoryValidUserOnlyOneLine() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.get(username)).thenReturn(userMock);
        when(userMock.getHistory(1)).thenReturn(List.of("Some history"));

        String expected = "Some history";
        assertEquals(expected, cmdExecutor.execute(
            CommandFactory.of("get-history 1"), sessionToken));
    }

    @Test
    void testGetStatusLoggedInUser() {
        when(sessionUsernames.get(sessionToken)).thenReturn(username);
        when(usersTable.get(username)).thenReturn(userMock);
        when(userMock.getStatus()).thenReturn("status");

        assertEquals("status", cmdExecutor.execute(
            CommandFactory.of("get-status"), sessionToken));
    }

    @Test
    void testShutDownServerCommand() {
        Server serverMock = mock(Server.class);
        cmdExecutor.setCurrentServer(serverMock);
        when(sessionUsernames.get(sessionToken)).thenReturn("admin");

        String expected = "Server is shutting down";
        assertEquals(expected, cmdExecutor.execute(
                CommandFactory.of("shutdown-server"), sessionToken),
                "Should shut down because user is admin");

    }
}
