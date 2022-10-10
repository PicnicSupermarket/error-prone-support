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
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collections;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class TestNGToAssertJTemplatesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        (Runnable) () -> assertEquals(new byte[0], null),
        (Runnable) () -> assertEqualsNoOrder(null, null),
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
    assertEquals(true, true);
    assertEquals((byte) 0, (byte) 0);
    assertEquals((char) 0, (char) 0);
    assertEquals((short) 0, (short) 0);
    assertEquals(0, 0);
    assertEquals(0L, 0L);
    assertEquals(0.0F, 0.0F);
    assertEquals(0.0, 0.0);
    assertEquals(new Object(), new Object());
    assertEquals("actual", "expected");
    assertEquals(ImmutableMap.of(), ImmutableMap.of());
  }

  void testAssertEqualWithMessage() {
    assertEquals(true, true, "foo");
    assertEquals((byte) 0, (byte) 0, "bar");
    assertEquals((char) 0, (char) 0, "baz");
    assertEquals((short) 0, (short) 0, "qux");
    assertEquals(0, 0, "quux");
    assertEquals(0L, 0L, "quuz");
    assertEquals(0.0F, 0.0F, "corge");
    assertEquals(0.0, 0.0, "grault");
    assertEquals(new Object(), new Object(), "garply");
    assertEquals("actual", "expected", "waldo");
    assertEquals(ImmutableMap.of(), ImmutableMap.of(), "plugh");
  }

  void testAssertEqualFloatsWithDelta() {
    assertEquals(0.0F, 0.0F, 0.0F);
  }

  void testAssertEqualFloatsWithDeltaWithMessage() {
    assertEquals(0.0F, 0.0F, 0.0F, "foo");
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

  void testAssertEqualArraysIrrespectiveOfOrder() {
    assertEqualsNoOrder(new Object[0], new Object[0]);
  }

  void testAssertEqualArraysIrrespectiveOfOrderWithMessage() {
    assertEqualsNoOrder(new Object[0], new Object[0], "foo");
  }

  void testAssertEqualIteratorIterationOrder() {
    assertEquals(
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()));
  }

  void testAssertEqualIteratorIterationOrderWithMessage() {
    assertEquals(
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        "foo");
  }

  void testAssertEqualIterableIterationOrder() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<>()),
        Iterables.unmodifiableIterable(new ArrayList<>()));
    assertEquals(
        Collections.synchronizedCollection(new ArrayList<>()),
        Collections.synchronizedCollection(new ArrayList<>()));
  }

  void testAssertEqualIterableIterationOrderWithMessage() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<>()),
        Iterables.unmodifiableIterable(new ArrayList<>()),
        "foo");
    assertEquals(
        Collections.synchronizedCollection(new ArrayList<>()),
        Collections.synchronizedCollection(new ArrayList<>()),
        "bar");
  }

  void testAssertEqualSets() {
    assertEquals(ImmutableSet.of(), ImmutableSet.of());
  }

  void testAssertEqualSetsWithMessage() {
    assertEquals(ImmutableSet.of(), ImmutableSet.of(), "foo");
  }

  void testAssertUnequal() {
    assertNotEquals(true, true);
    assertNotEquals((byte) 0, (byte) 0);
    assertNotEquals((char) 0, (char) 0);
    assertNotEquals((short) 0, (short) 0);
    assertNotEquals(0, 0);
    assertNotEquals(0L, 0L);
    assertNotEquals(0.0F, 0.0F);
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
    assertNotEquals(0.0F, 0.0F, "corge");
    assertNotEquals(0.0, 0.0, "grault");
    assertNotEquals(new Object(), new Object(), "garply");
    assertNotEquals("actual", "expected", "waldo");
    assertNotEquals(ImmutableSet.of(), ImmutableSet.of(), "fred");
    assertNotEquals(ImmutableMap.of(), ImmutableMap.of(), "plugh");
  }

  void testAssertUnequalFloatsWithDelta() {
    assertNotEquals(0.0F, 0.0F, 0.0F);
  }

  void testAssertUnequalFloatsWithDeltaWithMessage() {
    assertNotEquals(0.0F, 0.0F, 0.0F, "foo");
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
