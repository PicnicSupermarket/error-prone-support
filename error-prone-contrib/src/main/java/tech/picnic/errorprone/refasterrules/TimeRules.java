package tech.picnic.errorprone.refasterrules;

import static java.time.ZoneOffset.UTC;

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
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with time. */
@OnlineDocumentation
final class TimeRules {
  private TimeRules() {}

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
          UTC.normalized(),
          ZoneId.from(UTC));
    }

    @AfterTemplate
    ZoneOffset after() {
      return UTC;
    }
  }

  static final class InstantToLocalDate {
    @BeforeTemplate
    LocalDate before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalDate(),
          instant.atZone(zoneId).toOffsetDateTime().toLocalDate(),
          LocalDateTime.ofInstant(instant, zoneId).toLocalDate());
    }

    @BeforeTemplate
    LocalDate before(Instant instant, ZoneOffset zoneId) {
      return instant.atOffset(zoneId).toLocalDate();
    }

    @AfterTemplate
    LocalDate after(Instant instant, ZoneId zoneId) {
      return LocalDate.ofInstant(instant, zoneId);
    }
  }

  static final class InstantToLocalDateTime {
    @BeforeTemplate
    LocalDateTime before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalDateTime(),
          instant.atZone(zoneId).toOffsetDateTime().toLocalDateTime());
    }

    @BeforeTemplate
    LocalDateTime before(Instant instant, ZoneOffset zoneId) {
      return instant.atOffset(zoneId).toLocalDateTime();
    }

    @AfterTemplate
    LocalDateTime after(Instant instant, ZoneId zoneId) {
      return LocalDateTime.ofInstant(instant, zoneId);
    }
  }

  static final class InstantToLocalTime {
    @BeforeTemplate
    LocalTime before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalTime(),
          instant.atZone(zoneId).toOffsetDateTime().toLocalTime(),
          LocalDateTime.ofInstant(instant, zoneId).toLocalTime());
    }

    @BeforeTemplate
    LocalTime before(Instant instant, ZoneOffset zoneId) {
      return instant.atOffset(zoneId).toLocalTime();
    }

    @AfterTemplate
    LocalTime after(Instant instant, ZoneId zoneId) {
      return LocalTime.ofInstant(instant, zoneId);
    }
  }

  /** Prefer {@link Instant#atOffset(ZoneOffset)} over the more verbose alternative. */
  static final class InstantToOffsetDateTime {
    @BeforeTemplate
    OffsetDateTime before(Instant instant, ZoneOffset zoneOffset) {
      return OffsetDateTime.ofInstant(instant, zoneOffset);
    }

    @AfterTemplate
    OffsetDateTime after(Instant instant, ZoneOffset zoneOffset) {
      return instant.atOffset(zoneOffset);
    }
  }

  static final class InstantToOffsetTime {
    @BeforeTemplate
    OffsetTime before(Instant instant, ZoneId zoneId) {
      return instant.atZone(zoneId).toOffsetDateTime().toOffsetTime();
    }

    @BeforeTemplate
    OffsetTime before(Instant instant, ZoneOffset zoneId) {
      return instant.atOffset(zoneId).toOffsetTime();
    }

    @AfterTemplate
    OffsetTime after(Instant instant, ZoneId zoneId) {
      return OffsetTime.ofInstant(instant, zoneId);
    }
  }

  /** Prefer {@link Instant#atZone(ZoneId)} over the more verbose alternative. */
  static final class InstantToZonedDateTime {
    @BeforeTemplate
    ZonedDateTime before(Instant instant, ZoneId zoneId) {
      return ZonedDateTime.ofInstant(instant, zoneId);
    }

    @AfterTemplate
    ZonedDateTime after(Instant instant, ZoneId zoneId) {
      return instant.atZone(zoneId);
    }
  }

  /** Use {@link Clock#systemUTC()} when possible. */
  static final class UtcClock {
    @BeforeTemplate
    @SuppressWarnings("TimeZoneUsage")
    Clock before() {
      return Clock.system(UTC);
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

  /** Prefer {@link Duration#ofDays(long)} over alternative representations. */
  static final class DurationOfDays {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.DAYS);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofDays(amount);
    }
  }

  /** Prefer {@link Duration#ofHours(long)} over alternative representations. */
  static final class DurationOfHours {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.HOURS);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofHours(amount);
    }
  }

  /** Prefer {@link Duration#ofMillis(long)} over alternative representations. */
  static final class DurationOfMillis {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.MILLIS);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofMillis(amount);
    }
  }

  /** Prefer {@link Duration#ofMinutes(long)} over alternative representations. */
  static final class DurationOfMinutes {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofMinutes(amount);
    }
  }

  /** Prefer {@link Duration#ofNanos(long)} over alternative representations. */
  static final class DurationOfNanos {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.NANOS);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofNanos(amount);
    }
  }

  /** Prefer {@link Duration#ofSeconds(long)} over alternative representations. */
  static final class DurationOfSeconds {
    @BeforeTemplate
    Duration before(long amount) {
      return Duration.of(amount, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    Duration after(long amount) {
      return Duration.ofSeconds(amount);
    }
  }

  /**
   * Don't unnecessarily convert to and from milliseconds. (This way nanosecond precision is
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
   * Don't unnecessarily convert to and from milliseconds. (This way nanosecond precision is
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
