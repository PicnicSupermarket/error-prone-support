package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractDoubleAssert;

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
        assertThat(0.0).isCloseTo(1, offset(0.0)),
        assertThat(0.0).isCloseTo(Double.valueOf(1), offset(0.0)),
        assertThat(0.0).isCloseTo(1, withPercentage(0)),
        assertThat(0.0).isCloseTo(Double.valueOf(1), withPercentage(0)),
        assertThat(0.0).isEqualTo(Double.valueOf(1)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isNotCloseTo(1, offset(0.0)),
        assertThat(0.0).isNotCloseTo(Double.valueOf(1), offset(0.0)),
        assertThat(0.0).isNotCloseTo(1, withPercentage(0)),
        assertThat(0.0).isNotCloseTo(Double.valueOf(1), withPercentage(0)),
        assertThat(0.0).isNotEqualTo(Double.valueOf(1)));
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
}
