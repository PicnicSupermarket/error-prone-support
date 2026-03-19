package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractDoubleAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJDoubleRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsCloseTo() {
    return ImmutableSet.of(
        assertThat(1.0).isEqualTo(2, offset(0.0)),
        assertThat(1.0).isEqualTo(Double.valueOf(2), offset(0.0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(1.0).isCloseTo(2, offset(0.0)), assertThat(1.0).isCloseTo(2, withPercentage(0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(1.0).isNotCloseTo(2, offset(0.0)),
        assertThat(1.0).isNotCloseTo(2, withPercentage(0)));
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsEqualToZero() {
    return assertThat(1.0).isZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotEqualToZero() {
    return assertThat(1.0).isNotZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsEqualToOne() {
    return assertThat(1.0).isOne();
  }
}
