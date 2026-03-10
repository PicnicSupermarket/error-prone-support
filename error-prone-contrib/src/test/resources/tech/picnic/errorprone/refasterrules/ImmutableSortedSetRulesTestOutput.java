package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSortedSetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class);
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetOrderedBy() {
    return ImmutableSortedSet.orderedBy(Comparator.comparingInt(String::length));
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrder() {
    return ImmutableSortedSet.naturalOrder();
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrder() {
    return ImmutableSortedSet.reverseOrder();
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testImmutableSortedSetOf() {
    return ImmutableSet.of(ImmutableSortedSet.of(), ImmutableSortedSet.of());
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testImmutableSortedSetCopyOf() {
    // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
    return ImmutableSet.of(
        ImmutableSortedSet.copyOf(new Integer[] {1}),
        ImmutableSortedSet.copyOf(new Integer[] {2}),
        ImmutableSortedSet.copyOf(ImmutableList.of(3).iterator()),
        ImmutableSortedSet.copyOf(ImmutableSet.of(4).iterator()),
        ImmutableSortedSet.copyOf(ImmutableList.of(5).iterator()),
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(6)),
        ImmutableSortedSet.copyOf(ImmutableSet.of(7)),
        ImmutableSortedSet.copyOf(ImmutableSet.of(8)::iterator),
        ImmutableSortedSet.copyOf(ImmutableList.of(9)::iterator),
        ImmutableSortedSet.copyOf(ImmutableList.of(10)));
  }

  ImmutableSortedSet<Integer> testStreamCollectToImmutableSortedSet() {
    return Stream.of(1).collect(toImmutableSortedSet(naturalOrder()));
  }
}
