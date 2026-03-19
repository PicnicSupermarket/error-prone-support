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
    return ImmutableSet.of(assertThat(0L).isEqualTo(1), assertThat(0L).isEqualTo(2));
  }

  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0L).isNotEqualTo(1), assertThat(0L).isNotEqualTo(2));
  }

  AbstractLongAssert<?> testAbstractLongAssertIsEqualToZero() {
    return assertThat(0L).isEqualTo(0);
  }

  AbstractLongAssert<?> testAbstractLongAssertIsNotEqualToZero() {
    return assertThat(0L).isNotEqualTo(0);
  }

  AbstractLongAssert<?> testAbstractLongAssertIsEqualToOne() {
    return assertThat(0L).isEqualTo(1);
  }
}
