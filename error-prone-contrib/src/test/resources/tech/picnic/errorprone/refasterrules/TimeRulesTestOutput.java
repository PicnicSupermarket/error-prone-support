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
    return Clock.systemUTC().instant();
  }

  ImmutableSet<ZoneId> testUtcConstant() {
    return ImmutableSet.of(
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC);
  }

  ImmutableSet<LocalDate> testLocalDateOfInstant() {
    return ImmutableSet.of(
        LocalDate.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalDate.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        LocalDate.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")),
        LocalDate.ofInstant(Instant.EPOCH, ZoneOffset.MIN));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeOfInstant() {
    return ImmutableSet.of(
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")));
  }

  ImmutableSet<LocalTime> testLocalTimeOfInstant() {
    return ImmutableSet.of(
        LocalTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        LocalTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")),
        LocalTime.ofInstant(Instant.EPOCH, ZoneOffset.MIN),
        LocalTime.ofInstant(Instant.EPOCH, ZoneOffset.MAX));
  }

  OffsetDateTime testOffsetDateTimeOfInstant() {
    return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
  }

  OffsetDateTime testInstantAtOffset() {
    return Instant.EPOCH.atOffset(ZoneOffset.UTC);
  }

  ImmutableSet<OffsetTime> testOffsetTimeOfInstant() {
    return ImmutableSet.of(
        OffsetTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        OffsetTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
  }

  ZonedDateTime testInstantAtZone() {
    return Instant.EPOCH.atZone(ZoneOffset.UTC);
  }

  Clock testUtcClock() {
    return Clock.systemUTC();
  }

  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(Instant.EPOCH, Instant.EPOCH, Instant.EPOCH, Instant.EPOCH);
  }

  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(Instant.MIN.isBefore(Instant.MAX), !Instant.MIN.isBefore(Instant.MAX));
  }

  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(Instant.MIN.isAfter(Instant.MAX), !Instant.MIN.isAfter(Instant.MAX));
  }

  ImmutableSet<LocalTime> testLocalTimeMin() {
    return ImmutableSet.of(
        LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN);
  }

  LocalDateTime testLocalDateAtStartOfDay() {
    return LocalDate.EPOCH.atStartOfDay();
  }

  ImmutableSet<Boolean> testChronoLocalDateIsBefore() {
    return ImmutableSet.of(
        LocalDate.MIN.isBefore(LocalDate.MAX), !LocalDate.MIN.isBefore(LocalDate.MAX));
  }

  ImmutableSet<Boolean> testChronoLocalDateIsAfter() {
    return ImmutableSet.of(
        LocalDate.MIN.isAfter(LocalDate.MAX), !LocalDate.MIN.isAfter(LocalDate.MAX));
  }

  ImmutableSet<Boolean> testChronoLocalDateTimeIsBefore() {
    return ImmutableSet.of(
        LocalDateTime.MIN.isBefore(LocalDateTime.MAX),
        !LocalDateTime.MIN.isBefore(LocalDateTime.MAX));
  }

  ImmutableSet<Boolean> testChronoLocalDateTimeIsAfter() {
    return ImmutableSet.of(
        LocalDateTime.MIN.isAfter(LocalDateTime.MAX),
        !LocalDateTime.MIN.isAfter(LocalDateTime.MAX));
  }

  ImmutableSet<Boolean> testChronoZonedDateTimeIsBefore() {
    return ImmutableSet.of(
        ZonedDateTime.now().isBefore(ZonedDateTime.now()),
        !ZonedDateTime.now().isBefore(ZonedDateTime.now()));
  }

  ImmutableSet<Boolean> testChronoZonedDateTimeIsAfter() {
    return ImmutableSet.of(
        ZonedDateTime.now().isAfter(ZonedDateTime.now()),
        !ZonedDateTime.now().isAfter(ZonedDateTime.now()));
  }

  ImmutableSet<Boolean> testOffsetDateTimeIsAfter() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.isAfter(OffsetDateTime.MAX),
        !OffsetDateTime.MIN.isAfter(OffsetDateTime.MAX));
  }

  ImmutableSet<Boolean> testOffsetDateTimeIsBefore() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.isBefore(OffsetDateTime.MAX),
        !OffsetDateTime.MIN.isBefore(OffsetDateTime.MAX));
  }

  ImmutableSet<Duration> testZeroDuration() {
    return ImmutableSet.of(
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO,
        Duration.ZERO);
  }

  Duration testDurationOfDays() {
    return Duration.ofDays(1);
  }

  Duration testDurationOfHours() {
    return Duration.ofHours(1);
  }

  Duration testDurationOfMillis() {
    return Duration.ofMillis(1);
  }

  Duration testDurationOfMinutes() {
    return Duration.ofMinutes(1);
  }

  Duration testDurationOfNanos() {
    return Duration.ofNanos(1);
  }

  Duration testDurationOfSeconds() {
    return Duration.ofSeconds(1);
  }

  Duration testDurationBetweenInstants() {
    return Duration.between(Instant.MIN, Instant.MAX);
  }

  Duration testDurationBetweenOffsetDateTimes() {
    return Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX)
        .plus(Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX));
  }

  ImmutableSet<Boolean> testDurationIsZero() {
    return ImmutableSet.of(Duration.ofDays(1).isZero(), Duration.ofDays(2).isZero());
  }

  ImmutableSet<Period> testZeroPeriod() {
    return ImmutableSet.of(Period.ZERO, Period.ZERO, Period.ZERO, Period.ZERO, Period.ZERO);
  }

  ImmutableSet<LocalDate> testLocalDatePlusDays() {
    return ImmutableSet.of(LocalDate.EPOCH.plusDays(1L), LocalDate.EPOCH.plusDays(1));
  }

  ImmutableSet<LocalDate> testLocalDatePlusWeeks() {
    return ImmutableSet.of(LocalDate.EPOCH.plusWeeks(1L), LocalDate.EPOCH.plusWeeks(1));
  }

  ImmutableSet<LocalDate> testLocalDatePlusMonths() {
    return ImmutableSet.of(LocalDate.EPOCH.plusMonths(1L), LocalDate.EPOCH.plusMonths(1));
  }

  ImmutableSet<LocalDate> testLocalDatePlusYears() {
    return ImmutableSet.of(LocalDate.EPOCH.plusYears(1L), LocalDate.EPOCH.plusYears(1));
  }

  ImmutableSet<LocalDate> testLocalDateMinusDays() {
    return ImmutableSet.of(LocalDate.EPOCH.minusDays(1L), LocalDate.EPOCH.minusDays(1));
  }

  ImmutableSet<LocalDate> testLocalDateMinusWeeks() {
    return ImmutableSet.of(LocalDate.EPOCH.minusWeeks(1L), LocalDate.EPOCH.minusWeeks(1));
  }

  ImmutableSet<LocalDate> testLocalDateMinusMonths() {
    return ImmutableSet.of(LocalDate.EPOCH.minusMonths(1L), LocalDate.EPOCH.minusMonths(1));
  }

  ImmutableSet<LocalDate> testLocalDateMinusYears() {
    return ImmutableSet.of(LocalDate.EPOCH.minusYears(1L), LocalDate.EPOCH.minusYears(1));
  }

  ImmutableSet<LocalTime> testLocalTimePlusNanos() {
    return ImmutableSet.of(LocalTime.NOON.plusNanos(1L), LocalTime.NOON.plusNanos(1));
  }

  ImmutableSet<LocalTime> testLocalTimePlusSeconds() {
    return ImmutableSet.of(LocalTime.NOON.plusSeconds(1L), LocalTime.NOON.plusSeconds(1));
  }

  ImmutableSet<LocalTime> testLocalTimePlusMinutes() {
    return ImmutableSet.of(LocalTime.NOON.plusMinutes(1L), LocalTime.NOON.plusMinutes(1));
  }

  ImmutableSet<LocalTime> testLocalTimePlusHours() {
    return ImmutableSet.of(LocalTime.NOON.plusHours(1L), LocalTime.NOON.plusHours(1));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusNanos() {
    return ImmutableSet.of(LocalTime.NOON.minusNanos(1L), LocalTime.NOON.minusNanos(1));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusSeconds() {
    return ImmutableSet.of(LocalTime.NOON.minusSeconds(1L), LocalTime.NOON.minusSeconds(1));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusMinutes() {
    return ImmutableSet.of(LocalTime.NOON.minusMinutes(1L), LocalTime.NOON.minusMinutes(1));
  }

  ImmutableSet<LocalTime> testLocalTimeMinusHours() {
    return ImmutableSet.of(LocalTime.NOON.minusHours(1L), LocalTime.NOON.minusHours(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusNanos() {
    return ImmutableSet.of(OffsetTime.MIN.plusNanos(1L), OffsetTime.MIN.plusNanos(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusSeconds() {
    return ImmutableSet.of(OffsetTime.MIN.plusSeconds(1L), OffsetTime.MIN.plusSeconds(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusMinutes() {
    return ImmutableSet.of(OffsetTime.MIN.plusMinutes(1L), OffsetTime.MIN.plusMinutes(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimePlusHours() {
    return ImmutableSet.of(OffsetTime.MIN.plusHours(1L), OffsetTime.MIN.plusHours(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusNanos() {
    return ImmutableSet.of(OffsetTime.MAX.minusNanos(1L), OffsetTime.MAX.minusNanos(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusSeconds() {
    return ImmutableSet.of(OffsetTime.MAX.minusSeconds(1L), OffsetTime.MAX.minusSeconds(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusMinutes() {
    return ImmutableSet.of(OffsetTime.MAX.minusMinutes(1L), OffsetTime.MAX.minusMinutes(1));
  }

  ImmutableSet<OffsetTime> testOffsetTimeMinusHours() {
    return ImmutableSet.of(OffsetTime.MAX.minusHours(1L), OffsetTime.MAX.minusHours(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusNanos() {
    return ImmutableSet.of(LocalDateTime.MIN.plusNanos(1L), LocalDateTime.MIN.plusNanos(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusSeconds() {
    return ImmutableSet.of(LocalDateTime.MIN.plusSeconds(1L), LocalDateTime.MIN.plusSeconds(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusMinutes() {
    return ImmutableSet.of(LocalDateTime.MIN.plusMinutes(1L), LocalDateTime.MIN.plusMinutes(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusHours() {
    return ImmutableSet.of(LocalDateTime.MIN.plusHours(1L), LocalDateTime.MIN.plusHours(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusDays() {
    return ImmutableSet.of(LocalDateTime.MIN.plusDays(1L), LocalDateTime.MIN.plusDays(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusWeeks() {
    return ImmutableSet.of(LocalDateTime.MIN.plusWeeks(1L), LocalDateTime.MIN.plusWeeks(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusMonths() {
    return ImmutableSet.of(LocalDateTime.MIN.plusMonths(1L), LocalDateTime.MIN.plusMonths(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimePlusYears() {
    return ImmutableSet.of(LocalDateTime.MIN.plusYears(1L), LocalDateTime.MIN.plusYears(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusNanos() {
    return ImmutableSet.of(LocalDateTime.MAX.minusNanos(1L), LocalDateTime.MAX.minusNanos(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusSeconds() {
    return ImmutableSet.of(LocalDateTime.MAX.minusSeconds(1L), LocalDateTime.MAX.minusSeconds(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusMinutes() {
    return ImmutableSet.of(LocalDateTime.MAX.minusMinutes(1L), LocalDateTime.MAX.minusMinutes(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusHours() {
    return ImmutableSet.of(LocalDateTime.MAX.minusHours(1L), LocalDateTime.MAX.minusHours(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusDays() {
    return ImmutableSet.of(LocalDateTime.MAX.minusDays(1L), LocalDateTime.MAX.minusDays(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusWeeks() {
    return ImmutableSet.of(LocalDateTime.MAX.minusWeeks(1L), LocalDateTime.MAX.minusWeeks(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusMonths() {
    return ImmutableSet.of(LocalDateTime.MAX.minusMonths(1L), LocalDateTime.MAX.minusMonths(1));
  }

  ImmutableSet<LocalDateTime> testLocalDateTimeMinusYears() {
    return ImmutableSet.of(LocalDateTime.MAX.minusYears(1L), LocalDateTime.MAX.minusYears(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusNanos() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusNanos(1L), OffsetDateTime.MIN.plusNanos(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusSeconds() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusSeconds(1L), OffsetDateTime.MIN.plusSeconds(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusMinutes() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusMinutes(1L), OffsetDateTime.MIN.plusMinutes(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusHours() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusHours(1L), OffsetDateTime.MIN.plusHours(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusDays() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusDays(1L), OffsetDateTime.MIN.plusDays(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusWeeks() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusWeeks(1L), OffsetDateTime.MIN.plusWeeks(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusMonths() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusMonths(1L), OffsetDateTime.MIN.plusMonths(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimePlusYears() {
    return ImmutableSet.of(OffsetDateTime.MIN.plusYears(1L), OffsetDateTime.MIN.plusYears(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusNanos() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusNanos(1L), OffsetDateTime.MAX.minusNanos(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusSeconds() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusSeconds(1L), OffsetDateTime.MAX.minusSeconds(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusMinutes() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusMinutes(1L), OffsetDateTime.MAX.minusMinutes(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusHours() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusHours(1L), OffsetDateTime.MAX.minusHours(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusDays() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusDays(1L), OffsetDateTime.MAX.minusDays(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusWeeks() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusWeeks(1L), OffsetDateTime.MAX.minusWeeks(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusMonths() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusMonths(1L), OffsetDateTime.MAX.minusMonths(1));
  }

  ImmutableSet<OffsetDateTime> testOffsetDateTimeMinusYears() {
    return ImmutableSet.of(OffsetDateTime.MAX.minusYears(1L), OffsetDateTime.MAX.minusYears(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusNanos() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusNanos(1L), ZONED_DATE_TIME.plusNanos(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusSeconds() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusSeconds(1L), ZONED_DATE_TIME.plusSeconds(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusMinutes() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusMinutes(1L), ZONED_DATE_TIME.plusMinutes(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusHours() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusHours(1L), ZONED_DATE_TIME.plusHours(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusDays() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusDays(1L), ZONED_DATE_TIME.plusDays(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusWeeks() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusWeeks(1L), ZONED_DATE_TIME.plusWeeks(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusMonths() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusMonths(1L), ZONED_DATE_TIME.plusMonths(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimePlusYears() {
    return ImmutableSet.of(ZONED_DATE_TIME.plusYears(1L), ZONED_DATE_TIME.plusYears(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusNanos() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusNanos(1L), ZONED_DATE_TIME.minusNanos(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusSeconds() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusSeconds(1L), ZONED_DATE_TIME.minusSeconds(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusMinutes() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusMinutes(1L), ZONED_DATE_TIME.minusMinutes(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusHours() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusHours(1L), ZONED_DATE_TIME.minusHours(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusDays() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusDays(1L), ZONED_DATE_TIME.minusDays(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusWeeks() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusWeeks(1L), ZONED_DATE_TIME.minusWeeks(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusMonths() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusMonths(1L), ZONED_DATE_TIME.minusMonths(1));
  }

  ImmutableSet<ZonedDateTime> testZonedDateTimeMinusYears() {
    return ImmutableSet.of(ZONED_DATE_TIME.minusYears(1L), ZONED_DATE_TIME.minusYears(1));
  }
}
