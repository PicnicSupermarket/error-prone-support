package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;

final class AssertJStringTemplatesTest implements RefasterTemplateTestCase {
  void testAbstractStringAssertStringIsEmpty() {
    assertThat("foo").isEqualTo("");
  }

  void testAssertThatStringIsEmpty() {
    assertThat("foo".isEmpty()).isTrue();
  }

  AbstractStringAssert<?> testAbstractStringAssertStringIsNotEmpty() {
    return assertThat("foo").isNotEqualTo("");
  }

  AbstractAssert<?, ?> testAssertThatStringIsNotEmpty() {
    return assertThat("foo".isEmpty()).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo".matches(".*")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo".matches(".*")).isFalse();
  }
}
