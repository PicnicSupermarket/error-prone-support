package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.Comparators.greatest;
import static com.google.common.collect.Comparators.least;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ComparatorRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collections.class,
        ImmutableList.class,
        ImmutableSet.class,
        Stream.class,
        identity());
  }

  ImmutableSet<Comparator<String>> testNaturalOrder() {
    return ImmutableSet.of(
        naturalOrder(),
        naturalOrder(),
        naturalOrder(),
        Comparator.comparing(s -> 0),
        naturalOrder(),
        naturalOrder());
  }

  ImmutableSet<Comparator<String>> testReverseOrder() {
    return ImmutableSet.of(
        Comparator.reverseOrder(), Comparator.reverseOrder(), Comparator.reverseOrder());
  }

  ImmutableSet<Comparator<String>> testCustomComparator() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length),
        Comparator.comparingInt(String::length),
        Comparator.comparing(s -> "foo", Comparator.comparingInt(String::length)));
  }

  Comparator<String> testComparingEnum() {
    return comparing(s -> RoundingMode.valueOf(s));
  }

  Comparator<String> testComparatorThenComparing() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty);
  }

  Comparator<String> testComparatorThenComparingReverseOrder() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty, reverseOrder());
  }

  Comparator<String> testComparatorThenComparingWithComparator() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty, reverseOrder());
  }

  Comparator<String> testComparatorThenComparingComparatorReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(String::isEmpty, Comparator.<Boolean>reverseOrder().reversed());
  }

  Comparator<Integer> testComparatorThenComparingDouble() {
    return Comparator.<Integer>naturalOrder().thenComparingDouble(Integer::doubleValue);
  }

  Comparator<Integer> testComparatorThenComparingInt() {
    return Comparator.<Integer>naturalOrder().thenComparingInt(Integer::intValue);
  }

  Comparator<Integer> testComparatorThenComparingLong() {
    return Comparator.<Integer>naturalOrder().thenComparingLong(Integer::longValue);
  }

  ImmutableSet<Comparator<String>> testComparatorThenComparingNaturalOrder() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().thenComparing(naturalOrder()),
        Comparator.<String>naturalOrder().thenComparing(naturalOrder()),
        Comparator.<String>naturalOrder().thenComparing(s -> 0));
  }

  ImmutableSet<Integer> testComparableCompareTo() {
    return ImmutableSet.of("foo".compareTo("bar"), "qux".compareTo("baz"));
  }

  void testCollectionsSort() {
    Collections.sort(ImmutableList.of("foo", "bar"));
  }

  ImmutableSet<String> testCollectionsMin() {
    return ImmutableSet.of(
        Collections.min(ImmutableList.of("foo")), Collections.min(ImmutableList.of("bar")));
  }

  String testCollectionsMinArraysAsListOfArray() {
    return Collections.min(Arrays.asList(new String[0]), naturalOrder());
  }

  String testCollectionsMinWithComparator() {
    return Collections.min(ImmutableSet.of("foo", "bar"), naturalOrder());
  }

  int testCollectionsMinArraysAsList() {
    return Collections.min(Arrays.asList(1, 2), naturalOrder());
  }

  ImmutableSet<String> testComparatorsMinOfPair() {
    return ImmutableSet.of(
        Comparators.min("a", "b"),
        Comparators.min("a", "b"),
        Comparators.min("b", "a"),
        Comparators.min("b", "a"),
        Comparators.min("a", "b"),
        Comparators.min("a", "b"),
        Comparators.min("a", "b"),
        Comparators.min("a", "b"),
        Comparators.min("a", "b"));
  }

  ImmutableSet<Object> testComparatorsMinOfPairWithComparator() {
    return ImmutableSet.of(
        Comparators.min("a", "b", Comparator.comparingInt(String::length)),
        Comparators.min("a", "b", Comparator.comparingInt(String::length)),
        Comparators.min("b", "a", Comparator.comparingInt(String::length)),
        Comparators.min("b", "a", Comparator.comparingInt(String::length)),
        Comparators.min("a", "b", (a, b) -> -1),
        Comparators.min("a", "b", (a, b) -> 0),
        Comparators.min("a", "b", (a, b) -> 1));
  }

  ImmutableSet<String> testCollectionsMax() {
    return ImmutableSet.of(
        Collections.max(ImmutableList.of("foo")), Collections.max(ImmutableList.of("bar")));
  }

  String testCollectionsMaxArraysAsListOfArray() {
    return Collections.max(Arrays.asList(new String[0]), naturalOrder());
  }

  String testCollectionsMaxWithComparator() {
    return Collections.max(ImmutableSet.of("foo", "bar"), naturalOrder());
  }

  int testCollectionsMaxArraysAsList() {
    return Collections.max(Arrays.asList(1, 2), naturalOrder());
  }

  ImmutableSet<String> testComparatorsMaxOfPair() {
    return ImmutableSet.of(
        Comparators.max("a", "b"),
        Comparators.max("a", "b"),
        Comparators.max("b", "a"),
        Comparators.max("b", "a"),
        Comparators.max("a", "b"),
        Comparators.max("a", "b"),
        Comparators.max("a", "b"),
        Comparators.max("a", "b"),
        Comparators.max("a", "b"));
  }

  ImmutableSet<Object> testComparatorsMaxOfPairWithComparator() {
    return ImmutableSet.of(
        Comparators.max("a", "b", Comparator.comparingInt(String::length)),
        Comparators.max("a", "b", Comparator.comparingInt(String::length)),
        Comparators.max("b", "a", Comparator.comparingInt(String::length)),
        Comparators.max("b", "a", Comparator.comparingInt(String::length)),
        Comparators.max("a", "b", (a, b) -> -1),
        Comparators.max("a", "b", (a, b) -> 0),
        Comparators.max("a", "b", (a, b) -> 1));
  }

  Collector<String, ?, List<String>> testLeast() {
    return least(1, Comparator.comparingInt(String::length));
  }

  Collector<String, ?, List<String>> testGreatest() {
    return greatest(1, Comparator.comparingInt(String::length));
  }

  Collector<String, ?, List<String>> testLeastNaturalOrder() {
    return least(1, naturalOrder());
  }

  Collector<String, ?, List<String>> testGreatestNaturalOrder() {
    return greatest(1, naturalOrder());
  }

  BinaryOperator<String> testComparatorsMin() {
    return Comparators::min;
  }

  BinaryOperator<String> testComparatorsMax() {
    return Comparators::max;
  }

  Collector<Integer, ?, Optional<Integer>> testMinByNaturalOrder() {
    return minBy(naturalOrder());
  }

  Collector<Integer, ?, Optional<Integer>> testMaxByNaturalOrder() {
    return maxBy(naturalOrder());
  }

  ImmutableSet<Boolean> testEnumCompareToLessThan() {
    return ImmutableSet.of(
        RoundingMode.UP.compareTo(RoundingMode.DOWN) < 0,
        RoundingMode.UP.compareTo(RoundingMode.DOWN) >= 0);
  }

  ImmutableSet<Boolean> testEnumCompareToLessThanOrEqualTo() {
    return ImmutableSet.of(
        RoundingMode.UP.compareTo(RoundingMode.DOWN) <= 0,
        RoundingMode.UP.compareTo(RoundingMode.DOWN) > 0);
  }
}
