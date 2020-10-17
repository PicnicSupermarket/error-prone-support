package tech.picnic.errorprone.bugpatterns;

import static java.util.function.Function.identity;

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
