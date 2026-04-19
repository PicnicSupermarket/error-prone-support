package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.function.Supplier;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.TypeMigration;
import tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference;

/**
 * Refaster rules that replace JUnit APIs with AssertJ equivalents.
 *
 * <p><strong>Warning:</strong> while both libraries throw an {@link AssertionError} in case of an
 * assertion failure, the exact subtype used generally differs.
 */
// XXX: The `AssertThat*Array*ContainsExactly*` rules assume that `expected` and `actual` are not
// both `null`.
// XXX: The `ThrowingSupplier`-typed before-templates below should additionally require that the
// matched expression is void-compatible (i.e., also satisfies the `ThrowingCallable` functional
// interface). Without this constraint, a lambda like `() -> "constant"` can match the
// `ThrowingSupplier<?>` before-template, yet fail to compile as `ThrowingCallable` in the
// after-template. The `@Matches(IsLambdaExpressionOrMethodReference.class)` annotation added to
// these templates does not address this; a dedicated matcher is still needed.
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
      "assertTrue(BooleanSupplier)",
      "assertTrue(BooleanSupplier, String)",
      "assertTrue(BooleanSupplier, Supplier<String>)",
      "fail(Supplier<String>)"
    })
final class JUnitToAssertJRules {
  private JUnitToAssertJRules() {}

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyBoolean {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean[] actual, boolean[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyBooleanString {
    @BeforeTemplate
    void before(boolean[] actual, String newErrorMessage, boolean[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean[] actual, String newErrorMessage, boolean[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyBooleanSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(boolean[] actual, Supplier<@Nullable String> supplier, boolean[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean[] actual, Supplier<@Nullable String> supplier, boolean[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyByte {
    @BeforeTemplate
    void before(byte[] actual, byte[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(byte[] actual, byte[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyByteString {
    @BeforeTemplate
    void before(byte[] actual, String newErrorMessage, byte[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(byte[] actual, String newErrorMessage, byte[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyByteSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(byte[] actual, Supplier<@Nullable String> supplier, byte[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(byte[] actual, Supplier<@Nullable String> supplier, byte[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyChar {
    @BeforeTemplate
    void before(char[] actual, char[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(char[] actual, char[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyCharString {
    @BeforeTemplate
    void before(char[] actual, String newErrorMessage, char[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(char[] actual, String newErrorMessage, char[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyCharSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(char[] actual, Supplier<@Nullable String> supplier, char[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(char[] actual, Supplier<@Nullable String> supplier, char[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyShort {
    @BeforeTemplate
    void before(short[] actual, short[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(short[] actual, short[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyShortString {
    @BeforeTemplate
    void before(short[] actual, String newErrorMessage, short[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(short[] actual, String newErrorMessage, short[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyShortSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(short[] actual, Supplier<@Nullable String> supplier, short[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(short[] actual, Supplier<@Nullable String> supplier, short[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyInt {
    @BeforeTemplate
    void before(int[] actual, int[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int[] actual, int[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyIntString {
    @BeforeTemplate
    void before(int[] actual, String newErrorMessage, int[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int[] actual, String newErrorMessage, int[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyIntSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(int[] actual, Supplier<@Nullable String> supplier, int[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int[] actual, Supplier<@Nullable String> supplier, int[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyLong {
    @BeforeTemplate
    void before(long[] actual, long[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(long[] actual, long[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyLongString {
    @BeforeTemplate
    void before(long[] actual, String newErrorMessage, long[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(long[] actual, String newErrorMessage, long[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyLongSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(long[] actual, Supplier<@Nullable String> supplier, long[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(long[] actual, Supplier<@Nullable String> supplier, long[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyFloat {
    @BeforeTemplate
    void before(float[] actual, float[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, float[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyFloatString {
    @BeforeTemplate
    void before(float[] actual, String newErrorMessage, float[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, String newErrorMessage, float[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyFloatSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(float[] actual, Supplier<@Nullable String> supplier, float[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, Supplier<@Nullable String> supplier, float[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatContainsExactlyOffsetFloat {
    @BeforeTemplate
    void before(float[] actual, float[] values, float value) {
      assertArrayEquals(values, actual, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, float[] values, float value) {
      assertThat(actual).containsExactly(values, offset(value));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageContainsExactlyOffsetFloatString {
    @BeforeTemplate
    void before(float[] actual, String newErrorMessage, float[] values, float value) {
      assertArrayEquals(values, actual, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, String newErrorMessage, float[] values, float value) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(values, offset(value));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageContainsExactlyOffsetFloatSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(float[] actual, Supplier<@Nullable String> supplier, float[] values, float value) {
      assertArrayEquals(values, actual, value, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, Supplier<@Nullable String> supplier, float[] values, float value) {
      assertThat(actual).withFailMessage(supplier).containsExactly(values, offset(value));
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyDouble {
    @BeforeTemplate
    void before(double[] actual, double[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, double[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyDoubleString {
    @BeforeTemplate
    void before(double[] actual, String newErrorMessage, double[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, String newErrorMessage, double[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyDoubleSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(double[] actual, Supplier<@Nullable String> supplier, double[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, Supplier<@Nullable String> supplier, double[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatContainsExactlyOffsetDouble {
    @BeforeTemplate
    void before(double[] actual, double[] values, double value) {
      assertArrayEquals(values, actual, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, double[] values, double value) {
      assertThat(actual).containsExactly(values, offset(value));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageContainsExactlyOffsetDoubleString {
    @BeforeTemplate
    void before(double[] actual, String newErrorMessage, double[] values, double value) {
      assertArrayEquals(values, actual, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, String newErrorMessage, double[] values, double value) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(values, offset(value));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageContainsExactlyOffsetDoubleSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(
        double[] actual, Supplier<@Nullable String> supplier, double[] values, double value) {
      assertArrayEquals(values, actual, value, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(
        double[] actual, Supplier<@Nullable String> supplier, double[] values, double value) {
      assertThat(actual).withFailMessage(supplier).containsExactly(values, offset(value));
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatContainsExactlyObject {
    @BeforeTemplate
    void before(Object[] actual, Object[] expected) {
      assertArrayEquals(expected, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyObjectString {
    @BeforeTemplate
    void before(Object[] actual, String newErrorMessage, Object[] expected) {
      assertArrayEquals(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, String newErrorMessage, Object[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageContainsExactlyObjectSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object[] actual, Supplier<@Nullable String> supplier, Object[] expected) {
      assertArrayEquals(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Supplier<@Nullable String> supplier, Object[] expected) {
      assertThat(actual).withFailMessage(supplier).containsExactly(expected);
    }
  }

  /** Prefer {@link org.assertj.core.api.Assertions#fail()} over non-AssertJ alternatives. */
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

  /** Prefer {@link org.assertj.core.api.Assertions#fail(String)} over non-AssertJ alternatives. */
  static final class FailWithString<T> {
    @BeforeTemplate
    T before(String failureMessage) {
      return Assertions.fail(failureMessage);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    T after(String failureMessage) {
      return fail(failureMessage);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#fail(String, Throwable)} over non-AssertJ
   * alternatives.
   */
  static final class FailWithStringAndThrowable<T> {
    @BeforeTemplate
    T before(String failureMessage, Throwable realCause) {
      return Assertions.fail(failureMessage, realCause);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    T after(String failureMessage, Throwable realCause) {
      return fail(failureMessage, realCause);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#fail(Throwable)} over non-AssertJ alternatives.
   */
  static final class FailWithThrowable<T> {
    @BeforeTemplate
    T before(Throwable realCause) {
      return Assertions.fail(realCause);
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`. See `Fail` comment.
    @AfterTemplate
    @DoNotCall
    T after(Throwable realCause) {
      return fail(realCause);
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isTrue()} over non-AssertJ alternatives. */
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

  /** Prefer {@link AbstractBooleanAssert#isTrue()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsTrueString {
    @BeforeTemplate
    void before(boolean actual, String newErrorMessage) {
      assertTrue(actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String newErrorMessage) {
      assertThat(actual).withFailMessage(newErrorMessage).isTrue();
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isTrue()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsTrueSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(boolean actual, Supplier<@Nullable String> supplier) {
      assertTrue(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, Supplier<@Nullable String> supplier) {
      assertThat(actual).withFailMessage(supplier).isTrue();
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isFalse()} over non-AssertJ alternatives. */
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

  /** Prefer {@link AbstractBooleanAssert#isFalse()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsFalseString {
    @BeforeTemplate
    void before(boolean actual, String newErrorMessage) {
      assertFalse(actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String newErrorMessage) {
      assertThat(actual).withFailMessage(newErrorMessage).isFalse();
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isFalse()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsFalseSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(boolean actual, Supplier<@Nullable String> supplier) {
      assertFalse(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, Supplier<@Nullable String> supplier) {
      assertThat(actual).withFailMessage(supplier).isFalse();
    }
  }

  /** Prefer {@link AbstractAssert#isNull()} over non-AssertJ alternatives. */
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

  /** Prefer {@link AbstractAssert#isNull()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNullString {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage) {
      assertNull(actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage) {
      assertThat(actual).withFailMessage(newErrorMessage).isNull();
    }
  }

  /** Prefer {@link AbstractAssert#isNull()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNullSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object actual, Supplier<@Nullable String> supplier) {
      assertNull(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<@Nullable String> supplier) {
      assertThat(actual).withFailMessage(supplier).isNull();
    }
  }

  /** Prefer {@link AbstractAssert#isNotNull()} over non-AssertJ alternatives. */
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

  /** Prefer {@link AbstractAssert#isNotNull()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotNullString {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage) {
      assertNotNull(actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotNull();
    }
  }

  /** Prefer {@link AbstractAssert#isNotNull()} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotNullSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object actual, Supplier<@Nullable String> supplier) {
      assertNotNull(actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<@Nullable String> supplier) {
      assertThat(actual).withFailMessage(supplier).isNotNull();
    }
  }

  /** Prefer {@link AbstractAssert#isSameAs(Object)} over non-AssertJ alternatives. */
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

  /** Prefer {@link AbstractAssert#isSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsSameAsString {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object expected) {
      assertSame(expected, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object expected) {
      assertThat(actual).withFailMessage(newErrorMessage).isSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsSameAsSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object actual, Supplier<@Nullable String> supplier, Object expected) {
      assertSame(expected, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<@Nullable String> supplier, Object expected) {
      assertThat(actual).withFailMessage(supplier).isSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, Object other) {
      assertNotSame(other, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object other) {
      assertThat(actual).isNotSameAs(other);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotSameAsString {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object other) {
      assertNotSame(other, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object other) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotSameAs(other);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotSameAsSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object actual, Supplier<@Nullable String> supplier, Object other) {
      assertNotSame(other, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<@Nullable String> supplier, Object other) {
      assertThat(actual).withFailMessage(supplier).isNotSameAs(other);
    }
  }

  /**
   * Prefer {@code assertThatThrownBy(...).isExactlyInstanceOf(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatThrownByIsExactlyInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        Class<T> type) {
      assertThrowsExactly(type, shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).isExactlyInstanceOf(type);
    }
  }

  /**
   * Prefer {@code assertThatThrownBy(...).isExactlyInstanceOf(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatThrownByWithFailMessageIsExactlyInstanceOfString<
      T extends Throwable> {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        String newErrorMessage,
        Class<T> type) {
      assertThrowsExactly(type, shouldRaiseThrowable, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, String newErrorMessage, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable)
          .withFailMessage(newErrorMessage)
          .isExactlyInstanceOf(type);
    }
  }

  /**
   * Prefer {@code assertThatThrownBy(...).isExactlyInstanceOf(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatThrownByWithFailMessageIsExactlyInstanceOfSupplier<
      T extends Throwable> {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        Supplier<@Nullable String> supplier,
        Class<T> type) {
      assertThrowsExactly(type, shouldRaiseThrowable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(
        ThrowingCallable shouldRaiseThrowable, Supplier<@Nullable String> supplier, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).withFailMessage(supplier).isExactlyInstanceOf(type);
    }
  }

  /** Prefer {@code assertThatThrownBy(...).isInstanceOf(...)} over non-AssertJ alternatives. */
  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        Class<T> type) {
      assertThrows(type, shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(type);
    }
  }

  /** Prefer {@code assertThatThrownBy(...).isInstanceOf(...)} over non-AssertJ alternatives. */
  static final class AssertThatThrownByWithFailMessageIsInstanceOfString<T extends Throwable> {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        String newErrorMessage,
        Class<T> type) {
      assertThrows(type, shouldRaiseThrowable, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, String newErrorMessage, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).withFailMessage(newErrorMessage).isInstanceOf(type);
    }
  }

  /** Prefer {@code assertThatThrownBy(...).isInstanceOf(...)} over non-AssertJ alternatives. */
  static final class AssertThatThrownByWithFailMessageIsInstanceOfSupplier<T extends Throwable> {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseThrowable,
        Supplier<@Nullable String> supplier,
        Class<T> type) {
      assertThrows(type, shouldRaiseThrowable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(
        ThrowingCallable shouldRaiseThrowable, Supplier<@Nullable String> supplier, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).withFailMessage(supplier).isInstanceOf(type);
    }
  }

  /**
   * Prefer {@code assertThatCode(...).doesNotThrowAnyException()} over non-AssertJ alternatives.
   */
  static final class AssertThatCodeDoesNotThrowAnyException {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseOrNotThrowable) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable);
    }

    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class)
            ThrowingSupplier<?> shouldRaiseOrNotThrowable) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseOrNotThrowable) {
      assertThatCode(shouldRaiseOrNotThrowable).doesNotThrowAnyException();
    }
  }

  /**
   * Prefer {@code assertThatCode(...).doesNotThrowAnyException()} over non-AssertJ alternatives.
   */
  static final class AssertThatCodeWithFailMessageDoesNotThrowAnyExceptionString {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseOrNotThrowable,
        String newErrorMessage) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable, newErrorMessage);
    }

    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class)
            ThrowingSupplier<?> shouldRaiseOrNotThrowable,
        String newErrorMessage) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseOrNotThrowable, String newErrorMessage) {
      assertThatCode(shouldRaiseOrNotThrowable)
          .withFailMessage(newErrorMessage)
          .doesNotThrowAnyException();
    }
  }

  /**
   * Prefer {@code assertThatCode(...).doesNotThrowAnyException()} over non-AssertJ alternatives.
   */
  static final class AssertThatCodeWithFailMessageDoesNotThrowAnyExceptionSupplier {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) Executable shouldRaiseOrNotThrowable,
        Supplier<@Nullable String> supplier) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable, supplier);
    }

    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class)
            ThrowingSupplier<?> shouldRaiseOrNotThrowable,
        Supplier<@Nullable String> supplier) {
      assertDoesNotThrow(shouldRaiseOrNotThrowable, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseOrNotThrowable, Supplier<@Nullable String> supplier) {
      assertThatCode(shouldRaiseOrNotThrowable)
          .withFailMessage(supplier)
          .doesNotThrowAnyException();
    }
  }

  /** Prefer {@link AbstractAssert#isInstanceOf(Class)} over non-AssertJ alternatives. */
  static final class AssertThatIsInstanceOf<T> {
    @BeforeTemplate
    void before(Object actual, Class<T> type) {
      assertInstanceOf(type, actual);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Class<T> type) {
      assertThat(actual).isInstanceOf(type);
    }
  }

  /** Prefer {@link AbstractAssert#isInstanceOf(Class)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsInstanceOfString<T> {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Class<T> type) {
      assertInstanceOf(type, actual, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Class<T> type) {
      assertThat(actual).withFailMessage(newErrorMessage).isInstanceOf(type);
    }
  }

  /** Prefer {@link AbstractAssert#isInstanceOf(Class)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsInstanceOfSupplier<T> {
    @BeforeTemplate
    // XXX: Drop this suppression once the SonarCloud false positive is resolved.
    @SuppressWarnings("java:S4449" /* SonarCloud thinks that `supplier` itself is `@Nullable`. */)
    void before(Object actual, Supplier<@Nullable String> supplier, Class<T> type) {
      assertInstanceOf(type, actual, supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Supplier<@Nullable String> supplier, Class<T> type) {
      assertThat(actual).withFailMessage(supplier).isInstanceOf(type);
    }
  }
}
