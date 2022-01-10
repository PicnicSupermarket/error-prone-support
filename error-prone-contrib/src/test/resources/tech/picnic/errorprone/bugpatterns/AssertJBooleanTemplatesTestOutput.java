package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJBooleanTemplatesTest implements RefasterTemplateTestCase {
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isEqualTo(false);
  }

  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isNotEqualTo(false);
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(
        assertThat(true).isTrue(),
        assertThat(true).isTrue(),
        assertThat(true).isTrue(),
        assertThat(true).isTrue());
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(true).isTrue();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(
        assertThat(true).isFalse(),
        assertThat(true).isFalse(),
        assertThat(true).isFalse(),
        assertThat(true).isFalse());
  }

  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(true).isFalse();
  }
}
