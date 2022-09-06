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
        assertThat((byte) 0).isEqualTo((byte) 1), assertThat((byte) 0).isEqualTo((byte) 1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isEqualTo((byte) 2), assertThat((byte) 1).isEqualTo((byte) 2));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotEqualTo((byte) 1), assertThat((byte) 0).isNotEqualTo((byte) 1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isNotEqualTo((byte) 2), assertThat((byte) 1).isNotEqualTo((byte) 2));
  }

  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 0).isEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 0).isNotEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 0).isEqualTo((byte) 1);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThan((byte) 2), assertThat((byte) 1).isLessThan((byte) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThan((byte) 2), assertThat((byte) 1).isGreaterThan((byte) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractByteAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2));
  }
}
