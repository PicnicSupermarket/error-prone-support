package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0).isEqualTo(1), assertThat(0).isEqualTo(1));
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsEqualToExpected() {
    return assertThat(1).isEqualTo(2);
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0).isNotEqualTo(1), assertThat(0).isNotEqualTo(1));
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsNotEqualToExpected() {
    return assertThat(1).isNotEqualTo(2);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(0).isEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(0).isNotEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(0).isEqualTo(1);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsLessThanExpected() {
    return assertThat(1).isLessThan(2);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsLessThanOrEqualToExpected() {
    return assertThat(1).isLessThanOrEqualTo(2);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsGreaterThanExpected() {
    return assertThat(1).isGreaterThan(2);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertActualIsGreaterThanOrEqualToExpected() {
    return assertThat(1).isGreaterThanOrEqualTo(2);
  }
}
