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

import com.google.common.collect.ImmutableSet;
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

/**
 * Refaster rules to replace JUnit assertions with AssertJ equivalents.
 *
 * <p>Note that, while both libraries throw an {@link AssertionError} in case of an assertion
 * failure, the exact subtype used generally differs.
 */
// XXX: Not all JUnit `Assertions` methods have an associated Refaster rule yet; expand this class.
// XXX: Introduce a `@Matcher` on `Executable` and `ThrowingSupplier` expressions, such that they
// are only matched if they are also compatible with the `ThrowingCallable` functional interface.
// When implementing such a matcher, note that expressions with a non-void return type such as
// `() -> toString()` match both `ThrowingSupplier` and `ThrowingCallable`, but `() -> "constant"`
// is only compatible with the former.
@OnlineDocumentation
final class JUnitToAssertJRules {
  private JUnitToAssertJRules() {}

  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Assertions.class,
        assertDoesNotThrow(() -> null),
        assertInstanceOf(null, null),
        assertThrows(null, null),
        assertThrowsExactly(null, null),
        (Runnable) () -> assertFalse(true),
        (Runnable) () -> assertNotNull(null),
        (Runnable) () -> assertNotSame(null, null),
        (Runnable) () -> assertNull(null),
        (Runnable) () -> assertSame(null, null),
        (Runnable) () -> assertTrue(true));
  }

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

  static final class FailWithMessage<T> {
    @BeforeTemplate
    T before(String message) {
      return Assertions.fail(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(String message) {
      return fail(message);
    }
  }

  static final class FailWithMessageAndThrowable<T> {
    @BeforeTemplate
    T before(String message, Throwable throwable) {
      return Assertions.fail(message, throwable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(String message, Throwable throwable) {
      return fail(message, throwable);
    }
  }

  static final class FailWithThrowable {
    @BeforeTemplate
    void before(Throwable throwable) {
      Assertions.fail(throwable);
    }

    @AfterTemplate
    @DoNotCall
    void after(Throwable throwable) {
      throw new AssertionError(throwable);
    }
  }

  static final class AssertThatIsTrue {
    @BeforeTemplate
    void before(boolean actual) {
      assertTrue(actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual) {
      assertThat(actual).isTrue();
    }
  }

  static final class AssertThatWithFailMessageStringIsTrue {
    @BeforeTemplate
    void before(boolean actual, String message) {
      assertTrue(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String message) {
      assertThat(actual).withFailMessage(message).isTrue();
    }
  }

  static final class AssertThatWithFailMessageSupplierIsTrue {
    @BeforeTemplate
    void before(boolean actual, Supplier<String> supplier) {
      assertTrue(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isTrue();
    }
  }

  static final class AssertThatIsFalse {
    @BeforeTemplate
    void before(boolean actual) {
      assertFalse(actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual) {
      assertThat(actual).isFalse();
    }
  }

  static final class AssertThatWithFailMessageStringIsFalse {
    @BeforeTemplate
    void before(boolean actual, String message) {
      assertFalse(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String message) {
      assertThat(actual).withFailMessage(message).isFalse();
    }
  }

  static final class AssertThatWithFailMessageSupplierIsFalse {
    @BeforeTemplate
    void before(boolean actual, Supplier<String> supplier) {
      assertFalse(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isFalse();
    }
  }

  static final class AssertThatIsNull {
    @BeforeTemplate
    void before(Object actual) {
      assertNull(actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual) {
      assertThat(actual).isNull();
    }
  }

  static final class AssertThatWithFailMessageStringIsNull {
    @BeforeTemplate
    void before(Object actual, String message) {
      assertNull(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message) {
      assertThat(actual).withFailMessage(message).isNull();
    }
  }

  static final class AssertThatWithFailMessageSupplierIsNull {
    @BeforeTemplate
    void before(Object actual, Supplier<String> supplier) {
      assertNull(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isNull();
    }
  }

  static final class AssertThatIsNotNull {
    @BeforeTemplate
    void before(Object actual) {
      assertNotNull(actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual) {
      assertThat(actual).isNotNull();
    }
  }

  static final class AssertThatWithFailMessageStringIsNotNull {
    @BeforeTemplate
    void before(Object actual, String message) {
      assertNotNull(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message) {
      assertThat(actual).withFailMessage(message).isNotNull();
    }
  }

  static final class AssertThatWithFailMessageSupplierIsNotNull {
    @BeforeTemplate
    void before(Object actual, Supplier<String> supplier) {
      assertNotNull(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isNotNull();
    }
  }

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

  static final class AssertThatThrownByIsExactlyInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz) {
      assertThrowsExactly(clazz, throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz) {
      assertThatThrownBy(throwingCallable).isExactlyInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageStringIsExactlyInstanceOf<
      T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz, String message) {
      assertThrowsExactly(clazz, throwingCallable, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz, String message) {
      assertThatThrownBy(throwingCallable).withFailMessage(message).isExactlyInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageSupplierIsExactlyInstanceOf<
      T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz, Supplier<String> supplier) {
      assertThrowsExactly(clazz, throwingCallable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz, Supplier<String> supplier) {
      assertThatThrownBy(throwingCallable).withFailMessage(supplier).isExactlyInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz) {
      assertThrows(clazz, throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz) {
      assertThatThrownBy(throwingCallable).isInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageStringIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz, String message) {
      assertThrows(clazz, throwingCallable, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz, String message) {
      assertThatThrownBy(throwingCallable).withFailMessage(message).isInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageSupplierIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Class<T> clazz, Supplier<String> supplier) {
      assertThrows(clazz, throwingCallable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> clazz, Supplier<String> supplier) {
      assertThatThrownBy(throwingCallable).withFailMessage(supplier).isInstanceOf(clazz);
    }
  }

  static final class AssertThatCodeDoesNotThrowAnyException {
    @BeforeTemplate
    void before(Executable throwingCallable) {
      assertDoesNotThrow(throwingCallable);
    }

    @BeforeTemplate
    void before(ThrowingSupplier<?> throwingCallable) {
      assertDoesNotThrow(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable) {
      assertThatCode(throwingCallable).doesNotThrowAnyException();
    }
  }

  static final class AssertThatCodeWithFailMessageStringDoesNotThrowAnyException {
    @BeforeTemplate
    void before(Executable throwingCallable, String message) {
      assertDoesNotThrow(throwingCallable, message);
    }

    @BeforeTemplate
    void before(ThrowingSupplier<?> throwingCallable, String message) {
      assertDoesNotThrow(throwingCallable, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, String message) {
      assertThatCode(throwingCallable).withFailMessage(message).doesNotThrowAnyException();
    }
  }

  static final class AssertThatCodeWithFailMessageSupplierDoesNotThrowAnyException {
    @BeforeTemplate
    void before(Executable throwingCallable, Supplier<String> supplier) {
      assertDoesNotThrow(throwingCallable, supplier);
    }

    @BeforeTemplate
    void before(ThrowingSupplier<?> throwingCallable, Supplier<String> supplier) {
      assertDoesNotThrow(throwingCallable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Supplier<String> supplier) {
      assertThatCode(throwingCallable).withFailMessage(supplier).doesNotThrowAnyException();
    }
  }

  static final class AssertThatIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object actual, Class<T> clazz) {
      assertInstanceOf(clazz, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Class<T> clazz) {
      assertThat(actual).isInstanceOf(clazz);
    }
  }

  static final class AssertThatWithFailMessageStringIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object actual, Class<T> clazz, String message) {
      assertInstanceOf(clazz, actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Class<T> clazz, String message) {
      assertThat(actual).withFailMessage(message).isInstanceOf(clazz);
    }
  }

  static final class AssertThatWithFailMessageSupplierIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object actual, Class<T> clazz, Supplier<String> supplier) {
      assertInstanceOf(clazz, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Class<T> clazz, Supplier<String> supplier) {
      assertThat(actual).withFailMessage(supplier).isInstanceOf(clazz);
    }
  }
}
