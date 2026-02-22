package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    assertThat(new boolean[] {false}).containsExactly(new boolean[] {true});
  }

  void testAssertThatBooleanArrayWithFailMessageContainsExactly() {
    assertThat(new boolean[] {false}).withFailMessage("foo").containsExactly(new boolean[] {true});
  }

  void testAssertThatBooleanArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new boolean[] {false})
        .withFailMessage(() -> "foo")
        .containsExactly(new boolean[] {true});
  }

  void testAssertThatByteArrayContainsExactly() {
    assertThat(new byte[] {2}).containsExactly(new byte[] {1});
  }

  void testAssertThatByteArrayWithFailMessageContainsExactly() {
    assertThat(new byte[] {2}).withFailMessage("foo").containsExactly(new byte[] {1});
  }

  void testAssertThatByteArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new byte[] {2}).withFailMessage(() -> "foo").containsExactly(new byte[] {1});
  }

  void testAssertThatCharArrayContainsExactly() {
    assertThat(new char[] {'b'}).containsExactly(new char[] {'a'});
  }

  void testAssertThatCharArrayWithFailMessageContainsExactly() {
    assertThat(new char[] {'b'}).withFailMessage("foo").containsExactly(new char[] {'a'});
  }

  void testAssertThatCharArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new char[] {'b'}).withFailMessage(() -> "foo").containsExactly(new char[] {'a'});
  }

  void testAssertThatShortArrayContainsExactly() {
    assertThat(new short[] {2}).containsExactly(new short[] {1});
  }

  void testAssertThatShortArrayWithFailMessageContainsExactly() {
    assertThat(new short[] {2}).withFailMessage("foo").containsExactly(new short[] {1});
  }

  void testAssertThatShortArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new short[] {2}).withFailMessage(() -> "foo").containsExactly(new short[] {1});
  }

  void testAssertThatIntArrayContainsExactly() {
    assertThat(new int[] {2}).containsExactly(new int[] {1});
  }

  void testAssertThatIntArrayWithFailMessageContainsExactly() {
    assertThat(new int[] {2}).withFailMessage("foo").containsExactly(new int[] {1});
  }

  void testAssertThatIntArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new int[] {2}).withFailMessage(() -> "foo").containsExactly(new int[] {1});
  }

  void testAssertThatLongArrayContainsExactly() {
    assertThat(new long[] {2L}).containsExactly(new long[] {1L});
  }

  void testAssertThatLongArrayWithFailMessageContainsExactly() {
    assertThat(new long[] {2L}).withFailMessage("foo").containsExactly(new long[] {1L});
  }

  void testAssertThatLongArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new long[] {2L}).withFailMessage(() -> "foo").containsExactly(new long[] {1L});
  }

  void testAssertThatFloatArrayContainsExactly() {
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f});
  }

  void testAssertThatFloatArrayWithFailMessageContainsExactly() {
    assertThat(new float[] {2.0f}).withFailMessage("foo").containsExactly(new float[] {1.0f});
  }

  void testAssertThatFloatArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new float[] {2.0f}).withFailMessage(() -> "foo").containsExactly(new float[] {1.0f});
  }

  void testAssertThatFloatArrayContainsExactlyWithOffset() {
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatFloatArrayWithFailMessageContainsExactlyWithOffset() {
    assertThat(new float[] {2.0f})
        .withFailMessage("foo")
        .containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatFloatArrayWithFailMessageSupplierContainsExactlyWithOffset() {
    assertThat(new float[] {2.0f})
        .withFailMessage(() -> "foo")
        .containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatDoubleArrayContainsExactly() {
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0});
  }

  void testAssertThatDoubleArrayWithFailMessageContainsExactly() {
    assertThat(new double[] {2.0}).withFailMessage("foo").containsExactly(new double[] {1.0});
  }

  void testAssertThatDoubleArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new double[] {2.0}).withFailMessage(() -> "foo").containsExactly(new double[] {1.0});
  }

  void testAssertThatDoubleArrayContainsExactlyWithOffset() {
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatDoubleArrayWithFailMessageContainsExactlyWithOffset() {
    assertThat(new double[] {2.0})
        .withFailMessage("foo")
        .containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatDoubleArrayWithFailMessageSupplierContainsExactlyWithOffset() {
    assertThat(new double[] {2.0})
        .withFailMessage(() -> "foo")
        .containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatObjectArrayContainsExactly() {
    assertThat(new Object[] {"bar"}).containsExactly(new Object[] {"foo"});
  }

  void testAssertThatObjectArrayWithFailMessageContainsExactly() {
    assertThat(new Object[] {"bar"}).withFailMessage("foo").containsExactly(new Object[] {"foo"});
  }

  void testAssertThatObjectArrayWithFailMessageSupplierContainsExactly() {
    assertThat(new Object[] {"bar"})
        .withFailMessage(() -> "foo")
        .containsExactly(new Object[] {"foo"});
  }

  Object testFail() {
    return org.assertj.core.api.Assertions.fail();
  }

  Object testFailWithMessage() {
    return org.assertj.core.api.Assertions.fail("foo");
  }

  Object testFailWithMessageAndThrowable() {
    return org.assertj.core.api.Assertions.fail("foo", new IllegalStateException());
  }

  Object testFailWithThrowable() {
    return org.assertj.core.api.Assertions.fail(new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertThat(true).isTrue();
  }

  void testAssertThatWithFailMessageStringIsTrue() {
    assertThat(true).withFailMessage("foo").isTrue();
  }

  void testAssertThatWithFailMessageSupplierIsTrue() {
    assertThat(true).withFailMessage(() -> "foo").isTrue();
  }

  void testAssertThatIsFalse() {
    assertThat(true).isFalse();
  }

  void testAssertThatWithFailMessageStringIsFalse() {
    assertThat(true).withFailMessage("foo").isFalse();
  }

  void testAssertThatWithFailMessageSupplierIsFalse() {
    assertThat(true).withFailMessage(() -> "foo").isFalse();
  }

  void testAssertThatIsNull() {
    assertThat(new Object()).isNull();
  }

  void testAssertThatWithFailMessageStringIsNull() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  void testAssertThatWithFailMessageSupplierIsNull() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNull();
  }

  void testAssertThatIsNotNull() {
    assertThat(new Object()).isNotNull();
  }

  void testAssertThatWithFailMessageStringIsNotNull() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  void testAssertThatWithFailMessageSupplierIsNotNull() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNotNull();
  }

  void testAssertThatIsSameAs() {
    assertThat("bar").isSameAs("foo");
  }

  void testAssertThatWithFailMessageStringIsSameAs() {
    assertThat("bar").withFailMessage("baz").isSameAs("foo");
  }

  void testAssertThatWithFailMessageSupplierIsSameAs() {
    assertThat("bar").withFailMessage(() -> "baz").isSameAs("foo");
  }

  void testAssertThatIsNotSameAs() {
    assertThat("bar").isNotSameAs("foo");
  }

  void testAssertThatWithFailMessageStringIsNotSameAs() {
    assertThat("bar").withFailMessage("baz").isNotSameAs("foo");
  }

  void testAssertThatWithFailMessageSupplierIsNotSameAs() {
    assertThat("bar").withFailMessage(() -> "baz").isNotSameAs("foo");
  }

  void testAssertThatThrownByIsExactlyInstanceOf() {
    assertThatThrownBy(() -> {}).isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageStringIsExactlyInstanceOf() {
    assertThatThrownBy(() -> {})
        .withFailMessage("foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageSupplierIsExactlyInstanceOf() {
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageStringIsInstanceOf() {
    assertThatThrownBy(() -> {}).withFailMessage("foo").isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageSupplierIsInstanceOf() {
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatCodeDoesNotThrowAnyException() {
    assertThatCode(() -> {}).doesNotThrowAnyException();
    assertThatCode(() -> toString()).doesNotThrowAnyException();
  }

  void testAssertThatCodeWithFailMessageStringDoesNotThrowAnyException() {
    assertThatCode(() -> {}).withFailMessage("foo").doesNotThrowAnyException();
    assertThatCode(() -> toString()).withFailMessage("bar").doesNotThrowAnyException();
  }

  void testAssertThatCodeWithFailMessageSupplierDoesNotThrowAnyException() {
    assertThatCode(() -> {}).withFailMessage(() -> "foo").doesNotThrowAnyException();
    assertThatCode(() -> toString()).withFailMessage(() -> "bar").doesNotThrowAnyException();
  }

  void testAssertThatIsInstanceOf() {
    assertThat(new Object()).isInstanceOf(Object.class);
  }

  void testAssertThatWithFailMessageStringIsInstanceOf() {
    assertThat(new Object()).withFailMessage("foo").isInstanceOf(Object.class);
  }

  void testAssertThatWithFailMessageSupplierIsInstanceOf() {
    assertThat(new Object()).withFailMessage(() -> "foo").isInstanceOf(Object.class);
  }
}
