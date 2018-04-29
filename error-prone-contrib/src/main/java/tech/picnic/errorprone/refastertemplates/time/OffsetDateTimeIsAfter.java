package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.OffsetDateTime;

/**
 * Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over explicit comparison, as it yields
 * more readable code.
 */
final class OffsetDateTimeIsAfter {
  @BeforeTemplate
  boolean before(OffsetDateTime a, OffsetDateTime b) {
    return a.compareTo(b) > 0;
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(OffsetDateTime a, OffsetDateTime b) {
    return a.isAfter(b);
  }
}
