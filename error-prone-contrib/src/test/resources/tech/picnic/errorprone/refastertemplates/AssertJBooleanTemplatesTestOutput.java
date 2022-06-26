package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;

final class AssertJBooleanTemplatesTest implements RefasterTemplateTestCase {
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isEqualTo(Boolean.FALSE);
  }

  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isNotEqualTo(Boolean.FALSE);
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(
        assertThat(true).isTrue(),
        assertThat(true).isTrue(),
        assertThat(true).isTrue(),
        assertThat(true).isTrue());
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(Boolean.TRUE).isTrue();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(
        assertThat(true).isFalse(),
        assertThat(true).isFalse(),
        assertThat(true).isFalse(),
        assertThat(true).isFalse());
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(Boolean.TRUE).isFalse();
  }
}
