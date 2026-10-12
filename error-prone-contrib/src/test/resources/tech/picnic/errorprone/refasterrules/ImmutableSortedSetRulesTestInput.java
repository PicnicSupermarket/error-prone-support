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
    return new ImmutableSortedSet.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetNaturalOrder() {
    return ImmutableSortedSet.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedSet.Builder<String> testImmutableSortedSetReverseOrder() {
    return ImmutableSortedSet.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testImmutableSortedSetOf() {
    return ImmutableSet.of(
        ImmutableSortedSet.<Integer>naturalOrder().build(),
        Stream.<Integer>empty().collect(toImmutableSortedSet(naturalOrder())));
  }

  ImmutableSet<ImmutableSortedSet<Integer>> testImmutableSortedSetCopyOf() {
    // XXX: The first subexpression is not rewritten (`naturalOrder()` isn't dropped). WHY!?
    return ImmutableSet.of(
        ImmutableSortedSet.<Integer>naturalOrder().add(new Integer[] {1}).build(),
        Arrays.stream(new Integer[] {2}).collect(toImmutableSortedSet(naturalOrder())),
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(3).iterator()),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(4).iterator()).build(),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableSortedSet(naturalOrder())),
        ImmutableSortedSet.copyOf(naturalOrder(), ImmutableList.of(6)),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(7)).build(),
        ImmutableSortedSet.<Integer>naturalOrder().addAll(ImmutableSet.of(8)::iterator).build(),
        Streams.stream(ImmutableList.of(9)::iterator).collect(toImmutableSortedSet(naturalOrder())),
        ImmutableList.of(10).stream().collect(toImmutableSortedSet(naturalOrder())));
  }

  ImmutableSortedSet<Integer> testStreamCollectToImmutableSortedSetNaturalOrder() {
    return ImmutableSortedSet.copyOf(Stream.of(1).iterator());
  }
}
