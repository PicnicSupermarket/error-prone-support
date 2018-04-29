package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.chrono.ChronoLocalDateTime;

/**
 * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over explicit comparison, as it
 * yields more readable code.
 */
final class ChronoLocalDateTimeIsAfter {
  @BeforeTemplate
  boolean before(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
    return a.compareTo(b) > 0;
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
    return a.isAfter(b);
  }
}
