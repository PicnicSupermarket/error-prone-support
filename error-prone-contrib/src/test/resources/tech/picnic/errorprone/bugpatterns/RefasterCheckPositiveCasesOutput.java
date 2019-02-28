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
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testEqualBooleans(boolean b1, boolean b2) {
    return b1 == b2;
  }

  boolean testEqualBooleansNegation(boolean b1, boolean b2) {
    return b1 != b2;
  }

  boolean testOptionalIsEmpty() {
    return Optional.empty().isEmpty();
  }

  ZoneId testUtcConstant() {
    return ZoneOffset.UTC;
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
    return LocalDate.MIN.isAfter(LocalDate.MAX);
  }

  boolean testChronoLocalDateIsBefore() {
    return LocalDate.MIN.isBefore(LocalDate.MAX);
  }

  boolean testChronoLocalDateTimeIsAfter() {
    return LocalDateTime.MIN.isAfter(LocalDateTime.MAX);
  }

  boolean testChronoLocalDateTimeIsBefore() {
    return LocalDateTime.MIN.isBefore(LocalDateTime.MAX);
  }

  boolean testChronoZonedDateTimeIsAfter() {
    return ZonedDateTime.now().isAfter(ZonedDateTime.now());
  }

  boolean testChronoZonedDateTimeIsBefore() {
    return ZonedDateTime.now().isBefore(ZonedDateTime.now());
  }

  Instant testClockInstant() {
    return Clock.systemUTC().instant();
  }

  Duration testDurationBetweenInstants() {
    return Duration.between(Instant.MIN, Instant.MAX);
  }

  Duration testDurationBetweenOffsetDateTimes() {
    return Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX)
        .plus(Duration.between(OffsetDateTime.MIN, OffsetDateTime.MAX));
  }

  ImmutableSet<Instant> testEpochInstant() {
    return ImmutableSet.of(Instant.EPOCH, Instant.EPOCH, Instant.EPOCH, Instant.EPOCH);
  }

  boolean testInstantIsAfter() {
    return Instant.MIN.isAfter(Instant.MAX);
  }

  boolean testInstantIsBefore() {
    return Instant.MIN.isBefore(Instant.MAX);
  }

  boolean testOffsetDateTimeIsAfter() {
    return OffsetDateTime.MIN.isAfter(OffsetDateTime.MAX);
  }

  boolean testOffsetDateTimeIsBefore() {
    return OffsetDateTime.MIN.isBefore(OffsetDateTime.MAX);
  }
}
