package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJDurationRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatHasNanos() {
    return assertThat(Duration.ZERO.toNanos()).isEqualTo(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasMillis() {
    return assertThat(Duration.ZERO.toMillis()).isEqualTo(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasSeconds() {
    return assertThat(Duration.ZERO.toSeconds()).isEqualTo(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasMinutes() {
    return assertThat(Duration.ZERO.toMinutes()).isEqualTo(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasHours() {
    return assertThat(Duration.ZERO.toHours()).isEqualTo(0L);
  }

  AbstractAssert<?, ?> testAssertThatHasDays() {
    return assertThat(Duration.ZERO.toDays()).isEqualTo(0L);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsZero() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0).isZero()).isTrue(),
        assertThat(Duration.ofMillis(1)).isEqualTo(Duration.ZERO));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsPositive() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0).isPositive()).isTrue(),
        assertThat(Duration.ofMillis(1)).isGreaterThan(Duration.ZERO));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNegative() {
    return ImmutableSet.of(
        assertThat(Duration.ofMillis(0).isNegative()).isTrue(),
        assertThat(Duration.ofMillis(1)).isLessThan(Duration.ZERO));
  }
}
