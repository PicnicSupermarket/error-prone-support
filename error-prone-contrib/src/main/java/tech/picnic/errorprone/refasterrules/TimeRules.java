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

  /** Prefer {@link LocalDate#ofInstant(Instant, ZoneId)} over more indirect alternatives. */
  static final class LocalDateOfInstant {
    @BeforeTemplate
    LocalDate before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalDate(),
          LocalDateTime.ofInstant(instant, zoneId).toLocalDate(),
          OffsetDateTime.ofInstant(instant, zoneId).toLocalDate());
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

  /** Prefer {@link LocalDateTime#ofInstant(Instant, ZoneId)} over more indirect alternatives. */
  static final class LocalDateTimeOfInstant {
    @BeforeTemplate
    LocalDateTime before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalDateTime(),
          OffsetDateTime.ofInstant(instant, zoneId).toLocalDateTime());
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

  /** Prefer {@link LocalTime#ofInstant(Instant, ZoneId)} over more indirect alternatives. */
  static final class LocalTimeOfInstant {
    @BeforeTemplate
    LocalTime before(Instant instant, ZoneId zoneId) {
      return Refaster.anyOf(
          instant.atZone(zoneId).toLocalTime(),
          LocalDateTime.ofInstant(instant, zoneId).toLocalTime(),
          OffsetDateTime.ofInstant(instant, zoneId).toLocalTime(),
          OffsetTime.ofInstant(instant, zoneId).toLocalTime());
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

  /** Prefer {@link OffsetDateTime#ofInstant(Instant, ZoneId)} over more indirect alternatives. */
  static final class OffsetDateTimeOfInstant {
    @BeforeTemplate
    OffsetDateTime before(Instant instant, ZoneId zoneId) {
      return instant.atZone(zoneId).toOffsetDateTime();
    }

    @AfterTemplate
    OffsetDateTime after(Instant instant, ZoneId zoneId) {
      return OffsetDateTime.ofInstant(instant, zoneId);
    }
  }

  /** Prefer {@link Instant#atOffset(ZoneOffset)} over more verbose alternatives. */
  static final class InstantAtOffset {
    @BeforeTemplate
    OffsetDateTime before(Instant instant, ZoneOffset zoneOffset) {
      return OffsetDateTime.ofInstant(instant, zoneOffset);
    }

    @AfterTemplate
    OffsetDateTime after(Instant instant, ZoneOffset zoneOffset) {
      return instant.atOffset(zoneOffset);
    }
  }

  /** Prefer {@link OffsetTime#ofInstant(Instant, ZoneId)} over more indirect alternatives. */
  static final class OffsetTimeOfInstant {
    @BeforeTemplate
    OffsetTime before(Instant instant, ZoneId zoneId) {
      return OffsetDateTime.ofInstant(instant, zoneId).toOffsetTime();
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

  /** Prefer {@link Instant#atZone(ZoneId)} over more verbose alternatives. */
  static final class InstantAtZone {
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

  // XXX: The `DateType{Plus,Minus}Unit` rules below contain a lot of boilerplate. Consider
  // introducing an Error Prone check instead.

  /** Prefer {@link LocalDate#plusDays(long)} over more contrived alternatives. */
  static final class LocalDatePlusDays {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int days) {
      return localDate.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long days) {
      return localDate.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int days) {
      return localDate.plusDays(days);
    }
  }

  /** Prefer {@link LocalDate#plusWeeks(long)} over more contrived alternatives. */
  static final class LocalDatePlusWeeks {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int weeks) {
      return localDate.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long weeks) {
      return localDate.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int weeks) {
      return localDate.plusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDate#plusMonths(long)} over more contrived alternatives. */
  static final class LocalDatePlusMonths {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int months) {
      return localDate.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long months) {
      return localDate.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int months) {
      return localDate.plusMonths(months);
    }
  }

  /** Prefer {@link LocalDate#plusYears(long)} over more contrived alternatives. */
  static final class LocalDatePlusYears {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int years) {
      return localDate.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long years) {
      return localDate.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int years) {
      return localDate.plusYears(years);
    }
  }

  /** Prefer {@link LocalDate#minusDays(long)} over more contrived alternatives. */
  static final class LocalDateMinusDays {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int days) {
      return localDate.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long days) {
      return localDate.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int days) {
      return localDate.minusDays(days);
    }
  }

  /** Prefer {@link LocalDate#minusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateMinusWeeks {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int weeks) {
      return localDate.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long weeks) {
      return localDate.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int weeks) {
      return localDate.minusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDate#minusMonths(long)} over more contrived alternatives. */
  static final class LocalDateMinusMonths {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int months) {
      return localDate.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long months) {
      return localDate.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int months) {
      return localDate.minusMonths(months);
    }
  }

  /** Prefer {@link LocalDate#minusYears(long)} over more contrived alternatives. */
  static final class LocalDateMinusYears {
    @BeforeTemplate
    LocalDate before(LocalDate localDate, int years) {
      return localDate.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDate before(LocalDate localDate, long years) {
      return localDate.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDate after(LocalDate localDate, int years) {
      return localDate.minusYears(years);
    }
  }

  /** Prefer {@link LocalTime#plusNanos(long)} over more contrived alternatives. */
  static final class LocalTimePlusNanos {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int nanos) {
      return localTime.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long nanos) {
      return localTime.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int nanos) {
      return localTime.plusNanos(nanos);
    }
  }

  /** Prefer {@link LocalTime#plusSeconds(long)} over more contrived alternatives. */
  static final class LocalTimePlusSeconds {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int seconds) {
      return localTime.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long seconds) {
      return localTime.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int seconds) {
      return localTime.plusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalTime#plusMinutes(long)} over more contrived alternatives. */
  static final class LocalTimePlusMinutes {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int minutes) {
      return localTime.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long minutes) {
      return localTime.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int minutes) {
      return localTime.plusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalTime#plusHours(long)} over more contrived alternatives. */
  static final class LocalTimePlusHours {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int hours) {
      return localTime.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long hours) {
      return localTime.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int hours) {
      return localTime.plusHours(hours);
    }
  }

  /** Prefer {@link LocalTime#minusNanos(long)} over more contrived alternatives. */
  static final class LocalTimeMinusNanos {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int nanos) {
      return localTime.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long nanos) {
      return localTime.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int nanos) {
      return localTime.minusNanos(nanos);
    }
  }

  /** Prefer {@link LocalTime#minusSeconds(long)} over more contrived alternatives. */
  static final class LocalTimeMinusSeconds {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int seconds) {
      return localTime.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long seconds) {
      return localTime.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int seconds) {
      return localTime.minusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalTime#minusMinutes(long)} over more contrived alternatives. */
  static final class LocalTimeMinusMinutes {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int minutes) {
      return localTime.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long minutes) {
      return localTime.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int minutes) {
      return localTime.minusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalTime#minusHours(long)} over more contrived alternatives. */
  static final class LocalTimeMinusHours {
    @BeforeTemplate
    LocalTime before(LocalTime localTime, int hours) {
      return localTime.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalTime before(LocalTime localTime, long hours) {
      return localTime.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalTime after(LocalTime localTime, int hours) {
      return localTime.minusHours(hours);
    }
  }

  /** Prefer {@link OffsetTime#plusNanos(long)} over more contrived alternatives. */
  static final class OffsetTimePlusNanos {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int nanos) {
      return offsetTime.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long nanos) {
      return offsetTime.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int nanos) {
      return offsetTime.plusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetTime#plusSeconds(long)} over more contrived alternatives. */
  static final class OffsetTimePlusSeconds {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int seconds) {
      return offsetTime.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long seconds) {
      return offsetTime.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int seconds) {
      return offsetTime.plusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetTime#plusMinutes(long)} over more contrived alternatives. */
  static final class OffsetTimePlusMinutes {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int minutes) {
      return offsetTime.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long minutes) {
      return offsetTime.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int minutes) {
      return offsetTime.plusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetTime#plusHours(long)} over more contrived alternatives. */
  static final class OffsetTimePlusHours {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int hours) {
      return offsetTime.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long hours) {
      return offsetTime.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int hours) {
      return offsetTime.plusHours(hours);
    }
  }

  /** Prefer {@link OffsetTime#minusNanos(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusNanos {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int nanos) {
      return offsetTime.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long nanos) {
      return offsetTime.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int nanos) {
      return offsetTime.minusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetTime#minusSeconds(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusSeconds {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int seconds) {
      return offsetTime.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long seconds) {
      return offsetTime.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int seconds) {
      return offsetTime.minusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetTime#minusMinutes(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusMinutes {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int minutes) {
      return offsetTime.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long minutes) {
      return offsetTime.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int minutes) {
      return offsetTime.minusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetTime#minusHours(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusHours {
    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, int hours) {
      return offsetTime.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime offsetTime, long hours) {
      return offsetTime.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime offsetTime, int hours) {
      return offsetTime.minusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusNanos {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int nanos) {
      return localDateTime.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long nanos) {
      return localDateTime.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int nanos) {
      return localDateTime.plusNanos(nanos);
    }
  }

  /** Prefer {@link LocalDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusSeconds {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int seconds) {
      return localDateTime.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long seconds) {
      return localDateTime.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int seconds) {
      return localDateTime.plusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusMinutes {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int minutes) {
      return localDateTime.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long minutes) {
      return localDateTime.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int minutes) {
      return localDateTime.plusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalDateTime#plusHours(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusHours {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int hours) {
      return localDateTime.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long hours) {
      return localDateTime.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int hours) {
      return localDateTime.plusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#plusDays(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusDays {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int days) {
      return localDateTime.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long days) {
      return localDateTime.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int days) {
      return localDateTime.plusDays(days);
    }
  }

  /** Prefer {@link LocalDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusWeeks {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int weeks) {
      return localDateTime.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long weeks) {
      return localDateTime.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int weeks) {
      return localDateTime.plusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusMonths {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int months) {
      return localDateTime.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long months) {
      return localDateTime.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int months) {
      return localDateTime.plusMonths(months);
    }
  }

  /** Prefer {@link LocalDateTime#plusYears(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusYears {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int years) {
      return localDateTime.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long years) {
      return localDateTime.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int years) {
      return localDateTime.plusYears(years);
    }
  }

  /** Prefer {@link LocalDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusNanos {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int nanos) {
      return localDateTime.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long nanos) {
      return localDateTime.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int nanos) {
      return localDateTime.minusNanos(nanos);
    }
  }

  /** Prefer {@link LocalDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusSeconds {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int seconds) {
      return localDateTime.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long seconds) {
      return localDateTime.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int seconds) {
      return localDateTime.minusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusMinutes {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int minutes) {
      return localDateTime.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long minutes) {
      return localDateTime.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int minutes) {
      return localDateTime.minusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalDateTime#minusHours(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusHours {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int hours) {
      return localDateTime.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long hours) {
      return localDateTime.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int hours) {
      return localDateTime.minusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#minusDays(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusDays {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int days) {
      return localDateTime.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long days) {
      return localDateTime.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int days) {
      return localDateTime.minusDays(days);
    }
  }

  /** Prefer {@link LocalDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusWeeks {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int weeks) {
      return localDateTime.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long weeks) {
      return localDateTime.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int weeks) {
      return localDateTime.minusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusMonths {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int months) {
      return localDateTime.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long months) {
      return localDateTime.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int months) {
      return localDateTime.minusMonths(months);
    }
  }

  /** Prefer {@link LocalDateTime#minusYears(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusYears {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, int years) {
      return localDateTime.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime localDateTime, long years) {
      return localDateTime.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime localDateTime, int years) {
      return localDateTime.minusYears(years);
    }
  }

  /** Prefer {@link OffsetDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusNanos {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int nanos) {
      return offsetDateTime.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long nanos) {
      return offsetDateTime.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int nanos) {
      return offsetDateTime.plusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusSeconds {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int seconds) {
      return offsetDateTime.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long seconds) {
      return offsetDateTime.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int seconds) {
      return offsetDateTime.plusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusMinutes {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int minutes) {
      return offsetDateTime.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long minutes) {
      return offsetDateTime.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int minutes) {
      return offsetDateTime.plusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetDateTime#plusHours(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusHours {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int hours) {
      return offsetDateTime.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long hours) {
      return offsetDateTime.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int hours) {
      return offsetDateTime.plusHours(hours);
    }
  }

  /** Prefer {@link OffsetDateTime#plusDays(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusDays {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int days) {
      return offsetDateTime.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long days) {
      return offsetDateTime.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int days) {
      return offsetDateTime.plusDays(days);
    }
  }

  /** Prefer {@link OffsetDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusWeeks {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int weeks) {
      return offsetDateTime.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long weeks) {
      return offsetDateTime.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int weeks) {
      return offsetDateTime.plusWeeks(weeks);
    }
  }

  /** Prefer {@link OffsetDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusMonths {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int months) {
      return offsetDateTime.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long months) {
      return offsetDateTime.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int months) {
      return offsetDateTime.plusMonths(months);
    }
  }

  /** Prefer {@link OffsetDateTime#plusYears(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusYears {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int years) {
      return offsetDateTime.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long years) {
      return offsetDateTime.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int years) {
      return offsetDateTime.plusYears(years);
    }
  }

  /** Prefer {@link OffsetDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusNanos {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int nanos) {
      return offsetDateTime.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long nanos) {
      return offsetDateTime.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int nanos) {
      return offsetDateTime.minusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusSeconds {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int seconds) {
      return offsetDateTime.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long seconds) {
      return offsetDateTime.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int seconds) {
      return offsetDateTime.minusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusMinutes {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int minutes) {
      return offsetDateTime.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long minutes) {
      return offsetDateTime.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int minutes) {
      return offsetDateTime.minusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetDateTime#minusHours(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusHours {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int hours) {
      return offsetDateTime.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long hours) {
      return offsetDateTime.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int hours) {
      return offsetDateTime.minusHours(hours);
    }
  }

  /** Prefer {@link OffsetDateTime#minusDays(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusDays {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int days) {
      return offsetDateTime.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long days) {
      return offsetDateTime.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int days) {
      return offsetDateTime.minusDays(days);
    }
  }

  /** Prefer {@link OffsetDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusWeeks {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int weeks) {
      return offsetDateTime.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long weeks) {
      return offsetDateTime.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int weeks) {
      return offsetDateTime.minusWeeks(weeks);
    }
  }

  /** Prefer {@link OffsetDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusMonths {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int months) {
      return offsetDateTime.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long months) {
      return offsetDateTime.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int months) {
      return offsetDateTime.minusMonths(months);
    }
  }

  /** Prefer {@link OffsetDateTime#minusYears(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusYears {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, int years) {
      return offsetDateTime.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime offsetDateTime, long years) {
      return offsetDateTime.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime offsetDateTime, int years) {
      return offsetDateTime.minusYears(years);
    }
  }

  /** Prefer {@link ZonedDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusNanos {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int nanos) {
      return zonedDateTime.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long nanos) {
      return zonedDateTime.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int nanos) {
      return zonedDateTime.plusNanos(nanos);
    }
  }

  /** Prefer {@link ZonedDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusSeconds {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int seconds) {
      return zonedDateTime.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long seconds) {
      return zonedDateTime.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int seconds) {
      return zonedDateTime.plusSeconds(seconds);
    }
  }

  /** Prefer {@link ZonedDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusMinutes {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int minutes) {
      return zonedDateTime.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long minutes) {
      return zonedDateTime.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int minutes) {
      return zonedDateTime.plusMinutes(minutes);
    }
  }

  /** Prefer {@link ZonedDateTime#plusHours(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusHours {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int hours) {
      return zonedDateTime.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long hours) {
      return zonedDateTime.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int hours) {
      return zonedDateTime.plusHours(hours);
    }
  }

  /** Prefer {@link ZonedDateTime#plusDays(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusDays {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int days) {
      return zonedDateTime.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long days) {
      return zonedDateTime.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int days) {
      return zonedDateTime.plusDays(days);
    }
  }

  /** Prefer {@link ZonedDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusWeeks {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int weeks) {
      return zonedDateTime.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long weeks) {
      return zonedDateTime.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int weeks) {
      return zonedDateTime.plusWeeks(weeks);
    }
  }

  /** Prefer {@link ZonedDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusMonths {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int months) {
      return zonedDateTime.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long months) {
      return zonedDateTime.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int months) {
      return zonedDateTime.plusMonths(months);
    }
  }

  /** Prefer {@link ZonedDateTime#plusYears(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusYears {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int years) {
      return zonedDateTime.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long years) {
      return zonedDateTime.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int years) {
      return zonedDateTime.plusYears(years);
    }
  }

  /** Prefer {@link ZonedDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusNanos {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int nanos) {
      return zonedDateTime.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long nanos) {
      return zonedDateTime.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int nanos) {
      return zonedDateTime.minusNanos(nanos);
    }
  }

  /** Prefer {@link ZonedDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusSeconds {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int seconds) {
      return zonedDateTime.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long seconds) {
      return zonedDateTime.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int seconds) {
      return zonedDateTime.minusSeconds(seconds);
    }
  }

  /** Prefer {@link ZonedDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusMinutes {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int minutes) {
      return zonedDateTime.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long minutes) {
      return zonedDateTime.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int minutes) {
      return zonedDateTime.minusMinutes(minutes);
    }
  }

  /** Prefer {@link ZonedDateTime#minusHours(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusHours {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int hours) {
      return zonedDateTime.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long hours) {
      return zonedDateTime.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int hours) {
      return zonedDateTime.minusHours(hours);
    }
  }

  /** Prefer {@link ZonedDateTime#minusDays(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusDays {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int days) {
      return zonedDateTime.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long days) {
      return zonedDateTime.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int days) {
      return zonedDateTime.minusDays(days);
    }
  }

  /** Prefer {@link ZonedDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusWeeks {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int weeks) {
      return zonedDateTime.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long weeks) {
      return zonedDateTime.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int weeks) {
      return zonedDateTime.minusWeeks(weeks);
    }
  }

  /** Prefer {@link ZonedDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusMonths {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int months) {
      return zonedDateTime.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long months) {
      return zonedDateTime.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int months) {
      return zonedDateTime.minusMonths(months);
    }
  }

  /** Prefer {@link ZonedDateTime#minusYears(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusYears {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, int years) {
      return zonedDateTime.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime zonedDateTime, long years) {
      return zonedDateTime.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime zonedDateTime, int years) {
      return zonedDateTime.minusYears(years);
    }
  }
}
