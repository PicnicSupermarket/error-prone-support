package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
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

  static final class ThrowNewAssertionError {
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

  static final class AssertThatIsTrue {
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

  static final class AssertThatWithFailMessageStringIsTrue {
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

  static final class AssertThatWithFailMessageSupplierIsTrue {
    @BeforeTemplate
    void before(boolean condition, Supplier<String> supplier) {
      assertTrue(condition, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, Supplier<String> supplier) {
      assertThat(condition).withFailMessage(supplier).isTrue();
    }
  }

  static final class AssertThatIsFalse {
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

  static final class AssertThatWithFailMessageStringIsFalse {
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

  static final class AssertThatWithFailMessageSupplierIsFalse {
    @BeforeTemplate
    void before(boolean condition, Supplier<String> supplier) {
      assertFalse(condition, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, Supplier<String> supplier) {
      assertThat(condition).withFailMessage(supplier).isFalse();
    }
  }

  static final class AssertThatIsNull {
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

  static final class AssertThatWithFailMessageStringIsNull {
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

  static final class AssertThatWithFailMessageSupplierIsNull {
    @BeforeTemplate
    void before(Object object, Supplier<String> supplier) {
      assertNull(object, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object object, Supplier<String> supplier) {
      assertThat(object).withFailMessage(supplier).isNull();
    }
  }

  static final class AssertThatIsNotNull {
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

  static final class AssertThatWithFailMessageStringIsNotNull {
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

  static final class AssertThatWithFailMessageSupplierIsNotNull {
    @BeforeTemplate
    void before(Object object, Supplier<String> supplier) {
      assertNotNull(object, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object object, Supplier<String> supplier) {
      assertThat(object).withFailMessage(supplier).isNotNull();
    }
  }

  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertEquals`.
  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertArrayEquals`.
  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertIterableEquals`.
  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertLinesMatch`.
  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertNotEquals`.

  static final class AssertThatIsSameAs {
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

  static final class AssertThatWithFailMessageStringIsSameAs {
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

  static final class AssertThatWithFailMessageSupplierIsSameAs {
    @BeforeTemplate
    void before(Object actual, Object expected, Supplier<String> supplier) {
      assertSame(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isSameAs(expected);
    }
  }

  static final class AssertThatIsNotSameAs {
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

  static final class AssertThatWithFailMessageStringIsNotSameAs {
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

  static final class AssertThatWithFailMessageSupplierIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, Object expected, Supplier<String> supplier) {
      assertNotSame(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isNotSameAs(expected);
    }
  }

  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertAll`.

  // XXX: Switch params?
  static final class AssertThatThrownByIsExactlyInstanceOf<T extends Throwable> {
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

  static final class AssertThatThrownByWithFailMessageStringIsExactlyInstanceOf<
      T extends Throwable> {
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

  static final class AssertThatThrownByWithFailMessageSupplierIsExactlyInstanceOf<
      T extends Throwable> {
    @BeforeTemplate
    void before(Executable runnable, Class<T> clazz, Supplier<String> supplier) {
      assertThrowsExactly(clazz, runnable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable runnable, Class<T> clazz, Supplier<String> supplier) {
      assertThatThrownBy(runnable).withFailMessage(supplier).isExactlyInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
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

  static final class AssertThatThrownByWithFailMessageStringIsInstanceOf<T extends Throwable> {
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

  static final class AssertThatThrownByWithFailMessageSupplierIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable runnable, Class<T> clazz, Supplier<String> supplier) {
      assertThrows(clazz, runnable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable runnable, Class<T> clazz, Supplier<String> supplier) {
      assertThatThrownBy(runnable).withFailMessage(supplier).isInstanceOf(clazz);
    }
  }

  static final class AssertThatCodeDoesNotThrowAnyException {
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

  static final class AssertThatCodeWithFailMessageStringDoesNotThrowAnyException {
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

  static final class AssertThatCodeWithFailMessageSupplierDoesNotThrowAnyException {
    @BeforeTemplate
    void before(Executable runnable, Supplier<String> supplier) {
      assertDoesNotThrow(runnable, supplier);
    }

    @BeforeTemplate
    void before(ThrowingSupplier<?> runnable, Supplier<String> supplier) {
      assertDoesNotThrow(runnable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable runnable, Supplier<String> supplier) {
      assertThatCode(runnable).withFailMessage(supplier).doesNotThrowAnyException();
    }
  }

  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertTimeout`.
  // XXX: Rewrite `org.junit.jupiter.api.Assertions.assertTimeoutPreemptively`.

  static final class AssertThatIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object object, Class<T> clazz) {
      assertInstanceOf(clazz, object);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object object, Class<T> clazz) {
      assertThat(object).isInstanceOf(clazz);
    }
  }

  static final class AssertThatWithFailMessageStringIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object object, Class<T> clazz, String message) {
      assertInstanceOf(clazz, object, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object object, Class<T> clazz, String message) {
      assertThat(object).withFailMessage(message).isInstanceOf(clazz);
    }
  }

  static final class AssertThatWithFailMessageSupplierIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object object, Class<T> clazz, Supplier<String> supplier) {
      assertInstanceOf(clazz, object, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object object, Class<T> clazz, Supplier<String> supplier) {
      assertThat(object).withFailMessage(supplier).isInstanceOf(clazz);
    }
  }
}
