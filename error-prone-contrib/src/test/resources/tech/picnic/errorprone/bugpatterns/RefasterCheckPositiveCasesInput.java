import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
      return Preconditions.checkElementIndex(0, 1);
    }

    ImmutableMap<Integer, String> testStreamOfMapEntriesImmutableMap() {
      // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
      // Looks like a @Placeholder limitation. Try to track down and fix.
      return Stream.of(1, 2, 3)
          .map(n -> Map.entry(Integer.valueOf(n), n.toString()))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToMap() {
      return ImmutableSet.of(
          ImmutableList.of(1).stream().collect(toImmutableMap(identity(), n -> n * 2)),
          Streams.stream(ImmutableList.of(2)::iterator)
              .collect(toImmutableMap(n -> n, Integer::valueOf)),
          Streams.stream(ImmutableList.of(3).iterator())
              .collect(toImmutableMap(identity(), n -> n.intValue())));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIterableUniqueIndex() {
      return ImmutableSet.of(
          ImmutableList.of(1).stream().collect(toImmutableMap(n -> n * 2, identity())),
          Streams.stream(ImmutableList.of(2)::iterator)
              .collect(toImmutableMap(Integer::valueOf, n -> n)),
          Streams.stream(ImmutableList.of(3).iterator())
              .collect(toImmutableMap(n -> n.intValue(), identity())));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testSetToImmutableMap() {
      return ImmutableSet.of(
          ImmutableMap.copyOf(Maps.asMap(ImmutableSet.of(1), n -> n + 2)),
          ImmutableMap.copyOf(Maps.asMap(ImmutableSet.of(2), Integer::valueOf)));
    }
  }

  static final class BigDecimalTemplates {
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

  static final class CollectionTemplates {
    ImmutableSet<Boolean> testCollectionIsEmpty() {
      return ImmutableSet.of(
          ImmutableSet.of(1).size() == 0,
          ImmutableSet.of(2).size() <= 0,
          ImmutableSet.of(3).size() < 1,
          ImmutableSet.of(4).size() != 0,
          ImmutableSet.of(5).size() > 0,
          ImmutableSet.of(6).size() >= 1);
    }

    ImmutableSet<Boolean> testCollectionAddAllFromCollection() {
      return ImmutableSet.of(
          Iterables.addAll(new ArrayList<>(), new HashSet<>()),
          Iterables.addAll(new ArrayList<>(), new HashSet<>()::iterator));
    }

    ArrayList<String> testNewArrayListFromCollection() {
      return Lists.newArrayList(ImmutableList.of("foo"));
    }

    Stream<Integer> testImmutableCollectionAsListToStream() {
      return ImmutableSet.of(1).asList().stream();
    }

    ImmutableList<Integer> testImmutableCollectionAsList() {
      return ImmutableList.copyOf(ImmutableSet.of(1));
    }
  }

  static final class EqualityTemplates {
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

  static final class ImmutableListTemplates {
    ImmutableList.Builder<String> testImmutableListBuilder() {
      return new ImmutableList.Builder<>();
    }

    ImmutableList<ImmutableList<Integer>> testIterableToImmutableList() {
      return ImmutableList.of(
          ImmutableList.of(1).stream().collect(toImmutableList()),
          Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableList()),
          Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableList()),
          ImmutableList.<Integer>builder().addAll(ImmutableList.of(4)).build(),
          ImmutableList.<Integer>builder().addAll(ImmutableList.of(5)::iterator).build(),
          ImmutableList.<Integer>builder().addAll(ImmutableList.of(6).iterator()).build(),
          ImmutableList.<Integer>builder().add(new Integer[] {7}).build(),
          Stream.of(new Integer[] {8}).collect(toImmutableList()),
          Arrays.stream(new Integer[] {9}).collect(toImmutableList()));
    }

    ImmutableList<Integer> testImmutableListAsList() {
      return ImmutableList.of(1, 2, 3).asList();
    }

    ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
      return ImmutableSet.of(
          ImmutableList.sortedCopyOf(naturalOrder(), ImmutableSet.of(1)),
          ImmutableSet.of(2).stream().sorted().collect(toImmutableList()),
          Streams.stream(ImmutableSet.of(3)::iterator).sorted().collect(toImmutableList()));
    }

    ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
      return ImmutableSet.of(
          ImmutableSet.of("foo").stream()
              .sorted(Comparator.comparing(String::length))
              .collect(toImmutableList()),
          Streams.stream(ImmutableSet.of("bar")::iterator)
              .sorted(Comparator.comparing(String::isEmpty))
              .collect(toImmutableList()));
    }
  }

  static final class ImmutableSetTemplates {
    ImmutableSet.Builder<String> testImmutableSetBuilder() {
      return new ImmutableSet.Builder<>();
    }

    ImmutableSet<ImmutableSet<Integer>> testIterableToImmutableSet() {
      return ImmutableSet.of(
          ImmutableList.of(1).stream().collect(toImmutableSet()),
          Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableSet()),
          Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableSet()),
          ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(4)).build(),
          ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(5)::iterator).build(),
          ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(6).iterator()).build(),
          ImmutableSet.<Integer>builder().add(new Integer[] {7}).build(),
          Stream.of(new Integer[] {8}).collect(toImmutableSet()),
          Arrays.stream(new Integer[] {9}).collect(toImmutableSet()));
    }

    ImmutableSet<Integer> testImmutableSetCopyOfImmutableSet() {
      return ImmutableSet.copyOf(ImmutableSet.of(1, 2));
    }

    ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
      return ImmutableSet.copyOf(Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)));
    }
  }

  static final class ImmutableSortedSetTemplates {
    ImmutableSortedSet.Builder<String> testImmutableSortedSetBuilder() {
      return new ImmutableSortedSet.Builder<>(Comparator.comparingInt(String::length));
    }

    ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrderBuilder() {
      return ImmutableSortedSet.orderedBy(Comparator.<String>naturalOrder());
    }

    ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrderBuilder() {
      return ImmutableSortedSet.orderedBy(Comparator.<String>reverseOrder());
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
      return ImmutableSet.of(
          ImmutableList.of(1).stream().collect(toImmutableSortedSet(naturalOrder())),
          Streams.stream(ImmutableList.of(2)::iterator)
              .collect(toImmutableSortedSet(naturalOrder())),
          Streams.stream(ImmutableList.of(3).iterator())
              .collect(toImmutableSortedSet(naturalOrder())),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(4)).build(),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(5)::iterator).build(),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(6).iterator()).build(),
          ImmutableSortedSet.<Integer>naturalOrder().add(new Integer[] {7}).build(),
          Stream.of(new Integer[] {8}).collect(toImmutableSortedSet(naturalOrder())),
          Arrays.stream(new Integer[] {9}).collect(toImmutableSortedSet(naturalOrder())));
    }
  }

  static final class MapEntryTemplates {
    ImmutableSet<Map.Entry<String, Integer>> testMapEntry() {
      return ImmutableSet.of(
          Maps.immutableEntry("foo", 1), new AbstractMap.SimpleImmutableEntry<>("bar", 2));
    }

    ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByKey() {
      return ImmutableSet.of(
          Comparator.comparing(Map.Entry::getKey),
          Map.Entry.comparingByKey(Comparator.naturalOrder()));
    }

    Comparator<Map.Entry<Integer, String>> testMapEntryComparingByKeyWithCustomComparator() {
      return Comparator.comparing(Map.Entry::getKey, Comparator.comparingInt(i -> i * 2));
    }

    ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByValue() {
      return ImmutableSet.of(
          Comparator.comparing(Map.Entry::getValue),
          Map.Entry.comparingByValue(Comparator.naturalOrder()));
    }

    Comparator<Map.Entry<Integer, String>> testMapEntryComparingByValueWithCustomComparator() {
      return Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt(String::length));
    }
  }

  static final class NullTemplates {
    String testRequireNonNullElse() {
      return MoreObjects.firstNonNull("foo", "bar");
    }

    long testIsNullFunction() {
      return Stream.of("foo").filter(s -> s == null).count();
    }

    long testNonNullFunction() {
      return Stream.of("foo").filter(s -> s != null).count();
    }
  }

  static final class OptionalTemplates {
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

    Stream<String> testMapToOptionalGet(Map<Integer, Optional<String>> map) {
      return Stream.of(1).map(n -> map.get(n).get());
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

    ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
      return ImmutableSet.of(
          ImmutableSet.copyOf(Stream.of(1).iterator()),
          Stream.of(2).distinct().collect(toImmutableSet()));
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

  static final class StringTemplates {
    ImmutableSet<Boolean> testStringIsEmpty() {
      return ImmutableSet.of(
          "foo".length() == 0,
          "bar".length() <= 0,
          "baz".length() < 1,
          "foo".length() != 0,
          "bar".length() > 0,
          "baz".length() >= 1);
    }

    ImmutableSet<Boolean> testStringIsNullOrEmpty() {
      return ImmutableSet.of(
          getClass().getName() == null || getClass().getName().isEmpty(),
          getClass().getName() != null && !getClass().getName().isEmpty());
    }

    ImmutableSet<Optional<String>> testOptionalNonEmptyString() {
      return ImmutableSet.of(
          Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.of(toString()),
          Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.ofNullable(toString()));
    }
  }

  static final class TimeTemplates {
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
