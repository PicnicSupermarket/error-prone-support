package tech.picnic.errorprone.refasterrules;

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
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableMapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class, Streams.class, identity());
  }

  ImmutableMap.Builder<String, Integer> testImmutableMapBuilder() {
    return ImmutableMap.builder();
  }

  ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToImmutableMap() {
    return ImmutableSet.of(
        Maps.toMap(ImmutableList.of(1), n -> n * 2),
        Maps.toMap(ImmutableList.of(2), n -> n * 2),
        ImmutableList.of(3).stream().collect(toImmutableMap(k -> 0, n -> n * 2)),
        Maps.toMap(ImmutableList.of(4)::iterator, Integer::valueOf),
        Maps.toMap(ImmutableList.of(5)::iterator, Integer::valueOf),
        Streams.stream(ImmutableList.of(6)::iterator)
            .collect(toImmutableMap(k -> 0, Integer::valueOf)),
        Maps.toMap(ImmutableList.of(7).iterator(), n -> n.intValue()),
        Maps.toMap(ImmutableList.of(8).iterator(), n -> n.intValue()),
        Streams.stream(ImmutableList.of(9).iterator())
            .collect(toImmutableMap(k -> 0, n -> n.intValue())),
        Maps.toMap(ImmutableSet.of(10), Integer::valueOf));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testEntryIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  ImmutableMap<Integer, String> testStreamOfMapEntriesToImmutableMap() {
    return Stream.of(1, 2, 3).collect(toImmutableMap(n -> n, n -> n.toString()));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testIndexIterableToImmutableMap() {
    return ImmutableSet.of(
        Maps.uniqueIndex(ImmutableList.of(1), n -> n * 2),
        Maps.uniqueIndex(ImmutableList.of(2), n -> n * 2),
        ImmutableList.of(3).stream().collect(toImmutableMap(n -> n * 2, v -> 0)),
        Maps.uniqueIndex(ImmutableList.of(4)::iterator, Integer::valueOf),
        Maps.uniqueIndex(ImmutableList.of(5)::iterator, Integer::valueOf),
        Streams.stream(ImmutableList.of(6)::iterator)
            .collect(toImmutableMap(Integer::valueOf, v -> 0)),
        Maps.uniqueIndex(ImmutableList.of(7).iterator(), n -> n.intValue()),
        Maps.uniqueIndex(ImmutableList.of(8).iterator(), n -> n.intValue()),
        Streams.stream(ImmutableList.of(9).iterator())
            .collect(toImmutableMap(n -> n.intValue(), v -> 0)));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testTransformMapValuesToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("foo", 1L), v -> Math.toIntExact(v))),
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("bar", 2L), v -> Math.toIntExact(v))));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf() {
    return ImmutableSet.of(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf1() {
    return ImmutableSet.of(
        ImmutableMap.of("k1", "v1"), ImmutableMap.of("k1", "v1"), ImmutableMap.of("k1", "v1"));
  }

  Map<String, String> testImmutableMapOf2() {
    return ImmutableMap.of("k1", "v1", "k2", "v2");
  }

  Map<String, String> testImmutableMapOf3() {
    return ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3");
  }

  Map<String, String> testImmutableMapOf4() {
    return ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4");
  }

  Map<String, String> testImmutableMapOf5() {
    return ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4", "k5", "v5");
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterKeys() {
    return ImmutableMap.copyOf(Maps.filterKeys(ImmutableMap.of("foo", 1), k -> k.length() > 1));
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterValues() {
    return ImmutableMap.copyOf(Maps.filterValues(ImmutableMap.of("foo", 1), v -> v > 0));
  }
}
