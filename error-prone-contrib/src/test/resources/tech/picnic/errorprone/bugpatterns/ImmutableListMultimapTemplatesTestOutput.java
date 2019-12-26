package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;
import java.util.Map;
import java.util.stream.Stream;

final class ImmutableListMultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Streams.class,
        (Runnable) () -> flatteningToImmutableListMultimap(null, null),
        (Runnable) () -> identity());
  }

  ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
    return ImmutableSet.of(
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testEmptyImmutableListMultimap() {
    return ImmutableSet.of(
        ImmutableListMultimap.of(), ImmutableListMultimap.of(), ImmutableListMultimap.of());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testPairToImmutableListMultimap() {
    return ImmutableSet.of(
        ImmutableListMultimap.of("foo", 1),
        ImmutableListMultimap.of("bar", 2),
        ImmutableListMultimap.of("baz", 3));
  }

  ImmutableList<ImmutableMultimap<String, Integer>> testEntryToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  ImmutableList<ImmutableMultimap<String, Integer>> testIterableToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  ImmutableListMultimap<Integer, String> testStreamOfMapEntriesToImmutableListMultimap() {
    // XXX: If `Integer.valueOf(n)` is replaced with `n` this doesn't work, even though it should.
    // Looks like a @Placeholder limitation. Try to track down and fix.
    return Stream.of(1, 2, 3)
        .collect(toImmutableListMultimap(n -> Integer.valueOf(n), n -> n.toString()));
  }

  ImmutableSet<ImmutableListMultimap<Integer, Integer>> testIndexIterableToImmutableListMultimap() {
    return ImmutableSet.of(
        Multimaps.index(ImmutableList.of(1), n -> n * 2),
        Multimaps.index(ImmutableList.of(2)::iterator, Integer::valueOf),
        Multimaps.index(ImmutableList.of(3).iterator(), n -> n.intValue()));
  }

  ImmutableListMultimap<String, Integer> testTransformMultimapValuesToImmutableListMultimap() {
    return ImmutableListMultimap.copyOf(
        Multimaps.transformValues(ImmutableListMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
  }

  ImmutableSet<ImmutableListMultimap<String, Integer>>
      testTransformMultimapValuesToImmutableListMultimap2() {
    return ImmutableSet.of(
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(
                (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                n -> Math.toIntExact(n))),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
  }

  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfImmutableListMultimap() {
    return ImmutableListMultimap.of("foo", 1);
  }
}
