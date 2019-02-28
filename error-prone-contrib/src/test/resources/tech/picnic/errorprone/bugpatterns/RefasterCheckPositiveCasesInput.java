import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testEqualBooleans(boolean b1, boolean b2) {
    return b1 ? b2 : !b2;
  }

  boolean testEqualBooleansNegation(boolean b1, boolean b2) {
    return b1 ? !b2 : b2;
  }

  boolean testOptionalIsEmpty() {
    return !Optional.empty().isPresent();
  }

  ZoneId testUtcConstant() {
    return ZoneId.of("UTC");
  }

  // XXX: Doesn't work. Why is `.asList()` not dropped?
  //  void testImmutableCollectionAsListToStream() {
  //    ImmutableSet.of(1, 2).asList().stream().collect(toImmutableMap(identity(), identity()));
  //  }
  //
  //  <T> void testImmutableCollectionAsListToStream(ImmutableCollection<T> collection) {
  //    collection.asList().stream();
  //  }

  boolean testChronoLocalDateIsAfter() {
    return LocalDate.MIN.compareTo(LocalDate.MAX) > 0;
  }

  boolean testChronoLocalDateIsBefore() {
    return LocalDate.MIN.compareTo(LocalDate.MAX) < 0;
  }

  boolean testChronoLocalDateTimeIsAfter() {
    return LocalDateTime.MIN.compareTo(LocalDateTime.MAX) > 0;
  }

  boolean testChronoLocalDateTimeIsBefore() {
    return LocalDateTime.MIN.compareTo(LocalDateTime.MAX) < 0;
  }

  boolean testChronoZonedDateTimeIsAfter() {
    return ZonedDateTime.now().compareTo(ZonedDateTime.now()) > 0;
  }

  boolean testChronoZonedDateTimeIsBefore() {
    return ZonedDateTime.now().compareTo(ZonedDateTime.now()) < 0;
  }

  Instant testClockInstant() {
    return Instant.now(Clock.systemUTC());
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

  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(
        Instant.ofEpochMilli(0),
        Instant.ofEpochMilli(0L),
        Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(0, 0));
  }

  boolean testInstantIsAfter() {
    return Instant.MIN.compareTo(Instant.MAX) > 0;
  }

  boolean testInstantIsBefore() {
    return Instant.MIN.compareTo(Instant.MAX) < 0;
  }

  boolean testOffsetDateTimeIsAfter() {
    return OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) > 0;
  }

  boolean testOffsetDateTimeIsBefore() {
    return OffsetDateTime.MIN.compareTo(OffsetDateTime.MAX) < 0;
  }
}
