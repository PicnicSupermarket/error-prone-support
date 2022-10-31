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

  ImmutableSet<LocalDate> testInstantToLocalDate() {
    return ImmutableSet.of(
        LocalDate.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalDate.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Paris")),
        LocalDate.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        LocalDate.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")));
  }

  ImmutableSet<LocalDateTime> testInstantToLocalDateTime() {
    return ImmutableSet.of(
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")),
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
  }

  ImmutableSet<LocalTime> testInstantToLocalTime() {
    return ImmutableSet.of(
        LocalTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        LocalTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Paris")),
        LocalTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        LocalTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Berlin")));
  }

  OffsetDateTime testInstantToOffsetDateTime() {
    return Instant.EPOCH.atOffset(ZoneOffset.UTC);
  }

  ImmutableSet<OffsetTime> testInstantToOffsetTime() {
    return ImmutableSet.of(
        OffsetTime.ofInstant(Instant.EPOCH, ZoneId.of("Europe/Amsterdam")),
        OffsetTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
  }

  ZonedDateTime testInstantToZonedDateTime() {
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
}
