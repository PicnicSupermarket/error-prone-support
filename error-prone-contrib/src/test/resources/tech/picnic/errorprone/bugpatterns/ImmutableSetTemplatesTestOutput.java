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
    return ImmutableSet.builder();
  }

  ImmutableSet<ImmutableSet<Integer>> testEmptyImmutableSet() {
    return ImmutableSet.of(ImmutableSet.of(), ImmutableSet.of());
  }

  Set<String> testSingletonImmutableSet() {
    return ImmutableSet.of("foo");
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
        Stream.of(1).collect(toImmutableSet()),
        Stream.of(2).collect(toImmutableSet()),
        Stream.of(3).collect(toImmutableSet()),
        Stream.of(4).collect(toImmutableSet()));
  }

  ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
    return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }

  Set<?> testImmutableSetOf() {
    return ImmutableSet.of();
  }

  Set<String> testImmutableSetOf1() {
    return ImmutableSet.of("1");
  }

  Set<String> testImmutableSetOf2() {
    return ImmutableSet.of("1", "2");
  }

  Set<String> testImmutableSetOf3() {
    return ImmutableSet.of("1", "2", "3");
  }

  Set<String> testImmutableSetOf4() {
    return ImmutableSet.of("1", "2", "3", "4");
  }

  Set<String> testImmutableSetOf5() {
    return ImmutableSet.of("1", "2", "3", "4", "5");
  }
}
