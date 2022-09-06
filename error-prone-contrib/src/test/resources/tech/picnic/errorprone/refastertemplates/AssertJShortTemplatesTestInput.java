package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJShortTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isCloseTo((short) 1, offset((short) 0)),
        assertThat((short) 0).isCloseTo((short) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 == (short) 2).isTrue(), assertThat((short) 1 != (short) 2).isFalse());
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isNotCloseTo((short) 1, offset((short) 0)),
        assertThat((short) 0).isNotCloseTo((short) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 != (short) 2).isTrue(), assertThat((short) 1 == (short) 2).isFalse());
  }

  AbstractShortAssert<?> testAbstractShortAssertIsZero() {
    return assertThat((short) 0).isZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotZero() {
    return assertThat((short) 0).isNotZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsOne() {
    return assertThat((short) 0).isOne();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 < (short) 2).isTrue(), assertThat((short) 1 >= (short) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 <= (short) 2).isTrue(), assertThat((short) 1 > (short) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 > (short) 2).isTrue(), assertThat((short) 1 <= (short) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1 >= (short) 2).isTrue(), assertThat((short) 1 < (short) 2).isFalse());
  }
}
