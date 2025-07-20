package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Instant;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractInstantAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link Instant}s.
 *
 * <p>These rules simplify and improve the readability of Instant assertions by using the more
 * specific AssertJ Instant assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJInstantRules {
  private AssertJInstantRules() {}

  static final class AbstractInstantAssertIsAfter {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.isAfter(other)).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isAfter(other);
    }
  }

  static final class AbstractInstantAssertIsNotAfter {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.isAfter(other)).isFalse();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isBeforeOrEqualTo(other);
    }
  }

  static final class AbstractInstantAssertIsBefore {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.isBefore(other)).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isBefore(other);
    }
  }

  static final class AbstractInstantAssertIsNotBefore {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.isBefore(other)).isFalse();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isAfterOrEqualTo(other);
    }
  }

  static final class AbstractInstantAssertIsBeforeOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.compareTo(other) <= 0).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isBeforeOrEqualTo(other);
    }
  }

  static final class AbstractInstantAssertIsAfterOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Instant instant, Instant other) {
      return assertThat(instant.compareTo(other) >= 0).isTrue();
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant other) {
      return assertThat(instant).isAfterOrEqualTo(other);
    }
  }

  static final class AbstractInstantAssertIsBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant instant, Instant start, Instant end) {
      return assertThat(instant).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant start, Instant end) {
      return assertThat(instant).isBetween(start, end);
    }
  }

  static final class AbstractInstantAssertIsStrictlyBetween {
    @BeforeTemplate
    AbstractInstantAssert<?> before(Instant instant, Instant start, Instant end) {
      return assertThat(instant).isAfter(start).isBefore(end);
    }

    @AfterTemplate
    AbstractInstantAssert<?> after(Instant instant, Instant start, Instant end) {
      return assertThat(instant).isStrictlyBetween(start, end);
    }
  }
}
