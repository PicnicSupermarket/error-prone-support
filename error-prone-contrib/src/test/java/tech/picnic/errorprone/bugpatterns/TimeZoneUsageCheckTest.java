package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class TimeZoneUsageCheckTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(TimeZoneUsageCheck.class, getClass())
          .expectErrorMessage(
              "X",
              containsPattern(
                  "Derive the current time from an existing `Clock` Spring bean, and don't rely on a `Clock`'s time zone"));

  @Test
  void identification() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "import static java.time.ZoneOffset.UTC;",
            "",
            "import java.time.Clock;",
            "import java.time.Duration;",
            "import java.time.Instant;",
            "import java.time.LocalDate;",
            "import java.time.LocalDateTime;",
            "import java.time.LocalTime;",
            "",
            "class A {",
            "  void m() {",
            "    Clock clock = Clock.fixed(Instant.EPOCH, UTC);",
            "    clock.instant();",
            "    clock.millis();",
            "    Clock.offset(clock, Duration.ZERO);",
            "    Clock.tick(clock, Duration.ZERO);",
            "",
            "    // BUG: Diagnostic matches: X",
            "    Clock.systemUTC();",
            "    // BUG: Diagnostic matches: X",
            "    Clock.systemDefaultZone();",
            "    // BUG: Diagnostic matches: X",
            "    Clock.system(UTC);",
            "    // BUG: Diagnostic matches: X",
            "    Clock.tickMillis(UTC);",
            "    // BUG: Diagnostic matches: X",
            "    Clock.tickMinutes(UTC);",
            "    // BUG: Diagnostic matches: X",
            "    Clock.tickSeconds(UTC);",
            "    // BUG: Diagnostic matches: X",
            "    clock.getZone();",
            "    // BUG: Diagnostic matches: X",
            "    clock.withZone(UTC);",
            "",
            "    // BUG: Diagnostic matches: X",
            "    Instant.now();",
            "    // This is equivalent to `clock.instant()`, which is fine.",
            "    Instant.now(clock);",
            "",
            "    // BUG: Diagnostic matches: X",
            "    LocalDate.now();",
            "    // BUG: Diagnostic matches: X",
            "    LocalDate.now(clock);",
            "    // BUG: Diagnostic matches: X",
            "    LocalDate.now(UTC);",
            "",
            "    // BUG: Diagnostic matches: X",
            "    LocalDateTime.now();",
            "    // BUG: Diagnostic matches: X",
            "    LocalDateTime.now(clock);",
            "    // BUG: Diagnostic matches: X",
            "    LocalDateTime.now(UTC);",
            "",
            "    // BUG: Diagnostic matches: X",
            "    LocalTime.now();",
            "    // BUG: Diagnostic matches: X",
            "    LocalTime.now(clock);",
            "    // BUG: Diagnostic matches: X",
            "    LocalTime.now(UTC);",
            "  }",
            "}")
        .doTest();
  }
}
