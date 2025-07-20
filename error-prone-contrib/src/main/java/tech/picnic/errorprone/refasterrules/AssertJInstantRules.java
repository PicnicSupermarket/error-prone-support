package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Instant;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractInstantAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link Instant}s.
 *
 * <p>These rules simplify and improve the readability of tests by using {@link Instant}-specific
 * AssertJ assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJInstantRules {
  private AssertJInstantRules() {}

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

  static final class AssertThatIsBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant actual, Instant start, Instant end) {
      return Refaster.anyOf(
          assertThat(actual).isAfterOrEqualTo(start).isBeforeOrEqualTo(end),
          assertThat(actual).isBeforeOrEqualTo(end).isAfterOrEqualTo(start));
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant start, Instant end) {
      return assertThat(actual).isBetween(start, end);
    }
  }

  static final class AssertThatIsStrictlyBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant actual, Instant start, Instant end) {
      return Refaster.anyOf(
          assertThat(actual).isAfter(start).isBefore(end),
          assertThat(actual).isBefore(end).isAfter(start));
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant actual, Instant start, Instant end) {
      return assertThat(actual).isStrictlyBetween(start, end);
    }
  }
}
