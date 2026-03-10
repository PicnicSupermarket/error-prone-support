package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.Comparators.greatest;
import static com.google.common.collect.Comparators.least;
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
    return ImmutableSet.of(Stream.class, identity());
  }

  ImmutableSet<Comparator<String>> testNaturalOrder() {
    return ImmutableSet.of(
        String::compareTo,
        Comparator.comparing(s -> 0),
        Comparator.comparing(identity()),
        Collections.<String>reverseOrder(reverseOrder()),
        Comparator.<String>reverseOrder().reversed());
  }

  ImmutableSet<Comparator<String>> testReverseOrder() {
    return ImmutableSet.of(
        Collections.reverseOrder(),
        Collections.<String>reverseOrder(naturalOrder()),
        Comparator.<String>naturalOrder().reversed());
  }

  ImmutableSet<Comparator<String>> testComparatorIdentity() {
    return ImmutableSet.of(
        Comparator.comparing(s -> "foo", Comparator.comparingInt(String::length)),
        Comparator.comparing(identity(), Comparator.comparingInt(String::length)));
  }

  Comparator<String> testComparingEnum() {
    return Comparator.comparingInt(s -> RoundingMode.valueOf(s).ordinal());
  }

  Comparator<String> testComparatorThenComparing() {
    return Comparator.<String>naturalOrder().thenComparing(Comparator.comparing(String::isEmpty));
  }

  Comparator<String> testComparatorThenComparingReverseOrder() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty).reversed());
  }

  Comparator<String> testComparatorThenComparingWithComparator() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty, reverseOrder()));
  }

  Comparator<String> testComparatorThenComparingComparatorReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(
            Comparator.comparing(String::isEmpty, Comparator.<Boolean>reverseOrder()).reversed());
  }

  Comparator<Integer> testComparatorThenComparingDouble() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingDouble(Integer::doubleValue));
  }

  Comparator<Integer> testComparatorThenComparingInt() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingInt(Integer::intValue));
  }

  Comparator<Integer> testComparatorThenComparingLong() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingLong(Integer::longValue));
  }

  ImmutableSet<Comparator<String>> testComparatorThenComparingNaturalOrder() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().thenComparing(s -> 0),
        Comparator.<String>naturalOrder().thenComparing(identity()));
  }

  ImmutableSet<Integer> testComparableCompareTo() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().compare("foo", "bar"),
        Comparator.<String>reverseOrder().compare("baz", "qux"));
  }

  void testCollectionsSort() {
    Collections.sort(ImmutableList.of("foo", "bar"), naturalOrder());
  }

  ImmutableSet<String> testCollectionsMin() {
    return ImmutableSet.of(
        Collections.min(ImmutableList.of("foo"), naturalOrder()),
        Collections.max(ImmutableList.of("bar"), reverseOrder()));
  }

  String testCollectionsMinArraysAsListOfArray() {
    return Arrays.stream(new String[0]).min(naturalOrder()).orElseThrow();
  }

  String testCollectionsMinWithComparator() {
    return ImmutableSet.of("foo", "bar").stream().min(naturalOrder()).orElseThrow();
  }

  int testCollectionsMinArraysAsList() {
    return Stream.of(1, 2).min(naturalOrder()).orElseThrow();
  }

  ImmutableSet<String> testComparatorsMinOfPair() {
    return ImmutableSet.of(
        "foo".compareTo("bar") <= 0 ? "foo" : "bar",
        "baz".compareTo("qux") > 0 ? "qux" : "baz",
        "quux".compareTo("corge") < 0 ? "quux" : "corge",
        "grault".compareTo("garply") >= 0 ? "garply" : "grault",
        Comparators.min("waldo", "fred", naturalOrder()),
        Comparators.max("plugh", "xyzzy", reverseOrder()),
        Collections.min(Arrays.asList("thud", "foo")),
        Collections.min(ImmutableList.of("bar", "baz")),
        Collections.min(ImmutableSet.of("qux", "quux")));
  }

  ImmutableSet<Object> testComparatorsMinOfPairWithComparator() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length).compare("foo", "bar") <= 0 ? "foo" : "bar",
        Comparator.comparingInt(String::length).compare("baz", "qux") > 0 ? "qux" : "baz",
        Comparator.comparingInt(String::length).compare("quux", "corge") < 0 ? "quux" : "corge",
        Comparator.comparingInt(String::length).compare("grault", "garply") >= 0
            ? "garply"
            : "grault",
        Collections.min(Arrays.asList("waldo", "fred"), (a, b) -> -1),
        Collections.min(ImmutableList.of("plugh", "xyzzy"), (a, b) -> 0),
        Collections.min(ImmutableSet.of("thud", "foo"), (a, b) -> 1));
  }

  ImmutableSet<String> testCollectionsMax() {
    return ImmutableSet.of(
        Collections.max(ImmutableList.of("foo"), naturalOrder()),
        Collections.min(ImmutableList.of("bar"), reverseOrder()));
  }

  String testCollectionsMaxArraysAsListOfArray() {
    return Arrays.stream(new String[0]).max(naturalOrder()).orElseThrow();
  }

  String testCollectionsMaxWithComparator() {
    return ImmutableSet.of("foo", "bar").stream().max(naturalOrder()).orElseThrow();
  }

  int testCollectionsMaxArraysAsList() {
    return Stream.of(1, 2).max(naturalOrder()).orElseThrow();
  }

  ImmutableSet<String> testComparatorsMaxOfPair() {
    return ImmutableSet.of(
        "foo".compareTo("bar") >= 0 ? "foo" : "bar",
        "baz".compareTo("qux") < 0 ? "qux" : "baz",
        "quux".compareTo("corge") > 0 ? "quux" : "corge",
        "grault".compareTo("garply") <= 0 ? "garply" : "grault",
        Comparators.max("waldo", "fred", naturalOrder()),
        Comparators.min("plugh", "xyzzy", reverseOrder()),
        Collections.max(Arrays.asList("thud", "foo")),
        Collections.max(ImmutableList.of("bar", "baz")),
        Collections.max(ImmutableSet.of("qux", "quux")));
  }

  ImmutableSet<Object> testComparatorsMaxOfPairWithComparator() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length).compare("foo", "bar") >= 0 ? "foo" : "bar",
        Comparator.comparingInt(String::length).compare("baz", "qux") < 0 ? "qux" : "baz",
        Comparator.comparingInt(String::length).compare("quux", "corge") > 0 ? "quux" : "corge",
        Comparator.comparingInt(String::length).compare("grault", "garply") <= 0
            ? "garply"
            : "grault",
        Collections.max(Arrays.asList("waldo", "fred"), (a, b) -> -1),
        Collections.max(ImmutableList.of("plugh", "xyzzy"), (a, b) -> 0),
        Collections.max(ImmutableSet.of("thud", "foo"), (a, b) -> 1));
  }

  Collector<String, ?, List<String>> testLeast() {
    return greatest(1, Comparator.comparingInt(String::length).reversed());
  }

  Collector<String, ?, List<String>> testGreatest() {
    return least(1, Comparator.comparingInt(String::length).reversed());
  }

  Collector<String, ?, List<String>> testLeastNaturalOrder() {
    return greatest(1, reverseOrder());
  }

  Collector<String, ?, List<String>> testGreatestNaturalOrder() {
    return least(1, reverseOrder());
  }

  BinaryOperator<String> testComparatorsMin() {
    return BinaryOperator.minBy(naturalOrder());
  }

  BinaryOperator<String> testComparatorsMax() {
    return BinaryOperator.maxBy(naturalOrder());
  }

  Collector<Integer, ?, Optional<Integer>> testMinByNaturalOrder() {
    return maxBy(reverseOrder());
  }

  Collector<Integer, ?, Optional<Integer>> testMaxByNaturalOrder() {
    return minBy(reverseOrder());
  }

  ImmutableSet<Boolean> testEnumCompareToLessThanZero() {
    return ImmutableSet.of(
        RoundingMode.UP.ordinal() < RoundingMode.DOWN.ordinal(),
        RoundingMode.UP.ordinal() >= RoundingMode.DOWN.ordinal());
  }

  ImmutableSet<Boolean> testEnumCompareToLessThanOrEqualToZero() {
    return ImmutableSet.of(
        RoundingMode.UP.ordinal() <= RoundingMode.DOWN.ordinal(),
        RoundingMode.UP.ordinal() > RoundingMode.DOWN.ordinal());
  }
}
