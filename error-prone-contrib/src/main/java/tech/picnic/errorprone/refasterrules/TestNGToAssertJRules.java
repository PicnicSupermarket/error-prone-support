package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.offset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.data.Offset;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.annotation.TypeMigration;
import tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference;

/**
 * Refaster rules that replace TestNG APIs with AssertJ equivalents.
 *
 * <p>Some of the classes below have TestNG {@code @BeforeTemplate}s that reference wildcard type
 * bounds ({@code <?>}), while the associated AssertJ {@code @AfterTemplate}s reference stricter
 * type bounds. This introduces the risk of producing invalid code. We do this anyway, because
 * TestNG's wildcard types can cause javac to infer less specific types than AssertJ requires, while
 * the appropriate (more specific) types _will_ be inferred properly when plugged into AssertJ's
 * API.
 *
 * <p>The following is an example of a TestNG statement, which would not be rewritten if it weren't
 * for the wildcard matching (note that the type parameters of the map on the right-hand side will
 * be inferred to be {@code <Object, Object>} rather than {@code <String, Object>}).
 *
 * <pre>{@code
 * List<Map<String, Object>> myMaps = new ArrayList<>();
 * assertEquals(myMaps, ImmutableList.of(ImmutableMap.of()));
 * }</pre>
 *
 * <p><strong>Warning:</strong> while both libraries throw an {@link AssertionError} in case of an
 * assertion failure, the exact subtype used generally differs.
 */
// XXX: As-is these rules do not result in a complete migration:
// - Expressions containing comments are skipped due to a limitation of Refaster.
// - Assertions inside lambda expressions are also skipped. Unclear why.
@OnlineDocumentation
@TypeMigration(
    of = Assert.class,
    unmigratedMethods = {
      /*
       * These `assertEqualsDeep` methods cannot (easily) be expressed using AssertJ because they
       * mix regular equality and array equality:
       */
      "assertEqualsDeep(Map<?, ?>, Map<?, ?>)",
      "assertEqualsDeep(Map<?, ?>, Map<?, ?>, String)",
      "assertEqualsDeep(Set<?>, Set<?>)",
      "assertEqualsDeep(Set<?>, Set<?>, String)",
      // XXX: Add migrations for the methods below.
      "assertEqualsNoOrder(Collection<?>, Collection<?>)",
      "assertEqualsNoOrder(Collection<?>, Collection<?>, String)",
      "assertEqualsNoOrder(Iterator<?>, Iterator<?>)",
      "assertEqualsNoOrder(Iterator<?>, Iterator<?>, String)",
      "assertListContains(List<T>, Predicate<T>, String)",
      "assertListContainsObject(List<T>, T, String)",
      "assertListNotContains(List<T>, Predicate<T>, String)",
      "assertListNotContainsObject(List<T>, T, String)",
      "assertNotEquals(Collection<?>, Collection<?>)",
      "assertNotEquals(Collection<?>, Collection<?>, String)",
      "assertNotEquals(Iterator<?>, Iterator<?>)",
      "assertNotEquals(Iterator<?>, Iterator<?>, String)",
      "assertNotEquals(Object[], Object[], String)",
      /*
       * These `assertNotEqualsDeep` methods cannot (easily) be expressed using AssertJ because they
       * mix regular equality and array equality:
       */
      "assertNotEqualsDeep(Map<?, ?>, Map<?, ?>)",
      "assertNotEqualsDeep(Map<?, ?>, Map<?, ?>, String)",
      "assertNotEqualsDeep(Set<?>, Set<?>)",
      "assertNotEqualsDeep(Set<?>, Set<?>, String)",
      // XXX: Add a migration for this `assertThrows` method.
      "assertThrows(String, Class<T>, ThrowingRunnable)",
      /*
       * These `expectThrows` methods return the caught exception; there is no direct counterpart
       * for this in AssertJ.
       */
      "expectThrows(Class<T>, ThrowingRunnable)",
      "expectThrows(String, Class<T>, ThrowingRunnable)"
    })
final class TestNGToAssertJRules {
  private TestNGToAssertJRules() {}

  /** Prefer {@link Assertions#fail()} over non-AssertJ alternatives. */
  static final class Fail {
    @BeforeTemplate
    void before() {
      Assert.fail();
    }

    @AfterTemplate
    @DoNotCall
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after() {
      fail();
    }
  }

  /** Prefer {@link Assertions#fail(String)} over non-AssertJ alternatives. */
  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithString {
    @BeforeTemplate
    void before(String failureMessage) {
      Assert.fail(failureMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String failureMessage) {
      fail(failureMessage);
    }
  }

  /** Prefer {@link Assertions#fail(String, Throwable)} over non-AssertJ alternatives. */
  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithStringAndThrowable {
    @BeforeTemplate
    void before(String failureMessage, Throwable realCause) {
      Assert.fail(failureMessage, realCause);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String failureMessage, Throwable realCause) {
      fail(failureMessage, realCause);
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
  static final class AssertThatWithFailMessageIsTrue {
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
  static final class AssertThatWithFailMessageIsFalse {
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
  static final class AssertThatWithFailMessageIsNull {
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
  static final class AssertThatWithFailMessageIsNotNull {
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

  /** Prefer {@link AbstractAssert#isSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatIsSameAs {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertSame(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsSameAs {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object expected) {
      assertSame(actual, expected, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object expected) {
      assertThat(actual).withFailMessage(newErrorMessage).isSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, Object other) {
      assertNotSame(actual, other);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object other) {
      assertThat(actual).isNotSameAs(other);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object other) {
      assertNotSame(actual, other, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object other) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotSameAs(other);
    }
  }

  /** Prefer {@link AbstractAssert#isEqualTo(Object)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S1448" /* Each variant requires a separate `@BeforeTemplate` method. */)
  static final class AssertThatIsEqualTo {
    @BeforeTemplate
    void before(boolean actual, boolean expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(boolean actual, Boolean expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Boolean actual, boolean expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Boolean actual, Boolean expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte actual, byte expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte actual, Byte expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Byte actual, byte expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Byte actual, Byte expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char actual, char expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char actual, Character expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Character actual, char expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Character actual, Character expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short actual, short expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short actual, Short expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Short actual, short expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Short actual, Short expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int actual, int expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int actual, Integer expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Integer actual, int expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Integer actual, Integer expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long actual, long expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long actual, Long expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Long actual, long expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Long actual, Long expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float actual, float expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float actual, Float expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Float actual, float expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Float actual, Float expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double actual, double expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double actual, Double expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Double actual, double expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Double actual, Double expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(String actual, String expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, Map<?, ?> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isEqualTo(Object)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S1448" /* Each variant requires a separate `@BeforeTemplate` method. */)
  static final class AssertThatWithFailMessageIsEqualTo {
    @BeforeTemplate
    void before(boolean actual, String newErrorMessage, boolean expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(boolean actual, String newErrorMessage, Boolean expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Boolean actual, String newErrorMessage, boolean expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Boolean actual, String newErrorMessage, Boolean expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(byte actual, String newErrorMessage, byte expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(byte actual, String newErrorMessage, Byte expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Byte actual, String newErrorMessage, byte expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Byte actual, String newErrorMessage, Byte expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(char actual, String newErrorMessage, char expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(char actual, String newErrorMessage, Character expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Character actual, String newErrorMessage, char expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Character actual, String newErrorMessage, Character expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(short actual, String newErrorMessage, short expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(short actual, String newErrorMessage, Short expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Short actual, String newErrorMessage, short expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Short actual, String newErrorMessage, Short expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(int actual, String newErrorMessage, int expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(int actual, String newErrorMessage, Integer expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Integer actual, String newErrorMessage, int expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Integer actual, String newErrorMessage, Integer expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(long actual, String newErrorMessage, long expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(long actual, String newErrorMessage, Long expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Long actual, String newErrorMessage, long expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Long actual, String newErrorMessage, Long expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(float actual, String newErrorMessage, float expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(float actual, String newErrorMessage, Float expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Float actual, String newErrorMessage, float expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Float actual, String newErrorMessage, Float expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(double actual, String newErrorMessage, double expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(double actual, String newErrorMessage, Double expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Double actual, String newErrorMessage, double expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Double actual, String newErrorMessage, Double expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(String actual, String newErrorMessage, String expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, String newErrorMessage, Map<?, ?> expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object expected) {
      assertThat(actual).withFailMessage(newErrorMessage).isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractFloatAssert#isCloseTo(float, Offset)} over non-AssertJ alternatives. */
  static final class AssertThatIsCloseToOffsetFloat {
    @BeforeTemplate
    void before(float actual, float expected, float value) {
      assertEquals(actual, expected, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float value) {
      assertThat(actual).isCloseTo(expected, offset(value));
    }
  }

  /** Prefer {@link AbstractFloatAssert#isCloseTo(float, Offset)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsCloseToOffsetFloat {
    @BeforeTemplate
    void before(float actual, String newErrorMessage, float expected, float value) {
      assertEquals(actual, expected, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, String newErrorMessage, float expected, float value) {
      assertThat(actual).withFailMessage(newErrorMessage).isCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatIsCloseToOffsetDouble {
    @BeforeTemplate
    void before(double actual, double expected, double value) {
      assertEquals(actual, expected, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double value) {
      assertThat(actual).isCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageIsCloseToOffsetDouble {
    @BeforeTemplate
    void before(double actual, String newErrorMessage, double expected, double value) {
      assertEquals(actual, expected, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, String newErrorMessage, double expected, double value) {
      assertThat(actual).withFailMessage(newErrorMessage).isCloseTo(expected, offset(value));
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactly {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte[] actual, byte[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char[] actual, char[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short[] actual, short[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int[] actual, int[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long[] actual, long[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float[] actual, float[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double[] actual, double[] expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Object[] actual, Object[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatWithFailMessageContainsExactly {
    @BeforeTemplate
    void before(boolean[] actual, String newErrorMessage, boolean[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(byte[] actual, String newErrorMessage, byte[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(char[] actual, String newErrorMessage, char[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(short[] actual, String newErrorMessage, short[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(int[] actual, String newErrorMessage, int[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(long[] actual, String newErrorMessage, long[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(float[] actual, String newErrorMessage, float[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(double[] actual, String newErrorMessage, double[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @BeforeTemplate
    void before(Object[] actual, String newErrorMessage, Object[] expected) {
      assertEquals(actual, expected, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, String newErrorMessage, Object[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatContainsExactlyOffsetFloat {
    @BeforeTemplate
    void before(float[] actual, float[] values, float value) {
      assertEquals(actual, values, value);
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
  static final class AssertThatWithFailMessageContainsExactlyOffsetFloat {
    @BeforeTemplate
    void before(float[] actual, String newErrorMessage, float[] values, float value) {
      assertEquals(actual, values, value, newErrorMessage);
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
  static final class AssertThatContainsExactlyOffsetDouble {
    @BeforeTemplate
    void before(double[] actual, double[] values, double value) {
      assertEquals(actual, values, value);
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
  static final class AssertThatWithFailMessageContainsExactlyOffsetDouble {
    @BeforeTemplate
    void before(double[] actual, String newErrorMessage, double[] values, double value) {
      assertEquals(actual, values, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, String newErrorMessage, double[] values, double value) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactly(values, offset(value));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactlyInAnyOrder(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatContainsExactlyInAnyOrder {
    @BeforeTemplate
    void before(Object[] actual, Object[] expected) {
      assertEqualsNoOrder(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).containsExactlyInAnyOrder(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactlyInAnyOrder(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageContainsExactlyInAnyOrder {
    @BeforeTemplate
    void before(Object[] actual, String newErrorMessage, Object[] expected) {
      assertEqualsNoOrder(actual, expected, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, String newErrorMessage, Object[] expected) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactlyInAnyOrder(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).toIterable().containsExactlyElementsOf(...)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final class AssertThatToIterableContainsExactlyElementsOfImmutableListCopyOf<
      S, T extends S> {
    @BeforeTemplate
    void before(Iterator<S> actual, Iterator<T> elements) {
      assertEquals(actual, elements);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterator<S> actual, Iterator<T> elements) {
      // XXX: This is not `null`-safe.
      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
      assertThat(actual).toIterable().containsExactlyElementsOf(ImmutableList.copyOf(elements));
    }
  }

  /**
   * Prefer {@code assertThat(...).toIterable().containsExactlyElementsOf(...)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final
  class AssertThatToIterableWithFailMessageContainsExactlyElementsOfImmutableListCopyOf<
      S, T extends S> {
    @BeforeTemplate
    void before(Iterator<S> actual, String newErrorMessage, Iterator<T> elements) {
      assertEquals(actual, elements, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterator<S> actual, String newErrorMessage, Iterator<T> elements) {
      // XXX: This is not `null`-safe.
      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
      assertThat(actual)
          .toIterable()
          .withFailMessage(newErrorMessage)
          .containsExactlyElementsOf(ImmutableList.copyOf(elements));
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#containsExactlyElementsOf(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: This rule fails for `java.nio.file.Path` as it is `Iterable`, but AssertJ's
  // `assertThat(Path)` does not support `.containsExactlyElementsOf`.
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterable<?>` and `Collection<?>` arguments. As
  // such some expressions will not be rewritten.
  static final class AssertThatContainsExactlyElementsOf<S, T extends S> {
    @BeforeTemplate
    void before(Iterable<S> actual, Iterable<T> iterable) {
      assertEquals(actual, iterable);
    }

    @BeforeTemplate
    void before(Collection<S> actual, Collection<T> iterable) {
      assertEquals(actual, iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterable<S> actual, Iterable<T> iterable) {
      assertThat(actual).containsExactlyElementsOf(iterable);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#containsExactlyElementsOf(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: This rule fails for `java.nio.file.Path` as it is `Iterable`, but AssertJ's
  // `assertThat(Path)` does not support `.containsExactlyElementsOf`.
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterable<?>` and `Collection<?>` arguments. As
  // such some expressions will not be rewritten.
  static final class AssertThatWithFailMessageContainsExactlyElementsOf<S, T extends S> {
    @BeforeTemplate
    void before(Iterable<S> actual, String newErrorMessage, Iterable<T> iterable) {
      assertEquals(actual, iterable, newErrorMessage);
    }

    @BeforeTemplate
    void before(Collection<S> actual, String newErrorMessage, Collection<T> iterable) {
      assertEquals(actual, iterable, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterable<S> actual, String newErrorMessage, Iterable<T> iterable) {
      assertThat(actual).withFailMessage(newErrorMessage).containsExactlyElementsOf(iterable);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#hasSameElementsAs(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertThatHasSameElementsAs<S, T extends S> {
    @BeforeTemplate
    void before(Set<S> actual, Set<T> iterable) {
      assertEquals(actual, iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Set<S> actual, Set<T> iterable) {
      assertThat(actual).hasSameElementsAs(iterable);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#hasSameElementsAs(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertThatWithFailMessageHasSameElementsAs<S, T extends S> {
    @BeforeTemplate
    void before(Set<S> actual, String newErrorMessage, Set<T> iterable) {
      assertEquals(actual, iterable, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Set<S> actual, String newErrorMessage, Set<T> iterable) {
      assertThat(actual).withFailMessage(newErrorMessage).hasSameElementsAs(iterable);
    }
  }

  /** Prefer {@link AbstractAssert#isNotEqualTo(Object)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsNotEqualTo {
    @BeforeTemplate
    void before(boolean actual, boolean other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(byte actual, byte other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(char actual, char other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(short actual, short other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(int actual, int other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(long actual, long other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(float actual, float other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(double actual, double other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(Object actual, Object other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(String actual, String other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(Set<?> actual, Set<?> other) {
      assertNotEquals(actual, other);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, Map<?, ?> other) {
      assertNotEquals(actual, other);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object other) {
      assertThat(actual).isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractAssert#isNotEqualTo(Object)} over non-AssertJ alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatWithFailMessageIsNotEqualTo {
    @BeforeTemplate
    void before(boolean actual, String newErrorMessage, boolean other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(byte actual, String newErrorMessage, byte other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(char actual, String newErrorMessage, char other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(short actual, String newErrorMessage, short other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(int actual, String newErrorMessage, int other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(long actual, String newErrorMessage, long other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(float actual, String newErrorMessage, float other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(double actual, String newErrorMessage, double other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(Object actual, String newErrorMessage, Object other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(String actual, String newErrorMessage, String other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(Set<?> actual, String newErrorMessage, Set<?> other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, String newErrorMessage, Map<?, ?> other) {
      assertNotEquals(actual, other, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String newErrorMessage, Object other) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotEqualTo(other);
    }
  }

  /**
   * Prefer {@link AbstractFloatAssert#isNotCloseTo(float, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatIsNotCloseToOffsetFloat {
    @BeforeTemplate
    void before(float actual, float expected, float value) {
      assertNotEquals(actual, expected, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float value) {
      assertThat(actual).isNotCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link AbstractFloatAssert#isNotCloseTo(float, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageIsNotCloseToOffsetFloat {
    @BeforeTemplate
    void before(float actual, String newErrorMessage, float expected, float value) {
      assertNotEquals(actual, expected, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, String newErrorMessage, float expected, float value) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isNotCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatIsNotCloseToOffsetDouble {
    @BeforeTemplate
    void before(double actual, double expected, double value) {
      assertNotEquals(actual, expected, value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double value) {
      assertThat(actual).isNotCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isNotCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatWithFailMessageIsNotCloseToOffsetDouble {
    @BeforeTemplate
    void before(double actual, String newErrorMessage, double expected, double value) {
      assertNotEquals(actual, expected, value, newErrorMessage);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, String newErrorMessage, double expected, double value) {
      assertThat(actual).withFailMessage(newErrorMessage).isNotCloseTo(expected, offset(value));
    }
  }

  /**
   * Prefer {@link Assertions#assertThatThrownBy(ThrowingCallable)} over non-AssertJ alternatives.
   */
  static final class AssertThatThrownBy {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) ThrowingRunnable shouldRaiseThrowable) {
      assertThrows(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable) {
      assertThatThrownBy(shouldRaiseThrowable);
    }
  }

  /** Prefer {@code assertThatThrownBy(...).isInstanceOf(...)} over non-AssertJ alternatives. */
  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(
        @Matches(IsLambdaExpressionOrMethodReference.class) ThrowingRunnable shouldRaiseThrowable,
        Class<T> type) {
      assertThrows(type, shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(type);
    }
  }
}
