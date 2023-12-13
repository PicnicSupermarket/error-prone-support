package tech.picnic.errorprone.refasterrules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        (Runnable) () -> assertTrue(true),
        (Runnable) () -> assertEquals(0, 0),
        (Runnable) () -> assertEquals(0, 0, "foo"),
        (Runnable) () -> assertEquals(0, 0, () -> "foo"));
  }

  void testThrowNewAssertionError() {
    Assertions.fail();
  }

  Object testFailWithMessage() {
    return Assertions.fail("foo");
  }

  Object testFailWithMessageAndThrowable() {
    return Assertions.fail("foo", new IllegalStateException());
  }

  void testFailWithThrowable() {
    Assertions.fail(new IllegalStateException());
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

  void testAssertThatByteIsEqualTo() {
    assertEquals((byte) 0, (byte) 0);
  }

  void testAssertThatByteWithFailMessageStringIsEqualTo() {
    assertEquals((byte) 0, (byte) 0, "foo");
  }

  void testAssertThatByteWithFailMessageSupplierIsEqualTo() {
    assertEquals((byte) 0, (byte) 0, () -> "foo");
  }

  void testAssertThatCharIsEqualTo() {
    assertEquals('a', 'a');
  }

  void testAssertThatCharWithFailMessageStringIsEqualTo() {
    assertEquals('a', 'a', "foo");
  }

  void testAssertThatCharWithFailMessageSupplierIsEqualTo() {
    assertEquals('a', 'a', () -> "foo");
  }
}
