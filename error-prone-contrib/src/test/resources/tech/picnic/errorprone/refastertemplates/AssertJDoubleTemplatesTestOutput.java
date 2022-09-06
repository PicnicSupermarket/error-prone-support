package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJDoubleTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0.0).isCloseTo(1, offset(0.0)),
        assertThat(0.0).isCloseTo(Double.valueOf(1), offset(0.0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0.0).isEqualTo(1), assertThat(0.0).isEqualTo(1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0).isEqualTo(2.0), assertThat(1.0).isEqualTo(2.0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0.0).isNotEqualTo(1), assertThat(0.0).isNotEqualTo(1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(assertThat(1.0).isNotEqualTo(2.0), assertThat(1.0).isNotEqualTo(2.0));
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsZero() {
    return assertThat(0.0).isEqualTo(0);
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotZero() {
    return assertThat(0.0).isNotEqualTo(0);
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsOne() {
    return assertThat(0.0).isEqualTo(1);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsLessThanExpected() {
    return ImmutableSet.of(assertThat(1.0).isLessThan(2.0), assertThat(1.0).isLessThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isLessThanOrEqualTo(2.0), assertThat(1.0).isLessThanOrEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(assertThat(1.0).isGreaterThan(2.0), assertThat(1.0).isGreaterThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractDoubleAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isGreaterThanOrEqualTo(2.0), assertThat(1.0).isGreaterThanOrEqualTo(2.0));
  }
}
