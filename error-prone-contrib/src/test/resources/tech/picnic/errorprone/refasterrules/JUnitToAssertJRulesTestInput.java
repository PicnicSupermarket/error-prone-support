package tech.picnic.errorprone.refasterrules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
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

    void testAssertNull() {
        assertNull(new Object());
    }

    void testAssertNullWithMessage() {
        assertNull(new Object(), "foo");
    }

    void testAssertNullWithMessageSupplier() {
        assertNull(new Object(), () -> "foo");
    }

    void testAssertNotNull() {
        assertNotNull(new Object());
    }

    void testAssertNotNullWithMessage() {
        assertNotNull(new Object(), "foo");
    }

    void testAssertNotNullWithMessageSupplier() {
        assertNotNull(new Object(), () -> "foo");
    }

    void testAssertSame() {
        Object actual = new Object();
        Object expected = new Object();
        assertSame(expected, actual);
    }

    void testAssertSameWithMessage() {
        Object actual = new Object();
        Object expected = new Object();
        assertSame(expected, actual, "foo");
    }

    void testAssertSameWithMessageSupplier() {
        Object actual = new Object();
        Object expected = new Object();
        assertSame(expected, actual, () -> "foo");
    }

    void testAssertNotSame() {
        Object actual = new Object();
        Object expected = new Object();
        assertNotSame(expected, actual);
    }

    void testAssertNotSameWithMessage() {
        Object actual = new Object();
        Object expected = new Object();
        assertNotSame(expected, actual, "foo");
    }

    void testAssertNotSameWithMessageSupplier() {
        Object actual = new Object();
        Object expected = new Object();
        assertNotSame(expected, actual, () -> "foo");
    }

    void testAssertThrowsExactly() {
        assertThrowsExactly(IllegalStateException.class, () -> {});
    }

    void testAssertThrowsExactlyWithMessage() {
        assertThrowsExactly(IllegalStateException.class, () -> {}, "foo");
    }

    void testAssertThrowsExactlyWithMessageSupplier() {
        assertThrowsExactly(IllegalStateException.class, () -> {}, () -> "foo");
    }

    void testAssertThrows() {
        assertThrows(IllegalStateException.class, () -> {});
    }

    void testAssertThrowsWithMessage() {
        assertThrows(IllegalStateException.class, () -> {}, "foo");
    }

    void testAssertThrowsWithMessageSupplier() {
        assertThrows(IllegalStateException.class, () -> {}, () -> "foo");
    }

    void testAssertDoesNotThrow() {
        assertDoesNotThrow(() -> {});
    }

    void testAssertDoesNotThrowWithMessage() {
        assertDoesNotThrow(() -> {}, "foo");
    }

    void testAssertDoesNotThrowWithMessageSupplier() {
        assertDoesNotThrow(() -> {}, () -> "foo");
    }
}
