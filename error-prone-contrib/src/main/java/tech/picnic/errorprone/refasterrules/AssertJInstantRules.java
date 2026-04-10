package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Instant;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractInstantAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Instant}s. */
@OnlineDocumentation
final class AssertJInstantRules {
  private AssertJInstantRules() {}

  /** Prefer {@link AbstractInstantAssert#isAfter(Instant)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsAfter {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant actual, Instant other) {
      return assertThat(actual.isAfter(other)).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant other) {
      return assertThat(actual).isAfter(other);
    }
  }

  /**
   * Prefer {@link AbstractInstantAssert#isBeforeOrEqualTo(Instant)} over less explicit
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsBeforeOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant actual, Instant other) {
      return assertThat(actual.isAfter(other)).isFalse();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant other) {
      return assertThat(actual).isBeforeOrEqualTo(other);
    }
  }

  /** Prefer {@link AbstractInstantAssert#isBefore(Instant)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsBefore {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant actual, Instant other) {
      return assertThat(actual.isBefore(other)).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant other) {
      return assertThat(actual).isBefore(other);
    }
  }

  /**
   * Prefer {@link AbstractInstantAssert#isAfterOrEqualTo(Instant)} over less explicit alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsAfterOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant actual, Instant other) {
      return assertThat(actual.isBefore(other)).isFalse();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant other) {
      return assertThat(actual).isAfterOrEqualTo(other);
    }
  }

  /**
   * Prefer {@link AbstractInstantAssert#isBetween(Instant, Instant)} over more verbose
   * alternatives.
   */
  static final class AssertThatIsBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant actual, Instant startInclusive, Instant endInclusive) {
      return Refaster.anyOf(
          assertThat(actual).isAfterOrEqualTo(startInclusive).isBeforeOrEqualTo(endInclusive),
          assertThat(actual).isBeforeOrEqualTo(endInclusive).isAfterOrEqualTo(startInclusive));
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant startInclusive, Instant endInclusive) {
      return assertThat(actual).isBetween(startInclusive, endInclusive);
    }
  }

  /**
   * Prefer {@link AbstractInstantAssert#isStrictlyBetween(Instant, Instant)} over more verbose
   * alternatives.
   */
  static final class AssertThatIsStrictlyBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant actual, Instant startExclusive, Instant endExclusive) {
      return Refaster.anyOf(
          assertThat(actual).isAfter(startExclusive).isBefore(endExclusive),
          assertThat(actual).isBefore(endExclusive).isAfter(startExclusive));
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant startExclusive, Instant endExclusive) {
      return assertThat(actual).isStrictlyBetween(startExclusive, endExclusive);
    }
  }
}
