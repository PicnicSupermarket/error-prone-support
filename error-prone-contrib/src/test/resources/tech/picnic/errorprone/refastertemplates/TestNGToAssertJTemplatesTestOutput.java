package tech.picnic.errorprone.refastertemplates;

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
    throw new AssertionError();
  }

  @Template(FailWithMessage.class)
  void testFailWithMessage() {
    fail("foo");
  }

  @Template(FailWithMessageAndThrowable.class)
  void testFailWithMessageAndThrowable() {
    fail("foo", new IllegalStateException());
  }

  @Template(AssertTrue.class)
  void testAssertTrue() {
    assertThat(true).isTrue();
  }

  @Template(AssertTrueWithMessage.class)
  void testAssertTrueWithMessage() {
    assertThat(true).withFailMessage("foo").isTrue();
  }

  @Template(AssertFalse.class)
  void testAssertFalse() {
    assertThat(true).isFalse();
  }

  @Template(AssertFalseWithMessage.class)
  void testAssertFalseWithMessage() {
    assertThat(true).withFailMessage("message").isFalse();
  }

  @Template(AssertNull.class)
  void testAssertNull() {
    assertThat(new Object()).isNull();
  }

  @Template(AssertNullWithMessage.class)
  void testAssertNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  @Template(AssertNotNull.class)
  void testAssertNotNull() {
    assertThat(new Object()).isNotNull();
  }

  @Template(AssertNotNullWithMessage.class)
  void testAssertNotNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  @Template(AssertSame.class)
  void testAssertSame() {
    assertThat(new Object()).isSameAs(new Object());
  }

  @Template(AssertSameWithMessage.class)
  void testAssertSameWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isSameAs(new Object());
  }

  @Template(AssertNotSame.class)
  void testAssertNotSame() {
    assertThat(new Object()).isNotSameAs(new Object());
  }

  @Template(AssertNotSameWithMessage.class)
  void testAssertNotSameWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNotSameAs(new Object());
  }

  @Template(AssertEqual.class)
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
    assertThat(ImmutableSet.of()).hasSameElementsAs(ImmutableSet.of());
    assertThat(ImmutableMap.of()).isEqualTo(ImmutableMap.of());
  }

  @Template(AssertEqualWithMessage.class)
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

  @Template(AssertEqualFloatsWithDelta.class)
  void testAssertEqualFloatsWithDelta() {
    assertThat(0.0F).isCloseTo(0.0F, offset(0.0F));
  }

  @Template(AssertEqualFloatsWithDeltaWithMessage.class)
  void testAssertEqualFloatsWithDeltaWithMessage() {
    assertThat(0.0F).withFailMessage("foo").isCloseTo(0.0F, offset(0.0F));
  }

  @Template(AssertEqualDoublesWithDelta.class)
  void testAssertEqualDoublesWithDelta() {
    assertThat(0.0).isCloseTo(0.0, offset(0.0));
  }

  @Template(AssertEqualDoublesWithDeltaWithMessage.class)
  void testAssertEqualDoublesWithDeltaWithMessage() {
    assertThat(0.0).withFailMessage("foo").isCloseTo(0.0, offset(0.0));
  }

  @Template(AssertEqualArrayIterationOrder.class)
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

  @Template(AssertEqualArrayIterationOrderWithMessage.class)
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

  @Template(AssertEqualArraysIrrespectiveOfOrder.class)
  void testAssertEqualArraysIrrespectiveOfOrder() {
    assertThat(new Object[0]).containsExactlyInAnyOrder(new Object[0]);
  }

  @Template(AssertEqualArraysIrrespectiveOfOrderWithMessage.class)
  void testAssertEqualArraysIrrespectiveOfOrderWithMessage() {
    assertThat(new Object[0]).withFailMessage("foo").containsExactlyInAnyOrder(new Object[0]);
  }

  @Template(AssertEqualIteratorIterationOrder.class)
  void testAssertEqualIteratorIterationOrder() {
    assertThat(Iterators.unmodifiableIterator(new ArrayList<>().iterator()))
        .toIterable()
        .containsExactlyElementsOf(
            copyOf(Iterators.unmodifiableIterator(new ArrayList<>().iterator())));
  }

  @Template(AssertEqualIteratorIterationOrderWithMessage.class)
  void testAssertEqualIteratorIterationOrderWithMessage() {
    assertThat(Iterators.unmodifiableIterator(new ArrayList<>().iterator()))
        .toIterable()
        .withFailMessage("foo")
        .containsExactlyElementsOf(
            copyOf(Iterators.unmodifiableIterator(new ArrayList<>().iterator())));
  }

  @Template(AssertEqualIterableIterationOrder.class)
  void testAssertEqualIterableIterationOrder() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<>()))
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<>()));
    assertThat(Collections.synchronizedCollection(new ArrayList<>()))
        .containsExactlyElementsOf(Collections.synchronizedCollection(new ArrayList<>()));
  }

  @Template(AssertEqualIterableIterationOrderWithMessage.class)
  void testAssertEqualIterableIterationOrderWithMessage() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<>()))
        .withFailMessage("foo")
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<>()));
    assertThat(Collections.synchronizedCollection(new ArrayList<>()))
        .withFailMessage("bar")
        .containsExactlyElementsOf(Collections.synchronizedCollection(new ArrayList<>()));
  }

  @Template(AssertEqualSets.class)
  void testAssertEqualSets() {
    assertThat(ImmutableSet.of()).hasSameElementsAs(ImmutableSet.of());
  }

  @Template(AssertEqualSetsWithMessage.class)
  void testAssertEqualSetsWithMessage() {
    assertThat(ImmutableSet.of()).withFailMessage("foo").hasSameElementsAs(ImmutableSet.of());
  }

  @Template(AssertUnequal.class)
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

  @Template(AssertUnequalWithMessage.class)
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

  @Template(AssertUnequalFloatsWithDelta.class)
  void testAssertUnequalFloatsWithDelta() {
    assertThat(0.0F).isNotCloseTo(0.0F, offset(0.0F));
  }

  @Template(AssertUnequalFloatsWithDeltaWithMessage.class)
  void testAssertUnequalFloatsWithDeltaWithMessage() {
    assertThat(0.0F).withFailMessage("foo").isNotCloseTo(0.0F, offset(0.0F));
  }

  @Template(AssertUnequalDoublesWithDelta.class)
  void testAssertUnequalDoublesWithDelta() {
    assertThat(0.0).isNotCloseTo(0.0, offset(0.0));
  }

  @Template(AssertUnequalDoublesWithDeltaWithMessage.class)
  void testAssertUnequalDoublesWithDeltaWithMessage() {
    assertThat(0.0).withFailMessage("foo").isNotCloseTo(0.0, offset(0.0));
  }

  @Template(AssertThrows.class)
  void testAssertThrows() {
    assertThatThrownBy(() -> {});
  }

  @Template(AssertThrowsWithType.class)
  void testAssertThrowsWithType() {
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }
}
