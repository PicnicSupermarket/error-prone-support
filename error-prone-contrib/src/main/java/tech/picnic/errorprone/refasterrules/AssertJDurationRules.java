package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Duration;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDurationAssert;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link Duration}s.
 *
 * <p>These rules simplify and improve the rAssertJDurationRuleseadability of tests by using {@link
 * Duration}-specific AssertJ assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJDurationRules {
  private AssertJDurationRules() {}

  static final class AssertThatHasNanos {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long nanos) {
      return assertThat(duration.toNanos()).isEqualTo(nanos);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long nanos) {
      return assertThat(duration).hasNanos(nanos);
    }
  }

  static final class AssertThatHasMillis {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long millis) {
      return assertThat(duration.toMillis()).isEqualTo(millis);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long millis) {
      return assertThat(duration).hasMillis(millis);
    }
  }

  static final class AssertThatHasSeconds {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long seconds) {
      return assertThat(duration.toSeconds()).isEqualTo(seconds);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long seconds) {
      return assertThat(duration).hasSeconds(seconds);
    }
  }

  static final class AssertThatHasMinutes {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long minutes) {
      return assertThat(duration.toMinutes()).isEqualTo(minutes);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long minutes) {
      return assertThat(duration).hasMinutes(minutes);
    }
  }

  static final class AssertThatHasHours {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long hours) {
      return assertThat(duration.toHours()).isEqualTo(hours);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long hours) {
      return assertThat(duration).hasHours(hours);
    }
  }

  static final class AssertThatHasDays {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long days) {
      return assertThat(duration.toDays()).isEqualTo(days);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long days) {
      return assertThat(duration).hasDays(days);
    }
  }

  static final class AssertThatIsZero {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration duration) {
      return assertThat(duration.isZero()).isTrue();
    }

    // XXX: This method can be folded into the preceding method using `Refaster#anyOf`, but by
    // keeping it separate, we show that this rule, contrary to the other one, retains the correct
    // return type.
    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration duration) {
      return assertThat(duration).isEqualTo(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration) {
      return assertThat(duration).isZero();
    }
  }

  static final class AssertThatIsPositive {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration duration) {
      return assertThat(duration.isPositive()).isTrue();
    }

    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration duration) {
      return assertThat(duration).isGreaterThan(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration) {
      return assertThat(duration).isPositive();
    }
  }

  static final class AssertThatIsNegative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration duration) {
      return assertThat(duration.isNegative()).isTrue();
    }

    // XXX: This method can be folded into the preceding method using `Refaster#anyOf`, but by
    // keeping it separate, we show that this rule, contrary to the other one, retains the correct
    // return type.
    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration duration) {
      return assertThat(duration).isLessThan(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration) {
      return assertThat(duration).isNegative();
    }
  }
}
