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
    return new ImmutableSet.Builder<>();
  }

  ImmutableSet<ImmutableSet<Integer>> testImmutableSetCopyOf() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().add(new Integer[] {1}).build(),
        Arrays.stream(new Integer[] {2}).collect(toImmutableSet()),
        ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(3).iterator()).build(),
        Streams.stream(ImmutableList.of(4).iterator()).collect(toImmutableSet()),
        ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(5)).build(),
        Streams.stream(ImmutableList.of(6)::iterator).collect(toImmutableSet()),
        ImmutableList.of(7).stream().collect(toImmutableSet()));
  }

  ImmutableSet<ImmutableSet<Integer>> testStreamCollectToImmutableSet() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(Stream.of(1).iterator()),
        Stream.of(2).distinct().collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testSetViewImmutableCopy() {
    return ImmutableSet.copyOf(Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)));
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf0() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableSet()),
        Collections.<Integer>emptySet(),
        Set.<Integer>of());
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf1() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().add(1).build(), Collections.singleton(2), Set.of(3));
  }

  Set<Integer> testImmutableSetOf2() {
    return Set.of(1, 2);
  }

  Set<Integer> testImmutableSetOf3() {
    return Set.of(1, 2, 3);
  }

  Set<Integer> testImmutableSetOf4() {
    return Set.of(1, 2, 3, 4);
  }

  Set<Integer> testImmutableSetOf5() {
    return Set.of(1, 2, 3, 4, 5);
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceImmutableCopy() {
    return ImmutableSet.of(
        ImmutableSet.of(1).stream()
            .filter(not(ImmutableSet.of(2)::contains))
            .collect(toImmutableSet()),
        ImmutableSet.of(3).stream()
            .filter(v -> !ImmutableSet.of(4).contains(v))
            .collect(toImmutableSet()));
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceMapKeySetImmutableCopy() {
    return ImmutableSet.of(
        ImmutableSet.of(1).stream()
            .filter(not(ImmutableMap.of(2, 3)::containsKey))
            .collect(toImmutableSet()),
        ImmutableSet.of(4).stream()
            .filter(v -> !ImmutableMap.of(5, 6).containsKey(v))
            .collect(toImmutableSet()));
  }

  ImmutableSet<ImmutableSet<Integer>> testSetsDifferenceMultimapKeySetImmutableCopy() {
    return ImmutableSet.of(
        ImmutableSet.of(1).stream()
            .filter(not(ImmutableSetMultimap.of(2, 3)::containsKey))
            .collect(toImmutableSet()),
        ImmutableSet.of(4).stream()
            .filter(v -> !ImmutableSetMultimap.of(5, 6).containsKey(v))
            .collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testSetsIntersectionImmutableCopy() {
    return ImmutableSet.of(1).stream()
        .filter(ImmutableSet.of(2)::contains)
        .collect(toImmutableSet());
  }

  ImmutableSet<Integer> testSetsIntersectionMapKeySetImmutableCopy() {
    return ImmutableSet.of(1).stream()
        .filter(ImmutableMap.of(2, 3)::containsKey)
        .collect(toImmutableSet());
  }

  ImmutableSet<Integer> testSetsIntersectionMultimapKeySetImmutableCopy() {
    return ImmutableSet.of(1).stream()
        .filter(ImmutableSetMultimap.of(2, 3)::containsKey)
        .collect(toImmutableSet());
  }

  ImmutableSet<Integer> testSetsUnionImmutableCopy() {
    return Stream.concat(ImmutableSet.of(1).stream(), ImmutableSet.of(2).stream())
        .collect(toImmutableSet());
  }
}
