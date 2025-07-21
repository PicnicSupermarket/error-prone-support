package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJArrayRulesTest implements RefasterRuleCollectionTestCase {
  void testAssertThatArrayIsEmpty() {
    assertThat(new String[0]).isEmpty();
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSize() {
    return assertThat(new String[7]).hasSize(7);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSameSizeAs() {
    return assertThat(new String[3]).hasSameSizeAs(new Integer[3]);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThanOrEqualTo() {
    return assertThat(new String[2]).hasSizeLessThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThan() {
    return assertThat(new String[4]).hasSizeLessThan(5);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThan() {
    return assertThat(new String[5]).hasSizeGreaterThan(4);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThanOrEqualTo() {
    return assertThat(new String[1]).hasSizeGreaterThanOrEqualTo(1);
  }
}