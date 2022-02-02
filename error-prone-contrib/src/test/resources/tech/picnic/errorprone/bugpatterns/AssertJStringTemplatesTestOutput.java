package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;

final class AssertJStringTemplatesTest implements RefasterTemplateTestCase {
  void testAbstractStringAssertStringIsEmpty() {
    assertThat("foo").isEmpty();
  }

  void testAssertThatStringIsEmpty() {
    assertThat("foo").isEmpty();
  }

  AbstractStringAssert<?> testAbstractStringAssertStringIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatStringIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo").matches(".*");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo").doesNotMatch(".*");
  }
}
