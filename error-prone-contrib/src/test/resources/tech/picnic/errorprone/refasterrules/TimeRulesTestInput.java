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
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
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
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")).toLocalDate(),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.MIN).toLocalDate());
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
        OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")).toLocalTime(),
        OffsetTime.ofInstant(Instant.EPOCH, ZoneOffset.MIN).toLocalTime(),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.MAX).toLocalTime());
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
}
