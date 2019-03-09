import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;

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
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
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
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
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
          (Runnable) () -> identity(),
          (Runnable) () -> joining(),
          (Runnable) () -> toImmutableListMultimap(null, null),
          (Runnable) () -> toImmutableSetMultimap(null, null),
          (Runnable) () -> toImmutableSortedMap(null, null, null));

  static final class AssortedTemplates {
    int testCheckIndex() {
      return Preconditions.checkElementIndex(0, 1);
    }

    String testMapGetOrNull() {
      return ImmutableMap.of(1, "foo").getOrDefault("bar", null);
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

    ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
      return Stream.of(BoundType.OPEN).collect(toImmutableSet());
    }

    ImmutableMap<String, Integer> testTransformMapValueToImmutableMap() {
      return ImmutableMap.of("foo", 1L).entrySet().stream()
          .collect(toImmutableMap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
    }

    ImmutableSet<String> testIteratorGetNextOrDefault() {
      return ImmutableSet.of(
          ImmutableList.of("a").iterator().hasNext()
              ? ImmutableList.of("a").iterator().next()
              : "foo",
          Streams.stream(ImmutableList.of("b").iterator()).findFirst().orElse("bar"),
          Streams.stream(ImmutableList.of("c").iterator()).findAny().orElse("baz"));
    }

    // XXX: Only the first statement is rewritten. Make smarter.
    ImmutableSet<Boolean> testLogicalImplication() {
      return ImmutableSet.of(
          toString().isEmpty() || (!toString().isEmpty() && true),
          !toString().isEmpty() || (toString().isEmpty() && true),
          3 < 4 || (3 >= 4 && true),
          3 >= 4 || (3 < 4 && true));
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

    ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
      return ImmutableSet.of(
          ImmutableSet.of(0).stream().findAny(),
          ImmutableSet.of(1).isEmpty()
              ? Optional.empty()
              : Optional.of(ImmutableSet.of(1).iterator().next()),
          ImmutableList.of(2).isEmpty()
              ? Optional.empty()
              : Optional.of(ImmutableList.of(2).get(0)),
          ImmutableSortedSet.of(3).isEmpty()
              ? Optional.empty()
              : Optional.of(ImmutableSortedSet.of(3).first()),
          !ImmutableSet.of(1).isEmpty()
              ? Optional.of(ImmutableSet.of(1).iterator().next())
              : Optional.empty(),
          !ImmutableList.of(2).isEmpty()
              ? Optional.of(ImmutableList.of(2).get(0))
              : Optional.empty(),
          !ImmutableSortedSet.of(3).isEmpty()
              ? Optional.of(ImmutableSortedSet.of(3).first())
              : Optional.empty());
    }

    ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
      return ImmutableSet.of(
          new LinkedList<String>().stream().findAny(),
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.of(new LinkedList<String>().peek()),
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.ofNullable(new LinkedList<String>().peek()),
          !new LinkedList<String>().isEmpty()
              ? Optional.of(new LinkedList<String>().peek())
              : Optional.empty(),
          !new LinkedList<String>().isEmpty()
              ? Optional.ofNullable(new LinkedList<String>().peek())
              : Optional.empty());
    }

    ImmutableSet<Optional<String>> testRemoveOptionalFirstNavigableSetElement() {
      return ImmutableSet.of(
          new TreeSet<String>().isEmpty()
              ? Optional.empty()
              : Optional.of(new TreeSet<String>().pollFirst()),
          new TreeSet<String>().isEmpty()
              ? Optional.empty()
              : Optional.ofNullable(new TreeSet<String>().pollFirst()),
          !new TreeSet<String>().isEmpty()
              ? Optional.of(new TreeSet<String>().pollFirst())
              : Optional.empty(),
          !new TreeSet<String>().isEmpty()
              ? Optional.ofNullable(new TreeSet<String>().pollFirst())
              : Optional.empty());
    }

    ImmutableSet<Optional<String>> testRemoveOptionalFirstQueueElement() {
      return ImmutableSet.of(
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.of(new LinkedList<String>().poll()),
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.of(new LinkedList<String>().remove()),
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.ofNullable(new LinkedList<String>().poll()),
          new LinkedList<String>().isEmpty()
              ? Optional.empty()
              : Optional.ofNullable(new LinkedList<String>().remove()),
          !new LinkedList<String>().isEmpty()
              ? Optional.of(new LinkedList<String>().poll())
              : Optional.empty(),
          !new LinkedList<String>().isEmpty()
              ? Optional.of(new LinkedList<String>().remove())
              : Optional.empty(),
          !new LinkedList<String>().isEmpty()
              ? Optional.ofNullable(new LinkedList<String>().poll())
              : Optional.empty(),
          !new LinkedList<String>().isEmpty()
              ? Optional.ofNullable(new LinkedList<String>().remove())
              : Optional.empty());
    }
  }

  static final class ComparatorTemplates {
    ImmutableSet<Comparator<String>> testNaturalOrderComparator() {
      return ImmutableSet.of(Comparator.comparing(identity()), Comparator.comparing(s -> s));
    }

    ImmutableSet<Comparator<String>> testNaturalOrderComparatorFallback() {
      return ImmutableSet.of(
          Comparator.<String>naturalOrder().thenComparing(identity()),
          Comparator.<String>naturalOrder().thenComparing(s -> s));
    }

    Comparator<String> testReverseOrder() {
      return Comparator.<String>naturalOrder().reversed();
    }
  }

  static final class EqualityTemplates {
    ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
      return ImmutableSet.of(
          Objects.equals(true, false),
          Objects.equals((byte) 0, (byte) 1),
          Objects.equals((short) 0, (short) 1),
          Objects.equals(0, 1),
          Objects.equals(0L, 1L),
          Objects.equals(0F, 1F),
          Objects.equals(0.0, 1.0),
          Objects.equals(Boolean.TRUE, Boolean.FALSE),
          Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
          Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
          Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
          Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
          Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
          RoundingMode.UP.equals(RoundingMode.DOWN),
          Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
          !Objects.equals(true, false),
          !Objects.equals((byte) 0, (byte) 1),
          !Objects.equals((short) 0, (short) 1),
          !Objects.equals(0, 1),
          !Objects.equals(0L, 1L),
          !Objects.equals(0F, 1F),
          !Objects.equals(0.0, 1.0),
          !Objects.equals(Boolean.TRUE, Boolean.FALSE),
          !Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
          !Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
          !Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
          !Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
          !Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
          !Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
          !RoundingMode.UP.equals(RoundingMode.DOWN),
          !Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
    }

    boolean testEqualsPredicate() {
      // XXX: When boxing is involved this rule seems to break. Example:
      // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
      return Stream.of("foo").anyMatch(s -> "bar".equals(s));
    }

    boolean testDoubleNegation() {
      return !!true;
    }

    ImmutableSet<Boolean> testNegation() {
      return ImmutableSet.of(
          true ? !false : false,
          !(true == false),
          !((byte) 3 == (byte) 4),
          !((short) 3 == (short) 4),
          !(3 == 4),
          !(3L == 4L),
          !(3F == 4F),
          !(3.0 == 4.0),
          !(BoundType.OPEN == BoundType.CLOSED));
    }

    ImmutableSet<Boolean> testIndirectDoubleNegation() {
      return ImmutableSet.of(
          true ? false : !false,
          !(true != false),
          !((byte) 3 != (byte) 4),
          !((short) 3 != (short) 4),
          !(3 != 4),
          !(3L != 4L),
          !(3F != 4F),
          !(3.0 != 4.0),
          !(BoundType.OPEN != BoundType.CLOSED));
    }
  }

  static final class ImmutableListMultimapTemplates {
    ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
      return ImmutableSet.of(
          new ImmutableListMultimap.Builder<>(),
          new ImmutableMultimap.Builder<>(),
          ImmutableMultimap.builder());
    }

    ImmutableSet<ImmutableMultimap<String, Integer>> testEmptyImmutableListMultimap() {
      return ImmutableSet.of(
          ImmutableListMultimap.<String, Integer>builder().build(),
          ImmutableMultimap.<String, Integer>builder().build(),
          ImmutableMultimap.of());
    }

    ImmutableSet<ImmutableMultimap<String, Integer>> testPairToImmutableListMultimap() {
      return ImmutableSet.of(
          ImmutableListMultimap.<String, Integer>builder().put("foo", 1).build(),
          ImmutableMultimap.<String, Integer>builder().put("bar", 2).build(),
          ImmutableMultimap.of("baz", 3));
    }

    ImmutableList<ImmutableMultimap<String, Integer>> testEntryToImmutableListMultimap() {
      return ImmutableList.of(
          ImmutableListMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
          Stream.of(Map.entry("foo", 1))
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          ImmutableMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
          ImmutableMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
    }

    ImmutableList<ImmutableMultimap<String, Integer>> testIterableToImmutableListMultimap() {
      return ImmutableList.of(
          ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
          ImmutableListMultimap.<String, Integer>builder()
              .putAll(ImmutableListMultimap.of("foo", 1))
              .build(),
          ImmutableListMultimap.<String, Integer>builder()
              .putAll(ImmutableListMultimap.of("foo", 1).entries())
              .build(),
          ImmutableListMultimap.of("foo", 1).entries().stream()
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          ImmutableMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
          ImmutableMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
          ImmutableMultimap.<String, Integer>builder()
              .putAll(ImmutableListMultimap.of("foo", 1))
              .build(),
          ImmutableMultimap.<String, Integer>builder()
              .putAll(ImmutableListMultimap.of("foo", 1).entries())
              .build(),
          ImmutableMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
    }

    ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfImmutableListMultimap() {
      return ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1));
    }
  }

  static final class ImmutableListTemplates {
    ImmutableList.Builder<String> testImmutableListBuilder() {
      return new ImmutableList.Builder<>();
    }

    ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
      return ImmutableSet.of(
          ImmutableList.<Integer>builder().build(),
          Stream.<Integer>empty().collect(toImmutableList()));
    }

    ImmutableSet<ImmutableList<Integer>> testIterableToImmutableList() {
      return ImmutableSet.of(
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

  static final class ImmutableMapTemplates {
    ImmutableMap.Builder<String, Integer> testImmutableMapBuilder() {
      return new ImmutableMap.Builder<>();
    }

    ImmutableMap<String, Integer> testEmptyImmutableMap() {
      return ImmutableMap.<String, Integer>builder().build();
    }

    ImmutableMap<String, Integer> testPairToImmutableMap() {
      return ImmutableMap.<String, Integer>builder().put("foo", 1).build();
    }

    ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
      return ImmutableSet.of(
          ImmutableMap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
          Stream.of(Map.entry("foo", 1))
              .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    ImmutableSet<ImmutableMap<String, Integer>> testIterableToImmutableMap() {
      return ImmutableSet.of(
          ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
          ImmutableMap.<String, Integer>builder().putAll(ImmutableMap.of("foo", 1)).build(),
          ImmutableMap.<String, Integer>builder()
              .putAll(ImmutableMap.of("foo", 1).entrySet())
              .build(),
          ImmutableMap.of("foo", 1).entrySet().stream()
              .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)),
          Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
              .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    ImmutableMap<String, Integer> testImmutableMapCopyOfImmutableMap() {
      return ImmutableMap.copyOf(ImmutableMap.of("foo", 1));
    }
  }

  static final class ImmutableMultisetTemplates {
    ImmutableMultiset.Builder<String> testImmutableMultisetBuilder() {
      return new ImmutableMultiset.Builder<>();
    }

    ImmutableMultiset<ImmutableMultiset<Integer>> testEmptyImmutableMultiset() {
      return ImmutableMultiset.of(
          ImmutableMultiset.<Integer>builder().build(),
          Stream.<Integer>empty().collect(toImmutableMultiset()));
    }

    ImmutableMultiset<ImmutableMultiset<Integer>> testIterableToImmutableMultiset() {
      return ImmutableMultiset.of(
          ImmutableList.of(1).stream().collect(toImmutableMultiset()),
          Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableMultiset()),
          Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableMultiset()),
          ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(4)).build(),
          ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(5)::iterator).build(),
          ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(6).iterator()).build(),
          ImmutableMultiset.<Integer>builder().add(new Integer[] {7}).build(),
          Stream.of(new Integer[] {8}).collect(toImmutableMultiset()),
          Arrays.stream(new Integer[] {9}).collect(toImmutableMultiset()));
    }

    ImmutableMultiset<Integer> testImmutableMultisetCopyOfImmutableMultiset() {
      return ImmutableMultiset.copyOf(ImmutableMultiset.of(1, 2));
    }
  }

  static final class ImmutableSetMultimapTemplates {
    ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
      return new ImmutableSetMultimap.Builder<>();
    }

    ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
      return ImmutableSetMultimap.<String, Integer>builder().build();
    }

    ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
      return ImmutableSetMultimap.<String, Integer>builder().put("foo", 1).build();
    }

    ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
      return ImmutableSet.of(
          ImmutableSetMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
          Stream.of(Map.entry("foo", 1))
              .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
      return ImmutableSet.of(
          ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
          ImmutableSetMultimap.<String, Integer>builder()
              .putAll(ImmutableSetMultimap.of("foo", 1))
              .build(),
          ImmutableSetMultimap.<String, Integer>builder()
              .putAll(ImmutableSetMultimap.of("foo", 1).entries())
              .build(),
          ImmutableSetMultimap.of("foo", 1).entries().stream()
              .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
              .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfImmutableSetMultimap() {
      return ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1));
    }
  }

  static final class ImmutableSetTemplates {
    ImmutableSet.Builder<String> testImmutableSetBuilder() {
      return new ImmutableSet.Builder<>();
    }

    ImmutableSet<ImmutableSet<Integer>> testEmptyImmutableSet() {
      return ImmutableSet.of(
          ImmutableSet.<Integer>builder().build(),
          Stream.<Integer>empty().collect(toImmutableSet()));
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

  static final class ImmutableSortedMapBuilder {
    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapBuilder() {
      return new ImmutableSortedMap.Builder<>(Comparator.comparingInt(String::length));
    }

    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
      return ImmutableSortedMap.orderedBy(Comparator.<String>naturalOrder());
    }

    ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
      return ImmutableSortedMap.orderedBy(Comparator.<String>reverseOrder());
    }

    ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
      return ImmutableSortedMap.<String, Integer>naturalOrder().build();
    }

    ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
      return ImmutableSortedMap.<String, Integer>naturalOrder().put("foo", 1).build();
    }

    ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
      return ImmutableSet.of(
          ImmutableSortedMap.<String, Integer>naturalOrder().put(Map.entry("foo", 1)).build(),
          Stream.of(Map.entry("foo", 1))
              .collect(
                  toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
    }

    ImmutableSet<ImmutableSortedMap<String, Integer>> testIterableToImmutableSortedMap() {
      return ImmutableSet.of(
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1), naturalOrder()),
          ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
          ImmutableSortedMap.<String, Integer>naturalOrder()
              .putAll(ImmutableSortedMap.of("foo", 1))
              .build(),
          ImmutableSortedMap.<String, Integer>naturalOrder()
              .putAll(ImmutableSortedMap.of("foo", 1).entrySet())
              .build(),
          ImmutableSortedMap.of("foo", 1).entrySet().stream()
              .collect(
                  toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)),
          Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
              .collect(
                  toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
    }
  }

  static final class ImmutableSortedMultisetTemplates {
    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetBuilder() {
      return new ImmutableSortedMultiset.Builder<>(Comparator.comparingInt(String::length));
    }

    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetNaturalOrderBuilder() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<String>naturalOrder());
    }

    ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetReverseOrderBuilder() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<String>reverseOrder());
    }

    ImmutableMultiset<ImmutableSortedMultiset<Integer>> testEmptyImmutableSortedMultiset() {
      return ImmutableMultiset.of(
          ImmutableSortedMultiset.<Integer>naturalOrder().build(),
          Stream.<Integer>empty().collect(toImmutableSortedMultiset(naturalOrder())));
    }

    ImmutableMultiset<ImmutableSortedMultiset<Integer>> testIterableToImmutableSortedMultiset() {
      return ImmutableMultiset.of(
          ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(1)),
          ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(2).iterator()),
          ImmutableList.of(3).stream().collect(toImmutableSortedMultiset(naturalOrder())),
          Streams.stream(ImmutableList.of(4)::iterator)
              .collect(toImmutableSortedMultiset(naturalOrder())),
          Streams.stream(ImmutableList.of(5).iterator())
              .collect(toImmutableSortedMultiset(naturalOrder())),
          ImmutableSortedMultiset.<Integer>naturalOrder().addAll(ImmutableMultiset.of(6)).build(),
          ImmutableSortedMultiset.<Integer>naturalOrder()
              .addAll(ImmutableMultiset.of(7)::iterator)
              .build(),
          ImmutableSortedMultiset.<Integer>naturalOrder()
              .addAll(ImmutableMultiset.of(8).iterator())
              .build(),
          ImmutableSortedMultiset.<Integer>naturalOrder().add(new Integer[] {9}).build(),
          Stream.of(new Integer[] {10}).collect(toImmutableSortedMultiset(naturalOrder())),
          Arrays.stream(new Integer[] {11}).collect(toImmutableSortedMultiset(naturalOrder())));
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

    ImmutableSet<ImmutableSortedSet<Integer>> testEmptyImmutableSortedSet() {
      return ImmutableSet.of(
          ImmutableSortedSet.<Integer>naturalOrder().build(),
          Stream.<Integer>empty().collect(toImmutableSortedSet(naturalOrder())));
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
      // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
      return ImmutableSet.of(
          ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(1)),
          ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(2).iterator()),
          ImmutableList.of(3).stream().collect(toImmutableSortedSet(naturalOrder())),
          Streams.stream(ImmutableList.of(4)::iterator)
              .collect(toImmutableSortedSet(naturalOrder())),
          Streams.stream(ImmutableList.of(5).iterator())
              .collect(toImmutableSortedSet(naturalOrder())),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(6)).build(),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(7)::iterator).build(),
          ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(8).iterator()).build(),
          ImmutableSortedSet.<Integer>naturalOrder().add(new Integer[] {9}).build(),
          Stream.of(new Integer[] {10}).collect(toImmutableSortedSet(naturalOrder())),
          Arrays.stream(new Integer[] {11}).collect(toImmutableSortedSet(naturalOrder())));
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
    ImmutableSet<Optional<String>> testOptionalOfNullable() {
      return ImmutableSet.of(
          toString() == null ? Optional.empty() : Optional.of(toString()),
          toString() != null ? Optional.of(toString()) : Optional.empty());
    }

    ImmutableSet<Boolean> testOptionalIsEmpty() {
      return ImmutableSet.of(!Optional.empty().isPresent(), !Optional.of("foo").isPresent());
    }

    ImmutableSet<Boolean> testOptionalIsPresent() {
      return ImmutableSet.of(!Optional.empty().isEmpty(), !Optional.of("foo").isEmpty());
    }

    Stream<Object> testOptionalToStream() {
      return Stream.concat(Streams.stream(Optional.empty()), Streams.stream(Optional.of("foo")));
    }

    ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
      return ImmutableSet.of(
          ImmutableSet.of("foo").iterator().hasNext()
              ? Optional.of(ImmutableSet.of("foo").iterator().next())
              : Optional.empty(),
          !ImmutableSet.of("foo").iterator().hasNext()
              ? Optional.empty()
              : Optional.of(ImmutableSet.of("foo").iterator().next()));
    }

    ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
      return ImmutableSet.of(
          "foo".length() > 5 ? Optional.of("foo") : Optional.empty(),
          !"bar".contains("baz") ? Optional.of("bar") : Optional.empty());
    }

    ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
      return ImmutableSet.of(
          "foo".length() > 5 ? Optional.empty() : Optional.of("foo"),
          !"bar".contains("baz") ? Optional.empty() : Optional.of("bar"));
    }

    ImmutableSet<Boolean> testMapOptionalToBoolean() {
      return ImmutableSet.of(
          Optional.of("foo").map(String::isEmpty).orElse(false),
          Optional.of("bar").map(s -> s.isEmpty()).orElse(Boolean.FALSE));
    }

    ImmutableSet<Optional<String>> testMapToNullable() {
      return ImmutableSet.of(
          Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n))),
          Optional.of(2).flatMap(n -> Optional.ofNullable(String.valueOf(n))));
    }

    Stream<Object> testFlatmapOptionalToStream() {
      return Stream.concat(
          Stream.of(Optional.empty()).filter(Optional::isPresent).map(Optional::get),
          Stream.of(Optional.of("foo")).flatMap(Streams::stream));
    }

    Stream<String> testMapToOptionalGet(Map<Integer, Optional<String>> map) {
      return Stream.of(1).map(n -> map.get(n).get());
    }

    Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length()).filter(len -> len > 0));
    }

    Optional<Integer> testMapOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length()).map(len -> len * 0));
    }

    Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
      return Optional.of("foo").flatMap(v -> Optional.of(v.length()).flatMap(Optional::of));
    }
  }

  static final class PrimitiveTemplates {
    ImmutableSet<Boolean> testLessThan() {
      return ImmutableSet.of(
          !((byte) 3 >= (byte) 4),
          !((short) 3 >= (short) 4),
          !(3 >= 4),
          !(3L >= 4L),
          !(3F >= 4F),
          !(3.0 >= 4.0));
    }

    ImmutableSet<Boolean> testLessThanOrEqualTo() {
      return ImmutableSet.of(
          !((byte) 3 > (byte) 4),
          !((short) 3 > (short) 4),
          !(3 > 4),
          !(3L > 4L),
          !(3F > 4F),
          !(3.0 > 4.0));
    }

    ImmutableSet<Boolean> testGreaterThan() {
      return ImmutableSet.of(
          !((byte) 3 <= (byte) 4),
          !((short) 3 <= (short) 4),
          !(3 <= 4),
          !(3L <= 4L),
          !(3F <= 4F),
          !(3.0 <= 4.0));
    }

    ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
      return ImmutableSet.of(
          !((byte) 3 < (byte) 4),
          !((short) 3 < (short) 4),
          !(3 < 4),
          !(3L < 4L),
          !(3F < 4F),
          !(3.0 < 4.0));
    }

    int testLongToIntExact() {
      return Ints.checkedCast(Long.MAX_VALUE);
    }
  }

  static final class StreamTemplates {
    ImmutableSet<Stream<String>> testStreamOfNullable() {
      return ImmutableSet.of(
          Stream.of("a").filter(Objects::nonNull), Optional.ofNullable("b").stream());
    }

    Stream<Integer> testConcatOneStream() {
      return Streams.concat(Stream.of(1));
    }

    Stream<Integer> testConcatTwoStreams() {
      return Streams.concat(Stream.of(1), Stream.of(2));
    }

    Stream<Integer> testFilterOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length()).filter(len -> len > 0));
    }

    Stream<Integer> testMapOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length()).map(len -> len * 0));
    }

    Stream<Integer> testFlatMapOuterStreamAfterFlatMap() {
      return Stream.of("foo").flatMap(v -> Stream.of(v.length()).flatMap(Stream::of));
    }

    ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
      return ImmutableSet.of(
          ImmutableList.copyOf(Stream.of(1).iterator()),
          ImmutableList.copyOf(Stream.of(2).iterator()));
    }

    ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
      return ImmutableSet.of(
          ImmutableSet.copyOf(Stream.of(1).iterator()),
          ImmutableSet.copyOf(Stream.of(2)::iterator),
          Stream.of(3).distinct().collect(toImmutableSet()));
    }

    ImmutableSet<ImmutableSortedSet<Integer>> testStreamToImmutableSortedSet() {
      return ImmutableSet.of(
          ImmutableSortedSet.copyOf(Stream.of(1).iterator()),
          ImmutableSortedSet.copyOf(Stream.of(2)::iterator));
    }

    ImmutableSet<ImmutableMultiset<Integer>> testStreamToImmutableMultiset() {
      return ImmutableSet.of(
          ImmutableMultiset.copyOf(Stream.of(1).iterator()),
          ImmutableMultiset.copyOf(Stream.of(2)::iterator));
    }

    ImmutableSet<ImmutableSortedMultiset<Integer>> testStreamToImmutableSortedMultiset() {
      return ImmutableSet.of(
          ImmutableSortedMultiset.copyOf(Stream.of(1).iterator()),
          ImmutableSortedMultiset.copyOf(Stream.of(2)::iterator));
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
          Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.ofNullable(toString()),
          !Strings.isNullOrEmpty(toString()) ? Optional.of(toString()) : Optional.empty(),
          !Strings.isNullOrEmpty(toString()) ? Optional.ofNullable(toString()) : Optional.empty());
    }

    ImmutableSet<String> testJoinStrings() {
      return ImmutableSet.of(
          Joiner.on("a").join(new String[] {"foo", "bar"}),
          Joiner.on("b").join(new CharSequence[] {"baz", "quux"}),
          Stream.of(new String[] {"foo", "bar"}).collect(joining("c")),
          Arrays.stream(new CharSequence[] {"baz", "quux"}).collect(joining("d")),
          Joiner.on("e").join(ImmutableList.of("foo", "bar")),
          Streams.stream(Iterables.cycle(ImmutableList.of("foo", "bar"))).collect(joining("f")),
          ImmutableList.of("baz", "quux").stream().collect(joining("g")));
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
