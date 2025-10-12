package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJDurationRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatHasNanos() {
    return assertThat(Duration.ZERO).hasNanos(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasMillis() {
    return assertThat(Duration.ZERO).hasMillis(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasSeconds() {
    return assertThat(Duration.ZERO).hasSeconds(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasMinutes() {
    return assertThat(Duration.ZERO).hasMinutes(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasHours() {
    return assertThat(Duration.ZERO).hasHours(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasDays() {
    return assertThat(Duration.ZERO).hasDays(0L);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsZero() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0)).isZero(), assertThat(Duration.ofMillis(1)).isZero());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsPositive() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0)).isPositive(),
        assertThat(Duration.ofMillis(1)).isPositive());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNegative() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0)).isNegative(),
        assertThat(Duration.ofMillis(1)).isNegative());
  }
}
