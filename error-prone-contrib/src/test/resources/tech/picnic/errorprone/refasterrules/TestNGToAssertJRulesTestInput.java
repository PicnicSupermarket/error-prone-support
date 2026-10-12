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
import org.testng.Assert.ThrowingRunnable;
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

  void testFailWithString() {
    org.testng.Assert.fail("foo");
  }

  void testFailWithStringAndThrowable() {
    org.testng.Assert.fail("foo", new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertTrue(true);
  }

  void testAssertThatWithFailMessageIsTrue() {
    assertTrue(true, "foo");
  }

  void testAssertThatIsFalse() {
    assertFalse(true);
  }

  void testAssertThatWithFailMessageIsFalse() {
    assertFalse(true, "foo");
  }

  void testAssertThatIsNull() {
    assertNull(new Object());
  }

  void testAssertThatWithFailMessageIsNull() {
    assertNull(new Object(), "foo");
  }

  void testAssertThatIsNotNull() {
    assertNotNull(new Object());
  }

  void testAssertThatWithFailMessageIsNotNull() {
    assertNotNull(new Object(), "foo");
  }

  void testAssertThatIsSameAs() {
    assertSame(new Object(), new StringBuilder());
  }

  void testAssertThatWithFailMessageIsSameAs() {
    assertSame(new Object(), new StringBuilder(), "foo");
  }

  void testAssertThatIsNotSameAs() {
    assertNotSame(new Object(), new StringBuilder());
  }

  void testAssertThatWithFailMessageIsNotSameAs() {
    assertNotSame(new Object(), new StringBuilder(), "foo");
  }

  void testAssertThatIsEqualTo() {
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

  void testAssertThatWithFailMessageIsEqualTo() {
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

  void testAssertThatIsCloseToOffsetFloat() {
    assertEquals(1.0f, 2.0f, 0.0f);
  }

  void testAssertThatWithFailMessageIsCloseToOffsetFloat() {
    assertEquals(1.0f, 2.0f, 0.0f, "foo");
  }

  void testAssertThatIsCloseToOffsetDouble() {
    assertEquals(1.0, 2.0, 0.0);
  }

  void testAssertThatWithFailMessageIsCloseToOffsetDouble() {
    assertEquals(1.0, 2.0, 0.0, "foo");
  }

  void testAssertThatContainsExactly() {
    assertEquals(new boolean[] {false}, new boolean[] {true});
    assertEquals(new byte[] {2}, new byte[] {1});
    assertEquals(new char[] {'b'}, new char[] {'a'});
    assertEquals(new short[] {2}, new short[] {1});
    assertEquals(new int[] {2}, new int[] {1});
    assertEquals(new long[] {2L}, new long[] {1L});
    assertEquals(new float[] {2.0f}, new float[] {1.0f});
    assertEquals(new double[] {2.0}, new double[] {1.0});
    assertEquals(new Object[] {"bar"}, new Object[] {"foo"});
  }

  void testAssertThatWithFailMessageContainsExactly() {
    assertEquals(new boolean[] {false}, new boolean[] {true}, "foo");
    assertEquals(new byte[] {2}, new byte[] {1}, "bar");
    assertEquals(new char[] {'b'}, new char[] {'a'}, "baz");
    assertEquals(new short[] {2}, new short[] {1}, "qux");
    assertEquals(new int[] {2}, new int[] {1}, "quux");
    assertEquals(new long[] {2L}, new long[] {1L}, "corge");
    assertEquals(new float[] {2.0f}, new float[] {1.0f}, "grault");
    assertEquals(new double[] {2.0}, new double[] {1.0}, "garply");
    assertEquals(new Object[] {"bar"}, new Object[] {"foo"}, "waldo");
  }

  void testAssertThatContainsExactlyOffsetFloat() {
    assertEquals(new float[] {2.0f}, new float[] {1.0f}, 0.0f);
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetFloat() {
    assertEquals(new float[] {2.0f}, new float[] {1.0f}, 0.0f, "foo");
  }

  void testAssertThatContainsExactlyOffsetDouble() {
    assertEquals(new double[] {2.0}, new double[] {1.0}, 0.0);
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetDouble() {
    assertEquals(new double[] {2.0}, new double[] {1.0}, 0.0, "foo");
  }

  void testAssertThatContainsExactlyInAnyOrder() {
    assertEqualsNoOrder(new Object[] {"bar"}, new Object[] {"foo"});
  }

  void testAssertThatWithFailMessageContainsExactlyInAnyOrder() {
    assertEqualsNoOrder(new Object[] {"bar"}, new Object[] {"foo"}, "foo");
  }

  void testAssertThatToIterableContainsExactlyElementsOfImmutableListCopyOf() {
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<Integer>().iterator());
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator());
  }

  void testAssertThatToIterableWithFailMessageContainsExactlyElementsOfImmutableListCopyOf() {
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<Integer>().iterator(), "foo");
    assertEquals(new ArrayList<Number>().iterator(), new ArrayList<String>().iterator(), "bar");
  }

  void testAssertThatContainsExactlyElementsOf() {
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<Integer>()));
    assertEquals(
        Iterables.unmodifiableIterable(new ArrayList<Number>()),
        Iterables.unmodifiableIterable(new ArrayList<String>()));
    assertEquals(new ArrayList<Number>(), new ArrayList<Integer>());
    assertEquals(new ArrayList<Number>(), new ArrayList<String>());
  }

  void testAssertThatWithFailMessageContainsExactlyElementsOf() {
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

  void testAssertThatHasSameElementsAs() {
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<Integer>of());
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of());
  }

  void testAssertThatWithFailMessageHasSameElementsAs() {
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<Integer>of(), "foo");
    assertEquals(ImmutableSet.<Number>of(), ImmutableSet.<String>of(), "bar");
  }

  void testAssertThatIsNotEqualTo() {
    assertNotEquals(true, false);
    assertNotEquals((byte) 0, (byte) 1);
    assertNotEquals('a', 'b');
    assertNotEquals((short) 0, (short) 1);
    assertNotEquals(0, 1);
    assertNotEquals(0L, 1L);
    assertNotEquals(0.0f, 1.0f);
    assertNotEquals(0.0, 1.0);
    assertNotEquals(new Object(), new StringBuilder());
    assertNotEquals("foo", "bar");
    assertNotEquals(ImmutableSet.of(), ImmutableSet.of(1));
    assertNotEquals(ImmutableMap.of(), ImmutableMap.of(1, 2));
  }

  void testAssertThatWithFailMessageIsNotEqualTo() {
    assertNotEquals(true, false, "foo");
    assertNotEquals((byte) 0, (byte) 1, "bar");
    assertNotEquals('a', 'b', "baz");
    assertNotEquals((short) 0, (short) 1, "qux");
    assertNotEquals(0, 1, "quux");
    assertNotEquals(0L, 1L, "corge");
    assertNotEquals(0.0f, 1.0f, "grault");
    assertNotEquals(0.0, 1.0, "garply");
    assertNotEquals(new Object(), new StringBuilder(), "waldo");
    assertNotEquals("foo", "bar", "fred");
    assertNotEquals(ImmutableSet.of(), ImmutableSet.of(1), "plugh");
    assertNotEquals(ImmutableMap.of(), ImmutableMap.of(1, 2), "xyzzy");
  }

  void testAssertThatIsNotCloseToOffsetFloat() {
    assertNotEquals(1.0f, 2.0f, 0.0f);
  }

  void testAssertThatWithFailMessageIsNotCloseToOffsetFloat() {
    assertNotEquals(1.0f, 2.0f, 0.0f, "foo");
  }

  void testAssertThatIsNotCloseToOffsetDouble() {
    assertNotEquals(1.0, 2.0, 0.0);
  }

  void testAssertThatWithFailMessageIsNotCloseToOffsetDouble() {
    assertNotEquals(1.0, 2.0, 0.0, "foo");
  }

  void testAssertThatThrownBy() {
    assertThrows((ThrowingRunnable) null);
    assertThrows(() -> {});
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThrows(IllegalStateException.class, (ThrowingRunnable) null);
    assertThrows(IllegalStateException.class, () -> {});
  }
}
