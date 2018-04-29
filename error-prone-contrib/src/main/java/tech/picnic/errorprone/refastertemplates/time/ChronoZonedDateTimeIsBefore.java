package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.chrono.ChronoZonedDateTime;

/**
 * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over explicit comparison, as it
 * yields more readable code.
 */
final class ChronoZonedDateTimeIsBefore {
  @BeforeTemplate
  boolean before(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
    return a.compareTo(b) < 0;
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
    return a.isBefore(b);
  }
}
