package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.function.Supplier;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules to replace JUnit assertions with AssertJ equivalents. */
@OnlineDocumentation
final class JUnitToAssertJRules {
    private JUnitToAssertJRules() {}
    
    // XXX: Rewrite org.junit.jupiter.api.Assertions.fail

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

    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertFalse
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertNull
    // XXX: Rewrite org.junit.jupiter.api.Assertions.assertNotNull
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
