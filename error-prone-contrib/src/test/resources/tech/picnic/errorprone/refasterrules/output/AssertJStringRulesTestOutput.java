package tech.picnic.errorprone.refasterrules.output;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStringRulesTest implements RefasterRuleCollectionTestCase {
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
