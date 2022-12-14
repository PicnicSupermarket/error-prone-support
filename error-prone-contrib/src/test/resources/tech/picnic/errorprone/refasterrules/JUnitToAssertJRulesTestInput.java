package tech.picnic.errorprone.refasterrules;

import static org.junit.jupiter.api.Assertions.assertTrue;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitToAssertJRulesTest implements RefasterRuleCollectionTestCase {
    void testAssertTrue() {
        assertTrue(true);
    }

    void testAssertTrueWithMessage() {
        assertTrue(true, "foo");
    }

    void testAssertTrueWithMessageSupplier() {
        assertTrue(true, () -> "foo");
    }
}
