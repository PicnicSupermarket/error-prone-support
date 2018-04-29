package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Instant;

/** Prefer {@link Instant#EPOCH} over alternative representations. */
final class EpochInstant {
  @BeforeTemplate
  Instant before1() {
    return Instant.ofEpochMilli(0);
  }

  @BeforeTemplate
  Instant before2() {
    return Instant.ofEpochSecond(0);
  }

  @BeforeTemplate
  Instant before3() {
    return Instant.ofEpochSecond(0, 0);
  }

  @AfterTemplate
  Instant after() {
    return Instant.EPOCH;
  }
}
