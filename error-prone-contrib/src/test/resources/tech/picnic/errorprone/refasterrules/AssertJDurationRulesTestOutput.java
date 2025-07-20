package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractComparableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJDurationRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Duration.class);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasNanos() {
    return assertThat(Duration.ofNanos(1000)).hasNanos(1000L);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasMillis() {
    return assertThat(Duration.ofMillis(1000)).hasMillis(1000L);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasSeconds() {
    return assertThat(Duration.ofSeconds(60)).hasSeconds(60L);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasMinutes() {
    return assertThat(Duration.ofMinutes(5)).hasMinutes(5L);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasHours() {
    return assertThat(Duration.ofHours(2)).hasHours(2L);
  }

  AbstractComparableAssert<?, ?> testAbstractDurationAssertHasDays() {
    return assertThat(Duration.ofDays(7)).hasDays(7L);
  }

  AbstractAssert<?, ?> testAbstractDurationAssertIsZero() {
    return assertThat(Duration.ZERO).isZero();
  }

  AbstractAssert<?, ?> testAbstractDurationAssertIsNegative() {
    return assertThat(Duration.ofSeconds(-1)).isNegative();
  }
}
