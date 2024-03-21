package tech.picnic.errorprone.refasterrules.output;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Collections.class, Streams.class, not(null));
  }

  ImmutableSet.Builder<String> testImmutableSetBuilder() {
    return ImmutableSet.builder();
  }

  ImmutableSet<ImmutableSet<Integer>> testIterableToImmutableSet() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(ImmutableList.of(1)),
        ImmutableSet.copyOf(ImmutableList.of(2)::iterator),
        ImmutableSet.copyOf(ImmutableList.of(3).iterator()),
        ImmutableSet.copyOf(ImmutableSet.of(4)),
        ImmutableSet.copyOf(ImmutableSet.of(5)::iterator),
        ImmutableSet.copyOf(ImmutableSet.of(6).iterator()),
        ImmutableSet.copyOf(new Integer[] {7}),
        ImmutableSet.copyOf(new Integer[] {8}));
  }

  ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableSet()), Stream.of(2).collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
    return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf() {
    return ImmutableSet.of(
        ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf1() {
    return ImmutableSet.of(ImmutableSet.of(1), ImmutableSet.of(1), ImmutableSet.of(1));
  }

  Set<Integer> testImmutableSetOf2() {
    return ImmutableSet.of(1, 2);
  }

  Set<Integer> testImmutableSetOf3() {
    return ImmutableSet.of(1, 2, 3);
  }

  Set<Integer> testImmutableSetOf4() {
    return ImmutableSet.of(1, 2, 3, 4);
  }

  Set<Integer> testImmutableSetOf5() {
    return ImmutableSet.of(1, 2, 3, 4, 5);
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifference() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy(),
        Sets.difference(ImmutableSet.of(3), ImmutableSet.of(4)).immutableCopy());
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceMap() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableMap.of(2, 3).keySet()).immutableCopy(),
        Sets.difference(ImmutableSet.of(4), ImmutableMap.of(5, 6).keySet()).immutableCopy());
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceMultimap() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableSetMultimap.of(2, 3).keySet()).immutableCopy(),
        Sets.difference(ImmutableSet.of(4), ImmutableSetMultimap.of(5, 6).keySet())
            .immutableCopy());
  }

  ImmutableSet<Integer> testSetsIntersection() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }

  ImmutableSet<Integer> testSetsIntersectionMap() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableMap.of(2, 3).keySet()).immutableCopy();
  }

  ImmutableSet<Integer> testSetsIntersectionMultimap() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableSetMultimap.of(2, 3).keySet())
        .immutableCopy();
  }

  ImmutableSet<Integer> testSetsUnion() {
    return Sets.union(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }
}
