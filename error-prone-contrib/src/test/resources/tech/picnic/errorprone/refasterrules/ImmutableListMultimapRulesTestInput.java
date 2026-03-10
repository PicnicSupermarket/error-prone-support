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
        new ImmutableListMultimap.Builder<>(),
        new ImmutableMultimap.Builder<>(),
        ImmutableMultimap.builder());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapOf() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().build(), ImmutableMultimap.of());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapOf1() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().put("foo", 1).build(),
        ImmutableMultimap.of("bar", 2));
  }

  ImmutableSet<ImmutableMultimap<String, Integer>>
      testImmutableListMultimapOfMapEntryGetKeyMapEntryGetValue() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("bar", 2))
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testImmutableListMultimapCopyOf() {
    return ImmutableSet.of(
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.<String, Integer>builder()
            .putAll(ImmutableListMultimap.of("bar", 2))
            .build(),
        ImmutableMultimap.copyOf(ImmutableListMultimap.of("baz", 3)),
        ImmutableMultimap.copyOf(ImmutableListMultimap.of("qux", 4).entries()),
        ImmutableListMultimap.<String, Integer>builder()
            .putAll(Iterables.cycle(Map.entry("quux", 5)))
            .build(),
        Streams.stream(Iterables.cycle(Map.entry("corge", 6)))
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        ImmutableMultimap.copyOf(Iterables.cycle(Map.entry("grault", 7))),
        ImmutableListMultimap.of("garply", 8).entries().stream()
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableListMultimap<Integer, String> testStreamCollectToImmutableListMultimap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSet<ImmutableListMultimap<Integer, Integer>> testMultimapsIndex() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1)::iterator)
            .collect(toImmutableListMultimap(Integer::valueOf, v -> 0)),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableListMultimap(Integer::valueOf, identity())),
        ImmutableList.of(3).stream().collect(toImmutableListMultimap(n -> n.intValue(), v -> 0)),
        ImmutableList.of(4).stream()
            .collect(toImmutableListMultimap(n -> n.intValue(), identity())),
        Streams.stream(ImmutableList.of(5).iterator())
            .collect(toImmutableListMultimap(n -> n * 2, v -> 0)),
        Streams.stream(ImmutableList.of(6).iterator())
            .collect(toImmutableListMultimap(n -> n * 2, identity())));
  }

  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfMultimapsTransformValues() {
    return ImmutableListMultimap.of("foo", 1L).entries().stream()
        .collect(toImmutableListMultimap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
  }

  ImmutableSet<ImmutableListMultimap<String, Integer>>
      testImmutableListMultimapCopyOfMultimapsTransformValuesWithFunction() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1L).asMap().entrySet().stream()
            .collect(
                flatteningToImmutableListMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap((Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L))
            .entrySet()
            .stream()
            .collect(
                flatteningToImmutableListMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(ImmutableListMultimap.of("baz", 3L)).entrySet().stream()
            .collect(
                flatteningToImmutableListMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap(ImmutableSetMultimap.of("qux", 4L)).entrySet().stream()
            .collect(
                flatteningToImmutableListMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(TreeMultimap.<String, Long>create()).entrySet().stream()
            .collect(
                flatteningToImmutableListMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))));
  }

  ImmutableSet<ImmutableListMultimap.Builder<String, Integer>>
      testImmutableListMultimapBuilderPut() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().put(Map.entry("foo", 1)),
        ImmutableListMultimap.<String, Integer>builder().putAll("bar", 2));
  }
}
