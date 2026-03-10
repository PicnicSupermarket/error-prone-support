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
    return ImmutableSet.of(assertThat(1).isEqualTo(2), assertThat(1).isEqualTo(2));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(1).isNotEqualTo(2), assertThat(1).isNotEqualTo(2));
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(1).isEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(1).isNotEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(1).isEqualTo(1);
  }
}
