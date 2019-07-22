package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.Offset.offset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.api.OptionalDoubleAssert;
import org.assertj.core.api.OptionalIntAssert;
import org.assertj.core.api.OptionalLongAssert;

// XXX: Add class documentation. We should have separate classes for migration *to* AssertJ and
// applying best practices when *using* AssertJ.
// XXX: Also fix the subclass names.
// XXX: In some places we use generics, in others not. Let's be consistent.
// XXX: drop `offset(0)` and `offset(0.0)`.
// XXX: Replace `.isEqualTo(collection)` with the appropriate more specific alternative.
// XXX: Drop (now) unnecessary intermediate collection constructions. First on the RHS.
// XXX: Then on the LHS, though some copy operations here may cause a deduplication. The resultant
// test failures should be fixed on the RHS.
final class AssertJTemplates {
  private AssertJTemplates() {}

  /// TestNG

  static final class XXXFail {
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
  static final class XXXFailWithMessage {
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
  static final class XXXFailWithMessageAndThrowable {
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

  static final class XXXAssertTrue {
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

  static final class XXXAssertTrueWithMessage {
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

  static final class XXXAssertFalse {
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

  static final class XXXAssertFalseWithMessage {
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

  static final class XXXAssertNull {
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

  static final class XXXAssertNullWithMessage {
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

  static final class XXXAssertNotNull {
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

  static final class XXXAssertNotNullWithMessage {
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

  static final class XXXAssertSame {
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

  static final class XXXAssertSameWithMessage {
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

  static final class XXXAssertNotSame {
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

  static final class XXXAssertNotSameWithMessage {
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

  static final class XXXAssertEqualObjects {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualObjectsWithMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBooleans {
    @BeforeTemplate
    void before(boolean actual, boolean expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(boolean actual, boolean expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBooleansWithMessage {
    @BeforeTemplate
    void before(boolean actual, boolean expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(boolean actual, boolean expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBooleanArrays {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(boolean[] actual, boolean[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBooleanArraysWithMessage {
    @BeforeTemplate
    void before(boolean[] actual, boolean[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(boolean[] actual, boolean[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBytes {
    @BeforeTemplate
    void before(byte actual, byte expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(byte actual, byte expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualBytesWithMessage {
    @BeforeTemplate
    void before(byte actual, byte expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(byte actual, byte expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualByteArrays {
    @BeforeTemplate
    void before(byte[] actual, byte[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(byte[] actual, byte[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualByteArraysWithMessage {
    @BeforeTemplate
    void before(byte[] actual, byte[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(byte[] actual, byte[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualChars {
    @BeforeTemplate
    void before(char actual, char expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(char actual, char expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualCharsWithMessage {
    @BeforeTemplate
    void before(char actual, char expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(char actual, char expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualCharArrays {
    @BeforeTemplate
    void before(char[] actual, char[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(char[] actual, char[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualCharArraysWithMessage {
    @BeforeTemplate
    void before(char[] actual, char[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(char[] actual, char[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualShorts {
    @BeforeTemplate
    void before(short actual, short expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(short actual, short expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualShortsWithMessage {
    @BeforeTemplate
    void before(short actual, short expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(short actual, short expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualShortArrays {
    @BeforeTemplate
    void before(short[] actual, short[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(short[] actual, short[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualShortArraysWithMessage {
    @BeforeTemplate
    void before(short[] actual, short[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(short[] actual, short[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualInts {
    @BeforeTemplate
    void before(int actual, int expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(int actual, int expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualIntsWithMessage {
    @BeforeTemplate
    void before(int actual, int expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(int actual, int expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualIntArrays {
    @BeforeTemplate
    void before(int[] actual, int[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(int[] actual, int[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualIntArraysWithMessage {
    @BeforeTemplate
    void before(int[] actual, int[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(int[] actual, int[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualLongs {
    @BeforeTemplate
    void before(long actual, long expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(long actual, long expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualLongsWithMessage {
    @BeforeTemplate
    void before(long actual, long expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(long actual, long expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualLongArrays {
    @BeforeTemplate
    void before(long[] actual, long[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(long[] actual, long[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualLongArraysWithMessage {
    @BeforeTemplate
    void before(long[] actual, long[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(long[] actual, long[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualFloats {
    @BeforeTemplate
    void before(float actual, float expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualFloatsWithMessage {
    @BeforeTemplate
    void before(float actual, float expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualFloatsWithDelta {
    @BeforeTemplate
    void before(float actual, float expected, float delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta) {
      assertThat(actual).isEqualTo(expected, offset(delta));
    }
  }

  static final class XXXAssertEqualFloatsWithDeltaWithMessage {
    @BeforeTemplate
    void before(float actual, float expected, float delta, String message) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float actual, float expected, float delta, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected, offset(delta));
    }
  }

  static final class XXXAssertEqualFloatArrays {
    @BeforeTemplate
    void before(float[] actual, float[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float[] actual, float[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualFloatArraysWithMessage {
    @BeforeTemplate
    void before(float[] actual, float[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(float[] actual, float[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualDoubles {
    @BeforeTemplate
    void before(double actual, double expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualDoublesWithMessage {
    @BeforeTemplate
    void before(double actual, double expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualDoublesWithDelta {
    @BeforeTemplate
    void before(double actual, double expected, double delta) {
      assertEquals(actual, expected, delta);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta) {
      assertThat(actual).isEqualTo(expected, offset(delta));
    }
  }

  static final class XXXAssertEqualDoublesWithDeltaWithMessage {
    @BeforeTemplate
    void before(double actual, double expected, double delta, String message) {
      assertEquals(actual, expected, delta, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double actual, double expected, double delta, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected, offset(delta));
    }
  }

  static final class XXXAssertEqualDoubleArrays {
    @BeforeTemplate
    void before(double[] actual, double[] expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double[] actual, double[] expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualDoubleArraysWithMessage {
    @BeforeTemplate
    void before(double[] actual, double[] expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(double[] actual, double[] expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualStrings {
    @BeforeTemplate
    void before(String actual, String expected) {
      assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String actual, String expected) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  static final class XXXAssertEqualStringsWithMessage {
    @BeforeTemplate
    void before(String actual, String expected, String message) {
      assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String actual, String expected, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }
  }

  // BELOW: Experiment with more concise syntax.

  // XXX: Merge the assertions above into this one!
  static final class XXXAssertEquals {
    @BeforeTemplate
    void before(Iterator<?> actual, Iterator<?> expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Iterable<?> actual, Iterable<?> expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Collection<?> actual, Collection<?> expected) {
      assertEquals(actual, expected);
    }

    @BeforeTemplate
    void before(Set<?> actual, Set<?> expected) {
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

  static final class XXXAssertEqualsWithMessage {
    @BeforeTemplate
    void before(Iterator<?> actual, Iterator<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Iterable<?> actual, Iterable<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Collection<?> actual, Collection<?> expected, String message) {
      assertEquals(actual, expected, message);
    }

    @BeforeTemplate
    void before(Set<?> actual, Set<?> expected, String message) {
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

  // XXX: Skipped:
  // assertEquals Object[] Object[]
  // assertEquals Object[] Object[] msg
  // assertEqualsNoOrder Object[] Object[]
  // assertEqualsNoOrder Object[] Object[] msg
  // assertEqualsDeep Set Set
  // assertEqualsDeep Set Set msg
  // assertEqualsDeep Map Map
  // assertEqualsDeep Map Map msg

  // Also still TBD:
  // assertNotEquals: all except Object, Object [msg]
  // ^ Before doing this one, first migrate `assertEquals` above to the concise syntax.
  // assertThrows
  // expectThrows

  static final class XXXAssertObjectsNotEqual {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class XXXAssertObjectsNotEqualWithMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      assertThat(actual).withFailMessage(message).isNotEqualTo(expected);
    }
  }

  ///////////////////////////////////////////
  // XXX: The rules below enforce AssertJ best-practices.
  // XXX: The rules below don't handle intermediate steps such as `withFailMessage` and `as`. Fix.

  static final class AssertThatBooleanIsTrue<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(true),
          boolAssert.isEqualTo(Boolean.TRUE),
          boolAssert.isNotEqualTo(false),
          boolAssert.isNotEqualTo(Boolean.FALSE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isTrue();
    }
  }

  static final class AssertThatBooleanIsFalse<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(false),
          boolAssert.isEqualTo(Boolean.FALSE),
          boolAssert.isNotEqualTo(true),
          boolAssert.isNotEqualTo(Boolean.TRUE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isFalse();
    }
  }

  static final class AssertThatIntegerIsZero<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(0);
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isZero();
    }
  }

  static final class AssertThatIntegerIsNotZero<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotEqualTo(0);
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotZero();
    }
  }

  static final class AssertThatIntegerIsPositive<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isGreaterThan(0), intAssert.isGreaterThanOrEqualTo(1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isPositive();
    }
  }

  static final class AssertThatIntegerIsNotPositive<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isLessThanOrEqualTo(0), intAssert.isLessThan(1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotPositive();
    }
  }

  static final class AssertThatIntegerIsNegative<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isLessThan(0), intAssert.isLessThanOrEqualTo(-1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNegative();
    }
  }

  static final class AssertThatIntegerIsNotNegative<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isGreaterThanOrEqualTo(0), intAssert.isGreaterThan(-1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotNegative();
    }
  }

  /// XXX: Above this line: context-independent rewrite rules. Should be applied first.
  // XXX: Below this line: context-dependent rewrite rules.

  static final class AssertThatOptional<T> {
    @BeforeTemplate
    ObjectAssert<T> before(Optional<T> optional) {
      return assertThat(optional.get());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional) {
      return assertThat(optional).get();
    }
  }

  static final class AssertThatOptionalIsPresent<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isPresent()).isTrue(),
          assertThat(optional.isEmpty()).isFalse(),
          assertThat(optional).isNotEqualTo(Optional.empty()));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isPresent();
    }
  }

  static final class AssertThatOptionalIsEmpty<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isEmpty()).isTrue(),
          assertThat(optional.isPresent()).isFalse(),
          assertThat(optional).isEqualTo(Optional.empty()));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isEmpty();
    }
  }

  static final class AssertThatOptionalHasValue<T> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> optional, T value) {
      return assertThat(optional).isEqualTo(Optional.of(value));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional, T value) {
      return assertThat(optional).hasValue(value);
    }
  }

  static final class AssertThatOptionalHasValueMatching<T> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional.filter(predicate)).isPresent();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional).get().matches(predicate);
    }
  }

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsDouble`.
  static final class AssertThatOptionalDouble {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(OptionalDouble optional, double expected) {
      return assertThat(optional.getAsDouble()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalDoubleAssert after(OptionalDouble optional, double expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsInt`.
  static final class AssertThatOptionalInt {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(OptionalInt optional, int expected) {
      return assertThat(optional.getAsInt()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalIntAssert after(OptionalInt optional, int expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsLong`.
  static final class AssertThatOptionalLong {
    @BeforeTemplate
    AbstractLongAssert<?> before(OptionalLong optional, long expected) {
      return assertThat(optional.getAsLong()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalLongAssert after(OptionalLong optional, long expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  static final class AssertThatIsInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isInstanceOf(Refaster.<T>clazz());
    }
  }

  static final class AssertThatIsNotInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isNotInstanceOf(Refaster.<T>clazz());
    }
  }

  static final class AssertThatStringIsEmpty {
    @BeforeTemplate
    void before(String string) {
      Refaster.anyOf(
          assertThat(string).isEqualTo(""),
          assertThat(string).hasSize(0),
          assertThat(string).hasSizeLessThan(1),
          assertThat(string.isEmpty()).isTrue(),
          assertThat(string.length()).isEqualTo(0),
          assertThat(string.length()).isZero(),
          assertThat(string.length()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String string) {
      assertThat(string).isEmpty();
    }
  }

  static final class AssertThatStringIsNotEmpty {
    @BeforeTemplate
    AbstractAssert<?, ?> before(String string) {
      return Refaster.anyOf(
          assertThat(string).isNotEqualTo(""),
          assertThat(string).hasSizeGreaterThan(0),
          assertThat(string.isEmpty()).isFalse(),
          assertThat(string.length()).isNotEqualTo(0),
          assertThat(string.length()).isNotZero(),
          assertThat(string.length()).isPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string) {
      return assertThat(string).isNotEmpty();
    }
  }

  static final class AssertThatStringHasLength {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(String string, int length) {
      return assertThat(string.length()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, int length) {
      return assertThat(string).hasSize(length);
    }
  }

  // XXX: Most/all of these Iterable rules can also be applied to arrays.
  // XXX: Elsewhere add a rule to disallow `Collection.emptyList()` and variants as well as
  // `Arrays.asList()`.
  static final class AssertThatIterableIsEmpty<E> {
    @BeforeTemplate
    void before(Iterable<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable)
              .isEqualTo(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      ImmutableSortedSet.of(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable).hasSize(0),
          assertThat(iterable).hasSizeLessThan(1),
          assertThat(Iterables.isEmpty(iterable)).isTrue(),
          assertThat(iterable.iterator().hasNext()).isFalse(),
          assertThat(Iterables.size(iterable)).isZero(),
          assertThat(Iterables.size(iterable)).isNotPositive());
    }

    @BeforeTemplate
    void before(Collection<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable.isEmpty()).isTrue(),
          assertThat(iterable.size()).isZero(),
          assertThat(iterable.size()).isNotPositive());
    }

    @BeforeTemplate
    void before(List<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable).isEqualTo(Refaster.anyOf(ImmutableList.of(), new ArrayList<>())),
          assertThat(iterable).hasSize(0),
          assertThat(iterable).hasSizeLessThan(1));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Collection<E> iterable) {
      assertThat(iterable).isEmpty();
    }
  }

  static final class AssertThatIterableIsNotEmpty<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable)
              .isNotEqualTo(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      ImmutableSortedSet.of(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable).hasSizeGreaterThan(0),
          assertThat(iterable).hasSizeGreaterThanOrEqualTo(1),
          assertThat(Iterables.isEmpty(iterable)).isFalse(),
          assertThat(iterable.iterator().hasNext()).isTrue(),
          assertThat(Iterables.size(iterable)).isNotZero(),
          assertThat(Iterables.size(iterable)).isPositive());
    }

    @BeforeTemplate
    AbstractAssert<?, ?> before(Collection<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable.isEmpty()).isFalse(),
          assertThat(iterable.size()).isNotZero(),
          assertThat(iterable.size()).isPositive());
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable).isNotEqualTo(Refaster.anyOf(ImmutableList.of(), new ArrayList<>())),
          assertThat(iterable).hasSizeGreaterThan(0),
          assertThat(iterable).hasSizeGreaterThanOrEqualTo(1));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable) {
      return assertThat(iterable).isNotEmpty();
    }
  }

  static final class AssertThatIterableHasSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Iterable<E> iterable, int length) {
      return assertThat(Iterables.size(iterable)).isEqualTo(length);
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> iterable, int length) {
      return assertThat(iterable.size()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, int length) {
      return assertThat(iterable).hasSize(length);
    }
  }

  static final class AssertThatIterablesHaveSameSize<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable1, Iterable<E> iterable2) {
      return assertThat(iterable1).hasSize(Iterables.size(iterable2));
    }

    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable1, Collection<E> iterable2) {
      return assertThat(iterable1).hasSize(iterable2.size());
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable1, Iterable<E> iterable2) {
      return assertThat(iterable1).hasSize(Iterables.size(iterable2));
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable1, Collection<E> iterable2) {
      return assertThat(iterable1).hasSize(iterable2.size());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable1, Iterable<E> iterable2) {
      return assertThat(iterable1).hasSameSizeAs(iterable2);
    }
  }

  // XXX: Add a variant which checks the exact size.
  static final class AssertThatMapIsEmpty<K, V> {
    @BeforeTemplate
    void before(Map<K, V> map) {
      Refaster.anyOf(
          assertThat(map)
              .isEqualTo(
                  Refaster.anyOf(
                      ImmutableMap.of(),
                      ImmutableBiMap.of(),
                      ImmutableSortedMap.of(),
                      new HashMap<>(),
                      new LinkedHashMap<>(),
                      new TreeMap<>())),
          assertThat(map).hasSize(0),
          assertThat(Refaster.anyOf(map.keySet(), map.values())).hasSize(0),
          assertThat(map).hasSizeLessThan(1),
          assertThat(Refaster.anyOf(map.keySet(), map.values())).hasSizeLessThan(1),
          assertThat(map.isEmpty()).isTrue(),
          assertThat(Refaster.anyOf(map.keySet(), map.values()).isEmpty()).isTrue(),
          assertThat(Refaster.anyOf(map.size(), map.keySet().size(), map.values().size())).isZero(),
          assertThat(Refaster.anyOf(map.size(), map.keySet().size(), map.values().size()))
              .isNotPositive());
    }

    @BeforeTemplate
    void before2(Map<K, V> map) {
      assertThat(Refaster.anyOf(map.keySet(), map.values())).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Map<K, V> map) {
      assertThat(map).isEmpty();
    }
  }

  static final class AssertThatMapIsNotEmpty<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map) {
      return Refaster.anyOf(
          assertThat(map)
              .isNotEqualTo(
                  Refaster.anyOf(
                      ImmutableMap.of(),
                      ImmutableBiMap.of(),
                      ImmutableSortedMap.of(),
                      new HashMap<>(),
                      new LinkedHashMap<>(),
                      new TreeMap<>())),
          assertThat(map).hasSizeGreaterThan(0),
          assertThat(Refaster.anyOf(map.keySet(), map.values())).hasSizeGreaterThan(0),
          assertThat(map.isEmpty()).isFalse(),
          assertThat(Refaster.anyOf(map.keySet(), map.values()).isEmpty()).isFalse(),
          assertThat(Refaster.anyOf(map.size(), map.keySet().size(), map.values().size()))
              .isNotZero(),
          assertThat(Refaster.anyOf(map.size(), map.keySet().size(), map.values().size()))
              .isPositive());
    }

    @BeforeTemplate
    IterableAssert<?> before2(Map<K, V> map) {
      return assertThat(Refaster.anyOf(map.keySet(), map.values())).isNotEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map) {
      return assertThat(map).isNotEmpty();
    }
  }

  static final class AssertThatMapHasSize<K, V> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Map<K, V> map, int length) {
      return assertThat(Refaster.anyOf(map.size(), map.keySet().size(), map.values().size()))
          .isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, int length) {
      return assertThat(map).hasSize(length);
    }
  }

  static final class AssertThatMapsHaveSameSize<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map1, Map<K, V> map2) {
      return Refaster.anyOf(
          assertThat(map1)
              .hasSize(Refaster.anyOf(map2.size(), map2.keySet().size(), map2.values().size())),
          assertThat(Refaster.anyOf(map1.keySet(), map1.values()))
              .hasSize(Refaster.anyOf(map2.size(), map2.keySet().size(), map2.values().size())));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map1, Map<K, V> map2) {
      return assertThat(map1).hasSameSizeAs(map2);
    }
  }

  // XXX: Should also add a rule (elsewhere) to simplify `map.keySet().contains(key)`.
  static final class AssertThatMapContainsKey<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
      return assertThat(map.containsKey(key)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).containsKey(key);
    }
  }

  static final class AssertThatMapDoesNotContainKey<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
      return assertThat(map.containsKey(key)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).doesNotContainKey(key);
    }
  }

  // XXX: There's a bunch of variations on this theme.
  // XXX: The `Iterables.getOnlyElement` variant doesn't match in
  // `analytics/analytics-message-listener`. Why?
  // XXX: Here and elsewhere: make sure `Arrays.asList` is migrated away from, then drop it here.
  static final class AssertThatOnlyElementIsEqualTo<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable, E expected) {
      return Refaster.anyOf(
          assertThat(Iterables.getOnlyElement(iterable)).isEqualTo(expected),
          assertThat(iterable)
              .isEqualTo(
                  Refaster.anyOf(
                      ImmutableList.of(expected),
                      Arrays.asList(expected),
                      ImmutableSet.of(expected),
                      ImmutableMultiset.of(expected))));
    }

    @BeforeTemplate
    AbstractAssert<?, ?> before(List<E> iterable, E expected) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableList.of(expected), Arrays.asList(expected)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E expected) {
      return assertThat(iterable).containsExactly(expected);
    }
  }

  static final class AssertThatOnlyComparableElementIsEqualTo<E extends Comparable<? super E>> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable, E expected) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.<Object>anyOf(
                  ImmutableSortedSet.of(expected), ImmutableSortedMultiset.of(expected)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E expected) {
      return assertThat(iterable).containsExactly(expected);
    }
  }

  static final class AssertThatIterableContainsTwoSpecificElementsInOrder<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2), Arrays.asList(e1, e2)));
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable, E e1, E e2) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2), Arrays.asList(e1, e2)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2) {
      return assertThat(iterable).containsExactly(e1, e2);
    }
  }

  static final class AssertThatIterableContainsThreeSpecificElementsInOrder<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2, e3), Arrays.asList(e1, e2, e3)));
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable, E e1, E e2, E e3) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2, e3), Arrays.asList(e1, e2, e3)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3) {
      return assertThat(iterable).containsExactly(e1, e2, e3);
    }
  }

  static final class AssertThatIterableContainsFourSpecificElementsInOrder<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(ImmutableList.of(e1, e2, e3, e4), Arrays.asList(e1, e2, e3, e4)));
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable, E e1, E e2, E e3, E e4) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(ImmutableList.of(e1, e2, e3, e4), Arrays.asList(e1, e2, e3, e4)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
      return assertThat(iterable).containsExactly(e1, e2, e3, e4);
    }
  }

  // XXX: Up to 12...? :)
  static final class AssertThatIterableContainsFiveSpecificElementsInOrder<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(
                  ImmutableList.of(e1, e2, e3, e4, e5), Arrays.asList(e1, e2, e3, e4, e5)));
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(
                  ImmutableList.of(e1, e2, e3, e4, e5), Arrays.asList(e1, e2, e3, e4, e5)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(iterable).containsExactly(e1, e2, e3, e4, e5);
    }
  }

  // XXX: For this and other variants we could also match other behavior-preserving collection
  // operations.
  static final class AssertThatStreamContainsTwoSpecificElementsInOrder<E> {
    @BeforeTemplate
    ListAssert<E> before(Stream<E> stream, E e1, E e2) {
      return Refaster.anyOf(
          assertThat(stream.collect(toImmutableList()))
              .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2), Arrays.asList(e1, e2))),
          assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2) {
      return assertThat(stream).containsExactly(e1, e2);
    }
  }

  static final class AssertThatStreamContainsThreeSpecificElementsInOrder<E> {
    @BeforeTemplate
    ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3) {
      return Refaster.anyOf(
          assertThat(stream.collect(toImmutableList()))
              .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2, e3), Arrays.asList(e1, e2, e3))),
          assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2, e3));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3) {
      return assertThat(stream).containsExactly(e1, e2, e3);
    }
  }

  static final class AssertThatStreamContainsFourSpecificElementsInOrder<E> {
    @BeforeTemplate
    ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4) {
      return Refaster.anyOf(
          assertThat(stream.collect(toImmutableList()))
              .isEqualTo(
                  Refaster.anyOf(ImmutableList.of(e1, e2, e3, e4), Arrays.asList(e1, e2, e3, e4))),
          assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2, e3, e4));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4) {
      return assertThat(stream).containsExactly(e1, e2, e3, e4);
    }
  }

  // XXX: Up to 12...? :)
  static final class AssertThatStreamContainsFiveSpecificElementsInOrder<E> {
    @BeforeTemplate
    ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
      return Refaster.anyOf(
          assertThat(stream.collect(toImmutableList()))
              .isEqualTo(
                  Refaster.anyOf(
                      ImmutableList.of(e1, e2, e3, e4, e5), Arrays.asList(e1, e2, e3, e4, e5))),
          assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2, e3, e4, e5));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(stream).containsExactly(e1, e2, e3, e4, e5);
    }
  }

  static final class AssertThatIterableContainsTwoSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableSet.of(e1, e2), ImmutableMultiset.of(e1, e2)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2) {
      return assertThat(iterable).containsExactlyInAnyOrder(e1, e2);
    }
  }

  static final class AssertThatIterableContainsThreeSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3) {
      return assertThat(iterable)
          .isEqualTo(Refaster.anyOf(ImmutableSet.of(e1, e2, e3), ImmutableMultiset.of(e1, e2, e3)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3) {
      return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3);
    }
  }

  static final class AssertThatIterableContainsFourSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(
                  ImmutableSet.of(e1, e2, e3, e4), ImmutableMultiset.of(e1, e2, e3, e4)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
      return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3, e4);
    }
  }

  // XXX: Up to 12...? :)
  static final class AssertThatIterableContainsFiveSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(iterable)
          .isEqualTo(
              Refaster.anyOf(
                  ImmutableSet.of(e1, e2, e3, e4, e5), ImmutableMultiset.of(e1, e2, e3, e4, e5)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3, e4, e5);
    }
  }

  static final class AssertThatStreamContainsTwoSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Stream<E> stream, E e1, E e2) {
      return assertThat(stream.collect(toImmutableSet())).isEqualTo(ImmutableSet.of(e1, e2));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2) {
      return assertThat(stream).containsOnly(e1, e2);
    }
  }

  static final class AssertThatStreamContainsThreeSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Stream<E> stream, E e1, E e2, E e3) {
      return assertThat(stream.collect(toImmutableSet())).isEqualTo(ImmutableSet.of(e1, e2, e3));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3) {
      return assertThat(stream).containsOnly(e1, e2, e3);
    }
  }

  // XXX: Up to 12...? :)
  static final class AssertThatStreamContainsFiveSpecificElements<E> {
    @BeforeTemplate
    IterableAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(stream.collect(toImmutableSet()))
          .isEqualTo(ImmutableSet.of(e1, e2, e3, e4, e5));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
      return assertThat(stream).containsOnly(e1, e2, e3, e4, e5);
    }
  }
}
