package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.chrono.ChronoLocalDate;

/**
 * Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over explicit comparison, as it yields
 * more readable code.
 */
final class ChronoLocalDateIsAfter {
  @BeforeTemplate
  boolean before(ChronoLocalDate a, ChronoLocalDate b) {
    return a.compareTo(b) > 0;
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(ChronoLocalDate a, ChronoLocalDate b) {
    return a.isAfter(b);
  }
}
