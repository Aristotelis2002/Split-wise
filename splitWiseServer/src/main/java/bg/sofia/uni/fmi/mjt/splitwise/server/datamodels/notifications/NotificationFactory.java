package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.notifications;

public class NotificationFactory {
    private static final String ADDED_FRIEND = "%s added you as a friend";
    private static final String ADDED_TO_GROUP = "%s added you to the group \"%s\"";
    private static final String SPLITED_BILL = "%s splitted %s LV with you and now %s [%s]";
    private static final String SPLITED_BILL_GROUP = "%s splitted %s LV with a group(%s) "
            + " that you are part of and now %s [%s]";
    private static final String PAYMENT_ACCEPTED = "%s aproved your payment %s LV";

    private NotificationFactory() {
    }

    public static Notification addedFriend(String username) {
        return new Notification(null, String.format(ADDED_FRIEND, username));
    }

    public static Notification addedToGroup(String groupName, String addedBy) {
        return new Notification(groupName, String.format(ADDED_TO_GROUP, addedBy, groupName));
    }

    public static Notification splitedBill(String username, String amount, String status, String reason) {
        return new Notification(null, String.format(SPLITED_BILL, username, amount, status, reason));
    }

    public static Notification splitedBillGroup(String username, String groupName,
            String amount, String status, String reason) {
        return new Notification(groupName,
                String.format(SPLITED_BILL_GROUP, username, amount, groupName, status, reason));
    }

    public static Notification paymentAccepted(String username, String amount) {
        return new Notification(null, String.format(PAYMENT_ACCEPTED, username, amount));
    }
}
