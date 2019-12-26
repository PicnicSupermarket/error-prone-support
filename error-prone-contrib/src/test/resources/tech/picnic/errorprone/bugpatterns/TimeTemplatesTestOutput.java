package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

final class TimeTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(Instant.EPOCH, Instant.EPOCH, Instant.EPOCH, Instant.EPOCH);
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
        ZoneOffset.UTC,
        ZoneOffset.UTC);
  }

  Clock testUtcClock() {
    return Clock.systemUTC();
  }

  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(Instant.MIN.isBefore(Instant.MAX), !Instant.MIN.isBefore(Instant.MAX));
  }

  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(Instant.MIN.isAfter(Instant.MAX), !Instant.MIN.isAfter(Instant.MAX));
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
}
