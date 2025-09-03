package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJBooleanRulesTest implements RefasterRuleCollectionTestCase {
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isNotEqualTo(!Boolean.FALSE);
  }

  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isEqualTo(!Boolean.FALSE);
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(assertThat(true).isEqualTo(true), assertThat(true).isNotEqualTo(false));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(!Boolean.TRUE).isFalse();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(assertThat(true).isEqualTo(false), assertThat(true).isNotEqualTo(true));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(!Boolean.TRUE).isTrue();
  }
}
