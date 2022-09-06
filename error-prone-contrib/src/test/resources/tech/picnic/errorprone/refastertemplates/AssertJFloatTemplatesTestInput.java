package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractFloatAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJFloatTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0F).isEqualTo(1, offset(0F)),
        assertThat(0F).isEqualTo(Float.valueOf(1), offset(0F)));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0F).isCloseTo(1, offset(0F)), assertThat(0F).isCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsEqualToExpected() {
    return ImmutableSet.of(assertThat(1F == 2F).isTrue(), assertThat(1F != 2F).isFalse());
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0F).isNotCloseTo(1, offset(0F)),
        assertThat(0F).isNotCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(assertThat(1F != 2F).isTrue(), assertThat(1F == 2F).isFalse());
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsZero() {
    return assertThat(0F).isZero();
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsNotZero() {
    return assertThat(0F).isNotZero();
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsOne() {
    return assertThat(0F).isOne();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsLessThanExpected() {
    return ImmutableSet.of(assertThat(1F < 2F).isTrue(), assertThat(1F >= 2F).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1F <= 2F).isTrue(), assertThat(1F > 2F).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(assertThat(1F > 2F).isTrue(), assertThat(1F <= 2F).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractFloatAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(assertThat(1F >= 2F).isTrue(), assertThat(1F < 2F).isFalse());
  }
}
