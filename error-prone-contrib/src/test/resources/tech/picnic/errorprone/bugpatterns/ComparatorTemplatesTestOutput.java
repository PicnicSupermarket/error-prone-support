package tech.picnic.errorprone.bugpatterns;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

final class ComparatorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Collections.class, ImmutableList.class, ImmutableSet.class, identity());
  }

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
