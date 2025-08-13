package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJArrayRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatHasSize() {
    return assertThat(new String[0].length).isEqualTo(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeLessThan() {
    return assertThat(new String[0].length).isLessThan(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeLessThanOrEqualTo() {
    return assertThat(new String[0].length).isLessThanOrEqualTo(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeGreaterThan() {
    return assertThat(new String[0].length).isGreaterThan(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeGreaterThanOrEqualTo() {
    return assertThat(new String[0].length).isGreaterThanOrEqualTo(1);
  }

  AbstractAssert<?, ?> testAssertThatHasSizeBetween() {
    return assertThat(new String[0].length).isBetween(1, 2);
  }
}
