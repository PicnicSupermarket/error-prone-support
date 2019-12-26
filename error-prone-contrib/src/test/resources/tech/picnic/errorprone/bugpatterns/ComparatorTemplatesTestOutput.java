package tech.picnic.errorprone.bugpatterns;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;

final class ComparatorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of((Runnable) () -> identity());
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
}
