package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TimeZoneUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TimeZoneUsage.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static java.time.ZoneOffset.UTC;

            import java.time.Clock;
            import java.time.Duration;
            import java.time.Instant;
            import java.time.LocalDate;
            import java.time.LocalDateTime;
            import java.time.LocalTime;
            import java.time.OffsetDateTime;
            import java.time.OffsetTime;
            import java.time.ZoneId;
            import java.time.ZonedDateTime;

            class A {
              void m() {
                Clock clock = Clock.fixed(Instant.EPOCH, UTC);
                clock.instant();
                clock.millis();
                Clock.offset(clock, Duration.ZERO);
                Clock.tick(clock, Duration.ZERO);

                // BUG: Diagnostic contains:
                Clock.systemUTC();
                // BUG: Diagnostic contains:
                Clock.systemDefaultZone();
                // BUG: Diagnostic contains:
                Clock.system(UTC);
                // BUG: Diagnostic contains:
                Clock.tickMillis(UTC);
                // BUG: Diagnostic contains:
                Clock.tickMinutes(UTC);
                // BUG: Diagnostic contains:
                Clock.tickSeconds(UTC);
                // BUG: Diagnostic contains:
                clock.getZone();
                // BUG: Diagnostic contains:
                clock.withZone(UTC);

                // BUG: Diagnostic contains:
                Instant.now();
                // This is equivalent to `clock.instant()`, which is fine.
                Instant.now(clock);

                // BUG: Diagnostic contains:
                LocalDate.now();
                // BUG: Diagnostic contains:
                LocalDate.now(clock);
                // BUG: Diagnostic contains:
                LocalDate.now(UTC);

                // BUG: Diagnostic contains:
                LocalDateTime.now();
                // BUG: Diagnostic contains:
                LocalDateTime.now(clock);
                // BUG: Diagnostic contains:
                LocalDateTime.now(UTC);

                // BUG: Diagnostic contains:
                LocalTime.now();
                // BUG: Diagnostic contains:
                LocalTime.now(clock);
                // BUG: Diagnostic contains:
                LocalTime.now(UTC);

                // BUG: Diagnostic contains:
                OffsetDateTime.now();
                // BUG: Diagnostic contains:
                OffsetDateTime.now(clock);
                // BUG: Diagnostic contains:
                OffsetDateTime.now(UTC);

                // BUG: Diagnostic contains:
                OffsetTime.now();
                // BUG: Diagnostic contains:
                OffsetTime.now(clock);
                // BUG: Diagnostic contains:
                OffsetTime.now(UTC);

                // BUG: Diagnostic contains:
                ZonedDateTime.now();
                // BUG: Diagnostic contains:
                ZonedDateTime.now(clock);
                // BUG: Diagnostic contains:
                ZonedDateTime.now(UTC);
              }

              abstract class ForwardingClock extends Clock {
                private final Clock clock;

                ForwardingClock(Clock clock) {
                  this.clock = clock;
                }

                @Override
                public ZoneId getZone() {
                  return clock.getZone();
                }

                @Override
                public Clock withZone(ZoneId zone) {
                  return clock.withZone(zone);
                }
              }
            }
            """)
        .doTest();
  }
}
