package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJArrayRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatHasSize() {
    return assertThat(new String[0]).hasSize(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeLessThan() {
    return assertThat(new String[0]).hasSizeLessThan(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeLessThanOrEqualTo() {
    return assertThat(new String[0]).hasSizeLessThanOrEqualTo(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeGreaterThan() {
    return assertThat(new String[0]).hasSizeGreaterThan(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeGreaterThanOrEqualTo() {
    return assertThat(new String[0]).hasSizeGreaterThanOrEqualTo(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeBetween() {
    return assertThat(new String[0]).hasSizeBetween(1, 2);
  }
}
