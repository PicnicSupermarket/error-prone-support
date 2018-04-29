package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Instant;

/**
 * Prefer {@link Instant#isBefore(Instant)} over explicit comparison, as it yields more readable
 * code.
 */
final class InstantIsAfter {
  @BeforeTemplate
  boolean before(Instant a, Instant b) {
    return a.compareTo(b) > 0;
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(Instant a, Instant b) {
    return a.isAfter(b);
  }
}
