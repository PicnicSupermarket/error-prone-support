package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules to replace JUnit assertions with AssertJ equivalents. */
@OnlineDocumentation
final class JUnitToAssertJRules {
    private JUnitToAssertJRules() {}

    static final class Fail {
        @BeforeTemplate
        void before() {
            Assertions.fail();
        }

        @AfterTemplate
        @DoNotCall
        void after() {
            throw new AssertionError();
        }
    }

    static final class FailWithMessage {
        @BeforeTemplate
        void before(String message) {
            Assertions.fail(message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(String message) {
            fail(message);
        }
    }

    static final class FailWithMessageAndThrowable {
        @BeforeTemplate
        void before(String message, Throwable throwable) {
            Assertions.fail(message, throwable);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(String message, Throwable throwable) {
            fail(message, throwable);
        }
    }

    static final class AssertTrue {
        @BeforeTemplate
        void before(boolean condition) {
            assertTrue(condition);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition) {
            assertThat(condition).isTrue();
        }
    }

    static final class AssertTrueWithMessage {
        @BeforeTemplate
        void before(boolean condition, String message) {
            assertTrue(condition, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition, String message) {
            assertThat(condition).withFailMessage(message).isTrue();
        }
    }

    static final class AssertTrueWithMessageSupplier {
        @BeforeTemplate
        void before(boolean condition, Supplier<String> messageSupplier) {
            assertTrue(condition, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition, Supplier<String> messageSupplier) {
            assertThat(condition).withFailMessage(messageSupplier).isTrue();
        }
    }

    static final class AssertFalse {
        @BeforeTemplate
        void before(boolean condition) {
            assertFalse(condition);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition) {
            assertThat(condition).isFalse();
        }
    }

    static final class AssertFalseWithMessage {
        @BeforeTemplate
        void before(boolean condition, String message) {
            assertFalse(condition, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition, String message) {
            assertThat(condition).withFailMessage(message).isFalse();
        }
    }

    static final class AssertFalseWithMessageSupplier {
        @BeforeTemplate
        void before(boolean condition, Supplier<String> messageSupplier) {
            assertFalse(condition, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(boolean condition, Supplier<String> messageSupplier) {
            assertThat(condition).withFailMessage(messageSupplier).isFalse();
        }
    }

    static final class AssertNull {
        @BeforeTemplate
        void before(Object object) {
            assertNull(object);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object) {
            assertThat(object).isNull();
        }
    }

    static final class AssertNullWithMessage {
        @BeforeTemplate
        void before(Object object, String message) {
            assertNull(object, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object, String message) {
            assertThat(object).withFailMessage(message).isNull();
        }
    }

    static final class AssertNullWithMessageSupplier {
        @BeforeTemplate
        void before(Object object, Supplier<String> messageSupplier) {
            assertNull(object, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object, Supplier<String> messageSupplier) {
            assertThat(object).withFailMessage(messageSupplier).isNull();
        }
    }

    static final class AssertNotNull {
        @BeforeTemplate
        void before(Object object) {
            assertNotNull(object);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object) {
            assertThat(object).isNotNull();
        }
    }

    static final class AssertNotNullWithMessage {
        @BeforeTemplate
        void before(Object object, String message) {
            assertNotNull(object, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object, String message) {
            assertThat(object).withFailMessage(message).isNotNull();
        }
    }

    static final class AssertNotNullWithMessageSupplier {
        @BeforeTemplate
        void before(Object object, Supplier<String> messageSupplier) {
            assertNotNull(object, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object object, Supplier<String> messageSupplier) {
            assertThat(object).withFailMessage(messageSupplier).isNotNull();
        }
    }

    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertEquals
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertArrayEquals
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertIterableEquals
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertLinesMatch
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertNotEquals
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertSame
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertNotSame
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertAll
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertThrowsExactly
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertThrows
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertDoesNotThrow
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertTimeout
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertInstanceOf
}
