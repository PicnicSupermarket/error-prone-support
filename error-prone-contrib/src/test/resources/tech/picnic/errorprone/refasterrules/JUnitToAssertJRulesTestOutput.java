package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitToAssertJRulesTest implements RefasterRuleCollectionTestCase {
    void testFail() {
        throw new AssertionError();
    }

    void testFailWithMessage() {
        fail("foo");
    }

    void testFailWithMessageAndThrowable() {
        fail("foo", new IllegalStateException());
    }

    void testAssertTrue() {
        assertThat(true).isTrue();
    }

    void testAssertTrueWithMessage() {
        assertThat(true).withFailMessage("foo").isTrue();
    }

    void testAssertTrueWithMessageSupplier() {
        assertThat(true).withFailMessage(() -> "foo").isTrue();
    }

    void testAssertFalse() {
        assertThat(true).isFalse();
    }

    void testAssertFalseWithMessage() {
        assertThat(true).withFailMessage("foo").isFalse();
    }

    void testAssertFalseWithMessageSupplier() {
        assertThat(true).withFailMessage(() -> "foo").isFalse();
    }

    void testAssertNull() {
        assertThat(new Object()).isNull();
    }

    void testAssertNullWithMessage() {
        assertThat(new Object()).withFailMessage("foo").isNull();
    }

    void testAssertNullWithMessageSupplier() {
        assertThat(new Object()).withFailMessage(() -> "foo").isNull();
    }

    void testAssertNotNull() {
        assertThat(new Object()).isNotNull();
    }

    void testAssertNotNullWithMessage() {
        assertThat(new Object()).withFailMessage("foo").isNotNull();
    }

    void testAssertNotNullWithMessageSupplier() {
        assertThat(new Object()).withFailMessage(() -> "foo").isNotNull();
    }

    void testAssertSame() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).isSameAs(expected);
    }

    void testAssertSameWithMessage() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).withFailMessage("foo").isSameAs(expected);
    }

    void testAssertSameWithMessageSupplier() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).withFailMessage(() -> "foo").isSameAs(expected);
    }

    void testAssertNotSame() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).isNotSameAs(expected);
    }

    void testAssertNotSameWithMessage() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).withFailMessage("foo").isNotSameAs(expected);
    }

    void testAssertNotSameWithMessageSupplier() {
        Object actual = new Object();
        Object expected = new Object();
        assertThat(actual).withFailMessage(() -> "foo").isNotSameAs(expected);
    }
}
