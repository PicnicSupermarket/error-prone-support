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
    return new ImmutableMap.Builder<>();
  }

  ImmutableMap<Object, Object> testImmutableMapBuilderBuildOrThrow() {
    return ImmutableMap.builder().build();
  }

  ImmutableSet<ImmutableMap<String, Integer>> testImmutableMapOfMapEntryGetKeyMapEntryGetValue() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put(Map.entry("foo", 1)).buildOrThrow(),
        Stream.of(Map.entry("bar", 2))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testMapsToMap() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1).iterator())
            .collect(toImmutableMap(k -> 0, n -> n.intValue())),
        Streams.stream(ImmutableList.of(2).iterator())
            .collect(toImmutableMap(identity(), n -> n.intValue())),
        Streams.stream(ImmutableList.of(3)::iterator)
            .collect(toImmutableMap(k -> 0, Integer::valueOf)),
        Streams.stream(ImmutableList.of(4)::iterator)
            .collect(toImmutableMap(identity(), Integer::valueOf)),
        ImmutableList.of(5).stream().collect(toImmutableMap(k -> 0, n -> n * 2)),
        ImmutableList.of(6).stream().collect(toImmutableMap(identity(), n -> n * 2)),
        ImmutableMap.copyOf(Maps.asMap(ImmutableSet.of(7), Integer::valueOf)));
  }

  ImmutableSet<Map<String, Integer>> testImmutableMapCopyOf() {
    return ImmutableSet.of(
        ImmutableMap.copyOf(ImmutableMap.of("foo", 1).entrySet()),
        ImmutableMap.<String, Integer>builder().putAll(ImmutableMap.of("bar", 2)).buildOrThrow(),
        Map.copyOf(ImmutableMap.of("baz", 3)),
        ImmutableMap.<String, Integer>builder()
            .putAll(ImmutableMap.of("qux", 4).entrySet())
            .buildOrThrow(),
        Streams.stream(Iterables.cycle(Map.entry("corge", 6)))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)),
        ImmutableMap.of("quux", 5).entrySet().stream()
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableMap<Integer, String> testStreamCollectToImmutableMap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testMapsUniqueIndex() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1).iterator())
            .collect(toImmutableMap(n -> n.intValue(), v -> 0)),
        Streams.stream(ImmutableList.of(2).iterator())
            .collect(toImmutableMap(n -> n.intValue(), identity())),
        Streams.stream(ImmutableList.of(3)::iterator)
            .collect(toImmutableMap(Integer::valueOf, v -> 0)),
        Streams.stream(ImmutableList.of(4)::iterator)
            .collect(toImmutableMap(Integer::valueOf, identity())),
        ImmutableList.of(5).stream().collect(toImmutableMap(n -> n * 2, v -> 0)),
        ImmutableList.of(6).stream().collect(toImmutableMap(n -> n * 2, identity())));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testImmutableMapCopyOfMapsTransformValues() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1L).entrySet().stream()
            .collect(toImmutableMap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue()))),
        Maps.toMap(
            ImmutableMap.of("bar", 2L).keySet(),
            k -> Math.toIntExact(ImmutableMap.of("bar", 2L).get(k))));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf() {
    return ImmutableSet.of(
        ImmutableMap.<String, String>builder().buildOrThrow(),
        ImmutableMap.ofEntries(),
        Collections.<String, String>emptyMap(),
        Map.<String, String>of());
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf1() {
    return ImmutableSet.of(
        ImmutableMap.<String, String>builder().put("foo", "bar").buildOrThrow(),
        ImmutableMap.ofEntries(Map.entry("baz", "qux")),
        Collections.singletonMap("quux", "corge"),
        Map.of("grault", "garply"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf2() {
    return ImmutableSet.of(
        ImmutableMap.ofEntries(Map.entry("foo", "bar"), Map.entry("baz", "qux")),
        Map.of("quux", "corge", "grault", "garply"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf3() {
    return ImmutableSet.of(
        ImmutableMap.ofEntries(
            Map.entry("foo", "bar"), Map.entry("baz", "qux"), Map.entry("quux", "corge")),
        Map.of("grault", "garply", "waldo", "fred", "plugh", "xyzzy"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf4() {
    return ImmutableSet.of(
        ImmutableMap.ofEntries(
            Map.entry("foo", "bar"),
            Map.entry("baz", "qux"),
            Map.entry("quux", "corge"),
            Map.entry("grault", "garply")),
        Map.of("waldo", "fred", "plugh", "xyzzy", "thud", "foo", "bar", "baz"));
  }

  ImmutableSet<Map<String, String>> testImmutableMapOf5() {
    return ImmutableSet.of(
        ImmutableMap.ofEntries(
            Map.entry("foo", "bar"),
            Map.entry("baz", "qux"),
            Map.entry("quux", "corge"),
            Map.entry("grault", "garply"),
            Map.entry("waldo", "fred")),
        Map.of("plugh", "xyzzy", "thud", "foo", "bar", "baz", "qux", "quux", "corge", "grault"));
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterKeys() {
    return ImmutableMap.of("foo", 1).entrySet().stream()
        .filter(entry -> entry.getKey().length() > 1)
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableMap<String, Integer> testImmutableMapCopyOfMapsFilterValues() {
    return ImmutableMap.of("foo", 1).entrySet().stream()
        .filter(entry -> entry.getValue() > 0)
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSet<Map<String, Integer>> testImmutableMapOfEntries() {
    return ImmutableSet.of(
        Map.ofEntries(),
        Map.ofEntries(Map.entry("foo", 1)),
        Map.ofEntries(Map.entry("bar", 2), Map.entry("baz", 3)));
  }

  ImmutableSet<ImmutableMap.Builder<String, Integer>> testImmutableMapBuilderPut() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put(Map.entry("foo", 1)),
        ImmutableMap.<String, Integer>builder().putAll(ImmutableMap.of("bar", 2)));
  }
}
