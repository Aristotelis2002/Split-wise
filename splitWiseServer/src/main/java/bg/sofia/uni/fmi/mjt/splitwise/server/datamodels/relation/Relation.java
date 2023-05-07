package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.relation;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Relation implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private static final String MONEYLENDER_PERSPECTIVE = "%s owes you %s LV";
    private static final String IN_DEBT_PERSPECTIVE = "You owe %s %s LV";
    private static final double EPSILON = 0.01d;
    
    private String moneyLender;
    private String inDebted;
    private double sum;
    private String reason;
    private String groupName;
    private ObligationStatus relationStatus;

    public Relation(String moneyLender, String inDebted, double sum, String reason,
            String groupName, ObligationStatus relationStatus) {
        this.moneyLender = moneyLender;
        this.inDebted = inDebted;
        this.sum = sum;
        this.reason = reason;
        this.relationStatus = relationStatus;
        this.groupName = groupName;
    }

    public void updateSum(String gaveLoan, String receivedLoan, double sumToBeAdded, String reason) {
        if (moneyLender.equals(receivedLoan)) {
            sum += sumToBeAdded;
        } else {
            sum -= sumToBeAdded;
            if (sum < EPSILON * -1) {
                swapLenderDebted();
                sum *= -1;
            }
        }

        this.reason = reason;

        if (Math.abs(sum) < EPSILON) {
            relationStatus = ObligationStatus.NEUTRALIZED;
        } else {
            relationStatus = ObligationStatus.ACTIVE;
        }
    }

    public double payDebt(double amount) {
        double leftOver = amount - sum;
        sum -= amount;

        if (sum < EPSILON) {
            sum = 0.0;
            relationStatus = ObligationStatus.NEUTRALIZED;
        }

        if (leftOver < EPSILON) {
            return 0.0;
        }

        return leftOver;
    }

    public String getGroupName() {
        return groupName;
    }

    public ObligationStatus getRelationStatus() {
        return relationStatus;
    }

    public String getMoneyLender() {
        return moneyLender;
    }

    public double getSum() {
        return sum;
    }

    public String getReason() {
        return reason;
    }

    public String toString(String perspective) {
        if (moneyLender.equals(perspective)) {
            return String.format(MONEYLENDER_PERSPECTIVE, inDebted, DF.format(sum));
        }
        return String.format(IN_DEBT_PERSPECTIVE, moneyLender, DF.format(sum));
    }

    private void swapLenderDebted() {
        String tempForSwap = moneyLender;
        moneyLender = inDebted;
        inDebted = tempForSwap;
    }
}
