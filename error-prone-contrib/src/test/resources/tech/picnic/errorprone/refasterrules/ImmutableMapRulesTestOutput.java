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

  ImmutableMap<Object, Object> testImmutableMapBuilderBuildOrThrow() {
    return ImmutableMap.builder().buildOrThrow();
  }

  ImmutableSet<ImmutableMap<String, Integer>> testImmutableMapOfMapEntryGetKeyMapEntryGetValue() {
    return ImmutableSet.of(
        ImmutableMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableMap.of(Map.entry("bar", 2).getKey(), Map.entry("bar", 2).getValue()));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testMapsToMap() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1).iterator())
            .collect(toImmutableMap(k -> 0, n -> n.intValue())),
        Maps.toMap(ImmutableList.of(2).iterator(), n -> n.intValue()),
        Streams.stream(ImmutableList.of(3)::iterator)
            .collect(toImmutableMap(k -> 0, Integer::valueOf)),
        Maps.toMap(ImmutableList.of(4)::iterator, Integer::valueOf),
        ImmutableList.of(5).stream().collect(toImmutableMap(k -> 0, n -> n * 2)),
        Maps.toMap(ImmutableList.of(6), n -> n * 2),
        Maps.toMap(ImmutableSet.of(7), Integer::valueOf));
  }

  ImmutableSet<Map<String, Integer>> testImmutableMapCopyOf() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1)),
        ImmutableMap.copyOf(ImmutableMap.of("bar", 2)),
        ImmutableMap.copyOf(ImmutableMap.of("baz", 3)),
        ImmutableMap.copyOf(ImmutableMap.of("qux", 4).entrySet()),
        ImmutableMap.copyOf(Iterables.cycle(Map.entry("corge", 6))),
        ImmutableMap.copyOf(ImmutableMap.of("quux", 5).entrySet()));
  }

  ImmutableMap<Integer, String> testStreamCollectToImmutableMap() {
    return Stream.of(1, 2, 3).collect(toImmutableMap(n -> n, n -> n.toString()));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testMapsUniqueIndex() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1).iterator())
            .collect(toImmutableMap(n -> n.intValue(), v -> 0)),
        Maps.uniqueIndex(ImmutableList.of(2).iterator(), n -> n.intValue()),
        Streams.stream(ImmutableList.of(3)::iterator)
            .collect(toImmutableMap(Integer::valueOf, v -> 0)),
        Maps.uniqueIndex(ImmutableList.of(4)::iterator, Integer::valueOf),
        ImmutableList.of(5).stream().collect(toImmutableMap(n -> n * 2, v -> 0)),
        Maps.uniqueIndex(ImmutableList.of(6), n -> n * 2));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testImmutableMapCopyOfMapsTransformValues() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("foo", 1L), v -> Math.toIntExact(v))),
        ImmutableMap.copyOf(
            Maps.transformValues(ImmutableMap.of("bar", 2L), v -> Math.toIntExact(v))));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf() {
    return ImmutableSet.of(
        ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf1() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", "bar"),
        ImmutableMap.of("baz", "qux"),
        ImmutableMap.of("quux", "corge"),
        ImmutableMap.of("grault", "garply"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf2() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", "bar", "baz", "qux"),
        ImmutableMap.of("quux", "corge", "grault", "garply"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf3() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", "bar", "baz", "qux", "quux", "corge"),
        ImmutableMap.of("grault", "garply", "waldo", "fred", "plugh", "xyzzy"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf4() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", "bar", "baz", "qux", "quux", "corge", "grault", "garply"),
        ImmutableMap.of("waldo", "fred", "plugh", "xyzzy", "thud", "foo", "bar", "baz"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf5() {
    return ImmutableSet.of(
        ImmutableMap.of(
            "foo", "bar", "baz", "qux", "quux", "corge", "grault", "garply", "waldo", "fred"),
        ImmutableMap.of(
            "plugh", "xyzzy", "thud", "foo", "bar", "baz", "qux", "quux", "corge", "grault"));
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterKeys() {
    return ImmutableMap.copyOf(Maps.filterKeys(ImmutableMap.of("foo", 1), k -> k.length() > 1));
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterValues() {
    return ImmutableMap.copyOf(Maps.filterValues(ImmutableMap.of("foo", 1), v -> v > 0));
  }

  ImmutableSet<Map<String, Integer>> testImmutableMapOfEntries() {
    return ImmutableSet.of(
        ImmutableMap.ofEntries(),
        ImmutableMap.ofEntries(Map.entry("foo", 1)),
        ImmutableMap.ofEntries(Map.entry("bar", 2), Map.entry("baz", 3)));
  }

  ImmutableSet<ImmutableMap.Builder<String, Integer>> testImmutableMapBuilderPut() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put("foo", 1),
        ImmutableMap.<String, Integer>builder().put("bar", 2));
  }
}
