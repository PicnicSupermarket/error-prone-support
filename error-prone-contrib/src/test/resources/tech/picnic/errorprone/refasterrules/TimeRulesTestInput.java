package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
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
import java.time.temporal.ChronoUnit;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class TimeRulesTest implements RefasterRuleCollectionTestCase {
  private static final ZonedDateTime ZONED_DATE_TIME = Instant.EPOCH.atZone(ZoneOffset.UTC);

  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ChronoUnit.class);
  }

  Instant testClockInstant() {
    return Instant.now(Clock.systemUTC());
  }

  ImmutableSet<ZoneId> testUtcConstant() {
    return ImmutableSet.of(
        ZoneId.of("GMT"),
        ZoneId.of("UTC"),
        ZoneId.of("+0"),
        ZoneId.of("-0"),
        ZoneOffset.UTC,
        ZoneId.from(ZoneOffset.UTC));
  }

  ImmutableSet<LocalDate> testLocalDateOfInstant() {
    return ImmutableSet.of(
        Instant.EPOCH.atZone(ZoneId.of("Europe/Amsterdam")).toLocalDate(),
        Instant.EPOCH.atOffset(ZoneOffset.UTC).toLocalDate(),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")).toLocalDate(),
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.MIN).toLocalDate());
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeOfInstant() {
    return ImmutableSet.of(
        Instant.EPOCH.atZone(ZoneId.of("Europe/Amsterdam")).toLocalDateTime(),
        Instant.EPOCH.atOffset(ZoneOffset.UTC).toLocalDateTime(),
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")).toLocalDateTime());
  }

  ImmutableSet<LocalTime> testLocalTimeOfInstant() {
    return ImmutableSet.of(
        Instant.EPOCH.atZone(ZoneId.of("Europe/Amsterdam")).toLocalTime(),
        Instant.EPOCH.atOffset(ZoneOffset.UTC).toLocalTime(),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")).toLocalTime(),
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.MIN).toLocalTime(),
        OffsetTime.ofInstant(Instant.EPOCH, ZoneOffset.MAX).toLocalTime());
  }

  OffsetDateTime testOffsetDateTimeOfInstant() {
    return Instant.EPOCH.atZone(ZoneOffset.UTC).toOffsetDateTime();
  }

  OffsetDateTime testInstantAtOffset() {
    return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
  }

  ImmutableSet<OffsetTime> testOffsetTimeOfInstant() {
    return ImmutableSet.of(
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")).toOffsetTime(),
        Instant.EPOCH.atOffset(ZoneOffset.UTC).toOffsetTime());
  }

  ZonedDateTime testInstantAtZone() {
    return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
  }

  Clock testUtcClock() {
    return Clock.system(ZoneOffset.UTC);
  }

  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(
        Instant.ofEpochMilli(0),
        Instant.ofEpochMilli(0L),
        Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(0, 0));
  }

  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) < 0, Instant.MIN.compareTo(Instant.MAX) >= 0);
  }

  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) > 0, Instant.MIN.compareTo(Instant.MAX) <= 0);
  }

  ImmutableSet<LocalTime> testLocalTimeMin() {
    return ImmutableSet.of(
        LocalTime.MIDNIGHT,
        LocalTime.of(0, 0),
        LocalTime.of(0, 0, 0),
        LocalTime.of(0, 0, 0, 0),
        LocalTime.ofNanoOfDay(0),
        LocalTime.ofSecondOfDay(0));
  }

  LocalDateTime testLocalDateAtStartOfDay() {
    return LocalDate.EPOCH.atTime(LocalTime.MIN);
  }

  ImmutableSet<Boolean> testChronoLocalDateIsBefore() {
    return ImmutableSet.of(
        LocalDate.MIN.compareTo(LocalDate.MAX) < 0, LocalDate.MIN.compareTo(LocalDate.MAX) >= 0);
  }

  ImmutableSet<Boolean> testChronoLocalDateIsAfter() {
    return ImmutableSet.of(
        LocalDate.MIN.compareTo(LocalDate.MAX) > 0, LocalDate.MIN.compareTo(LocalDate.MAX) <= 0);
  }

  ImmutableSet<Boolean> testChronoLocalDateTimeIsBefore() {
    return ImmutableSet.of(
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) < 0,
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) >= 0);
  }

  ImmutableSet<Boolean> testChronoLocalDateTimeIsAfter() {
    return ImmutableSet.of(
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) > 0,
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) <= 0);
  }

  ImmutableSet<Boolean> testChronoZonedDateTimeIsBefore() {
    return ImmutableSet.of(
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) < 0,
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) >= 0);
  }

  ImmutableSet<Boolean> testChronoZonedDateTimeIsAfter() {
    return ImmutableSet.of(
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) > 0,
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) <= 0);
  }

  ImmutableSet<Boolean> testOffsetDateTimeIsAfter() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) > 0,
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) <= 0);
  }

  ImmutableSet<Boolean> testOffsetDateTimeIsBefore() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) < 0,
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) >= 0);
  }

  ImmutableSet<Duration> testZeroDuration() {
    return ImmutableSet.of(
        Duration.ofNanos(0),
        Duration.ofMillis(0),
        Duration.ofSeconds(0),
        Duration.ofSeconds(0, 0),
        Duration.ofMinutes(0),
        Duration.ofHours(0),
        Duration.ofDays(0),
        Duration.of(0, ChronoUnit.MILLIS));
  }

  Duration testDurationOfDays() {
    return Duration.of(1, ChronoUnit.DAYS);
  }

  Duration testDurationOfHours() {
    return Duration.of(1, ChronoUnit.HOURS);
  }

  Duration testDurationOfMillis() {
    return Duration.of(1, ChronoUnit.MILLIS);
  }

  Duration testDurationOfMinutes() {
    return Duration.of(1, ChronoUnit.MINUTES);
  }

  Duration testDurationOfNanos() {
    return Duration.of(1, ChronoUnit.NANOS);
  }

  Duration testDurationOfSeconds() {
    return Duration.of(1, ChronoUnit.SECONDS);
  }

  Duration testDurationBetweenInstants() {
    return Duration.ofMillis(Instant.MAX.toEpochMilli() - Instant.MIN.toEpochMilli());
  }

  Duration testDurationBetweenOffsetDateTimes() {
    return Duration.between(OffsetDateTime.MIN.toInstant(), OffsetDateTime.MAX.toInstant())
        .plus(
            Duration.ofSeconds(
                OffsetDateTime.MAX.toEpochSecond() - OffsetDateTime.MIN.toEpochSecond()));
  }

  ImmutableSet<Boolean> testDurationIsZero() {
    return ImmutableSet.of(
        Duration.ofDays(1).equals(Duration.ZERO), Duration.ZERO.equals(Duration.ofDays(2)));
  }

  ImmutableSet<Period> testZeroPeriod() {
    return ImmutableSet.of(
        Period.ofDays(0),
        Period.ofWeeks(0),
        Period.ofMonths(0),
        Period.ofYears(0),
        Period.of(0, 0, 0));
  }

  ImmutableSet<LocalDate> testLocalDatePlusDays() {
    return ImmutableSet.of(
        LocalDate.EPOCH.plus(1L, ChronoUnit.DAYS), LocalDate.EPOCH.plus(Period.ofDays(1)));
  }

  ImmutableSet<LocalDate> testLocalDatePlusWeeks() {
    return ImmutableSet.of(
        LocalDate.EPOCH.plus(1L, ChronoUnit.WEEKS), LocalDate.EPOCH.plus(Period.ofWeeks(1)));
  }

  ImmutableSet<LocalDate> testLocalDatePlusMonths() {
    return ImmutableSet.of(
        LocalDate.EPOCH.plus(1L, ChronoUnit.MONTHS), LocalDate.EPOCH.plus(Period.ofMonths(1)));
  }

  ImmutableSet<LocalDate> testLocalDatePlusYears() {
    return ImmutableSet.of(
        LocalDate.EPOCH.plus(1L, ChronoUnit.YEARS), LocalDate.EPOCH.plus(Period.ofYears(1)));
  }

  ImmutableSet<LocalDate> testLocalDateMinusDays() {
    return ImmutableSet.of(
        LocalDate.EPOCH.minus(1L, ChronoUnit.DAYS), LocalDate.EPOCH.minus(Period.ofDays(1)));
  }

  ImmutableSet<LocalDate> testLocalDateMinusWeeks() {
    return ImmutableSet.of(
        LocalDate.EPOCH.minus(1L, ChronoUnit.WEEKS), LocalDate.EPOCH.minus(Period.ofWeeks(1)));
  }

  ImmutableSet<LocalDate> testLocalDateMinusMonths() {
    return ImmutableSet.of(
        LocalDate.EPOCH.minus(1L, ChronoUnit.MONTHS), LocalDate.EPOCH.minus(Period.ofMonths(1)));
  }

  ImmutableSet<LocalDate> testLocalDateMinusYears() {
    return ImmutableSet.of(
        LocalDate.EPOCH.minus(1L, ChronoUnit.YEARS), LocalDate.EPOCH.minus(Period.ofYears(1)));
  }

  ImmutableSet<LocalTime> testLocalTimePlusNanos() {
    return ImmutableSet.of(
        LocalTime.NOON.plus(1L, ChronoUnit.NANOS), LocalTime.NOON.plus(Duration.ofNanos(1)));
  }

  ImmutableSet<LocalTime> testLocalTimePlusSeconds() {
    return ImmutableSet.of(
        LocalTime.NOON.plus(1L, ChronoUnit.SECONDS), LocalTime.NOON.plus(Duration.ofSeconds(1)));
  }

  ImmutableSet<LocalTime> testLocalTimePlusMinutes() {
    return ImmutableSet.of(
        LocalTime.NOON.plus(1L, ChronoUnit.MINUTES), LocalTime.NOON.plus(Duration.ofMinutes(1)));
  }

  ImmutableSet<LocalTime> testLocalTimePlusHours() {
    return ImmutableSet.of(
        LocalTime.NOON.plus(1L, ChronoUnit.HOURS), LocalTime.NOON.plus(Duration.ofHours(1)));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusNanos() {
    return ImmutableSet.of(
        LocalTime.NOON.minus(1L, ChronoUnit.NANOS), LocalTime.NOON.minus(Duration.ofNanos(1)));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusSeconds() {
    return ImmutableSet.of(
        LocalTime.NOON.minus(1L, ChronoUnit.SECONDS), LocalTime.NOON.minus(Duration.ofSeconds(1)));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusMinutes() {
    return ImmutableSet.of(
        LocalTime.NOON.minus(1L, ChronoUnit.MINUTES), LocalTime.NOON.minus(Duration.ofMinutes(1)));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusHours() {
    return ImmutableSet.of(
        LocalTime.NOON.minus(1L, ChronoUnit.HOURS), LocalTime.NOON.minus(Duration.ofHours(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusNanos() {
    return ImmutableSet.of(
        OffsetTime.MIN.plus(1L, ChronoUnit.NANOS), OffsetTime.MIN.plus(Duration.ofNanos(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusSeconds() {
    return ImmutableSet.of(
        OffsetTime.MIN.plus(1L, ChronoUnit.SECONDS), OffsetTime.MIN.plus(Duration.ofSeconds(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusMinutes() {
    return ImmutableSet.of(
        OffsetTime.MIN.plus(1L, ChronoUnit.MINUTES), OffsetTime.MIN.plus(Duration.ofMinutes(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusHours() {
    return ImmutableSet.of(
        OffsetTime.MIN.plus(1L, ChronoUnit.HOURS), OffsetTime.MIN.plus(Duration.ofHours(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusNanos() {
    return ImmutableSet.of(
        OffsetTime.MAX.minus(1L, ChronoUnit.NANOS), OffsetTime.MAX.minus(Duration.ofNanos(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusSeconds() {
    return ImmutableSet.of(
        OffsetTime.MAX.minus(1L, ChronoUnit.SECONDS), OffsetTime.MAX.minus(Duration.ofSeconds(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusMinutes() {
    return ImmutableSet.of(
        OffsetTime.MAX.minus(1L, ChronoUnit.MINUTES), OffsetTime.MAX.minus(Duration.ofMinutes(1)));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusHours() {
    return ImmutableSet.of(
        OffsetTime.MAX.minus(1L, ChronoUnit.HOURS), OffsetTime.MAX.minus(Duration.ofHours(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusNanos() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.NANOS), LocalDateTime.MIN.plus(Duration.ofNanos(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusSeconds() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.SECONDS),
        LocalDateTime.MIN.plus(Duration.ofSeconds(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusMinutes() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.MINUTES),
        LocalDateTime.MIN.plus(Duration.ofMinutes(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusHours() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.HOURS), LocalDateTime.MIN.plus(Duration.ofHours(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusDays() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.DAYS), LocalDateTime.MIN.plus(Period.ofDays(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusWeeks() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.WEEKS), LocalDateTime.MIN.plus(Period.ofWeeks(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusMonths() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.MONTHS), LocalDateTime.MIN.plus(Period.ofMonths(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusYears() {
    return ImmutableSet.of(
        LocalDateTime.MIN.plus(1L, ChronoUnit.YEARS), LocalDateTime.MIN.plus(Period.ofYears(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusNanos() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.NANOS),
        LocalDateTime.MAX.minus(Duration.ofNanos(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusSeconds() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.SECONDS),
        LocalDateTime.MAX.minus(Duration.ofSeconds(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusMinutes() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.MINUTES),
        LocalDateTime.MAX.minus(Duration.ofMinutes(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusHours() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.HOURS),
        LocalDateTime.MAX.minus(Duration.ofHours(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusDays() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.DAYS), LocalDateTime.MAX.minus(Period.ofDays(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusWeeks() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.WEEKS), LocalDateTime.MAX.minus(Period.ofWeeks(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusMonths() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.MONTHS),
        LocalDateTime.MAX.minus(Period.ofMonths(1)));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusYears() {
    return ImmutableSet.of(
        LocalDateTime.MAX.minus(1L, ChronoUnit.YEARS), LocalDateTime.MAX.minus(Period.ofYears(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusNanos() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.NANOS),
        OffsetDateTime.MIN.plus(Duration.ofNanos(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusSeconds() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.SECONDS),
        OffsetDateTime.MIN.plus(Duration.ofSeconds(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusMinutes() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.MINUTES),
        OffsetDateTime.MIN.plus(Duration.ofMinutes(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusHours() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.HOURS),
        OffsetDateTime.MIN.plus(Duration.ofHours(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusDays() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.DAYS), OffsetDateTime.MIN.plus(Period.ofDays(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusWeeks() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.WEEKS), OffsetDateTime.MIN.plus(Period.ofWeeks(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusMonths() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.MONTHS),
        OffsetDateTime.MIN.plus(Period.ofMonths(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusYears() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.plus(1L, ChronoUnit.YEARS), OffsetDateTime.MIN.plus(Period.ofYears(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusNanos() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.NANOS),
        OffsetDateTime.MAX.minus(Duration.ofNanos(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusSeconds() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.SECONDS),
        OffsetDateTime.MAX.minus(Duration.ofSeconds(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusMinutes() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.MINUTES),
        OffsetDateTime.MAX.minus(Duration.ofMinutes(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusHours() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.HOURS),
        OffsetDateTime.MAX.minus(Duration.ofHours(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusDays() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.DAYS), OffsetDateTime.MAX.minus(Period.ofDays(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusWeeks() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.WEEKS),
        OffsetDateTime.MAX.minus(Period.ofWeeks(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusMonths() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.MONTHS),
        OffsetDateTime.MAX.minus(Period.ofMonths(1)));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusYears() {
    return ImmutableSet.of(
        OffsetDateTime.MAX.minus(1L, ChronoUnit.YEARS),
        OffsetDateTime.MAX.minus(Period.ofYears(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusNanos() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.NANOS), ZONED_DATE_TIME.plus(Duration.ofNanos(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusSeconds() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.SECONDS), ZONED_DATE_TIME.plus(Duration.ofSeconds(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusMinutes() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.MINUTES), ZONED_DATE_TIME.plus(Duration.ofMinutes(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusHours() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.HOURS), ZONED_DATE_TIME.plus(Duration.ofHours(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusDays() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.DAYS), ZONED_DATE_TIME.plus(Period.ofDays(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusWeeks() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.WEEKS), ZONED_DATE_TIME.plus(Period.ofWeeks(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusMonths() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.MONTHS), ZONED_DATE_TIME.plus(Period.ofMonths(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusYears() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.plus(1L, ChronoUnit.YEARS), ZONED_DATE_TIME.plus(Period.ofYears(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusNanos() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.NANOS), ZONED_DATE_TIME.minus(Duration.ofNanos(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusSeconds() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.SECONDS),
        ZONED_DATE_TIME.minus(Duration.ofSeconds(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusMinutes() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.MINUTES),
        ZONED_DATE_TIME.minus(Duration.ofMinutes(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusHours() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.HOURS), ZONED_DATE_TIME.minus(Duration.ofHours(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusDays() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.DAYS), ZONED_DATE_TIME.minus(Period.ofDays(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusWeeks() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.WEEKS), ZONED_DATE_TIME.minus(Period.ofWeeks(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusMonths() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.MONTHS), ZONED_DATE_TIME.minus(Period.ofMonths(1)));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusYears() {
    return ImmutableSet.of(
        ZONED_DATE_TIME.minus(1L, ChronoUnit.YEARS), ZONED_DATE_TIME.minus(Period.ofYears(1)));
  }
}
