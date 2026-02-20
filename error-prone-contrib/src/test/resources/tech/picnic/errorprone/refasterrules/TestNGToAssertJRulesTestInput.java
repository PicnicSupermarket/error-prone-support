package tech.picnic.errorprone.refasterrules;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class TestNGToAssertJRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        (Runnable) () -> assertEquals(new byte[0], null),
        (Runnable) () -> assertEqualsNoOrder((Object[]) null, null),
        (Runnable) () -> assertFalse(true),
        (Runnable) () -> assertNotEquals(new byte[0], null),
        (Runnable) () -> assertNotNull(null),
        (Runnable) () -> assertNotSame(null, null),
        (Runnable) () -> assertNull(null),
        (Runnable) () -> assertSame(null, null),
        (Runnable) () -> assertThrows(null),
        (Runnable) () -> assertTrue(true));
  }

  void testFail() {
    org.testng.Assert.fail();
  }

  void testFailWithMessage() {
    org.testng.Assert.fail("foo");
  }

  void testFailWithMessageAndThrowable() {
    org.testng.Assert.fail("foo", new IllegalStateException());
  }

  void testAssertTrue() {
    assertTrue(true);
  }

  void testAssertTrueWithMessage() {
    assertTrue(true, "foo");
  }

  void testAssertFalse() {
    assertFalse(true);
  }

  void testAssertFalseWithMessage() {
    assertFalse(true, "message");
  }

  void testAssertNull() {
    assertNull(new Object());
  }

  void testAssertNullWithMessage() {
    assertNull(new Object(), "foo");
  }

  void testAssertNotNull() {
    assertNotNull(new Object());
  }

  void testAssertNotNullWithMessage() {
    assertNotNull(new Object(), "foo");
  }

  void testAssertSame() {
    assertSame(new Object(), new Object());
  }

  void testAssertSameWithMessage() {
    assertSame(new Object(), new Object(), "foo");
  }

  void testAssertNotSame() {
    assertNotSame(new Object(), new Object());
  }

  void testAssertNotSameWithMessage() {
    assertNotSame(new Object(), new Object(), "foo");
  }

  void testAssertEqual() {
    assertEquals(true, false);
    assertEquals(true, Boolean.FALSE);
    assertEquals(Boolean.TRUE, false);
    assertEquals(Boolean.TRUE, Boolean.FALSE);
    assertEquals((byte) 0, (byte) 1);
    assertEquals((byte) 0, Byte.decode("1"));
    assertEquals(Byte.decode("0"), (byte) 1);
    assertEquals(Byte.decode("0"), Byte.decode("1"));
    assertEquals('a', 'b');
    assertEquals('a', Character.valueOf('b'));
    assertEquals(Character.valueOf('a'), 'b');
    assertEquals(Character.valueOf('a'), Character.valueOf('b'));
    assertEquals((short) 0, (short) 1);
    assertEquals((short) 0, Short.decode("1"));
    assertEquals(Short.decode("0"), (short) 1);
    assertEquals(Short.decode("0"), Short.decode("1"));
    assertEquals(0, 1);
    assertEquals(0, Integer.valueOf(1));
    assertEquals(Integer.valueOf(0), 1);
    assertEquals(Integer.valueOf(0), Integer.valueOf(1));
    assertEquals(0L, 1L);
    assertEquals(0L, Long.valueOf(1));
    assertEquals(Long.valueOf(0), 1L);
    assertEquals(Long.valueOf(0), Long.valueOf(1));
    assertEquals(0.0f, 1.0f);
    assertEquals(0.0f, Float.valueOf(1.0f));
    assertEquals(Float.valueOf(0.0f), 1.0f);
    assertEquals(Float.valueOf(0.0f), Float.valueOf(1.0f));
    assertEquals(0.0, 1.0);
    assertEquals(0.0, Double.valueOf(1.0));
    assertEquals(Double.valueOf(0.0), 1.0);
    assertEquals(Double.valueOf(0.0), Double.valueOf(1.0));
    assertEquals(new Object(), new StringBuilder());
    assertEquals("actual", "expected");
    assertEquals(ImmutableMap.of(), ImmutableMap.of(1, 2));
  }

  void testAssertEqualWithMessage() {
    assertEquals(true, false, "foo");
    assertEquals(true, Boolean.FALSE, "bar");
    assertEquals(Boolean.TRUE, false, "baz");
    assertEquals(Boolean.TRUE, Boolean.FALSE, "qux");
    assertEquals((byte) 0, (byte) 1, "quux");
    assertEquals((byte) 0, Byte.decode("1"), "corge");
    assertEquals(Byte.decode("0"), (byte) 1, "grault");
    assertEquals(Byte.decode("0"), Byte.decode("1"), "garply");
    assertEquals('a', 'b', "waldo");
    assertEquals('a', Character.valueOf('b'), "fred");
    assertEquals(Character.valueOf('a'), 'b', "plugh");
    assertEquals(Character.valueOf('a'), Character.valueOf('b'), "xyzzy");
    assertEquals((short) 0, (short) 1, "thud");
    assertEquals((short) 0, Short.decode("1"), "foo");
    assertEquals(Short.decode("0"), (short) 1, "bar");
    assertEquals(Short.decode("0"), Short.decode("1"), "baz");
    assertEquals(0, 1, "qux");
    assertEquals(0, Integer.valueOf(1), "quux");
    assertEquals(Integer.valueOf(0), 1, "corge");
    assertEquals(Integer.valueOf(0), Integer.valueOf(1), "grault");
    assertEquals(0L, 1L, "garply");
    assertEquals(0L, Long.valueOf(1), "waldo");
    assertEquals(Long.valueOf(0), 1L, "fred");
    assertEquals(Long.valueOf(0), Long.valueOf(1), "plugh");
    assertEquals(0.0f, 1.0f, "xyzzy");
    assertEquals(0.0f, Float.valueOf(1.0f), "thud");
    assertEquals(Float.valueOf(0.0f), 1.0f, "foo");
    assertEquals(Float.valueOf(0.0f), Float.valueOf(1.0f), "bar");
    assertEquals(0.0, 1.0, "baz");
    assertEquals(0.0, Double.valueOf(1.0), "qux");
    assertEquals(Double.valueOf(0.0), 1.0, "quux");
    assertEquals(Double.valueOf(0.0), Double.valueOf(1.0), "corge");
    assertEquals(new Object(), new StringBuilder(), "grault");
    assertEquals("actual", "expected", "garply");
    assertEquals(ImmutableMap.of(), ImmutableMap.of(1, 2), "waldo");
  }

  void testAssertEqualFloatsWithDelta() {
    assertEquals(0.0f, 0.0f, 0.0f);
  }

  void testAssertEqualFloatsWithDeltaWithMessage() {
    assertEquals(0.0f, 0.0f, 0.0f, "foo");
  }

  void testAssertEqualDoublesWithDelta() {
    assertEquals(0.0, 0.0, 0.0);
  }

  void testAssertEqualDoublesWithDeltaWithMessage() {
    assertEquals(0.0, 0.0, 0.0, "foo");
  }

  void testAssertEqualArrayIterationOrder() {
    assertEquals(new boolean[0], new boolean[0]);
    assertEquals(new byte[0], new byte[0]);
    assertEquals(new char[0], new char[0]);
    assertEquals(new short[0], new short[0]);
    assertEquals(new int[0], new int[0]);
    assertEquals(new long[0], new long[0]);
    assertEquals(new float[0], new float[0]);
    assertEquals(new double[0], new double[0]);
    assertEquals(new Object[0], new Object[0]);
  }

  void testAssertEqualArrayIterationOrderWithMessage() {
    assertEquals(new boolean[0], new boolean[0], "foo");
    assertEquals(new byte[0], new byte[0], "bar");
    assertEquals(new char[0], new char[0], "baz");
    assertEquals(new short[0], new short[0], "qux");
    assertEquals(new int[0], new int[0], "quux");
    assertEquals(new long[0], new long[0], "quuz");
    assertEquals(new float[0], new float[0], "corge");
    assertEquals(new double[0], new double[0], "grault");
    assertEquals(new Object[0], new Object[0], "garply");
  }

  void testAssertEqualFloatArraysWithDelta() {
    assertEquals(new float[0], new float[0], 0.0f);
  }

  void testAssertEqualFloatArraysWithDeltaWithMessage() {
    assertEquals(new float[0], new float[0], 0.0f, "foo");
  }

  void testAssertEqualDoubleArraysWithDelta() {
    assertEquals(new double[0], new double[0], 0.0);
  }

  void testAssertEqualDoubleArraysWithDeltaWithMessage() {
    assertEquals(new double[0], new double[0], 0.0, "foo");
  }

  void testAssertEqualArraysIrrespectiveOfOrder() {
    assertEqualsNoOrder(new Object[0], new Object[0]);
  }

  void testAssertEqualArraysIrrespectiveOfOrderWithMessage() {
    assertEqualsNoOrder(new Object[0], new Object[0], "foo");
  }

  void testAssertEqualIteratorIterationOrder() {
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<Integer>().iterator());
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator());
  }

  void testAssertEqualIteratorIterationOrderWithMessage() {
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<Integer>().iterator(), "foo");
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator(), "bar");
  }

  void testAssertEqualIterableIterationOrder() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<Integer>()));
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<String>()));
    assertEquals(new ArrayList<Number>(), new ArrayList<Integer>());
    assertEquals(new ArrayList<Number>(), new ArrayList<String>());
  }

  void testAssertEqualIterableIterationOrderWithMessage() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<Integer>()),
        "foo");
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<String>()),
        "bar");
    assertEquals(new ArrayList<Number>(), new ArrayList<Integer>(), "baz");
    assertEquals(new ArrayList<Number>(), new ArrayList<String>(), "qux");
  }

  void testAssertEqualSets() {
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<Integer>of());
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of());
  }

  void testAssertEqualSetsWithMessage() {
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<Integer>of(), "foo");
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of(), "bar");
  }

  void testAssertUnequal() {
    assertNotEquals(true, true);
    assertNotEquals((byte) 0, (byte) 0);
    assertNotEquals((char) 0, (char) 0);
    assertNotEquals((short) 0, (short) 0);
    assertNotEquals(0, 0);
    assertNotEquals(0L, 0L);
    assertNotEquals(0.0f, 0.0f);
    assertNotEquals(0.0, 0.0);
    assertNotEquals(new Object(), new Object());
    assertNotEquals("actual", "expected");
    assertNotEquals(ImmutableSet.of(), ImmutableSet.of());
    assertNotEquals(ImmutableMap.of(), ImmutableMap.of());
  }

  void testAssertUnequalWithMessage() {
    assertNotEquals(true, true, "foo");
    assertNotEquals((byte) 0, (byte) 0, "bar");
    assertNotEquals((char) 0, (char) 0, "baz");
    assertNotEquals((short) 0, (short) 0, "qux");
    assertNotEquals(0, 0, "quux");
    assertNotEquals(0L, 0L, "quuz");
    assertNotEquals(0.0f, 0.0f, "corge");
    assertNotEquals(0.0, 0.0, "grault");
    assertNotEquals(new Object(), new Object(), "garply");
    assertNotEquals("actual", "expected", "waldo");
    assertNotEquals(ImmutableSet.of(), ImmutableSet.of(), "fred");
    assertNotEquals(ImmutableMap.of(), ImmutableMap.of(), "plugh");
  }

  void testAssertUnequalFloatsWithDelta() {
    assertNotEquals(0.0f, 0.0f, 0.0f);
  }

  void testAssertUnequalFloatsWithDeltaWithMessage() {
    assertNotEquals(0.0f, 0.0f, 0.0f, "foo");
  }

  void testAssertUnequalDoublesWithDelta() {
    assertNotEquals(0.0, 0.0, 0.0);
  }

  void testAssertUnequalDoublesWithDeltaWithMessage() {
    assertNotEquals(0.0, 0.0, 0.0, "foo");
  }

  void testAssertThrows() {
    assertThrows(() -> {});
  }

  void testAssertThrowsWithType() {
    assertThrows(IllegalStateException.class, () -> {});
  }
}
