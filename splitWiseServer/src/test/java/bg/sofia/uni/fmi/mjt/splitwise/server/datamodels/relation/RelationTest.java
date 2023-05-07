package bg.sofia.uni.fmi.mjt.splitwise.server.datamodels.relation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "checkstyle:linelength", "checkstyle:magicnumber" })
public class RelationTest {
    private static final double EPSILON = 0.01d;
    private Relation relation;
    private String inDebtedPerspective = "You owe John 10.00 LV";

    @BeforeEach
    void rebuildRelationObject() {
        relation = new Relation("John", "Jane",
                10.00, "for groceries", "Group1", ObligationStatus.ACTIVE);
    }

    @Test
    void testRelationPerspectiveJane() {
        assertEquals(inDebtedPerspective, relation.toString("Jane"),
                "Jane owes John money");
    }

    @Test
    void testUpdateSumAssureSumIncreasesCorrectly() {
        relation.updateSum("Jane", "John", 5.00, "for lunch");
        assertEquals(15.00, relation.getSum(), EPSILON, "5.00 must be added to the sum");
    }

    @Test
    void testUpdateSumAssureReasonChanges() {
        relation.updateSum("Jane", "John", 5.00, "for lunch");
        assertEquals("for lunch", relation.getReason(), "reason must be changed");
    }

    @Test
    void testUpdateSumAssureStatusRemainsActive() {
        relation.updateSum("Jane", "John", 5.00, "for lunch");
        assertEquals(ObligationStatus.ACTIVE, relation.getRelationStatus(), "status must remain active");
    }

    @Test
    void testUpdateSumAssureMoneyLenderIsTheSame() {
        relation.updateSum("Jane", "John", 5.00, "for lunch");
        assertEquals("John", relation.getMoneyLender(), "Jane still owes money to John");
    }

    @Test
    void testUpdateSumAssureSumDecreasesCorrectly() {
        relation.updateSum("John", "Jane", 20.00, "for dinner");
        assertEquals(10.0, relation.getSum(), EPSILON);
    }

    @Test
    void testUpdateSumAssureMoneyLenderFlipped() {
        relation.updateSum("John", "Jane", 20.00, "for dinner");
        assertEquals("Jane", relation.getMoneyLender(), "Now John should owe Jane money");
    }

    @Test
    void testUpdateSumAssureSumNeutralized() {
        relation.updateSum("John", "Jane", 10.00, "for cake");
        assertEquals(0.0, relation.getSum(), EPSILON, "Sum should be 0.0");
    }

    @Test
    void testUpdateSumAssureStatusBecomesNeutral() {
        relation.updateSum("John", "Jane", 10.00, "for cake");
        assertEquals(ObligationStatus.NEUTRALIZED, relation.getRelationStatus(), "status must change to neutral");
    }

    @Test
    void testPayDebtLeftOver() {
        assertEquals(0.0, relation.payDebt(7.0), EPSILON, "7 - 10 should be 0");
    }

    @Test
    void testPayDebtLeftOverBigPay() {
        assertEquals(10.0, relation.payDebt(20.0), EPSILON, "20 - 10 should be 10");
    }

    @Test
    void testPayDebtCorrectSum() {
        relation.payDebt(5.00);
        assertEquals(5.00, relation.getSum(), EPSILON, "10 - 5 should be 5");
    }

    @Test
    void testPayDebtFullSumAssureNeutralization() {
        relation.payDebt(10.00);
        assertEquals(ObligationStatus.NEUTRALIZED, relation.getRelationStatus(),
                "10-10 should set the flag to neutral");
    }

    @Test
    void testOverPayDebtAssureNeutralization() {
        relation.payDebt(40.00);
        assertEquals(ObligationStatus.NEUTRALIZED, relation.getRelationStatus(),
                "40-10 should set the flag to neutral");
    }

}
