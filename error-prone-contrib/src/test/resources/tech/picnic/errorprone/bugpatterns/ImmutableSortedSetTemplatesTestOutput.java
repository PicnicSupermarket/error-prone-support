package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

final class ImmutableSortedSetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Streams.class,
        (Runnable) () -> collectingAndThen(null, null),
        (Runnable) () -> toList());
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetBuilder() {
    return ImmutableSortedSet.orderedBy(Comparator.comparingInt(String::length));
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrderBuilder() {
    return ImmutableSortedSet.naturalOrder();
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrderBuilder() {
    return ImmutableSortedSet.reverseOrder();
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testEmptyImmutableSortedSet() {
    return ImmutableSet.of(ImmutableSortedSet.of(), ImmutableSortedSet.of());
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
    // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
    return ImmutableSet.of(
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(1)),
        ImmutableSortedSet.copyOf(ImmutableList.of(2).iterator()),
        ImmutableSortedSet.copyOf(ImmutableList.of(3)),
        ImmutableSortedSet.copyOf(ImmutableList.of(4)::iterator),
        ImmutableSortedSet.copyOf(ImmutableList.of(5).iterator()),
        ImmutableSortedSet.copyOf(ImmutableSet.of(6)),
        ImmutableSortedSet.copyOf(ImmutableSet.of(7)::iterator),
        ImmutableSortedSet.copyOf(ImmutableSet.of(8).iterator()),
        ImmutableSortedSet.copyOf(new Integer[] {9}),
        ImmutableSortedSet.copyOf(new Integer[] {10}),
        ImmutableSortedSet.copyOf(new Integer[] {11}));
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testStreamToImmutableSortedSet() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableSortedSet(naturalOrder())),
        Stream.of(2).collect(toImmutableSortedSet(naturalOrder())),
        Stream.of(3).collect(toImmutableSortedSet(naturalOrder())));
  }
}
