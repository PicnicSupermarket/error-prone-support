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
import tech.picnic.errorprone.refaster.annotation.TypeMigration;

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
    void before(String message) {
      Assert.fail(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String message) {
      fail(message);
    }
  }

  /** Prefer {@link Assertions#fail(String, Throwable)} over non-AssertJ alternatives. */
  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithStringAndThrowable {
    @BeforeTemplate
    void before(String message, Throwable throwable) {
      Assert.fail(message, throwable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String message, Throwable throwable) {
      fail(message, throwable);
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
    void before(boolean actual, String message) {
      assertTrue(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String message) {
      assertThat(actual).withFailMessage(message).isTrue();
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
    void before(boolean actual, String message) {
      assertFalse(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean actual, String message) {
      assertThat(actual).withFailMessage(message).isFalse();
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
    void before(Object actual, String message) {
      assertNull(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message) {
      assertThat(actual).withFailMessage(message).isNull();
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
    void before(Object actual, String message) {
      assertNotNull(actual, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message) {
      assertThat(actual).withFailMessage(message).isNotNull();
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
    void before(Object actual, String message, Object expected) {
      assertSame(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertNotSame(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isNotSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotSameAs(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotSameAs {
    @BeforeTemplate
    void before(Object actual, String message, Object expected) {
      assertNotSame(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isNotSameAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isEqualTo(Object)} over non-AssertJ alternatives. */
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
  @SuppressWarnings("java:S1448" /* Each variant requires a separate `@BeforeTemplate` method. */)
  static final class AssertThatWithFailMessageIsEqualTo {
    @BeforeTemplate
    void before(boolean actual, String message, boolean expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(boolean actual, String message, Boolean expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Boolean actual, String message, boolean expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Boolean actual, String message, Boolean expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte actual, String message, byte expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte actual, String message, Byte expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Byte actual, String message, byte expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Byte actual, String message, Byte expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char actual, String message, char expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char actual, String message, Character expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Character actual, String message, char expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Character actual, String message, Character expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short actual, String message, short expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short actual, String message, Short expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Short actual, String message, short expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Short actual, String message, Short expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int actual, String message, int expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int actual, String message, Integer expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Integer actual, String message, int expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Integer actual, String message, Integer expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long actual, String message, long expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long actual, String message, Long expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Long actual, String message, long expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Long actual, String message, Long expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float actual, String message, float expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float actual, String message, Float expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Float actual, String message, float expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Float actual, String message, Float expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double actual, String message, double expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double actual, String message, Double expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Double actual, String message, double expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Double actual, String message, Double expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object actual, String message, Object expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(String actual, String message, String expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, String message, Map<?, ?> expected) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractFloatAssert#isCloseTo(float, Offset)} over non-AssertJ alternatives. */
  static final class AssertThatFloatIsCloseToOffset {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta) {
      assertThat(actual).isCloseTo(expected, offset(delta));
    }
  }

  /** Prefer {@link AbstractFloatAssert#isCloseTo(float, Offset)} over non-AssertJ alternatives. */
  static final class AssertThatFloatWithFailMessageIsCloseToOffset {
    @BeforeTemplate
    void before(float actual, String message, float expected, float delta) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, String message, float expected, float delta) {
      assertThat(actual).withFailMessage(message).isCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleIsCloseToOffset {
    @BeforeTemplate
    void before(double actual, double expected, double delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta) {
      assertThat(actual).isCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleWithFailMessageIsCloseToOffset {
    @BeforeTemplate
    void before(double actual, String message, double expected, double delta) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, String message, double expected, double delta) {
      assertThat(actual).withFailMessage(message).isCloseTo(expected, offset(delta));
    }
  }

  /** Prefer {@code assertThat(...).containsExactly(...)} over non-AssertJ alternatives. */
  static final class AssertThatArrayContainsExactly {
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
  static final class AssertThatArrayWithFailMessageContainsExactly {
    @BeforeTemplate
    void before(boolean[] actual, String message, boolean[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte[] actual, String message, byte[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char[] actual, String message, char[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short[] actual, String message, short[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int[] actual, String message, int[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long[] actual, String message, long[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float[] actual, String message, float[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double[] actual, String message, double[] expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object[] actual, String message, Object[] expected) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, String message, Object[] expected) {
      assertThat(actual).withFailMessage(message).containsExactly(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatFloatArrayContainsExactlyOffset {
    @BeforeTemplate
    void before(float[] actual, float[] expected, float delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, float[] expected, float delta) {
      assertThat(actual).containsExactly(expected, offset(delta));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatFloatArrayWithFailMessageContainsExactlyOffset {
    @BeforeTemplate
    void before(float[] actual, String message, float[] expected, float delta) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float[] actual, String message, float[] expected, float delta) {
      assertThat(actual).withFailMessage(message).containsExactly(expected, offset(delta));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleArrayContainsExactlyOffset {
    @BeforeTemplate
    void before(double[] actual, double[] expected, double delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, double[] expected, double delta) {
      assertThat(actual).containsExactly(expected, offset(delta));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactly(..., offset(...))} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleArrayWithFailMessageContainsExactlyOffset {
    @BeforeTemplate
    void before(double[] actual, String message, double[] expected, double delta) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double[] actual, String message, double[] expected, double delta) {
      assertThat(actual).withFailMessage(message).containsExactly(expected, offset(delta));
    }
  }

  /**
   * Prefer {@code assertThat(...).containsExactlyInAnyOrder(...)} over non-AssertJ alternatives.
   */
  static final class AssertThatArrayContainsExactlyInAnyOrder {
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
  static final class AssertThatArrayWithFailMessageContainsExactlyInAnyOrder {
    @BeforeTemplate
    void before(Object[] actual, String message, Object[] expected) {
      assertEqualsNoOrder(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, String message, Object[] expected) {
      assertThat(actual).withFailMessage(message).containsExactlyInAnyOrder(expected);
    }
  }

  /**
   * Prefer {@code assertThat(...).toIterable().containsExactlyElementsOf(...)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final class AssertThatIteratorToIterableContainsExactlyElementsOfImmutableListCopyOf<
      S, T extends S> {
    @BeforeTemplate
    void before(Iterator<S> actual, Iterator<T> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterator<S> actual, Iterator<T> expected) {
      // XXX: This is not `null`-safe.
      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
      assertThat(actual).toIterable().containsExactlyElementsOf(ImmutableList.copyOf(expected));
    }
  }

  /**
   * Prefer {@code assertThat(...).toIterable().containsExactlyElementsOf(...)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final
  class AssertThatIteratorToIterableWithFailMessageContainsExactlyElementsOfImmutableListCopyOf<
      S, T extends S> {
    @BeforeTemplate
    void before(Iterator<S> actual, String message, Iterator<T> expected) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterator<S> actual, String message, Iterator<T> expected) {
      // XXX: This is not `null`-safe.
      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
      assertThat(actual)
          .toIterable()
          .withFailMessage(message)
          .containsExactlyElementsOf(ImmutableList.copyOf(expected));
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
  static final class AssertThatIterableContainsExactlyElementsOf<S, T extends S> {
    @BeforeTemplate
    void before(Iterable<S> actual, Iterable<T> expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Collection<S> actual, Collection<T> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterable<S> actual, Iterable<T> expected) {
      assertThat(actual).containsExactlyElementsOf(expected);
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
  static final class AssertThatIterableWithFailMessageContainsExactlyElementsOf<S, T extends S> {
    @BeforeTemplate
    void before(Iterable<S> actual, String message, Iterable<T> expected) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Collection<S> actual, String message, Collection<T> expected) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterable<S> actual, String message, Iterable<T> expected) {
      assertThat(actual).withFailMessage(message).containsExactlyElementsOf(expected);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#hasSameElementsAs(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertThatSetHasSameElementsAs<S, T extends S> {
    @BeforeTemplate
    void before(Set<S> actual, Set<T> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Set<S> actual, Set<T> expected) {
      assertThat(actual).hasSameElementsAs(expected);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#hasSameElementsAs(Iterable)} over non-AssertJ
   * alternatives.
   */
  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertThatSetWithFailMessageHasSameElementsAs<S, T extends S> {
    @BeforeTemplate
    void before(Set<S> actual, String message, Set<T> expected) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Set<S> actual, String message, Set<T> expected) {
      assertThat(actual).withFailMessage(message).hasSameElementsAs(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotEqualTo(Object)} over non-AssertJ alternatives. */
  static final class AssertThatIsNotEqualTo {
    @BeforeTemplate
    void before(boolean actual, boolean expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte actual, byte expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char actual, char expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short actual, short expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int actual, int expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long actual, long expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float actual, float expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double actual, double expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(String actual, String expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Set<?> actual, Set<?> expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, Map<?, ?> expected) {
      assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isNotEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractAssert#isNotEqualTo(Object)} over non-AssertJ alternatives. */
  static final class AssertThatWithFailMessageIsNotEqualTo {
    @BeforeTemplate
    void before(boolean actual, String message, boolean expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte actual, String message, byte expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char actual, String message, char expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short actual, String message, short expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int actual, String message, int expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long actual, String message, long expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float actual, String message, float expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double actual, String message, double expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object actual, String message, Object expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(String actual, String message, String expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Set<?> actual, String message, Set<?> expected) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, String message, Map<?, ?> expected) {
      assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, String message, Object expected) {
      assertThat(actual).withFailMessage(message).isNotEqualTo(expected);
    }
  }

  /**
   * Prefer {@link AbstractFloatAssert#isNotCloseTo(float, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatFloatIsNotCloseToOffset {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertNotEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta) {
      assertThat(actual).isNotCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link AbstractFloatAssert#isNotCloseTo(float, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatFloatWithFailMessageIsNotCloseToOffset {
    @BeforeTemplate
    void before(float actual, String message, float expected, float delta) {
      assertNotEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(float actual, String message, float expected, float delta) {
      assertThat(actual).withFailMessage(message).isNotCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isNotCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleIsNotCloseToOffset {
    @BeforeTemplate
    void before(double actual, double expected, double delta) {
      assertNotEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta) {
      assertThat(actual).isNotCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link AbstractDoubleAssert#isNotCloseTo(double, Offset)} over non-AssertJ alternatives.
   */
  static final class AssertThatDoubleWithFailMessageIsNotCloseToOffset {
    @BeforeTemplate
    void before(double actual, String message, double expected, double delta) {
      assertNotEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(double actual, String message, double expected, double delta) {
      assertThat(actual).withFailMessage(message).isNotCloseTo(expected, offset(delta));
    }
  }

  /**
   * Prefer {@link Assertions#assertThatThrownBy(ThrowingCallable)} over non-AssertJ alternatives.
   */
  static final class AssertThatThrownBy {
    @BeforeTemplate
    void before(ThrowingRunnable runnable) {
      assertThrows(runnable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable runnable) {
      assertThatThrownBy(runnable);
    }
  }

  /** Prefer {@code assertThatThrownBy(...).isInstanceOf(...)} over non-AssertJ alternatives. */
  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(ThrowingRunnable runnable, Class<T> clazz) {
      assertThrows(clazz, runnable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable runnable, Class<T> clazz) {
      assertThatThrownBy(runnable).isInstanceOf(clazz);
    }
  }
}
