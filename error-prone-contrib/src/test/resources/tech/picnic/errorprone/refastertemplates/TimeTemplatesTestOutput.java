package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoLocalDateIsAfter;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoLocalDateIsBefore;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoLocalDateTimeIsAfter;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoLocalDateTimeIsBefore;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoZonedDateTimeIsAfter;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ChronoZonedDateTimeIsBefore;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ClockInstant;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.DurationBetweenInstants;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.DurationBetweenOffsetDateTimes;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.DurationIsZero;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.EpochInstant;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.InstantIsAfter;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.InstantIsBefore;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.LocalDateAtStartOfDay;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.LocalTimeMin;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.OffsetDateTimeIsAfter;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.OffsetDateTimeIsBefore;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.UtcClock;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.UtcConstant;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ZeroDuration;
import tech.picnic.errorprone.refastertemplates.TimeTemplates.ZeroPeriod;

@TemplateCollection(TimeTemplates.class)
final class TimeTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ChronoUnit.class);
  }

  @Template(ClockInstant.class)
  Instant testClockInstant() {
    return Clock.systemUTC().instant();
  }

  @Template(UtcConstant.class)
  ImmutableSet<ZoneId> testUtcConstant() {
    return ImmutableSet.of(
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC,
        ZoneOffset.UTC);
  }

  @Template(UtcClock.class)
  Clock testUtcClock() {
    return Clock.systemUTC();
  }

  @Template(EpochInstant.class)
  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(Instant.EPOCH, Instant.EPOCH, Instant.EPOCH, Instant.EPOCH);
  }

  @Template(InstantIsBefore.class)
  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(Instant.MIN.isBefore(Instant.MAX), !Instant.MIN.isBefore(Instant.MAX));
  }

  @Template(InstantIsAfter.class)
  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(Instant.MIN.isAfter(Instant.MAX), !Instant.MIN.isAfter(Instant.MAX));
  }

  @Template(LocalTimeMin.class)
  ImmutableSet<LocalTime> testLocalTimeMin() {
    return ImmutableSet.of(
        LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN, LocalTime.MIN);
  }

  @Template(LocalDateAtStartOfDay.class)
  LocalDateTime testLocalDateAtStartOfDay() {
    return LocalDate.EPOCH.atStartOfDay();
  }

  @Template(ChronoLocalDateIsBefore.class)
  ImmutableSet<Boolean> testChronoLocalDateIsBefore() {
    return ImmutableSet.of(
        LocalDate.MIN.isBefore(LocalDate.MAX), !LocalDate.MIN.isBefore(LocalDate.MAX));
  }

  @Template(ChronoLocalDateIsAfter.class)
  ImmutableSet<Boolean> testChronoLocalDateIsAfter() {
    return ImmutableSet.of(
        LocalDate.MIN.isAfter(LocalDate.MAX), !LocalDate.MIN.isAfter(LocalDate.MAX));
  }

  @Template(ChronoLocalDateTimeIsBefore.class)
  ImmutableSet<Boolean> testChronoLocalDateTimeIsBefore() {
    return ImmutableSet.of(
        LocalDateTime.MIN.isBefore(LocalDateTime.MAX),
        !LocalDateTime.MIN.isBefore(LocalDateTime.MAX));
  }

  @Template(ChronoLocalDateTimeIsAfter.class)
  ImmutableSet<Boolean> testChronoLocalDateTimeIsAfter() {
    return ImmutableSet.of(
        LocalDateTime.MIN.isAfter(LocalDateTime.MAX),
        !LocalDateTime.MIN.isAfter(LocalDateTime.MAX));
  }

  @Template(ChronoZonedDateTimeIsBefore.class)
  ImmutableSet<Boolean> testChronoZonedDateTimeIsBefore() {
    return ImmutableSet.of(
        ZonedDateTime.now().isBefore(ZonedDateTime.now()),
        !ZonedDateTime.now().isBefore(ZonedDateTime.now()));
  }

  @Template(ChronoZonedDateTimeIsAfter.class)
  ImmutableSet<Boolean> testChronoZonedDateTimeIsAfter() {
    return ImmutableSet.of(
        ZonedDateTime.now().isAfter(ZonedDateTime.now()),
        !ZonedDateTime.now().isAfter(ZonedDateTime.now()));
  }

  @Template(OffsetDateTimeIsAfter.class)
  ImmutableSet<Boolean> testOffsetDateTimeIsAfter() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.isAfter(OffsetDateTime.MAX),
        !OffsetDateTime.MIN.isAfter(OffsetDateTime.MAX));
  }

  @Template(OffsetDateTimeIsBefore.class)
  ImmutableSet<Boolean> testOffsetDateTimeIsBefore() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.isBefore(OffsetDateTime.MAX),
        !OffsetDateTime.MIN.isBefore(OffsetDateTime.MAX));
  }

  @Template(ZeroDuration.class)
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

  @Template(DurationBetweenInstants.class)
  Duration testDurationBetweenInstants() {
    return Duration.between(Instant.MIN, Instant.MAX);
  }

  @Template(DurationBetweenOffsetDateTimes.class)
  Duration testDurationBetweenOffsetDateTimes() {
    return Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX)
        .plus(Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX));
  }

  @Template(DurationIsZero.class)
  ImmutableSet<Boolean> testDurationIsZero() {
    return ImmutableSet.of(Duration.ofDays(1).isZero(), Duration.ofDays(2).isZero());
  }

  @Template(ZeroPeriod.class)
  ImmutableSet<Period> testZeroPeriod() {
    return ImmutableSet.of(Period.ZERO, Period.ZERO, Period.ZERO, Period.ZERO, Period.ZERO);
  }
}
