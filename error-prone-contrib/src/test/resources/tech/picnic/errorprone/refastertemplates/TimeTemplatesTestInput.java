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
    return Instant.now(Clock.systemUTC());
  }

  @Template(UtcConstant.class)
  ImmutableSet<ZoneId> testUtcConstant() {
    return ImmutableSet.of(
        ZoneId.of("GMT"),
        ZoneId.of("UTC"),
        ZoneId.of("+0"),
        ZoneId.of("-0"),
        ZoneOffset.UTC,
        ZoneId.from(ZoneOffset.UTC));
  }

  @Template(UtcClock.class)
  Clock testUtcClock() {
    return Clock.system(ZoneOffset.UTC);
  }

  @Template(EpochInstant.class)
  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(
        Instant.ofEpochMilli(0),
        Instant.ofEpochMilli(0L),
        Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(0, 0));
  }

  @Template(InstantIsBefore.class)
  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) < 0, Instant.MIN.compareTo(Instant.MAX) >= 0);
  }

  @Template(InstantIsAfter.class)
  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) > 0, Instant.MIN.compareTo(Instant.MAX) <= 0);
  }

  @Template(LocalTimeMin.class)
  ImmutableSet<LocalTime> testLocalTimeMin() {
    return ImmutableSet.of(
        LocalTime.MIDNIGHT,
        LocalTime.of(0, 0),
        LocalTime.of(0, 0, 0),
        LocalTime.of(0, 0, 0, 0),
        LocalTime.ofNanoOfDay(0),
        LocalTime.ofSecondOfDay(0));
  }

  @Template(LocalDateAtStartOfDay.class)
  LocalDateTime testLocalDateAtStartOfDay() {
    return LocalDate.EPOCH.atTime(LocalTime.MIN);
  }

  @Template(ChronoLocalDateIsBefore.class)
  ImmutableSet<Boolean> testChronoLocalDateIsBefore() {
    return ImmutableSet.of(
        LocalDate.MIN.compareTo(LocalDate.MAX) < 0, LocalDate.MIN.compareTo(LocalDate.MAX) >= 0);
  }

  @Template(ChronoLocalDateIsAfter.class)
  ImmutableSet<Boolean> testChronoLocalDateIsAfter() {
    return ImmutableSet.of(
        LocalDate.MIN.compareTo(LocalDate.MAX) > 0, LocalDate.MIN.compareTo(LocalDate.MAX) <= 0);
  }

  @Template(ChronoLocalDateTimeIsBefore.class)
  ImmutableSet<Boolean> testChronoLocalDateTimeIsBefore() {
    return ImmutableSet.of(
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) < 0,
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) >= 0);
  }

  @Template(ChronoLocalDateTimeIsAfter.class)
  ImmutableSet<Boolean> testChronoLocalDateTimeIsAfter() {
    return ImmutableSet.of(
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) > 0,
        LocalDateTime.MIN.compareTo(LocalDateTime.MAX) <= 0);
  }

  @Template(ChronoZonedDateTimeIsBefore.class)
  ImmutableSet<Boolean> testChronoZonedDateTimeIsBefore() {
    return ImmutableSet.of(
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) < 0,
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) >= 0);
  }

  @Template(ChronoZonedDateTimeIsAfter.class)
  ImmutableSet<Boolean> testChronoZonedDateTimeIsAfter() {
    return ImmutableSet.of(
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) > 0,
        ZonedDateTime.now().compareTo(ZonedDateTime.now()) <= 0);
  }

  @Template(OffsetDateTimeIsAfter.class)
  ImmutableSet<Boolean> testOffsetDateTimeIsAfter() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) > 0,
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) <= 0);
  }

  @Template(OffsetDateTimeIsBefore.class)
  ImmutableSet<Boolean> testOffsetDateTimeIsBefore() {
    return ImmutableSet.of(
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) < 0,
        OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) >= 0);
  }

  @Template(ZeroDuration.class)
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

  @Template(DurationBetweenInstants.class)
  Duration testDurationBetweenInstants() {
    return Duration.ofMillis(Instant.MAX.toEpochMilli() - Instant.MIN.toEpochMilli());
  }

  @Template(DurationBetweenOffsetDateTimes.class)
  Duration testDurationBetweenOffsetDateTimes() {
    return Duration.between(OffsetDateTime.MIN.toInstant(), OffsetDateTime.MAX.toInstant())
        .plus(
            Duration.ofSeconds(
                OffsetDateTime.MAX.toEpochSecond() - OffsetDateTime.MIN.toEpochSecond()));
  }

  @Template(DurationIsZero.class)
  ImmutableSet<Boolean> testDurationIsZero() {
    return ImmutableSet.of(
        Duration.ofDays(1).equals(Duration.ZERO), Duration.ZERO.equals(Duration.ofDays(2)));
  }

  @Template(ZeroPeriod.class)
  ImmutableSet<Period> testZeroPeriod() {
    return ImmutableSet.of(
        Period.ofDays(0),
        Period.ofWeeks(0),
        Period.ofMonths(0),
        Period.ofYears(0),
        Period.of(0, 0, 0));
  }
}
