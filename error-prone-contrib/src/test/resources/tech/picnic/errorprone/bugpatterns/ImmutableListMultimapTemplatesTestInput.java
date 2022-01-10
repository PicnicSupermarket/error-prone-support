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
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class ImmutableListMultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Streams.class, flatteningToImmutableListMultimap(null, null), identity());
  }

  ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
    return ImmutableSet.of(
        new ImmutableListMultimap.Builder<>(),
        new ImmutableMultimap.Builder<>(),
        ImmutableMultimap.builder());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testEmptyImmutableListMultimap() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().build(), ImmutableMultimap.of());
  }

  ImmutableSet<ImmutableMultimap<String, Integer>> testPairToImmutableListMultimap() {
    return ImmutableSet.of(
        ImmutableListMultimap.<String, Integer>builder().put("foo", 1).build(),
        ImmutableMultimap.of("bar", 2));
  }

  ImmutableList<ImmutableMultimap<String, Integer>> testEntryToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableList<ImmutableMultimap<String, Integer>> testIterableToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.<String, Integer>builder()
            .putAll(ImmutableListMultimap.of("foo", 1))
            .build(),
        ImmutableListMultimap.<String, Integer>builder()
            .putAll(ImmutableListMultimap.of("foo", 1).entries())
            .build(),
        ImmutableListMultimap.of("foo", 1).entries().stream()
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        ImmutableMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  ImmutableListMultimap<Integer, String> testStreamOfMapEntriesToImmutableListMultimap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSet<ImmutableListMultimap<Integer, Integer>> testIndexIterableToImmutableListMultimap() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableListMultimap(n -> n * 2, identity())),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableListMultimap(Integer::valueOf, n -> n)),
        Streams.stream(ImmutableList.of(3).iterator())
            .collect(toImmutableListMultimap(n -> n.intValue(), identity())));
  }

  ImmutableListMultimap<String, Integer> testTransformMultimapValuesToImmutableListMultimap() {
    return ImmutableListMultimap.of("foo", 1L).entries().stream()
        .collect(toImmutableListMultimap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
  }

  ImmutableSet<ImmutableListMultimap<String, Integer>>
      testTransformMultimapValuesToImmutableListMultimap2() {
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

  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfImmutableListMultimap() {
    return ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1));
  }
}
