package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Duration;
import java.time.Instant;

/**
 * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
 * retained.)
 *
 * <p><strong>Warning:</strong> this rewrite rule increases precision!
 */
final class DurationBetweenInstants {
  @BeforeTemplate
  Duration before(Instant a, Instant b) {
    return Duration.ofMillis(b.toEpochMilli() - a.toEpochMilli());
  }

  @AfterTemplate
  Duration after(Instant a, Instant b) {
    return Duration.between(a, b);
  }
}
