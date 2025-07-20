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
 * <p>These rules simplify and improve the readability of Duration assertions by using the more
 * specific AssertJ Duration assertion methods instead of generic assertions on the Duration's
 * numeric values.
 */
@OnlineDocumentation
final class AssertJDurationRules {
  private AssertJDurationRules() {}

  static final class AbstractDurationAssertHasNanos {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long nanos) {
      return assertThat(duration.toNanos()).isEqualTo(nanos);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long nanos) {
      return assertThat(duration).hasNanos(nanos);
    }
  }

  static final class AbstractDurationAssertHasMillis {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long millis) {
      return assertThat(duration.toMillis()).isEqualTo(millis);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long millis) {
      return assertThat(duration).hasMillis(millis);
    }
  }

  static final class AbstractDurationAssertHasSeconds {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long seconds) {
      return assertThat(duration.toSeconds()).isEqualTo(seconds);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long seconds) {
      return assertThat(duration).hasSeconds(seconds);
    }
  }

  static final class AbstractDurationAssertHasMinutes {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long minutes) {
      return assertThat(duration.toMinutes()).isEqualTo(minutes);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long minutes) {
      return assertThat(duration).hasMinutes(minutes);
    }
  }

  static final class AbstractDurationAssertHasHours {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long hours) {
      return assertThat(duration.toHours()).isEqualTo(hours);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long hours) {
      return assertThat(duration).hasHours(hours);
    }
  }

  static final class AbstractDurationAssertHasDays {
    @BeforeTemplate
    AbstractLongAssert<?> before(Duration duration, long days) {
      return assertThat(duration.toDays()).isEqualTo(days);
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration, long days) {
      return assertThat(duration).hasDays(days);
    }
  }

  static final class AbstractDurationAssertIsZero {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration duration) {
      return assertThat(duration.isZero()).isTrue();
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration) {
      return assertThat(duration).isZero();
    }
  }

  static final class AbstractDurationAssertIsNegative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Duration duration) {
      return assertThat(duration.isNegative()).isTrue();
    }

    @AfterTemplate
    AbstractDurationAssert<?> after(Duration duration) {
      return assertThat(duration).isNegative();
    }
  }
}
