package tech.picnic.errorprone.refasterrules.output;

import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSortedMultisetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class);
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

  ImmutableSortedMultiset<Integer> testStreamToImmutableSortedMultiset() {
    return Stream.of(1).collect(toImmutableSortedMultiset(naturalOrder()));
  }
}
