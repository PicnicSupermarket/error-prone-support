package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.function.Supplier;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
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

    static final class AssertSame {
        @BeforeTemplate
        void before(Object actual, Object expected) {
            assertSame(expected, actual);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected) {
            assertThat(actual).isSameAs(expected);
        }
    }

    static final class AssertSameWithMessage {
        @BeforeTemplate
        void before(Object actual, Object expected, String message) {
            assertSame(expected, actual, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected, String message) {
            assertThat(actual).withFailMessage(message).isSameAs(expected);
        }
    }

    static final class AssertSameWithMessageSupplier {
        @BeforeTemplate
        void before(Object actual, Object expected, Supplier<String> messageSupplier) {
            assertSame(expected, actual, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected, Supplier<String> messageSupplier) {
            assertThat(actual).withFailMessage(messageSupplier).isSameAs(expected);
        }
    }

    static final class AssertNotSame {
        @BeforeTemplate
        void before(Object actual, Object expected) {
            assertNotSame(expected, actual);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected) {
            assertThat(actual).isNotSameAs(expected);
        }
    }

    static final class AssertNotSameWithMessage {
        @BeforeTemplate
        void before(Object actual, Object expected, String message) {
            assertNotSame(expected, actual, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected, String message) {
            assertThat(actual).withFailMessage(message).isNotSameAs(expected);
        }
    }

    static final class AssertNotSameWithMessageSupplier {
        @BeforeTemplate
        void before(Object actual, Object expected, Supplier<String> messageSupplier) {
            assertNotSame(expected, actual, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(Object actual, Object expected, Supplier<String> messageSupplier) {
            assertThat(actual).withFailMessage(messageSupplier).isNotSameAs(expected);
        }
    }

    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertAll

    static final class AssertThrowsExactly<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz) {
            assertThrowsExactly(clazz, runnable);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz) {
            assertThatThrownBy(runnable).isExactlyInstanceOf(clazz);
        }
    }

    static final class AssertThrowsExactlyWithMessage<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz, String message) {
            assertThrowsExactly(clazz, runnable, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz, String message) {
            assertThatThrownBy(runnable).withFailMessage(message).isExactlyInstanceOf(clazz);
        }
    }

    static final class AssertThrowsExactlyWithMessageSupplier<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz, Supplier<String> messageSupplier) {
            assertThrowsExactly(clazz, runnable, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz, Supplier<String> messageSupplier) {
            assertThatThrownBy(runnable).withFailMessage(messageSupplier).isExactlyInstanceOf(clazz);
        }
    }

    static final class AssertThrows<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz) {
            assertThrows(clazz, runnable);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz) {
            assertThatThrownBy(runnable).isInstanceOf(clazz);
        }
    }

    static final class AssertThrowsWithMessage<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz, String message) {
            assertThrows(clazz, runnable, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz, String message) {
            assertThatThrownBy(runnable).withFailMessage(message).isInstanceOf(clazz);
        }
    }

    static final class AssertThrowsWithMessageSupplier<T extends Throwable> {
        @BeforeTemplate
        void before(Executable runnable, Class<T> clazz, Supplier<String> messageSupplier) {
            assertThrows(clazz, runnable, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Class<T> clazz, Supplier<String> messageSupplier) {
            assertThatThrownBy(runnable).withFailMessage(messageSupplier).isInstanceOf(clazz);
        }
    }

    static final class AssertDoesNotThrow {
        @BeforeTemplate
        void before(Executable runnable) {
            assertDoesNotThrow(runnable);
        }

        @BeforeTemplate
        void before(ThrowingSupplier<?> runnable) {
            assertDoesNotThrow(runnable);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable) {
            assertThatCode(runnable).doesNotThrowAnyException();
        }
    }

    static final class AssertDoesNotThrowWithMessage {
        @BeforeTemplate
        void before(Executable runnable, String message) {
            assertDoesNotThrow(runnable, message);
        }

        @BeforeTemplate
        void before(ThrowingSupplier<?> runnable, String message) {
            assertDoesNotThrow(runnable, message);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, String message) {
            assertThatCode(runnable).withFailMessage(message).doesNotThrowAnyException();
        }
    }

    static final class AssertDoesNotThrowWithMessageSupplier {
        @BeforeTemplate
        void before(Executable runnable, Supplier<String> messageSupplier) {
            assertDoesNotThrow(runnable, messageSupplier);
        }

        @BeforeTemplate
        void before(ThrowingSupplier<?> runnable, Supplier<String> messageSupplier) {
            assertDoesNotThrow(runnable, messageSupplier);
        }

        @AfterTemplate
        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
        void after(ThrowingCallable runnable, Supplier<String> messageSupplier) {
            assertThatCode(runnable).withFailMessage(messageSupplier).doesNotThrowAnyException();
        }
    }

    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertTimeout
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertInstanceOf
}
