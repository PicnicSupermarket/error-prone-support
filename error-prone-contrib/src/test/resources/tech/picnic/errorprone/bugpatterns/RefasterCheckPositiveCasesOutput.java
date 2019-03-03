import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static com.google.common.collect.Streams.stream;
import static java.util.Comparator.naturalOrder;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static java.util.function.Function.identity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class RefasterCheckPositiveCases {
  /**
   * These types and statically imported methods may be fully replaced by some of the Refaster
   * templates under test. Refaster does not remove the associated imports, while Google Java
   * Formatter does. This listing ensures that any imports present in the input file are also
   * present in the output file.
   */
  private static final ImmutableSet<?> ELIDED_TYPES_AND_STATIC_IMPORTS =
      ImmutableSet.of(
          AbstractMap.class,
          Arrays.class,
          Lists.class,
          MoreObjects.class,
          Preconditions.class,
          (Runnable) () -> identity());

  static final class AssortedTemplates {
    int testCheckIndex() {
      return Objects.checkIndex(0, 1);
    }

    ImmutableMap<Integer, String> testStreamOfMapEntriesImmutableMap() {
      // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
      // Looks like a @Placeholder limitation. Try to track down and fix.
      return Stream.of(1, 2, 3).collect(toImmutableMap(n -> Integer.valueOf(n), n -> n.toString()));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToMap() {
      return ImmutableSet.of(
          Maps.toMap(ImmutableList.of(1), n -> n * 2),
          Maps.toMap(ImmutableList.of(2)::iterator, Integer::valueOf),
          Maps.toMap(ImmutableList.of(3).iterator(), n -> n.intValue()));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIterableUniqueIndex() {
      return ImmutableSet.of(
          Maps.uniqueIndex(ImmutableList.of(1), n -> n * 2),
          Maps.uniqueIndex(ImmutableList.of(2)::iterator, Integer::valueOf),
          Maps.uniqueIndex(ImmutableList.of(3).iterator(), n -> n.intValue()));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testSetToImmutableMap() {
      return ImmutableSet.of(
          Maps.toMap(ImmutableSet.of(1), n -> n + 2),
          Maps.toMap(ImmutableSet.of(2), Integer::valueOf));
    }

    ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
      return Stream.of(BoundType.OPEN).collect(toImmutableEnumSet());
    }
  }

  static final class BigDecimalTemplates {
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

  static final class CollectionTemplates {
    ImmutableSet<Boolean> testCollectionIsEmpty() {
      return ImmutableSet.of(
          ImmutableSet.of(1).isEmpty(),
          ImmutableSet.of(2).isEmpty(),
          ImmutableSet.of(3).isEmpty(),
          !ImmutableSet.of(4).isEmpty(),
          !ImmutableSet.of(5).isEmpty(),
          !ImmutableSet.of(6).isEmpty());
    }

    ImmutableSet<Boolean> testCollectionAddAllFromCollection() {
      return ImmutableSet.of(
          new ArrayList<>().addAll(new HashSet<>()),
          Iterables.addAll(new ArrayList<>(), new HashSet<>()::iterator));
    }

    ArrayList<String> testNewArrayListFromCollection() {
      return new ArrayList<>(ImmutableList.of("foo"));
    }

    Stream<Integer> testImmutableCollectionAsListToStream() {
      return ImmutableSet.of(1).stream();
    }

    ImmutableList<Integer> testImmutableCollectionAsList() {
      return ImmutableSet.of(1).asList();
    }
  }

  static final class EqualityTemplates {
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

  static final class ImmutableListTemplates {
    ImmutableList.Builder<String> testImmutableListBuilder() {
      return ImmutableList.builder();
    }

    ImmutableList<ImmutableList<Integer>> testIterableToImmutableList() {
      return ImmutableList.of(
          ImmutableList.copyOf(ImmutableList.of(1)),
          ImmutableList.copyOf(ImmutableList.of(2)::iterator),
          ImmutableList.copyOf(ImmutableList.of(3).iterator()),
          ImmutableList.copyOf(ImmutableList.of(4)),
          ImmutableList.copyOf(ImmutableList.of(5)::iterator),
          ImmutableList.copyOf(ImmutableList.of(6).iterator()),
          ImmutableList.copyOf(new Integer[] {7}),
          ImmutableList.copyOf(new Integer[] {8}),
          ImmutableList.copyOf(new Integer[] {9}));
    }

    ImmutableList<Integer> testImmutableListAsList() {
      return ImmutableList.of(1, 2, 3);
    }

    ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
      return ImmutableSet.of(
          ImmutableList.sortedCopyOf(ImmutableSet.of(1)),
          ImmutableList.sortedCopyOf(ImmutableSet.of(2)),
          ImmutableList.sortedCopyOf(ImmutableSet.of(3)::iterator));
    }

    ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
      return ImmutableSet.of(
          ImmutableList.sortedCopyOf(Comparator.comparing(String::length), ImmutableSet.of("foo")),
          ImmutableList.sortedCopyOf(
              Comparator.comparing(String::isEmpty), ImmutableSet.of("bar")::iterator));
    }
  }

  static final class ImmutableSetTemplates {
    ImmutableSet.Builder<String> testImmutableSetBuilder() {
      return ImmutableSet.builder();
    }

    ImmutableSet<ImmutableSet<Integer>> testIterableToImmutableSet() {
      return ImmutableSet.of(
          ImmutableSet.copyOf(ImmutableList.of(1)),
          ImmutableSet.copyOf(ImmutableList.of(2)::iterator),
          ImmutableSet.copyOf(ImmutableList.of(3).iterator()),
          ImmutableSet.copyOf(ImmutableSet.of(4)),
          ImmutableSet.copyOf(ImmutableSet.of(5)::iterator),
          ImmutableSet.copyOf(ImmutableSet.of(6).iterator()),
          ImmutableSet.copyOf(new Integer[] {7}),
          ImmutableSet.copyOf(new Integer[] {8}),
          ImmutableSet.copyOf(new Integer[] {9}));
    }

    ImmutableSet<Integer> testImmutableSetCopyOfImmutableSet() {
      return ImmutableSet.of(1, 2);
    }

    ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
      return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
    }
  }

  static final class ImmutableSortedSetTemplates {
    ImmutableSortedSet.Builder<String> testImmutableSortedSetBuilder() {
      return ImmutableSortedSet.orderedBy(Comparator.comparingInt(String::length));
    }

    ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrderBuilder() {
      return ImmutableSortedSet.naturalOrder();
    }

    ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrderBuilder() {
      return ImmutableSortedSet.reverseOrder();
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
      return ImmutableSet.of(
          ImmutableSortedSet.copyOf(ImmutableList.of(1)),
          ImmutableSortedSet.copyOf(ImmutableList.of(2)::iterator),
          ImmutableSortedSet.copyOf(ImmutableList.of(3).iterator()),
          ImmutableSortedSet.copyOf(ImmutableSet.of(4)),
          ImmutableSortedSet.copyOf(ImmutableSet.of(5)::iterator),
          ImmutableSortedSet.copyOf(ImmutableSet.of(6).iterator()),
          ImmutableSortedSet.copyOf(new Integer[] {7}),
          ImmutableSortedSet.copyOf(new Integer[] {8}),
          ImmutableSortedSet.copyOf(new Integer[] {9}));
    }
  }

  static final class MapEntryTemplates {
    ImmutableSet<Map.Entry<String, Integer>> testMapEntry() {
      return ImmutableSet.of(Map.entry("foo", 1), Map.entry("bar", 2));
    }

    ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByKey() {
      return ImmutableSet.of(comparingByKey(), comparingByKey());
    }

    Comparator<Map.Entry<Integer, String>> testMapEntryComparingByKeyWithCustomComparator() {
      return comparingByKey(Comparator.comparingInt(i -> i * 2));
    }

    ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByValue() {
      return ImmutableSet.of(comparingByValue(), comparingByValue());
    }

    Comparator<Map.Entry<Integer, String>> testMapEntryComparingByValueWithCustomComparator() {
      return comparingByValue(Comparator.comparingInt(String::length));
    }
  }

  static final class NullTemplates {
    String testRequireNonNullElse() {
      return Objects.requireNonNullElse("foo", "bar");
    }

    long testIsNullFunction() {
      return Stream.of("foo").filter(Objects::isNull).count();
    }

    long testNonNullFunction() {
      return Stream.of("foo").filter(Objects::nonNull).count();
    }
  }

  static final class OptionalTemplates {
    Optional<String> testOptionalOfNullable() {
      return Optional.ofNullable(toString());
    }

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

    ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
      return ImmutableSet.of(
          ImmutableSet.of(1).stream().findFirst(), ImmutableList.of(2).stream().findFirst());
    }

    Optional<String> testOptionalFirstIteratorElement() {
      return stream(ImmutableSet.of("foo").iterator()).findFirst();
    }

    Stream<String> testMapToOptionalGet(Map<Integer, Optional<String>> map) {
      return Stream.of(1).flatMap(n -> map.get(n).stream());
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

    ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableSet()), Stream.of(2).collect(toImmutableSet()));
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

  static final class StringTemplates {
    ImmutableSet<Boolean> testStringIsEmpty() {
      return ImmutableSet.of(
          "foo".isEmpty(),
          "bar".isEmpty(),
          "baz".isEmpty(),
          !"foo".isEmpty(),
          !"bar".isEmpty(),
          !"baz".isEmpty());
    }

    ImmutableSet<Boolean> testStringIsNullOrEmpty() {
      return ImmutableSet.of(
          Strings.isNullOrEmpty(getClass().getName()),
          !Strings.isNullOrEmpty(getClass().getName()));
    }

    ImmutableSet<Optional<String>> testOptionalNonEmptyString() {
      return ImmutableSet.of(
          Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
          Optional.ofNullable(toString()).filter(s -> !s.isEmpty()));
    }
  }

  static final class TimeTemplates {
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
