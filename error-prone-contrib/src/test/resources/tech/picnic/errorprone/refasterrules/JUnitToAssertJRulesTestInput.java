package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitToAssertJRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Assertions.class,
        assertDoesNotThrow(() -> null),
        assertInstanceOf(null, null),
        assertThrows(null, null),
        assertThrowsExactly(null, null),
        offset(0.0),
        (Runnable) () -> assertArrayEquals((int[]) null, null),
        (Runnable) () -> assertFalse(true),
        (Runnable) () -> assertNotNull(null),
        (Runnable) () -> assertNotSame(null, null),
        (Runnable) () -> assertNull(null),
        (Runnable) () -> assertSame(null, null),
        (Runnable) () -> assertTrue(true));
  }

  void testAssertThatContainsExactlyBoolean() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false});
  }

  void testAssertThatWithFailMessageContainsExactlyBooleanString() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyBooleanSupplier() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false}, () -> "foo");
  }

  void testAssertThatContainsExactlyByte() {
    assertArrayEquals(new byte[] {1}, new byte[] {2});
  }

  void testAssertThatWithFailMessageContainsExactlyByteString() {
    assertArrayEquals(new byte[] {1}, new byte[] {2}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyByteSupplier() {
    assertArrayEquals(new byte[] {1}, new byte[] {2}, () -> "foo");
  }

  void testAssertThatContainsExactlyChar() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'});
  }

  void testAssertThatWithFailMessageContainsExactlyCharString() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyCharSupplier() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'}, () -> "foo");
  }

  void testAssertThatContainsExactlyShort() {
    assertArrayEquals(new short[] {1}, new short[] {2});
  }

  void testAssertThatWithFailMessageContainsExactlyShortString() {
    assertArrayEquals(new short[] {1}, new short[] {2}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyShortSupplier() {
    assertArrayEquals(new short[] {1}, new short[] {2}, () -> "foo");
  }

  void testAssertThatContainsExactlyInt() {
    assertArrayEquals(new int[] {1}, new int[] {2});
  }

  void testAssertThatWithFailMessageContainsExactlyIntString() {
    assertArrayEquals(new int[] {1}, new int[] {2}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyIntSupplier() {
    assertArrayEquals(new int[] {1}, new int[] {2}, () -> "foo");
  }

  void testAssertThatContainsExactlyLong() {
    assertArrayEquals(new long[] {1L}, new long[] {2L});
  }

  void testAssertThatWithFailMessageContainsExactlyLongString() {
    assertArrayEquals(new long[] {1L}, new long[] {2L}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyLongSupplier() {
    assertArrayEquals(new long[] {1L}, new long[] {2L}, () -> "foo");
  }

  void testAssertThatContainsExactlyFloat() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f});
  }

  void testAssertThatWithFailMessageContainsExactlyFloatString() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyFloatSupplier() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f}, () -> "foo");
  }

  void testAssertThatContainsExactlyOffsetFloat() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f}, 0.1f);
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetFloatString() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f}, 0.1f, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetFloatSupplier() {
    assertArrayEquals(new float[] {1.0f}, new float[] {2.0f}, 0.1f, () -> "foo");
  }

  void testAssertThatContainsExactlyDouble() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0});
  }

  void testAssertThatWithFailMessageContainsExactlyDoubleString() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyDoubleSupplier() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, () -> "foo");
  }

  void testAssertThatContainsExactlyOffsetDouble() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1);
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetDoubleString() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetDoubleSupplier() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1, () -> "foo");
  }

  void testAssertThatContainsExactlyObject() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"});
  }

  void testAssertThatWithFailMessageContainsExactlyObjectString() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"}, "foo");
  }

  void testAssertThatWithFailMessageContainsExactlyObjectSupplier() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"}, () -> "foo");
  }

  Object testFail() {
    return Assertions.fail();
  }

  Object testFailWithString() {
    return Assertions.fail("foo");
  }

  Object testFailWithStringAndThrowable() {
    return Assertions.fail("foo", new IllegalStateException());
  }

  Object testFailWithThrowable() {
    return Assertions.fail(new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertTrue(true);
  }

  void testAssertThatWithFailMessageIsTrueString() {
    assertTrue(true, "foo");
  }

  void testAssertThatWithFailMessageIsTrueSupplier() {
    assertTrue(true, () -> "foo");
  }

  void testAssertThatIsFalse() {
    assertFalse(true);
  }

  void testAssertThatWithFailMessageIsFalseString() {
    assertFalse(true, "foo");
  }

  void testAssertThatWithFailMessageIsFalseSupplier() {
    assertFalse(true, () -> "foo");
  }

  void testAssertThatIsNull() {
    assertNull(new Object());
  }

  void testAssertThatWithFailMessageIsNullString() {
    assertNull(new Object(), "foo");
  }

  void testAssertThatWithFailMessageIsNullSupplier() {
    assertNull(new Object(), () -> "foo");
  }

  void testAssertThatIsNotNull() {
    assertNotNull(new Object());
  }

  void testAssertThatWithFailMessageIsNotNullString() {
    assertNotNull(new Object(), "foo");
  }

  void testAssertThatWithFailMessageIsNotNullSupplier() {
    assertNotNull(new Object(), () -> "foo");
  }

  void testAssertThatIsSameAs() {
    assertSame("foo", "bar");
  }

  void testAssertThatWithFailMessageIsSameAsString() {
    assertSame("foo", "bar", "baz");
  }

  void testAssertThatWithFailMessageIsSameAsSupplier() {
    assertSame("foo", "bar", () -> "baz");
  }

  void testAssertThatIsNotSameAs() {
    assertNotSame("foo", "bar");
  }

  void testAssertThatWithFailMessageIsNotSameAsString() {
    assertNotSame("foo", "bar", "baz");
  }

  void testAssertThatWithFailMessageIsNotSameAsSupplier() {
    assertNotSame("foo", "bar", () -> "baz");
  }

  void testAssertThatThrownByIsExactlyInstanceOf() {
    assertThrowsExactly(IllegalStateException.class, () -> {});
  }

  void testAssertThatThrownByWithFailMessageIsExactlyInstanceOfString() {
    assertThrowsExactly(IllegalStateException.class, () -> {}, "foo");
  }

  void testAssertThatThrownByWithFailMessageIsExactlyInstanceOfSupplier() {
    assertThrowsExactly(IllegalStateException.class, () -> {}, () -> "foo");
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThrows(IllegalStateException.class, () -> {});
  }

  void testAssertThatThrownByWithFailMessageIsInstanceOfString() {
    assertThrows(IllegalStateException.class, () -> {}, "foo");
  }

  void testAssertThatThrownByWithFailMessageIsInstanceOfSupplier() {
    assertThrows(IllegalStateException.class, () -> {}, () -> "foo");
  }

  void testAssertThatCodeDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {});
    assertDoesNotThrow(() -> toString());
  }

  void testAssertThatCodeWithFailMessageDoesNotThrowAnyExceptionString() {
    assertDoesNotThrow(() -> {}, "foo");
    assertDoesNotThrow(() -> toString(), "bar");
  }

  void testAssertThatCodeWithFailMessageDoesNotThrowAnyExceptionSupplier() {
    assertDoesNotThrow(() -> {}, () -> "foo");
    assertDoesNotThrow(() -> toString(), () -> "bar");
  }

  void testAssertThatIsInstanceOf() {
    assertInstanceOf(Object.class, new Object());
  }

  void testAssertThatWithFailMessageIsInstanceOfString() {
    assertInstanceOf(Object.class, new Object(), "foo");
  }

  void testAssertThatWithFailMessageIsInstanceOfSupplier() {
    assertInstanceOf(Object.class, new Object(), () -> "foo");
  }
}
