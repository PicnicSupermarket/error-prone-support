package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJArrayRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatArrayHasSize() {
    return assertThat(new String[7].length).isEqualTo(7);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThanOrEqualTo() {
    return assertThat(new String[2].length).isLessThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThan() {
    return assertThat(new String[4].length).isLessThan(5);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThan() {
    return assertThat(new String[5].length).isGreaterThan(4);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThanOrEqualTo() {
    return assertThat(new String[1].length).isGreaterThanOrEqualTo(1);
  }
}
