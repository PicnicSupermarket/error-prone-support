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

  /** Prefer {@link Clock#instant()} over more verbose alternatives. */
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

  /** Prefer {@link ZoneOffset#UTC} over less explicit alternatives. */
  static final class Utc {
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

  /** Prefer {@link LocalDate#ofInstant(Instant, ZoneId)} over more contrived alternatives. */
  static final class LocalDateOfInstant {
    @BeforeTemplate
    LocalDate before(Instant instant, ZoneId zone) {
      return Refaster.anyOf(
          instant.atZone(zone).toLocalDate(),
          LocalDateTime.ofInstant(instant, zone).toLocalDate(),
          OffsetDateTime.ofInstant(instant, zone).toLocalDate());
    }

    @BeforeTemplate
    LocalDate before(Instant instant, ZoneOffset zone) {
      return instant.atOffset(zone).toLocalDate();
    }

    @AfterTemplate
    LocalDate after(Instant instant, ZoneId zone) {
      return LocalDate.ofInstant(instant, zone);
    }
  }

  /** Prefer {@link LocalDateTime#ofInstant(Instant, ZoneId)} over more contrived alternatives. */
  static final class LocalDateTimeOfInstant {
    @BeforeTemplate
    LocalDateTime before(Instant instant, ZoneId zone) {
      return Refaster.anyOf(
          instant.atZone(zone).toLocalDateTime(),
          OffsetDateTime.ofInstant(instant, zone).toLocalDateTime());
    }

    @BeforeTemplate
    LocalDateTime before(Instant instant, ZoneOffset zone) {
      return instant.atOffset(zone).toLocalDateTime();
    }

    @AfterTemplate
    LocalDateTime after(Instant instant, ZoneId zone) {
      return LocalDateTime.ofInstant(instant, zone);
    }
  }

  /** Prefer {@link LocalTime#ofInstant(Instant, ZoneId)} over more contrived alternatives. */
  static final class LocalTimeOfInstant {
    @BeforeTemplate
    LocalTime before(Instant instant, ZoneId zone) {
      return Refaster.anyOf(
          instant.atZone(zone).toLocalTime(),
          LocalDateTime.ofInstant(instant, zone).toLocalTime(),
          OffsetDateTime.ofInstant(instant, zone).toLocalTime(),
          OffsetTime.ofInstant(instant, zone).toLocalTime());
    }

    @BeforeTemplate
    LocalTime before(Instant instant, ZoneOffset zone) {
      return instant.atOffset(zone).toLocalTime();
    }

    @AfterTemplate
    LocalTime after(Instant instant, ZoneId zone) {
      return LocalTime.ofInstant(instant, zone);
    }
  }

  /** Prefer {@link OffsetDateTime#ofInstant(Instant, ZoneId)} over more contrived alternatives. */
  static final class OffsetDateTimeOfInstant {
    @BeforeTemplate
    OffsetDateTime before(Instant instant, ZoneId zone) {
      return instant.atZone(zone).toOffsetDateTime();
    }

    @AfterTemplate
    OffsetDateTime after(Instant instant, ZoneId zone) {
      return OffsetDateTime.ofInstant(instant, zone);
    }
  }

  /** Prefer using {@link Instant}s as-is over less efficient alternatives. */
  static final class InstantIdentity {
    @BeforeTemplate
    Instant before(Instant instant, TemporalUnit unit) {
      return Refaster.anyOf(
          instant.plus(Duration.ZERO),
          instant.plus(0, unit),
          instant.plusNanos(0),
          instant.plusMillis(0),
          instant.plusSeconds(0),
          instant.minus(Duration.ZERO),
          instant.minus(0, unit),
          instant.minusNanos(0),
          instant.minusMillis(0),
          instant.minusSeconds(0),
          Instant.parse(instant.toString()),
          instant.truncatedTo(ChronoUnit.NANOS),
          Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNano()));
    }

    @AfterTemplate
    Instant after(Instant instant) {
      return instant;
    }
  }

  /**
   * Prefer {@link Instant#truncatedTo(TemporalUnit)} over less efficient alternatives.
   *
   * <p>Note that {@link Instant#toEpochMilli()} throws an {@link ArithmeticException} for dates
   * very far in the past or future, while the suggested alternative doesn't.
   */
  static final class InstantTruncatedToChronoUnitMillis {
    @BeforeTemplate
    Instant before(Instant instant) {
      return Instant.ofEpochMilli(instant.toEpochMilli());
    }

    @AfterTemplate
    Instant after(Instant instant) {
      return instant.truncatedTo(ChronoUnit.MILLIS);
    }
  }

  /** Prefer {@link Instant#truncatedTo(TemporalUnit)} over less efficient alternatives. */
  static final class InstantTruncatedToChronoUnitSeconds {
    @BeforeTemplate
    Instant before(Instant instant) {
      return Instant.ofEpochSecond(instant.getEpochSecond());
    }

    @AfterTemplate
    Instant after(Instant instant) {
      return instant.truncatedTo(ChronoUnit.SECONDS);
    }
  }

  /** Prefer {@link Instant#atOffset(ZoneOffset)} over more verbose alternatives. */
  static final class InstantAtOffset {
    @BeforeTemplate
    OffsetDateTime before(Instant instant, ZoneOffset offset) {
      return OffsetDateTime.ofInstant(instant, offset);
    }

    @AfterTemplate
    OffsetDateTime after(Instant instant, ZoneOffset offset) {
      return instant.atOffset(offset);
    }
  }

  /** Prefer {@link OffsetTime#ofInstant(Instant, ZoneId)} over more contrived alternatives. */
  static final class OffsetTimeOfInstant {
    @BeforeTemplate
    OffsetTime before(Instant instant, ZoneId zone) {
      return OffsetDateTime.ofInstant(instant, zone).toOffsetTime();
    }

    @BeforeTemplate
    OffsetTime before(Instant instant, ZoneOffset zone) {
      return instant.atOffset(zone).toOffsetTime();
    }

    @AfterTemplate
    OffsetTime after(Instant instant, ZoneId zone) {
      return OffsetTime.ofInstant(instant, zone);
    }
  }

  /** Prefer {@link Instant#atZone(ZoneId)} over more verbose alternatives. */
  static final class InstantAtZone {
    @BeforeTemplate
    ZonedDateTime before(Instant instant, ZoneId zone) {
      return ZonedDateTime.ofInstant(instant, zone);
    }

    @AfterTemplate
    ZonedDateTime after(Instant instant, ZoneId zone) {
      return instant.atZone(zone);
    }
  }

  /** Prefer {@link Clock#systemUTC()} over more verbose alternatives. */
  static final class ClockSystemUTC {
    @BeforeTemplate
    @SuppressWarnings("TimeZoneUsage" /* This violation will be rewritten. */)
    Clock before() {
      return Clock.system(UTC);
    }

    @AfterTemplate
    @SuppressWarnings("TimeZoneUsage" /* This violation is preferred over the alternative. */)
    Clock after() {
      return Clock.systemUTC();
    }
  }

  /** Prefer {@link Instant#EPOCH} over less efficient alternatives. */
  static final class InstantEpoch {
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

  /** Prefer {@link Instant#isBefore(Instant)} over less explicit alternatives. */
  static final class InstantIsBefore {
    @BeforeTemplate
    boolean before(Instant instant, Instant otherInstant) {
      return instant.compareTo(otherInstant) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Instant instant, Instant otherInstant) {
      return instant.isBefore(otherInstant);
    }
  }

  /** Prefer {@link Instant#isAfter(Instant)} over less explicit alternatives. */
  static final class InstantIsAfter {
    @BeforeTemplate
    boolean before(Instant instant, Instant otherInstant) {
      return instant.compareTo(otherInstant) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Instant instant, Instant otherInstant) {
      return instant.isAfter(otherInstant);
    }
  }

  /** Prefer {@link LocalTime#MIN} over less explicit alternatives. */
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
    LocalDateTime before(LocalDate date) {
      return date.atTime(LocalTime.MIN);
    }

    @AfterTemplate
    LocalDateTime after(LocalDate date) {
      return date.atStartOfDay();
    }
  }

  /** Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over less explicit alternatives. */
  static final class ChronoLocalDateIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDate date, ChronoLocalDate other) {
      return date.compareTo(other) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDate date, ChronoLocalDate other) {
      return date.isBefore(other);
    }
  }

  /** Prefer {@link ChronoLocalDate#isAfter(ChronoLocalDate)} over less explicit alternatives. */
  static final class ChronoLocalDateIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDate date, ChronoLocalDate other) {
      return date.compareTo(other) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDate date, ChronoLocalDate other) {
      return date.isAfter(other);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over less explicit
   * alternatives.
   */
  static final class ChronoLocalDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> time, ChronoLocalDateTime<?> other) {
      return time.compareTo(other) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDateTime<?> time, ChronoLocalDateTime<?> other) {
      return time.isBefore(other);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isAfter(ChronoLocalDateTime)} over less explicit
   * alternatives.
   */
  static final class ChronoLocalDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> time, ChronoLocalDateTime<?> other) {
      return time.compareTo(other) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoLocalDateTime<?> time, ChronoLocalDateTime<?> other) {
      return time.isAfter(other);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over less explicit
   * alternatives.
   */
  static final class ChronoZonedDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> time, ChronoZonedDateTime<?> other) {
      return time.compareTo(other) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoZonedDateTime<?> time, ChronoZonedDateTime<?> other) {
      return time.isBefore(other);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isAfter(ChronoZonedDateTime)} over less explicit
   * alternatives.
   */
  static final class ChronoZonedDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> time, ChronoZonedDateTime<?> other) {
      return time.compareTo(other) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(ChronoZonedDateTime<?> time, ChronoZonedDateTime<?> other) {
      return time.isAfter(other);
    }
  }

  /** Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over less explicit alternatives. */
  static final class OffsetDateTimeIsBefore {
    @BeforeTemplate
    boolean before(OffsetDateTime time, OffsetDateTime other) {
      return time.compareTo(other) < 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(OffsetDateTime time, OffsetDateTime other) {
      return time.isBefore(other);
    }
  }

  /** Prefer {@link OffsetDateTime#isAfter(OffsetDateTime)} over less explicit alternatives. */
  static final class OffsetDateTimeIsAfter {
    @BeforeTemplate
    boolean before(OffsetDateTime time, OffsetDateTime other) {
      return time.compareTo(other) > 0;
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(OffsetDateTime time, OffsetDateTime other) {
      return time.isAfter(other);
    }
  }

  /** Prefer {@link Duration#ZERO} over less explicit alternatives. */
  static final class DurationZero {
    @BeforeTemplate
    Duration before(TemporalUnit unit) {
      return Refaster.anyOf(
          Duration.ofNanos(0),
          Duration.ofMillis(0),
          Duration.ofSeconds(0),
          Duration.ofSeconds(0, 0),
          Duration.ofMinutes(0),
          Duration.ofHours(0),
          Duration.ofDays(0),
          Duration.of(0, unit));
    }

    @AfterTemplate
    Duration after() {
      return Duration.ZERO;
    }
  }

  /** Prefer {@link Duration#ofDays(long)} over more contrived alternatives. */
  static final class DurationOfDays {
    @BeforeTemplate
    Duration before(long days) {
      return Duration.of(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    Duration after(long days) {
      return Duration.ofDays(days);
    }
  }

  /** Prefer {@link Duration#ofHours(long)} over more contrived alternatives. */
  static final class DurationOfHours {
    @BeforeTemplate
    Duration before(long hours) {
      return Duration.of(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    Duration after(long hours) {
      return Duration.ofHours(hours);
    }
  }

  /** Prefer {@link Duration#ofMillis(long)} over more contrived alternatives. */
  static final class DurationOfMillis {
    @BeforeTemplate
    Duration before(long millis) {
      return Duration.of(millis, ChronoUnit.MILLIS);
    }

    @AfterTemplate
    Duration after(long millis) {
      return Duration.ofMillis(millis);
    }
  }

  /** Prefer {@link Duration#ofMinutes(long)} over more contrived alternatives. */
  static final class DurationOfMinutes {
    @BeforeTemplate
    Duration before(long minutes) {
      return Duration.of(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    Duration after(long minutes) {
      return Duration.ofMinutes(minutes);
    }
  }

  /** Prefer {@link Duration#ofNanos(long)} over more contrived alternatives. */
  static final class DurationOfNanos {
    @BeforeTemplate
    Duration before(long nanos) {
      return Duration.of(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    Duration after(long nanos) {
      return Duration.ofNanos(nanos);
    }
  }

  /** Prefer {@link Duration#ofSeconds(long)} over more contrived alternatives. */
  static final class DurationOfSeconds {
    @BeforeTemplate
    Duration before(long seconds) {
      return Duration.of(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    Duration after(long seconds) {
      return Duration.ofSeconds(seconds);
    }
  }

  /**
   * Prefer {@link Duration#between} over less efficient alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenInstant {
    @BeforeTemplate
    Duration before(Instant startInclusive, Instant endExclusive) {
      return Duration.ofMillis(endExclusive.toEpochMilli() - startInclusive.toEpochMilli());
    }

    @AfterTemplate
    Duration after(Instant startInclusive, Instant endExclusive) {
      return Duration.between(startInclusive, endExclusive);
    }
  }

  /**
   * Prefer {@link Duration#between} over less efficient alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenOffsetDateTime {
    @BeforeTemplate
    Duration before(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
      return Refaster.anyOf(
          Duration.between(startInclusive.toInstant(), endExclusive.toInstant()),
          Duration.ofSeconds(endExclusive.toEpochSecond() - startInclusive.toEpochSecond()));
    }

    @AfterTemplate
    Duration after(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
      return Duration.between(startInclusive, endExclusive);
    }
  }

  /** Prefer {@link Duration#isZero()} over more contrived alternatives. */
  static final class DurationIsZero {
    @BeforeTemplate
    boolean before(Duration other) {
      return Refaster.anyOf(other.equals(Duration.ZERO), Duration.ZERO.equals(other));
    }

    @AfterTemplate
    boolean after(Duration other) {
      return other.isZero();
    }
  }

  /** Prefer {@link Period#ZERO} over less explicit alternatives. */
  static final class PeriodZero {
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
    LocalDate before(LocalDate date, int daysToAdd) {
      return date.plus(Period.ofDays(daysToAdd));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long daysToAdd) {
      return date.plus(daysToAdd, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int daysToAdd) {
      return date.plusDays(daysToAdd);
    }
  }

  /** Prefer {@link LocalDate#plusWeeks(long)} over more contrived alternatives. */
  static final class LocalDatePlusWeeks {
    @BeforeTemplate
    LocalDate before(LocalDate date, int weeksToAdd) {
      return date.plus(Period.ofWeeks(weeksToAdd));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long weeksToAdd) {
      return date.plus(weeksToAdd, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int weeksToAdd) {
      return date.plusWeeks(weeksToAdd);
    }
  }

  /** Prefer {@link LocalDate#plusMonths(long)} over more contrived alternatives. */
  static final class LocalDatePlusMonths {
    @BeforeTemplate
    LocalDate before(LocalDate date, int monthsToAdd) {
      return date.plus(Period.ofMonths(monthsToAdd));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long monthsToAdd) {
      return date.plus(monthsToAdd, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int monthsToAdd) {
      return date.plusMonths(monthsToAdd);
    }
  }

  /** Prefer {@link LocalDate#plusYears(long)} over more contrived alternatives. */
  static final class LocalDatePlusYears {
    @BeforeTemplate
    LocalDate before(LocalDate date, int yearsToAdd) {
      return date.plus(Period.ofYears(yearsToAdd));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long yearsToAdd) {
      return date.plus(yearsToAdd, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int yearsToAdd) {
      return date.plusYears(yearsToAdd);
    }
  }

  /** Prefer {@link LocalDate#minusDays(long)} over more contrived alternatives. */
  static final class LocalDateMinusDays {
    @BeforeTemplate
    LocalDate before(LocalDate date, int daysToSubtract) {
      return date.minus(Period.ofDays(daysToSubtract));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long daysToSubtract) {
      return date.minus(daysToSubtract, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int daysToSubtract) {
      return date.minusDays(daysToSubtract);
    }
  }

  /** Prefer {@link LocalDate#minusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateMinusWeeks {
    @BeforeTemplate
    LocalDate before(LocalDate date, int weeksToSubtract) {
      return date.minus(Period.ofWeeks(weeksToSubtract));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long weeksToSubtract) {
      return date.minus(weeksToSubtract, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int weeksToSubtract) {
      return date.minusWeeks(weeksToSubtract);
    }
  }

  /** Prefer {@link LocalDate#minusMonths(long)} over more contrived alternatives. */
  static final class LocalDateMinusMonths {
    @BeforeTemplate
    LocalDate before(LocalDate date, int monthsToSubtract) {
      return date.minus(Period.ofMonths(monthsToSubtract));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long monthsToSubtract) {
      return date.minus(monthsToSubtract, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int monthsToSubtract) {
      return date.minusMonths(monthsToSubtract);
    }
  }

  /** Prefer {@link LocalDate#minusYears(long)} over more contrived alternatives. */
  static final class LocalDateMinusYears {
    @BeforeTemplate
    LocalDate before(LocalDate date, int yearsToSubtract) {
      return date.minus(Period.ofYears(yearsToSubtract));
    }

    @BeforeTemplate
    LocalDate before(LocalDate date, long yearsToSubtract) {
      return date.minus(yearsToSubtract, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDate after(LocalDate date, int yearsToSubtract) {
      return date.minusYears(yearsToSubtract);
    }
  }

  /** Prefer {@link LocalTime#plusNanos(long)} over more contrived alternatives. */
  static final class LocalTimePlusNanos {
    @BeforeTemplate
    LocalTime before(LocalTime time, int nanosToAdd) {
      return time.plus(Duration.ofNanos(nanosToAdd));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long nanosToAdd) {
      return time.plus(nanosToAdd, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int nanosToAdd) {
      return time.plusNanos(nanosToAdd);
    }
  }

  /** Prefer {@link LocalTime#plusSeconds(long)} over more contrived alternatives. */
  static final class LocalTimePlusSeconds {
    @BeforeTemplate
    LocalTime before(LocalTime time, int secondstoAdd) {
      return time.plus(Duration.ofSeconds(secondstoAdd));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long secondstoAdd) {
      return time.plus(secondstoAdd, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int secondstoAdd) {
      return time.plusSeconds(secondstoAdd);
    }
  }

  /** Prefer {@link LocalTime#plusMinutes(long)} over more contrived alternatives. */
  static final class LocalTimePlusMinutes {
    @BeforeTemplate
    LocalTime before(LocalTime time, int minutesToAdd) {
      return time.plus(Duration.ofMinutes(minutesToAdd));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long minutesToAdd) {
      return time.plus(minutesToAdd, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int minutesToAdd) {
      return time.plusMinutes(minutesToAdd);
    }
  }

  /** Prefer {@link LocalTime#plusHours(long)} over more contrived alternatives. */
  static final class LocalTimePlusHours {
    @BeforeTemplate
    LocalTime before(LocalTime time, int hoursToAdd) {
      return time.plus(Duration.ofHours(hoursToAdd));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long hoursToAdd) {
      return time.plus(hoursToAdd, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int hoursToAdd) {
      return time.plusHours(hoursToAdd);
    }
  }

  /** Prefer {@link LocalTime#minusNanos(long)} over more contrived alternatives. */
  static final class LocalTimeMinusNanos {
    @BeforeTemplate
    LocalTime before(LocalTime time, int nanosToSubtract) {
      return time.minus(Duration.ofNanos(nanosToSubtract));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long nanosToSubtract) {
      return time.minus(nanosToSubtract, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int nanosToSubtract) {
      return time.minusNanos(nanosToSubtract);
    }
  }

  /** Prefer {@link LocalTime#minusSeconds(long)} over more contrived alternatives. */
  static final class LocalTimeMinusSeconds {
    @BeforeTemplate
    LocalTime before(LocalTime time, int secondsToSubtract) {
      return time.minus(Duration.ofSeconds(secondsToSubtract));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long secondsToSubtract) {
      return time.minus(secondsToSubtract, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int secondsToSubtract) {
      return time.minusSeconds(secondsToSubtract);
    }
  }

  /** Prefer {@link LocalTime#minusMinutes(long)} over more contrived alternatives. */
  static final class LocalTimeMinusMinutes {
    @BeforeTemplate
    LocalTime before(LocalTime time, int minutesToSubtract) {
      return time.minus(Duration.ofMinutes(minutesToSubtract));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long minutesToSubtract) {
      return time.minus(minutesToSubtract, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int minutesToSubtract) {
      return time.minusMinutes(minutesToSubtract);
    }
  }

  /** Prefer {@link LocalTime#minusHours(long)} over more contrived alternatives. */
  static final class LocalTimeMinusHours {
    @BeforeTemplate
    LocalTime before(LocalTime time, int hoursToSubtract) {
      return time.minus(Duration.ofHours(hoursToSubtract));
    }

    @BeforeTemplate
    LocalTime before(LocalTime time, long hoursToSubtract) {
      return time.minus(hoursToSubtract, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalTime after(LocalTime time, int hoursToSubtract) {
      return time.minusHours(hoursToSubtract);
    }
  }

  /** Prefer {@link OffsetTime#plusNanos(long)} over more contrived alternatives. */
  static final class OffsetTimePlusNanos {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int nanos) {
      return time.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long nanos) {
      return time.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int nanos) {
      return time.plusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetTime#plusSeconds(long)} over more contrived alternatives. */
  static final class OffsetTimePlusSeconds {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int seconds) {
      return time.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long seconds) {
      return time.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int seconds) {
      return time.plusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetTime#plusMinutes(long)} over more contrived alternatives. */
  static final class OffsetTimePlusMinutes {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int minutes) {
      return time.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long minutes) {
      return time.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int minutes) {
      return time.plusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetTime#plusHours(long)} over more contrived alternatives. */
  static final class OffsetTimePlusHours {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int hours) {
      return time.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long hours) {
      return time.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int hours) {
      return time.plusHours(hours);
    }
  }

  /** Prefer {@link OffsetTime#minusNanos(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusNanos {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int nanos) {
      return time.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long nanos) {
      return time.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int nanos) {
      return time.minusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetTime#minusSeconds(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusSeconds {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int seconds) {
      return time.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long seconds) {
      return time.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int seconds) {
      return time.minusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetTime#minusMinutes(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusMinutes {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int minutes) {
      return time.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long minutes) {
      return time.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int minutes) {
      return time.minusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetTime#minusHours(long)} over more contrived alternatives. */
  static final class OffsetTimeMinusHours {
    @BeforeTemplate
    OffsetTime before(OffsetTime time, int hours) {
      return time.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetTime before(OffsetTime time, long hours) {
      return time.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetTime after(OffsetTime time, int hours) {
      return time.minusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusNanos {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int nanos) {
      return time.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long nanos) {
      return time.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int nanos) {
      return time.plusNanos(nanos);
    }
  }

  /** Prefer {@link LocalDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusSeconds {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int seconds) {
      return time.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long seconds) {
      return time.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int seconds) {
      return time.plusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusMinutes {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int minutes) {
      return time.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long minutes) {
      return time.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int minutes) {
      return time.plusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalDateTime#plusHours(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusHours {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int hours) {
      return time.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long hours) {
      return time.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int hours) {
      return time.plusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#plusDays(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusDays {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int days) {
      return time.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long days) {
      return time.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int days) {
      return time.plusDays(days);
    }
  }

  /** Prefer {@link LocalDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusWeeks {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int weeks) {
      return time.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long weeks) {
      return time.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int weeks) {
      return time.plusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusMonths {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int months) {
      return time.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long months) {
      return time.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int months) {
      return time.plusMonths(months);
    }
  }

  /** Prefer {@link LocalDateTime#plusYears(long)} over more contrived alternatives. */
  static final class LocalDateTimePlusYears {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int years) {
      return time.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long years) {
      return time.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int years) {
      return time.plusYears(years);
    }
  }

  /** Prefer {@link LocalDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusNanos {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int nanos) {
      return time.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long nanos) {
      return time.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int nanos) {
      return time.minusNanos(nanos);
    }
  }

  /** Prefer {@link LocalDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusSeconds {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int seconds) {
      return time.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long seconds) {
      return time.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int seconds) {
      return time.minusSeconds(seconds);
    }
  }

  /** Prefer {@link LocalDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusMinutes {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int minutes) {
      return time.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long minutes) {
      return time.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int minutes) {
      return time.minusMinutes(minutes);
    }
  }

  /** Prefer {@link LocalDateTime#minusHours(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusHours {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int hours) {
      return time.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long hours) {
      return time.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int hours) {
      return time.minusHours(hours);
    }
  }

  /** Prefer {@link LocalDateTime#minusDays(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusDays {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int days) {
      return time.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long days) {
      return time.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int days) {
      return time.minusDays(days);
    }
  }

  /** Prefer {@link LocalDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusWeeks {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int weeks) {
      return time.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long weeks) {
      return time.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int weeks) {
      return time.minusWeeks(weeks);
    }
  }

  /** Prefer {@link LocalDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusMonths {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int months) {
      return time.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long months) {
      return time.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int months) {
      return time.minusMonths(months);
    }
  }

  /** Prefer {@link LocalDateTime#minusYears(long)} over more contrived alternatives. */
  static final class LocalDateTimeMinusYears {
    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, int years) {
      return time.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    LocalDateTime before(LocalDateTime time, long years) {
      return time.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    LocalDateTime after(LocalDateTime time, int years) {
      return time.minusYears(years);
    }
  }

  /** Prefer {@link OffsetDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusNanos {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int nanos) {
      return time.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long nanos) {
      return time.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int nanos) {
      return time.plusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusSeconds {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int seconds) {
      return time.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long seconds) {
      return time.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int seconds) {
      return time.plusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusMinutes {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int minutes) {
      return time.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long minutes) {
      return time.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int minutes) {
      return time.plusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetDateTime#plusHours(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusHours {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int hours) {
      return time.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long hours) {
      return time.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int hours) {
      return time.plusHours(hours);
    }
  }

  /** Prefer {@link OffsetDateTime#plusDays(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusDays {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int days) {
      return time.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long days) {
      return time.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int days) {
      return time.plusDays(days);
    }
  }

  /** Prefer {@link OffsetDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusWeeks {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int weeks) {
      return time.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long weeks) {
      return time.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int weeks) {
      return time.plusWeeks(weeks);
    }
  }

  /** Prefer {@link OffsetDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusMonths {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int months) {
      return time.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long months) {
      return time.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int months) {
      return time.plusMonths(months);
    }
  }

  /** Prefer {@link OffsetDateTime#plusYears(long)} over more contrived alternatives. */
  static final class OffsetDateTimePlusYears {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int years) {
      return time.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long years) {
      return time.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int years) {
      return time.plusYears(years);
    }
  }

  /** Prefer {@link OffsetDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusNanos {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int nanos) {
      return time.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long nanos) {
      return time.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int nanos) {
      return time.minusNanos(nanos);
    }
  }

  /** Prefer {@link OffsetDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusSeconds {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int seconds) {
      return time.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long seconds) {
      return time.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int seconds) {
      return time.minusSeconds(seconds);
    }
  }

  /** Prefer {@link OffsetDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusMinutes {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int minutes) {
      return time.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long minutes) {
      return time.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int minutes) {
      return time.minusMinutes(minutes);
    }
  }

  /** Prefer {@link OffsetDateTime#minusHours(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusHours {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int hours) {
      return time.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long hours) {
      return time.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int hours) {
      return time.minusHours(hours);
    }
  }

  /** Prefer {@link OffsetDateTime#minusDays(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusDays {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int days) {
      return time.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long days) {
      return time.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int days) {
      return time.minusDays(days);
    }
  }

  /** Prefer {@link OffsetDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusWeeks {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int weeks) {
      return time.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long weeks) {
      return time.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int weeks) {
      return time.minusWeeks(weeks);
    }
  }

  /** Prefer {@link OffsetDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusMonths {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int months) {
      return time.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long months) {
      return time.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int months) {
      return time.minusMonths(months);
    }
  }

  /** Prefer {@link OffsetDateTime#minusYears(long)} over more contrived alternatives. */
  static final class OffsetDateTimeMinusYears {
    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, int years) {
      return time.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    OffsetDateTime before(OffsetDateTime time, long years) {
      return time.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    OffsetDateTime after(OffsetDateTime time, int years) {
      return time.minusYears(years);
    }
  }

  /** Prefer {@link ZonedDateTime#plusNanos(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusNanos {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int nanos) {
      return time.plus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long nanos) {
      return time.plus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int nanos) {
      return time.plusNanos(nanos);
    }
  }

  /** Prefer {@link ZonedDateTime#plusSeconds(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusSeconds {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int seconds) {
      return time.plus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long seconds) {
      return time.plus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int seconds) {
      return time.plusSeconds(seconds);
    }
  }

  /** Prefer {@link ZonedDateTime#plusMinutes(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusMinutes {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int minutes) {
      return time.plus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long minutes) {
      return time.plus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int minutes) {
      return time.plusMinutes(minutes);
    }
  }

  /** Prefer {@link ZonedDateTime#plusHours(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusHours {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int hours) {
      return time.plus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long hours) {
      return time.plus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int hours) {
      return time.plusHours(hours);
    }
  }

  /** Prefer {@link ZonedDateTime#plusDays(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusDays {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int days) {
      return time.plus(Period.ofDays(days));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long days) {
      return time.plus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int days) {
      return time.plusDays(days);
    }
  }

  /** Prefer {@link ZonedDateTime#plusWeeks(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusWeeks {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int weeks) {
      return time.plus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long weeks) {
      return time.plus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int weeks) {
      return time.plusWeeks(weeks);
    }
  }

  /** Prefer {@link ZonedDateTime#plusMonths(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusMonths {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int months) {
      return time.plus(Period.ofMonths(months));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long months) {
      return time.plus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int months) {
      return time.plusMonths(months);
    }
  }

  /** Prefer {@link ZonedDateTime#plusYears(long)} over more contrived alternatives. */
  static final class ZonedDateTimePlusYears {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int years) {
      return time.plus(Period.ofYears(years));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long years) {
      return time.plus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int years) {
      return time.plusYears(years);
    }
  }

  /** Prefer {@link ZonedDateTime#minusNanos(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusNanos {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int nanos) {
      return time.minus(Duration.ofNanos(nanos));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long nanos) {
      return time.minus(nanos, ChronoUnit.NANOS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int nanos) {
      return time.minusNanos(nanos);
    }
  }

  /** Prefer {@link ZonedDateTime#minusSeconds(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusSeconds {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int seconds) {
      return time.minus(Duration.ofSeconds(seconds));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long seconds) {
      return time.minus(seconds, ChronoUnit.SECONDS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int seconds) {
      return time.minusSeconds(seconds);
    }
  }

  /** Prefer {@link ZonedDateTime#minusMinutes(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusMinutes {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int minutes) {
      return time.minus(Duration.ofMinutes(minutes));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long minutes) {
      return time.minus(minutes, ChronoUnit.MINUTES);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int minutes) {
      return time.minusMinutes(minutes);
    }
  }

  /** Prefer {@link ZonedDateTime#minusHours(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusHours {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int hours) {
      return time.minus(Duration.ofHours(hours));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long hours) {
      return time.minus(hours, ChronoUnit.HOURS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int hours) {
      return time.minusHours(hours);
    }
  }

  /** Prefer {@link ZonedDateTime#minusDays(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusDays {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int days) {
      return time.minus(Period.ofDays(days));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long days) {
      return time.minus(days, ChronoUnit.DAYS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int days) {
      return time.minusDays(days);
    }
  }

  /** Prefer {@link ZonedDateTime#minusWeeks(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusWeeks {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int weeks) {
      return time.minus(Period.ofWeeks(weeks));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long weeks) {
      return time.minus(weeks, ChronoUnit.WEEKS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int weeks) {
      return time.minusWeeks(weeks);
    }
  }

  /** Prefer {@link ZonedDateTime#minusMonths(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusMonths {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int months) {
      return time.minus(Period.ofMonths(months));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long months) {
      return time.minus(months, ChronoUnit.MONTHS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int months) {
      return time.minusMonths(months);
    }
  }

  /** Prefer {@link ZonedDateTime#minusYears(long)} over more contrived alternatives. */
  static final class ZonedDateTimeMinusYears {
    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, int years) {
      return time.minus(Period.ofYears(years));
    }

    @BeforeTemplate
    ZonedDateTime before(ZonedDateTime time, long years) {
      return time.minus(years, ChronoUnit.YEARS);
    }

    @AfterTemplate
    ZonedDateTime after(ZonedDateTime time, int years) {
      return time.minusYears(years);
    }
  }
}
