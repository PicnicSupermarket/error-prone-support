package tech.picnic.errorprone.refasterrules;

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
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSetMultimapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class, flatteningToImmutableSetMultimap(null, null));
  }

  ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
    return new ImmutableSetMultimap.Builder<>();
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapOf0() {
    return ImmutableSetMultimap.<String, Integer>builder().build();
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapOf2() {
    return ImmutableSetMultimap.<String, Integer>builder().put("foo", 1).build();
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapOfEntryGetKeyEntryGetValue() {
    return ImmutableSet.of(
        ImmutableSetMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("bar", 2))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testImmutableSetMultimapCopyOf() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("bar", 2))
            .build(),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("baz", 3).entries())
            .build(),
        Streams.stream(Iterables.cycle(Map.entry("quux", 5)))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        ImmutableSetMultimap.of("qux", 4).entries().stream()
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSetMultimap<Integer, String> testStreamCollectToImmutableSetMultimap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfMultimapsTransformValues() {
    return ImmutableSetMultimap.of("foo", 1L).entries().stream()
        .collect(toImmutableSetMultimap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapCopyOfMultimapsTransformValuesWithFunction() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1L).asMap().entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap((Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L))
            .entrySet()
            .stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(ImmutableListMultimap.of("baz", 3L)).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap(ImmutableSetMultimap.of("qux", 4L)).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(TreeMultimap.<String, Long>create()).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))));
  }

  ImmutableSet<ImmutableSetMultimap.Builder<String, Integer>> testBuilderPut() {
    return ImmutableSet.of(
        ImmutableSetMultimap.<String, Integer>builder().put(Map.entry("foo", 1)),
        ImmutableSetMultimap.<String, Integer>builder().putAll("bar", 2));
  }
}
