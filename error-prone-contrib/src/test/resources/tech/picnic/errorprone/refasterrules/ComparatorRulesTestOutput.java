package tech.picnic.errorprone.refasterrules;

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
        identity(),
        ImmutableList.class,
        ImmutableSet.class,
        Stream.class);
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

  Comparator<String> testThenComparing() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty);
  }

  Comparator<String> testThenComparingReversed() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty, reverseOrder());
  }

  Comparator<String> testThenComparingCustom() {
    return Comparator.<String>naturalOrder().thenComparing(String::isEmpty, reverseOrder());
  }

  Comparator<String> testThenComparingCustomReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(String::isEmpty, Comparator.<Boolean>reverseOrder().reversed());
  }

  Comparator<Integer> testThenComparingDouble() {
    return Comparator.<Integer>naturalOrder().thenComparingDouble(Integer::doubleValue);
  }

  Comparator<Integer> testThenComparingInt() {
    return Comparator.<Integer>naturalOrder().thenComparingInt(Integer::intValue);
  }

  Comparator<Integer> testThenComparingLong() {
    return Comparator.<Integer>naturalOrder().thenComparingLong(Integer::longValue);
  }

  ImmutableSet<Comparator<String>> testThenComparingNaturalOrder() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().thenComparing(naturalOrder()),
        Comparator.<String>naturalOrder().thenComparing(naturalOrder()),
        Comparator.<String>naturalOrder().thenComparing(s -> 0));
  }

  ImmutableSet<Integer> testCompareTo() {
    return ImmutableSet.of("foo".compareTo("bar"), "qux".compareTo("baz"));
  }

  int testMinOfVarargs() {
    return Collections.min(Arrays.asList(1, 2), naturalOrder());
  }

  ImmutableSet<String> testMinOfPairNaturalOrder() {
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

  ImmutableSet<Object> testMinOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparators.min("a", "b", Comparator.comparingInt(String::length)),
        Comparators.min("a", "b", Comparator.comparingInt(String::length)),
        Comparators.min("b", "a", Comparator.comparingInt(String::length)),
        Comparators.min("b", "a", Comparator.comparingInt(String::length)),
        Comparators.min("a", "b", (a, b) -> -1),
        Comparators.min("a", "b", (a, b) -> 0),
        Comparators.min("a", "b", (a, b) -> 1));
  }

  int testMaxOfVarargs() {
    return Collections.max(Arrays.asList(1, 2), naturalOrder());
  }

  ImmutableSet<String> testMaxOfPairNaturalOrder() {
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

  ImmutableSet<Object> testMaxOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparators.max("a", "b", Comparator.comparingInt(String::length)),
        Comparators.max("a", "b", Comparator.comparingInt(String::length)),
        Comparators.max("b", "a", Comparator.comparingInt(String::length)),
        Comparators.max("b", "a", Comparator.comparingInt(String::length)),
        Comparators.max("a", "b", (a, b) -> -1),
        Comparators.max("a", "b", (a, b) -> 0),
        Comparators.max("a", "b", (a, b) -> 1));
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

  ImmutableSet<Boolean> testIsLessThan() {
    return ImmutableSet.of(
        RoundingMode.UP.compareTo(RoundingMode.DOWN) < 0,
        RoundingMode.UP.compareTo(RoundingMode.DOWN) >= 0);
  }

  ImmutableSet<Boolean> testIsLessThanOrEqualTo() {
    return ImmutableSet.of(
        RoundingMode.UP.compareTo(RoundingMode.DOWN) <= 0,
        RoundingMode.UP.compareTo(RoundingMode.DOWN) > 0);
  }
}
