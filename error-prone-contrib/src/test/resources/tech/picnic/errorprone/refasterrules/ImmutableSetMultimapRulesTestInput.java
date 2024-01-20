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
    return ImmutableSet.of(flatteningToImmutableSetMultimap(null, null), Streams.class);
  }

  ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
    return new ImmutableSetMultimap.Builder<>();
  }

  ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
    return ImmutableSetMultimap.<String, Integer>builder().build();
  }

  ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
    return ImmutableSetMultimap.<String, Integer>builder().put("foo", 1).build();
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("foo", 1))
            .build(),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("foo", 1).entries())
            .build(),
        ImmutableSetMultimap.of("foo", 1).entries().stream()
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSetMultimap<Integer, String> testStreamOfMapEntriesToImmutableSetMultimap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSetMultimap<String, Integer> testTransformMultimapValuesToImmutableSetMultimap() {
    return ImmutableSetMultimap.of("foo", 1L).entries().stream()
        .collect(toImmutableSetMultimap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testTransformMultimapValuesToImmutableSetMultimap2() {
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
}
