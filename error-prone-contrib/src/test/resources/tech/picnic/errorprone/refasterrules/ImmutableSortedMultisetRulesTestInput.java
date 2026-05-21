package tech.picnic.errorprone.refasterrules;

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
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class);
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetOrderedBy() {
    return new ImmutableSortedMultiset.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetNaturalOrder() {
    return ImmutableSortedMultiset.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedMultiset.Builder<String> testImmutableSortedMultisetReverseOrder() {
    return ImmutableSortedMultiset.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableSet<ImmutableSortedMultiset<Integer>> testImmutableSortedMultisetOf() {
    return ImmutableSet.of(
        ImmutableSortedMultiset.<Integer>naturalOrder().build(),
        Stream.<Integer>empty().collect(toImmutableSortedMultiset(naturalOrder())));
  }

  ImmutableSet<ImmutableSortedMultiset<Integer>> testImmutableSortedMultisetCopyOf() {
    return ImmutableSet.of(
        ImmutableSortedMultiset.<Integer>naturalOrder().add(new Integer[] {1}).build(),
        Arrays.stream(new Integer[] {2}).collect(toImmutableSortedMultiset(naturalOrder())),
        ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(3).iterator()),
        ImmutableSortedMultiset.<Integer>naturalOrder()
            .addAll(ImmutableMultiset.of(4).iterator())
            .build(),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableSortedMultiset(naturalOrder())),
        ImmutableSortedMultiset.copyOf(naturalOrder(), ImmutableList.of(6)),
        ImmutableSortedMultiset.<Integer>naturalOrder().addAll(ImmutableMultiset.of(7)).build(),
        ImmutableSortedMultiset.<Integer>naturalOrder()
            .addAll(ImmutableMultiset.of(8)::iterator)
            .build(),
        Streams.stream(ImmutableList.of(9)::iterator)
            .collect(toImmutableSortedMultiset(naturalOrder())),
        ImmutableList.of(10).stream().collect(toImmutableSortedMultiset(naturalOrder())));
  }

  ImmutableSortedMultiset<Integer> testStreamCollectToImmutableSortedMultisetNaturalOrder() {
    return ImmutableSortedMultiset.copyOf(Stream.of(1).iterator());
  }
}
