package tech.picnic.errorprone.refasterrules;

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
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableListMultimapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Streams.class, flatteningToImmutableListMultimap(null, null), identity());
  }

  ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
    return ImmutableSet.of(
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapOf() {
    return ImmutableSet.of(ImmutableListMultimap.of(), ImmutableListMultimap.of());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapOf1() {
    return ImmutableSet.of(ImmutableListMultimap.of("foo", 1), ImmutableListMultimap.of("bar", 2));
  }

  ImmutableSet<ImmutableMultimap<String, Integer>>
      testImmutableListMultimapOfMapEntryGetKeyMapEntryGetValue() {
    return ImmutableSet.of(
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableListMultimap.of(Map.entry("bar", 2).getKey(), Map.entry("bar", 2).getValue()));
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapCopyOf() {
    return ImmutableSet.of(
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("bar", 2)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("baz", 3)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("qux", 4)),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("quux", 5))),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("corge", 6))),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("grault", 7))),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("garply", 8).entries()));
  }

  ImmutableListMultimap<Integer, String> testStreamCollectToImmutableListMultimap() {
    return Stream.of(1, 2, 3).collect(toImmutableListMultimap(n -> n, n -> n.toString()));
  }

  ImmutableSet<ImmutableListMultimap<Integer, Integer>> testMultimapsIndex() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1)::iterator)
            .collect(toImmutableListMultimap(Integer::valueOf, v -> 0)),
        Multimaps.index(ImmutableList.of(2)::iterator, Integer::valueOf),
        ImmutableList.of(3).stream().collect(toImmutableListMultimap(n -> n.intValue(), v -> 0)),
        Multimaps.index(ImmutableList.of(4), n -> n.intValue()),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableListMultimap(n -> n * 2, v -> 0)),
        Multimaps.index(ImmutableList.of(6).iterator(), n -> n * 2));
  }

  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfMultimapsTransformValues() {
    return ImmutableListMultimap.copyOf(
        Multimaps.transformValues(ImmutableListMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
  }

  ImmutableSet<ImmutableListMultimap<String, Integer>>
      testImmutableListMultimapCopyOfMultimapsTransformValuesWithFunction() {
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

  ImmutableSet<ImmutableListMultimap.Builder<String, Integer>>
      testImmutableListMultimapBuilderPut() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().put("foo", 1),
        ImmutableListMultimap.<String, Integer>builder().put("bar", 2));
  }
}
