package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Duration;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDurationAssert;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Duration}s. */
@OnlineDocumentation
final class AssertJDurationRules {
  private AssertJDurationRules() {}

  /** Prefer {@link AbstractDurationAssert#hasNanos(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasNanos {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherNanos) {
      return assertThat(actual.toNanos()).isEqualTo(otherNanos);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherNanos) {
      return assertThat(actual).hasNanos(otherNanos);
    }
  }

  /** Prefer {@link AbstractDurationAssert#hasMillis(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasMillis {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherMillis) {
      return assertThat(actual.toMillis()).isEqualTo(otherMillis);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherMillis) {
      return assertThat(actual).hasMillis(otherMillis);
    }
  }

  /** Prefer {@link AbstractDurationAssert#hasSeconds(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSeconds {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherSeconds) {
      return assertThat(actual.toSeconds()).isEqualTo(otherSeconds);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherSeconds) {
      return assertThat(actual).hasSeconds(otherSeconds);
    }
  }

  /** Prefer {@link AbstractDurationAssert#hasMinutes(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasMinutes {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherMinutes) {
      return assertThat(actual.toMinutes()).isEqualTo(otherMinutes);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherMinutes) {
      return assertThat(actual).hasMinutes(otherMinutes);
    }
  }

  /** Prefer {@link AbstractDurationAssert#hasHours(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasHours {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherHours) {
      return assertThat(actual.toHours()).isEqualTo(otherHours);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherHours) {
      return assertThat(actual).hasHours(otherHours);
    }
  }

  /** Prefer {@link AbstractDurationAssert#hasDays(long)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasDays {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration actual, long otherDays) {
      return assertThat(actual.toDays()).isEqualTo(otherDays);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual, long otherDays) {
      return assertThat(actual).hasDays(otherDays);
    }
  }

  /** Prefer {@link AbstractDurationAssert#isZero()} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsZero {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration actual) {
      return assertThat(actual.isZero()).isTrue();
    }

    // XXX: This method can be folded into the preceding method using `Refaster#anyOf`, but by
    // keeping it separate, we show that this rule, contrary to the other one, retains the correct
    // return type.
    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration actual) {
      return assertThat(actual).isEqualTo(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual) {
      return assertThat(actual).isZero();
    }
  }

  /** Prefer {@link AbstractDurationAssert#isPositive()} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsPositive {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration actual) {
      return assertThat(actual.isPositive()).isTrue();
    }

    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration actual) {
      return assertThat(actual).isGreaterThan(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual) {
      return assertThat(actual).isPositive();
    }
  }

  /** Prefer {@link AbstractDurationAssert#isNegative()} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsNegative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration actual) {
      return assertThat(actual.isNegative()).isTrue();
    }

    // XXX: This method can be folded into the preceding method using `Refaster#anyOf`, but by
    // keeping it separate, we show that this rule, contrary to the other one, retains the correct
    // return type.
    @BeforeTemplate
    AbstractDurationAssert<?> before2(Duration actual) {
      return assertThat(actual).isLessThan(Duration.ZERO);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration actual) {
      return assertThat(actual).isNegative();
    }
  }
}
