package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJIntegerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(1).isCloseTo(2, offset(0)), assertThat(1).isCloseTo(2, withPercentage(0)));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(1).isNotCloseTo(2, offset(0)), assertThat(1).isNotCloseTo(2, withPercentage(0)));
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(1).isZero();
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(1).isNotZero();
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(1).isOne();
  }
}
