package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.copyOf;
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
    fail();
  }

  void testFailWithString() {
    fail("foo");
  }

  void testFailWithStringAndThrowable() {
    fail("foo", new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertThat(true).isTrue();
  }

  void testAssertThatWithFailMessageIsTrue() {
    assertThat(true).withFailMessage("foo").isTrue();
  }

  void testAssertThatIsFalse() {
    assertThat(true).isFalse();
  }

  void testAssertThatWithFailMessageIsFalse() {
    assertThat(true).withFailMessage("foo").isFalse();
  }

  void testAssertThatIsNull() {
    assertThat(new Object()).isNull();
  }

  void testAssertThatWithFailMessageIsNull() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  void testAssertThatIsNotNull() {
    assertThat(new Object()).isNotNull();
  }

  void testAssertThatWithFailMessageIsNotNull() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  void testAssertThatIsSameAs() {
    assertThat(new Object()).isSameAs(new StringBuilder());
  }

  void testAssertThatWithFailMessageIsSameAs() {
    assertThat(new Object()).withFailMessage("foo").isSameAs(new StringBuilder());
  }

  void testAssertThatIsNotSameAs() {
    assertThat(new Object()).isNotSameAs(new StringBuilder());
  }

  void testAssertThatWithFailMessageIsNotSameAs() {
    assertThat(new Object()).withFailMessage("foo").isNotSameAs(new StringBuilder());
  }

  void testAssertThatIsEqualTo() {
    assertThat(true).isEqualTo(false);
    assertThat(true).isEqualTo(Boolean.FALSE);
    assertThat(Boolean.TRUE).isEqualTo(false);
    assertThat(Boolean.TRUE).isEqualTo(Boolean.FALSE);
    assertThat((byte) 0).isEqualTo((byte) 1);
    assertThat((byte) 0).isEqualTo(Byte.decode("1"));
    assertThat(Byte.decode("0")).isEqualTo((byte) 1);
    assertThat(Byte.decode("0")).isEqualTo(Byte.decode("1"));
    assertThat('a').isEqualTo('b');
    assertThat('a').isEqualTo(Character.valueOf('b'));
    assertThat(Character.valueOf('a')).isEqualTo('b');
    assertThat(Character.valueOf('a')).isEqualTo(Character.valueOf('b'));
    assertThat((short) 0).isEqualTo((short) 1);
    assertThat((short) 0).isEqualTo(Short.decode("1"));
    assertThat(Short.decode("0")).isEqualTo((short) 1);
    assertThat(Short.decode("0")).isEqualTo(Short.decode("1"));
    assertThat(0).isEqualTo(1);
    assertThat(0).isEqualTo(Integer.valueOf(1));
    assertThat(Integer.valueOf(0)).isEqualTo(1);
    assertThat(Integer.valueOf(0)).isEqualTo(Integer.valueOf(1));
    assertThat(0L).isEqualTo(1L);
    assertThat(0L).isEqualTo(Long.valueOf(1));
    assertThat(Long.valueOf(0)).isEqualTo(1L);
    assertThat(Long.valueOf(0)).isEqualTo(Long.valueOf(1));
    assertThat(0.0f).isEqualTo(1.0f);
    assertThat(0.0f).isEqualTo(Float.valueOf(1.0f));
    assertThat(Float.valueOf(0.0f)).isEqualTo(1.0f);
    assertThat(Float.valueOf(0.0f)).isEqualTo(Float.valueOf(1.0f));
    assertThat(0.0).isEqualTo(1.0);
    assertThat(0.0).isEqualTo(Double.valueOf(1.0));
    assertThat(Double.valueOf(0.0)).isEqualTo(1.0);
    assertThat(Double.valueOf(0.0)).isEqualTo(Double.valueOf(1.0));
    assertThat(new Object()).isEqualTo(new StringBuilder());
    assertThat("actual").isEqualTo("expected");
    assertThat(ImmutableMap.of()).isEqualTo(ImmutableMap.of(1, 2));
  }

  void testAssertThatWithFailMessageIsEqualTo() {
    assertThat(true).withFailMessage("foo").isEqualTo(false);
    assertThat(true).withFailMessage("bar").isEqualTo(Boolean.FALSE);
    assertThat(Boolean.TRUE).withFailMessage("baz").isEqualTo(false);
    assertThat(Boolean.TRUE).withFailMessage("qux").isEqualTo(Boolean.FALSE);
    assertThat((byte) 0).withFailMessage("quux").isEqualTo((byte) 1);
    assertThat((byte) 0).withFailMessage("corge").isEqualTo(Byte.decode("1"));
    assertThat(Byte.decode("0")).withFailMessage("grault").isEqualTo((byte) 1);
    assertThat(Byte.decode("0")).withFailMessage("garply").isEqualTo(Byte.decode("1"));
    assertThat('a').withFailMessage("waldo").isEqualTo('b');
    assertThat('a').withFailMessage("fred").isEqualTo(Character.valueOf('b'));
    assertThat(Character.valueOf('a')).withFailMessage("plugh").isEqualTo('b');
    assertThat(Character.valueOf('a')).withFailMessage("xyzzy").isEqualTo(Character.valueOf('b'));
    assertThat((short) 0).withFailMessage("thud").isEqualTo((short) 1);
    assertThat((short) 0).withFailMessage("foo").isEqualTo(Short.decode("1"));
    assertThat(Short.decode("0")).withFailMessage("bar").isEqualTo((short) 1);
    assertThat(Short.decode("0")).withFailMessage("baz").isEqualTo(Short.decode("1"));
    assertThat(0).withFailMessage("qux").isEqualTo(1);
    assertThat(0).withFailMessage("quux").isEqualTo(Integer.valueOf(1));
    assertThat(Integer.valueOf(0)).withFailMessage("corge").isEqualTo(1);
    assertThat(Integer.valueOf(0)).withFailMessage("grault").isEqualTo(Integer.valueOf(1));
    assertThat(0L).withFailMessage("garply").isEqualTo(1L);
    assertThat(0L).withFailMessage("waldo").isEqualTo(Long.valueOf(1));
    assertThat(Long.valueOf(0)).withFailMessage("fred").isEqualTo(1L);
    assertThat(Long.valueOf(0)).withFailMessage("plugh").isEqualTo(Long.valueOf(1));
    assertThat(0.0f).withFailMessage("xyzzy").isEqualTo(1.0f);
    assertThat(0.0f).withFailMessage("thud").isEqualTo(Float.valueOf(1.0f));
    assertThat(Float.valueOf(0.0f)).withFailMessage("foo").isEqualTo(1.0f);
    assertThat(Float.valueOf(0.0f)).withFailMessage("bar").isEqualTo(Float.valueOf(1.0f));
    assertThat(0.0).withFailMessage("baz").isEqualTo(1.0);
    assertThat(0.0).withFailMessage("qux").isEqualTo(Double.valueOf(1.0));
    assertThat(Double.valueOf(0.0)).withFailMessage("quux").isEqualTo(1.0);
    assertThat(Double.valueOf(0.0)).withFailMessage("corge").isEqualTo(Double.valueOf(1.0));
    assertThat(new Object()).withFailMessage("grault").isEqualTo(new StringBuilder());
    assertThat("actual").withFailMessage("garply").isEqualTo("expected");
    assertThat(ImmutableMap.of()).withFailMessage("waldo").isEqualTo(ImmutableMap.of(1, 2));
  }

  void testAssertThatFloatIsCloseToOffset() {
    assertThat(1.0f).isCloseTo(2.0f, offset(0.0f));
  }

  void testAssertThatFloatWithFailMessageIsCloseToOffset() {
    assertThat(1.0f).withFailMessage("foo").isCloseTo(2.0f, offset(0.0f));
  }

  void testAssertThatDoubleIsCloseToOffset() {
    assertThat(1.0).isCloseTo(2.0, offset(0.0));
  }

  void testAssertThatDoubleWithFailMessageIsCloseToOffset() {
    assertThat(1.0).withFailMessage("foo").isCloseTo(2.0, offset(0.0));
  }

  void testAssertThatArrayContainsExactly() {
    assertThat(new boolean[] {false}).containsExactly(new boolean[] {true});
    assertThat(new byte[] {2}).containsExactly(new byte[] {1});
    assertThat(new char[] {'b'}).containsExactly(new char[] {'a'});
    assertThat(new short[] {2}).containsExactly(new short[] {1});
    assertThat(new int[] {2}).containsExactly(new int[] {1});
    assertThat(new long[] {2L}).containsExactly(new long[] {1L});
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f});
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0});
    assertThat(new Object[] {"bar"}).containsExactly(new Object[] {"foo"});
  }

  void testAssertThatArrayWithFailMessageContainsExactly() {
    assertThat(new boolean[] {false}).withFailMessage("foo").containsExactly(new boolean[] {true});
    assertThat(new byte[] {2}).withFailMessage("bar").containsExactly(new byte[] {1});
    assertThat(new char[] {'b'}).withFailMessage("baz").containsExactly(new char[] {'a'});
    assertThat(new short[] {2}).withFailMessage("qux").containsExactly(new short[] {1});
    assertThat(new int[] {2}).withFailMessage("quux").containsExactly(new int[] {1});
    assertThat(new long[] {2L}).withFailMessage("corge").containsExactly(new long[] {1L});
    assertThat(new float[] {2.0f}).withFailMessage("grault").containsExactly(new float[] {1.0f});
    assertThat(new double[] {2.0}).withFailMessage("garply").containsExactly(new double[] {1.0});
    assertThat(new Object[] {"bar"}).withFailMessage("waldo").containsExactly(new Object[] {"foo"});
  }

  void testAssertThatFloatArrayContainsExactlyOffset() {
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f}, offset(0.0f));
  }

  void testAssertThatFloatArrayWithFailMessageContainsExactlyOffset() {
    assertThat(new float[] {2.0f})
        .withFailMessage("foo")
        .containsExactly(new float[] {1.0f}, offset(0.0f));
  }

  void testAssertThatDoubleArrayContainsExactlyOffset() {
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0}, offset(0.0));
  }

  void testAssertThatDoubleArrayWithFailMessageContainsExactlyOffset() {
    assertThat(new double[] {2.0})
        .withFailMessage("foo")
        .containsExactly(new double[] {1.0}, offset(0.0));
  }

  void testAssertThatArrayContainsExactlyInAnyOrder() {
    assertThat(new Object[] {"bar"}).containsExactlyInAnyOrder(new Object[] {"foo"});
  }

  void testAssertThatArrayWithFailMessageContainsExactlyInAnyOrder() {
    assertThat(new Object[] {"bar"})
        .withFailMessage("foo")
        .containsExactlyInAnyOrder(new Object[] {"foo"});
  }

  void testAssertThatIteratorToIterableContainsExactlyElementsOfImmutableListCopyOf() {
    assertThat(new ArrayList<Number>().iterator())
        .toIterable()
        .containsExactlyElementsOf(copyOf(new ArrayList<Integer>().iterator()));
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator());
  }

  void
      testAssertThatIteratorToIterableWithFailMessageContainsExactlyElementsOfImmutableListCopyOf() {
    assertThat(new ArrayList<Number>().iterator())
        .toIterable()
        .withFailMessage("foo")
        .containsExactlyElementsOf(copyOf(new ArrayList<Integer>().iterator()));
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator(), "bar");
  }

  void testAssertThatIterableContainsExactlyElementsOf() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<Number>()))
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<Integer>()));
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<String>()));
    assertThat(new ArrayList<Number>()).containsExactlyElementsOf(new ArrayList<Integer>());
    assertEquals(new ArrayList<Number>(), new ArrayList<String>());
  }

  void testAssertThatIterableWithFailMessageContainsExactlyElementsOf() {
    assertThat(Iterables.unmodifiableIterable(new ArrayList<Number>()))
        .withFailMessage("foo")
        .containsExactlyElementsOf(Iterables.unmodifiableIterable(new ArrayList<Integer>()));
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<String>()),
        "bar");
    assertThat(new ArrayList<Number>())
        .withFailMessage("baz")
        .containsExactlyElementsOf(new ArrayList<Integer>());
    assertEquals(new ArrayList<Number>(), new ArrayList<String>(), "qux");
  }

  void testAssertThatSetHasSameElementsAs() {
    assertThat(ImmutableSet.<Number>of()).hasSameElementsAs(ImmutableSet.<Integer>of());
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of());
  }

  void testAssertThatSetWithFailMessageHasSameElementsAs() {
    assertThat(ImmutableSet.<Number>of())
        .withFailMessage("foo")
        .hasSameElementsAs(ImmutableSet.<Integer>of());
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of(), "bar");
  }

  void testAssertThatIsNotEqualTo() {
    assertThat(true).isNotEqualTo(false);
    assertThat((byte) 0).isNotEqualTo((byte) 1);
    assertThat('a').isNotEqualTo('b');
    assertThat((short) 0).isNotEqualTo((short) 1);
    assertThat(0).isNotEqualTo(1);
    assertThat(0L).isNotEqualTo(1L);
    assertThat(0.0f).isNotEqualTo(1.0f);
    assertThat(0.0).isNotEqualTo(1.0);
    assertThat(new Object()).isNotEqualTo(new StringBuilder());
    assertThat("foo").isNotEqualTo("bar");
    assertThat(ImmutableSet.of()).isNotEqualTo(ImmutableSet.of(1));
    assertThat(ImmutableMap.of()).isNotEqualTo(ImmutableMap.of(1, 2));
  }

  void testAssertThatWithFailMessageIsNotEqualTo() {
    assertThat(true).withFailMessage("foo").isNotEqualTo(false);
    assertThat((byte) 0).withFailMessage("bar").isNotEqualTo((byte) 1);
    assertThat('a').withFailMessage("baz").isNotEqualTo('b');
    assertThat((short) 0).withFailMessage("qux").isNotEqualTo((short) 1);
    assertThat(0).withFailMessage("quux").isNotEqualTo(1);
    assertThat(0L).withFailMessage("corge").isNotEqualTo(1L);
    assertThat(0.0f).withFailMessage("grault").isNotEqualTo(1.0f);
    assertThat(0.0).withFailMessage("garply").isNotEqualTo(1.0);
    assertThat(new Object()).withFailMessage("waldo").isNotEqualTo(new StringBuilder());
    assertThat("foo").withFailMessage("fred").isNotEqualTo("bar");
    assertThat(ImmutableSet.of()).withFailMessage("plugh").isNotEqualTo(ImmutableSet.of(1));
    assertThat(ImmutableMap.of()).withFailMessage("xyzzy").isNotEqualTo(ImmutableMap.of(1, 2));
  }

  void testAssertThatFloatIsNotCloseToOffset() {
    assertThat(1.0f).isNotCloseTo(2.0f, offset(0.0f));
  }

  void testAssertThatFloatWithFailMessageIsNotCloseToOffset() {
    assertThat(1.0f).withFailMessage("foo").isNotCloseTo(2.0f, offset(0.0f));
  }

  void testAssertThatDoubleIsNotCloseToOffset() {
    assertThat(1.0).isNotCloseTo(2.0, offset(0.0));
  }

  void testAssertThatDoubleWithFailMessageIsNotCloseToOffset() {
    assertThat(1.0).withFailMessage("foo").isNotCloseTo(2.0, offset(0.0));
  }

  void testAssertThatThrownBy() {
    assertThatThrownBy(() -> {});
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }
}
