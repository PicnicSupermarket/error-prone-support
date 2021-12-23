package tech.picnic.errorprone.refastertemplates;

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
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqual;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualArrayIterationOrder;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualArrayIterationOrderWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualArraysIrrespectiveOfOrder;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualArraysIrrespectiveOfOrderWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualDoublesWithDelta;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualDoublesWithDeltaWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualFloatsWithDelta;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualFloatsWithDeltaWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualIterableIterationOrder;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualIterableIterationOrderWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualIteratorIterationOrder;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualIteratorIterationOrderWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualSets;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualSetsWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertEqualWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertFalse;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertFalseWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNotNull;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNotNullWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNotSame;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNotSameWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNull;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertNullWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertSame;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertSameWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertThrows;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertThrowsWithType;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertTrue;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertTrueWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequal;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequalDoublesWithDelta;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequalDoublesWithDeltaWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequalFloatsWithDelta;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequalFloatsWithDeltaWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.AssertUnequalWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.Fail;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.FailWithMessage;
import tech.picnic.errorprone.refastertemplates.TestNGToAssertJTemplates.FailWithMessageAndThrowable;

@TemplateCollection(TestNGToAssertJTemplates.class)
final class TestNGToAssertJTemplatesTest implements RefasterTemplateTestCase {
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

  @Template(Fail.class)
  void testFail() {
    org.testng.Assert.fail();
  }

  @Template(FailWithMessage.class)
  void testFailWithMessage() {
    org.testng.Assert.fail("foo");
  }

  @Template(FailWithMessageAndThrowable.class)
  void testFailWithMessageAndThrowable() {
    org.testng.Assert.fail("foo", new IllegalStateException());
  }

  @Template(AssertTrue.class)
  void testAssertTrue() {
    assertTrue(true);
  }

  @Template(AssertTrueWithMessage.class)
  void testAssertTrueWithMessage() {
    assertTrue(true, "foo");
  }

  @Template(AssertFalse.class)
  void testAssertFalse() {
    assertFalse(true);
  }

  @Template(AssertFalseWithMessage.class)
  void testAssertFalseWithMessage() {
    assertFalse(true, "message");
  }

  @Template(AssertNull.class)
  void testAssertNull() {
    assertNull(new Object());
  }

  @Template(AssertNullWithMessage.class)
  void testAssertNullWithMessage() {
    assertNull(new Object(), "foo");
  }

  @Template(AssertNotNull.class)
  void testAssertNotNull() {
    assertNotNull(new Object());
  }

  @Template(AssertNotNullWithMessage.class)
  void testAssertNotNullWithMessage() {
    assertNotNull(new Object(), "foo");
  }

  @Template(AssertSame.class)
  void testAssertSame() {
    assertSame(new Object(), new Object());
  }

  @Template(AssertSameWithMessage.class)
  void testAssertSameWithMessage() {
    assertSame(new Object(), new Object(), "foo");
  }

  @Template(AssertNotSame.class)
  void testAssertNotSame() {
    assertNotSame(new Object(), new Object());
  }

  @Template(AssertNotSameWithMessage.class)
  void testAssertNotSameWithMessage() {
    assertNotSame(new Object(), new Object(), "foo");
  }

  @Template(AssertEqual.class)
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
    assertEquals(ImmutableSet.of(), ImmutableSet.of());
    assertEquals(ImmutableMap.of(), ImmutableMap.of());
  }

  @Template(AssertEqualWithMessage.class)
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

  @Template(AssertEqualFloatsWithDelta.class)
  void testAssertEqualFloatsWithDelta() {
    assertEquals(0.0F, 0.0F, 0.0F);
  }

  @Template(AssertEqualFloatsWithDeltaWithMessage.class)
  void testAssertEqualFloatsWithDeltaWithMessage() {
    assertEquals(0.0F, 0.0F, 0.0F, "foo");
  }

  @Template(AssertEqualDoublesWithDelta.class)
  void testAssertEqualDoublesWithDelta() {
    assertEquals(0.0, 0.0, 0.0);
  }

  @Template(AssertEqualDoublesWithDeltaWithMessage.class)
  void testAssertEqualDoublesWithDeltaWithMessage() {
    assertEquals(0.0, 0.0, 0.0, "foo");
  }

  @Template(AssertEqualArrayIterationOrder.class)
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

  @Template(AssertEqualArrayIterationOrderWithMessage.class)
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

  @Template(AssertEqualArraysIrrespectiveOfOrder.class)
  void testAssertEqualArraysIrrespectiveOfOrder() {
    assertEqualsNoOrder(new Object[0], new Object[0]);
  }

  @Template(AssertEqualArraysIrrespectiveOfOrderWithMessage.class)
  void testAssertEqualArraysIrrespectiveOfOrderWithMessage() {
    assertEqualsNoOrder(new Object[0], new Object[0], "foo");
  }

  @Template(AssertEqualIteratorIterationOrder.class)
  void testAssertEqualIteratorIterationOrder() {
    assertEquals(
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()));
  }

  @Template(AssertEqualIteratorIterationOrderWithMessage.class)
  void testAssertEqualIteratorIterationOrderWithMessage() {
    assertEquals(
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        Iterators.unmodifiableIterator(new ArrayList<>().iterator()),
        "foo");
  }

  @Template(AssertEqualIterableIterationOrder.class)
  void testAssertEqualIterableIterationOrder() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<>()),
        Iterables.unmodifiableIterable(new ArrayList<>()));
    assertEquals(
        Collections.synchronizedCollection(new ArrayList<>()),
        Collections.synchronizedCollection(new ArrayList<>()));
  }

  @Template(AssertEqualIterableIterationOrderWithMessage.class)
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

  @Template(AssertEqualSets.class)
  void testAssertEqualSets() {
    assertEquals(ImmutableSet.of(), ImmutableSet.of());
  }

  @Template(AssertEqualSetsWithMessage.class)
  void testAssertEqualSetsWithMessage() {
    assertEquals(ImmutableSet.of(), ImmutableSet.of(), "foo");
  }

  @Template(AssertUnequal.class)
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

  @Template(AssertUnequalWithMessage.class)
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

  @Template(AssertUnequalFloatsWithDelta.class)
  void testAssertUnequalFloatsWithDelta() {
    assertNotEquals(0.0F, 0.0F, 0.0F);
  }

  @Template(AssertUnequalFloatsWithDeltaWithMessage.class)
  void testAssertUnequalFloatsWithDeltaWithMessage() {
    assertNotEquals(0.0F, 0.0F, 0.0F, "foo");
  }

  @Template(AssertUnequalDoublesWithDelta.class)
  void testAssertUnequalDoublesWithDelta() {
    assertNotEquals(0.0, 0.0, 0.0);
  }

  @Template(AssertUnequalDoublesWithDeltaWithMessage.class)
  void testAssertUnequalDoublesWithDeltaWithMessage() {
    assertNotEquals(0.0, 0.0, 0.0, "foo");
  }

  @Template(AssertThrows.class)
  void testAssertThrows() {
    assertThrows(() -> {});
  }

  @Template(AssertThrowsWithType.class)
  void testAssertThrowsWithType() {
    assertThrows(IllegalStateException.class, () -> {});
  }
}
