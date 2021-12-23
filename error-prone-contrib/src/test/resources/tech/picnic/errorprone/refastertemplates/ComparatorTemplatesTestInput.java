package tech.picnic.errorprone.refastertemplates;

import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.CustomComparator;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.MaxOfPairCustomOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.MaxOfPairNaturalOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.MinOfPairCustomOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.MinOfPairNaturalOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.NaturalOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ReverseOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparing;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingCustom;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingCustomReversed;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingDouble;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingInt;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingLong;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingNaturalOrder;
import tech.picnic.errorprone.refastertemplates.ComparatorTemplates.ThenComparingReversed;

@TemplateCollection(ComparatorTemplates.class)
final class ComparatorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Collections.class, ImmutableList.class, ImmutableSet.class, identity());
  }

  @Template(NaturalOrder.class)
  ImmutableSet<Comparator<String>> testNaturalOrder() {
    return ImmutableSet.of(
        Comparator.comparing(identity()),
        Comparator.comparing(s -> s),
        Comparator.<String>reverseOrder().reversed());
  }

  @Template(ReverseOrder.class)
  Comparator<String> testReverseOrder() {
    return Comparator.<String>naturalOrder().reversed();
  }

  @Template(CustomComparator.class)
  ImmutableSet<Comparator<String>> testCustomComparator() {
    return ImmutableSet.of(
        Comparator.comparing(identity(), Comparator.comparingInt(String::length)),
        Comparator.comparing(s -> s, Comparator.comparingInt(String::length)));
  }

  @Template(ThenComparing.class)
  Comparator<String> testThenComparing() {
    return Comparator.<String>naturalOrder().thenComparing(Comparator.comparing(String::isEmpty));
  }

  @Template(ThenComparingReversed.class)
  Comparator<String> testThenComparingReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty).reversed());
  }

  @Template(ThenComparingCustom.class)
  Comparator<String> testThenComparingCustom() {
    return Comparator.<String>naturalOrder()
        .thenComparing(Comparator.comparing(String::isEmpty, reverseOrder()));
  }

  @Template(ThenComparingCustomReversed.class)
  Comparator<String> testThenComparingCustomReversed() {
    return Comparator.<String>naturalOrder()
        .thenComparing(
            Comparator.comparing(String::isEmpty, Comparator.<Boolean>reverseOrder()).reversed());
  }

  @Template(ThenComparingDouble.class)
  Comparator<Integer> testThenComparingDouble() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingDouble(Integer::doubleValue));
  }

  @Template(ThenComparingInt.class)
  Comparator<Integer> testThenComparingInt() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingInt(Integer::intValue));
  }

  @Template(ThenComparingLong.class)
  Comparator<Integer> testThenComparingLong() {
    return Comparator.<Integer>naturalOrder()
        .thenComparing(Comparator.comparingLong(Integer::longValue));
  }

  @Template(ThenComparingNaturalOrder.class)
  ImmutableSet<Comparator<String>> testThenComparingNaturalOrder() {
    return ImmutableSet.of(
        Comparator.<String>naturalOrder().thenComparing(identity()),
        Comparator.<String>naturalOrder().thenComparing(s -> s));
  }

  @Template(MinOfPairNaturalOrder.class)
  ImmutableSet<String> testMinOfPairNaturalOrder() {
    return ImmutableSet.of(
        Collections.min(Arrays.asList("a", "b")),
        Collections.min(ImmutableList.of("a", "b")),
        Collections.min(ImmutableSet.of("a", "b")));
  }

  @Template(MinOfPairCustomOrder.class)
  ImmutableSet<Object> testMinOfPairCustomOrder() {
    return ImmutableSet.of(
        Collections.min(Arrays.asList(new Object(), new Object()), (a, b) -> -1),
        Collections.min(ImmutableList.of(new Object(), new Object()), (a, b) -> 0),
        Collections.min(ImmutableSet.of(new Object(), new Object()), (a, b) -> 1));
  }

  @Template(MaxOfPairNaturalOrder.class)
  ImmutableSet<String> testMaxOfPairNaturalOrder() {
    return ImmutableSet.of(
        Collections.max(Arrays.asList("a", "b")),
        Collections.max(ImmutableList.of("a", "b")),
        Collections.max(ImmutableSet.of("a", "b")));
  }

  @Template(MaxOfPairCustomOrder.class)
  ImmutableSet<Object> testMaxOfPairCustomOrder() {
    return ImmutableSet.of(
        Collections.max(Arrays.asList(new Object(), new Object()), (a, b) -> -1),
        Collections.max(ImmutableList.of(new Object(), new Object()), (a, b) -> 0),
        Collections.max(ImmutableSet.of(new Object(), new Object()), (a, b) -> 1));
  }
}
