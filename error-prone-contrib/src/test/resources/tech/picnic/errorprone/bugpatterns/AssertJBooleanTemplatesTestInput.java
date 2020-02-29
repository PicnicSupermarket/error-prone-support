package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;

final class AssertJBooleanTemplatesTest implements RefasterTemplateTestCase {
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isNotEqualTo(!false);
  }

  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isEqualTo(!false);
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(true),
        assertThat(true).isEqualTo(Boolean.TRUE),
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(Boolean.FALSE));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(!true).isFalse();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(Boolean.FALSE),
        assertThat(true).isNotEqualTo(true),
        assertThat(true).isNotEqualTo(Boolean.TRUE));
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(!true).isTrue();
  }
}
