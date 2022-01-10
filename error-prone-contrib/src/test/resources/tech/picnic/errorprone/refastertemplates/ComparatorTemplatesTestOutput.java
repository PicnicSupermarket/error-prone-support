package tech.picnic.errorprone.refastertemplates;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class ComparatorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Collections.class, ImmutableList.class, ImmutableSet.class, identity());
  }

  ImmutableSet<Comparator<String>> testNaturalOrder() {
    return ImmutableSet.of(naturalOrder(), naturalOrder(), naturalOrder());
  }

  Comparator<String> testReverseOrder() {
    return reverseOrder();
  }

  ImmutableSet<Comparator<String>> testCustomComparator() {
    return ImmutableSet.of(
        Comparator.comparingInt(String::length), Comparator.comparingInt(String::length));
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
        Comparator.<String>naturalOrder().thenComparing(naturalOrder()));
  }

  ImmutableSet<String> testMinOfPairNaturalOrder() {
    return ImmutableSet.of(
        Comparators.min("a", "b"), Comparators.min("a", "b"), Comparators.min("a", "b"));
  }

  ImmutableSet<Object> testMinOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparators.min(new Object(), new Object(), (a, b) -> -1),
        Comparators.min(new Object(), new Object(), (a, b) -> 0),
        Comparators.min(new Object(), new Object(), (a, b) -> 1));
  }

  ImmutableSet<String> testMaxOfPairNaturalOrder() {
    return ImmutableSet.of(
        Comparators.max("a", "b"), Comparators.max("a", "b"), Comparators.max("a", "b"));
  }

  ImmutableSet<Object> testMaxOfPairCustomOrder() {
    return ImmutableSet.of(
        Comparators.max(new Object(), new Object(), (a, b) -> -1),
        Comparators.max(new Object(), new Object(), (a, b) -> 0),
        Comparators.max(new Object(), new Object(), (a, b) -> 1));
  }
}
