package tech.picnic.errorprone.bugpatterns;

import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;

final class ComparatorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of((Runnable) () -> identity());
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
}
