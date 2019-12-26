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
    return ImmutableSet.of(
        Instant.ofEpochMilli(0),
        Instant.ofEpochMilli(0L),
        Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(0, 0));
  }

  Instant testClockInstant() {
    return Instant.now(Clock.systemUTC());
  }

  ImmutableSet<ZoneId> testUtcConstant() {
    return ImmutableSet.of(
        ZoneId.of("GMT"),
        ZoneId.of("UTC"),
        ZoneId.of("Z"),
        ZoneId.of("+0"),
        ZoneId.of("-0"),
        ZoneOffset.UTC,
        ZoneId.from(ZoneOffset.UTC));
  }

  Clock testUtcClock() {
    return Clock.system(ZoneOffset.UTC);
  }

  ImmutableSet<Boolean> testInstantIsBefore() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) < 0, Instant.MIN.compareTo(Instant.MAX) >= 0);
  }

  ImmutableSet<Boolean> testInstantIsAfter() {
    return ImmutableSet.of(
        Instant.MIN.compareTo(Instant.MAX) > 0, Instant.MIN.compareTo(Instant.MAX) <= 0);
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
}
