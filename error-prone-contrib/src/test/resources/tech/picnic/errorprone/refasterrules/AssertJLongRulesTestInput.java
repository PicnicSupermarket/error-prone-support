package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJLongRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0L).isCloseTo(1, offset(0L)), assertThat(0L).isCloseTo(1, withPercentage(0)));
  }

  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0L).isNotCloseTo(1, offset(0L)),
        assertThat(0L).isNotCloseTo(1, withPercentage(0)));
  }

  AbstractLongAssert<?> testAbstractLongAssertIsZero() {
    return assertThat(0L).isZero();
  }

  AbstractLongAssert<?> testAbstractLongAssertIsNotZero() {
    return assertThat(0L).isNotZero();
  }

  AbstractLongAssert<?> testAbstractLongAssertIsOne() {
    return assertThat(0L).isOne();
  }
}
