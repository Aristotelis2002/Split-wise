package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendAlreadyAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendNotAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupSizeException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.HistoryLogger;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "checkstyle:linelength", "checkstyle:magicnumber" })
public class UserTest {
    private User user;
    private User friend1;
    private User friend2;

    @Mock
    private BlockingQueue<UsernameEventPair> blQueue;

    @BeforeEach
    void setUpUsers() {
        user = new User("User1");
        friend1 = new User("Friend1");
        friend2 = new User("Friend2");
        HistoryLogger.initializeLogger(blQueue);
    }

    @Test
    void testAddNewFriendNotThrows() {
        when(blQueue.add(any())).thenReturn(true);
        User friend = new User("Friend1");
        assertDoesNotThrow(() -> user.addFriend("Friend1", friend));
        assertThrows(FriendAlreadyAddedException.class, () -> user.addFriend("Friend1", friend), 
                "Should not throw because user has 0 friends");
    }

    @Test
    void testAddExistingFriendThrows() throws FriendAlreadyAddedException {
        when(blQueue.add(any())).thenReturn(true);
        User friend = new User("Friend1");
        user.addFriend("Friend1", friend);
        assertThrows(FriendAlreadyAddedException.class, () -> user.addFriend("Friend1", friend),
                "Friend is already added");
    }

    @Test
    void testCreateNewValidGroup() {
        when(blQueue.add(any())).thenReturn(true);
        Collection<User> members = Arrays.asList(user, friend1, friend2);
        assertDoesNotThrow(() -> user.createGroup("Group1", members),
                "New group should not throw");
    }

    @Test
    void testCreateGroupThatAlreadyExists() throws GroupAlreadyExistsException, GroupSizeException {
        when(blQueue.add(any())).thenReturn(true);
        Collection<User> members = Arrays.asList(user, friend1, friend2);
        user.createGroup("Group1", members);
        assertThrows(GroupAlreadyExistsException.class, () -> user.createGroup("Group1", members),
                "Group already added, should throw");
    }

    @Test
    void testCreateGroupTooSmallSize() {
        assertThrows(GroupSizeException.class, () -> user.createGroup("Group2", Arrays.asList(friend1)));
    }

    @Test
    void testSplitFriendWhoIsAdded() throws FriendAlreadyAddedException {
        when(blQueue.add(any())).thenReturn(true);
        User friend = new User("Friend1");
        user.addFriend("Friend1", friend);
        assertDoesNotThrow(() -> user.splitFriend(friend, 10.0, "Test reason"),
                "Friend is added and sum is valid");
    }

    @Test
    void testSplitFriendWhoIsNotAdded() {
        assertThrows(FriendNotAddedException.class, () -> user.splitFriend(new User("NonFriend"), 10.0, "Test reason"));
    }

    @Test
    void testSplitGroupWithValidGroup() throws GroupAlreadyExistsException, GroupSizeException {
        when(blQueue.add(any())).thenReturn(true);
        Collection<User> members = Arrays.asList(user, friend1, friend2);
        user.createGroup("Group1", members);
        assertDoesNotThrow(() -> user.splitGroup("Group1", 10.0, "Test reason"));
    }

    @Test
    void testSplitGroupWithNonExistingGroup() throws GroupAlreadyExistsException, GroupSizeException {
        assertThrows(GroupNotFoundException.class, () -> user.splitGroup("NonExistingGroup", 10.0, "Test reason"));
    }

    @Test
    void testGetStatusDefault() {
        String expected = "Friends:" + System.lineSeparator() +
                "Groups:" + System.lineSeparator();
        assertEquals(expected, user.getStatus(), "No status except default");
    }

    @Test
    void testGetStatusSplittedWithFriend() throws FriendAlreadyAddedException, FriendNotAddedException {
        when(blQueue.add(any())).thenReturn(true);

        user.addFriend("Friend1", friend1);
        user.splitFriend(friend1, 10.0, "Test reason");
        
        String expected = "Friends:" + System.lineSeparator() +
            "Friend1 owes you 5.00 LV" + System.lineSeparator() +
            "Groups:" + System.lineSeparator();
        
        assertEquals(expected, user.getStatus(), "Split should work and owe only 5");
    }

    @Test
    void testGetStatusSplittedWithGroup() throws GroupAlreadyExistsException, GroupSizeException {
        when(blQueue.add(any())).thenReturn(true);
        Collection<User> members = Arrays.asList(user, friend1, friend2);
        user.createGroup("Group1", members);
        assertDoesNotThrow(() -> user.splitGroup("Group1", 9.0, "Test reason"));
        
        String expected = "Friends:" + System.lineSeparator() +
            "Groups:" + System.lineSeparator() +
            "** Group1" + System.lineSeparator() +
            "Friend1 owes you 3.00 LV" + System.lineSeparator() +
            "Friend2 owes you 3.00 LV" + System.lineSeparator();
        
        assertEquals(expected, user.getStatus(), "Status should be updated with group");
    }

    @Test
    void testAcceptPaymentWhenNoDebt() {
        String expected = "Friend1" + " doesn't owe you any money. Payment was not accepted";
        assertEquals(expected, user.acceptPayment(friend1, 5.00),
                "No debt should cause acceptance failure");
    }

    @Test
    void testAcceptPaymentWhenReverseDebt() throws FriendAlreadyAddedException, FriendNotAddedException {
        when(blQueue.add(any())).thenReturn(true);
        friend1.addFriend("User1", user);
        friend1.splitFriend(user, 10.00, "null");
        String expected = "Payment was not accepted because you owe " + 
            "Friend1" + " 5.00 LV";
        assertEquals(expected, user.acceptPayment(friend1, 5.00), "You owe, not friend to you");
    }

    @Test
    void testAcceptPaymentWhenPayingTooMuch() throws FriendNotAddedException, FriendAlreadyAddedException {
        when(blQueue.add(any())).thenReturn(true);
        user.addFriend("Friend1", friend1);
        user.splitFriend(friend1, 10.0, "Test reason");
        String expected = "Payment was not accepted because " + "Friend1" +
            " owes only " + "5.00" + " LV and the given payment is " +
            "15.00" + " LV";
        assertEquals(expected, user.acceptPayment(friend1, 15.00));
    }

    @Test
    void testAcceptPaymentChangeStatusToNeutral() throws FriendAlreadyAddedException, FriendNotAddedException {
        when(blQueue.add(any())).thenReturn(true);
        user.addFriend("Friend1", friend1);
        user.splitFriend(friend1, 10.0, "Test reason");
        String expected = "Accepting payment was sucessful" + System.lineSeparator() +
            "Now " + "Friend1" + " doesn't owe you any money";
        assertEquals(expected, user.acceptPayment(friend1, 5.00), "Should clear debt");
    }

    @Test
    void testAcceptPaymentPartiallyPayedDebt() throws FriendAlreadyAddedException, FriendNotAddedException {
        when(blQueue.add(any())).thenReturn(true);
        user.addFriend("Friend1", friend1);
        user.splitFriend(friend1, 10.0, "Test reason");
        String expected = "Accepting payment was sucessful" + System.lineSeparator() +
            "Now " + "Friend1" + " owes you in total " + "2.00" + " LV";

        assertEquals(expected, user.acceptPayment(friend1, 3.00), 
            "5 - 3 = 2, therefore friend1 has to have only 2 lv debt remaining");
    }

    @Test
    void testNoNewNotifications() {
        assertEquals("No new notifications", user.getNotifications());
    }

    @Test
    void testNotificationAddedNewFriend() throws FriendAlreadyAddedException {
        String expected = "*** Notifications ***" + System.lineSeparator() +
                "Friends:" + System.lineSeparator() +
                "User1 added you as a friend" + System.lineSeparator() + System.lineSeparator() +
                "Groups:" + System.lineSeparator();
        when(blQueue.add(any())).thenReturn(true);
        user.addFriend("Friend1", friend1);
        assertEquals(expected, friend1.getNotifications());
    }
}
