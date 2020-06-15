package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;
import java.util.Map;
import java.util.stream.Stream;

final class ImmutableSetMultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class, flatteningToImmutableSetMultimap(null, null));
  }

  ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
    return ImmutableSetMultimap.builder();
  }

  ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
    return ImmutableSetMultimap.of();
  }

  ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
    return ImmutableSetMultimap.of("foo", 1);
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  ImmutableSetMultimap<Integer, String> testStreamOfMapEntriesToImmutableSetMultimap() {
    return Stream.of(1, 2, 3).collect(toImmutableSetMultimap(n -> n, n -> n.toString()));
  }

  ImmutableSetMultimap<String, Integer> testTransformMultimapValuesToImmutableSetMultimap() {
    return ImmutableSetMultimap.copyOf(
        Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testTransformMultimapValuesToImmutableSetMultimap2() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(
                (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                n -> Math.toIntExact(n))),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfImmutableSetMultimap() {
    return ImmutableSetMultimap.of("foo", 1);
  }
}
