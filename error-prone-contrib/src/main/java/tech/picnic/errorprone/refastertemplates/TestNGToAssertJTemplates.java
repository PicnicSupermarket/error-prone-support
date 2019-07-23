package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Refaster templates which replace TestNG assertions with equivalent AssertJ assertions.
 *
 * <p>Some of the classes below have TestNG {@code @BeforeTemplate}s that reference wildcard type
 * bounds ({@code <?>}), while the associated AssertJ {@code @AfterTemplate}s reference stricter
 * type bounds. This introduces the risk of producing invalid code. We do this anyway, because
 * TestNG's wildcard types can cause javac to infer less specific types than AssertJ requires, while
 * the appropriate (more specific) types _will_ be inferred properly when plugged into AssertJ's
 * API.
 *
 * <p>The following is an example of a TestNG statement which would not be rewritten if it weren't
 * for the wildcard matching (note that the type parameters of the map on the right-hand side will
 * be inferred to be {@code <Object, Object>} rather than {@code <String, Object>}.)
 *
 * <pre>{@code
 * List<Map<String, Object>> myMaps = new ArrayList<>();
 * assertEquals(myMaps, ImmutableList.of(ImmutableMap.of()));
 * }</pre>
 */
final class TestNGToAssertJTemplates {
  private TestNGToAssertJTemplates() {}

  static final class Fail {
    @BeforeTemplate
    void before() {
      org.testng.Assert.fail();
    }

    @AfterTemplate
    void after() {
      throw new AssertionError();
    }
  }

  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithMessage {
    @BeforeTemplate
    void before(String message) {
      org.testng.Assert.fail(message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String message) {
      fail(message);
    }
  }

  // XXX: This may cause the TestNG import not to be cleaned up, yielding a compilation failure.
  static final class FailWithMessageAndThrowable {
    @BeforeTemplate
    void before(String message, Throwable throwable) {
      org.testng.Assert.fail(message, throwable);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isSameAs(expected);
    }
  }

  static final class AssertSameWithMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertSame(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isSameAs(expected);
    }
  }

  static final class AssertNotSame {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertNotSame(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isNotSameAs(expected);
    }
  }

  static final class AssertNotSameWithMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertNotSame(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isNotSameAs(expected);
    }
  }

  static final class AssertEqual {
    @BeforeTemplate
    void before(boolean actual, boolean expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte actual, byte expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char actual, char expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short actual, short expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int actual, int expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long actual, long expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float actual, float expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double actual, double expected) {
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AssertEqualWithMessage {
    @BeforeTemplate
    void before(boolean actual, boolean expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte actual, byte expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char actual, char expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short actual, short expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int actual, int expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long actual, long expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float actual, float expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double actual, double expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(String actual, String expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, Map<?, ?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class AssertEqualFloatsWithDelta {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta) {
      assertThat(actual).isCloseTo(expected, offset(delta));
    }
  }

  static final class AssertEqualFloatsWithDeltaWithMessage {
    @BeforeTemplate
    void before(float actual, float expected, float delta, String message) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta, String message) {
      assertThat(actual).withFailMessage(message).isCloseTo(expected, offset(delta));
    }
  }

  static final class AssertEqualDoublesWithDelta {
    @BeforeTemplate
    void before(double actual, double expected, double delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta) {
      assertThat(actual).isCloseTo(expected, offset(delta));
    }
  }

  static final class AssertEqualDoublesWithDeltaWithMessage {
    @BeforeTemplate
    void before(double actual, double expected, double delta, String message) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta, String message) {
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
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).containsExactly(expected);
    }
  }

  static final class AssertEqualArrayIterationOrderWithMessage {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte[] actual, byte[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char[] actual, char[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short[] actual, short[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int[] actual, int[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long[] actual, long[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float[] actual, float[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double[] actual, double[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object[] actual, Object[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected, String message) {
      assertThat(actual).withFailMessage(message).containsExactly(expected);
    }
  }

  static final class AssertEqualArraysIrrespectiveOfOrder {
    @BeforeTemplate
    void before(Object[] actual, Object[] expected) {
      assertEqualsNoOrder(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).containsExactlyInAnyOrder(expected);
    }
  }

  static final class AssertEqualArraysIrrespectiveOfOrderWithMessage {
    @BeforeTemplate
    void before(Object[] actual, Object[] expected, String message) {
      assertEqualsNoOrder(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected, String message) {
      assertThat(actual).withFailMessage(message).containsExactlyInAnyOrder(expected);
    }
  }

  static final class AssertEqualIteratorIterationOrder {
    @BeforeTemplate
    void before(Iterator<?> actual, Iterator<?> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Iterator<S> actual, Iterator<T> expected) {
      // XXX: This is not `null`-safe.
      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
      assertThat(actual).toIterable().containsExactlyElementsOf(ImmutableList.copyOf(expected));
    }
  }

  static final class AssertEqualIteratorIterationOrderWithMessage {
    @BeforeTemplate
    void before(Iterator<?> actual, Iterator<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Iterator<S> actual, Iterator<T> expected, String message) {
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
  static final class AssertEqualIterableIterationOrder {
    @BeforeTemplate
    void before(Iterable<?> actual, Iterable<?> expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Collection<?> actual, Collection<?> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Iterable<S> actual, Iterable<T> expected) {
      assertThat(actual).containsExactlyElementsOf(expected);
    }
  }

  static final class AssertEqualIterableIterationOrderWithMessage {
    @BeforeTemplate
    void before(Iterable<?> actual, Iterable<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Collection<?> actual, Collection<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Iterable<S> actual, Iterable<T> expected, String message) {
      assertThat(actual).withFailMessage(message).containsExactlyElementsOf(expected);
    }
  }

  static final class AssertEqualSets {
    @BeforeTemplate
    void before(Set<?> actual, Set<?> expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Set<S> actual, Set<T> expected) {
      assertThat(actual).hasSameElementsAs(expected);
    }
  }

  static final class AssertEqualSetsWithMessage {
    @BeforeTemplate
    void before(Set<?> actual, Set<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    <S, T extends S> void after(Set<S> actual, Set<T> expected, String message) {
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
    void before(Map<?, ?> actual, Map<?, ?> expected) {
      assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AssertUnequalWithMessage {
    @BeforeTemplate
    void before(boolean actual, boolean expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte actual, byte expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char actual, char expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short actual, short expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int actual, int expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long actual, long expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float actual, float expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double actual, double expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(String actual, String expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Map<?, ?> actual, Map<?, ?> expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isNotEqualTo(expected);
    }
  }

  static final class AssertUnequalFloatsWithDelta {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertNotEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta) {
      assertThat(actual).isNotCloseTo(expected, offset(delta));
    }
  }

  static final class AssertUnequalFloatsWithDeltaWithMessage {
    @BeforeTemplate
    void before(float actual, float expected, float delta, String message) {
      assertNotEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta, String message) {
      assertThat(actual).withFailMessage(message).isNotCloseTo(expected, offset(delta));
    }
  }

  static final class AssertUnequalDoublesWithDelta {
    @BeforeTemplate
    void before(double actual, double expected, double delta) {
      assertNotEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta) {
      assertThat(actual).isNotCloseTo(expected, offset(delta));
    }
  }

  static final class AssertUnequalDoublesWithDeltaWithMessage {
    @BeforeTemplate
    void before(double actual, double expected, double delta, String message) {
      assertNotEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta, String message) {
      assertThat(actual).withFailMessage(message).isNotCloseTo(expected, offset(delta));
    }
  }

  static final class AssertUnequalArrayIterationOrder {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(byte[] actual, byte[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(char[] actual, char[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(short[] actual, short[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(int[] actual, int[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(long[] actual, long[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(float[] actual, float[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(double[] actual, double[] expected) {
      assertNotEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Object[] actual, Object[] expected) {
      assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected) {
      assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class AssertUnequalArrayIterationOrderWithMessage {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(byte[] actual, byte[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(char[] actual, char[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(short[] actual, short[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(int[] actual, int[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(long[] actual, long[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(float[] actual, float[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(double[] actual, double[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Object[] actual, Object[] expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object[] actual, Object[] expected, String message) {
      assertThat(actual).withFailMessage(message).isNotEqualTo(expected);
    }
  }

  // XXX: The assertions below are WRONG! Figure out how we _should_ implement them.
  //  static final class AssertUnequalIteratorIterationOrder {
  //    @BeforeTemplate
  //    void before(Iterator<?> actual, Iterator<?> expected) {
  //      assertNotEquals(actual, expected);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Iterator<S> actual, Iterator<T> expected) {
  //      // XXX: This is not `null`-safe.
  //      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
  //      assertThat(actual).toIterable().containsExactlyElementsOf(ImmutableList.copyOf(expected));
  //    }
  //  }
  //
  //  static final class AssertUnequalIteratorIterationOrderWithMessage {
  //    @BeforeTemplate
  //    void before(Iterator<?> actual, Iterator<?> expected, String message) {
  //      assertNotEquals(actual, expected, message);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Iterator<S> actual, Iterator<T> expected, String message) {
  //      // XXX: This is not `null`-safe.
  //      // XXX: The `ImmutableList.copyOf` should actually *not* be imported statically.
  //      assertThat(actual)
  //          .toIterable()
  //          .withFailMessage(message)
  //          .containsExactlyElementsOf(ImmutableList.copyOf(expected));
  //    }
  //  }
  //
  //  // XXX This rule fails for `java.nio.file.Path` as it is `Iterable`, but AssertJ's
  //  // `assertThat(Path)` does not support `.containsExactlyElementsOf`.
  //  static final class AssertUnequalIterableIterationOrder {
  //    @BeforeTemplate
  //    void before(Iterable<?> actual, Iterable<?> expected) {
  //      assertNotEquals(actual, expected);
  //    }
  //
  //    @BeforeTemplate
  //    void before(Collection<?> actual, Collection<?> expected) {
  //      assertNotEquals(actual, expected);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Iterable<S> actual, Iterable<T> expected) {
  //      assertThat(actual).containsExactlyElementsOf(expected);
  //    }
  //  }
  //
  //  static final class AssertUnequalIterableIterationOrderWithMessage {
  //    @BeforeTemplate
  //    void before(Iterable<?> actual, Iterable<?> expected, String message) {
  //      assertNotEquals(actual, expected, message);
  //    }
  //
  //    @BeforeTemplate
  //    void before(Collection<?> actual, Collection<?> expected, String message) {
  //      assertNotEquals(actual, expected, message);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Iterable<S> actual, Iterable<T> expected, String message) {
  //      assertThat(actual).withFailMessage(message).containsExactlyElementsOf(expected);
  //    }
  //  }
  //
  //  static final class AssertUnequalSets {
  //    @BeforeTemplate
  //    void before(Set<?> actual, Set<?> expected) {
  //      assertNotEquals(actual, expected);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Set<S> actual, Set<T> expected) {
  //      assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  //    }
  //  }
  //
  //  static final class AssertUnequalSetsWithMessage {
  //    @BeforeTemplate
  //    void before(Set<?> actual, Set<?> expected, String message) {
  //      assertNotEquals(actual, expected, message);
  //    }
  //
  //    @AfterTemplate
  //    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //    <S, T extends S> void after(Set<S> actual, Set<T> expected, String message) {
  //      assertThat(actual).withFailMessage(message).containsExactlyInAnyOrderElementsOf(expected);
  //    }
  //  }

  // XXX: Skipped methods (these test array equality of elements/values if applicable):
  // assertEqualsDeep Set Set
  // assertEqualsDeep Set Set msg
  // assertEqualsDeep Map Map
  // assertEqualsDeep Map Map msg

  // Also still TBD:
  // assertThrows
  // expectThrows

  // XXX: Not matched:
  // - Stuff with comments (Check whether we should fork Refaster...)
  // - Assertions inside lambda expressions (why?)
}
