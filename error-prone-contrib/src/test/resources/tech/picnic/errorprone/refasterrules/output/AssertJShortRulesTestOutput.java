package tech.picnic.errorprone.refasterrules.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJShortRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isEqualTo((short) 1), assertThat((short) 0).isEqualTo((short) 1));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isNotEqualTo((short) 1),
        assertThat((short) 0).isNotEqualTo((short) 1));
  }

  AbstractShortAssert<?> testAbstractShortAssertIsZero() {
    return assertThat((short) 0).isEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotZero() {
    return assertThat((short) 0).isNotEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsOne() {
    return assertThat((short) 0).isEqualTo((short) 1);
  }
}
