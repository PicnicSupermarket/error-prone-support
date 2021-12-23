package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.EmptyImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.EntryIterableToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.EntryToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.ImmutableMapBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.ImmutableMapCopyOfImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.IndexIterableToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.IterableToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.PairToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.StreamOfMapEntriesToImmutableMap;
import tech.picnic.errorprone.refastertemplates.ImmutableMapTemplates.TransformMapValuesToImmutableMap;

@TemplateCollection(ImmutableMapTemplates.class)
final class ImmutableMapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class, Streams.class, identity());
  }

  @Template(ImmutableMapBuilder.class)
  ImmutableMap.Builder<String, Integer> testImmutableMapBuilder() {
    return new ImmutableMap.Builder<>();
  }

  @Template(EmptyImmutableMap.class)
  ImmutableMap<String, Integer> testEmptyImmutableMap() {
    return ImmutableMap.<String, Integer>builder().build();
  }

  @Template(PairToImmutableMap.class)
  ImmutableSet<Map<String, Integer>> testPairToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put("foo", 1).build(),
        Collections.singletonMap("bar", 2));
  }

  @Template(EntryToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Template(IterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableMap(identity(), n -> n * 2)),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableMap(n -> n, Integer::valueOf)),
        Streams.stream(ImmutableList.of(3).iterator())
            .collect(toImmutableMap(identity(), n -> n.intValue())),
        ImmutableMap.copyOf(Maps.asMap(ImmutableSet.of(4), Integer::valueOf)));
  }

  @Template(EntryIterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testEntryIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.<String, Integer>builder().putAll(ImmutableMap.of("foo", 1)).build(),
        ImmutableMap.<String, Integer>builder()
            .putAll(ImmutableMap.of("foo", 1).entrySet())
            .build(),
        ImmutableMap.of("foo", 1).entrySet().stream()
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Template(StreamOfMapEntriesToImmutableMap.class)
  ImmutableMap<Integer, String> testStreamOfMapEntriesToImmutableMap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Template(IndexIterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<Integer, Integer>> testIndexIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableMap(n -> n * 2, identity())),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableMap(Integer::valueOf, n -> n)),
        Streams.stream(ImmutableList.of(3).iterator())
            .collect(toImmutableMap(n -> n.intValue(), identity())));
  }

  @Template(TransformMapValuesToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testTransformMapValuesToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1L).entrySet().stream()
            .collect(toImmutableMap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue()))),
        Maps.toMap(
            ImmutableMap.of("bar", 2L).keySet(),
            k -> Math.toIntExact(ImmutableMap.of("bar", 2L).get(k))));
  }

  @Template(ImmutableMapCopyOfImmutableMap.class)
  ImmutableMap<String, Integer> testImmutableMapCopyOfImmutableMap() {
    return ImmutableMap.copyOf(ImmutableMap.of("foo", 1));
  }
}
