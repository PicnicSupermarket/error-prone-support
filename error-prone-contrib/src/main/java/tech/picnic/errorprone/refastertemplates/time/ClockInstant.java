package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Clock;
import java.time.Instant;

/**
 * Prefer {@link Clock#instant()} over {@link Instant#now(Clock)}, as it is more concise and more
 * "OOP-py".
 */
final class ClockInstant {
  @BeforeTemplate
  Instant before(Clock clock) {
    return Instant.now(clock);
  }

  @AfterTemplate
  Instant after(Clock clock) {
    return clock.instant();
  }
}
