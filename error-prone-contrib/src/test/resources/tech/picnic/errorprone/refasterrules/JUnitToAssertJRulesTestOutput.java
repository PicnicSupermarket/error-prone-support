package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
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
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        (Runnable) () -> assertDoesNotThrow(() -> null),
        (Runnable) () -> assertFalse(true),
        (Runnable) () -> assertInstanceOf(null, null),
        (Runnable) () -> assertNotNull(null),
        (Runnable) () -> assertNotSame(null, null),
        (Runnable) () -> assertNull(null),
        (Runnable) () -> assertSame(null, null),
        (Runnable) () -> assertThrows(null, null),
        (Runnable) () -> assertThrowsExactly(null, null),
        (Runnable) () -> assertTrue(true),
        (Runnable) () -> Assertions.fail());
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

  void testAssertTrueWithMessageSupplier() {
    assertThat(true).withFailMessage(() -> "foo").isTrue();
  }

  void testAssertFalse() {
    assertThat(true).isFalse();
  }

  void testAssertFalseWithMessage() {
    assertThat(true).withFailMessage("foo").isFalse();
  }

  void testAssertFalseWithMessageSupplier() {
    assertThat(true).withFailMessage(() -> "foo").isFalse();
  }

  void testAssertNull() {
    assertThat(new Object()).isNull();
  }

  void testAssertNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNull();
  }

  void testAssertNullWithMessageSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNull();
  }

  void testAssertNotNull() {
    assertThat(new Object()).isNotNull();
  }

  void testAssertNotNullWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isNotNull();
  }

  void testAssertNotNullWithMessageSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isNotNull();
  }

  void testAssertSame() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).isSameAs(expected);
  }

  void testAssertSameWithMessage() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).withFailMessage("foo").isSameAs(expected);
  }

  void testAssertSameWithMessageSupplier() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).withFailMessage(() -> "foo").isSameAs(expected);
  }

  void testAssertNotSame() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).isNotSameAs(expected);
  }

  void testAssertNotSameWithMessage() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).withFailMessage("foo").isNotSameAs(expected);
  }

  void testAssertNotSameWithMessageSupplier() {
    Object actual = new Object();
    Object expected = new Object();
    assertThat(actual).withFailMessage(() -> "foo").isNotSameAs(expected);
  }

  void testAssertThrowsExactly() {
    assertThatThrownBy(() -> {}).isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThrowsExactlyWithMessage() {
    assertThatThrownBy(() -> {})
        .withFailMessage("foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThrowsExactlyWithMessageSupplier() {
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  void testAssertThrows() {
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  void testAssertThrowsWithMessage() {
    assertThatThrownBy(() -> {}).withFailMessage("foo").isInstanceOf(IllegalStateException.class);
  }

  void testAssertThrowsWithMessageSupplier() {
    assertThatThrownBy(() -> {})
        .withFailMessage(() -> "foo")
        .isInstanceOf(IllegalStateException.class);
  }

  void testAssertDoesNotThrow() {
    assertThatCode(() -> {}).doesNotThrowAnyException();
  }

  void testAssertDoesNotThrowWithMessage() {
    assertThatCode(() -> {}).withFailMessage("foo").doesNotThrowAnyException();
  }

  void testAssertDoesNotThrowWithMessageSupplier() {
    assertThatCode(() -> {}).withFailMessage(() -> "foo").doesNotThrowAnyException();
  }

  void testAssertInstanceOf() {
    assertThat(new Object()).isInstanceOf(Object.class);
  }

  void testAssertInstanceOfWithMessage() {
    assertThat(new Object()).withFailMessage("foo").isInstanceOf(Object.class);
  }

  void testAssertInstanceOfWithMessageSupplier() {
    assertThat(new Object()).withFailMessage(() -> "foo").isInstanceOf(Object.class);
  }
}
