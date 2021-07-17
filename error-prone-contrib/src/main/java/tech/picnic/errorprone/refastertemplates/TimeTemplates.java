package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.TemporalUnit;

/** Refaster templates related to expressions dealing with time. */
final class TimeTemplates {
  private TimeTemplates() {}

  /**
   * Prefer {@link Clock#instant()} over {@link Instant#now(Clock)}, as it is more concise and more
   * "OOP-py".
   */
  static final class ClockInstant {
    @BeforeTemplate
    Instant before(Clock clock) {
      return Instant.now(clock);
    }

    @AfterTemplate
    Instant after(Clock clock) {
      return clock.instant();
    }
  }

  /** Use {@link ZoneOffset#UTC} when possible. */
  static final class UtcConstant {
    @BeforeTemplate
    ZoneId before() {
      // `ZoneId.of("Z")` is not listed, because Error Prone flags it out of the box.
      return Refaster.anyOf(
          ZoneId.of("GMT"),
          ZoneId.of("UTC"),
          ZoneId.of("+0"),
          ZoneId.of("-0"),
          ZoneOffset.UTC.normalized(),
          ZoneId.from(ZoneOffset.UTC));
    }

    @AfterTemplate
    ZoneOffset after() {
      return ZoneOffset.UTC;
    }
  }

  /** Use {@link Clock#systemUTC()} when possible. */
  static final class UtcClock {
    @BeforeTemplate
    @SuppressWarnings("TimeZoneUsage")
    Clock before() {
      return Clock.system(ZoneOffset.UTC);
    }

    @AfterTemplate
    @SuppressWarnings("TimeZoneUsage")
    Clock after() {
      return Clock.systemUTC();
    }
  }

  /** Prefer {@link Instant#EPOCH} over alternative representations. */
  static final class EpochInstant {
    @BeforeTemplate
    Instant before() {
      return Refaster.anyOf(
          Instant.ofEpochMilli(0), Instant.ofEpochSecond(0), Instant.ofEpochSecond(0, 0));
    }

    @AfterTemplate
    Instant after() {
      return Instant.EPOCH;
    }
  }

  /**
   * Prefer {@link Instant#isBefore(Instant)} over explicit comparison, as it yields more readable
   * code.
   */
  static final class InstantIsBefore {
    @BeforeTemplate
    boolean before(Instant a, Instant b) {
      return a.compareTo(b) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Instant a, Instant b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link Instant#isBefore(Instant)} over explicit comparison, as it yields more readable
   * code.
   */
  static final class InstantIsAfter {
    @BeforeTemplate
    boolean before(Instant a, Instant b) {
      return a.compareTo(b) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Instant a, Instant b) {
      return a.isAfter(b);
    }
  }

  /** Prefer the {@link LocalTime#MIN} over alternative representations. */
  static final class LocalTimeMin {
    @BeforeTemplate
    LocalTime before() {
      return Refaster.anyOf(
          LocalTime.MIDNIGHT,
          LocalTime.of(0, 0),
          LocalTime.of(0, 0, 0),
          LocalTime.of(0, 0, 0, 0),
          LocalTime.ofNanoOfDay(0),
          LocalTime.ofSecondOfDay(0));
    }

    @AfterTemplate
    LocalTime after() {
      return LocalTime.MIN;
    }
  }

  /** Prefer {@link LocalDate#atStartOfDay()} over more contrived alternatives. */
  static final class LocalDateAtStartOfDay {
    @BeforeTemplate
    LocalDateTime before(LocalDate localDate) {
      return localDate.atTime(LocalTime.MIN);
    }

    @AfterTemplate
    LocalDateTime after(LocalDate localDate) {
      return localDate.atStartOfDay();
    }
  }

  /**
   * Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class ChronoLocalDateIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDate a, ChronoLocalDate b) {
      return a.compareTo(b) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDate a, ChronoLocalDate b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class ChronoLocalDateIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDate a, ChronoLocalDate b) {
      return a.compareTo(b) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDate a, ChronoLocalDate b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoLocalDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.compareTo(b) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoLocalDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.compareTo(b) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoZonedDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.compareTo(b) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoZonedDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.compareTo(b) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class OffsetDateTimeIsBefore {
    @BeforeTemplate
    boolean before(OffsetDateTime a, OffsetDateTime b) {
      return a.compareTo(b) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(OffsetDateTime a, OffsetDateTime b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class OffsetDateTimeIsAfter {
    @BeforeTemplate
    boolean before(OffsetDateTime a, OffsetDateTime b) {
      return a.compareTo(b) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(OffsetDateTime a, OffsetDateTime b) {
      return a.isAfter(b);
    }
  }

  static final class ZeroDuration {
    @BeforeTemplate
    Duration before(TemporalUnit temporalUnit) {
      return Refaster.anyOf(
          Duration.ofNanos(0),
          Duration.ofMillis(0),
          Duration.ofSeconds(0),
          Duration.ofSeconds(0, 0),
          Duration.ofMinutes(0),
          Duration.ofHours(0),
          Duration.ofDays(0),
          Duration.of(0, temporalUnit));
    }

    @AfterTemplate
    Duration after() {
      return Duration.ZERO;
    }
  }

  /**
   * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
   * retained.)
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenInstants {
    @BeforeTemplate
    Duration before(Instant a, Instant b) {
      return Duration.ofMillis(b.toEpochMilli() - a.toEpochMilli());
    }

    @AfterTemplate
    Duration after(Instant a, Instant b) {
      return Duration.between(a, b);
    }
  }

  /**
   * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
   * retained.)
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenOffsetDateTimes {
    @BeforeTemplate
    Duration before(OffsetDateTime a, OffsetDateTime b) {
      return Refaster.anyOf(
          Duration.between(a.toInstant(), b.toInstant()),
          Duration.ofSeconds(b.toEpochSecond() - a.toEpochSecond()));
    }

    @AfterTemplate
    Duration after(OffsetDateTime a, OffsetDateTime b) {
      return Duration.between(a, b);
    }
  }

  /** Prefer {@link Duration#isZero()} over more contrived alternatives. */
  static final class DurationIsZero {
    @BeforeTemplate
    boolean before(Duration duration) {
      return Refaster.anyOf(duration.equals(Duration.ZERO), Duration.ZERO.equals(duration));
    }

    @AfterTemplate
    boolean after(Duration duration) {
      return duration.isZero();
    }
  }

  static final class ZeroPeriod {
    @BeforeTemplate
    Period before() {
      return Refaster.anyOf(
          Period.ofDays(0),
          Period.ofWeeks(0),
          Period.ofMonths(0),
          Period.ofYears(0),
          Period.of(0, 0, 0));
    }

    @AfterTemplate
    Period after() {
      return Period.ZERO;
    }
  }
}
