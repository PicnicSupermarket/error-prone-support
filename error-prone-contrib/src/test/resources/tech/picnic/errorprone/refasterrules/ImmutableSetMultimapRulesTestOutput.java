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
    return ImmutableSetMultimap.builder();
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapOf0() {
    return ImmutableSetMultimap.of();
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapOf2() {
    return ImmutableSetMultimap.of("foo", 1);
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapOfEntryGetKeyEntryGetValue() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSetMultimap.of(Map.entry("bar", 2).getKey(), Map.entry("bar", 2).getValue()));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>> testImmutableSetMultimapCopyOf() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("bar", 2)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("baz", 3).entries()),
        ImmutableSetMultimap.copyOf(Iterables.cycle(Map.entry("quux", 5))),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("qux", 4).entries()));
  }

  ImmutableSetMultimap<Integer, String> testStreamCollectToImmutableSetMultimap() {
    return Stream.of(1, 2, 3).collect(toImmutableSetMultimap(n -> n, n -> n.toString()));
  }

  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfMultimapsTransformValues() {
    return ImmutableSetMultimap.copyOf(
        Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), e -> Math.toIntExact(e)));
  }

  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapCopyOfMultimapsTransformValuesWithFunction() {
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

  ImmutableSet<ImmutableSetMultimap.Builder<String, Integer>> testBuilderPut() {
    return ImmutableSet.of(
        ImmutableSetMultimap.<String, Integer>builder().put("foo", 1),
        ImmutableSetMultimap.<String, Integer>builder().put("bar", 2));
  }
}
