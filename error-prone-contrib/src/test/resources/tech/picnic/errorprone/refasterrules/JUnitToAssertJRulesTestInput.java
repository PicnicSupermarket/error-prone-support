package tech.picnic.errorprone.refasterrules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitToAssertJRulesTest implements RefasterRuleCollectionTestCase {
    void testFail() {
        Assertions.fail();
    }

    void testFailWithMessage() {
        Assertions.fail("foo");
    }

    void testFailWithMessageAndThrowable() {
        Assertions.fail("foo", new IllegalStateException());
    }

    void testAssertTrue() {
        assertTrue(true);
    }

    void testAssertTrueWithMessage() {
        assertTrue(true, "foo");
    }

    void testAssertTrueWithMessageSupplier() {
        assertTrue(true, () -> "foo");
    }

    void testAssertFalse() {
        assertFalse(true);
    }

    void testAssertFalseWithMessage() {
        assertFalse(true, "foo");
    }

    void testAssertFalseWithMessageSupplier() {
        assertFalse(true, () -> "foo");
    }
}
