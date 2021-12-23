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
    return ImmutableMap.builder();
  }

  @Template(EmptyImmutableMap.class)
  ImmutableMap<String, Integer> testEmptyImmutableMap() {
    return ImmutableMap.of();
  }

  @Template(PairToImmutableMap.class)
  ImmutableSet<Map<String, Integer>> testPairToImmutableMap() {
    return ImmutableSet.of(ImmutableMap.of("foo", 1), ImmutableMap.of("bar", 2));
  }

  @Template(EntryToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  @Template(IterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToImmutableMap() {
    return ImmutableSet.of(
        Maps.toMap(ImmutableList.of(1), n -> n * 2),
        Maps.toMap(ImmutableList.of(2)::iterator, Integer::valueOf),
        Maps.toMap(ImmutableList.of(3).iterator(), n -> n.intValue()),
        Maps.toMap(ImmutableSet.of(4), Integer::valueOf));
  }

  @Template(EntryIterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testEntryIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  @Template(StreamOfMapEntriesToImmutableMap.class)
  ImmutableMap<Integer, String> testStreamOfMapEntriesToImmutableMap() {
    return Stream.of(1, 2, 3).collect(toImmutableMap(n -> n, n -> n.toString()));
  }

  @Template(IndexIterableToImmutableMap.class)
  ImmutableSet<ImmutableMap<Integer, Integer>> testIndexIterableToImmutableMap() {
    return ImmutableSet.of(
        Maps.uniqueIndex(ImmutableList.of(1), n -> n * 2),
        Maps.uniqueIndex(ImmutableList.of(2)::iterator, Integer::valueOf),
        Maps.uniqueIndex(ImmutableList.of(3).iterator(), n -> n.intValue()));
  }

  @Template(TransformMapValuesToImmutableMap.class)
  ImmutableSet<ImmutableMap<String, Integer>> testTransformMapValuesToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("foo", 1L), v -> Math.toIntExact(v))),
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("bar", 2L), v -> Math.toIntExact(v))));
  }

  @Template(ImmutableMapCopyOfImmutableMap.class)
  ImmutableMap<String, Integer> testImmutableMapCopyOfImmutableMap() {
    return ImmutableMap.of("foo", 1);
  }
}
