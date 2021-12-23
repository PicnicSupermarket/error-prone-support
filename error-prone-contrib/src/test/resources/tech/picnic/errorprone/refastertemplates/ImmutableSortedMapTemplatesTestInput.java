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
    return new ImmutableSortedMap.Builder<>(Comparator.comparingInt(String::length));
  }

  @Template(ImmutableSortedMapNaturalOrderBuilder.class)
  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>naturalOrder());
  }

  @Template(ImmutableSortedMapReverseOrderBuilder.class)
  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>reverseOrder());
  }

  @Template(EmptyImmutableSortedMap.class)
  ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().build();
  }

  @Template(PairToImmutableSortedMap.class)
  ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().put("foo", 1).build();
  }

  @Template(EntryToImmutableSortedMap.class)
  ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.<String, Integer>naturalOrder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Template(IterableToImmutableSortedMap.class)
  ImmutableSet<ImmutableSortedMap<String, Integer>> testIterableToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1), naturalOrder()),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("foo", 1))
            .build(),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("foo", 1).entrySet())
            .build(),
        ImmutableSortedMap.of("foo", 1).entrySet().stream()
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }
}
