package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSortedMapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Stream.class, Streams.class, naturalOrder(), toImmutableSortedMap(null, null));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapOrderedBy() {
    return ImmutableSortedMap.orderedBy(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrder() {
    return ImmutableSortedMap.naturalOrder();
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrder() {
    return ImmutableSortedMap.reverseOrder();
  }

  ImmutableSortedMap<String, Integer> testImmutableSortedMapOf() {
    return ImmutableSortedMap.of();
  }

  ImmutableSortedMap<String, Integer> testImmutableSortedMapOfWithComparableAndObject() {
    return ImmutableSortedMap.of("foo", 1);
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>>
      testImmutableSortedMapOfEntryGetKeyEntryGetValue() {
    return ImmutableSet.of(
        ImmutableSortedMap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSortedMap.of(Map.entry("bar", 2).getKey(), Map.entry("bar", 2).getValue()));
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>> testImmutableSortedMapCopyOf() {
    return ImmutableSet.of(
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1)),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("bar", 2)),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("baz", 3)),
        ImmutableSortedMap.copyOf(Iterables.cycle(Map.entry("qux", 4))),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("quux", 5).entrySet()),
        ImmutableSortedMap.copyOf(Iterables.cycle(Map.entry("corge", 6))),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("grault", 7).entrySet()));
  }

  Collector<Integer, ?, ImmutableSortedMap<String, Double>> testToImmutableSortedMap() {
    return toImmutableSortedMap(String::valueOf, Double::valueOf);
  }

  Collector<Integer, ?, ImmutableSortedMap<String, Double>>
      testToImmutableSortedMapWithBinaryOperator() {
    return toImmutableSortedMap(String::valueOf, Double::valueOf, Double::sum);
  }
}
