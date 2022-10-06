package tech.picnic.errorprone.refastertemplates.output;

import static com.google.common.collect.ImmutableList.copyOf;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collections;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

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

  void testFail() {
    throw new AssertionError();
  }

  void testFailWithMessage() {
    fail("foo");
  }

  void testFailWithMessageAndThrowable() {
    fail("foo", new IllegalStateException());
  }

  void testAssertTrue() {
    assertThat(true).isTrue();
  }

  void testAssertTrueWithMessage() {
    assertThat(true).withFailMessage("foo").isTrue();
  }

  void testAssertFalse() {
    assertThat(true).isFalse();
  }

  void testAssertFalseWithMessage() {
    assertThat(true).withFailMessage("message").isFalse();
  }

  void testAssertNull() {
    assertThat(new Object()).isNull();
  }

  void testAssertNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  void testAssertNotNull() {
    assertThat(new Object()).isNotNull();
  }

  void testAssertNotNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  void testAssertSame() {
    assertThat(new Object()).isSameAs(new Object());
  }

  void testAssertSameWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isSameAs(new Object());
  }

  void testAssertNotSame() {
    assertThat(new Object()).isNotSameAs(new Object());
  }

  void testAssertNotSameWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNotSameAs(new Object());
  }

  void testAssertEqual() {
    assertThat(true).isEqualTo(true);
    assertThat((byte) 0).isEqualTo((byte) 0);
    assertThat((char) 0).isEqualTo((char) 0);
    assertThat((short) 0).isEqualTo((short) 0);
    assertThat(0).isEqualTo(0);
    assertThat(0L).isEqualTo(0L);
    assertThat(0.0F).isEqualTo(0.0F);
    assertThat(0.0).isEqualTo(0.0);
    assertThat(new Object()).isEqualTo(new Object());
    assertThat("actual").isEqualTo("expected");
    assertThat(ImmutableMap.of()).isEqualTo(ImmutableMap.of());
  }

  void testAssertEqualWithMessage() {
    assertThat(true).withFailMessage("foo").isEqualTo(true);
    assertThat((byte) 0).withFailMessage("bar").isEqualTo((byte) 0);
    assertThat((char) 0).withFailMessage("baz").isEqualTo((char) 0);
    assertThat((short) 0).withFailMessage("qux").isEqualTo((short) 0);
    assertThat(0).withFailMessage("quux").isEqualTo(0);
    assertThat(0L).withFailMessage("quuz").isEqualTo(0L);
    assertThat(0.0F).withFailMessage("corge").isEqualTo(0.0F);
    assertThat(0.0).withFailMessage("grault").isEqualTo(0.0);
    assertThat(new Object()).withFailMessage("garply").isEqualTo(new Object());
    assertThat("actual").withFailMessage("waldo").isEqualTo("expected");
    assertThat(ImmutableMap.of()).withFailMessage("plugh").isEqualTo(ImmutableMap.of());
  }

  void testAssertEqualFloatsWithDelta() {
    assertThat(0.0F).isCloseTo(0.0F, offset(0.0F));
  }

  void testAssertEqualFloatsWithDeltaWithMessage() {
    assertThat(0.0F).withFailMessage("foo").isCloseTo(0.0F, offset(0.0F));
  }

  void testAssertEqualDoublesWithDelta() {
    assertThat(0.0).isCloseTo(0.0, offset(0.0));
  }

  void testAssertEqualDoublesWithDeltaWithMessage() {
    assertThat(0.0).withFailMessage("foo").isCloseTo(0.0, offset(0.0));
  }

  void testAssertEqualArrayIterationOrder() {
    assertThat(new boolean[0]).containsExactly(new boolean[0]);
    assertThat(new byte[0]).containsExactly(new byte[0]);
    assertThat(new char[0]).containsExactly(new char[0]);
    assertThat(new short[0]).containsExactly(new short[0]);
    assertThat(new int[0]).containsExactly(new int[0]);
    assertThat(new long[0]).containsExactly(new long[0]);
    assertThat(new float[0]).containsExactly(new float[0]);
    assertThat(new double[0]).containsExactly(new double[0]);
    assertThat(new Object[0]).containsExactly(new Object[0]);
  }

  void testAssertEqualArrayIterationOrderWithMessage() {
    assertThat(new boolean[0]).withFailMessage("foo").containsExactly(new boolean[0]);
    assertThat(new byte[0]).withFailMessage("bar").containsExactly(new byte[0]);
    assertThat(new char[0]).withFailMessage("baz").containsExactly(new char[0]);
    assertThat(new short[0]).withFailMessage("qux").containsExactly(new short[0]);
    assertThat(new int[0]).withFailMessage("quux").containsExactly(new int[0]);
    assertThat(new long[0]).withFailMessage("quuz").containsExactly(new long[0]);
    assertThat(new float[0]).withFailMessage("corge").containsExactly(new float[0]);
    assertThat(new double[0]).withFailMessage("grault").containsExactly(new double[0]);
    assertThat(new Object[0]).withFailMessage("garply").containsExactly(new Object[0]);
  }

  void testAssertEqualArraysIrrespectiveOfOrder() {
    assertThat(new Object[0]).containsExactlyInAnyOrder(new Object[0]);
  }

  void testAssertEqualArraysIrrespectiveOfOrderWithMessage() {
    assertThat(new Object[0]).withFailMessage("foo").containsExactlyInAnyOrder(new Object[0]);
  }

  void testAssertEqualIteratorIterationOrder() {
    assertThat(Iterators.unmodifiableIterator(new ArrayList<>().iterator()))
        .toIterable()
        .containsExactlyElementsOf(
            copyOf(Iterators.unmodifiableIterator(new ArrayList<>().iterator())));
  }

  void testAssertEqualIteratorIterationOrderWithMessage() {
    assertThat(Iterators.unmodifiableIterator(new ArrayList<>().iterator()))
        .toIterable()
        .withFailMessage("foo")
        .containsExactlyElementsOf(
            copyOf(Iterators.unmodifiableIterator(new ArrayList<>().iterator())));
  }

  void testAssertEqualIterableIterationOrder() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<>()))
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<>()));
    assertThat(Collections.synchronizedCollection(new ArrayList<>()))
        .containsExactlyElementsOf(Collections.synchronizedCollection(new ArrayList<>()));
  }

  void testAssertEqualIterableIterationOrderWithMessage() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<>()))
        .withFailMessage("foo")
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<>()));
    assertThat(Collections.synchronizedCollection(new ArrayList<>()))
        .withFailMessage("bar")
        .containsExactlyElementsOf(Collections.synchronizedCollection(new ArrayList<>()));
  }

  void testAssertEqualSets() {
    assertThat(ImmutableSet.of()).hasSameElementsAs(ImmutableSet.of());
  }

  void testAssertEqualSetsWithMessage() {
    assertThat(ImmutableSet.of()).withFailMessage("foo").hasSameElementsAs(ImmutableSet.of());
  }

  void testAssertUnequal() {
    assertThat(true).isNotEqualTo(true);
    assertThat((byte) 0).isNotEqualTo((byte) 0);
    assertThat((char) 0).isNotEqualTo((char) 0);
    assertThat((short) 0).isNotEqualTo((short) 0);
    assertThat(0).isNotEqualTo(0);
    assertThat(0L).isNotEqualTo(0L);
    assertThat(0.0F).isNotEqualTo(0.0F);
    assertThat(0.0).isNotEqualTo(0.0);
    assertThat(new Object()).isNotEqualTo(new Object());
    assertThat("actual").isNotEqualTo("expected");
    assertThat(ImmutableSet.of()).isNotEqualTo(ImmutableSet.of());
    assertThat(ImmutableMap.of()).isNotEqualTo(ImmutableMap.of());
  }

  void testAssertUnequalWithMessage() {
    assertThat(true).withFailMessage("foo").isNotEqualTo(true);
    assertThat((byte) 0).withFailMessage("bar").isNotEqualTo((byte) 0);
    assertThat((char) 0).withFailMessage("baz").isNotEqualTo((char) 0);
    assertThat((short) 0).withFailMessage("qux").isNotEqualTo((short) 0);
    assertThat(0).withFailMessage("quux").isNotEqualTo(0);
    assertThat(0L).withFailMessage("quuz").isNotEqualTo(0L);
    assertThat(0.0F).withFailMessage("corge").isNotEqualTo(0.0F);
    assertThat(0.0).withFailMessage("grault").isNotEqualTo(0.0);
    assertThat(new Object()).withFailMessage("garply").isNotEqualTo(new Object());
    assertThat("actual").withFailMessage("waldo").isNotEqualTo("expected");
    assertThat(ImmutableSet.of()).withFailMessage("fred").isNotEqualTo(ImmutableSet.of());
    assertThat(ImmutableMap.of()).withFailMessage("plugh").isNotEqualTo(ImmutableMap.of());
  }

  void testAssertUnequalFloatsWithDelta() {
    assertThat(0.0F).isNotCloseTo(0.0F, offset(0.0F));
  }

  void testAssertUnequalFloatsWithDeltaWithMessage() {
    assertThat(0.0F).withFailMessage("foo").isNotCloseTo(0.0F, offset(0.0F));
  }

  void testAssertUnequalDoublesWithDelta() {
    assertThat(0.0).isNotCloseTo(0.0, offset(0.0));
  }

  void testAssertUnequalDoublesWithDeltaWithMessage() {
    assertThat(0.0).withFailMessage("foo").isNotCloseTo(0.0, offset(0.0));
  }

  void testAssertThrows() {
    assertThatThrownBy(() -> {});
  }

  void testAssertThrowsWithType() {
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }
}
