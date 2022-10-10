package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class ImmutableSortedMapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Stream.class, Streams.class, naturalOrder(), toImmutableSortedMap(null, null, null));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
    return ImmutableSortedMap.naturalOrder();
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
    return ImmutableSortedMap.reverseOrder();
  }

  ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
    return ImmutableSortedMap.of();
  }

  ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
    return ImmutableSortedMap.of("foo", 1);
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

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
