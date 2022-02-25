package tech.picnic.errorprone.bugpatterns;

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

final class ImmutableMapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class, Streams.class, identity());
  }

  ImmutableMap.Builder<String, Integer> testImmutableMapBuilder() {
    return new ImmutableMap.Builder<>();
  }

  ImmutableMap<String, Integer> testEmptyImmutableMap() {
    return ImmutableMap.<String, Integer>builder().build();
  }

  ImmutableSet<Map<String, Integer>> testPairToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put("foo", 1).build(),
        Collections.singletonMap("bar", 2));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testEntryToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableMap(identity(), n -> n * 2)),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableMap(n -> n, Integer::valueOf)),
        Streams.stream(ImmutableList.of(3).iterator())
            .collect(toImmutableMap(identity(), n -> n.intValue())),
        ImmutableMap.copyOf(Maps.asMap(ImmutableSet.of(4), Integer::valueOf)));
  }

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

  ImmutableMap<Integer, String> testStreamOfMapEntriesToImmutableMap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  ImmutableSet<ImmutableMap<Integer, Integer>> testIndexIterableToImmutableMap() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableMap(n -> n * 2, identity())),
        Streams.stream(ImmutableList.of(2)::iterator)
            .collect(toImmutableMap(Integer::valueOf, n -> n)),
        Streams.stream(ImmutableList.of(3).iterator())
            .collect(toImmutableMap(n -> n.intValue(), identity())));
  }

  ImmutableSet<ImmutableMap<String, Integer>> testTransformMapValuesToImmutableMap() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1L).entrySet().stream()
            .collect(toImmutableMap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue()))),
        Maps.toMap(
            ImmutableMap.of("bar", 2L).keySet(),
            k -> Math.toIntExact(ImmutableMap.of("bar", 2L).get(k))));
  }

  Map<Object, Object> testImmutableMapOf() {
    return Collections.emptyMap();
  }

  Map<String, String> testImmutableMapOf1() {
    return Map.of("k1", "v1");
  }

  Map<String, String> testImmutableMapOf2() {
    return Map.of("k1", "v1", "k2", "v2");
  }

  Map<String, String> testImmutableMapOf3() {
    return Map.of("k1", "v1", "k2", "v2", "k3", "v3");
  }

  Map<String, String> testImmutableMapOf4() {
    return Map.of("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4");
  }

  Map<String, String> testImmutableMapOf5() {
    return Map.of("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4", "k5", "v5");
  }
}
