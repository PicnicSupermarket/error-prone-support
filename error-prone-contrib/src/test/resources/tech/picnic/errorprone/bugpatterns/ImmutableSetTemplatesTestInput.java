package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

final class ImmutableSetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collections.class,
        Streams.class,
        collectingAndThen(null, null),
        toList(),
        toSet());
  }

  ImmutableSet.Builder<String> testImmutableSetBuilder() {
    return new ImmutableSet.Builder<>();
  }

  ImmutableSet<ImmutableSet<Integer>> testEmptyImmutableSet() {
    return ImmutableSet.of(
        ImmutableSet.<Integer>builder().build(), Stream.<Integer>empty().collect(toImmutableSet()));
  }

  Set<String> testSingletonImmutableSet() {
    return Collections.singleton("foo");
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
        Stream.of(2).distinct().collect(toImmutableSet()),
        Stream.of(3).collect(collectingAndThen(toList(), ImmutableSet::copyOf)),
        Stream.of(4).collect(collectingAndThen(toSet(), ImmutableSet::copyOf)));
  }

  ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
    return ImmutableSet.copyOf(Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)));
  }
}
