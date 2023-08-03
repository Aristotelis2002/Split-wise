package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.notifications.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.notifications.NotificationFactory;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.relation.ObligationStatus;
import bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.relation.Relation;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendAlreadyAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.friend.FriendNotAddedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.datamodel.group.GroupSizeException;
import bg.sofia.uni.fmi.mjt.splitwise.server.filehandling.HistoryReader;
import bg.sofia.uni.fmi.mjt.splitwise.server.logging.HistoryLogger;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MIN_GROUP_SIZE = 3;
    private static final double EPSILON = 0.01d;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private String username;
    private Map<String, User> friends;
    private Map<String, Group> groups;
    private Map<UsernameGroupnamePair, Relation> relations;
    private Queue<Notification> notifications;

    public User(String username) {
        this.username = username;
        friends = new HashMap<>();
        groups = new HashMap<>();
        relations = new HashMap<>();
        notifications = new ArrayDeque<>();
    }

    public void addFriend(String friendName, User friend) throws FriendAlreadyAddedException {
        if (friends.containsKey(friendName)) {
            throw new FriendAlreadyAddedException("Friend already added");
        }

        friends.put(friendName, friend);
        friend.friends.put(username, this);
        friend.notifications.add(NotificationFactory.addedFriend(username));

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(username, "Added " + friendName + " to your friends list"));
        HistoryLogger.getInstance()
                .log(new UsernameEventPair(friendName, username + " addded you to their friends list"));
    }

    public void createGroup(String groupName, Collection<User> members)
            throws GroupAlreadyExistsException, GroupSizeException {
        assertGroupDoesNotExists(groupName, members);
        assertValidGroupSize(members.size());

        Group groupObject = new Group(groupName, new ArrayList<>(members));
        for (User user : members) {
            user.addGroup(groupObject);
            if (!user.getUsername().equals(this.username)) {
                user.notifications.add(NotificationFactory.addedToGroup(groupName, username));
                HistoryLogger.getInstance()
                        .log(new UsernameEventPair(user.username, "You were added to the group " +
                                groupName + " by " + username));
            }
        }

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(username, "You created the group " + groupName));
    }

    public void splitFriend(User friend, double sum, String reason) throws FriendNotAddedException {
        assertFriendship(friend.getUsername());

        double splitSum = sum / 2;
        split(friend, splitSum, reason, null);

        Relation rl = getRelation(friend.username, null);
        if (rl != null) {
            friend.notifications.add(NotificationFactory.splitedBill(username, DF.format(sum),
                    rl.toString(friend.username), rl.getReason()));
        } else {
            friend.notifications.add(NotificationFactory.splitedBill(username, DF.format(sum),
                    " you should check your updated status", reason));
        }

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(username, "You splitted " +
                        DF.format(sum) + " LV with " + friend.username +
                        "[" + reason + "]"));
        HistoryLogger.getInstance()
                .log(new UsernameEventPair(friend.username, username + " splitted " +
                        DF.format(sum) + " LV with you" +
                        "[" + reason + "]"));

    }

    public void splitGroup(String groupName, double amount, String reason) throws GroupNotFoundException {
        assertGroupExists(groupName);

        double splitAmount = amount / (groups.get(groupName).members().size());

        groups.get(groupName).members().forEach((m) -> {
            if (!m.getUsername().equals(username)) {
                split(m, splitAmount, reason, groupName);

                Relation rl = getRelation(m.username, groupName);
                if (rl != null) {
                    m.notifications.add(NotificationFactory.splitedBillGroup(username, groupName, DF.format(amount),
                            rl.toString(m.username), rl.getReason()));
                } else {
                    m.notifications.add(NotificationFactory.splitedBillGroup(username, groupName, DF.format(amount),
                            "he owes you less", reason));
                }

                HistoryLogger.getInstance()
                        .log(new UsernameEventPair(m.username, username +
                                " splitted " + DF.format(amount) + " with the group" +
                                groupName + " which you are part of[" + reason + "]"));
            }
        });

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(username, "You splitted " +
                        DF.format(amount) + " LV with the group " + groupName +
                        "[" + reason + "]"));
    }

    public String getStatus() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Friends:" + System.lineSeparator());

        friends.keySet().forEach((f) -> {
            var pair = new UsernameGroupnamePair(f, null);
            if (relations.containsKey(pair) &&
                    relations.get(pair).getRelationStatus() == ObligationStatus.ACTIVE) {
                strBuilder.append(relations.get(pair).toString(username) + System.lineSeparator());
            }
        });

        strBuilder.append("Groups:" + System.lineSeparator());
        for (Group group : groups.values()) {
            
            if (!anyActiveObligations(group.groupName())) {
                continue;
            }

            strBuilder.append("** " + group.groupName() + System.lineSeparator());
            group.members().forEach((m) -> {
                var pair = new UsernameGroupnamePair(m.username, group.groupName());
                if (relations.containsKey(pair) &&
                        relations.get(pair).getRelationStatus() == ObligationStatus.ACTIVE) {
                    strBuilder.append(relations.get(pair).toString(username) + System.lineSeparator());
                }
            });
        }

        return strBuilder.toString();
    }

    // this method exists because of serialization problems with Map<String, Group>
    public void setGroups(Collection<Group> groups) {
        for (Group group : groups) {
            this.groups.put(group.groupName(), group);
        }
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public String acceptPayment(User person, double amount) {
        double debt = getDebtAmount(person);

        if (Math.abs(debt) < EPSILON) {
            return person.username + " doesn't owe you any money. Payment was not accepted";
        }

        if (debt < EPSILON * -1) {
            return "Payment was not accepted because you owe " + person.username +
                    " " + DF.format(-debt) + " LV";
        }

        if (amount - debt > EPSILON) {
            return "Payment was not accepted because " + person.username +
                    " owes only " + DF.format(debt) + " LV and the given payment is " +
                    DF.format(amount) + " LV";
        }

        clearDebts(person, amount);
        debt = getDebtAmount(person);

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(username, "You accepted " +
                        DF.format(amount) + " LV from " + person.username));
        person.notifications.add(NotificationFactory.paymentAccepted(username, DF.format(amount)));

        HistoryLogger.getInstance()
                .log(new UsernameEventPair(person.username, "Your payment of " +
                        DF.format(amount) + " LV were accepted by " + username));

        if (debt < EPSILON) {
            return "Accepting payment was sucessful" + System.lineSeparator() +
                    "Now " + person.username + " doesn't owe you any money";
        }

        return "Accepting payment was sucessful" + System.lineSeparator() +
                "Now " + person.username + " owes you in total " + DF.format(debt) + " LV";
    }

    public List<String> getHistory(int linesOfHistory) {
        return HistoryReader.getInstance().readLines(username, linesOfHistory);
    }

    public String getNotifications() {
        if (notifications.isEmpty()) {
            return "No new notifications";
        }

        StringBuilder notificationsString = new StringBuilder("*** Notifications ***" + System.lineSeparator());
        notificationsString.append("Friends:" + System.lineSeparator());
        notificationsString.append(notifications.stream()
                .filter((n) -> n.groupName() == null)
                .map((n) -> n.body())
                .collect(Collectors.joining(System.lineSeparator())));

        notificationsString.append(System.lineSeparator() + System.lineSeparator());

        notificationsString.append("Groups:" + System.lineSeparator());
        List<String> allGroupNames = notifications.stream()
                .map((n) -> n.groupName())
                .filter((s) -> s != null)
                .distinct()
                .toList();

        Map<String, List<String>> groupsAndNotfis = new HashMap<>();
        allGroupNames.forEach((s) -> groupsAndNotfis.put(s, new ArrayList<>()));
        notifications.stream()
                .filter((n) -> n.groupName() != null)
                .forEach((n) -> groupsAndNotfis.get(n.groupName()).add(n.body()));

        groupsAndNotfis.entrySet().forEach((e) -> {
            notificationsString.append("* " + e.getKey() + ":" + System.lineSeparator());
            notificationsString.append(e.getValue().stream()
                    .collect(Collectors.joining(System.lineSeparator())));
            notificationsString.append(System.lineSeparator());
        });

        notifications.clear();
        return notificationsString.toString();
    }

    public String getUsername() {
        return username;
    }

    private void split(User person, double sum, String reason, String groupName) {
        UsernameGroupnamePair key = new UsernameGroupnamePair(person.getUsername(), groupName);
        double debt = getDebtAmount(person);

        if (debt < EPSILON * -1) {
            clearDebts(person, sum);
        }
        if (debt < EPSILON * -1) {
            sum = sum + debt;
        }
        if (sum < EPSILON * -1) {
            return;
        }

        if (!relations.containsKey(key)) {
            Relation relationNew = new Relation(username, person.getUsername(), sum, reason, groupName,
                    ObligationStatus.ACTIVE);
            addRelation(key, relationNew);
            UsernameGroupnamePair key2 = new UsernameGroupnamePair(this.username, groupName);
            person.addRelation(key2, relationNew);
            return;
        }

        relations.get(key).updateSum(person.getUsername(), username, sum, reason);

    }

    private void clearDebts(User person, double amount) {
        double leftOver = clearDebt(person.username, null, amount);

        if (Math.abs(leftOver) < EPSILON) {
            return;
        }

        for (String groupName : groups.keySet()) {
            leftOver = clearDebt(person.username, groupName, leftOver);
            if (Math.abs(leftOver) < EPSILON) {
                return;
            }
        }
    }

    private double clearDebt(String personName, String groupName, double amount) {
        double leftOver = amount;
        var pair = new UsernameGroupnamePair(personName, groupName);

        if (relations.containsKey(pair) &&
                relations.get(pair).getRelationStatus() == ObligationStatus.ACTIVE) {
            Relation rl = relations.get(pair);
            leftOver = rl.payDebt(amount);
        }

        return leftOver;
    }

    private double getDebtAmount(User person) {
        double amount = 0.0;
        amount += getAmountFromRelation(person.username, null);
        for (String groupName : groups.keySet()) {
            amount += getAmountFromRelation(person.username, groupName);
        }
        return amount;
    }

    private double getAmountFromRelation(String personName, String groupName) {
        var pair = new UsernameGroupnamePair(personName, groupName);

        if (relations.containsKey(pair) &&
                relations.get(pair).getRelationStatus() == ObligationStatus.ACTIVE) {
            Relation rl = relations.get(pair);
            if (rl.getMoneyLender().equals(username)) {
                return rl.getSum();
            } else {
                return -rl.getSum();
            }
        }

        return 0.0;
    }

    private boolean anyActiveObligations(String groupName) {
        return groups.get(groupName).members().stream().anyMatch((m) -> {
            var pair = new UsernameGroupnamePair(m.username, groupName);
            return relations.containsKey(pair) &&
                    relations.get(pair).getRelationStatus() == ObligationStatus.ACTIVE;
        });
    }

    private void addGroup(Group groupToBeAdded) {
        groups.put(groupToBeAdded.groupName(), groupToBeAdded);
    }

    private void addRelation(UsernameGroupnamePair key, Relation relationToBeAdded) {
        relations.put(key, relationToBeAdded);
    }

    private Relation getRelation(String username, String groupname) {
        return relations.get(new UsernameGroupnamePair(username, groupname));
    }

    private void assertGroupExists(String groupName) throws GroupNotFoundException {
        if (!groups.containsKey(groupName)) {
            throw new GroupNotFoundException(groupName + " group was not found");
        }
    }

    private void assertGroupDoesNotExists(String groupName, Collection<User> members)
            throws GroupAlreadyExistsException {
        for (User member : members) {
            if (member.groups.containsKey(groupName)) {
                throw new GroupAlreadyExistsException(member.username + " already has a group named " + groupName);
            }
        }
    }

    private void assertValidGroupSize(int size) throws GroupSizeException {
        if (size < MIN_GROUP_SIZE) {
            throw new GroupSizeException("You need at least " + MIN_GROUP_SIZE + " members to create a group");
        }
    }

    private void assertFriendship(String friendName) throws FriendNotAddedException {
        if (!friends.containsKey(friendName)) {
            throw new FriendNotAddedException(friendName + " is not added as friend");
        }
    }

}
