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
        assertThat((short) 1).isEqualTo((short) 2), assertThat((short) 1).isEqualTo((short) 2));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat((short) 1).isNotEqualTo((short) 2));
  }

  AbstractShortAssert<?> testAbstractShortAssertIsEqualToZero() {
    return assertThat((short) 1).isEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotEqualToZero() {
    return assertThat((short) 1).isNotEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsEqualToOne() {
    return assertThat((short) 1).isEqualTo((short) 1);
  }
}
