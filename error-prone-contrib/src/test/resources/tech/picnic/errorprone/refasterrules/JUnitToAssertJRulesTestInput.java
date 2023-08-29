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

  void testAssertThatBooleanArrayContainsExactly() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false});
  }

  void testAssertThatBooleanArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false}, "foo");
  }

  void testAssertThatBooleanArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new boolean[] {true}, new boolean[] {false}, () -> "foo");
  }

  void testAssertThatByteArrayContainsExactly() {
    assertArrayEquals(new byte[] {1}, new byte[] {2});
  }

  void testAssertThatByteArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new byte[] {1}, new byte[] {2}, "foo");
  }

  void testAssertThatByteArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new byte[] {1}, new byte[] {2}, () -> "foo");
  }

  void testAssertThatCharArrayContainsExactly() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'});
  }

  void testAssertThatCharArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'}, "foo");
  }

  void testAssertThatCharArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new char[] {'a'}, new char[] {'b'}, () -> "foo");
  }

  void testAssertThatShortArrayContainsExactly() {
    assertArrayEquals(new short[] {1}, new short[] {2});
  }

  void testAssertThatShortArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new short[] {1}, new short[] {2}, "foo");
  }

  void testAssertThatShortArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new short[] {1}, new short[] {2}, () -> "foo");
  }

  void testAssertThatIntArrayContainsExactly() {
    assertArrayEquals(new int[] {1}, new int[] {2});
  }

  void testAssertThatIntArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new int[] {1}, new int[] {2}, "foo");
  }

  void testAssertThatIntArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new int[] {1}, new int[] {2}, () -> "foo");
  }

  void testAssertThatLongArrayContainsExactly() {
    assertArrayEquals(new long[] {1L}, new long[] {2L});
  }

  void testAssertThatLongArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new long[] {1L}, new long[] {2L}, "foo");
  }

  void testAssertThatLongArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new long[] {1L}, new long[] {2L}, () -> "foo");
  }

  void testAssertThatFloatArrayContainsExactly() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F});
  }

  void testAssertThatFloatArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F}, "foo");
  }

  void testAssertThatFloatArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F}, () -> "foo");
  }

  void testAssertThatFloatArrayContainsExactlyWithOffset() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F}, 0.1f);
  }

  void testAssertThatFloatArrayWithFailMessageContainsExactlyWithOffset() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F}, 0.1f, "foo");
  }

  void testAssertThatFloatArrayWithFailMessageSupplierContainsExactlyWithOffset() {
    assertArrayEquals(new float[] {1.0F}, new float[] {2.0F}, 0.1f, () -> "foo");
  }

  void testAssertThatDoubleArrayContainsExactly() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0});
  }

  void testAssertThatDoubleArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, "foo");
  }

  void testAssertThatDoubleArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, () -> "foo");
  }

  void testAssertThatDoubleArrayContainsExactlyWithOffset() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1);
  }

  void testAssertThatDoubleArrayWithFailMessageContainsExactlyWithOffset() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1, "foo");
  }

  void testAssertThatDoubleArrayWithFailMessageSupplierContainsExactlyWithOffset() {
    assertArrayEquals(new double[] {1.0}, new double[] {2.0}, 0.1, () -> "foo");
  }

  void testAssertThatObjectArrayContainsExactly() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"});
  }

  void testAssertThatObjectArrayWithFailMessageContainsExactly() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"}, "foo");
  }

  void testAssertThatObjectArrayWithFailMessageSupplierContainsExactly() {
    assertArrayEquals(new Object[] {"foo"}, new Object[] {"bar"}, () -> "foo");
  }

  Object testFail() {
    return Assertions.fail();
  }

  Object testFailWithMessage() {
    return Assertions.fail("foo");
  }

  Object testFailWithMessageAndThrowable() {
    return Assertions.fail("foo", new IllegalStateException());
  }

  Object testFailWithThrowable() {
    return Assertions.fail(new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertTrue(true);
  }

  void testAssertThatWithFailMessageStringIsTrue() {
    assertTrue(true, "foo");
  }

  void testAssertThatWithFailMessageSupplierIsTrue() {
    assertTrue(true, () -> "foo");
  }

  void testAssertThatIsFalse() {
    assertFalse(true);
  }

  void testAssertThatWithFailMessageStringIsFalse() {
    assertFalse(true, "foo");
  }

  void testAssertThatWithFailMessageSupplierIsFalse() {
    assertFalse(true, () -> "foo");
  }

  void testAssertThatIsNull() {
    assertNull(new Object());
  }

  void testAssertThatWithFailMessageStringIsNull() {
    assertNull(new Object(), "foo");
  }

  void testAssertThatWithFailMessageSupplierIsNull() {
    assertNull(new Object(), () -> "foo");
  }

  void testAssertThatIsNotNull() {
    assertNotNull(new Object());
  }

  void testAssertThatWithFailMessageStringIsNotNull() {
    assertNotNull(new Object(), "foo");
  }

  void testAssertThatWithFailMessageSupplierIsNotNull() {
    assertNotNull(new Object(), () -> "foo");
  }

  void testAssertThatIsSameAs() {
    assertSame("foo", "bar");
  }

  void testAssertThatWithFailMessageStringIsSameAs() {
    assertSame("foo", "bar", "baz");
  }

  void testAssertThatWithFailMessageSupplierIsSameAs() {
    assertSame("foo", "bar", () -> "baz");
  }

  void testAssertThatIsNotSameAs() {
    assertNotSame("foo", "bar");
  }

  void testAssertThatWithFailMessageStringIsNotSameAs() {
    assertNotSame("foo", "bar", "baz");
  }

  void testAssertThatWithFailMessageSupplierIsNotSameAs() {
    assertNotSame("foo", "bar", () -> "baz");
  }

  void testAssertThatThrownByIsExactlyInstanceOf() {
    assertThrowsExactly(IllegalStateException.class, () -> {});
  }

  void testAssertThatThrownByWithFailMessageStringIsExactlyInstanceOf() {
    assertThrowsExactly(IllegalStateException.class, () -> {}, "foo");
  }

  void testAssertThatThrownByWithFailMessageSupplierIsExactlyInstanceOf() {
    assertThrowsExactly(IllegalStateException.class, () -> {}, () -> "foo");
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThrows(IllegalStateException.class, () -> {});
  }

  void testAssertThatThrownByWithFailMessageStringIsInstanceOf() {
    assertThrows(IllegalStateException.class, () -> {}, "foo");
  }

  void testAssertThatThrownByWithFailMessageSupplierIsInstanceOf() {
    assertThrows(IllegalStateException.class, () -> {}, () -> "foo");
  }

  void testAssertThatCodeDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {});
    assertDoesNotThrow(() -> toString());
  }

  void testAssertThatCodeWithFailMessageStringDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {}, "foo");
    assertDoesNotThrow(() -> toString(), "bar");
  }

  void testAssertThatCodeWithFailMessageSupplierDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {}, () -> "foo");
    assertDoesNotThrow(() -> toString(), () -> "bar");
  }

  void testAssertThatIsInstanceOf() {
    assertInstanceOf(Object.class, new Object());
  }

  void testAssertThatWithFailMessageStringIsInstanceOf() {
    assertInstanceOf(Object.class, new Object(), "foo");
  }

  void testAssertThatWithFailMessageSupplierIsInstanceOf() {
    assertInstanceOf(Object.class, new Object(), () -> "foo");
  }
}
