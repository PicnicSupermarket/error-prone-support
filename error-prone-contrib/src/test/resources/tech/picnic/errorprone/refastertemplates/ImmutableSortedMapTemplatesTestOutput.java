package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.EmptyImmutableSortedMap;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.EntryToImmutableSortedMap;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.ImmutableSortedMapBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.ImmutableSortedMapNaturalOrderBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.ImmutableSortedMapReverseOrderBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.IterableToImmutableSortedMap;
import tech.picnic.errorprone.refastertemplates.ImmutableSortedMapTemplates.PairToImmutableSortedMap;

@TemplateCollection(ImmutableSortedMapTemplates.class)
final class ImmutableSortedMapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Stream.class, Streams.class, naturalOrder(), toImmutableSortedMap(null, null, null));
  }

  @Template(ImmutableSortedMapBuilder.class)
  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.comparingInt(String::length));
  }

  @Template(ImmutableSortedMapNaturalOrderBuilder.class)
  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
    return ImmutableSortedMap.naturalOrder();
  }

  @Template(ImmutableSortedMapReverseOrderBuilder.class)
  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
    return ImmutableSortedMap.reverseOrder();
  }

  @Template(EmptyImmutableSortedMap.class)
  ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
    return ImmutableSortedMap.of();
  }

  @Template(PairToImmutableSortedMap.class)
  ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
    return ImmutableSortedMap.of("foo", 1);
  }

  @Template(EntryToImmutableSortedMap.class)
  ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  @Template(IterableToImmutableSortedMap.class)
  ImmutableSet<ImmutableSortedMap<String, Integer>> testIterableToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
        ImmutableSortedMap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }
}
