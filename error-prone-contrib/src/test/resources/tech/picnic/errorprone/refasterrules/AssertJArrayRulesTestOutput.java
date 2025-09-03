package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJArrayRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSize() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSize(1), assertThat(new String[0][0].length).isEqualTo(2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSizeLessThan() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSizeLessThan(1),
        assertThat(new String[0][0].length).isLessThan(2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSizeLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSizeLessThanOrEqualTo(1),
        assertThat(new String[0][0].length).isLessThanOrEqualTo(2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSizeGreaterThan() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSizeGreaterThan(1),
        assertThat(new String[0][0].length).isGreaterThan(2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSizeGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSizeGreaterThanOrEqualTo(1),
        assertThat(new String[0][0].length).isGreaterThanOrEqualTo(2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSizeBetween() {
    return ImmutableSet.of(
        assertThat(new String[0]).hasSizeBetween(1, 2),
        assertThat(new String[0][0].length).isBetween(3, 4));
  }
}
