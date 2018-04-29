package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
 * retained.)
 *
 * <p><strong>Warning:</strong> this rewrite rule increases precision!
 */
final class DurationBetweenOffsetDateTimes {
  @BeforeTemplate
  Duration before(OffsetDateTime a, OffsetDateTime b) {
    return Refaster.anyOf(
        Duration.between(a.toInstant(), b.toInstant()),
        Duration.ofSeconds(b.toEpochSecond() - a.toEpochSecond()));
  }

  @AfterTemplate
  Duration after(OffsetDateTime a, OffsetDateTime b) {
    return Duration.between(a, b);
  }
}
