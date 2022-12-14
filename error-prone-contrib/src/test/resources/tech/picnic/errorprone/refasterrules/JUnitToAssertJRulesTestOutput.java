package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
