package tech.picnic.errorprone.bugpatterns;

import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

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
    return ImmutableSet.of(
        Comparator.comparing(identity()),
        Comparator.comparing(s -> s),
        Comparator.<String>reverseOrder().reversed());
  }

  Comparator<String> testReverseOrder() {
    return Comparator.<String>naturalOrder().reversed();
  }

  ImmutableSet<Comparator<String>> testCustomComparator() {
    return ImmutableSet.of(
        Comparator.comparing(identity(), Comparator.comparingInt(String::length)),
        Comparator.comparing(s -> s, Comparator.comparingInt(String::length)));
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
        Comparator.<String>naturalOrder().thenComparing(s -> s));
  }

  ImmutableSet<String> testMinOfPairNaturalOrder() {
    return ImmutableSet.of(
        Collections.min(Arrays.asList("a", "b")),
        Collections.min(ImmutableList.of("a", "b")),
        Collections.min(ImmutableSet.of("a", "b")));
  }

  ImmutableSet<Object> testMinOfPairCustomOrder() {
    return ImmutableSet.of(
        Collections.min(Arrays.asList(new Object(), new Object()), (a, b) -> -1),
        Collections.min(ImmutableList.of(new Object(), new Object()), (a, b) -> 0),
        Collections.min(ImmutableSet.of(new Object(), new Object()), (a, b) -> 1));
  }

  ImmutableSet<String> testMaxOfPairNaturalOrder() {
    return ImmutableSet.of(
        Collections.max(Arrays.asList("a", "b")),
        Collections.max(ImmutableList.of("a", "b")),
        Collections.max(ImmutableSet.of("a", "b")));
  }

  ImmutableSet<Object> testMaxOfPairCustomOrder() {
    return ImmutableSet.of(
        Collections.max(Arrays.asList(new Object(), new Object()), (a, b) -> -1),
        Collections.max(ImmutableList.of(new Object(), new Object()), (a, b) -> 0),
        Collections.max(ImmutableSet.of(new Object(), new Object()), (a, b) -> 1));
  }
}
