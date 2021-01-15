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
    return ImmutableSortedMultiset.orderedBy(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetNaturalOrderBuilder() {
    return ImmutableSortedMultiset.naturalOrder();
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetReverseOrderBuilder() {
    return ImmutableSortedMultiset.reverseOrder();
  }

  ImmutableMultiset<ImmutableSortedMultiset<Integer>> testEmptyImmutableSortedMultiset() {
    return ImmutableMultiset.of(ImmutableSortedMultiset.of(), ImmutableSortedMultiset.of());
  }

  ImmutableMultiset<ImmutableSortedMultiset<Integer>> testIterableToImmutableSortedMultiset() {
    return ImmutableMultiset.of(
        ImmutableSortedMultiset.copyOf(ImmutableList.of(1)),
        ImmutableSortedMultiset.copyOf(ImmutableList.of(2).iterator()),
        ImmutableSortedMultiset.copyOf(ImmutableList.of(3)),
        ImmutableSortedMultiset.copyOf(ImmutableList.of(4)::iterator),
        ImmutableSortedMultiset.copyOf(ImmutableList.of(5).iterator()),
        ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(6)),
        ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(7)::iterator),
        ImmutableSortedMultiset.copyOf(ImmutableMultiset.of(8).iterator()),
        ImmutableSortedMultiset.copyOf(new Integer[] {9}),
        ImmutableSortedMultiset.copyOf(new Integer[] {10}));
  }

  ImmutableSet<ImmutableSortedMultiset<Integer>> testStreamToImmutableSortedMultiset() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableSortedMultiset(naturalOrder())),
        Stream.of(2).collect(toImmutableSortedMultiset(naturalOrder())));
  }
}
