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
import tech.picnic.errorprone.refaster.annotation.TypeMigration;

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
@TypeMigration(
    of = Assertions.class,
    unmigratedMethods = {
      "assertAll(Collection<Executable>)",
      "assertAll(Executable[])",
      "assertAll(Stream<Executable>)",
      "assertAll(String, Collection<Executable>)",
      "assertAll(String, Executable[])",
      "assertAll(String, Stream<Executable>)",
      "assertArrayEquals(boolean[], boolean[])",
      "assertArrayEquals(boolean[], boolean[], String)",
      "assertArrayEquals(boolean[], boolean[], Supplier<String>)",
      "assertArrayEquals(byte[], byte[])",
      "assertArrayEquals(byte[], byte[], String)",
      "assertArrayEquals(byte[], byte[], Supplier<String>)",
      "assertArrayEquals(char[], char[])",
      "assertArrayEquals(char[], char[], String)",
      "assertArrayEquals(char[], char[], Supplier<String>)",
      "assertArrayEquals(double[], double[])",
      "assertArrayEquals(double[], double[], double)",
      "assertArrayEquals(double[], double[], double, String)",
      "assertArrayEquals(double[], double[], double, Supplier<String>)",
      "assertArrayEquals(double[], double[], String)",
      "assertArrayEquals(double[], double[], Supplier<String>)",
      "assertArrayEquals(float[], float[])",
      "assertArrayEquals(float[], float[], float)",
      "assertArrayEquals(float[], float[], float, String)",
      "assertArrayEquals(float[], float[], float, Supplier<String>)",
      "assertArrayEquals(float[], float[], String)",
      "assertArrayEquals(float[], float[], Supplier<String>)",
      "assertArrayEquals(int[], int[])",
      "assertArrayEquals(int[], int[], String)",
      "assertArrayEquals(int[], int[], Supplier<String>)",
      "assertArrayEquals(long[], long[])",
      "assertArrayEquals(long[], long[], String)",
      "assertArrayEquals(long[], long[], Supplier<String>)",
      "assertArrayEquals(Object[], Object[])",
      "assertArrayEquals(Object[], Object[], String)",
      "assertArrayEquals(Object[], Object[], Supplier<String>)",
      "assertArrayEquals(short[], short[])",
      "assertArrayEquals(short[], short[], String)",
      "assertArrayEquals(short[], short[], Supplier<String>)",
      "assertEquals(Byte, Byte)",
      "assertEquals(Byte, byte)",
      "assertEquals(byte, Byte)",
      "assertEquals(byte, byte)",
      "assertEquals(Byte, Byte, String)",
      "assertEquals(Byte, byte, String)",
      "assertEquals(byte, Byte, String)",
      "assertEquals(byte, byte, String)",
      "assertEquals(Byte, Byte, Supplier<String>)",
      "assertEquals(Byte, byte, Supplier<String>)",
      "assertEquals(byte, Byte, Supplier<String>)",
      "assertEquals(byte, byte, Supplier<String>)",
      "assertEquals(char, char)",
      "assertEquals(char, char, String)",
      "assertEquals(char, char, Supplier<String>)",
      "assertEquals(char, Character)",
      "assertEquals(char, Character, String)",
      "assertEquals(char, Character, Supplier<String>)",
      "assertEquals(Character, char)",
      "assertEquals(Character, char, String)",
      "assertEquals(Character, char, Supplier<String>)",
      "assertEquals(Character, Character)",
      "assertEquals(Character, Character, String)",
      "assertEquals(Character, Character, Supplier<String>)",
      "assertEquals(Double, Double)",
      "assertEquals(Double, double)",
      "assertEquals(double, Double)",
      "assertEquals(double, double)",
      "assertEquals(double, double, double)",
      "assertEquals(double, double, double, String)",
      "assertEquals(double, double, double, Supplier<String>)",
      "assertEquals(Double, Double, String)",
      "assertEquals(Double, double, String)",
      "assertEquals(double, Double, String)",
      "assertEquals(double, double, String)",
      "assertEquals(Double, Double, Supplier<String>)",
      "assertEquals(Double, double, Supplier<String>)",
      "assertEquals(double, Double, Supplier<String>)",
      "assertEquals(double, double, Supplier<String>)",
      "assertEquals(Float, Float)",
      "assertEquals(Float, float)",
      "assertEquals(float, Float)",
      "assertEquals(float, float)",
      "assertEquals(float, float, float)",
      "assertEquals(float, float, float, String)",
      "assertEquals(float, float, float, Supplier<String>)",
      "assertEquals(Float, Float, String)",
      "assertEquals(Float, float, String)",
      "assertEquals(float, Float, String)",
      "assertEquals(float, float, String)",
      "assertEquals(Float, Float, Supplier<String>)",
      "assertEquals(Float, float, Supplier<String>)",
      "assertEquals(float, Float, Supplier<String>)",
      "assertEquals(float, float, Supplier<String>)",
      "assertEquals(int, int)",
      "assertEquals(int, int, String)",
      "assertEquals(int, int, Supplier<String>)",
      "assertEquals(int, Integer)",
      "assertEquals(int, Integer, String)",
      "assertEquals(int, Integer, Supplier<String>)",
      "assertEquals(Integer, int)",
      "assertEquals(Integer, int, String)",
      "assertEquals(Integer, int, Supplier<String>)",
      "assertEquals(Integer, Integer)",
      "assertEquals(Integer, Integer, String)",
      "assertEquals(Integer, Integer, Supplier<String>)",
      "assertEquals(Long, Long)",
      "assertEquals(Long, long)",
      "assertEquals(long, Long)",
      "assertEquals(long, long)",
      "assertEquals(Long, Long, String)",
      "assertEquals(Long, long, String)",
      "assertEquals(long, Long, String)",
      "assertEquals(long, long, String)",
      "assertEquals(Long, Long, Supplier<String>)",
      "assertEquals(Long, long, Supplier<String>)",
      "assertEquals(long, Long, Supplier<String>)",
      "assertEquals(long, long, Supplier<String>)",
      "assertEquals(Object, Object)",
      "assertEquals(Object, Object, String)",
      "assertEquals(Object, Object, Supplier<String>)",
      "assertEquals(Short, Short)",
      "assertEquals(Short, short)",
      "assertEquals(short, Short)",
      "assertEquals(short, short)",
      "assertEquals(Short, Short, String)",
      "assertEquals(Short, short, String)",
      "assertEquals(short, Short, String)",
      "assertEquals(short, short, String)",
      "assertEquals(Short, Short, Supplier<String>)",
      "assertEquals(Short, short, Supplier<String>)",
      "assertEquals(short, Short, Supplier<String>)",
      "assertEquals(short, short, Supplier<String>)",
      "assertFalse(BooleanSupplier)",
      "assertFalse(BooleanSupplier, String)",
      "assertFalse(BooleanSupplier, Supplier<String>)",
      "assertIterableEquals(Iterable<?>, Iterable<?>)",
      "assertIterableEquals(Iterable<?>, Iterable<?>, String)",
      "assertIterableEquals(Iterable<?>, Iterable<?>, Supplier<String>)",
      "assertLinesMatch(List<String>, List<String>)",
      "assertLinesMatch(List<String>, List<String>, String)",
      "assertLinesMatch(List<String>, List<String>, Supplier<String>)",
      "assertLinesMatch(Stream<String>, Stream<String>)",
      "assertLinesMatch(Stream<String>, Stream<String>, String)",
      "assertLinesMatch(Stream<String>, Stream<String>, Supplier<String>)",
      "assertNotEquals(Byte, Byte)",
      "assertNotEquals(Byte, byte)",
      "assertNotEquals(byte, Byte)",
      "assertNotEquals(byte, byte)",
      "assertNotEquals(Byte, Byte, String)",
      "assertNotEquals(Byte, byte, String)",
      "assertNotEquals(byte, Byte, String)",
      "assertNotEquals(byte, byte, String)",
      "assertNotEquals(Byte, Byte, Supplier<String>)",
      "assertNotEquals(Byte, byte, Supplier<String>)",
      "assertNotEquals(byte, Byte, Supplier<String>)",
      "assertNotEquals(byte, byte, Supplier<String>)",
      "assertNotEquals(char, char)",
      "assertNotEquals(char, char, String)",
      "assertNotEquals(char, char, Supplier<String>)",
      "assertNotEquals(char, Character)",
      "assertNotEquals(char, Character, String)",
      "assertNotEquals(char, Character, Supplier<String>)",
      "assertNotEquals(Character, char)",
      "assertNotEquals(Character, char, String)",
      "assertNotEquals(Character, char, Supplier<String>)",
      "assertNotEquals(Character, Character)",
      "assertNotEquals(Character, Character, String)",
      "assertNotEquals(Character, Character, Supplier<String>)",
      "assertNotEquals(Double, Double)",
      "assertNotEquals(Double, double)",
      "assertNotEquals(double, Double)",
      "assertNotEquals(double, double)",
      "assertNotEquals(double, double, double)",
      "assertNotEquals(double, double, double, String)",
      "assertNotEquals(double, double, double, Supplier<String>)",
      "assertNotEquals(Double, Double, String)",
      "assertNotEquals(Double, double, String)",
      "assertNotEquals(double, Double, String)",
      "assertNotEquals(double, double, String)",
      "assertNotEquals(Double, Double, Supplier<String>)",
      "assertNotEquals(Double, double, Supplier<String>)",
      "assertNotEquals(double, Double, Supplier<String>)",
      "assertNotEquals(double, double, Supplier<String>)",
      "assertNotEquals(Float, Float)",
      "assertNotEquals(Float, float)",
      "assertNotEquals(float, Float)",
      "assertNotEquals(float, float)",
      "assertNotEquals(float, float, float)",
      "assertNotEquals(float, float, float, String)",
      "assertNotEquals(float, float, float, Supplier<String>)",
      "assertNotEquals(Float, Float, String)",
      "assertNotEquals(Float, float, String)",
      "assertNotEquals(float, Float, String)",
      "assertNotEquals(float, float, String)",
      "assertNotEquals(Float, Float, Supplier<String>)",
      "assertNotEquals(Float, float, Supplier<String>)",
      "assertNotEquals(float, Float, Supplier<String>)",
      "assertNotEquals(float, float, Supplier<String>)",
      "assertNotEquals(int, int)",
      "assertNotEquals(int, int, String)",
      "assertNotEquals(int, int, Supplier<String>)",
      "assertNotEquals(int, Integer)",
      "assertNotEquals(int, Integer, String)",
      "assertNotEquals(int, Integer, Supplier<String>)",
      "assertNotEquals(Integer, int)",
      "assertNotEquals(Integer, int, String)",
      "assertNotEquals(Integer, int, Supplier<String>)",
      "assertNotEquals(Integer, Integer)",
      "assertNotEquals(Integer, Integer, String)",
      "assertNotEquals(Integer, Integer, Supplier<String>)",
      "assertNotEquals(Long, Long)",
      "assertNotEquals(Long, long)",
      "assertNotEquals(long, Long)",
      "assertNotEquals(long, long)",
      "assertNotEquals(Long, Long, String)",
      "assertNotEquals(Long, long, String)",
      "assertNotEquals(long, Long, String)",
      "assertNotEquals(long, long, String)",
      "assertNotEquals(Long, Long, Supplier<String>)",
      "assertNotEquals(Long, long, Supplier<String>)",
      "assertNotEquals(long, Long, Supplier<String>)",
      "assertNotEquals(long, long, Supplier<String>)",
      "assertNotEquals(Object, Object)",
      "assertNotEquals(Object, Object, String)",
      "assertNotEquals(Object, Object, Supplier<String>)",
      "assertNotEquals(Short, Short)",
      "assertNotEquals(Short, short)",
      "assertNotEquals(short, Short)",
      "assertNotEquals(short, short)",
      "assertNotEquals(Short, Short, String)",
      "assertNotEquals(Short, short, String)",
      "assertNotEquals(short, Short, String)",
      "assertNotEquals(short, short, String)",
      "assertNotEquals(Short, Short, Supplier<String>)",
      "assertNotEquals(Short, short, Supplier<String>)",
      "assertNotEquals(short, Short, Supplier<String>)",
      "assertNotEquals(short, short, Supplier<String>)",
      "assertTimeout(Duration, Executable)",
      "assertTimeout(Duration, Executable, String)",
      "assertTimeout(Duration, Executable, Supplier<String>)",
      "assertTimeout(Duration, ThrowingSupplier<T>)",
      "assertTimeout(Duration, ThrowingSupplier<T>, String)",
      "assertTimeout(Duration, ThrowingSupplier<T>, Supplier<String>)",
      "assertTimeoutPreemptively(Duration, Executable)",
      "assertTimeoutPreemptively(Duration, Executable, String)",
      "assertTimeoutPreemptively(Duration, Executable, Supplier<String>)",
      "assertTimeoutPreemptively(Duration, ThrowingSupplier<T>)",
      "assertTimeoutPreemptively(Duration, ThrowingSupplier<T>, String)",
      "assertTimeoutPreemptively(Duration, ThrowingSupplier<T>, Supplier<String>)",
      "assertTimeoutPreemptively(Duration, ThrowingSupplier<T>, Supplier<String>, TimeoutFailureFactory<E>)",
      "assertTrue(BooleanSupplier)",
      "assertTrue(BooleanSupplier, String)",
      "assertTrue(BooleanSupplier, Supplier<String>)",
      "fail(Supplier<String>)"
    })
final class JUnitToAssertJRules {
  private JUnitToAssertJRules() {}

  static final class Fail<T> {
    @BeforeTemplate
    T before() {
      return Assertions.fail();
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)` once
    // https://github.com/google/error-prone/pull/3584 is resolved. Until that time, statically
    // importing AssertJ's `fail` is likely to clash with an existing static import of JUnit's
    // `fail`. Note that combining Error Prone's `RemoveUnusedImports` and
    // `UnnecessarilyFullyQualified` checks and our `StaticImport` check will anyway cause the
    // method to be imported statically if possible; just in a less efficient manner.
    @AfterTemplate
    @DoNotCall
    T after() {
      return fail();
    }
  }

  static final class FailWithMessage<T> {
    @BeforeTemplate
    T before(String message) {
      return Assertions.fail(message);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    T after(String message) {
      return fail(message);
    }
  }

  static final class FailWithMessageAndThrowable<T> {
    @BeforeTemplate
    T before(String message, Throwable throwable) {
      return Assertions.fail(message, throwable);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    T after(String message, Throwable throwable) {
      return fail(message, throwable);
    }
  }

  static final class FailWithThrowable<T> {
    @BeforeTemplate
    T before(Throwable throwable) {
      return Assertions.fail(throwable);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    @DoNotCall
    T after(Throwable throwable) {
      return fail(throwable);
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
    void before(Object actual, String message, Object expected) {
      assertSame(expected, actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isSameAs(expected);
    }
  }

  static final class AssertThatWithFailMessageSupplierIsSameAs {
    @BeforeTemplate
    void before(Object actual, Supplier<String> supplier, Object expected) {
      assertSame(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<String> supplier, Object expected) {
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
    void before(Object actual, String message, Object expected) {
      assertNotSame(expected, actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isNotSameAs(expected);
    }
  }

  static final class AssertThatWithFailMessageSupplierIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, Supplier<String> supplier, Object expected) {
      assertNotSame(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<String> supplier, Object expected) {
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
    void before(Executable throwingCallable, String message, Class<T> clazz) {
      assertThrowsExactly(clazz, throwingCallable, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, String message, Class<T> clazz) {
      assertThatThrownBy(throwingCallable).withFailMessage(message).isExactlyInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageSupplierIsExactlyInstanceOf<
      T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Supplier<String> supplier, Class<T> clazz) {
      assertThrowsExactly(clazz, throwingCallable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Supplier<String> supplier, Class<T> clazz) {
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
    void before(Executable throwingCallable, String message, Class<T> clazz) {
      assertThrows(clazz, throwingCallable, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, String message, Class<T> clazz) {
      assertThatThrownBy(throwingCallable).withFailMessage(message).isInstanceOf(clazz);
    }
  }

  static final class AssertThatThrownByWithFailMessageSupplierIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(Executable throwingCallable, Supplier<String> supplier, Class<T> clazz) {
      assertThrows(clazz, throwingCallable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Supplier<String> supplier, Class<T> clazz) {
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
    void before(Object actual, String message, Class<T> clazz) {
      assertInstanceOf(clazz, actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Class<T> clazz) {
      assertThat(actual).withFailMessage(message).isInstanceOf(clazz);
    }
  }

  static final class AssertThatWithFailMessageSupplierIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object actual, Supplier<String> supplier, Class<T> clazz) {
      assertInstanceOf(clazz, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<String> supplier, Class<T> clazz) {
      assertThat(actual).withFailMessage(supplier).isInstanceOf(clazz);
    }
  }
}
