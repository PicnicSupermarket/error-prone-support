package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSetTemplatesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Collections.class, Streams.class);
  }

  ImmutableSet.Builder<String> testImmutableSetBuilder() {
    return new ImmutableSet.Builder<>();
  }

  ImmutableSet<ImmutableSet<Integer>> testIterableToImmutableSet() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableSet()),
        Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableSet()),
        Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableSet()),
        ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(4)).build(),
        ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(5)::iterator).build(),
        ImmutableSet.<Integer>builder().addAll(ImmutableSet.of(6).iterator()).build(),
        ImmutableSet.<Integer>builder().add(new Integer[] {7}).build(),
        Arrays.stream(new Integer[] {8}).collect(toImmutableSet()));
  }

  ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(Stream.of(1).iterator()),
        Stream.of(2).distinct().collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
    return ImmutableSet.copyOf(Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)));
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableSet()),
        Collections.<Integer>emptySet(),
        Set.<Integer>of());
  }

  ImmutableSet<Set<Integer>> testImmutableSetOf1() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().add(1).build(), Collections.singleton(1), Set.of(1));
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
}
