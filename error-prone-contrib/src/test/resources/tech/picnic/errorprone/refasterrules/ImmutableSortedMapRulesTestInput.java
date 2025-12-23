package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSortedMapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Collections.class,
        Stream.class,
        Streams.class,
        naturalOrder(),
        toImmutableSortedMap(null, null, null));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapBuilder() {
    return new ImmutableSortedMap.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrderBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrderBuilder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableSortedMap<String, Integer> testEmptyImmutableSortedMap() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().buildOrThrow();
  }

  ImmutableSortedMap<String, Integer> testPairToImmutableSortedMap() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().put("foo", 1).buildOrThrow();
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>> testEntryToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.<String, Integer>naturalOrder().put(Map.entry("foo", 1)).buildOrThrow(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>> testIterableToImmutableSortedMap() {
    return ImmutableSet.of(
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1), naturalOrder()),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1).entrySet()),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("foo", 1))
            .buildOrThrow(),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("foo", 1).entrySet())
            .buildOrThrow(),
        ImmutableSortedMap.of("foo", 1).entrySet().stream()
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableSortedMap.Builder<String, Integer>>
      testImmutableSortedMapBuilderPutOverPutAllSingleEntry() {
    return ImmutableSet.of(
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(Collections.singletonMap("key", 1)),
        ImmutableSortedMap.<String, Integer>naturalOrder().putAll(Map.of("key", 2)));
  }
}
