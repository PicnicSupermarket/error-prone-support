package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

final class ImmutableSortedMultisetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class, collectingAndThen(null, null), toList());
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetBuilder() {
    return new ImmutableSortedMultiset.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetNaturalOrderBuilder() {
    return ImmutableSortedMultiset.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetReverseOrderBuilder() {
    return ImmutableSortedMultiset.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableMultiset<ImmutableSortedMultiset<Integer>> testEmptyImmutableSortedMultiset() {
    return ImmutableMultiset.of(
        ImmutableSortedMultiset.<Integer>naturalOrder().build(),
        Stream.<Integer>empty().collect(toImmutableSortedMultiset(naturalOrder())));
  }

  ImmutableMultiset<ImmutableSortedMultiset<Integer>> testIterableToImmutableSortedMultiset() {
    return ImmutableMultiset.of(
        ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(1)),
        ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(2).iterator()),
        ImmutableList.of(3).stream().collect(toImmutableSortedMultiset(naturalOrder())),
        Streams.stream(ImmutableList.of(4)::iterator)
            .collect(toImmutableSortedMultiset(naturalOrder())),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableSortedMultiset(naturalOrder())),
        ImmutableSortedMultiset.<Integer>naturalOrder().addAll(ImmutableMultiset.of(6)).build(),
        ImmutableSortedMultiset.<Integer>naturalOrder()
            .addAll(ImmutableMultiset.of(7)::iterator)
            .build(),
        ImmutableSortedMultiset.<Integer>naturalOrder()
            .addAll(ImmutableMultiset.of(8).iterator())
            .build(),
        ImmutableSortedMultiset.<Integer>naturalOrder().add(new Integer[] {9}).build(),
        Stream.of(new Integer[] {10}).collect(toImmutableSortedMultiset(naturalOrder())),
        Arrays.stream(new Integer[] {11}).collect(toImmutableSortedMultiset(naturalOrder())));
  }

  ImmutableSet<ImmutableSortedMultiset<Integer>> testStreamToImmutableSortedMultiset() {
    return ImmutableSet.of(
        ImmutableSortedMultiset.copyOf(Stream.of(1).iterator()),
        ImmutableSortedMultiset.copyOf(Stream.of(2)::iterator),
        Stream.of(3).collect(collectingAndThen(toList(), ImmutableSortedMultiset::copyOf)));
  }
}
