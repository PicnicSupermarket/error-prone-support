package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJShortRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 1).isCloseTo((short) 2, offset((short) 0)),
        assertThat((short) 1).isCloseTo((short) 2, withPercentage(0)));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 1).isNotCloseTo((short) 2, offset((short) 0)),
        assertThat((short) 1).isNotCloseTo((short) 2, withPercentage(0)));
  }

  AbstractShortAssert<?> testAbstractShortAssertIsEqualToZero() {
    return assertThat((short) 1).isZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotEqualToZero() {
    return assertThat((short) 1).isNotZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsEqualToOne() {
    return assertThat((short) 1).isOne();
  }
}
