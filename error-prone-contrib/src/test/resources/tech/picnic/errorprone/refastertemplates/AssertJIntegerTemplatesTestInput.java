package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0).isCloseTo(1, offset(0)), assertThat(0).isCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractIntegerAssertActualIsEqualToExpected() {
    return ImmutableSet.of(assertThat(1 == 2).isTrue(), assertThat(1 != 2).isFalse());
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0).isNotCloseTo(1, offset(0)), assertThat(0).isNotCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractIntegerAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(assertThat(1 != 2).isTrue(), assertThat(1 == 2).isFalse());
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(0).isZero();
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(0).isNotZero();
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(0).isOne();
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractIntegerAssertActualIsLessThanExpected() {
    return ImmutableSet.of(assertThat(1 < 2).isTrue(), assertThat(1 >= 2).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractIntegerAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1 <= 2).isTrue(), assertThat(1 > 2).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractIntegerAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(assertThat(1 > 2).isTrue(), assertThat(1 <= 2).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractIntegerAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1 >= 2).isTrue(), assertThat(1 < 2).isFalse());
  }
}
