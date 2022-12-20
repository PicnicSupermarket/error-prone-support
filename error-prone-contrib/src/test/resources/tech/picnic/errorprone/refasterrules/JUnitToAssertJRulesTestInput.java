package tech.picnic.errorprone.refasterrules;

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
        () -> assertFalse(true),
        () -> assertInstanceOf(null, null),
        () -> assertNotNull(null),
        () -> assertNotSame(null, null),
        () -> assertNull(null),
        () -> assertSame(null, null),
        () -> assertThrows(null, null),
        () -> assertThrowsExactly(null, null),
        () -> assertTrue(true),
        () -> Assertions.fail());
  }

  void testThrowNewAssertionError() {
    Assertions.fail();
  }

  void testFailWithMessage() {
    Assertions.fail("foo");
  }

  void testFailWithMessageAndThrowable() {
    Assertions.fail("foo", new IllegalStateException());
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
    Object actual = new Object();
    Object expected = new Object();
    assertSame(expected, actual);
  }

  void testAssertThatWithFailMessageStringIsSameAs() {
    Object actual = new Object();
    Object expected = new Object();
    assertSame(expected, actual, "foo");
  }

  void testAssertThatWithFailMessageSupplierIsSameAs() {
    Object actual = new Object();
    Object expected = new Object();
    assertSame(expected, actual, () -> "foo");
  }

  void testAssertThatIsNotSameAs() {
    Object actual = new Object();
    Object expected = new Object();
    assertNotSame(expected, actual);
  }

  void testAssertThatWithFailMessageStringIsNotSameAs() {
    Object actual = new Object();
    Object expected = new Object();
    assertNotSame(expected, actual, "foo");
  }

  void testAssertThatWithFailMessageSupplierIsNotSameAs() {
    Object actual = new Object();
    Object expected = new Object();
    assertNotSame(expected, actual, () -> "foo");
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
  }

  void testAssertThatCodeWithFailMessageStringDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {}, "foo");
  }

  void testAssertThatCodeWithFailMessageSupplierDoesNotThrowAnyException() {
    assertDoesNotThrow(() -> {}, () -> "foo");
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
