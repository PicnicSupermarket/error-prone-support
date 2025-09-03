package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.Offset.offset;
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
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.TypeMigration;

/**
 * Refaster rules that replace TestNG assertions with equivalent AssertJ assertions.
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
 */
// XXX: As-is these rules do not result in a complete migration:
// - Expressions containing comments are skipped due to a limitation of Refaster.
// - Assertions inside lambda expressions are also skipped. Unclear why.
// XXX: Many of the test expressions for these rules use the same expression for `expected` and
// `actual`, which makes the validation weaker than necessary; fix this. (And investigate whether we
// can introduce validation for this.)
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

  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithMessage {
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

  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithMessageAndThrowable {
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

  static final class AssertSame {
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

  static final class AssertSameWithMessage {
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

  static final class AssertNotSame {
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

  static final class AssertNotSameWithMessage {
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

  @SuppressWarnings("java:S1448" /* Each variant requires a separate `@BeforeTemplate` method. */)
  static final class AssertEqual {
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

  @SuppressWarnings("java:S1448" /* Each variant requires a separate `@BeforeTemplate` method. */)
  static final class AssertEqualWithMessage {
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

  static final class AssertEqualFloatsWithDelta {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Float actual, float expected, float delta) {
      assertThat(actual).isCloseTo(expected, offset(delta));
    }
  }

  static final class AssertEqualFloatsWithDeltaWithMessage {
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

  static final class AssertEqualDoublesWithDelta {
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

  static final class AssertEqualDoublesWithDeltaWithMessage {
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

  static final class AssertEqualArrayIterationOrder {
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

  static final class AssertEqualArrayIterationOrderWithMessage {
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

  static final class AssertEqualFloatArraysWithDelta {
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

  static final class AssertEqualFloatArraysWithDeltaWithMessage {
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

  static final class AssertEqualDoubleArraysWithDelta {
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

  static final class AssertEqualDoubleArraysWithDeltaWithMessage {
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

  static final class AssertEqualArraysIrrespectiveOfOrder {
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

  static final class AssertEqualArraysIrrespectiveOfOrderWithMessage {
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

  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final class AssertEqualIteratorIterationOrder<S, T extends S> {
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

  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterator<?>` arguments. As such some
  // expressions will not be rewritten.
  static final class AssertEqualIteratorIterationOrderWithMessage<S, T extends S> {
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

  // XXX This rule fails for `java.nio.file.Path` as it is `Iterable`, but AssertJ's
  // `assertThat(Path)` does not support `.containsExactlyElementsOf`.
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterable<?>` and `Collection<?>` arguments. As
  // such some expressions will not be rewritten.
  static final class AssertEqualIterableIterationOrder<S, T extends S> {
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

  // XXX This rule fails for `java.nio.file.Path` as it is `Iterable`, but AssertJ's
  // `assertThat(Path)` does not support `.containsExactlyElementsOf`.
  // XXX: TestNG's `assertEquals` accepts arbitrary `Iterable<?>` and `Collection<?>` arguments. As
  // such some expressions will not be rewritten.
  static final class AssertEqualIterableIterationOrderWithMessage<S, T extends S> {
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

  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertEqualSets<S, T extends S> {
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

  // XXX: TestNG's `assertEquals` accepts arbitrary `Set<?>` arguments. As such some expressions
  // will not be rewritten.
  static final class AssertEqualSetsWithMessage<S, T extends S> {
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

  static final class AssertUnequal {
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

  static final class AssertUnequalWithMessage {
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

  static final class AssertUnequalFloatsWithDelta {
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

  static final class AssertUnequalFloatsWithDeltaWithMessage {
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

  static final class AssertUnequalDoublesWithDelta {
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

  static final class AssertUnequalDoublesWithDeltaWithMessage {
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

  static final class AssertThrows {
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

  static final class AssertThrowsWithType<T extends Throwable> {
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
