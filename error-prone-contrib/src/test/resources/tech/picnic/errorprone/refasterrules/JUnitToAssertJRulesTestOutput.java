package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        (Runnable) () -> assertFalse(true),
        (Runnable) () -> assertNotNull(null),
        (Runnable) () -> assertNotSame(null, null),
        (Runnable) () -> assertNull(null),
        (Runnable) () -> assertSame(null, null),
        (Runnable) () -> assertTrue(true));
  }

  void testThrowNewAssertionError() {
    throw new AssertionError();
  }

  Object testFailWithMessage() {
    return org.assertj.core.api.Assertions.fail("foo");
  }

  Object testFailWithMessageAndThrowable() {
    return org.assertj.core.api.Assertions.fail("foo", new IllegalStateException());
  }

  void testFailWithThrowable() {
    throw new AssertionError(new IllegalStateException());
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
