import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class RefasterCheckPositiveCases {
  // XXX: Move down.
  Stream<Integer> testImmutableCollectionAsListToStream() {
    return ImmutableSet.of(1).stream();
  }

  static final class BigDecimals {
    ImmutableSet<BigDecimal> testBigDecimalZero() {
      return ImmutableSet.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    ImmutableSet<BigDecimal> testBigDecimalOne() {
      return ImmutableSet.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
    }

    ImmutableSet<BigDecimal> testBigDecimalTen() {
      return ImmutableSet.of(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }

    ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
      return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L));
    }
  }

  static final class Equality {
    ImmutableSet<Boolean> testPrimitiveEquals() {
      // XXX: The negated variants of the primitive expressions below trigger an "overlapping
      // replacements" bug. Figure out how/why and fix. Then add these:
      // !Objects.equals(0, 1),
      // !Objects.equals(0L, 1L),
      return ImmutableSet.of(
          0 == 1,
          0L == 1L,
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
          RoundingMode.UP == RoundingMode.DOWN, RoundingMode.UP == RoundingMode.DOWN);
    }

    boolean testEqualsPredicate() {
      // XXX: When boxing is involved this rule seems to break. Example:
      // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
      return Stream.of("foo").anyMatch("bar"::equals);
    }

    boolean testEqualBooleans(boolean b1, boolean b2) {
      return b1 == b2;
    }

    boolean testUnequalBooleans(boolean b1, boolean b2) {
      return b1 != b2;
    }
  }

  static final class Optionals {
    ImmutableSet<Boolean> testOptionalIsEmpty() {
      return ImmutableSet.of(Optional.empty().isEmpty(), Optional.of("foo").isEmpty());
    }

    ImmutableSet<Boolean> testOptionalIsPresent() {
      return ImmutableSet.of(Optional.empty().isPresent(), Optional.of("foo").isPresent());
    }

    Stream<Object> testOptionalToStream() {
      return Stream.concat(Optional.empty().stream(), Optional.of("foo").stream());
    }

    Stream<Object> testFlatmapOptionalToStream() {
      return Stream.concat(
          Stream.of(Optional.empty()).flatMap(Optional::stream),
          Stream.of(Optional.of("foo")).flatMap(Optional::stream));
    }
  }

  static final class StreamTemplates {
    Stream<Integer> testConcatOneStream() {
      return Stream.of(1);
    }

    Stream<Integer> testConcatTwoStreams() {
      return Stream.concat(Stream.of(1), Stream.of(2));
    }

    Stream<Integer> testConcatThreeStreams() {
      return Streams.concat(Stream.of(1), Stream.of(2), Stream.of(3));
    }

    ImmutableList<Integer> testStreamToImmutableList() {
      return Stream.of(1).collect(toImmutableList());
    }

    ImmutableSet<Integer> testStreamToImmutableSet() {
      return Stream.of(1).collect(toImmutableSet());
    }

    ImmutableSortedSet<Integer> testStreamToImmutableSortedSet() {
      return Stream.of(1).collect(toImmutableSortedSet(naturalOrder()));
    }

    ImmutableMultiset<Integer> testStreamToImmutableMultiset() {
      return Stream.of(1).collect(toImmutableMultiset());
    }

    ImmutableSortedMultiset<Integer> testStreamToImmutableSortedMultiset() {
      return Stream.of(1).collect(toImmutableSortedMultiset(naturalOrder()));
    }
  }

  static final class Time {
    ImmutableSet<Instant> testEpochInstant() {
      return ImmutableSet.of(Instant.EPOCH, Instant.EPOCH, Instant.EPOCH, Instant.EPOCH);
    }

    Instant testClockInstant() {
      return Clock.systemUTC().instant();
    }

    ZoneId testUtcConstant() {
      return ZoneOffset.UTC;
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
  }
}
