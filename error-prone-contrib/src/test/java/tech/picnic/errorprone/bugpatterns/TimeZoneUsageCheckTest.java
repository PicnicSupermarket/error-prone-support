package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class TimeZoneUsageCheckTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(TimeZoneUsageCheck.class, getClass());

  @Test
  public void testNoIdentificationFoundCases() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "import static java.time.ZoneOffset.UTC;",
            "",
            "import java.time.Clock;",
            "import java.time.Duration;",
            "import java.time.Instant;",
            "",
            "class A {",
            "void m() {",
            "    Clock clock = Clock.fixed(Instant.EPOCH, UTC);",
            "clock.instant();",
            "clock.millis();",
            "Clock.offset(clock, Duration.ZERO);",
            "Clock.tick(clock, Duration.ZERO);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testIdentifyCases() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "import static java.time.ZoneOffset.UTC;",
            "",
            "import java.time.Clock;",
            "import java.time.Instant;",
            "import java.time.LocalDate;",
            "import java.time.LocalDateTime;",
            "import java.time.LocalTime;",
            "",
            "class A {",
            "void m() {",
            "    // BUG: Diagnostic contains:",
            "Clock.systemUTC();",
            "    // BUG: Diagnostic contains:",
            "Clock.systemDefaultZone();",
            "    // BUG: Diagnostic contains:",
            "Clock.system(UTC);",
            "    // BUG: Diagnostic contains:",
            "Clock.tickMillis(UTC);",
            "    // BUG: Diagnostic contains:",
            "Clock.tickMinutes(UTC);",
            "    // BUG: Diagnostic contains:",
            "Clock.tickSeconds(UTC);",
            "    Clock clock = Clock.fixed(Instant.EPOCH, UTC);",
            "    // BUG: Diagnostic contains:",
            "clock.getZone();",
            "    // BUG: Diagnostic contains:",
            "clock.withZone(UTC);",
            "    // BUG: Diagnostic contains:",
            "Instant.now();",
            "    // BUG: Diagnostic contains:",
            "Instant.now(clock);",
            "    // BUG: Diagnostic contains:",
            "LocalDate.now();",
            "    // BUG: Diagnostic contains:",
            "LocalDate.now(clock);",
            "    // BUG: Diagnostic contains:",
            "LocalDate.now(UTC);",
            "    // BUG: Diagnostic contains:",
            "LocalDateTime.now();",
            "    // BUG: Diagnostic contains:",
            "LocalDateTime.now(clock);",
            "    // BUG: Diagnostic contains:",
            "LocalDateTime.now(UTC);",
            "    // BUG: Diagnostic contains:",
            "LocalTime.now();",
            "    // BUG: Diagnostic contains:",
            "LocalTime.now(clock);",
            "    // BUG: Diagnostic contains:",
            "LocalTime.now(UTC);",
            "  }",
            "}")
        .doTest();
  }
}
