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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
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
    assertThat(new boolean[] {false}).containsExactly(new boolean[] {true});
  }

  void testAssertThatWithFailMessageContainsExactlyBooleanString() {
    assertThat(new boolean[] {false}).withFailMessage("foo").containsExactly(new boolean[] {true});
  }

  void testAssertThatWithFailMessageContainsExactlyBooleanSupplier() {
    assertThat(new boolean[] {false})
        .withFailMessage(() -> "foo")
        .containsExactly(new boolean[] {true});
  }

  void testAssertThatContainsExactlyByte() {
    assertThat(new byte[] {2}).containsExactly(new byte[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyByteString() {
    assertThat(new byte[] {2}).withFailMessage("foo").containsExactly(new byte[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyByteSupplier() {
    assertThat(new byte[] {2}).withFailMessage(() -> "foo").containsExactly(new byte[] {1});
  }

  void testAssertThatContainsExactlyChar() {
    assertThat(new char[] {'b'}).containsExactly(new char[] {'a'});
  }

  void testAssertThatWithFailMessageContainsExactlyCharString() {
    assertThat(new char[] {'b'}).withFailMessage("foo").containsExactly(new char[] {'a'});
  }

  void testAssertThatWithFailMessageContainsExactlyCharSupplier() {
    assertThat(new char[] {'b'}).withFailMessage(() -> "foo").containsExactly(new char[] {'a'});
  }

  void testAssertThatContainsExactlyShort() {
    assertThat(new short[] {2}).containsExactly(new short[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyShortString() {
    assertThat(new short[] {2}).withFailMessage("foo").containsExactly(new short[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyShortSupplier() {
    assertThat(new short[] {2}).withFailMessage(() -> "foo").containsExactly(new short[] {1});
  }

  void testAssertThatContainsExactlyInt() {
    assertThat(new int[] {2}).containsExactly(new int[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyIntString() {
    assertThat(new int[] {2}).withFailMessage("foo").containsExactly(new int[] {1});
  }

  void testAssertThatWithFailMessageContainsExactlyIntSupplier() {
    assertThat(new int[] {2}).withFailMessage(() -> "foo").containsExactly(new int[] {1});
  }

  void testAssertThatContainsExactlyLong() {
    assertThat(new long[] {2L}).containsExactly(new long[] {1L});
  }

  void testAssertThatWithFailMessageContainsExactlyLongString() {
    assertThat(new long[] {2L}).withFailMessage("foo").containsExactly(new long[] {1L});
  }

  void testAssertThatWithFailMessageContainsExactlyLongSupplier() {
    assertThat(new long[] {2L}).withFailMessage(() -> "foo").containsExactly(new long[] {1L});
  }

  void testAssertThatContainsExactlyFloat() {
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f});
  }

  void testAssertThatWithFailMessageContainsExactlyFloatString() {
    assertThat(new float[] {2.0f}).withFailMessage("foo").containsExactly(new float[] {1.0f});
  }

  void testAssertThatWithFailMessageContainsExactlyFloatSupplier() {
    assertThat(new float[] {2.0f}).withFailMessage(() -> "foo").containsExactly(new float[] {1.0f});
  }

  void testAssertThatContainsExactlyOffsetFloat() {
    assertThat(new float[] {2.0f}).containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetFloatString() {
    assertThat(new float[] {2.0f})
        .withFailMessage("foo")
        .containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetFloatSupplier() {
    assertThat(new float[] {2.0f})
        .withFailMessage(() -> "foo")
        .containsExactly(new float[] {1.0f}, offset(0.1f));
  }

  void testAssertThatContainsExactlyDouble() {
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0});
  }

  void testAssertThatWithFailMessageContainsExactlyDoubleString() {
    assertThat(new double[] {2.0}).withFailMessage("foo").containsExactly(new double[] {1.0});
  }

  void testAssertThatWithFailMessageContainsExactlyDoubleSupplier() {
    assertThat(new double[] {2.0}).withFailMessage(() -> "foo").containsExactly(new double[] {1.0});
  }

  void testAssertThatContainsExactlyOffsetDouble() {
    assertThat(new double[] {2.0}).containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetDoubleString() {
    assertThat(new double[] {2.0})
        .withFailMessage("foo")
        .containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatWithFailMessageContainsExactlyOffsetDoubleSupplier() {
    assertThat(new double[] {2.0})
        .withFailMessage(() -> "foo")
        .containsExactly(new double[] {1.0}, offset(0.1));
  }

  void testAssertThatContainsExactlyObject() {
    assertThat(new Object[] {"bar"}).containsExactly(new Object[] {"foo"});
  }

  void testAssertThatWithFailMessageContainsExactlyObjectString() {
    assertThat(new Object[] {"bar"}).withFailMessage("foo").containsExactly(new Object[] {"foo"});
  }

  void testAssertThatWithFailMessageContainsExactlyObjectSupplier() {
    assertThat(new Object[] {"bar"})
        .withFailMessage(() -> "foo")
        .containsExactly(new Object[] {"foo"});
  }

  Object testFail() {
    return org.assertj.core.api.Assertions.fail();
  }

  Object testFailWithString() {
    return org.assertj.core.api.Assertions.fail("foo");
  }

  Object testFailWithStringAndThrowable() {
    return org.assertj.core.api.Assertions.fail("foo", new IllegalStateException());
  }

  Object testFailWithThrowable() {
    return org.assertj.core.api.Assertions.fail(new IllegalStateException());
  }

  void testAssertThatIsTrue() {
    assertThat(true).isTrue();
  }

  void testAssertThatWithFailMessageIsTrueString() {
    assertThat(true).withFailMessage("foo").isTrue();
  }

  void testAssertThatWithFailMessageIsTrueSupplier() {
    assertThat(true).withFailMessage(() -> "foo").isTrue();
  }

  void testAssertThatIsFalse() {
    assertThat(true).isFalse();
  }

  void testAssertThatWithFailMessageIsFalseString() {
    assertThat(true).withFailMessage("foo").isFalse();
  }

  void testAssertThatWithFailMessageIsFalseSupplier() {
    assertThat(true).withFailMessage(() -> "foo").isFalse();
  }

  void testAssertThatIsNull() {
    assertThat(new Object()).isNull();
  }

  void testAssertThatWithFailMessageIsNullString() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  void testAssertThatWithFailMessageIsNullSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNull();
  }

  void testAssertThatIsNotNull() {
    assertThat(new Object()).isNotNull();
  }

  void testAssertThatWithFailMessageIsNotNullString() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  void testAssertThatWithFailMessageIsNotNullSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNotNull();
  }

  void testAssertThatIsSameAs() {
    assertThat("bar").isSameAs("foo");
  }

  void testAssertThatWithFailMessageIsSameAsString() {
    assertThat("bar").withFailMessage("baz").isSameAs("foo");
  }

  void testAssertThatWithFailMessageIsSameAsSupplier() {
    assertThat("bar").withFailMessage(() -> "baz").isSameAs("foo");
  }

  void testAssertThatIsNotSameAs() {
    assertThat("bar").isNotSameAs("foo");
  }

  void testAssertThatWithFailMessageIsNotSameAsString() {
    assertThat("bar").withFailMessage("baz").isNotSameAs("foo");
  }

  void testAssertThatWithFailMessageIsNotSameAsSupplier() {
    assertThat("bar").withFailMessage(() -> "baz").isNotSameAs("foo");
  }

  void testAssertThatThrownByIsExactlyInstanceOf() {
    assertThrowsExactly(IllegalStateException.class, (Executable) null);
    assertThatThrownBy(() -> {}).isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageIsExactlyInstanceOfString() {
    assertThrowsExactly(IllegalStateException.class, (Executable) null, "foo");
    assertThatThrownBy(() -> {})
        .withFailMessage("foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageIsExactlyInstanceOfSupplier() {
    assertThrowsExactly(IllegalStateException.class, (Executable) null, () -> "foo");
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThrows(IllegalStateException.class, (Executable) null);
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageIsInstanceOfString() {
    assertThrows(IllegalStateException.class, (Executable) null, "foo");
    assertThatThrownBy(() -> {}).withFailMessage("foo").isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatThrownByWithFailMessageIsInstanceOfSupplier() {
    assertThrows(IllegalStateException.class, (Executable) null, () -> "foo");
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isInstanceOf(IllegalStateException.class);
  }

  void testAssertThatCodeDoesNotThrowAnyException() {
    assertDoesNotThrow((Executable) null);
    assertThatCode(() -> {}).doesNotThrowAnyException();
    assertDoesNotThrow((ThrowingSupplier<String>) null);
    assertThatCode(() -> toString()).doesNotThrowAnyException();
  }

  void testAssertThatCodeWithFailMessageDoesNotThrowAnyExceptionString() {
    assertDoesNotThrow((Executable) null);
    assertThatCode(() -> {}).withFailMessage("foo").doesNotThrowAnyException();
    assertDoesNotThrow((ThrowingSupplier<String>) null, "bar");
    assertThatCode(() -> toString()).withFailMessage("bar").doesNotThrowAnyException();
  }

  void testAssertThatCodeWithFailMessageDoesNotThrowAnyExceptionSupplier() {
    assertDoesNotThrow((Executable) null, () -> "foo");
    assertThatCode(() -> {}).withFailMessage(() -> "foo").doesNotThrowAnyException();
    assertDoesNotThrow((ThrowingSupplier<String>) null, () -> "bar");
    assertThatCode(() -> toString()).withFailMessage(() -> "bar").doesNotThrowAnyException();
  }

  void testAssertThatIsInstanceOf() {
    assertThat(new Object()).isInstanceOf(Object.class);
  }

  void testAssertThatWithFailMessageIsInstanceOfString() {
    assertThat(new Object()).withFailMessage("foo").isInstanceOf(Object.class);
  }

  void testAssertThatWithFailMessageIsInstanceOfSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isInstanceOf(Object.class);
  }
}
