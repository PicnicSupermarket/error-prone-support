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
    return ImmutableSet.of(Arrays.class, Streams.class, collectingAndThen(null, null), toList());
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetBuilder() {
    return new ImmutableSortedSet.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrderBuilder() {
    return ImmutableSortedSet.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrderBuilder() {
    return ImmutableSortedSet.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testEmptyImmutableSortedSet() {
    return ImmutableSet.of(
        ImmutableSortedSet.<Integer>naturalOrder().build(),
        Stream.<Integer>empty().collect(toImmutableSortedSet(naturalOrder())));
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testIterableToImmutableSortedSet() {
    // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
    return ImmutableSet.of(
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(1)),
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(2).iterator()),
        ImmutableList.of(3).stream().collect(toImmutableSortedSet(naturalOrder())),
        Streams.stream(ImmutableList.of(4)::iterator).collect(toImmutableSortedSet(naturalOrder())),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableSortedSet(naturalOrder())),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(6)).build(),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(7)::iterator).build(),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(8).iterator()).build(),
        ImmutableSortedSet.<Integer>naturalOrder().add(new Integer[] {9}).build(),
        Arrays.stream(new Integer[] {10}).collect(toImmutableSortedSet(naturalOrder())));
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testStreamToImmutableSortedSet() {
    return ImmutableSet.of(
        ImmutableSortedSet.copyOf(Stream.of(1).iterator()),
        Stream.of(2).collect(collectingAndThen(toList(), ImmutableSortedSet::copyOf)));
  }
}
