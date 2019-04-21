import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static com.google.common.collect.Streams.stream;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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
          Ints.class,
          Iterators.class,
          Joiner.class,
          Lists.class,
          MoreObjects.class,
          Preconditions.class,
          Streams.class,
          (Runnable) () -> collectingAndThen(null, null),
          (Runnable) () -> flatteningToImmutableListMultimap(null, null),
          (Runnable) () -> flatteningToImmutableSetMultimap(null, null),
          (Runnable) () -> identity(),
          (Runnable) () -> joining(),
          (Runnable) () -> not(null),
          (Runnable) () -> toImmutableListMultimap(null, null),
          (Runnable) () -> toImmutableSetMultimap(null, null),
          (Runnable) () -> toImmutableSortedMap(null, null, null),
          (Runnable) () -> toList(),
          (Runnable) () -> toSet());

  static final class AssortedTemplates {
    int testCheckIndex() {
      return Objects.checkIndex(0, 1);
    }

    String testMapGetOrNull() {
      return ImmutableMap.of(1, "foo").get("bar");
    }

    ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
      return Stream.of(BoundType.OPEN).collect(toImmutableEnumSet());
    }

    ImmutableSet<String> testIteratorGetNextOrDefault() {
      return ImmutableSet.of(
          Iterators.getNext(ImmutableList.of("a").iterator(), "foo"),
          Iterators.getNext(ImmutableList.of("b").iterator(), "bar"),
          Iterators.getNext(ImmutableList.of("c").iterator(), "baz"));
    }

    // XXX: Only the first statement is rewritten. Make smarter.
    ImmutableSet<Boolean> testLogicalImplication() {
      return ImmutableSet.of(
          toString().isEmpty() || true,
          !toString().isEmpty() || (toString().isEmpty() && true),
          3 < 4 || (3 >= 4 && true),
          3 >= 4 || (3 < 4 && true));
    }

    Stream<String> testUnboundedSingleElementStream() {
      return Stream.generate(() -> "foo");
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

    boolean testCollectionAddAllFromCollection() {
      return new ArrayList<>().addAll(ImmutableSet.of("foo"));
    }

    void testCollectionAddAllToCollection() {
      new ArrayList<>().addAll(ImmutableSet.of("foo"));
      new ArrayList<Number>().addAll(ImmutableSet.of(1));
      new ArrayList<Number>().addAll(ImmutableSet.of(2));
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

    ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
      return ImmutableSet.of(
          ImmutableSet.of(0).stream().findFirst(),
          ImmutableSet.of(1).stream().findFirst(),
          ImmutableList.of(2).stream().findFirst(),
          ImmutableSortedSet.of(3).stream().findFirst(),
          ImmutableSet.of(1).stream().findFirst(),
          ImmutableList.of(2).stream().findFirst(),
          ImmutableSortedSet.of(3).stream().findFirst());
    }

    ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
      return ImmutableSet.of(
          new LinkedList<String>().stream().findFirst(),
          Optional.ofNullable(new LinkedList<String>().peek()),
          Optional.ofNullable(new LinkedList<String>().peek()),
          Optional.ofNullable(new LinkedList<String>().peek()),
          Optional.ofNullable(new LinkedList<String>().peek()));
    }

    ImmutableSet<Optional<String>> testRemoveOptionalFirstNavigableSetElement() {
      return ImmutableSet.of(
          Optional.ofNullable(new TreeSet<String>().pollFirst()),
          Optional.ofNullable(new TreeSet<String>().pollFirst()),
          Optional.ofNullable(new TreeSet<String>().pollFirst()),
          Optional.ofNullable(new TreeSet<String>().pollFirst()));
    }

    ImmutableSet<Optional<String>> testRemoveOptionalFirstQueueElement() {
      return ImmutableSet.of(
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()),
          Optional.ofNullable(new LinkedList<String>().poll()));
    }
  }

  static final class ComparatorTemplates {
    ImmutableSet<Comparator<String>> testNaturalOrderComparator() {
      return ImmutableSet.of(naturalOrder(), naturalOrder());
    }

    ImmutableSet<Comparator<String>> testNaturalOrderComparatorFallback() {
      return ImmutableSet.of(
          Comparator.<String>naturalOrder().thenComparing(naturalOrder()),
          Comparator.<String>naturalOrder().thenComparing(naturalOrder()));
    }

    Comparator<String> testReverseOrder() {
      return reverseOrder();
    }
  }

  static final class DoubleStreamTemplates {
    DoubleStream testConcatOneDoubleStream() {
      return DoubleStream.of(1);
    }

    DoubleStream testConcatTwoDoubleStreams() {
      return DoubleStream.concat(DoubleStream.of(1), DoubleStream.of(2));
    }

    DoubleStream testFilterOuterDoubleStreamAfterFlatMap() {
      return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
    }

    DoubleStream testFilterOuterStreamAfterFlatMapToDouble() {
      return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
    }

    DoubleStream testMapOuterDoubleStreamAfterFlatMap() {
      return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).map(n -> n * 1);
    }

    DoubleStream testMapOuterStreamAfterFlatMapToDouble() {
      return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).map(n -> n * 1);
    }

    DoubleStream testFlatMapOuterDoubleStreamAfterFlatMap() {
      return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
    }

    DoubleStream testFlatMapOuterStreamAfterFlatMapToDouble() {
      return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
    }

    ImmutableSet<Boolean> testDoubleStreamIsEmpty() {
      return ImmutableSet.of(
          DoubleStream.of(1).findAny().isEmpty(),
          DoubleStream.of(2).findAny().isEmpty(),
          DoubleStream.of(3).findAny().isEmpty(),
          DoubleStream.of(4).findAny().isEmpty());
    }

    ImmutableSet<Boolean> testDoubleStreamIsNotEmpty() {
      return ImmutableSet.of(
          DoubleStream.of(1).findAny().isPresent(),
          DoubleStream.of(2).findAny().isPresent(),
          DoubleStream.of(3).findAny().isPresent(),
          DoubleStream.of(4).findAny().isPresent());
    }

    ImmutableSet<Boolean> testDoubleStreamNoneMatch() {
      DoublePredicate pred = i -> i > 0;
      return ImmutableSet.of(
          DoubleStream.of(1).noneMatch(n -> n > 1),
          DoubleStream.of(2).noneMatch(pred),
          DoubleStream.of(3).noneMatch(pred));
    }

    boolean testDoubleStreamNoneMatch2() {
      return DoubleStream.of(1).noneMatch(n -> n > 1);
    }

    ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
      return ImmutableSet.of(
          DoubleStream.of(1).anyMatch(n -> n > 1), DoubleStream.of(2).anyMatch(n -> n > 2));
    }

    ImmutableSet<Boolean> testDoubleStreamAllMatch() {
      DoublePredicate pred = i -> i > 0;
      return ImmutableSet.of(
          DoubleStream.of(1).allMatch(pred),
          DoubleStream.of(2).allMatch(pred),
          DoubleStream.of(3).allMatch(pred));
    }

    ImmutableSet<Boolean> testDoubleStreamAllMatch2() {
      return ImmutableSet.of(
          DoubleStream.of(1).allMatch(n -> n > 1),
          DoubleStream.of(2).allMatch(n -> n > 2),
          DoubleStream.of(3).allMatch(n -> n > 3));
    }
  }

  static final class EqualityTemplates {
    ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
      return ImmutableSet.of(
          true == false,
          (byte) 0 == (byte) 1,
          (short) 0 == (short) 1,
          0 == 1,
          0L == 1L,
          0F == 1F,
          0.0 == 1.0,
          Objects.equals(Boolean.TRUE, Boolean.FALSE),
          Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
          Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
          Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
          Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
          Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
          RoundingMode.UP == RoundingMode.DOWN,
          RoundingMode.UP == RoundingMode.DOWN,
          true != false,
          (byte) 0 != (byte) 1,
          (short) 0 != (short) 1,
          0 != 1,
          0L != 1L,
          0F != 1F,
          0.0 != 1.0,
          !Objects.equals(Boolean.TRUE, Boolean.FALSE),
          !Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
          !Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
          !Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          !Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
          !Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
          !Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
          RoundingMode.UP != RoundingMode.DOWN,
          RoundingMode.UP != RoundingMode.DOWN);
    }

    boolean testEqualsPredicate() {
      // XXX: When boxing is involved this rule seems to break. Example:
      // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
      return Stream.of("foo").anyMatch("bar"::equals);
    }

    boolean testDoubleNegation() {
      return true;
    }

    ImmutableSet<Boolean> testNegation() {
      return ImmutableSet.of(
          true != false,
          true != false,
          (byte) 3 != (byte) 4,
          (short) 3 != (short) 4,
          3 != 4,
          3L != 4L,
          3F != 4F,
          3.0 != 4.0,
          BoundType.OPEN != BoundType.CLOSED);
    }

    ImmutableSet<Boolean> testIndirectDoubleNegation() {
      return ImmutableSet.of(
          true == false,
          true == false,
          (byte) 3 == (byte) 4,
          (short) 3 == (short) 4,
          3 == 4,
          3L == 4L,
          3F == 4F,
          3.0 == 4.0,
          BoundType.OPEN == BoundType.CLOSED);
    }
  }

  static final class ImmutableListMultimapTemplates {
    ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
      return ImmutableSet.of(
          ImmutableListMultimap.builder(),
          ImmutableListMultimap.builder(),
          ImmutableListMultimap.builder());
    }

    ImmutableSet<ImmutableMultimap<String, Integer>> testEmptyImmutableListMultimap() {
      return ImmutableSet.of(
          ImmutableListMultimap.of(), ImmutableListMultimap.of(), ImmutableListMultimap.of());
    }

    ImmutableSet<ImmutableMultimap<String, Integer>> testPairToImmutableListMultimap() {
      return ImmutableSet.of(
          ImmutableListMultimap.of("foo", 1),
          ImmutableListMultimap.of("bar", 2),
          ImmutableListMultimap.of("baz", 3));
    }

    ImmutableList<ImmutableMultimap<String, Integer>> testEntryToImmutableListMultimap() {
      return ImmutableList.of(
          ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
    }

    ImmutableList<ImmutableMultimap<String, Integer>> testIterableToImmutableListMultimap() {
      return ImmutableList.of(
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
          ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
          ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
    }

    ImmutableListMultimap<Integer, String> testStreamOfMapEntriesToImmutableListMultimap() {
      // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
      // Looks like a @Placeholder limitation. Try to track down and fix.
      return Stream.of(1, 2, 3)
          .collect(toImmutableListMultimap(n -> Integer.valueOf(n), n -> n.toString()));
    }

    ImmutableSet<ImmutableListMultimap<Integer, Integer>>
        testIndexIterableToImmutableListMultimap() {
      return ImmutableSet.of(
          Multimaps.index(ImmutableList.of(1), n -> n * 2),
          Multimaps.index(ImmutableList.of(2)::iterator, Integer::valueOf),
          Multimaps.index(ImmutableList.of(3).iterator(), n -> n.intValue()));
    }

    ImmutableListMultimap<String, Integer> testTransformMultimapValuesToImmutableListMultimap() {
      return ImmutableListMultimap.copyOf(
          Multimaps.transformValues(ImmutableListMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
    }

    ImmutableSet<ImmutableListMultimap<String, Integer>>
        testTransformMultimapValuesToImmutableListMultimap2() {
      return ImmutableSet.of(
          ImmutableListMultimap.copyOf(
              Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
          ImmutableListMultimap.copyOf(
              Multimaps.transformValues(
                  (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                  n -> Math.toIntExact(n))),
          ImmutableListMultimap.copyOf(
              Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
          ImmutableListMultimap.copyOf(
              Multimaps.transformValues(
                  ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
          ImmutableListMultimap.copyOf(
              Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
    }

    ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfImmutableListMultimap() {
      return ImmutableListMultimap.of("foo", 1);
    }
  }

  static final class ImmutableListTemplates {
    ImmutableList.Builder<String> testImmutableListBuilder() {
      return ImmutableList.builder();
    }

    ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
      return ImmutableSet.of(ImmutableList.of(), ImmutableList.of());
    }

    ImmutableSet<ImmutableList<Integer>> testIterableToImmutableList() {
      return ImmutableSet.of(
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

    ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableList()),
          Stream.of(2).collect(toImmutableList()),
          Stream.of(3).collect(toImmutableList()));
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

  static final class ImmutableMapTemplates {
    ImmutableMap.Builder<String, Integer> testImmutableMapBuilder() {
      return ImmutableMap.builder();
    }

    ImmutableMap<String, Integer> testEmptyImmutableMap() {
      return ImmutableMap.of();
    }

    ImmutableMap<String, Integer> testPairToImmutableMap() {
      return ImmutableMap.of("foo", 1);
    }

    ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
      return ImmutableSet.of(
          ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToImmutableMap() {
      return ImmutableSet.of(
          Maps.toMap(ImmutableList.of(1), n -> n * 2),
          Maps.toMap(ImmutableList.of(2)::iterator, Integer::valueOf),
          Maps.toMap(ImmutableList.of(3).iterator(), n -> n.intValue()),
          Maps.toMap(ImmutableSet.of(4), Integer::valueOf));
    }

    ImmutableSet<ImmutableMap<String, Integer>> testEntryIterableToImmutableMap() {
      return ImmutableSet.of(
          ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
          ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
          ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
          ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
          ImmutableMap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
    }

    ImmutableMap<Integer, String> testStreamOfMapEntriesToImmutableMap() {
      // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
      // Looks like a @Placeholder limitation. Try to track down and fix.
      return Stream.of(1, 2, 3).collect(toImmutableMap(n -> Integer.valueOf(n), n -> n.toString()));
    }

    ImmutableSet<ImmutableMap<Integer, Integer>> testIndexIterableToImmutableMap() {
      return ImmutableSet.of(
          Maps.uniqueIndex(ImmutableList.of(1), n -> n * 2),
          Maps.uniqueIndex(ImmutableList.of(2)::iterator, Integer::valueOf),
          Maps.uniqueIndex(ImmutableList.of(3).iterator(), n -> n.intValue()));
    }

    ImmutableMap<String, Integer> testTransformMapValuesToImmutableMap() {
      return ImmutableMap.copyOf(
          Maps.transformValues(ImmutableMap.of("foo", 1L), v -> Math.toIntExact(v)));
    }

    ImmutableMap<String, Integer> testImmutableMapCopyOfImmutableMap() {
      return ImmutableMap.of("foo", 1);
    }
  }

  static final class ImmutableMultisetTemplates {
    ImmutableMultiset.Builder<String> testImmutableMultisetBuilder() {
      return ImmutableMultiset.builder();
    }

    ImmutableMultiset<ImmutableMultiset<Integer>> testEmptyImmutableMultiset() {
      return ImmutableMultiset.of(ImmutableMultiset.of(), ImmutableMultiset.of());
    }

    ImmutableMultiset<ImmutableMultiset<Integer>> testIterableToImmutableMultiset() {
      return ImmutableMultiset.of(
          ImmutableMultiset.copyOf(ImmutableList.of(1)),
          ImmutableMultiset.copyOf(ImmutableList.of(2)::iterator),
          ImmutableMultiset.copyOf(ImmutableList.of(3).iterator()),
          ImmutableMultiset.copyOf(ImmutableMultiset.of(4)),
          ImmutableMultiset.copyOf(ImmutableMultiset.of(5)::iterator),
          ImmutableMultiset.copyOf(ImmutableMultiset.of(6).iterator()),
          ImmutableMultiset.copyOf(new Integer[] {7}),
          ImmutableMultiset.copyOf(new Integer[] {8}),
          ImmutableMultiset.copyOf(new Integer[] {9}));
    }

    ImmutableSet<ImmutableMultiset<Integer>> testStreamToImmutableMultiset() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableMultiset()),
          Stream.of(2).collect(toImmutableMultiset()),
          Stream.of(3).collect(toImmutableMultiset()));
    }

    ImmutableMultiset<Integer> testImmutableMultisetCopyOfImmutableMultiset() {
      return ImmutableMultiset.of(1, 2);
    }
  }

  static final class ImmutableSetMultimapTemplates {
    ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
      return ImmutableSetMultimap.builder();
    }

    ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
      return ImmutableSetMultimap.of();
    }

    ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
      return ImmutableSetMultimap.of("foo", 1);
    }

    ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
      return ImmutableSet.of(
          ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
    }

    ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
      return ImmutableSet.of(
          ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
          ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
          ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
          ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
          ImmutableSetMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
    }

    ImmutableSetMultimap<Integer, String> testStreamOfMapEntriesToImmutableSetMultimap() {
      // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
      // Looks like a @Placeholder limitation. Try to track down and fix.
      return Stream.of(1, 2, 3)
          .collect(toImmutableSetMultimap(n -> Integer.valueOf(n), n -> n.toString()));
    }

    ImmutableSetMultimap<String, Integer> testTransformMultimapValuesToImmutableSetMultimap() {
      return ImmutableSetMultimap.copyOf(
          Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
    }

    ImmutableSet<ImmutableSetMultimap<String, Integer>>
        testTransformMultimapValuesToImmutableSetMultimap2() {
      return ImmutableSet.of(
          ImmutableSetMultimap.copyOf(
              Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
          ImmutableSetMultimap.copyOf(
              Multimaps.transformValues(
                  (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                  n -> Math.toIntExact(n))),
          ImmutableSetMultimap.copyOf(
              Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
          ImmutableSetMultimap.copyOf(
              Multimaps.transformValues(
                  ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
          ImmutableSetMultimap.copyOf(
              Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
    }

    ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfImmutableSetMultimap() {
      return ImmutableSetMultimap.of("foo", 1);
    }
  }

  static final class ImmutableSetTemplates {
    ImmutableSet.Builder<String> testImmutableSetBuilder() {
      return ImmutableSet.builder();
    }

    ImmutableSet<ImmutableSet<Integer>> testEmptyImmutableSet() {
      return ImmutableSet.of(ImmutableSet.of(), ImmutableSet.of());
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

    ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableSet()),
          Stream.of(2).collect(toImmutableSet()),
          Stream.of(3).collect(toImmutableSet()),
          Stream.of(4).collect(toImmutableSet()),
          Stream.of(5).collect(toImmutableSet()));
    }

    ImmutableSet<Integer> testImmutableSetCopyOfImmutableSet() {
      return ImmutableSet.of(1, 2);
    }

    ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
      return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
    }
  }

  static final class ImmutableSortedMapBuilder {
    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapBuilder() {
      return ImmutableSortedMap.orderedBy(Comparator.comparingInt(String::length));
    }

    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
      return ImmutableSortedMap.naturalOrder();
    }

    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
      return ImmutableSortedMap.reverseOrder();
    }

    ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
      return ImmutableSortedMap.of();
    }

    ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
      return ImmutableSortedMap.of("foo", 1);
    }

    ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
      return ImmutableSet.of(
          ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
          ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
    }

    ImmutableSet<ImmutableSortedMap<String, Integer>> testIterableToImmutableSortedMap() {
      return ImmutableSet.of(
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
          ImmutableSortedMap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
    }
  }

  static final class ImmutableSortedMultisetTemplates {
    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetBuilder() {
      return ImmutableSortedMultiset.orderedBy(Comparator.comparingInt(String::length));
    }

    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetNaturalOrderBuilder() {
      return ImmutableSortedMultiset.naturalOrder();
    }

    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetReverseOrderBuilder() {
      return ImmutableSortedMultiset.reverseOrder();
    }

    ImmutableMultiset<ImmutableSortedMultiset<Integer>> testEmptyImmutableSortedMultiset() {
      return ImmutableMultiset.of(ImmutableSortedMultiset.of(), ImmutableSortedMultiset.of());
    }

    ImmutableMultiset<ImmutableSortedMultiset<Integer>> testIterableToImmutableSortedMultiset() {
      return ImmutableMultiset.of(
          ImmutableSortedMultiset.copyOf(ImmutableList.of(1)),
          ImmutableSortedMultiset.copyOf(ImmutableList.of(2).iterator()),
          ImmutableSortedMultiset.copyOf(ImmutableList.of(3)),
          ImmutableSortedMultiset.copyOf(ImmutableList.of(4)::iterator),
          ImmutableSortedMultiset.copyOf(ImmutableList.of(5).iterator()),
          ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(6)),
          ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(7)::iterator),
          ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(8).iterator()),
          ImmutableSortedMultiset.copyOf(new Integer[] {9}),
          ImmutableSortedMultiset.copyOf(new Integer[] {10}),
          ImmutableSortedMultiset.copyOf(new Integer[] {11}));
    }

    ImmutableSet<ImmutableSortedMultiset<Integer>> testStreamToImmutableSortedMultiset() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableSortedMultiset(naturalOrder())),
          Stream.of(2).collect(toImmutableSortedMultiset(naturalOrder())),
          Stream.of(3).collect(toImmutableSortedMultiset(naturalOrder())));
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

    ImmutableSet<ImmutableSortedSet<Integer>> testEmptyImmutableSortedSet() {
      return ImmutableSet.of(ImmutableSortedSet.of(), ImmutableSortedSet.of());
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
      // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
      return ImmutableSet.of(
          ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(1)),
          ImmutableSortedSet.copyOf(ImmutableList.of(2).iterator()),
          ImmutableSortedSet.copyOf(ImmutableList.of(3)),
          ImmutableSortedSet.copyOf(ImmutableList.of(4)::iterator),
          ImmutableSortedSet.copyOf(ImmutableList.of(5).iterator()),
          ImmutableSortedSet.copyOf(ImmutableSet.of(6)),
          ImmutableSortedSet.copyOf(ImmutableSet.of(7)::iterator),
          ImmutableSortedSet.copyOf(ImmutableSet.of(8).iterator()),
          ImmutableSortedSet.copyOf(new Integer[] {9}),
          ImmutableSortedSet.copyOf(new Integer[] {10}),
          ImmutableSortedSet.copyOf(new Integer[] {11}));
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testStreamToImmutableSortedSet() {
      return ImmutableSet.of(
          Stream.of(1).collect(toImmutableSortedSet(naturalOrder())),
          Stream.of(2).collect(toImmutableSortedSet(naturalOrder())),
          Stream.of(3).collect(toImmutableSortedSet(naturalOrder())));
    }
  }

  static final class IntStreamTemplates {
    IntStream testIntStreamClosedOpenRange() {
      return IntStream.range(0, 42);
    }

    IntStream testConcatOneIntStream() {
      return IntStream.of(1);
    }

    IntStream testConcatTwoIntStreams() {
      return IntStream.concat(IntStream.of(1), IntStream.of(2));
    }

    IntStream testFilterOuterIntStreamAfterFlatMap() {
      return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).filter(n -> n > 1);
    }

    IntStream testFilterOuterStreamAfterFlatMapToInt() {
      return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).filter(n -> n > 1);
    }

    IntStream testMapOuterIntStreamAfterFlatMap() {
      return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).map(n -> n * 1);
    }

    IntStream testMapOuterStreamAfterFlatMapToInt() {
      return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).map(n -> n * 1);
    }

    IntStream testFlatMapOuterIntStreamAfterFlatMap() {
      return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).flatMap(IntStream::of);
    }

    IntStream testFlatMapOuterStreamAfterFlatMapToInt() {
      return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).flatMap(IntStream::of);
    }

    ImmutableSet<Boolean> testIntStreamIsEmpty() {
      return ImmutableSet.of(
          IntStream.of(1).findAny().isEmpty(),
          IntStream.of(2).findAny().isEmpty(),
          IntStream.of(3).findAny().isEmpty(),
          IntStream.of(4).findAny().isEmpty());
    }

    ImmutableSet<Boolean> testIntStreamIsNotEmpty() {
      return ImmutableSet.of(
          IntStream.of(1).findAny().isPresent(),
          IntStream.of(2).findAny().isPresent(),
          IntStream.of(3).findAny().isPresent(),
          IntStream.of(4).findAny().isPresent());
    }

    ImmutableSet<Boolean> testIntStreamNoneMatch() {
      IntPredicate pred = i -> i > 0;
      return ImmutableSet.of(
          IntStream.of(1).noneMatch(n -> n > 1),
          IntStream.of(2).noneMatch(pred),
          IntStream.of(3).noneMatch(pred));
    }

    boolean testIntStreamNoneMatch2() {
      return IntStream.of(1).noneMatch(n -> n > 1);
    }

    ImmutableSet<Boolean> testIntStreamAnyMatch() {
      return ImmutableSet.of(
          IntStream.of(1).anyMatch(n -> n > 1), IntStream.of(2).anyMatch(n -> n > 2));
    }

    ImmutableSet<Boolean> testIntStreamAllMatch() {
      IntPredicate pred = i -> i > 0;
      return ImmutableSet.of(
          IntStream.of(1).allMatch(pred),
          IntStream.of(2).allMatch(pred),
          IntStream.of(3).allMatch(pred));
    }

    ImmutableSet<Boolean> testIntStreamAllMatch2() {
      return ImmutableSet.of(
          IntStream.of(1).allMatch(n -> n > 1),
          IntStream.of(2).allMatch(n -> n > 2),
          IntStream.of(3).allMatch(n -> n > 3));
    }
  }

  static final class LongStreamTemplates {
    LongStream testLongStreamClosedOpenRange() {
      return LongStream.range(0, 42);
    }

    LongStream testConcatOneLongStream() {
      return LongStream.of(1);
    }

    LongStream testConcatTwoLongStreams() {
      return LongStream.concat(LongStream.of(1), LongStream.of(2));
    }

    LongStream testFilterOuterLongStreamAfterFlatMap() {
      return LongStream.of(1).flatMap(v -> LongStream.of(v * v)).filter(n -> n > 1);
    }

    LongStream testFilterOuterStreamAfterFlatMapToLong() {
      return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v)).filter(n -> n > 1);
    }

    LongStream testMapOuterLongStreamAfterFlatMap() {
      return LongStream.of(1).flatMap(v -> LongStream.of(v * v)).map(n -> n * 1);
    }

    LongStream testMapOuterStreamAfterFlatMapToLong() {
      return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v)).map(n -> n * 1);
    }

    LongStream testFlatMapOuterLongStreamAfterFlatMap() {
      return LongStream.of(1).flatMap(v -> LongStream.of(v * v)).flatMap(LongStream::of);
    }

    LongStream testFlatMapOuterStreamAfterFlatMapToLong() {
      return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v)).flatMap(LongStream::of);
    }

    ImmutableSet<Boolean> testLongStreamIsEmpty() {
      return ImmutableSet.of(
          LongStream.of(1).findAny().isEmpty(),
          LongStream.of(2).findAny().isEmpty(),
          LongStream.of(3).findAny().isEmpty(),
          LongStream.of(4).findAny().isEmpty());
    }

    ImmutableSet<Boolean> testLongStreamIsNotEmpty() {
      return ImmutableSet.of(
          LongStream.of(1).findAny().isPresent(),
          LongStream.of(2).findAny().isPresent(),
          LongStream.of(3).findAny().isPresent(),
          LongStream.of(4).findAny().isPresent());
    }

    ImmutableSet<Boolean> testLongStreamNoneMatch() {
      LongPredicate pred = i -> i > 0;
      return ImmutableSet.of(
          LongStream.of(1).noneMatch(n -> n > 1),
          LongStream.of(2).noneMatch(pred),
          LongStream.of(3).noneMatch(pred));
    }

    boolean testLongStreamNoneMatch2() {
      return LongStream.of(1).noneMatch(n -> n > 1);
    }

    ImmutableSet<Boolean> testLongStreamAnyMatch() {
      return ImmutableSet.of(
          LongStream.of(1).anyMatch(n -> n > 1), LongStream.of(2).anyMatch(n -> n > 2));
    }

    ImmutableSet<Boolean> testLongStreamAllMatch() {
      LongPredicate pred = i -> i > 0;
      return ImmutableSet.of(
          LongStream.of(1).allMatch(pred),
          LongStream.of(2).allMatch(pred),
          LongStream.of(3).allMatch(pred));
    }

    ImmutableSet<Boolean> testLongStreamAllMatch2() {
      return ImmutableSet.of(
          LongStream.of(1).allMatch(n -> n > 1),
          LongStream.of(2).allMatch(n -> n > 2),
          LongStream.of(3).allMatch(n -> n > 3));
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
      return requireNonNullElse("foo", "bar");
    }

    long testIsNullFunction() {
      return Stream.of("foo").filter(Objects::isNull).count();
    }

    long testNonNullFunction() {
      return Stream.of("foo").filter(Objects::nonNull).count();
    }
  }

  static final class OptionalTemplates {
    ImmutableSet<Optional<String>> testOptionalOfNullable() {
      return ImmutableSet.of(Optional.ofNullable(toString()), Optional.ofNullable(toString()));
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

    ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
      return ImmutableSet.of(
          stream(ImmutableSet.of("foo").iterator()).findFirst(),
          stream(ImmutableSet.of("foo").iterator()).findFirst());
    }

    ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
      return ImmutableSet.of(
          /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
              .filter(v -> v.length() > 5),
          /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
              .filter(v -> !v.contains("baz")));
    }

    ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
      return ImmutableSet.of(
          /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
              .filter(v -> v.length() <= 5),
          /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
              .filter(v -> v.contains("baz")));
    }

    ImmutableSet<Boolean> testMapOptionalToBoolean() {
      return ImmutableSet.of(
          Optional.of("foo").filter(String::isEmpty).isPresent(),
          Optional.of("bar").filter(s -> s.isEmpty()).isPresent());
    }

    ImmutableSet<Optional<String>> testMapToNullable() {
      return ImmutableSet.of(
          Optional.of(1).map(n -> String.valueOf(n)), Optional.of(2).map(n -> String.valueOf(n)));
    }

    Optional<String> testMapToOptionalGet() {
      return Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n)));
    }

    String testOrElseGetToOptionalGet() {
      return Optional.of("foo").or(() -> Optional.of("bar")).get();
    }

    Stream<Object> testStreamFlatmapOptional() {
      return Stream.concat(
          Stream.of(Optional.empty()).flatMap(Optional::stream),
          Stream.of(Optional.of("foo")).flatMap(Optional::stream));
    }

    Stream<String> testStreamMapToOptionalGet() {
      return Stream.of(1).flatMap(n -> Optional.of(String.valueOf(n)).stream());
    }

    Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length())).filter(len -> len > 0);
    }

    Optional<Integer> testMapOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length())).map(len -> len * 0);
    }

    Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length())).flatMap(Optional::of);
    }
  }

  static final class PrimitiveTemplates {
    ImmutableSet<Boolean> testLessThan() {
      return ImmutableSet.of(
          (byte) 3 < (byte) 4, (short) 3 < (short) 4, 3 < 4, 3L < 4L, 3F < 4F, 3.0 < 4.0);
    }

    ImmutableSet<Boolean> testLessThanOrEqualTo() {
      return ImmutableSet.of(
          (byte) 3 <= (byte) 4, (short) 3 <= (short) 4, 3 <= 4, 3L <= 4L, 3F <= 4F, 3.0 <= 4.0);
    }

    ImmutableSet<Boolean> testGreaterThan() {
      return ImmutableSet.of(
          (byte) 3 > (byte) 4, (short) 3 > (short) 4, 3 > 4, 3L > 4L, 3F > 4F, 3.0 > 4.0);
    }

    ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
      return ImmutableSet.of(
          (byte) 3 >= (byte) 4, (short) 3 >= (short) 4, 3 >= 4, 3L >= 4L, 3F >= 4F, 3.0 >= 4.0);
    }

    int testLongToIntExact() {
      return Math.toIntExact(Long.MAX_VALUE);
    }
  }

  static final class StreamTemplates {
    ImmutableSet<Stream<String>> testStreamOfNullable() {
      return ImmutableSet.of(Stream.ofNullable("a"), Stream.ofNullable("b"));
    }

    Stream<Integer> testConcatOneStream() {
      return Stream.of(1);
    }

    Stream<Integer> testConcatTwoStreams() {
      return Stream.concat(Stream.of(1), Stream.of(2));
    }

    Stream<Integer> testFilterOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length())).filter(len -> len > 0);
    }

    Stream<Integer> testMapOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length())).map(len -> len * 0);
    }

    Stream<Integer> testFlatMapOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length())).flatMap(Stream::of);
    }

    ImmutableSet<Optional<Integer>> testStreamMapFirst() {
      return ImmutableSet.of(
          Stream.of("foo").findFirst().map(s -> s.length()),
          Stream.of("bar").findFirst().map(String::length));
    }

    ImmutableSet<Boolean> testStreamIsEmpty() {
      return ImmutableSet.of(
          Stream.of(1).findAny().isEmpty(),
          Stream.of(2).findAny().isEmpty(),
          Stream.of(3).findAny().isEmpty(),
          Stream.of(4).findAny().isEmpty());
    }

    ImmutableSet<Boolean> testStreamIsNotEmpty() {
      return ImmutableSet.of(
          Stream.of(1).findAny().isPresent(),
          Stream.of(2).findAny().isPresent(),
          Stream.of(3).findAny().isPresent(),
          Stream.of(4).findAny().isPresent());
    }

    ImmutableSet<Boolean> testStreamNoneMatch() {
      Predicate<String> pred = String::isBlank;
      return ImmutableSet.of(
          Stream.of("foo").noneMatch(s -> s.length() > 1),
          Stream.of("bar").noneMatch(String::isBlank),
          Stream.of("baz").noneMatch(pred),
          Stream.of("qux").noneMatch(String::isEmpty));
    }

    boolean testStreamNoneMatch2() {
      return Stream.of("foo").noneMatch(s -> s.isBlank());
    }

    ImmutableSet<Boolean> testStreamAnyMatch() {
      return ImmutableSet.of(
          Stream.of("foo").anyMatch(s -> s.length() > 1),
          Stream.of("bar").anyMatch(String::isEmpty));
    }

    ImmutableSet<Boolean> testStreamAllMatch() {
      Predicate<String> pred = String::isBlank;
      return ImmutableSet.of(
          Stream.of("foo").allMatch(String::isBlank),
          Stream.of("bar").allMatch(pred),
          Stream.of("baz").allMatch(s -> s.length() > 1),
          Stream.of("qux").allMatch(pred),
          Stream.of("quux").allMatch(String::isEmpty),
          Stream.of("quuz").allMatch(pred));
    }

    ImmutableSet<Boolean> testStreamAllMatch2() {
      return ImmutableSet.of(
          Stream.of("foo").allMatch(s -> s.isBlank()),
          Stream.of("bar").allMatch(s -> s.isEmpty()),
          Stream.of("baz").allMatch(s -> s.isBlank()));
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
          Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
          Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
          Optional.ofNullable(toString()).filter(s -> !s.isEmpty()));
    }

    Optional<String> testFilterEmptyString() {
      return Optional.of("foo").filter(s -> !s.isEmpty());
    }

    ImmutableSet<String> testJoinStrings() {
      return ImmutableSet.of(
          String.join("a", new String[] {"foo", "bar"}),
          String.join("b", new CharSequence[] {"baz", "qux"}),
          String.join("c", new String[] {"foo", "bar"}),
          String.join("d", new CharSequence[] {"baz", "qux"}),
          String.join("e", ImmutableList.of("foo", "bar")),
          String.join("f", Iterables.cycle(ImmutableList.of("foo", "bar"))),
          String.join("g", ImmutableList.of("baz", "qux")));
    }
  }

  static final class TimeTemplates {
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
  }
}
