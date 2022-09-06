package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJDoubleTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0.0).isEqualTo(1, offset(0.0)),
        assertThat(0.0).isEqualTo(Double.valueOf(1), offset(0.0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isCloseTo(1, offset(0.0)), assertThat(0.0).isCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractDoubleAssertActualIsEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0 == 2.0).isTrue(), assertThat(1.0 != 2.0).isFalse());
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isNotCloseTo(1, offset(0.0)),
        assertThat(0.0).isNotCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractDoubleAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0 != 2.0).isTrue(), assertThat(1.0 == 2.0).isFalse());
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsZero() {
    return assertThat(0.0).isZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotZero() {
    return assertThat(0.0).isNotZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsOne() {
    return assertThat(0.0).isOne();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractDoubleAssertActualIsLessThanExpected() {
    return ImmutableSet.of(assertThat(1.0 < 2.0).isTrue(), assertThat(1.0 >= 2.0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractDoubleAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0 <= 2.0).isTrue(), assertThat(1.0 > 2.0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractDoubleAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(assertThat(1.0 > 2.0).isTrue(), assertThat(1.0 <= 2.0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractDoubleAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0 >= 2.0).isTrue(), assertThat(1.0 < 2.0).isFalse());
  }
}
