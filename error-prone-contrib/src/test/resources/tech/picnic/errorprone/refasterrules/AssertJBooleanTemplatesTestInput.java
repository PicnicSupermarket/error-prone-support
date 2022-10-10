package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJBooleanTemplatesTest implements RefasterRuleCollectionTestCase {
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isNotEqualTo(!Boolean.FALSE);
  }

  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isEqualTo(!Boolean.FALSE);
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(true),
        assertThat(true).isEqualTo(Boolean.TRUE),
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(Boolean.FALSE));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(!Boolean.TRUE).isFalse();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(Boolean.FALSE),
        assertThat(true).isNotEqualTo(true),
        assertThat(true).isNotEqualTo(Boolean.TRUE));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(!Boolean.TRUE).isTrue();
  }
}
