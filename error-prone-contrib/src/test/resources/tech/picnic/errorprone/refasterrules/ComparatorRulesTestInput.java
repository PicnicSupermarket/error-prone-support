package tech.picnic.errorprone.refasterrules;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BinaryOperator;
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
        String::compareTo,
        Comparator.comparing(identity()),
        Comparator.comparing(s -> s),
        Comparator.comparing(s -> 0),
        Collections.<String>reverseOrder(reverseOrder()),
        Comparator.<String>reverseOrder().reversed());
  }

  ImmutableSet<Comparator<String>> testReverseOrder() {
    return ImmutableSet.of(
        Collections.reverseOrder(),
        Collections.<String>reverseOrder(naturalOrder()),
        Comparator.<String>naturalOrder().reversed());
  }

  ImmutableSet<Comparator<String>> testCustomComparator() {
    return ImmutableSet.of(
        Comparator.comparing(identity(), Comparator.comparingInt(String::length)),
        Comparator.comparing(s -> s, Comparator.comparingInt(String::length)),
        Comparator.comparing(s -> "foo", Comparator.comparingInt(String::length)));
  }

  Comparator<String> testThenComparing() {
    return Comparator.<String>naturalOrder().thenComparing(Comparator.comparing(String::isEmpty));
  }

  Comparator<String> testThenComparingReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty).reversed());
  }

  Comparator<String> testThenComparingCustom() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty, reverseOrder()));
  }

  Comparator<String> testThenComparingCustomReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(
            Comparator.comparing(String::isEmpty, Comparator.<Boolean>reverseOrder()).reversed());
  }

  Comparator<Integer> testThenComparingDouble() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingDouble(Integer::doubleValue));
  }

  Comparator<Integer> testThenComparingInt() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingInt(Integer::intValue));
  }

  Comparator<Integer> testThenComparingLong() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingLong(Integer::longValue));
  }

  ImmutableSet<Comparator<String>> testThenComparingNaturalOrder() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().thenComparing(identity()),
        Comparator.<String>naturalOrder().thenComparing(s -> s),
        Comparator.<String>naturalOrder().thenComparing(s -> 0));
  }

  ImmutableSet<Integer> testCompareTo() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().compare("foo", "bar"),
        Comparator.<String>reverseOrder().compare("baz", "qux"));
  }

  int testMinOfVarargs() {
    return Stream.of(1, 2).min(naturalOrder()).orElseThrow();
  }

  ImmutableSet<String> testMinOfPairNaturalOrder() {
    return ImmutableSet.of(
        "a".compareTo("b") <= 0 ? "a" : "b",
        "a".compareTo("b") > 0 ? "b" : "a",
        "a".compareTo("b") < 0 ? "a" : "b",
        "a".compareTo("b") >= 0 ? "b" : "a",
        Comparators.min("a", "b", naturalOrder()),
        Comparators.max("a", "b", reverseOrder()),
        Collections.min(Arrays.asList("a", "b")),
        Collections.min(ImmutableList.of("a", "b")),
        Collections.min(ImmutableSet.of("a", "b")));
  }

  ImmutableSet<Object> testMinOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length).compare("a", "b") <= 0 ? "a" : "b",
        Comparator.comparingInt(String::length).compare("a", "b") > 0 ? "b" : "a",
        Comparator.comparingInt(String::length).compare("a", "b") < 0 ? "a" : "b",
        Comparator.comparingInt(String::length).compare("a", "b") >= 0 ? "b" : "a",
        Collections.min(Arrays.asList("a", "b"), (a, b) -> -1),
        Collections.min(ImmutableList.of("a", "b"), (a, b) -> 0),
        Collections.min(ImmutableSet.of("a", "b"), (a, b) -> 1));
  }

  int testMaxOfVarargs() {
    return Stream.of(1, 2).max(naturalOrder()).orElseThrow();
  }

  ImmutableSet<String> testMaxOfPairNaturalOrder() {
    return ImmutableSet.of(
        "a".compareTo("b") >= 0 ? "a" : "b",
        "a".compareTo("b") < 0 ? "b" : "a",
        "a".compareTo("b") > 0 ? "a" : "b",
        "a".compareTo("b") <= 0 ? "b" : "a",
        Comparators.max("a", "b", naturalOrder()),
        Comparators.min("a", "b", reverseOrder()),
        Collections.max(Arrays.asList("a", "b")),
        Collections.max(ImmutableList.of("a", "b")),
        Collections.max(ImmutableSet.of("a", "b")));
  }

  ImmutableSet<Object> testMaxOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length).compare("a", "b") >= 0 ? "a" : "b",
        Comparator.comparingInt(String::length).compare("a", "b") < 0 ? "b" : "a",
        Comparator.comparingInt(String::length).compare("a", "b") > 0 ? "a" : "b",
        Comparator.comparingInt(String::length).compare("a", "b") <= 0 ? "b" : "a",
        Collections.max(Arrays.asList("a", "b"), (a, b) -> -1),
        Collections.max(ImmutableList.of("a", "b"), (a, b) -> 0),
        Collections.max(ImmutableSet.of("a", "b"), (a, b) -> 1));
  }

  BinaryOperator<String> testComparatorsMin() {
    return BinaryOperator.minBy(naturalOrder());
  }

  BinaryOperator<String> testComparatorsMax() {
    return BinaryOperator.maxBy(naturalOrder());
  }
}
