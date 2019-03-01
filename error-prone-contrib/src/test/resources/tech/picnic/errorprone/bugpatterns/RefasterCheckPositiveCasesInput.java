import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class RefasterCheckPositiveCases {
  // XXX: Move down.
  Stream<Integer> testImmutableCollectionAsListToStream() {
    return ImmutableSet.of(1).asList().stream();
  }

  static final class BigDecimals {
    ImmutableSet<BigDecimal> testBigDecimalZero() {
      return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L), new BigDecimal("0"));
    }

    ImmutableSet<BigDecimal> testBigDecimalOne() {
      return ImmutableSet.of(BigDecimal.valueOf(1), BigDecimal.valueOf(1L), new BigDecimal("1"));
    }

    ImmutableSet<BigDecimal> testBigDecimalTen() {
      return ImmutableSet.of(BigDecimal.valueOf(10), BigDecimal.valueOf(10L), new BigDecimal("10"));
    }

    ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
      return ImmutableSet.of(new BigDecimal(0), new BigDecimal(0L));
    }
  }

  static final class Equality {
    ImmutableSet<Boolean> testPrimitiveEquals() {
      // XXX: The negated variants of the primitive expressions below trigger an "overlapping
      // replacements" bug. Figure out how/why and fix. Then add these:
      // !Objects.equals(0, 1),
      // !Objects.equals(0L, 1L),
      return ImmutableSet.of(
          Objects.equals(0, 1),
          Objects.equals(0L, 1L),
          Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          Objects.equals(Long.valueOf(0), Long.valueOf(1)),
          !Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          !Objects.equals(Long.valueOf(0), Long.valueOf(1)));
    }

    ImmutableSet<Boolean> testEnumEquals() {
      // XXX: The negated variants of the expressions below trigger an "overlapping
      // replacements" bug. Figure out how/why and fix. Then add these:
      // !RoundingMode.UP.equals(RoundingMode.DOWN)
      // !Objects.equals(RoundingMode.UP, RoundingMode.DOWN)
      return ImmutableSet.of(
          RoundingMode.UP.equals(RoundingMode.DOWN),
          Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
    }

    boolean testEqualsPredicate() {
      // XXX: When boxing is involved this rule seems to break. Example:
      // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
      return Stream.of("foo").anyMatch(s -> "bar".equals(s));
    }

    boolean testEqualBooleans(boolean b1, boolean b2) {
      return b1 ? b2 : !b2;
    }

    boolean testUnequalBooleans(boolean b1, boolean b2) {
      return b1 ? !b2 : b2;
    }
  }

  static final class Optionals {
    ImmutableSet<Boolean> testOptionalIsEmpty() {
      return ImmutableSet.of(!Optional.empty().isPresent(), !Optional.of("foo").isPresent());
    }

    ImmutableSet<Boolean> testOptionalIsPresent() {
      return ImmutableSet.of(!Optional.empty().isEmpty(), !Optional.of("foo").isEmpty());
    }

    Stream<Object> testOptionalToStream() {
      return Stream.concat(Streams.stream(Optional.empty()), Streams.stream(Optional.of("foo")));
    }

    Stream<Object> testFlatmapOptionalToStream() {
      return Stream.concat(
          Stream.of(Optional.empty()).filter(Optional::isPresent).map(Optional::get),
          Stream.of(Optional.of("foo")).flatMap(Streams::stream));
    }
  }

  static final class StreamTemplates {
    Stream<Integer> testConcatOneStream() {
      return Streams.concat(Stream.of(1));
    }

    Stream<Integer> testConcatTwoStreams() {
      return Streams.concat(Stream.of(1), Stream.of(2));
    }

    Stream<Integer> testConcatThreeStreams() {
      return Streams.concat(Stream.of(1), Stream.of(2), Stream.of(3));
    }

    ImmutableList<Integer> testStreamToImmutableList() {
      return ImmutableList.copyOf(Stream.of(1).iterator());
    }

    ImmutableSet<Integer> testStreamToImmutableSet() {
      return ImmutableSet.copyOf(Stream.of(1).iterator());
    }

    ImmutableSortedSet<Integer> testStreamToImmutableSortedSet() {
      return ImmutableSortedSet.copyOf(Stream.of(1).iterator());
    }

    ImmutableMultiset<Integer> testStreamToImmutableMultiset() {
      return ImmutableMultiset.copyOf(Stream.of(1).iterator());
    }

    ImmutableSortedMultiset<Integer> testStreamToImmutableSortedMultiset() {
      return ImmutableSortedMultiset.copyOf(Stream.of(1).iterator());
    }
  }

  static final class Time {
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

    ZoneId testUtcConstant() {
      return ZoneId.of("UTC");
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
  }
}
