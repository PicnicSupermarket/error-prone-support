package tech.picnic.errorprone.refasterrules;

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

  ImmutableSet<ImmutableSet<Integer>> testImmutableSetCopyOf() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(new Integer[] {1}),
        ImmutableSet.copyOf(new Integer[] {2}),
        ImmutableSet.copyOf(ImmutableSet.of(3).iterator()),
        ImmutableSet.copyOf(ImmutableList.of(4).iterator()),
        ImmutableSet.copyOf(ImmutableSet.of(5)),
        ImmutableSet.copyOf(ImmutableList.of(6)::iterator),
        ImmutableSet.copyOf(ImmutableList.of(7)));
  }

  ImmutableSet<ImmutableSet<Integer>> testStreamCollectToImmutableSet() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableSet()), Stream.of(2).collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testSetViewImmutableCopy() {
    return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf() {
    return ImmutableSet.of(
        ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf1() {
    return ImmutableSet.of(ImmutableSet.of(1), ImmutableSet.of(2), ImmutableSet.of(3));
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

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceImmutableCopy() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy(),
        Sets.difference(ImmutableSet.of(3), ImmutableSet.of(4)).immutableCopy());
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceKeySetImmutableCopy() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableMap.of(2, 3).keySet()).immutableCopy(),
        Sets.difference(ImmutableSet.of(4), ImmutableMap.of(5, 6).keySet()).immutableCopy());
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceMultimapKeySetImmutableCopy() {
    return ImmutableSet.of(
        Sets.difference(ImmutableSet.of(1), ImmutableSetMultimap.of(2, 3).keySet()).immutableCopy(),
        Sets.difference(ImmutableSet.of(4), ImmutableSetMultimap.of(5, 6).keySet())
            .immutableCopy());
  }

  ImmutableSet<Integer> testSetsIntersectionImmutableCopy() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }

  ImmutableSet<Integer> testSetsIntersectionMapKeySetImmutableCopy() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableMap.of(2, 3).keySet()).immutableCopy();
  }

  ImmutableSet<Integer> testSetsIntersectionMultimapKeySetImmutableCopy() {
    return Sets.intersection(ImmutableSet.of(1), ImmutableSetMultimap.of(2, 3).keySet())
        .immutableCopy();
  }

  ImmutableSet<Integer> testSetsUnionImmutableCopy() {
    return Sets.union(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }
}
