package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractByteAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJByteTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isCloseTo((byte) 1, offset((byte) 0)),
        assertThat((byte) 0).isCloseTo((byte) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 == (byte) 2).isTrue(), assertThat((byte) 1 != (byte) 2).isFalse());
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotCloseTo((byte) 1, offset((byte) 0)),
        assertThat((byte) 0).isNotCloseTo((byte) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 != (byte) 2).isTrue(), assertThat((byte) 1 == (byte) 2).isFalse());
  }

  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 0).isZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 0).isNotZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 0).isOne();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 < (byte) 2).isTrue(), assertThat((byte) 1 >= (byte) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 <= (byte) 2).isTrue(), assertThat((byte) 1 > (byte) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 > (byte) 2).isTrue(), assertThat((byte) 1 <= (byte) 2).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1 >= (byte) 2).isTrue(), assertThat((byte) 1 < (byte) 2).isFalse());
  }
}
